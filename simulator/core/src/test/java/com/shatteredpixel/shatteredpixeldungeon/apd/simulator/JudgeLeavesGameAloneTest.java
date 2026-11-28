package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.junit.Test;

//The fight judge reads a monster's damage and armor from a throwaway copy of it. Asking must change nothing in the
//game: not the loot tables (some monsters roll loot when they are created) and not the game's random numbers.
public class JudgeLeavesGameAloneTest {

	private static final String[] MONSTERS = {"Rat", "Snake", "Gnoll", "Crab", "Slime", "Swarm", "Goo", "Skeleton", "Thief", "Guard",
			"DM100", "Necromancer", "Tengu", "Bat", "Brute", "Shaman$RedShaman", "Spinner", "DM200", "Ghoul", "Elemental$FireElemental",
			"Warlock", "Monk", "Golem", "Succubus", "Eye", "Scorpio", "Statue", "ArmoredStatue", "Mimic", "GoldenMimic", "EbonyMimic",
			"GnollExile", "HermitCrab", "Wraith", "Piranha", "GreatCrab", "FetidRat", "GnollTrickster"};

	private static String lootTables(){
		Bundle b = new Bundle();
		Generator.storeInBundle(b);
		return b.toString();
	}

	@Test
	public void judgingEveryMonsterChangesNoLootTableAndNoGameRandomness() throws Exception {
		HeadlessTestSupport.start();
		SPDSettings.intro(false);
		Generator.fullReset();
		FightJudge.reset();
		int judged = 0;
		for (int depth : new int[]{1, 7, 13, 19, 24}){
			Dungeon.depth = depth;
			for (String name : MONSTERS){
				Mob m;
				try {
					m = (Mob) Class.forName("com.shatteredpixel.shatteredpixeldungeon.actors.mobs." + name).getDeclaredConstructor().newInstance();
				} catch (Throwable t){
					continue; //not every monster can be made outside a level; the judge falls back to a guess for those
				}
				String before = lootTables();
				Random.pushGenerator(12345L + judged);
				long next;
				try {
					Random.pushGenerator(999L);
					long want = Random.Long();
					Random.popGenerator();
					Random.pushGenerator(999L);
					FightJudge.stats(m);
					next = Random.Long();
					Random.popGenerator();
					assertEquals(name + " on depth " + depth + ": the judge drew from the game's random numbers", want, next);
				} finally {
					Random.popGenerator();
				}
				assertEquals(name + " on depth " + depth + ": the judge changed the loot tables", before, lootTables());
				judged++;
			}
		}
		org.junit.Assert.assertTrue("only " + judged + " monsters could be judged", judged > 100);
	}
}
