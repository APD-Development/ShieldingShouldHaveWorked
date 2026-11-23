package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GameFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PostmortemTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void hungerLevelsAreTheGamesOwn(){
		assertEquals((int) Hunger.HUNGRY, Postmortem.HUNGRY);
		assertEquals((int) Hunger.STARVING, Postmortem.STARVING);
	}

	@Test
	public void theFactsOfTheLastFiftyTurns(){
		SyntheticLog s = new SyntheticLog(tmp.getRoot(), "a", TelemetryVersion.CURRENT, 100)
				.start("a", 0, "WARRIOR", "r", 0, 30, false).enter(4);
		//turns 1-60: health falls from 30 to 1, hunger climbs past starving
		long[][] state = new long[60][];
		for (int i = 0; i < 60; i++) state[i] = new long[]{ Math.max(1, 30 - i / 2), 30, i == 55 ? 4 : 0, 400 + i };
		s.clock = 1;
		s.factSeries(GameFacts.HERO_STATE, "", state);
		//an effect that began and ended long ago, one still on, one that "ends" as the hero dies
		s.clock = 5; s.fact(GameFacts.STATUS_START, "Hunger", -1);
		s.clock = 8; s.fact(GameFacts.STATUS_END, "Hunger", 0);
		s.clock = 20; s.fact(GameFacts.STATUS_START, "Poisoned", 10);
		s.clock = 40; s.fact(GameFacts.STATUS_START, "Bleeding", 5);
		s.clock = 5; s.damageToHero("rat", "Rat", 50);           //outside the window (window is turns 12-61)
		s.clock = 30; s.damageToHero("gnoll", "Gnoll", 6);
		s.clock = 35; s.damageToHero("gnoll", "Gnoll", 7);
		s.clock = 50; s.damageToHero("Gnoll shaman", "GnollShaman", 9);
		s.acquiredVia("pot", "amber potion", ItemEvents.CAT_POTION, ItemEvents.ACQ_PICKUP)
				.acquiredVia("pot2", "amber potion", ItemEvents.CAT_POTION, ItemEvents.ACQ_PICKUP)
				.acquiredVia("sword", "sword", ItemEvents.CAT_WEAPON, ItemEvents.ACQ_PICKUP)
				.acquiredVia("scroll", "scroll of rage", ItemEvents.CAT_SCROLL, ItemEvents.ACQ_PICKUP);
		s.clock = 61;
		s.fact(GameFacts.STATUS_END, "Bleeding", 0);
		s.clock = 61;
		s.heroDies("Gnoll shaman", CombatEvents.SRC_CHAR);
		s.end(LifecycleEvents.OUTCOME_DIED, "GnollShaman", 4, 3, 10).close(true);

		Catalog cat = new DatasetStore(tmp.getRoot()).load();
		Postmortem p = Postmortem.of(cat, "a");
		assertTrue(p.found);
		assertTrue(p.finalDeath);
		assertEquals(61, p.deathTurn);
		assertEquals(4, p.floor);
		//hero.state rows are turns 1..60 (one a tick): the window keeps 12..60
		assertEquals(49, p.hp.size());
		assertEquals(12, p.hp.get(0).turn);
		assertEquals(1, p.lowest().hp);
		assertEquals(459, p.hunger);
		assertEquals("starving (459 of 450)", Postmortem.hungerLabel(p.hunger));
		assertEquals(2, p.damage.size());
		assertEquals("gnoll", p.damage.get(0).source);
		assertEquals(13, p.damage.get(0).amount);
		assertEquals(2, p.damage.get(0).hits);
		assertEquals(9, p.damage.get(1).amount);
		assertTrue(p.effects.contains("Poisoned"));
		assertTrue("an effect that ends as the hero dies was still on", p.effects.contains("Bleeding"));
		assertFalse(p.effects.contains("Hunger"));
		assertEquals(2, p.held.size());
		assertTrue(p.held.contains("amber potion (2)"));
		assertTrue(p.held.contains("scroll of rage"));
		String text = p.text();
		assertTrue(text, text.startsWith("Gnoll shaman killed you on floor 4, turn 61"));
		assertEquals("Bleeding", Postmortem.effectName("com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding@1a2b3c"));
		assertEquals("Magic immune", Postmortem.effectName("com.x.MagicImmune"));
		assertEquals("Poisoned", Postmortem.effectName("Poisoned"));
		assertTrue(text, text.contains("turn 61"));
		assertTrue(text, text.contains("Total: 22"));
		assertFalse("facts only: no advice", text.toLowerCase().contains("should"));
	}

	@Test
	public void aRunWithNoDeathSaysSo(){
		new SyntheticLog(tmp.getRoot(), "w", TelemetryVersion.CURRENT, 100).start("w", 0, "MAGE", "r", 0, 30, false).enter(1)
				.end(LifecycleEvents.OUTCOME_WON, "", 1, 1, 10).close(true);
		Postmortem p = Postmortem.of(new DatasetStore(tmp.getRoot()).load(), "w");
		assertFalse(p.found);
		assertEquals("No death is recorded for this run.", p.text());
		assertFalse(Postmortem.of(null, null).found);
	}
}
