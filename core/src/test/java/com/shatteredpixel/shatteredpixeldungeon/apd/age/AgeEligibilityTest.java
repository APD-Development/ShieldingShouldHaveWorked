/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

package com.shatteredpixel.shatteredpixeldungeon.apd.age;

import org.junit.Test;
import static org.junit.Assert.*;

public class AgeEligibilityTest {
	@Test public void eligibleAdultsStillChooseWhetherToEnableHumor(){
		assertTrue(AgeEligibility.adultHumor(AgeEligibility.State.ELIGIBLE, true));
		assertFalse(AgeEligibility.adultHumor(AgeEligibility.State.ELIGIBLE, false));
	}
	@Test public void ineligiblePlayersNeverReceiveAdultHumor(){
		assertFalse(AgeEligibility.adultHumor(AgeEligibility.State.INELIGIBLE, true));
	}
	@Test public void unknownAgeAllowsAnExplicitAdultChoiceWithoutClaimingVerification(){
		assertTrue(AgeEligibility.adultHumor(AgeEligibility.State.UNKNOWN, true));
		assertTrue(AgeEligibility.adultHumor(null, true));
		assertFalse(AgeEligibility.adultHumor(AgeEligibility.State.UNKNOWN, false));
	}
	@Test public void unavailableVerificationAllowsThePlayersChoice(){
		assertTrue(AgeEligibility.adultHumor(AgeEligibility.State.UNAVAILABLE, true));
		assertFalse(AgeEligibility.adultHumor(AgeEligibility.State.UNAVAILABLE, false));
		assertEquals(AgeEligibility.State.UNAVAILABLE, AgeEligibility.current());
	}
	@Test public void onlyVerifiedAdultsDefaultToAdultHumor(){
		for (AgeEligibility.State state : AgeEligibility.State.values()) {
			boolean defaultChoice = AgeEligibility.defaultAdultHumor(state);
			assertEquals(state == AgeEligibility.State.ELIGIBLE, defaultChoice);
			assertEquals(state == AgeEligibility.State.ELIGIBLE, AgeEligibility.adultHumor(state, defaultChoice));
		}
		assertFalse(AgeEligibility.defaultAdultHumor(null));
	}
	@Test public void anExplicitCleanChoiceOverridesTheVerifiedAdultDefault(){
		assertTrue(AgeEligibility.defaultAdultHumor(AgeEligibility.State.ELIGIBLE));
		assertFalse(AgeEligibility.adultHumor(AgeEligibility.State.ELIGIBLE, false));
	}
	@Test public void settingsAndTitleAreWiredThroughThePolicy() throws Exception {
		java.io.File root = new java.io.File(System.getProperty("apd.game.src"));
		String settings = java.nio.file.Files.readString(new java.io.File(root, "SPDSettings.java").toPath());
		assertTrue(settings.contains("AgeEligibility.adultHumor("));
		assertTrue(settings.contains("AgeEligibility.permitsAdult("));
		String title = java.nio.file.Files.readString(new java.io.File(root, "scenes/TitleScene.java").toPath());
		assertFalse(title.contains("HEY, GO HERE FOR DRUGS"));
		assertTrue(title.contains("annotation.visible = !annotationText.isEmpty()"));
	}
}
