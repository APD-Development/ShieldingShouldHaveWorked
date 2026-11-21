package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/** Script mods, items hook (Chris, Nov 21): declare, use, effects, drops with a capped weight, starting kit, saves. */
public class ScriptItemsTest {

	private Hero oldHero, hero;

	@Before public void setUp(){
		HeadlessTestSupport.start();
		ScriptModRuntime.clear();
		oldHero = Dungeon.hero;
		hero = new Hero();
		hero.HT = 50; hero.HP = 20;
		Dungeon.hero = hero;
	}

	@After public void tearDown(){
		ScriptModRuntime.clear();
		Dungeon.hero = oldHero;
	}

	static final String ECHOES =
			"APD.effect({ id: 'echoes', name: 'Echoes', description: 'Repeats things.', turns: 3,\n" +
			"  onStart(h){ h.heal(1); }, onTurn(h, left){ h.heal(2); }, onEnd(h){ h.heal(5); } });\n" +
			"APD.item({ id: 'echoes', kind: 'potion', name: 'Potion of Echoes', sprite: 'POTION_AMBER',\n" +
			"  description: 'For a few turns, echoes.', dropWeight: 2, startCount: 1,\n" +
			"  onUse(h){ h.addEffect('echoes'); } });\n" +
			"APD.item({ id: 'mend', kind: 'scroll', name: 'Scroll of Mending', startCount: 0, dropWeight: 1,\n" +
			"  onUse(h){ h.heal(10); h.addBuff('haste', 5); h.message('mended'); } });\n";

	private ScriptModRuntime.LoadedMod echoes(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("chris.echoes", ECHOES);
		assertFalse(m.disabledReason(), m.disabled());
		return m;
	}

	@Test public void declaresItemsAndEffects(){
		ScriptModRuntime.LoadedMod m = echoes();
		assertEquals(2, m.items().size());
		assertEquals(1, m.effects().size());
		ScriptDefs.ItemDef p = ScriptModRuntime.item("chris.echoes", "echoes");
		assertEquals(ScriptDefs.Kind.POTION, p.kind);
		assertEquals("Potion of Echoes", p.name);
		assertEquals(2.0, p.dropWeight, 0);
		assertEquals(1, p.startCount);
		ScriptDefs.EffectDef e = ScriptModRuntime.effect("chris.echoes", "echoes");
		assertEquals(3, e.turns);
		assertTrue(e.has("onTurn"));
		assertFalse(e.has("onKill"));
	}

	@Test public void usingAnItemRunsTheModWithBoundedHeroActions(){
		echoes();
		ScriptItem scroll = new ScriptItem(ScriptModRuntime.item("chris.echoes", "mend"));
		assertEquals("Scroll of Mending", scroll.name());
		ScriptItem.use(hero, scroll.def());
		assertEquals(30, hero.HP);
		assertNotNull(hero.buff(Haste.class));
		// heal never overshoots max HP
		hero.HP = 48;
		ScriptItem.use(hero, scroll.def());
		assertEquals(50, hero.HP);
	}

	@Test public void effectsRunStartTurnAndEndThenGoAway(){
		echoes();
		ScriptItem.use(hero, ScriptModRuntime.item("chris.echoes", "echoes"));
		assertEquals("onStart heals 1", 21, hero.HP);
		ScriptEffect e = hero.buff(ScriptEffect.class);
		assertNotNull(e);
		assertEquals(3, e.turnsLeft());
		e.act(); e.act();
		assertEquals("two turns of +2", 25, hero.HP);
		e.act();
		assertEquals("third turn +2 then onEnd +5", 32, hero.HP);
		assertNull(hero.buff(ScriptEffect.class));
	}

