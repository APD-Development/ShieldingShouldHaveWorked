package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

import org.junit.Test;

import static org.junit.Assert.*;

// Chris, Nov 19: modders must be able to add heroes, built from pieces that already exist (data only, no code).
public class HeroModTest {

	public static String hero(String over){
		String base = "{\"format\":1,\"id\":\"chris.battle-mage\",\"name\":\"Battle Mage\",\"author\":\"Chris\","
				+ "\"baseClass\":\"WARRIOR\",\"hp\":22,\"str\":11,"
				+ "\"startingItems\":[{\"item\":\"weapon.melee.Sword\",\"level\":1}],"
				+ "\"talents\":{\"tier1\":[\"HEARTY_MEAL\",\"SCHOLARS_INTUITION\",\"SUCKER_PUNCH\",\"NATURES_AID\"],"
				+ "\"tier2\":%T2%,\"tier3\":%T3%},"
				+ "\"armorAbilities\":[\"HeroicLeap\",\"WarpBeacon\"]}";
		base = base.replace("%T2%", names(HeroClass.WARRIOR, 2)).replace("%T3%", names(HeroClass.MAGE, 3));
		return over == null ? base : over.replace("$", base);
	}

	static String names(HeroClass hc, int tier){
		java.util.ArrayList<java.util.LinkedHashMap<Talent, Integer>> t = new java.util.ArrayList<>();
		Talent.initClassTalents(hc, t);
		StringBuilder b = new StringBuilder("[");
		for (Talent x : t.get(tier - 1).keySet()){ if (b.length() > 1) b.append(','); b.append('"').append(x.name()).append('"'); }
		return b.append(']').toString();
	}

	@Test public void aMixedHeroFromExistingPiecesParses() throws Exception {
		HeroMod m = HeroMod.parse(hero(null));
		assertEquals("chris.battle-mage", m.id);
		assertEquals(HeroClass.WARRIOR, m.baseClass);
		assertEquals(22, m.hp); assertEquals(11, m.str);
		assertEquals(Sword.class, m.startingItems.get(0).type);
		assertEquals(1, m.startingItems.get(0).level);
		assertEquals(Talent.SCHOLARS_INTUITION, m.talentTiers.get(0).get(1)); // a Mage talent on a Warrior-based hero
		assertEquals(HeroicLeap.class, m.armorAbilities.get(0));
		assertEquals(WarpBeacon.class, m.armorAbilities.get(1));
	}

	private static void refused(String json, String reason){
		try { HeroMod.parse(json); fail("accepted: " + reason); }
		catch (HeroMod.Invalid e){ assertTrue(e.getMessage(), e.getMessage().contains(reason)); }
	}

	@Test public void badFilesAreRefusedWithAReasonAModderCanFix(){
		refused("not json {", "JSON"); // lenient reader: "not valid JSON" or "must be one JSON object"
		refused("[1,2]", "must be one JSON object");
		refused(hero(null).replace("\"format\":1", "\"format\":2"), "unsupported format");
		refused(hero(null).replace("chris.battle-mage", "Bad ID!"), "id may only use");
		refused(hero(null).replace("\"WARRIOR\"", "\"WIZARD\""), "baseClass must be one of");
		refused(hero(null).replace("\"hp\":22", "\"hp\":0"), "hp must be between");
		refused(hero(null).replace("\"str\":11", "\"str\":1.5"), "str must be a whole number");
		refused(hero(null).replace("weapon.melee.Sword", "weapon.melee.Lightsaber"), "no such item");
		refused(hero(null).replace("weapon.melee.Sword", "weapon.Weapon"), "not an item that can be given");
		refused(hero(null).replace("weapon.melee.Sword", "../../Evil"), "bad item name");
		refused(hero(null).replace("\"HEARTY_MEAL\"", "\"MADE_UP\""), "unknown talent");
		refused(hero(null).replace("\"HEARTY_MEAL\"", "\"SCHOLARS_INTUITION\""), "listed twice");
		refused(hero(null).replace("\"SUCKER_PUNCH\",", ""), "needs exactly 4 talents");
		refused(hero(null).replace("\"NATURES_AID\"", "\"" + firstOf(HeroClass.ROGUE, 2) + "\""), "is not a tier 1 talent");
		refused(hero(null).replace("\"WarpBeacon\"", "\"Laser\""), "unknown armor ability");
		refused(hero(null).replace("\"armorAbilities\":[\"HeroicLeap\",\"WarpBeacon\"]", "\"armorAbilities\":[]"), "at least one ability");
	}

	static String firstOf(HeroClass hc, int tier){
		java.util.ArrayList<java.util.LinkedHashMap<Talent, Integer>> t = new java.util.ArrayList<>();
		Talent.initClassTalents(hc, t);
		return t.get(tier - 1).keySet().iterator().next().name();
	}
}
