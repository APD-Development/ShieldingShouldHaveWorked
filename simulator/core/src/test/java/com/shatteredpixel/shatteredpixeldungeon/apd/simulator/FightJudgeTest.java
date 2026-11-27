package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

//The judge's hit chance must be the game's own rule: both sides roll from 0 to their skill, attacker wins ties.
public class FightJudgeTest {

	@Test
	public void equalSkillsHitHalfTheTime(){
		assertEquals(0.5f, FightJudge.hitChance(10, 10), 0.0001f);
	}

	@Test
	public void strongerAttackerMissesOnlyWhenTheDefenderOutrolls(){
		//accuracy 20 against evasion 10: the defender's roll beats the attacker's a quarter of the time
		assertEquals(0.75f, FightJudge.hitChance(20, 10), 0.0001f);
	}

	@Test
	public void levelTwoHeroAgainstAWraithHitsOneSwingInTen(){
		//hero accuracy 12 against a floor 2 wraith's evasion 60
		assertEquals(0.1f, FightJudge.hitChance(12, 60), 0.0001f);
	}

	@Test
	public void perfectEvasionAndPerfectAccuracy(){
		assertEquals(0f, FightJudge.hitChance(50, Char.INFINITE_EVASION), 0f);
		assertEquals(1f, FightJudge.hitChance(Char.INFINITE_ACCURACY, 50), 0f);
		assertEquals(1f, FightJudge.hitChance(10, 0), 0f);
	}
}