	@Test public void mistakesTurnTheModOffWithAReadableReason(){
		String[][] bad = {
				{"APD.item({ id: 'x', kind: 'wand', name: 'X', onUse(h){} });", "kind must be potion, scroll or stone"},
				{"APD.item({ id: 'x', kind: 'potion', onUse(h){} });", "missing \"name\""},
				{"APD.item({ id: 'x', kind: 'potion', name: 'X' });", "onUse"},
				{"APD.item({ id: 'x', kind: 'potion', name: 'X', dropWeight: 50, onUse(h){} });", "dropWeight\" must be between 0 and 3"},
				{"APD.item({ id: 'x', kind: 'potion', name: 'X', startCount: 9, onUse(h){} });", "startCount\" must be between 0 and 5"},
				{"APD.item({ id: 'x', kind: 'potion', name: 'X', sprite: 'SCROLL_ODAL', onUse(h){} });", "isn't one of the potion sprites"},
				{"APD.item({ id: 'Bad Id', kind: 'potion', name: 'X', onUse(h){} });", "may only use"},
				{"APD.effect({ id: 'e', name: 'E', onTeleport(h){} });", "unknown trigger \"onTeleport\""},
				{"APD.item({ id: 'x', kind: 'potion', name: 'X', onUse(h){} }); APD.item({ id: 'x', kind: 'potion', name: 'Y', onUse(h){} });", "twice"},
		};
		for (String[] b : bad){
			ScriptModRuntime.clear();
			ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("bad.mod", b[0]);
			assertTrue(b[0], m.disabled());
			assertTrue(m.disabledReason() + " / expected " + b[1], m.disabledReason().contains(b[1]));
			assertTrue(m.disabledReason(), m.disabledReason().startsWith("Mod bad.mod was turned off"));
		}
	}

	@Test public void aBrokenOnUseTurnsOnlyThatModOffAndNeverThrows(){
		echoes();
		ScriptModRuntime.load("bad.loop", "APD.item({ id: 'spin', kind: 'stone', name: 'Spin', onUse(h){ while(true){} } });"
				+ "APD.item({ id: 'buff', kind: 'stone', name: 'B', onUse(h){ h.addBuff('godmode', 5); } });");
		ScriptItem.use(hero, ScriptModRuntime.item("bad.loop", "spin"));
		assertTrue(ScriptModRuntime.mod("bad.loop").disabled());
		ScriptItem spin = new ScriptItem();
		Bundle b = new Bundle();
		new ScriptItem(ScriptModRuntime.item("bad.loop", "buff")).storeInBundle(b);
		spin.restoreFromBundle(b);
		assertTrue("a turned-off mod's items stay but can't be used", spin.desc().contains("turned off"));
		assertFalse(spin.actions(hero).contains("USE"));
		// the other mod still works
		ScriptItem.use(hero, ScriptModRuntime.item("chris.echoes", "mend"));
		assertEquals(30, hero.HP);

		ScriptModRuntime.load("bad.buff", "APD.item({ id: 'b', kind: 'stone', name: 'B', onUse(h){ h.addBuff('godmode', 5); } });");
		ScriptItem.use(hero, ScriptModRuntime.item("bad.buff", "b"));
		assertTrue(ScriptModRuntime.mod("bad.buff").disabled());
		assertTrue(ScriptModRuntime.mod("bad.buff").disabledReason().contains("godmode"));
	}

	@Test public void amountsAndTurnsAreClamped(){
		ScriptModRuntime.load("big.mod", "APD.effect({ id: 'e', name: 'E', turns: 5 });"
				+ "APD.item({ id: 'i', kind: 'potion', name: 'I', onUse(h){ h.heal(1e12); h.addEffect('e', 1e9); } });");
		hero.HP = 1;
		ScriptItem.use(hero, ScriptModRuntime.item("big.mod", "i"));
		assertEquals(50, hero.HP);
		assertEquals(ScriptDefs.MAX_TURNS, hero.buff(ScriptEffect.class).turnsLeft());
	}

