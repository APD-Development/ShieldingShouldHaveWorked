/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//What the record says about how a run ended, facts only (UIMSD deep dive item 2, approved Nov 23): who killed the hero and when,
//the hero's health over the last turns, the damage taken by source, the effects on the hero, hunger, and the consumables still
//held. It gives no advice and draws no conclusion ("you should have drunk the potion" is a judgment rule; those need Chris's
//sign-off first). Anything the log does not hold is said to be unknown, never zero.
//
//Reads only the one run's rows (Dataset.rowsOf), so it is cheap enough for the death screen.
public final class Postmortem {

	public static final int WINDOW = 50;
	//the game's hunger levels (actors.buffs.Hunger.HUNGRY / STARVING; PostmortemTest keeps them equal)
	static final int HUNGRY = 300, STARVING = 450;

	public static final class HpPoint {
		public final long turn, hp, hpMax, shield;

		HpPoint( long turn, long hp, long hpMax, long shield ){
			this.turn = turn;
			this.hp = hp;
			this.hpMax = hpMax;
			this.shield = shield;
		}
	}

	public static final class DamageSource {
		public final String source;
		public long amount, hits, absorbed;

		DamageSource( String source ){
			this.source = source;
		}
	}

	public final String runId;
	public final boolean found;
	public long deathTurn = -1;
	public long floor = -1;
	public long heroLevel = -1;
	public String killer = "";
	public boolean finalDeath;
	public final List<HpPoint> hp = new ArrayList<>();
	public final List<DamageSource> damage = new ArrayList<>();
	public int uncertainDamage;
	public boolean damageRecorded, hpRecorded, effectsRecorded;
	public final List<String> effects = new ArrayList<>();
	public long hunger = -1;
	public final List<String> held = new ArrayList<>();
	public boolean heldRecorded;

	private Postmortem( String runId, boolean found ){
		this.runId = runId;
		this.found = found;
	}

	/** The last death of the run (the one that ended it, if one did). Never throws. */
	public static Postmortem of( Catalog cat, String runId ){
		try {
			return build(cat, runId);
		} catch (RuntimeException e){
			return new Postmortem(runId == null ? "" : runId, false);
		}
	}

	private static Postmortem build( Catalog cat, String runId ){
		if (cat == null || runId == null || runId.isEmpty()) return new Postmortem(runId == null ? "" : runId, false);
		Dataset deaths = cat.dataset(Datasets.DEATHS);
		if (deaths == null) return new Postmortem(runId, false);
		DatasetDef dd = deaths.def;
		int turnC = dd.indexOf("death.turn"), causeC = dd.indexOf("death.cause"), depthC = dd.indexOf("death.depth"),
				finalC = dd.indexOf("death.final"), levelC = dd.indexOf("death.heroLevel");
		Row death = null;
		for (Row r : deaths.rowsOf(runId)){
			if (r.imported || r.duplicate || !num(r, turnC)) continue;
			boolean fin = num(r, finalC) && r.num(finalC) == 1;
			if (death == null || fin || r.num(turnC) >= death.num(turnC)) death = r;
			if (fin) break;
		}
		if (death == null) return new Postmortem(runId, false);
		Postmortem p = new Postmortem(runId, true);
		p.deathTurn = death.num(turnC);
		p.finalDeath = num(death, finalC) && death.num(finalC) == 1;
		if (known(death, causeC)) p.killer = death.asText(causeC);
		if (num(death, depthC)) p.floor = death.num(depthC);
		if (num(death, levelC)) p.heroLevel = death.num(levelC);
		long from = Math.max(1, p.deathTurn - WINDOW + 1);
		p.health(cat, from);
		p.damage(cat, from);
		p.effects(cat);
		//item.heldAtEnd is the pack when the run ended, not at a death the hero came back from: only shown for the final death
		if (p.finalDeath) p.held(cat);
		return p;
	}

