package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKind;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKinds;

//The simulator's own fact kind, declared only inside the simulator app: one observation per run with the bot's settings,
//so the simulator's UIMSD can filter and compare runs by aggression and intelligence.
final class SimFacts {
	private SimFacts(){}

	static final FactKind RUN = FactKinds.register(FactKind.builder("sim.run", "Simulator run")
			.plural("Simulator runs")
			.description("The simulator's settings for this run (simulator app only).")
			.area("Simulator")
			.subject("Simulator")
			.tag("aggression", "Aggression")
			.tag("intelligence", "Intelligence")
			.tag("settings", "APD Settings")
			.tag("replay", "Replay")
			.value("botSeedHigh", "Bot seed (high)")
			.value("botSeedLow", "Bot seed (low)")
			.build());

	static void recordRun(SimProfile p, boolean replay, String settings, long botSeed){
		ApdFacts.record(RUN, p.name, "", new long[]{botSeed >>> 32, botSeed & 0xFFFFFFFFL},
				new String[]{p.aggression.title, p.intelligence.title, settings, replay ? "yes" : "no"});
	}
}
