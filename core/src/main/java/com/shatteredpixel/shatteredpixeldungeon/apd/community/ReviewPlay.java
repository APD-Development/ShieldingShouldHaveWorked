/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.community;

import com.watabou.utils.Bundle;

/** Run reviews: the one review playthrough in progress (Chris's design). The game starts from {@link #starting}
 *  (seed + exact setup, never the player's own settings, which are left untouched), keeps {@link #active} for the
 *  whole run (saved with it, so a restart keeps the one attempt), and hands the claim to the verdict screen when the
 *  reviewer reaches the original death floor or dies first. The seed is never shown: a review run has no custom seed
 *  text and its ranking keeps no seed. Loot generation is untouched. */
public final class ReviewPlay {

	private ReviewPlay(){}

	/** Set just before a review game starts; consumed by Dungeon.init. */
	public static volatile ReviewClaim starting = null;
	/** The review run being played (null in every normal run). */
	public static volatile ReviewClaim active = null;

	public static boolean isReviewRun(){ return active != null; }

	/** Dungeon.init: take the pending claim (if any) as this run's review. Any older review state is dropped, so a
	 *  normal new run is never treated as a review. */
	public static ReviewClaim beginRun(){
		ReviewClaim c = starting;
		starting = null;
		active = c;
		return c;
	}

	/** The reviewer arrived on a floor. Reaching the original death floor ends the review as counted (once). */
	public static void onFloor(int depth){
		ReviewClaim c = active;
		if (c == null || depth < 1) return;
		boolean before = c.reachedDeathFloor();
		c.reached(depth);
		if (!before && c.reachedDeathFloor()) finish(c);
	}

	/** The reviewer died. If they hadn't reached the death floor yet it's a failed review, which still counts. */
	public static void onDeath(){
		ReviewClaim c = active;
		if (c == null) return;
		if (!c.reachedDeathFloor()) finish(c);
		active = null;
	}

	private static void finish(ReviewClaim c){
		if (CommunityController.finishedReview == null) CommunityController.finishedReview = c;
	}

	// ---- saved with the run ----
	private static final String NODE = "apd_review";

	public static void store(Bundle bundle){
		ReviewClaim c = active;
		if (c == null) return;
		Bundle b = new Bundle();
		c.store(b);
		bundle.put(NODE, b);
	}

	/** Loading a run: a review run gets its claim back; any other run clears review state. A damaged review record
	 *  is dropped rather than guessed. */
	public static void restore(Bundle bundle){
		active = bundle != null && bundle.contains(NODE) ? ReviewClaim.restore(bundle.getBundle(NODE)) : null;
	}
}