	private static boolean known( Row r, int c ){
		return c >= 0 && c < r.size() && r.isKnown(c);
	}

	private static boolean num( Row r, int c ){
		return known(r, c) && r.kind(c) == Row.K_NUM;
	}

	private void health( Catalog cat, long from ){
		Dataset d = cat.dataset(FactDatasets.PREFIX + "hero.state");
		if (d == null) return;
		int t = d.def.indexOf(FactDatasets.TURN), hpC = d.def.indexOf("fact.v.hp"), maxC = d.def.indexOf("fact.v.hpMax"),
				shC = d.def.indexOf("fact.v.shield"), huC = d.def.indexOf("fact.v.hunger");
		long hungerTurn = -1;
		for (Row r : d.rowsOf(runId)){
			if (!r.publicRow || !num(r, t)) continue;
			long turn = r.num(t);
			if (turn > deathTurn) continue;
			if (num(r, huC) && turn >= hungerTurn){
				hungerTurn = turn;
				hunger = r.num(huC);
			}
			if (turn < from || !num(r, hpC)) continue;
			hp.add(new HpPoint(turn, r.num(hpC), num(r, maxC) ? r.num(maxC) : -1, num(r, shC) ? r.num(shC) : 0));
		}
		Collections.sort(hp, new Comparator<HpPoint>(){
			@Override
			public int compare( HpPoint a, HpPoint b ){
				return a.turn < b.turn ? -1 : a.turn > b.turn ? 1 : 0;
			}
		});
		hpRecorded = !hp.isEmpty();
	}

	private void damage( Catalog cat, long from ){
		Dataset d = cat.dataset(EventDatasets.DAMAGE);
		if (d == null) return;
		int t = d.def.indexOf("damage.turn"), kind = d.def.indexOf("damage.targetKind"), src = d.def.indexOf("damage.source"),
				amt = d.def.indexOf("damage.amount"), abs = d.def.indexOf("damage.shieldAbsorbed");
		Map<String, DamageSource> by = new LinkedHashMap<>();
		for (Row r : d.rowsOf(runId)){
			if (!r.publicRow || !num(r, t) || r.num(t) < from || r.num(t) > deathTurn) continue;
			if (!num(r, kind) || r.num(kind) != CombatEvents.KIND_HERO) continue;
			damageRecorded = true;
			if (!num(r, amt) || r.prov(amt) != Fact.PROV_RECORDED){
				uncertainDamage++;
				continue;
			}
			String s = known(r, src) ? r.asText(src) : "";
			if (s.isEmpty()) s = "Not recorded";
			DamageSource ds = by.get(s);
			if (ds == null) by.put(s, ds = new DamageSource(s));
			ds.amount += r.num(amt);
			ds.hits++;
			if (num(r, abs)) ds.absorbed += r.num(abs);
		}
		damage.addAll(by.values());
		Collections.sort(damage, new Comparator<DamageSource>(){
			@Override
			public int compare( DamageSource a, DamageSource b ){
				return a.amount != b.amount ? (a.amount > b.amount ? -1 : 1) : a.source.compareTo(b.source);
			}
		});
	}

	//effects on the hero (an empty "who") that began and had not ended before the death turn
	private void effects( Catalog cat ){
		Dataset start = cat.dataset(FactDatasets.PREFIX + "status.start"), end = cat.dataset(FactDatasets.PREFIX + "status.end");
		if (start == null) return;
		Map<String, int[]> open = new LinkedHashMap<>();
		List<long[]> order = new ArrayList<>();   //turn, +1/-1, index into names
		List<String> names = new ArrayList<>();
		collect(start, 1, order, names);
		if (end != null) collect(end, -1, order, names);
		effectsRecorded = !order.isEmpty();
		Collections.sort(order, new Comparator<long[]>(){
			@Override
			public int compare( long[] a, long[] b ){
				if (a[0] != b[0]) return a[0] < b[0] ? -1 : 1;
				return a[1] == b[1] ? 0 : a[1] > 0 ? -1 : 1;   //on one turn, a start before an end
			}
		});
		for (long[] o : order){
			if (o[0] > deathTurn || (o[1] < 0 && o[0] >= deathTurn)) continue; //effects end as the hero dies: that is not them wearing off
			String name = names.get((int) o[2]);
			int[] n = open.get(name);
			if (n == null) open.put(name, n = new int[1]);
			n[0] = Math.max(0, n[0] + (int) o[1]);
		}
		for (Map.Entry<String, int[]> e : open.entrySet()) if (e.getValue()[0] > 0) effects.add(e.getKey());
	}

