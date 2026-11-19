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

import com.badlogic.gdx.utils.JsonValue;
import com.watabou.utils.Bundle;

import java.util.HashMap;
import java.util.Map;

/** Run reviews: what a reviewer gets back from POST /v1/reviews/{id}/claim, validated strictly, plus the one-attempt
 *  progress (deepest floor reached) that the in-game playthrough will report. The seed is held here only to start the
 *  game; nothing in this class displays it. Pure: no settings, no game start, loot untouched ("luck drop" still open). */
public final class ReviewClaim {

	public final String requestId, seed;
	public final int deathDepth;
	public final ReviewSetup setup;
	public final boolean roasts;
	/** What the reviewed player asked for: roasts / advice / encouraging (Chris, Nov 19). */
	public final String reviewType;
	private int reachedDepth;
	/** The server has answered this reviewer's verdict (accepted or refused for good); never ask again. */
	public volatile boolean sent;

	private ReviewClaim(String requestId, String seed, int deathDepth, ReviewSetup setup, boolean roasts, String reviewType, int reachedDepth){
		this.requestId = requestId; this.seed = seed; this.deathDepth = deathDepth;
		this.setup = setup; this.roasts = roasts; this.reachedDepth = reachedDepth;
		this.reviewType = type(reviewType, roasts);
	}
	static String type(String t, boolean roasts){
		if ("roasts".equals(t) || "advice".equals(t) || "encouraging".equals(t)) return t;
		return roasts ? "roasts" : "advice";
	}

	/** null when anything is missing or invalid: never start a review game from a guessed setup or seed. */
	public static ReviewClaim parse(JsonValue b){
		if (b == null) return null;
		String id = b.getString("id", null), seed = b.getString("seed", null);
		if (id == null || !id.matches("[0-9a-f]+") || seed == null || seed.trim().isEmpty()) return null;
		JsonValue d = b.get("deathDepth");
		if (d == null || !d.isNumber()) return null;
		int depth = d.asInt();
		if (depth < 1 || depth > 100 || d.asDouble() != depth) return null;
		JsonValue s = b.get("setup");
		if (s == null || !s.isObject()) return null;
		Map<String, String> m = new HashMap<>();
		for (JsonValue v = s.child; v != null; v = v.next) if (v.isValue()) m.put(v.name, v.asString());
		ReviewSetup setup = ReviewSetup.parse(m);
		if (setup == null) return null;
		return new ReviewClaim(id, seed, depth, setup, b.getBoolean("roasts", false), b.getString("reviewType", null), 0);
	}

	/** Called whenever the reviewer arrives on a floor; only ever goes up. */
	public void reached(int depth){ if (depth > reachedDepth) reachedDepth = Math.min(depth, 100); }
	public int reachedDepth(){ return reachedDepth; }
	/** Reaching the original death floor ends the review as counted; dying first is a failed review (still counts). */
	public boolean reachedDeathFloor(){ return ReviewSetup.survived(reachedDepth, deathDepth); }

	// ---- survives an app restart mid-review (one attempt, no retry) ----
	private static final String ID = "rv_id", SEED = "rv_seed", DEPTH = "rv_death", ROASTS = "rv_roasts", REACHED = "rv_reached", TYPE = "rv_type", SENT = "rv_sent";
	private static final String[] SETUP_KEYS = {"heroClass", "challenges", "difficulty", "exotic", "customRules"};

	public void store(Bundle bundle){
		bundle.put(ID, requestId); bundle.put(SEED, seed); bundle.put(DEPTH, deathDepth);
		bundle.put(ROASTS, roasts); bundle.put(REACHED, reachedDepth); bundle.put(TYPE, reviewType); bundle.put(SENT, sent);
		Map<String, String> m = ReviewSetup.capture(setup.heroClass, setup.challenges, setup.difficulty, true, setup.exotic, setup.rules);
		for (String k : SETUP_KEYS) bundle.put("rv_s_" + k, m.get(k));
	}

	public static ReviewClaim restore(Bundle bundle){
		if (bundle == null || !bundle.contains(ID) || !bundle.contains(SEED) || !bundle.contains(DEPTH)) return null;
		Map<String, String> m = new HashMap<>();
		for (String k : SETUP_KEYS) if (bundle.contains("rv_s_" + k)) m.put(k, bundle.getString("rv_s_" + k));
		ReviewSetup setup = ReviewSetup.parse(m);
		String id = bundle.getString(ID), seed = bundle.getString(SEED);
		int depth = bundle.getInt(DEPTH);
		if (setup == null || !id.matches("[0-9a-f]+") || seed.trim().isEmpty() || depth < 1 || depth > 100) return null;
		ReviewClaim c = new ReviewClaim(id, seed, depth, setup, bundle.getBoolean(ROASTS), bundle.contains(TYPE) ? bundle.getString(TYPE) : null, 0);
		c.reached(bundle.getInt(REACHED));
		c.sent = bundle.contains(SENT) && bundle.getBoolean(SENT);
		return c;
	}
}
