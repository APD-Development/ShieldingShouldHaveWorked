package com.shatteredpixel.shatteredpixeldungeon.apd.community;

import com.badlogic.gdx.utils.JsonReader;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReviewPlayTest {

	private static ReviewClaim claim(int deathDepth){
		return ReviewClaim.parse(new JsonReader().parse("{\"id\":\"ab\",\"seed\":\"S\",\"deathDepth\":" + deathDepth
				+ ",\"setup\":{\"challenges\":\"0\",\"customRules\":\"0\",\"difficulty\":\"3\",\"exotic\":\"0\",\"heroClass\":\"WARRIOR\"}}"));
	}

	@After public void clear(){
		ReviewPlay.starting = null; ReviewPlay.active = null; CommunityController.finishedReview = null;
	}

	@Test public void aNormalRunIsNeverAReview(){
		ReviewPlay.active = claim(5);
		assertNull(ReviewPlay.beginRun()); // no pending claim: old review state is dropped
		assertFalse(ReviewPlay.isReviewRun());
		ReviewPlay.onFloor(9); ReviewPlay.onDeath();
		assertNull(CommunityController.finishedReview);
	}

	@Test public void reachingTheDeathFloorCountsOnce(){
		ReviewClaim c = claim(4);
		ReviewPlay.starting = c;
		assertSame(c, ReviewPlay.beginRun());
		assertNull(ReviewPlay.starting);
		assertTrue(ReviewPlay.isReviewRun());
		ReviewPlay.onFloor(1); ReviewPlay.onFloor(3);
		assertNull(CommunityController.finishedReview);
		ReviewPlay.onFloor(4);
		assertSame(c, CommunityController.finishedReview);
		assertTrue(c.reachedDeathFloor());
		CommunityController.finishedReview = null; // verdict sent
		ReviewPlay.onFloor(5); ReviewPlay.onDeath(); // later floors/death don't reopen it
		assertNull(CommunityController.finishedReview);
		assertFalse(ReviewPlay.isReviewRun());
	}

	@Test public void dyingFirstIsAFailedReviewThatStillCounts(){
		ReviewPlay.starting = claim(6);
		ReviewClaim c = ReviewPlay.beginRun();
		ReviewPlay.onFloor(2);
		ReviewPlay.onDeath();
		assertSame(c, CommunityController.finishedReview);
		assertEquals(2, c.reachedDepth());
		assertFalse(c.reachedDeathFloor());
		assertFalse(ReviewPlay.isReviewRun());
	}

	@Test public void theOneAttemptSurvivesSaveAndLoad(){
		ReviewPlay.starting = claim(6);
		ReviewPlay.beginRun();
		ReviewPlay.onFloor(3);
		Bundle saved = new Bundle();
		ReviewPlay.store(saved);
		ReviewPlay.active = null;
		ReviewPlay.restore(saved);
		assertTrue(ReviewPlay.isReviewRun());
		assertEquals(3, ReviewPlay.active.reachedDepth());
		assertEquals("S", ReviewPlay.active.seed);

		Bundle normal = new Bundle();
		ReviewPlay.restore(normal); // loading a normal run clears it
		assertFalse(ReviewPlay.isReviewRun());
		ReviewPlay.store(normal);
		assertFalse(normal.contains("apd_review"));
	}
}
