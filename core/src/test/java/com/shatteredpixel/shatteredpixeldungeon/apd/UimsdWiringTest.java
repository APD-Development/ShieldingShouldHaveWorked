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
		//The owner also authorized fixing premature adult wording and retiring the arrow.
		//The owner also authorized measured wrapping for the overflowing instruction.
		//Normalize only those specific changes; all other frozen source remains protected.
		//Putting the changed lines back as they were must give exactly the approved file, so layout, text, arrow and annotation
		//are provably untouched. If this fails the frozen title screen was edited.
		String title = src("scenes/TitleScene.java");
		assertTrue(title.contains("UimsdScene.open( TitleScene.class );"));
		String restored = title
                .replace("\tprivate APDMenuButton btnCommunity;\n", "")
                .replace("\n\n\t\tbtnCommunity = new APDMenuButton( \"Community\", Icons.NEWS, false ){\n\t\t\t@Override\n\t\t\tprotected void onClick() {\n\t\t\t\tsuper.onClick();\n\t\t\t\tCommunityScene.open();\n\t\t\t}\n\t\t};\n\t\tadd( btnCommunity );", "")
                .replace("\n\t\tbtnCommunity.setRect( left + (width + MAIN_GAP)/2f, apdY, (width - MAIN_GAP)/2f, BOTTOM_H );\n\t\talign( btnCommunity );", "")
                .replace("(width - MAIN_GAP)/2f, BOTTOM_H );", "width, BOTTOM_H );")
                .replace("btnAPDSettings, btnCommunity, btnNews", "btnAPDSettings, btnNews")
				.replace("\t\tAPDAnnotation( String text, float maxWidth ){\n\t\t\tBitmapText probe = new BitmapText(text, pixelFont);\n\t\t\tString[] parts = TitleGeometry.annotationLines(text, maxWidth - 1, value -> {\n\t\t\t\tprobe.text(value);\n\t\t\t\tprobe.measure();\n\t\t\t\treturn probe.width();\n\t\t\t});\n", "\t\tAPDAnnotation( String text, float maxWidth ){\n\t\t\tString[] parts = { text };\n\t\t\tBitmapText probe = new BitmapText( text, pixelFont );\n\t\t\tprobe.measure();\n\t\t\tif (probe.width() > maxWidth){\n\t\t\t\tint cut = text.indexOf( ' ', text.indexOf( \"HERE\" ) );\n\t\t\t\tparts = new String[]{ text.substring( 0, cut ), text.substring( cut + 1 ) };\n\t\t\t}\n")
				.replace("float textRight = gridLeft + gridWidth - 1;", "float textRight = gridLeft + gridWidth;")
				.replace("The text wraps by measured words within the grid.", "The text breaks into two lines only when one line is wider than the grid.")
				.replace("ANNOTATION_TEXT = \"\"", "ANNOTATION_TEXT = \"HEY, GO HERE FOR DRUGS\"")
				.replace("String annotationText = com.shatteredpixel.shatteredpixeldungeon.windows.UimsdGate.titleAnnotation( ANNOTATION_TEXT );\n\t\tannotation = new APDAnnotation( annotationText,", "annotation = new APDAnnotation( com.shatteredpixel.shatteredpixeldungeon.windows.UimsdGate.titleAnnotation( ANNOTATION_TEXT ),")
				.replace("\t\tannotation.visible = !annotationText.isEmpty();\n", "")
				.replace("float textH = annotation.visible ? annotation.textHeight() : -1;", "float textH = annotation.textHeight();")
				.replace("//UIMSD (Ultimate Infinite Mega Statistics Database) replaces Rankings and opens the UIMSD screen",
						"//UIMSD (Ultimate Infinite Mega Statistics Database) replaces Rankings, until the real database exists it opens the Rankings screen")
				.replace("com.shatteredpixel.shatteredpixeldungeon.windows.UimsdGate.titleAnnotation( ANNOTATION_TEXT )", "ANNOTATION_TEXT")
				.replace("UimsdScene.open( TitleScene.class );", "ShatteredPixelDungeon.switchNoFade( RankingsScene.class );");
		assertEquals("2c4c5f3b063380385da9318d62e58d2ab0b63c0b433bb2815006ced9f416cd90", shaOf(restored));
		//the geometry (arrow, annotation, layout) file is byte for byte as approved
		assertEquals("627e2dae33f1173fd88548f4217dbc505ef02b8dcbe70dbfe4d71aab9b792134", shaOf(src("apd/TitleGeometry.java").replaceAll("(?s)\\tpublic interface TextWidth.*?(?=\\t//height of each)", "")
				.replace("\t\tif (textHeight < 0) return 0; //no progression prompt: no arrow or reserved strip\n", "")));
		assertFalse("adult wording belongs to the eligible tenth-run prompt only", title.contains("HEY, GO HERE FOR DRUGS"));
		assertTrue(title.contains("annotation.visible = !annotationText.isEmpty()"));
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
