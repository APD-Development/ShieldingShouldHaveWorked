package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class HeadlessSimulationHarnessTest {

	@Test
	public void sameSeedReplaysMovementPickupAndCombatTranscript(){
		try {
			List<String> first = HeadlessSimulationHarness.run(638194L);
			List<String> replay = HeadlessSimulationHarness.run(638194L);

			assertEquals(first, replay);
			assertTrue(first.stream().anyMatch(line -> line.startsWith("move:")));
			assertTrue(first.stream().anyMatch(line -> line.startsWith("pickup:food=")));
			assertTrue(first.stream().anyMatch(line -> line.startsWith("resource:healing-potions=")));
			assertTrue(first.stream().anyMatch(line -> line.startsWith("boss:depth=5:gooHP=")));
			assertTrue(first.stream().anyMatch(line -> line.contains(":hero-drink-healing:")));
			assertTrue(first.stream().anyMatch(line -> line.contains(":goo-act:taken=")));
			String outcome = first.get(first.size() - 1);
			assertTrue("Goo simulation did not win:\n" + String.join("\n", first),
					outcome.contains("outcome:depth=5:heroAlive=true:gooAlive=false:"));
		} catch (Exception e){
			throw new AssertionError("headless simulation failed", e);
		}
	}
}