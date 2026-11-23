package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.EventTypes;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogWriter;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import java.io.File;

//A history of long runs shaped like a real one: every step, every turn's health, rests, and a few thousand fights per run,
//about 38,000 rows a run, the size of the long runs whose rows ran the phone out of memory (Nov 23 probe: 38,022 rows a run).
final class LongHistory {

	static final int STEPS = 15000, TURNS = 15000, RESTS = 3000, FIGHTS = 2000;

	private LongHistory(){}

	static void write( File root, int runs ) throws Exception {
		String[] classes = { "WARRIOR", "MAGE", "ROGUE", "HUNTRESS", "DUELIST", "CLERIC" };
		for (int i = 0; i < runs; i++){
			String runId = "long" + i;
			File f = new File(new File(root, "runs"), runId + ".apdlog");
			RunLogWriter w = RunLogWriter.create(f, TelemetryVersion.CURRENT, runId, "test", 1,
					new RunLogWriter.Options().queueCapacity(1 << 21));
			LifecycleEvents.RunStart s = new LifecycleEvents.RunStart();
			s.runId = runId;
			s.heroClass = classes[i % classes.length];
			s.rulesetId = "rules-a"; s.seed = 1000 + i; s.apdVersionName = "4.0.8-apd"; s.realTimeMillis = 1_700_000_000_000L + i * 3_600_000L;
			LifecycleEvents.emitRunStart(w, 0, false, s);

			long[] clocks = new long[STEPS];
			long[][] rows = new long[STEPS][];
			for (int k = 0; k < STEPS; k++){
				clocks[k] = k + 1;
				rows[k] = new long[]{ 100 + (k * 7 + i) % 900, 32, k % 50 == 0 ? 0 : 1 };
			}
			FactEvents.emitSeries(w, EventTypes.FACT_SERIES, "move.step", "", "you", new String[0], 3, clocks, rows, STEPS);
			clocks = new long[TURNS];
			rows = new long[TURNS][];
			for (int k = 0; k < TURNS; k++){
				clocks[k] = k + 1;
				int hpMax = 20 + k / 600;
				rows[k] = new long[]{ Math.max(1, hpMax - (k * 13 + i) % 17), hpMax, k % 97 == 0 ? 5 : 0, k % 450 };
			}
			FactEvents.emitSeries(w, EventTypes.FACT_SERIES, "hero.state", "", "you", new String[0], 4, clocks, rows, TURNS);
			clocks = new long[RESTS];
			rows = new long[RESTS][];
			for (int k = 0; k < RESTS; k++){
				clocks[k] = k * 5 + 2;
				rows[k] = new long[]{ k % 3 == 0 ? 1 : 0 };
			}
			FactEvents.emitSeries(w, EventTypes.FACT_SERIES, "explore.rest", "", "you", new String[0], 1, clocks, rows, RESTS);

			String[] enemies = { "rat", "gnoll", "crab", "skeleton", "thief", "bat", "brute", "spinner" };
			for (int k = 0; k < FIGHTS; k++){
				CombatEvents.Attack a = new CombatEvents.Attack();
				a.attacker.kind = CombatEvents.KIND_HERO; a.attacker.name = "you";
				a.defender.kind = CombatEvents.KIND_ENEMY; a.defender.name = enemies[k % enemies.length];
				a.hit = k % 4 != 0; a.weaponName = "sword";
				CombatEvents.emitAttack(w, k * 7 + 1, a);
				CombatEvents.Damage d = new CombatEvents.Damage();
				d.target.kind = CombatEvents.KIND_ENEMY; d.target.name = enemies[k % enemies.length]; d.shown = 1 + k % 9;
				CombatEvents.emitDamage(w, k * 7 + 1, d);
			}
			LifecycleEvents.RunEnd e = new LifecycleEvents.RunEnd();
			e.outcome = i % 5 == 0 ? LifecycleEvents.OUTCOME_WON : LifecycleEvents.OUTCOME_DIED;
			e.score = 1000 + i;
			LifecycleEvents.emitRunEnd(w, TURNS + 1, e);
			w.close(true);
		}
	}
}