	private void collect( Dataset d, int sign, List<long[]> order, List<String> names ){
		int t = d.def.indexOf(FactDatasets.TURN), subj = d.def.indexOf(FactDatasets.SUBJECT), actor = d.def.indexOf(FactDatasets.ACTOR);
		for (Row r : d.rowsOf(runId)){
			if (!r.publicRow || !num(r, t) || !known(r, subj)) continue;
			if (known(r, actor) && !r.asText(actor).isEmpty()) continue;
			String name = effectName(r.asText(subj));
			if (name.isEmpty()) continue;
			int i = names.indexOf(name);
			if (i < 0){
				i = names.size();
				names.add(name);
			}
			order.add(new long[]{ r.num(t), sign, i });
		}
	}

	//Runs recorded before Nov 23 stored some effects as the Java object's name ("com.shatteredpixel...Bleeding@1a2b"): show "Bleeding"
	static String effectName( String raw ){
		if (raw == null) return "";
		String s = raw.trim();
		int at = s.indexOf('@');
		if (at >= 0) s = s.substring(0, at);
		if (s.contains(".") && !s.contains(" ")){
			s = s.substring(s.lastIndexOf('.') + 1);
			int dollar = s.lastIndexOf('$');
			if (dollar >= 0) s = s.substring(dollar + 1);
			s = s.replaceAll("([a-z])([A-Z])", "$1 $2");
			if (s.length() > 1) s = s.charAt(0) + s.substring(1).toLowerCase(java.util.Locale.ROOT);
		}
		return s;
	}

	private void held( Catalog cat ){
		Dataset d = cat.dataset(Datasets.ITEMS);
		if (d == null) return;
		int heldC = d.def.indexOf("item.heldAtEnd"), catC = d.def.indexOf("item.category"), nameC = d.def.indexOf("item.name");
		Map<String, int[]> count = new LinkedHashMap<>();
		for (Row r : d.rowsOf(runId)){
			if (!r.publicRow || !num(r, heldC)) continue;
			heldRecorded = true;
			if (r.num(heldC) != 1 || !num(r, catC) || !consumable((int) r.num(catC)) || !known(r, nameC)) continue;
			String name = r.asText(nameC);
			int[] n = count.get(name);
			if (n == null) count.put(name, n = new int[1]);
			n[0]++;
		}
		for (Map.Entry<String, int[]> e : count.entrySet()) held.add(e.getValue()[0] > 1 ? e.getKey() + " (" + e.getValue()[0] + ")" : e.getKey());
	}

	static boolean consumable( int category ){
		switch (category){
			case ItemEvents.CAT_POTION: case ItemEvents.CAT_SCROLL: case ItemEvents.CAT_STONE: case ItemEvents.CAT_SPELL:
			case ItemEvents.CAT_BOMB: case ItemEvents.CAT_FOOD: case ItemEvents.CAT_SEED:
				return true;
			default:
				return false;
		}
	}

	public static String hungerLabel( long hunger ){
		if (hunger < 0) return "not recorded";
		if (hunger >= STARVING) return "starving (" + hunger + " of " + STARVING + ")";
		if (hunger >= HUNGRY) return "hungry (" + hunger + " of " + STARVING + ")";
		return "not hungry (" + hunger + " of " + STARVING + ")";
	}

