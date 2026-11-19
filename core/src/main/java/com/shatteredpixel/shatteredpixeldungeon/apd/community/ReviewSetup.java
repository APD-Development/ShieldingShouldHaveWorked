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
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
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
	/** The run's exact custom dungeon rules (Chris, Nov 19: reviewable, LOCKED for the reviewer); null for none. */
	public final CustomDungeonRules rules;

	private ReviewSetup(HeroClass heroClass, int challenges, int difficulty, int exotic, CustomDungeonRules rules){
		this.heroClass = heroClass; this.challenges = challenges; this.difficulty = difficulty;
		this.exotic = exotic; this.rules = rules; this.customRules = rules != null;
	}

	/** What a run sends. An unknown difficulty is not sent, so the reviewer side refuses it rather than guessing. */
	public static Map<String, String> capture(HeroClass heroClass, int challenges, int difficulty, boolean difficultyKnown,
	                                          int exotic, CustomDungeonRules rules){
		Map<String, String> m = new HashMap<>();
		m.put("heroClass", heroClass.name());
		m.put("challenges", Integer.toString(challenges));
		if (difficultyKnown) m.put("difficulty", Integer.toString(difficulty));
		m.put("exotic", Integer.toString(exotic));
		// "0" = no custom rules; otherwise every rule setting, so the reviewer plays exactly the same rules.
		m.put("customRules", rules == null ? "0" : rules.preferenceText());
		return m;
	}

	/** Whether this run can be reviewed with an exact setup. Custom-rule runs are reviewable (their full settings are sent). */
	public static boolean reviewable(boolean difficultyKnown){
		return difficultyKnown;
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
		String cr = m.get("customRules");
		if (cr == null) return null;
		CustomDungeonRules rules = null;
		if (!"0".equals(cr)){
			// An old flag-only "1" (or anything incomplete/invalid) has no settings to lock to: refused, never guessed.
			try { rules = CustomDungeonRules.fromPreferenceText(cr); } catch (RuntimeException e){ return null; }
		}
		return new ReviewSetup(hc, ch, diff, ex, rules);
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
