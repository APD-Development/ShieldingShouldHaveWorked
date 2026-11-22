package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** Script mods, monsters hook: declare, stats, spawn weight with the 25% cap, loot, triggers, saves, broken mods. */
public class ScriptMonstersTest {
	private Hero oldHero;

	@Before public void setUp(){
		HeadlessTestSupport.start();
		ScriptModRuntime.clear();
		oldHero = Dungeon.hero;
		Dungeon.hero = new Hero();
	}
	@After public void tearDown(){ ScriptModRuntime.clear(); Dungeon.hero = oldHero; }

	static final String MOD =
			"APD.item({ id: 'shell', kind: 'stone', name: 'Test Shell', onUse(h){ h.heal(1); } });\n" +
			"APD.monster({ id: 'crab', name: 'Test Crab', sprite: 'CrabSprite', hp: 30, attack: [2, 6], accuracy: 12,\n" +
			"  evasion: 5, armor: 3, speed: 1.5, xp: 4, spawn: { floors: [3, 8], weight: 2 }, loot: 'shell', lootChance: 0.5,\n" +
			"  onHurt(m, dmg){ m.heal(1); } });\n";

	@Test public void declaresAMonsterWithItsStats(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.mobs", MOD);
		assertFalse(m.disabledReason(), m.disabled());
		ScriptDefs.MonsterDef d = ScriptModRuntime.monster("test.mobs", "crab");
		assertEquals(30, d.hp); assertEquals(2, d.attackMin); assertEquals(6, d.attackMax);
		assertEquals(3, d.floorFrom); assertEquals(8, d.floorTo); assertEquals(2.0, d.spawnWeight, 0);
		ScriptMob mob = new ScriptMob(d);
		assertEquals("Test Crab", mob.name());
		assertEquals(30, mob.HP); assertEquals(30, mob.HT);
		assertEquals(CrabSprite.class, mob.spriteClass);
		assertEquals(12, mob.attackSkill(null));
		for (int i = 0; i < 100; i++){ int r = mob.damageRoll(); assertTrue(r >= 2 && r <= 6); }
		assertTrue(mob.createLoot() instanceof ScriptItem);
	}

	@Test public void armorTakesANumberOrMinMax(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("test.armor",
				"APD.monster({ id: 'a', name: 'A', sprite: 'RatSprite', armor: [2, 5] });\n" +
				"APD.monster({ id: 'b', name: 'B', sprite: 'RatSprite', armor: 4 });");
		assertFalse(m.disabledReason(), m.disabled());
		ScriptDefs.MonsterDef a = ScriptModRuntime.monster("test.armor", "a"), b = ScriptModRuntime.monster("test.armor", "b");
		assertEquals(2, a.armorMin); assertEquals(5, a.armor);
		assertEquals(0, b.armorMin); assertEquals(4, b.armor);
		ScriptModRuntime.clear();
		m = ScriptModRuntime.load("bad.armor", "APD.monster({ id: 'x', name: 'X', sprite: 'RatSprite', armor: [6, 1] });");
		assertTrue(m.disabled()); assertTrue(m.disabledReason(), m.disabledReason().contains("\"armor\" must be [min, max]"));
	}

	@Test public void mistakesTurnTheModOff(){
		String[][] bad = {
				{"APD.monster({ id: 'x', name: 'X', sprite: 'NotASprite' });", "isn't an existing monster sprite"},
				{"APD.monster({ id: 'x', name: 'X', sprite: 'RatSprite', hp: 999999 });", "\"hp\" must be between 1 and 2000"},
				{"APD.monster({ id: 'x', name: 'X', sprite: 'RatSprite', attack: [9, 2] });", "[min, max]"},
				{"APD.monster({ id: 'x', name: 'X', sprite: 'RatSprite', speed: 50 });", "\"speed\" must be between"},
				{"APD.monster({ id: 'x', name: 'X', sprite: 'RatSprite', spawn: { floors: [1, 5], weight: 99 } });", "\"weight\" must be between 0 and 3"},
				{"APD.monster({ id: 'x', name: 'X', sprite: 'RatSprite', onExplode(m){} });", "unknown trigger"},
				{"APD.monster({ id: 'x', name: 'X', sprite: 'HeroSprite' });", "isn't an existing monster sprite"},
		};
		for (String[] b : bad){
			ScriptModRuntime.clear();
			ScriptModRuntime.LoadedMod m = ScriptModRuntime.load("bad.mobs", b[0]);
			assertTrue(b[0], m.disabled());
			assertTrue(m.disabledReason() + " / expected " + b[1], m.disabledReason().contains(b[1]));
		}
	}

	@Test public void spawnsOnlyOnItsFloorsAndModsCantFloodSpawns(){
		ScriptModRuntime.load("test.mobs", MOD);
		assertEquals(0, ScriptModRuntime.monsterShare(2), 0);
		assertEquals(0, ScriptModRuntime.monsterShare(9), 0);
		assertEquals(2.0 / 12.0, ScriptModRuntime.monsterShare(5), 1e-9);
		StringBuilder many = new StringBuilder();
		for (int i = 0; i < 10; i++) many.append("APD.monster({ id: 'm").append(i).append("', name: 'M', sprite: 'RatSprite', spawn: { floors: [1, 99], weight: 3 } });");
		ScriptModRuntime.load("flood.mobs", many.toString());
		assertEquals(ScriptModRuntime.MAX_MOD_SHARE, ScriptModRuntime.monsterShare(5), 1e-9);
		Random.pushGenerator(3);
		try {
			int mod = 0, n = 8000;
			for (int i = 0; i < n; i++) if (ScriptModRuntime.maybeSpawn(5) != null) mod++;
			double got = mod / (double) n;
			assertTrue("about 25%, got " + got, got > 0.22 && got < 0.28);
		} finally { Random.popGenerator(); }
	}

	@Test public void noModMonstersMeansNoRandomNumbers(){
		Random.pushGenerator(77);
		try {
			assertNull(ScriptModRuntime.maybeSpawn(4));
			int next = Random.Int(1_000_000);
			Random.popGenerator();
			Random.pushGenerator(77);
			assertEquals(next, Random.Int(1_000_000));
		} finally { Random.popGenerator(); }
	}

	@Test public void triggersRunAndSavesKeepTheMonster(){
		ScriptModRuntime.load("test.mobs", MOD);
		ScriptMob mob = new ScriptMob(ScriptModRuntime.monster("test.mobs", "crab"));
		mob.HP = 20;
		mob.trigger("onHurt", 3.0);
		assertEquals("onHurt healed 1", 21, mob.HP);
		Bundle b = new Bundle();
		mob.storeInBundle(b);
		ScriptMob back = new ScriptMob();
		back.restoreFromBundle(b);
		assertEquals("Test Crab", back.name());
		assertEquals(21, back.HP);
		assertEquals(CrabSprite.class, back.spriteClass);

		ScriptModRuntime.clear();
		ScriptMob orphan = new ScriptMob();
		orphan.restoreFromBundle(b);
		assertEquals("mod monster", orphan.name());
		assertNull(orphan.def());
	}
}
