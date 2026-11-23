/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.GameAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Voice understanding (Chris, Nov 23: "a model that understands voice controls"). Dev build only.
 * Turns everyday phrasing into game steps using only what the player can see: visible enemies, the
 * items in the bag by their shown names, stairs the hero has found. No network, no AI download.
 *
 * Examples: "north three times", "go east then north", "attack the rat", "drink a healing potion",
 * "eat", "read identify", "zap frost at the gnoll", "throw a dart", "go downstairs", "pick up", "rest".
 * A multi-step plan stops by itself as soon as a new enemy comes into view.
 */
public final class VoiceBrain {
	private VoiceBrain(){}

	// ---------- steps ----------
	abstract static class Step { abstract boolean run(); String label(){ return ""; } }

	static final class KeyStep extends Step {
		final GameAction action; KeyStep(GameAction a){ action = a; }
		boolean run(){ VoiceCommands.press(action); return true; }
	}
	static final class MoveStep extends Step {
		final int dx, dy; MoveStep(int dx, int dy){ this.dx = dx; this.dy = dy; }
		boolean run(){
			Hero h = Dungeon.hero; int w = Dungeon.level.width();
			int x = h.pos % w + dx, y = h.pos / w + dy;
			if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()) return false;
			GameScene.handleCell(x + y * w); return true;
		}
	}
	static final class CellStep extends Step {
		final int cell; CellStep(int c){ cell = c; }
		boolean run(){ GameScene.handleCell(cell); return true; }
	}
	static final class ItemStep extends Step {
		final Item item; final String action; ItemStep(Item i, String a){ item = i; action = a; }
		boolean run(){
			if (!Dungeon.hero.belongings.contains(item)) return false;
			item.execute(Dungeon.hero, action); return true;
		}
	}
	/** Picks the target for a zap/throw that is waiting for one. */
	static final class TargetStep extends Step {
		final Mob mob; TargetStep(Mob m){ mob = m; }
		boolean run(){
			if (!GameScene.apdTargeting()) return false;
			if (mob == null || !mob.isAlive() || !Dungeon.level.heroFOV[mob.pos]){ GameScene.cancelCellSelector(); return false; }
			GameScene.handleCell(mob.pos); return true;
		}
	}

	// ---------- executor ----------
	private static final LinkedList<Step> queue = new LinkedList<>();
	private static int enemiesAtStart;
	private static long waitingSince;
	private static boolean justIssued;

	/** Called every frame from GameScene.update (render thread). */
	public static void tick(){
		if (queue.isEmpty() || Dungeon.hero == null || Dungeon.level == null) return;
		if (justIssued){ justIssued = false; return; } // let the issued action start first
		if (queue.size() > 0 && visibleEnemies().size() > enemiesAtStart){
			queue.clear(); GLog.w("Voice: stopped, an enemy came into view."); return;
		}
		Step next = queue.peek();
		boolean targeting = next instanceof TargetStep;
		if (!Dungeon.hero.ready || (!targeting && GameScene.interfaceBlockingHero()) || (targeting && !GameScene.apdTargeting())){
			if (System.currentTimeMillis() - waitingSince > 8000){ queue.clear(); }
			return;
		}
		queue.poll();
		waitingSince = System.currentTimeMillis();
		justIssued = true;
		if (!next.run()) queue.clear();
	}

	static void start(List<Step> plan){
		queue.clear();
		queue.addAll(plan);
		enemiesAtStart = visibleEnemies().size();
		waitingSince = System.currentTimeMillis();
		justIssued = false;
	}

	/** Render thread: understand the recognizer's guesses (best first) and run the first one that makes sense. */
	public static void understand(List<String> guesses){
		if (guesses == null || guesses.isEmpty()) return;
		for (String g : guesses){
			Plan p = plan(g);
			if (p == null) continue;
			if (p.stop){ VoiceCommands.stopFromVoice(); GLog.i("Voice: stopped listening."); return; }
			if (Dungeon.hero == null){
				// outside a run only simple keys make sense
				for (Step s : p.steps) if (s instanceof KeyStep) s.run();
				return;
			}
			start(p.steps);
			GLog.i("Voice: " + p.said);
			return;
		}
		GLog.w("Voice: didn't understand \"" + guesses.get(0) + "\"");
	}

	// ---------- understanding ----------
	static final class Plan {
		final List<Step> steps = new ArrayList<>(); final StringBuilder said = new StringBuilder(); boolean stop;
		void add(Step s, String what){ steps.add(s); if (what != null){ if (said.length() > 0) said.append(", "); said.append(what); } }
	}

	static final String[] VOCAB = {"north","south","east","west","northeast","northwest","southeast","southwest",
			"attack","fight","kill","drink","quaff","read","eat","zap","throw","wear","wield","equip","potion","scroll",
			"wand","rest","wait","pick","continue","resume","inventory","journal","downstairs","upstairs","descend","ascend",
			"stairs","slot","times","examine","cancel","listening","healing","identify","upgrade","teleport"};

	private static final String[][] NUMBERS = {{"one","1"},{"once","1"},{"two","2"},{"twice","2"},{"three","3"},{"thrice","3"},
			{"four","4"},{"five","5"},{"six","6"},{"seven","7"},{"eight","8"},{"nine","9"},{"ten","10"}};

	static int distance(String a, String b){
		int[] prev = new int[b.length()+1], cur = new int[b.length()+1];
		for (int j = 0; j <= b.length(); j++) prev[j] = j;
		for (int i = 1; i <= a.length(); i++){
			cur[0] = i;
			for (int j = 1; j <= b.length(); j++)
				cur[j] = Math.min(Math.min(cur[j-1]+1, prev[j]+1), prev[j-1] + (a.charAt(i-1)==b.charAt(j-1)?0:1));
			int[] t = prev; prev = cur; cur = t;
		}
		return prev[b.length()];
	}

	/** Lowercase, numbers as digits, common mishearings fixed, close misspellings snapped to game words. */
	static String clean(String s){
		String n = VoiceCommands.normalize(s);
		n = (" " + n + " ").replaceAll(" (to|too) (times) ", " 2 times ").replaceAll(" for (times) ", " 4 times ")
				.replaceAll(" slot (to|too) ", " slot 2 ").replaceAll(" slot for ", " slot 4 ").trim();
		StringBuilder out = new StringBuilder();
		for (String w : n.split(" ")){
			if (w.isEmpty()) continue;
			for (String[] num : NUMBERS) if (w.equals(num[0])){ w = num[1]; break; }
			if (w.length() >= 4 && !w.matches("\\d+")){
				String best = w; int bestD = w.length() >= 7 ? 2 : 1;
				for (String v : VOCAB){
					if (v.equals(w)){ best = w; bestD = -1; break; }
					int d = distance(w, v);
					if (d <= bestD && d < (best == w ? Integer.MAX_VALUE : distance(w, best))) best = v;
				}
				w = best;
			}
			if (out.length() > 0) out.append(' ');
			out.append(w);
		}
		return out.toString().replace("north east","northeast").replace("north west","northwest")
				.replace("south east","southeast").replace("south west","southwest")
				.replace("down stairs","downstairs").replace("up stairs","upstairs");
	}

	static List<String> clauses(String cleaned){
		List<String> out = new ArrayList<>();
		for (String c : cleaned.split(" (and then|then|after that|and) ")){
			c = c.trim(); if (!c.isEmpty()) out.add(c);
		}
		return out;
	}

	/** "north 3 times", "3 north", "north x3", "north three" -> 3; otherwise 1. Capped at 9 for safety. */
	static int count(String clause){
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:^| )(\\d{1,2})(?: times| steps| spaces|$| )").matcher(clause);
		int n = 1;
		if (m.find()) n = Integer.parseInt(m.group(1));
		return Math.max(1, Math.min(9, n));
	}

	private static final String[][] DIRS = {
			{"northeast","1","-1"},{"northwest","-1","-1"},{"southeast","1","1"},{"southwest","-1","1"},
			{"up right","1","-1"},{"up left","-1","-1"},{"down right","1","1"},{"down left","-1","1"},
			{"north","0","-1"},{"south","0","1"},{"east","1","0"},{"west","-1","0"},
			{"up","0","-1"},{"down","0","1"},{"right","1","0"},{"left","-1","0"}};

	static int[] direction(String clause){
		String c = " " + clause + " ";
		for (String[] d : DIRS) if (c.contains(" " + d[0] + " ")) return new int[]{Integer.parseInt(d[1]), Integer.parseInt(d[2])};
		return null;
	}

	private static final Set<String> FILLER = new HashSet<>(Arrays.asList("a","an","the","my","of","at","on","to","some",
			"please","go","use","that","this","it","with","one","potion","scroll","wand","nearest","closest"));

	/** Words left after the verb, minus filler; used to match item and enemy names. */
	static List<String> keywords(String text){
		List<String> out = new ArrayList<>();
		for (String w : text.split(" ")) if (!w.isEmpty() && !FILLER.contains(w) && !w.matches("\\d+")) out.add(w);
		return out;
	}

	/** How well spoken keywords match a shown name: every keyword must appear (allowing small slips). */
	static int nameScore(List<String> words, String shownName){
		if (words.isEmpty()) return 1;
		String[] parts = VoiceCommands.normalize(shownName).split(" ");
		int score = 0;
		for (String w : words){
			boolean hit = false;
			for (String p : parts){
				if (p.equals(w) || (p.startsWith(w) && w.length() >= 3) || (w.length() >= 5 && distance(p, w) <= 1)){ hit = true; break; }
			}
			if (!hit) return 0;
			score += 2;
		}
		return score;
	}

	static ArrayList<Mob> visibleEnemies(){
		ArrayList<Mob> out = new ArrayList<>();
		if (Dungeon.level == null || Dungeon.level.heroFOV == null) return out;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
			if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && m.invisible <= 0 && Dungeon.level.heroFOV[m.pos]) out.add(m);
		}
		return out;
	}

	static Mob enemy(List<String> words){
		Mob best = null; int bestScore = 0; float bestDist = Float.MAX_VALUE;
		for (Mob m : visibleEnemies()){
			int s = nameScore(words, m.name());
			float d = Dungeon.level.trueDistance(Dungeon.hero.pos, m.pos);
			if (s > bestScore || (s == bestScore && s > 0 && d < bestDist)){ best = m; bestScore = s; bestDist = d; }
		}
		return bestScore > 0 ? best : null;
	}

	static Item item(List<String> words, Class<? extends Item> kind){
		Item best = null; int bestScore = 0;
		for (Item i : Dungeon.hero.belongings){
			if (kind != null && !kind.isInstance(i)) continue;
			int s = nameScore(words, i.name());
			if (s > bestScore){ best = i; bestScore = s; }
		}
		return best;
	}

	private static boolean known(int cell){
		return cell >= 0 && (Dungeon.level.visited[cell] || Dungeon.level.mapped[cell]);
	}

	/** Pure language part: returns null when the phrase means nothing to the game. */
	static Plan plan(String heard){
		String cleaned = clean(heard);
		if (cleaned.isEmpty()) return null;
		Plan plan = new Plan();
		if ((" " + cleaned + " ").matches(".* (stop listening|voice off|stop voice) .*")){ plan.stop = true; return plan; }
		for (String clause : clauses(cleaned)){
			if (!planClause(clause, plan)) return null;
		}
		return plan.steps.isEmpty() ? null : plan;
	}

	private static String after(String clause, String... verbs){
		for (String v : verbs){
			int i = (" " + clause + " ").indexOf(" " + v + " ");
			if (i >= 0) return clause.substring(Math.min(clause.length(), i + v.length())).trim();
		}
		return null;
	}

	private static boolean planClause(String clause, Plan plan){
		boolean inRun = Dungeon.hero != null && Dungeon.level != null;
		String rest;

		// stairs
		if (clause.matches(".*\\b(downstairs|descend|next floor|go down the stairs|stairs down|take the stairs)\\b.*")){
			if (!inRun) return false;
			int exit = Dungeon.level.exit();
			if (!known(exit)){ GLog.w("Voice: you haven't found the way down yet."); return true; }
			plan.add(new CellStep(exit), "to the stairs down"); return true;
		}
		if (clause.matches(".*\\b(upstairs|ascend|previous floor|go up the stairs|stairs up)\\b.*")){
			if (!inRun) return false;
			int entrance = Dungeon.level.entrance();
			if (!known(entrance)) return false;
			plan.add(new CellStep(entrance), "to the stairs up"); return true;
		}

		// attack a named or the nearest enemy
		if ((rest = after(clause, "attack", "fight", "kill", "hit")) != null){
			if (!inRun) return false;
			List<String> words = keywords(rest);
			Mob m = enemy(words);
			if (m == null && !words.isEmpty()) m = enemy(new ArrayList<>());
			if (m == null){ plan.add(new KeyStep(SPDAction.TAG_ATTACK), "attack"); return true; }
			plan.add(new CellStep(m.pos), "attack " + m.name()); return true;
		}

		// items
		Object[][] verbs = {
				{new String[]{"drink","quaff"}, Potion.class, Potion.AC_DRINK, "drink"},
				{new String[]{"read"}, Scroll.class, Scroll.AC_READ, "read"},
				{new String[]{"eat"}, Food.class, Food.AC_EAT, "eat"},
				{new String[]{"wear","wield","equip","put on"}, EquipableItem.class, EquipableItem.AC_EQUIP, "equip"},
				{new String[]{"zap","cast"}, Wand.class, Wand.AC_ZAP, "zap"},
				{new String[]{"throw","toss"}, MissileWeapon.class, Item.AC_THROW, "throw"}};
		for (Object[] v : verbs){
			if ((rest = after(clause, (String[]) v[0])) == null) continue;
			if (!inRun) return false;
			String target = null;
			int at = (" " + rest + " ").indexOf(" at ");
			if (at >= 0){ target = rest.substring(Math.min(rest.length(), at + 3)).trim(); rest = rest.substring(0, Math.max(0, at)).trim(); }
			@SuppressWarnings("unchecked") Class<? extends Item> kind = (Class<? extends Item>) v[1];
			Item it = item(keywords(rest), kind);
			if (it == null){ GLog.w("Voice: no matching item for \"" + clause + "\""); return true; }
			plan.add(new ItemStep(it, (String) v[2]), v[3] + " " + it.name());
			if (v[2].equals(Wand.AC_ZAP) || v[2].equals(Item.AC_THROW)){
				Mob m = target == null ? null : enemy(keywords(target));
				if (m == null) m = enemy(new ArrayList<>());
				if (m != null) plan.add(new TargetStep(m), "at " + m.name());
			}
			return true;
		}

		// movement, with repeats
		int[] d = direction(clause);
		if (d != null && !clause.matches(".*\\b(zoom|slot)\\b.*")){
			if (!inRun) { VoiceCommands.Match m = VoiceCommands.parse(clause); if (m != null && m.action != null){ plan.add(new KeyStep(m.action), null); return true; } return false; }
			int n = count(clause);
			for (int i = 0; i < n; i++) plan.add(new MoveStep(d[0], d[1]), i == 0 ? clause.replaceAll("\\b(go|move|walk|step)\\b ?", "") : null);
			return true;
		}

		// everything else: the simple phrase table (wait, rest, pick up, inventory, slot 3, back...)
		VoiceCommands.Match m = VoiceCommands.parse(clause);
		if (m == null || m.action == null) return false;
		int n = (m.action == SPDAction.WAIT || m.action == SPDAction.WAIT_OR_PICKUP) ? count(clause) : 1;
		for (int i = 0; i < n; i++) plan.add(new KeyStep(m.action), i == 0 ? clause : null);
		return true;
	}
}
