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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.Difficulty;

import java.util.HashMap;
import java.util.Map;

/** Run reviews (Chris's design): the reviewer plays EXACTLY the original setup (class, challenges, difficulty,
 *  settings). This is the one place that writes that setup when a run is sent, and reads it back (validated) when a
 *  reviewer claims it. Pure: no game state, no settings writes. Loot generation is untouched ("luck drop" is still an
 *  open question for Chris). */
public final class ReviewSetup {

	public final HeroClass heroClass;
	public final int challenges, difficulty, exotic;
	public final boolean customRules;

	private ReviewSetup(HeroClass heroClass, int challenges, int difficulty, int exotic, boolean customRules){
		this.heroClass = heroClass; this.challenges = challenges; this.difficulty = difficulty;
		this.exotic = exotic; this.customRules = customRules;
	}

	/** What a run sends. An unknown difficulty is not sent, so the reviewer side refuses it rather than guessing. */
	public static Map<String, String> capture(HeroClass heroClass, int challenges, int difficulty, boolean difficultyKnown,
	                                          int exotic, boolean customRules){
		Map<String, String> m = new HashMap<>();
		m.put("heroClass", heroClass.name());
		m.put("challenges", Integer.toString(challenges));
		if (difficultyKnown) m.put("difficulty", Integer.toString(difficulty));
		m.put("exotic", Integer.toString(exotic));
		m.put("customRules", customRules ? "1" : "0");
		return m;
	}

	/** Whether this run can be reviewed with an exact setup. Custom dungeon rules aren't sent yet, so those runs can't. */
	public static boolean reviewable(boolean difficultyKnown, boolean customRules){
		return difficultyKnown && !customRules;
	}

	/** Reads a claimed setup back; null when anything is missing or out of range (never a guessed default). */
	public static ReviewSetup parse(Map<String, String> m){
		if (m == null) return null;
		HeroClass hc;
		try { hc = HeroClass.valueOf(m.get("heroClass")); } catch (RuntimeException e){ return null; }
		Integer ch = intOf(m.get("challenges")), diff = intOf(m.get("difficulty")), ex = intOf(m.get("exotic"));
		if (ch == null || ch < 0 || ch > Challenges.MAX_VALUE) return null;
		if (diff == null || !Difficulty.isValid(diff)) return null;
		if (ex == null || ex < 0) return null;
		if ("1".equals(m.get("customRules"))) return null;
		return new ReviewSetup(hc, ch, diff, ex, false);
	}

	/** A reviewer "counts" once they reach the floor where the original player died; dying before is a failed review. */
	public static boolean survived(int reachedDepth, int deathDepth){
		return reachedDepth >= deathDepth;
	}

	private static Integer intOf(String s){
		if (s == null) return null;
		try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e){ return null; }
	}
}