	@Test public void savedModItemsSurviveAndAMissingModNeverBreaksTheSave(){
		echoes();
		ScriptItem p = (ScriptItem) new ScriptItem(ScriptModRuntime.item("chris.echoes", "echoes")).quantity(3);
		Bundle b = new Bundle();
		p.storeInBundle(b);
		ScriptItem back = new ScriptItem();
		back.restoreFromBundle(b);
		assertEquals("Potion of Echoes", back.name());
		assertEquals(3, back.quantity());
		assertTrue(back.isSimilar(p));
		assertTrue(back.actions(hero).contains("DRINK"));

		ScriptModRuntime.clear(); // mod no longer installed
		ScriptItem orphan = new ScriptItem();
		orphan.restoreFromBundle(b);
		assertEquals("mod item (echoes)", orphan.name());
		assertFalse(orphan.actions(hero).contains("DRINK"));
		assertTrue(orphan.desc().contains("isn't active in this run"));
	}

	@Test public void startingKitUsesStartCount(){
		echoes();
		List<Item> kit = ScriptModRuntime.startingItems();
		assertEquals(1, kit.size());
		assertEquals("Potion of Echoes", kit.get(0).name());
		assertEquals(1, kit.get(0).quantity());
	}

	@Test public void noModsMeansNoRandomNumbersAndNoChange(){
		Random.pushGenerator(1234);
		try {
			assertNull(ScriptModRuntime.maybeDrop(Generator.Category.POTION));
			int next = Random.Int(1_000_000);
			Random.popGenerator();
			Random.pushGenerator(1234);
			assertEquals("maybeDrop with no mods used no random numbers", next, Random.Int(1_000_000));
		} finally { Random.popGenerator(); }
		// a mod with only starting-kit items doesn't touch the loot pool either
		ScriptModRuntime.load("kit.only", "APD.item({ id: 'k', kind: 'potion', name: 'K', startCount: 2, onUse(h){} });");
		assertEquals(0, ScriptModRuntime.modShare(Generator.Category.POTION), 0);
		assertEquals(0, ScriptModRuntime.modShare(Generator.Category.WEAPON), 0);
	}

	@Test public void dropWeightIsCappedSoModsCantFloodTheLootPool(){
		echoes(); // potion weight 2 against the potion category's 15
		double share = ScriptModRuntime.modShare(Generator.Category.POTION);
		assertEquals(2.0 / 17.0, share, 1e-9);
		// ten max-weight potions would be 30 vs 15; capped at 25%
		StringBuilder many = new StringBuilder();
		for (int i = 0; i < 10; i++) many.append("APD.item({ id: 'p").append(i).append("', kind: 'potion', name: 'P', dropWeight: 3, onUse(h){} });");
		ScriptModRuntime.load("flood.mod", many.toString());
		assertEquals(ScriptModRuntime.MAX_MOD_SHARE, ScriptModRuntime.modShare(Generator.Category.POTION), 1e-9);

		Random.pushGenerator(99);
		try {
			int mod = 0, n = 8000;
			for (int i = 0; i < n; i++) if (ScriptModRuntime.maybeDrop(Generator.Category.POTION) != null) mod++;
			double got = mod / (double) n;
			assertTrue("about 25% mod drops, got " + got, got > 0.22 && got < 0.28);
		} finally { Random.popGenerator(); }
	}

	@Test public void generatorRollsCanBecomeModItemsOnlyInTheirCategory(){
		Generator.fullReset();
		echoes();
		Random.pushGenerator(7);
		try {
			int modPotions = 0, n = 600;
			for (int i = 0; i < n; i++){
				Item it = Generator.random(Generator.Category.POTION);
				if (it instanceof ScriptItem){
					assertEquals("echoes", ((ScriptItem) it).itemId());
					modPotions++;
				}
			}
			assertTrue("some potions were mod potions: " + modPotions, modPotions > 20 && modPotions < 140);
			for (int i = 0; i < 200; i++) assertFalse(Generator.random(Generator.Category.WEAPON) instanceof ScriptItem);
		} finally { Random.popGenerator(); }
	}
}
