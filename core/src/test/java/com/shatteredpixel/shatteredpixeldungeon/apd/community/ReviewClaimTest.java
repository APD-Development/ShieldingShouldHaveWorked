package com.shatteredpixel.shatteredpixeldungeon.apd.community;

import com.badlogic.gdx.utils.JsonReader;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReviewClaimTest {

	private static String claim(String id, String seed, String depth, String setupJson){
		return "{\"id\":" + id + ",\"seed\":" + seed + ",\"deathDepth\":" + depth + ",\"setup\":" + setupJson + ",\"roasts\":true}";
	}
	private static final String SETUP = "{\"challenges\":\"5\",\"customRules\":\"0\",\"difficulty\":\"3\",\"exotic\":\"1\",\"heroClass\":\"ROGUE\"}";

	private static ReviewClaim parse(String s){ return ReviewClaim.parse(new JsonReader().parse(s)); }

	@Test public void readsTheServersClaimExactly(){
		ReviewClaim c = parse(claim("\"ab12\"", "\"XYZ-ABC-DEF\"", "7", SETUP));
		assertNotNull(c);
		assertEquals("ab12", c.requestId);
		assertEquals("XYZ-ABC-DEF", c.seed);
		assertEquals(7, c.deathDepth);
		assertEquals(HeroClass.ROGUE, c.setup.heroClass);
		assertEquals(5, c.setup.challenges);
		assertEquals(3, c.setup.difficulty);
		assertEquals(1, c.setup.exotic);
		assertTrue(c.roasts);
		assertEquals(0, c.reachedDepth());
	}

	@Test public void refusesAnythingMissingOrMalformed(){
		assertNull(ReviewClaim.parse(null));
		assertNull(parse(claim("\"../x\"", "\"S\"", "7", SETUP)));
		assertNull(parse(claim("\"ab\"", "\"  \"", "7", SETUP)));
		assertNull(parse(claim("\"ab\"", "\"S\"", "0", SETUP)));
		assertNull(parse(claim("\"ab\"", "\"S\"", "101", SETUP)));
		assertNull(parse(claim("\"ab\"", "\"S\"", "7.5", SETUP)));
		assertNull(parse(claim("\"ab\"", "\"S\"", "\"7\"", SETUP)));
		assertNull(parse(claim("\"ab\"", "\"S\"", "7", "{\"heroClass\":\"ROGUE\",\"challenges\":\"0\"}")));
		assertNull(parse(claim("\"ab\"", "\"S\"", "7", "[]")));
		assertNull(parse("{\"seed\":\"S\",\"deathDepth\":7,\"setup\":" + SETUP + "}"));
	}

	@Test public void deepestFloorOnlyGoesUpAndDecidesTheReview(){
		ReviewClaim c = parse(claim("\"ab\"", "\"S\"", "7", SETUP));
		c.reached(3); c.reached(2);
		assertEquals(3, c.reachedDepth());
		assertFalse(c.reachedDeathFloor());
		c.reached(7);
		assertTrue(c.reachedDeathFloor());
	}

	@Test public void survivesARestartWithoutLosingProgress(){
		ReviewClaim c = parse(claim("\"ab\"", "\"S\"", "7", SETUP));
		c.reached(4);
		Bundle b = new Bundle();
		c.store(b);
		ReviewClaim r = ReviewClaim.restore(b);
		assertNotNull(r);
		assertEquals("ab", r.requestId);
		assertEquals("S", r.seed);
		assertEquals(7, r.deathDepth);
		assertEquals(4, r.reachedDepth());
		assertEquals(HeroClass.ROGUE, r.setup.heroClass);
		assertEquals(3, r.setup.difficulty);
		assertTrue(r.roasts);
		assertNull(ReviewClaim.restore(new Bundle()));
		assertNull(ReviewClaim.restore(null));
	}
}
