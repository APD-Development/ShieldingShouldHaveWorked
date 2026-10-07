package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

//UIMSD's screen is wired in through APD Settings only. The title screen stays exactly as approved, and the screen's generic
//parts do not know any particular dataset.
public class UimsdWiringTest {

	private static String src( String relative ) throws Exception {
		return new String(Files.readAllBytes(new File(System.getProperty("apd.game.src"), relative).toPath()), "UTF-8");
	}

	private static String sha( String relative ) throws Exception {
		byte[] d = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(new File(System.getProperty("apd.game.src"), relative).toPath()));
		StringBuilder b = new StringBuilder();
		for (byte x : d) b.append(String.format("%02x", x));
		return b.toString();
	}

	@Test
	public void theTitleScreenIsExactlyAsApprovedAndFrozen() throws Exception {
		//If this fails the frozen title screen was edited. Update these only when the project owner asked for a title change.
		assertEquals("2c4c5f3b063380385da9318d62e58d2ab0b63c0b433bb2815006ced9f416cd90", sha("scenes/TitleScene.java"));
		assertEquals("627e2dae33f1173fd88548f4217dbc505ef02b8dcbe70dbfe4d71aab9b792134", sha("apd/TitleGeometry.java"));
	}

	@Test
	public void uimsdIsOpenedFromApdSettingsAndTheSceneReturnsToWhereItCameFrom() throws Exception {
		String pane = src("windows/APDSettingsPane.java");
		assertTrue(pane.contains("\"Open UIMSD (preview)\""));
		assertTrue(pane.contains("UimsdScene.open("));
		String scene = src("scenes/UimsdScene.java");
		assertTrue(scene.contains("backTo"));
		assertTrue(scene.contains("onBackPressed"));
		//the title scene does not know about the new scene
		assertFalse(src("scenes/TitleScene.java").contains("UimsdScene"));
	}

	@Test
	public void theScreenAndItsWindowsNeverNameAShippedDatasetOrColumn() throws Exception {
		String[] files = { "scenes/UimsdScene.java", "ui/UimsdTable.java", "windows/WndUimsdList.java", "windows/WndUimsdFilter.java",
				"windows/WndUimsdSort.java", "windows/WndUimsdColumns.java", "windows/WndUimsdOptions.java", "windows/WndUimsdRow.java",
				"windows/WndUimsdStatus.java", "windows/WndUimsdText.java" };
		for (String f : files){
			String text = src(f);
			for (String bad : new String[]{"FactKeys", "Datasets", "\"run.", "\"item.", "\"enemy.", "\"death.", "RowBuilders", "LegacyImporter"}){
				assertFalse(f + " mentions " + bad, text.contains(bad));
			}
		}
	}

	@Test
	public void theScreenNeverTouchesTheGameStateOrTheRecorderBeyondReading() throws Exception {
		for (String f : new String[]{"scenes/UimsdScene.java", "ui/UimsdTable.java", "windows/WndUimsdList.java", "windows/WndUimsdFilter.java",
				"windows/WndUimsdRow.java", "windows/WndUimsdOptions.java"}){
			String text = src(f);
			for (String bad : new String[]{"Dungeon.hero", "Dungeon.level", "Random.", "Actor.", "Statistics.", "Rankings.", ".damage(", ".spend("}){
				assertFalse(f + " touches " + bad, text.contains(bad));
			}
		}
		//the recorder is only asked to write out what is queued, and for where the history is
		String scene = src("scenes/UimsdScene.java");
		assertTrue(scene.contains("flushForReading"));
	}
}
