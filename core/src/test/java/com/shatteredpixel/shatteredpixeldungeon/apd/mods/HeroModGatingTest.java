package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.apd.community.ReviewClaim;
import com.shatteredpixel.shatteredpixeldungeon.apd.community.ReviewPlay;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashSet;

import static org.junit.Assert.*;

/** Mod-hero runs stay on the player's own app (Chris, Nov 19): no normal badges, marked Mod locally, no Community review. */
public class HeroModGatingTest {

	@After public void clear(){
		HeroModRun.starting = null; HeroModRun.active = null;
		ReviewPlay.starting = null; ReviewPlay.active = null;
	}

	private static void modRun() throws Exception {
		String json = HeroModTest.hero(null);
		HeroModRun.choose(HeroMod.parse(json), json);
		HeroModRun.beginRun();
	}

	@Test public void aModRunNeverUnlocksNormalBadges() throws Exception {
		Field f = Badges.class.getDeclaredField("global");
		f.setAccessible(true);
		Object old = f.get(null);
		HashSet<Badges.Badge> global = new HashSet<>();
		f.set(null, global);
		try {
			modRun();
			Badges.unlock(Badges.Badge.MASTERY_COMBO);
			assertTrue("mod run unlocked nothing", global.isEmpty());
			assertEquals("Battle Mage", HeroModRun.modName());
		} finally {
			f.set(null, old);
		}
	}

	@Test public void theRankingsRecordKeepsTheModMark_andNormalRecordsHaveNone(){
		Rankings.Record rec = new Rankings.Record();
		rec.modHero = "Battle Mage"; rec.customSeed = ""; rec.gameID = "g";
		Bundle b = new Bundle(); rec.storeInBundle(b);
		Rankings.Record back = new Rankings.Record(); back.restoreFromBundle(b);
		assertEquals("Battle Mage", back.modHero);

		Rankings.Record normal = new Rankings.Record(); normal.customSeed = ""; normal.gameID = "h";
		Bundle nb = new Bundle(); normal.storeInBundle(nb);
		assertFalse(nb.contains("apd_mod_hero_name"));
		Rankings.Record nback = new Rankings.Record(); nback.restoreFromBundle(nb);
		assertNull(nback.modHero);
		assertNull("normal run has no mod name", HeroModRun.modName());
	}

	@Test public void aReviewRunDropsAPendingModHero() throws Exception {
		String json = HeroModTest.hero(null);
		HeroModRun.choose(HeroMod.parse(json), json);
		ReviewPlay.starting = ReviewClaim.parse(new com.badlogic.gdx.utils.JsonReader().parse(
				"{\"id\":\"ab12\",\"seed\":\"XYZ-ABC-DEF\",\"deathDepth\":7,\"setup\":{\"challenges\":\"0\",\"customRules\":\"0\",\"difficulty\":\"3\",\"exotic\":\"0\",\"heroClass\":\"ROGUE\"}}"));
		assertNotNull(ReviewPlay.starting);
		assertNotNull(ReviewPlay.beginRun());
		HeroModRun.beginRun();
		assertFalse("review replays the normal hero", HeroModRun.isModRun());
	}
}
