package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.UimsdTourFlag;
import com.watabou.utils.GameSettings;
import java.io.File;
import java.nio.file.Files;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

// Chris, Nov 26: after taking the tour the welcome card still offered "Take the tour (2 minutes)". It hides after the
// first view, survives a restart, and the scene only offers it while the flag is unset.
public class UimsdTourHidesAfterFirstViewTest {
	@BeforeClass public static void headless(){ HeadlessTestSupport.start(); }
	@After public void clear(){
		SPDSettings.apdUimsdTourDone(false);
		//back to the shared test settings file
		GameSettings.set(new HeadlessPreferences(new FileHandle(new File(HeadlessTestSupport.tempDir(), "prefs/settings.xml"))));
		SPDSettings.apdUimsdTourDone(false);
	}

	@Test public void theButtonIsGoneOnceTheTourEnds(){
		SPDSettings.apdUimsdTourDone(false);
		assertTrue(UimsdTourFlag.showWelcomeTourButton());
		UimsdTourFlag.tourEnded();
		assertFalse(UimsdTourFlag.showWelcomeTourButton());
	}

	@Test public void andStaysGoneAfterTheAppRestarts(){
		File file = new File(HeadlessTestSupport.tempDir(), "prefs/tour-restart.xml");
		file.delete();
		GameSettings.set(new HeadlessPreferences(new FileHandle(file)));
		assertTrue(UimsdTourFlag.showWelcomeTourButton());
		UimsdTourFlag.tourEnded();
		//a restart: settings read back from the file on disk into a fresh preferences object
		GameSettings.set(new HeadlessPreferences(new FileHandle(file)));
		assertFalse(UimsdTourFlag.showWelcomeTourButton());
	}

	@Test public void everyWayATourEndsMarksIt() throws Exception {
		String scene = new String(Files.readAllBytes(new File(System.getProperty("apd.game.src"), "scenes/UimsdScene.java").toPath()), "UTF-8");
		int end = scene.indexOf("private static void endTutorial(){");
		assertTrue(end >= 0);
		assertTrue("Finish, Exit tour and Back all end through endTutorial, which marks the tour taken",
				scene.substring(end, end + 400).contains("UimsdTourFlag.tourEnded()"));
		int card = scene.indexOf("\"Take the tour (2 minutes)\"");
		assertTrue("the welcome card asks the flag before offering the tour",
				scene.lastIndexOf("UimsdTourFlag.showWelcomeTourButton()", card) > scene.lastIndexOf("private void buildFirstRun", card));
	}

	// Chris, Nov 26: APD Settings > History > "Offer the UIMSD tour again" brings the button back
	@Test public void theSettingBringsTheButtonBack() throws Exception {
		UimsdTourFlag.tourEnded();
		assertFalse(UimsdTourFlag.showWelcomeTourButton());
		SPDSettings.apdUimsdTourDone(false); //what ticking the setting does
		assertTrue(UimsdTourFlag.showWelcomeTourButton());
		String pane = new String(Files.readAllBytes(new File(System.getProperty("apd.game.src"), "windows/APDSettingsPane.java").toPath()), "UTF-8");
		assertTrue(pane.contains("\"Offer the UIMSD tour again\"") && pane.contains("SPDSettings.apdUimsdTourDone(!checked())"));
	}
}
