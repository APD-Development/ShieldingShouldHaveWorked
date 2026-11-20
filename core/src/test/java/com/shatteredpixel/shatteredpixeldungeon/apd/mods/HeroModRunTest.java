package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class HeroModRunTest {

	@After public void clear(){ HeroModRun.starting = null; HeroModRun.restore(null); }

	@Test public void aDamagedModRecordStaysAModRun_andIsKeptInTheSave(){
		Bundle save = new Bundle();
		save.put("apd_mod_hero", "{ not valid json");
		HeroModRun.restore(save);
		assertTrue("an unreadable mod record must never turn into a normal run", HeroModRun.isModRun());
		assertTrue(HeroModRun.isDamaged());
		assertEquals("Unreadable mod", HeroModRun.modName());
		Bundle again = new Bundle();
		HeroModRun.store(again);
		assertEquals("raw record written back unchanged", "{ not valid json", again.getString("apd_mod_hero"));
		HeroModRun.beginRun();
		assertFalse("a new normal run clears it", HeroModRun.isModRun());
	}

	@Test public void startingItemQuantity_isHonouredForStackableAndNonStackableItems() throws Exception {
		HeroMod.StartingItem sword = new HeroMod.StartingItem(
				com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword.class, 2, 3);
		java.util.List<com.shatteredpixel.shatteredpixeldungeon.items.Item> swords = HeroModRun.startingItems(sword);
		assertEquals("quantity 3 of a non-stackable item = 3 separate items", 3, swords.size());
		for (com.shatteredpixel.shatteredpixeldungeon.items.Item i : swords){
			assertEquals(1, i.quantity());
			assertEquals(2, i.level());
		}
		HeroMod.StartingItem potions = new HeroMod.StartingItem(
				com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class, 0, 3);
		java.util.List<com.shatteredpixel.shatteredpixeldungeon.items.Item> stack = HeroModRun.startingItems(potions);
		assertEquals("stackable = one stack", 1, stack.size());
		assertEquals(3, stack.get(0).quantity());
	}

	@Test public void aNormalRunIsUntouched(){
		HeroModRun.beginRun();
		assertFalse(HeroModRun.isModRun());
		assertFalse(HeroModRun.fillTalents(new ArrayList<>()));
		assertEquals(20, HeroModRun.baseHT(20));
		assertEquals(HeroClass.MAGE.armorAbilities().length, HeroModRun.armorAbilities(HeroClass.MAGE).length);
		assertEquals("Warrior", HeroModRun.heroName("Warrior"));
	}

	@Test public void aModRunUsesTheModsPieces_andTheModTravelsInsideTheSave() throws Exception {
		String json = HeroModTest.hero(null);
		HeroModRun.choose(HeroMod.parse(json), json);
		HeroModRun.beginRun();
		assertTrue(HeroModRun.isModRun());
		assertNull("taken once", HeroModRun.starting);
		assertEquals(22, HeroModRun.baseHT(20));
		assertEquals("Battle Mage", HeroModRun.heroName("Warrior"));

		ArrayList<LinkedHashMap<Talent, Integer>> tiers = new ArrayList<>();
		assertTrue(HeroModRun.fillTalents(tiers));
		assertEquals(Talent.MAX_TALENT_TIERS, tiers.size());
		assertTrue(tiers.get(0).containsKey(Talent.SCHOLARS_INTUITION));
		assertEquals(Integer.valueOf(0), tiers.get(0).get(Talent.SCHOLARS_INTUITION));
		assertFalse("class list replaced", tiers.get(0).containsKey(Talent.VETERANS_INTUITION));

		ArmorAbility[] abil = HeroModRun.armorAbilities(HeroClass.WARRIOR);
		assertEquals(2, abil.length);
		assertTrue(abil[0] instanceof HeroicLeap && abil[1] instanceof WarpBeacon);

		Bundle save = new Bundle();
		HeroModRun.store(save);
		HeroModRun.active = null;
		HeroModRun.restore(save);
		assertTrue("restored from the save, not the mods folder", HeroModRun.isModRun());
		assertEquals("chris.battle-mage", HeroModRun.active.id);

		HeroModRun.restore(new Bundle()); // loading a normal save clears it
		assertFalse(HeroModRun.isModRun());
		Bundle normal = new Bundle();
		HeroModRun.store(normal);
		assertFalse(normal.contains("apd_mod_hero"));
	}

	@Test public void aNewNormalRunAfterAModRunIsNormalAgain() throws Exception {
		String json = HeroModTest.hero(null);
		HeroModRun.choose(HeroMod.parse(json), json);
		HeroModRun.beginRun();
		HeroModRun.beginRun(); // next new run, nothing chosen
		assertFalse(HeroModRun.isModRun());
	}

	@Test public void heroTalentsComeFromTheModOnlyDuringAModRun() throws Exception {
		Hero hero = new Hero();
		hero.heroClass = HeroClass.WARRIOR;
		Talent.initClassTalents(hero);
		assertTrue(hero.talents.get(0).containsKey(Talent.VETERANS_INTUITION));

		String json = HeroModTest.hero(null);
		HeroModRun.choose(HeroMod.parse(json), json);
		HeroModRun.beginRun();
		Hero modded = new Hero();
		modded.heroClass = HeroClass.WARRIOR;
		Talent.initClassTalents(modded);
		assertTrue(modded.talents.get(0).containsKey(Talent.SCHOLARS_INTUITION));
		assertFalse(modded.talents.get(0).containsKey(Talent.VETERANS_INTUITION));
	}

	@Test public void continuingADamagedModSaveGivesAClearMessage_normalSavesGiveNone(){
		Bundle b = new Bundle();
		HeroModRun.restore(b);
		assertNull(HeroModRun.loadMessage());
		b.put("apd_mod_hero", "{not json");
		HeroModRun.restore(b);
		assertNotNull(HeroModRun.loadMessage());
		assertTrue(HeroModRun.loadMessage().contains("could not be read"));
		HeroModRun.restore(new Bundle());
		assertNull(HeroModRun.loadMessage());
	}
}
