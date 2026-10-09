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
	@Test public void unknownAgeNeverMakesASavedAdultPreferenceEffective(){
		assertFalse(AgeEligibility.adultHumor(AgeEligibility.State.UNKNOWN, true));
		assertFalse(AgeEligibility.adultHumor(null, true));
	}
	@Test public void unavailableVerificationFailsClosed(){
		assertFalse(AgeEligibility.adultHumor(AgeEligibility.State.UNAVAILABLE, true));
		assertEquals(AgeEligibility.State.UNAVAILABLE, AgeEligibility.current());
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
