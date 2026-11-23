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
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
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
	/** Picks the target for a zap/throw/ability that is waiting for one. A null mob means "use the lock-on rule". */
	static final class TargetStep extends Step {
		final Mob mob; final int[] dir;
		TargetStep(Mob m){ mob = m; dir = null; }
		TargetStep(int[] d){ mob = null; dir = d; }
		boolean run(){
			if (!GameScene.apdTargeting()) return true;
			Hero h = Dungeon.hero;
			if (dir != null){
				int w = Dungeon.level.width(), x = h.pos % w + dir[0], y = h.pos / w + dir[1];
				if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()){ GameScene.cancelCellSelector(); return false; }
				GameScene.handleCell(x + y * w); return true;
			}
			Char t = mob != null ? mob : lockOn();
			if (t == null || !t.isAlive() || !Dungeon.level.heroFOV[t.pos]){
				GameScene.cancelCellSelector(); GLog.w("Voice: no target in view."); return false;
			}
			QuickSlotButton.lastTarget = t;
			GameScene.handleCell(t.pos); return true;
		}
	}
	/** Attack an enemy once, or keep attacking until it dies ("kill"). */
	static final class AttackStep extends Step {
		final Mob mob; final boolean untilDead; int swings;
		AttackStep(Mob m, boolean u){ mob = m; untilDead = u; }
		boolean run(){
			if (mob == null || !mob.isAlive() || !Dungeon.level.heroFOV[mob.pos]) return true;
			QuickSlotButton.lastTarget = mob;
			GameScene.handleCell(mob.pos);
			if (untilDead && ++swings < 40) queue.addFirst(this);
			return true;
		}
	}
	/** The action button: Duelist weapon ability or whatever else is showing there. */
	static final class AbilityStep extends Step {
		boolean run(){
			if (ActionIndicator.action == null){ GLog.w("Voice: no ability is ready."); return false; }
			VoiceCommands.press(SPDAction.TAG_ACTION); return true;
		}
	}
	static final class SearchStep extends Step {
		boolean run(){ GameScene.cancelCellSelector(); Dungeon.hero.search(true); return true; }
	}

	/** Chris's lock-on rule (Nov 21): last target if still in reach, else closest in reach, else closest visible. */
	static Char lockOn(){
		Hero h = Dungeon.hero;
		if (h.belongings.weapon instanceof MeleeWeapon)
			return DuelistLockOn.pick(h, (MeleeWeapon) h.belongings.weapon, QuickSlotButton.lastTarget);
		return pickEnemy("");
	}

	// ---------- executor ----------
	private static final LinkedList<Step> queue = new LinkedList<>();
	private static int enemiesAtStart;
	private static long waitingSince;
	private static boolean justIssued;

	private static int hpAtStart;
	private static boolean fighting, multi;
	private static List<String> lastGuesses;

	/** Called every frame from GameScene.update (render thread). */
	public static void tick(){
		if (queue.isEmpty() || Dungeon.hero == null || Dungeon.level == null) return;
		if (justIssued){ justIssued = false; return; } // let the issued action start first
		Hero h = Dungeon.hero;
		if (!h.isAlive()){ queue.clear(); return; }
		if (visibleEnemies().size() > enemiesAtStart){ halt("an enemy came into view"); return; }
		if (fighting ? h.HP * 3 < h.HT : (multi && h.HP < hpAtStart)){ halt(fighting ? "your health is low" : "you got hurt"); return; }
		Step next = queue.peek();
		long now = System.currentTimeMillis();
		if (next instanceof TargetStep){
			if (!h.ready){ if (now - waitingSince > 8000) queue.clear(); return; }
			if (!GameScene.apdTargeting()){ if (now - waitingSince > 1200){ queue.poll(); waitingSince = now; } return; }
		} else if (!h.ready || GameScene.interfaceBlockingHero()){
			if (now - waitingSince > 8000) queue.clear();
			return;
		}
		queue.poll();
		waitingSince = now;
		justIssued = true;
		if (!next.run()) queue.clear();
	}

	private static void halt(String why){
		queue.clear();
		if (Dungeon.hero != null) Dungeon.hero.interrupt();
		GLog.w("Voice: stopped, " + why + ".");
	}

	static void start(Plan plan){
		queue.clear();
		queue.addAll(plan.steps);
		enemiesAtStart = visibleEnemies().size();
		hpAtStart = Dungeon.hero.HP;
		fighting = plan.fighting;
		multi = plan.steps.size() > 1;
		waitingSince = System.currentTimeMillis();
		justIssued = false;
	}

	static boolean inRun(){
		return scene() instanceof GameScene && Dungeon.hero != null && Dungeon.level != null;
	}

	private static com.watabou.noosa.Scene scene(){
		return Game.instance == null ? null : Game.scene();
	}

	/** Buttons the player can see right now: the top pop-up's, or the whole screen's when there is no pop-up. */
	static void buttons(Group g, List<StyledButton> out){
		for (Gizmo m : g.snapshot()){
			if (m == null || !m.isVisible() || !m.isActive()) continue;
			if (m instanceof StyledButton){ String t = ((StyledButton) m).text(); if (t != null && !t.trim().isEmpty()) out.add((StyledButton) m); }
			if (m instanceof Group) buttons((Group) m, out);
		}
	}

	/** Say the words on a button to press it. Returns true if one was pressed. */
	static boolean pressButton(String heard, boolean exactOnly){
		if (scene() == null) return false;
		Group root = scene();
		for (Gizmo m : scene().snapshot()) if (m instanceof Window && m.isVisible()) root = (Group) m;
		List<StyledButton> all = new ArrayList<>();
		buttons(root, all);
		String said = VoiceCommands.normalize(heard);
		if (said.isEmpty()) return false;
		List<String> words = keywords(said);
		StyledButton best = null; int bestScore = 0;
		for (StyledButton b : all){
			String label = VoiceCommands.normalize(b.text());
			int score = label.equals(said) ? 1000 : (exactOnly || words.isEmpty() ? 0 : nameScore(words, label) * 10 - label.split(" ").length);
			if (score > bestScore){ best = b; bestScore = score; }
		}
		if (best == null) return false;
		GLog.i("Voice: " + VoiceCommands.normalize(best.text()));
		best.apdVoiceClick();
		return true;
	}

	/** Render thread: understand the recognizer's guesses (best first) and run the first one that makes sense. */
	public static void understand(List<String> guesses){
		if (guesses == null || guesses.isEmpty()) return;
		String first = " " + clean(guesses.get(0)) + " ";
		if (isRepeat(first)){
			if (lastGuesses != null) understand(lastGuesses);
			return;
		}
		if (first.matches(" (stop|halt|hold on|never mind|nevermind|abort) ")){
			queue.clear();
			if (inRun()){ GameScene.cancelCellSelector(); Dungeon.hero.interrupt(); }
			GLog.i("Voice: stopped.");
			return;
		}
		boolean run = inRun();
		boolean popup = run && GameScene.showingWindow();
		// a pop-up or a menu: the words on a button press it
		if (!run || popup){
			for (String g : guesses) if (pressButton(g, false)) return;
		}
		for (String g : guesses){
			Plan p = plan(g);
			if (p == null) continue;
			if (p.stop){ VoiceCommands.stopFromVoice(); GLog.i("Voice: stopped listening."); return; }
			lastGuesses = new ArrayList<>(guesses);
			if (!run || popup){
				// outside a run, or with a pop-up open, only simple keys (back, inventory...) make sense
				for (Step s : p.steps) if (s instanceof KeyStep) s.run();
				return;
			}
			start(p);
			if (p.said.length() > 0) GLog.i("Voice: " + p.said);
			return;
		}
		if (run) GLog.w("Voice: didn't understand \"" + guesses.get(0) + "\"");
	}

	/** "again", "one more time"... on an already cleaned phrase (numbers are digits by then). */
	static boolean isRepeat(String cleaned){
		return (" " + cleaned.trim() + " ").matches(" (again|repeat|repeat that|do that again|same again|(one|1) more time) ");
	}

	// ---------- word list for the fast engine ----------
	/** Every word the fast engine may hear right now, space separated. Read from any thread. */
	public static volatile String vocabulary = "";

	private static final String CONTROL_WORDS = "stop halt hold on never mind nevermind abort again repeat that do same one more time "
			+ "listening voice off dungeon dungeons then and after times steps spaces until till dead dies "
			+ "one once two twice to too three thrice four for five six seven eight nine ten "
			+ "go move walk step use put on at of the a an my some please it this that with "
			+ "attack fight kill hit slay ability special weapon search here around look secrets "
			+ "drink quaff read eat wear wield equip zap cast throw toss get grab open loot pick up pickup take "
			+ "door item items pile gold key chest stuff stairs downstairs upstairs descend ascend next previous floor exit "
			+ "far farthest furthest other second different near nearest closest enemy monster side above below "
			+ "north south east west northeast northwest southeast southwest up down left right "
			+ "wait rest sleep continue resume action inventory backpack bag journal hero stats examine slot quick "
			+ "zoom in out cycle back cancel close yes no okay ok potion scroll wand ring armor sword";

	private static void addWords(java.util.TreeSet<String> out, String text){
		if (text == null) return;
		for (String w : VoiceCommands.normalize(text).split(" ")) if (w.matches("[a-z]{1,20}")) out.add(w);
	}

	/** Render thread: rebuild the word list from the fixed commands plus what is on screen and in the bag. */
	public static void refreshVocabulary(){
		java.util.TreeSet<String> out = new java.util.TreeSet<>();
		addWords(out, CONTROL_WORDS);
		for (String p : VoiceCommands.phrases()) addWords(out, p);
		for (String v : VOCAB) addWords(out, v);
		try {
			if (inRun()){
				for (Item i : Dungeon.hero.belongings) addWords(out, i.name());
				for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) addWords(out, m.name());
				for (Heap h : Dungeon.level.heaps.valueList()) if (h.seen && h.peek() != null) addWords(out, h.type == Heap.Type.HEAP ? h.peek().name() : h.title());
				if (ActionIndicator.action != null) addWords(out, ActionIndicator.action.actionName());
			}
			if (scene() != null && (!inRun() || GameScene.showingWindow())){
				Group root = scene();
				for (Gizmo m : scene().snapshot()) if (m instanceof Window && m.isVisible()) root = (Group) m;
				List<StyledButton> all = new ArrayList<>();
				buttons(root, all);
				for (StyledButton b : all) addWords(out, b.text());
			}
		} catch (RuntimeException ignored){
			// a half-built scene: keep the fixed words, the next refresh picks the rest up
		}
		StringBuilder sb = new StringBuilder();
		for (String w : out){ if (sb.length() > 0) sb.append(' '); sb.append(w); }
		vocabulary = sb.toString();
	}

	// ---------- understanding ----------
	static final class Plan {
		final List<Step> steps = new ArrayList<>(); final StringBuilder said = new StringBuilder(); boolean stop, fighting;
		void add(Step s, String what){ steps.add(s); if (what != null){ if (said.length() > 0) said.append(", "); said.append(what); } }
	}

	static final String[] VOCAB = {"north","south","east","west","northeast","northwest","southeast","southwest",
			"attack","fight","kill","drink","quaff","read","eat","zap","throw","wear","wield","equip","potion","scroll",
			"wand","rest","wait","pick","continue","resume","inventory","journal","downstairs","upstairs","descend","ascend",
			"stairs","slot","times","examine","cancel","listening","healing","identify","upgrade","teleport",
			"ability","special","search","again","repeat","door","farthest","furthest","closest","nearest","other","second","chest","item","loot",
			// real command words that are one letter from another game word: kept as said (cast/east, dead/read, toss, look/loot, right/fight, near/wear, till/kill, time/times)
			"cast","toss","dead","look","right","near","till","time"};

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
					// a real word that is a game word with one letter in front (meat/eat, bread/read) is kept
					if (w.length() == v.length() + 1 && w.endsWith(v)) continue;
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
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:^| )x?(\\d{1,2})(?: times| steps| spaces|$| )").matcher(clause);
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

	/** The step direction of a movement phrase, or null when the direction word belongs to another command ("pick up", "zoom in", "slot 2"). */
	static int[] moveDirection(String clause){
		if (clause.matches(".*\\b(zoom|slot|pick|pickup)\\b.*")) return null;
		return direction(clause);
	}

	private static final Set<String> FILLER = new HashSet<>(Arrays.asList("a","an","the","my","of","at","on","to","some",
			"please","go","use","that","this","it","with","one","potion","scroll","wand","nearest","closest","near","nearer",
			"far","farthest","furthest","other","second","next","different","enemy","monster","thing","guy","him","her","them",
			"north","south","east","west","northeast","northwest","southeast","southwest","up","down","left","right","side","above","below"));

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

	/** Does the enemy lie in that direction from the hero? Straight directions take the wider cone. */
	static boolean inDirection(int dx, int dy, int[] dir){
		if (dir[0] != 0 && dir[1] != 0) return Integer.signum(dx) == dir[0] && Integer.signum(dy) == dir[1];
		if (dir[0] != 0) return Integer.signum(dx) == dir[0] && Math.abs(dx) >= Math.abs(dy);
		return Integer.signum(dy) == dir[1] && Math.abs(dy) >= Math.abs(dx);
	}

	/**
	 * Which enemy the words mean. Name words narrow by name ("rat"), direction words by where it stands
	 * ("the north rat", "rat on the left"), "far"/"farthest" takes the farthest, "other"/"second"/"next" the
	 * next-closest (or anything but the last target). With no words at all: the last target if it is still
	 * in view, else the closest. Returns null when nothing in view fits; it never swaps in a different enemy.
	 */
	static Mob pickEnemy(String text){
		String t = " " + text + " ";
		List<String> words = keywords(text);
		int[] dir = direction(text);
		boolean far = t.matches(".* (far|farthest|furthest) .*");
		boolean other = t.matches(".* (other|second|next|different) .*");
		final Hero h = Dungeon.hero; final int w = Dungeon.level.width();
		ArrayList<Mob> fit = new ArrayList<>();
		for (Mob m : visibleEnemies()){
			if (nameScore(words, m.name()) == 0) continue;
			if (dir != null && !inDirection(m.pos % w - h.pos % w, m.pos / w - h.pos / w, dir)) continue;
			fit.add(m);
		}
		if (fit.isEmpty()) return null;
		java.util.Collections.sort(fit, (a, b) -> Float.compare(Dungeon.level.trueDistance(h.pos, a.pos), Dungeon.level.trueDistance(h.pos, b.pos)));
		if (far) return fit.get(fit.size() - 1);
		Char last = QuickSlotButton.lastTarget;
		if (other){
			for (Mob m : fit) if (m != last && (last != null || m != fit.get(0))) return m;
			return fit.size() > 1 ? fit.get(1) : null;
		}
		if (words.isEmpty() && dir == null && last instanceof Mob && fit.contains(last)) return (Mob) last;
		if (last instanceof Mob && fit.contains(last)
				&& Dungeon.level.trueDistance(h.pos, last.pos) <= Dungeon.level.trueDistance(h.pos, fit.get(0).pos)) return (Mob) last;
		return fit.get(0);
	}

	/** Nearest cell the player knows about that passes the test (doors, visible item piles). */
	static int nearest(java.util.function.IntPredicate test){
		int best = -1; float bestD = Float.MAX_VALUE;
		for (int c = 0; c < Dungeon.level.length(); c++){
			if (c == Dungeon.hero.pos || !(Dungeon.level.heroFOV[c] || known(c)) || !test.test(c)) continue;
			float d = Dungeon.level.trueDistance(Dungeon.hero.pos, c);
			if (d < bestD){ best = c; bestD = d; }
		}
		return best;
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
		boolean inRun = inRun();
		String rest;

		// stairs (plain "stairs" means the way down)
		if (clause.matches(".*\\b(upstairs|ascend|previous floor|go up the stairs|stairs up|up the stairs)\\b.*")){
			if (!inRun) return false;
			int entrance = Dungeon.level.entrance();
			if (!known(entrance)){ GLog.w("Voice: you haven't found the way up yet."); return true; }
			plan.add(new CellStep(entrance), "to the stairs up"); return true;
		}
		if (clause.matches(".*\\b(downstairs|descend|next floor|stairs|exit)\\b.*")){
			if (!inRun) return false;
			int exit = Dungeon.level.exit();
			if (!known(exit)){ GLog.w("Voice: you haven't found the way down yet."); return true; }
			plan.add(new CellStep(exit), "to the stairs down"); return true;
		}

		// the action button / Duelist weapon ability, optionally with a target
		String abilityName = inRun && ActionIndicator.action != null ? VoiceCommands.normalize(ActionIndicator.action.actionName()) : "";
		if ((rest = after(clause, "ability", "special", "use ability", "weapon ability")) != null
				|| (!abilityName.isEmpty() && (rest = after(clause, abilityName)) != null)){
			if (!inRun) return false;
			plan.add(new AbilityStep(), "ability");
			rest = rest.replaceAll("^(on|at|to) ", "");
			List<String> words = keywords(rest);
			int[] dir = direction(rest);
			if (!words.isEmpty() || rest.matches(".*\\b(far|farthest|furthest|other|second|next)\\b.*")){
				Mob m = pickEnemy(rest);
				if (m == null){ GLog.w("Voice: no " + rest + " in view."); plan.steps.clear(); return true; }
				plan.add(new TargetStep(m), "on " + m.name());
			} else if (dir != null){
				Mob m = pickEnemy(rest);
				if (m != null) plan.add(new TargetStep(m), "on " + m.name()); else plan.add(new TargetStep(dir), rest);
			} else {
				plan.add(new TargetStep((Mob) null), null);
			}
			return true;
		}

		// attack: named, by direction, far/other, or the last target / closest
		if ((rest = after(clause, "attack", "fight", "kill", "hit", "slay")) != null){
			if (!inRun) return false;
			boolean untilDead = clause.matches(".*\\b(kill|fight|slay)\\b.*") || rest.matches(".*\\b(until|till) .*");
			rest = rest.replaceAll("\\b(until|till) .*", "").trim();
			Mob m = pickEnemy(rest);
			if (m == null){
				int[] dir = direction(rest);
				if (dir != null && keywords(rest).isEmpty()){ plan.add(new MoveStep(dir[0], dir[1]), "attack " + rest); return true; }
				GLog.w(visibleEnemies().isEmpty() ? "Voice: no enemy in view." : "Voice: no " + (rest.isEmpty() ? "enemy" : rest) + " in view.");
				plan.steps.clear(); return true;
			}
			plan.fighting = untilDead;
			plan.add(new AttackStep(m, untilDead), (untilDead ? "kill " : "attack ") + m.name()); return true;
		}

		// search
		if (clause.matches("(search|search here|search around|look for secrets|look around)")){
			if (!inRun) return false;
			plan.add(new SearchStep(), "search"); return true;
		}

		// walk to a door or to an item on the floor
		if (clause.matches(".*\\bdoor\\b.*")){
			if (!inRun) return false;
			int c = nearest(cell -> Dungeon.level.map[cell] == Terrain.DOOR || Dungeon.level.map[cell] == Terrain.OPEN_DOOR);
			if (c < 0){ GLog.w("Voice: no door in sight."); return true; }
			plan.add(new CellStep(c), "to the door"); return true;
		}
		if ((rest = after(clause, "go to", "get", "grab", "walk to", "pick up", "open", "loot")) != null
				&& rest.matches(".*\\b(item|items|loot|chest|pile|gold|key|it)\\b.*|.+")
				&& !(rest.isEmpty()) && inRun){
			final List<String> words = keywords(rest.replaceAll("\\b(item|items|loot|pile|stuff)\\b", ""));
			int c = nearest(cell -> {
				Heap heap = Dungeon.level.heaps.get(cell);
				return heap != null && heap.seen && heap.peek() != null
						&& (words.isEmpty() || nameScore(words, heap.type == Heap.Type.HEAP ? heap.peek().name() : heap.title()) > 0);
			});
			if (c >= 0){ plan.add(new CellStep(c), "to the " + rest); return true; }
			if (VoiceCommands.parse(clause) == null){ GLog.w("Voice: no " + rest + " in sight."); return true; }
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
				if (target == null) plan.add(new TargetStep((Mob) null), null);
				else {
					Mob m = pickEnemy(target);
					if (m == null){ GLog.w("Voice: no " + target + " in view."); plan.steps.clear(); return true; }
					plan.add(new TargetStep(m), "at " + m.name());
				}
			}
			return true;
		}

		// movement, with repeats
		int[] d = moveDirection(clause);
		if (d != null){
			if (!inRun) { VoiceCommands.Match m = VoiceCommands.parse(clause); if (m != null && m.action != null){ plan.add(new KeyStep(m.action), null); return true; } return false; }
			int n = count(clause);
			for (int i = 0; i < n; i++) plan.add(new MoveStep(d[0], d[1]), i == 0 ? clause.replaceAll("\\b(go|move|walk|step)\\b ?", "") : null);
			return true;
		}

		// "slot 2 at the rat": use the quickslot, then aim it
		if (inRun && clause.matches("slot \\d (at|on) .+")){
			VoiceCommands.Match sm = VoiceCommands.parse(clause.substring(0, 6));
			Mob m = pickEnemy(clause.substring(clause.indexOf(' ', 7) + 1));
			if (sm == null || sm.action == null) return false;
			if (m == null){ GLog.w("Voice: no such enemy in view."); return true; }
			plan.add(new KeyStep(sm.action), clause.substring(0, 6));
			plan.add(new TargetStep(m), "at " + m.name());
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
