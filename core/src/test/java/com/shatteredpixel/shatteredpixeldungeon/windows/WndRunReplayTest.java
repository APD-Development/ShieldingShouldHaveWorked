package com.shatteredpixel.shatteredpixeldungeon.windows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WndRunReplayTest {

	//a floor with no recorded steps used to restart itself forever (toggle -> floor -> toggle ...)
	@Test
	public void emptyFloorNeverRestarts(){
		assertFalse(WndRunReplay.restartOnPlay(false, 0, 0));
		assertFalse(WndRunReplay.restartOnPlay(true, 0, 0));
	}

	@Test
	public void finishedFloorRestartsOnPlay(){
		assertTrue(WndRunReplay.restartOnPlay(false, 5, 5));
		assertFalse(WndRunReplay.restartOnPlay(false, 3, 5));
		assertFalse(WndRunReplay.restartOnPlay(true, 5, 5));
	}

	//Chris, Nov 25: a rat and a gnoll never share a color on the same floor, and a type keeps its color
	@Test
	public void eachCreatureTypeOnAFloorHasItsOwnColor(){
		java.util.List<String> types = new java.util.ArrayList<>();
		for (int i = 0; i < 30; i++) types.add("actors.mobs.Type" + i);
		types.add(0, "actors.mobs.Rat"); types.add(1, "actors.mobs.Gnoll");
		WndRunReplay.TypeColors c = new WndRunReplay.TypeColors(types);
		java.util.Set<Integer> seen = new java.util.HashSet<>();
		for (String t : types) org.junit.Assert.assertTrue(t, seen.add(c.color(t)));
		org.junit.Assert.assertEquals(c.color("actors.mobs.Rat"),
				new WndRunReplay.TypeColors(java.util.Arrays.asList("actors.mobs.Rat")).color("actors.mobs.Rat"));
		org.junit.Assert.assertNotEquals(c.color("actors.mobs.Rat"), c.color("actors.mobs.Gnoll"));
	}
}