	/** The lowest recorded health in the window, or null. */
	public HpPoint lowest(){
		HpPoint low = null;
		for (HpPoint h : hp) if (low == null || h.hp < low.hp) low = h;
		return low;
	}

	/** Plain text, facts only. */
	public String text(){
		if (!found) return "No death is recorded for this run.";
		StringBuilder b = new StringBuilder();
		b.append(killer.isEmpty() ? "Cause of death not recorded" : capital(killer) + " killed you");
		b.append(floor > 0 ? " on floor " + floor : "").append(", turn ").append(deathTurn);
		if (heroLevel > 0) b.append(", at level ").append(heroLevel);
		b.append('.');
		if (!finalDeath) b.append(" You came back from this one.");

		b.append("\n\nYour health before you died:\n");
		if (!hpRecorded) b.append("Not recorded for this run.");
		else {
			HpPoint first = hp.get(0), last = hp.get(hp.size() - 1), low = lowest();
			if (first.turn == last.turn) b.append("Only turn ").append(last.turn).append(" was recorded: ").append(hpText(first))
					.append(" down to ").append(hpText(last)).append('.');
			else {
				b.append("Turn ").append(first.turn).append(": ").append(hpText(first)).append('\n');
				if (low != last) b.append("Lowest, turn ").append(low.turn).append(": ").append(hpText(low)).append('\n');
				b.append("Turn ").append(last.turn).append(": ").append(hpText(last)).append('\n');
				b.append(trace());
			}
		}

		long from = Math.max(1, deathTurn - WINDOW + 1);
		b.append("\n\nDamage you took, turns ").append(from).append(" to ").append(deathTurn).append(":\n");
		if (!damageRecorded) b.append("None recorded.");
		else {
			long total = 0;
			for (DamageSource d : damage) total += d.amount;
			for (DamageSource d : damage){
				String who = d.source.equals("Not recorded") ? "Unknown source" : capital(d.source);
				b.append(who).append(": ").append(d.amount).append(" (").append(d.hits).append(d.hits == 1 ? " hit" : " hits").append(')');
				if (d.absorbed > 0) b.append(", ").append(d.absorbed).append(" blocked by shield");
				b.append('\n');
			}
			b.append("Total: ").append(total);
			if (uncertainDamage > 0) b.append(" (plus ").append(uncertainDamage).append(uncertainDamage == 1 ? " hit" : " hits").append(" of unknown size)");
		}

		b.append("\n\nEffects on you: ");
		if (!effectsRecorded) b.append("not recorded.");
		else b.append(effects.isEmpty() ? "none." : join(effects) + ".");
		b.append("\nHunger: ").append(hungerLabel(hunger)).append('.');

		b.append("\n\nStill in your pack: ");
		if (!finalDeath) b.append("not recorded for a death you came back from.");
		else if (!heldRecorded) b.append("not recorded.");
		else b.append(held.isEmpty() ? "no consumables." : join(held) + ".");
		return b.toString();
	}

	private static String capital( String s ){
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static String hpText( HpPoint h ){
		String s = h.hp + (h.hpMax > 0 ? "/" + h.hpMax : "") + " HP";
		return h.shield > 0 ? s + " + " + h.shield + " shield" : s;
	}

	//one number every five turns, so the shape of the last fifty turns fits on a phone line or two
	private String trace(){
		StringBuilder b = new StringBuilder("HP every 5 turns: ");
		long next = -1;
		int n = 0;
		for (HpPoint h : hp){
			if (next >= 0 && h.turn < next) continue;
			if (n++ > 0) b.append(' ');
			b.append(h.hp);
			next = h.turn + 5;
		}
		return b.toString();
	}

	private static String join( List<String> parts ){
		StringBuilder b = new StringBuilder();
		for (String s : parts) b.append(b.length() == 0 ? "" : ", ").append(s);
		return b.toString();
	}
}
