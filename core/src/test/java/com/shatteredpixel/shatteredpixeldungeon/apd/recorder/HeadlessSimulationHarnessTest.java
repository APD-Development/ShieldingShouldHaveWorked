package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import org.junit.Test;

import java.util.List;

public class HeadlessSimulationHarnessTest {

	@Test
	public void sameSeedReplaysMovementPickupAndCombatTranscript(){
		try {
			for (HeroClass heroClass : HeroClass.values()){
				List<String> first = HeadlessSimulationHarness.run(638194L, heroClass);
				List<String> replay = HeadlessSimulationHarness.run(638194L, heroClass);

				assertEquals(heroClass + " replay diverged", first, replay);
				assertTrue(first.stream().anyMatch(line -> line.startsWith("move:")));
				assertTrue(first.stream().anyMatch(line -> line.startsWith("pickup:food=")));
				assertTrue(first.stream().anyMatch(line -> line.startsWith("resource:healing-potions=")));
				assertTrue(first.stream().anyMatch(line -> line.startsWith("boss:depth=5:gooHP=")));
				assertTrue(first.stream().anyMatch(line -> line.contains(":hero-drink-healing:")));
				assertTrue(first.stream().anyMatch(line -> line.contains(":goo-act:taken=")));
				String outcome = first.get(first.size() - 1);
				assertTrue(heroClass + " Goo simulation did not win:\n" + String.join("\n", first),
						outcome.contains("outcome:depth=5:heroAlive=true:gooAlive=false:"));
			}
		} catch (Exception e){
			throw new AssertionError("headless simulation failed", e);
		}
	}
}