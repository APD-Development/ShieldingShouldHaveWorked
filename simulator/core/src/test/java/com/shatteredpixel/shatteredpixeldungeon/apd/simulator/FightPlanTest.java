package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//The plan's arithmetic: what a group costs when they all reach the hero against one at a time, and that a retreat
//is never planned past something faster than the hero.
public class FightPlanTest {

	private static FightPlan.Enemy enemy(float perTurn, float turnsToKill, float speed, int steps, boolean ranged){
		FightPlan.Enemy e = new FightPlan.Enemy();
		e.verdict = new FightJudge.Verdict();
		e.verdict.heroHits = 1f;
		e.verdict.heroDamage = 1f;
		e.verdict.perTurn = perTurn;
		e.verdict.turnsToKill = turnsToKill;
		e.speed = speed;
		e.steps = steps;
		e.ranged = ranged;
		return e;
	}

	@Test
	public void aCorridorMakesAGroupCheaper(){
		List<FightPlan.Enemy> three = new ArrayList<>();
		for (int i = 0; i < 3; i++) three.add(enemy(2f, 4f, 1f, 5, false));
		//all three swinging: 4 turns at 6, 4 at 4, 4 at 2 = 48. One at a time: 3 fights of 4 turns at 2 = 24.
		assertEquals(48f, FightPlan.cost(three, 3), 0.001f);
		assertEquals(24f, FightPlan.cost(three, 1), 0.001f);
	}

	@Test
	public void aCorridorDoesNotStopTheOnesAtRange(){
		List<FightPlan.Enemy> mixed = new ArrayList<>();
		mixed.add(enemy(2f, 4f, 1f, 5, false));
		mixed.add(enemy(2f, 4f, 1f, 5, true));
		//the shooter keeps shooting while the hero fights the other: same cost in the open and in a corridor
		assertEquals(FightPlan.cost(mixed, 2), FightPlan.cost(mixed, 1), 0.001f);
	}

	@Test
	public void sameSpeedEnemiesOnlyFollow(){
		List<FightPlan.Enemy> crowd = new ArrayList<>();
		crowd.add(enemy(2f, 4f, 1f, 1, false));
		assertTrue(FightPlan.reachableInTime(crowd, 12, 1f));
	}

	@Test
	public void noRetreatPastSomethingFaster(){
		List<FightPlan.Enemy> crab = new ArrayList<>();
		//a crab at twice the hero's speed, 4 steps away: it closes 3 cells of gap in 3 turns
		crab.add(enemy(2f, 4f, 2f, 4, false));
		assertFalse(FightPlan.reachableInTime(crab, 6, 1f));
		assertTrue(FightPlan.reachableInTime(crab, 3, 1f));
	}
}
