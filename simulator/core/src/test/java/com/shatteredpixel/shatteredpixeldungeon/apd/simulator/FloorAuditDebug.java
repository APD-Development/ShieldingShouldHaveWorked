package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import org.junit.Assume;
import org.junit.Test;

//Prints a floor as text around the audit's findings (-Papd.audit.debug=seed:depth). Skipped otherwise.
public class FloorAuditDebug {
	@Test public void print(){
		String d = System.getProperty("apd.audit.debug", "");
		Assume.assumeTrue(!d.isEmpty());
		HeadlessTestSupport.start();
		SPDSettings.intro(false);
		long seed = Long.parseLong(d.split(":")[0]);
		int depth = Integer.parseInt(d.split(":")[1]);
		Dungeon.daily = Dungeon.dailyReplay = false; Dungeon.customSeedText = ""; Dungeon.seed = seed;
		GamesInProgress.selectedClass = HeroClass.WARRIOR; Dungeon.hero = null; Dungeon.init();
		Level level = null;
		for (int i = 1; i <= depth; i++){ Dungeon.depth = i; Dungeon.branch = 0; level = Dungeon.newLevel(); Dungeon.level = level; }
		int start = level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE).cell();
		boolean[] r = FloorTruth.reach(level, start, true);
		StringBuilder b = new StringBuilder(level.getClass().getSimpleName() + "\n");
		for (int y = 0; y < level.height(); y++){
			for (int x = 0; x < level.width(); x++){
				int c = x + y * level.width();
				char ch;
				if (c == start) ch = 'S';
				else if (level.heaps.get(c) != null) ch = r[c] ? 'i' : 'I';
				else if (level.getTransition(c) != null) ch = 'X';
				else { int t = level.map[c]; ch = t == 4 || t == 12 ? '#' : t == 0 ? ' ' : (level.passable[c] ? (r[c] ? '.' : ',') : (char)('a' + 0) == 'x' ? 'x' : (t < 10 ? (char)('0' + t) : (char)('A' + t - 10))); }
				b.append(ch);
			}
			b.append('\n');
		}
		System.out.println(b);
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m : level.mobs){
			StringBuilder mb = new StringBuilder("MOB " + m.getClass().getSimpleName() + " at " + m.pos);
			for (String f : new String[]{"loot", "items"}){ Object v = SimUi.field(m, f); if (v != null) mb.append(" ").append(f).append("=").append(v); }
			System.out.println(mb);
		}
		for (int c = 0; c < level.length(); c++) if (level.map[c] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.LOCKED_DOOR) System.out.println("LOCKED_DOOR (" + c % level.width() + "," + c / level.width() + ")");
		for (com.shatteredpixel.shatteredpixeldungeon.items.Heap h : level.heaps.valueList()) System.out.println("PILE " + h.type + " (" + h.pos % level.width() + "," + h.pos / level.width() + ") " + h.items);
		java.util.Set<String> edges = new java.util.TreeSet<>();
		for (int c = 0; c < level.length(); c++) if (r[c]) for (int dd : com.watabou.utils.PathFinder.NEIGHBOURS8){
			int n = c + dd; if (n < 0 || n >= level.length() || r[n]) continue; int t = level.map[n];
			if (t == 4 || t == 12) continue;
			edges.add("EDGE (" + n % level.width() + "," + n / level.width() + ") terrain=" + t + " passable=" + level.passable[n] + " solid=" + level.solid[n] + " from (" + c % level.width() + "," + c / level.width() + ") t=" + level.map[c]);
		}
		for (String e : edges) System.out.println(e);
		for (com.shatteredpixel.shatteredpixeldungeon.items.Heap h : level.heaps.valueList()) if (!r[h.pos])
			System.out.println("HEAP " + h.pos + " (" + h.pos % level.width() + "," + h.pos / level.width() + ") terrain=" + level.map[h.pos] + " passable=" + level.passable[h.pos] + " type=" + h.type + " " + h.peek().getClass().getSimpleName());
	}
}
