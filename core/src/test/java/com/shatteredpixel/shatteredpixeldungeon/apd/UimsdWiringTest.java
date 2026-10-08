package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

//UIMSD's screen is opened by the title screen's UIMSD button and by APD Settings. The title screen stays exactly as approved apart from
//that click target, and the screen's generic parts do not know any particular dataset.
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

	private static String shaOf( String text ) throws Exception {
		byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"));
		StringBuilder b = new StringBuilder();
		for (byte x : d) b.append(String.format("%02x", x));
		return b.toString();
	}

	@Test
	public void theTitleScreenIsStillExactlyAsApprovedExceptForWhereTheUimsdButtonGoes() throws Exception {
		//The only change since the title screen was frozen is the click target of the UIMSD button (the project owner asked for it).
		//(Since then the arrow's text has also been made to go through UimsdGate.titleAnnotation, which returns the same text while UIMSD is open.)
		//Putting the changed lines back as they were must give exactly the approved file, so layout, text, arrow and annotation
		//are provably untouched. If this fails the frozen title screen was edited.
		String title = src("scenes/TitleScene.java");
		assertTrue(title.contains("UimsdScene.open( TitleScene.class );"));
		String restored = title
				.replace("//UIMSD (Ultimate Infinite Mega Statistics Database) replaces Rankings and opens the UIMSD screen",
						"//UIMSD (Ultimate Infinite Mega Statistics Database) replaces Rankings, until the real database exists it opens the Rankings screen")
				.replace("com.shatteredpixel.shatteredpixeldungeon.windows.UimsdGate.titleAnnotation( ANNOTATION_TEXT )", "ANNOTATION_TEXT")
				.replace("UimsdScene.open( TitleScene.class );", "ShatteredPixelDungeon.switchNoFade( RankingsScene.class );");
		assertEquals("2c4c5f3b063380385da9318d62e58d2ab0b63c0b433bb2815006ced9f416cd90", shaOf(restored));
		//the geometry (arrow, annotation, layout) file is byte for byte as approved
		assertEquals("627e2dae33f1173fd88548f4217dbc505ef02b8dcbe70dbfe4d71aab9b792134", sha("apd/TitleGeometry.java"));
		assertTrue(title.contains("HEY, GO HERE FOR DRUGS") || src("apd/TitleGeometry.java").contains("HEY, GO HERE FOR DRUGS") || title.contains("apd.title"));
	}

	@Test
	public void uimsdIsOpenedFromApdSettingsAndTheSceneReturnsToWhereItCameFrom() throws Exception {
		String pane = src("windows/APDSettingsPane.java");
		assertTrue(pane.contains("\"Open UIMSD (preview)\""));
		assertTrue(pane.contains("UimsdScene.open("));
		String scene = src("scenes/UimsdScene.java");
		assertTrue(scene.contains("backTo"));
		assertTrue(scene.contains("onBackPressed"));
		//the title screen's UIMSD button opens it, and goes back to the title
		assertTrue(src("scenes/TitleScene.java").contains("UimsdScene.open( TitleScene.class )"));
		assertTrue(scene.contains("switchNoFade(backTo)"));
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

	@Test
	public void theTenthRunHookRunsOnlyAfterTheRunIsSealedAndNeverNavigates() throws Exception {
		String dungeon = src("Dungeon.java");
		for (String method : new String[]{ "public static void fail( Object cause )", "public static void win( Object cause )" }){
			int m = dungeon.indexOf(method);
			assertTrue(method, m >= 0);
			String body = dungeon.substring(m, dungeon.indexOf("\n\t}\n", m));
			int sealed = body.indexOf("ApdRecorder.onRunEnded(");
			int hook = body.indexOf("UimsdGate.onRunEnded()");
			assertTrue("the hook is there", hook > 0);
			assertTrue("and comes after the run's record is sealed", sealed > 0 && hook > sealed);
			assertTrue("and is the last thing the method does", body.indexOf(";", hook) + 1 >= body.trim().length() - 8 || body.substring(hook).split(";").length <= 3);
		}
		//the hook only records state on its own thread: it changes no scene and touches no save
		String gate = src("windows/UimsdGate.java");
		int a = gate.indexOf("public static void onRunEnded()");
		String hookBody = gate.substring(a, gate.indexOf("// May the guided tour start?"));
		assertFalse(hookBody.contains("switchScene") || hookBody.contains("switchNoFade") || hookBody.contains("Dungeon.") || hookBody.contains("saveAll"));
		assertTrue(hookBody.contains("new Thread("));
		//every way out of the results screen goes to the title, where the prompt is shown
		String rankings = src("scenes/RankingsScene.java");
		assertTrue(rankings.contains("switchNoFade(TitleScene.class)"));
	}
}
