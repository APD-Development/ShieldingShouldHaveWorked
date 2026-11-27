package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import java.util.HashMap;
import java.util.Map;

//States the game should never be in. Checked on the hero's turns; each finding is a report (deduplicated by kind and place).
final class Invariants {

	private Invariants(){}

	static void check(SimRuntime sim){
		Hero h = Dungeon.hero;
		Level level = Dungeon.level;
		if (h == null || level == null) return;
		if (h.HP > h.HT) bad(sim, "hp>ht:hero", "The hero has more HP than their maximum (" + h.HP + "/" + h.HT + ").");
		if (h.HT <= 0) bad(sim, "ht<=0", "The hero's maximum HP is " + h.HT + ".");
		if (h.lvl < 1 || h.lvl > 30) bad(sim, "lvl", "The hero's level is " + h.lvl + ".");
		if (h.exp < 0) bad(sim, "exp<0", "The hero's experience is negative (" + h.exp + ").");
		if (h.lvl < 30 && h.exp >= h.maxExp()) bad(sim, "exp>=max", "The hero has " + h.exp + " experience but needs only " + h.maxExp() + " to level up and did not.");
		if (h.STR() < 1) bad(sim, "str", "The hero's strength is " + h.STR() + ".");
		if (Dungeon.gold < 0) bad(sim, "gold<0", "Gold is negative (" + Dungeon.gold + ").");
		if (Dungeon.energy < 0) bad(sim, "energy<0", "Alchemy energy is negative (" + Dungeon.energy + ").");
		if (h.pos < 0 || h.pos >= level.length()) bad(sim, "heropos", "The hero is outside the floor (cell " + h.pos + ").");
		else if (level.solid[h.pos] && !level.passable[h.pos]) bad(sim, "herowall", "The hero is standing inside a wall at " + SimBot.cell(h.pos) + ".");
		for (Item i : h.belongings){
			if (i.quantity() < 1) bad(sim, "qty:" + i.getClass().getName(), "An item in the backpack has quantity " + i.quantity() + " (" + i.getClass().getSimpleName() + ").");
			if (i.level() < -20 || i.level() > 100) bad(sim, "itemlvl:" + i.getClass().getName(), i.getClass().getSimpleName() + " has an impossible level " + i.level() + ".");
		}
		Map<Integer, Char> cells = new HashMap<>();
		for (Char c : Actor.chars()){
			if (!c.isAlive()) continue;
			if (c.HP > c.HT && c != h) bad(sim, "hp>ht:" + c.getClass().getName(), c.getClass().getSimpleName() + " has more HP than its maximum (" + c.HP + "/" + c.HT + ").");
			if (c.pos < 0 || c.pos >= level.length()){
				bad(sim, "charpos:" + c.getClass().getName(), c.getClass().getSimpleName() + " is outside the floor (cell " + c.pos + ").");
				continue;
			}
			Char other = cells.put(c.pos, c);
			if (other != null) bad(sim, "samecell:" + other.getClass().getName() + "+" + c.getClass().getName(),
					other.getClass().getSimpleName() + " and " + c.getClass().getSimpleName() + " are on the same cell " + SimBot.cell(c.pos) + ".");
		}
	}

	private static void bad(SimRuntime sim, String sig, String what){
		sim.report(BugReport.Kind.STATE, sig, what, "");
	}
}
