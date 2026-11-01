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
		// Only the exact legal notice may differ; retain both original executable-source hashes.
		String notice = " * ADHD Pixel Dungeon\n * Copyright (C) 2026 Chris Charlton\n *\n";
		int at = text.indexOf(notice);
		if (at >= 0) {
			assertTrue("Notice is confined to the initial copyright header", at < text.indexOf("package "));
			assertEquals("One APD notice only", -1, text.indexOf(notice, at + notice.length()));
			text = text.substring(0, at) + text.substring(at + notice.length());
		}
		byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"));
		StringBuilder b = new StringBuilder();
		for (byte x : d) b.append(String.format("%02x", x));
		return b.toString();
	}

	@Test
	public void theTitleScreenMatchesApprovedResponsiveLayoutAndKeepsItsUimsdTarget() throws Exception {
		//The only change since the title screen was frozen is the click target of the UIMSD button (the project owner asked for it).
		//The owner also authorized fixing premature adult wording and retiring the arrow.
		//The owner also authorized measured wrapping for the overflowing instruction.
		//October16: the owner additionally authorized the responsive footer/compact-art correction.
		//These exact source snapshots now freeze that approved layout as well; real geometry, click
		//and Android touch regressions separately verify its behavior. Other frozen source stays protected.
		//Putting the changed lines back as they were must give exactly the approved file, so layout, text, arrow and annotation
		//remain locked to the approved revision. A new intentional layout requires explicit review.
		String title = src("scenes/TitleScene.java");
		// The Hide UI footer's reviewed hitbox-only change is explicitly protected here,
		// while the original responsive layout checksum remains exactly unchanged.
		String reviewedFooterTouchPatch = "\t\t\t@Override\n\t\t\tprotected void layout() {\n\t\t\t\tsuper.layout();\n\t\t\t\t// Expand only the hit area into the reserved footer space. Keep the\n\t\t\t\t// 16px icon, existing Hide UI effect and all menu geometry intact.\n\t\t\t\tif (hotArea != null) {\n\t\t\t\t\thotArea.x = x - TitleGeometry.FADE_TOUCH_SIDE_PADDING;\n\t\t\t\t\thotArea.y = y - TitleGeometry.FADE_TOUCH_TOP_PADDING;\n\t\t\t\t\thotArea.width = width + 2 * TitleGeometry.FADE_TOUCH_SIDE_PADDING;\n\t\t\t\t\thotArea.height = height + TitleGeometry.FADE_TOUCH_TOP_PADDING;\n\t\t\t\t}\n\t\t\t}\n";
		String reviewedFooterPriority = "\t\t// The full-screen restore listener must not swallow a footer-button press.\n\t\tbtnFade.givePointerPriority();\n";
		String reviewedUimsdPrewarm = "\t\t// Prepare the exact UIMSD Home in the background while the title is open.\n\t\t// When it is ready, tapping UIMSD does not begin a new history load.\n\t\tUimsdScene.prewarmTitle();\n";
		assertTrue("Reviewed button patch must be present",title.contains(reviewedFooterTouchPatch));
		assertTrue("Reviewed footer pointer priority must be present",title.contains(reviewedFooterPriority));
		assertTrue("Reviewed prewarm must remain immediately after fade-in",
				title.contains("\t\tfadeIn();\n" + reviewedUimsdPrewarm));
		assertTrue("Do not silently remove the new target behavior",title.contains("hotArea.width = width + 2 * TitleGeometry.FADE_TOUCH_SIDE_PADDING;"));
		assertTrue(title.contains("UimsdScene.open( TitleScene.class );"));
		String reviewedViewportCover = "\t\tplate.scale.set(TitleGeometry.backgroundCoverScale(fullW,fullH,plate.width(),plate.height(),Camera.main.zoom));\n";
        assertTrue("Approved scale-independent plate must remain",title.contains(reviewedViewportCover));
        assertTrue("Only the 24x18 button frame belongs in the menu nine-patch",title.contains("bg = new NinePatch( \"interfaces/apd_button.png\", 0, 0, 24, 18, 8 );"));
        String restored = title.replace("bg = new NinePatch( \"interfaces/apd_button.png\", 0, 0, 24, 18, 8 );", "bg = new NinePatch( \"interfaces/apd_button.png\", 8 );").replace(reviewedViewportCover, "").replace(reviewedFooterTouchPatch, "").replace(reviewedFooterPriority, "").replace(reviewedUimsdPrewarm, "").replace("\t\tfinal boolean androidTopScores = DeviceCompat.isAndroid();\n\t\tbtnQuit = new APDMenuButton( androidTopScores ? \"Top Scores\" : \"Quit\", androidTopScores ? Icons.RANKINGS : Icons.EXIT, true ){", "\t\tbtnQuit = new APDMenuButton( \"Quit\", Icons.EXIT, true ){")
				.replace("\t\t\t\tif (androidTopScores) {\n\t\t\t\t\tShatteredPixelDungeon.switchNoFade( RankingsScene.class );\n\t\t\t\t} else {\n\t\t\t\t\tGame.instance.finish();\n\t\t\t\t}", "\t\t\t\tGame.instance.finish();")
				.replace("\t\tadd(new com.watabou.noosa.PointerArea(0, 0, 0, 0){\n\t\t\t@Override public void update(){\n\t\t\t\tsuper.update();\n\t\t\t\tfloat left = Float.MAX_VALUE, top = Float.MAX_VALUE, right = 0, bottom = 0;\n\t\t\t\tfor (BitmapText line : annotation.lines){\n\t\t\t\t\tleft = Math.min(left, line.x); top = Math.min(top, line.y);\n\t\t\t\t\tright = Math.max(right, line.x + line.width()); bottom = Math.max(bottom, line.y + line.height());\n\t\t\t\t}\n\t\t\t\tx = left; y = top; width = Math.max(0, right-left); height = Math.max(0, bottom-top);\n\t\t\t\tactive = annotation.visible;\n\t\t\t}\n\t\t\t@Override protected void onClick(com.watabou.input.PointerEvent event){\n\t\t\t\tcom.shatteredpixel.shatteredpixeldungeon.windows.UimsdGate.open(TitleScene.class);\n\t\t\t}\n\t\t});\n" , "")
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
		assertEquals("0051ccab7317b9aead08e57541ceb619cf26158df6a6485b8210a68dcd7e2f0a", shaOf(restored));
		//the geometry (arrow, annotation, layout) file is byte for byte as approved
		String reviewedCoverGeometry = "    /** Keep background pixels uniform and cover the viewport independently of UI scale. */\n    public static float backgroundCoverScale(float width,float height,float imageWidth,float imageHeight,float zoom){\n        if(!(width>0&&height>0&&imageWidth>0&&imageHeight>0&&zoom>0)\n                ||!Float.isFinite(width)||!Float.isFinite(height)||!Float.isFinite(imageWidth)||!Float.isFinite(imageHeight)||!Float.isFinite(zoom))throw new IllegalArgumentException(\"Invalid background dimensions\");\n        return (float)Math.ceil(Math.max(width*zoom/imageWidth,height*zoom/imageHeight))/zoom;\n    }\n\n";
        assertTrue(src("apd/TitleGeometry.java").contains(reviewedCoverGeometry));
        String reviewedFooterGeometry = "\t// Make the small footer icon easier to touch without moving the artwork.\n\tpublic static final int FADE_TOUCH_SIDE_PADDING = 8;\n\tpublic static final int FADE_TOUCH_TOP_PADDING = FOOTER_GAP;\n";
		assertTrue("The approved footer constants must remain present",src("apd/TitleGeometry.java").contains(reviewedFooterGeometry));
		assertEquals("6a71f191a2fc104bff81e9994f7a1102db217f7d6ce48e4738c4adbaacaa4329", shaOf(src("apd/TitleGeometry.java").replace(reviewedCoverGeometry, "").replace(reviewedFooterGeometry, "").replaceAll("(?s)\\tpublic interface TextWidth.*?(?=\\t//height of each)", "")
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
	public void aReadyHomeBuildsAfterSceneCreationButBeforeItsFirstVisibleDraw() throws Exception {
		String scene = src("scenes/UimsdScene.java");
		int create = scene.indexOf("public void create()");
		int draw = scene.indexOf("public void draw()", create);
		int drawHelper = scene.indexOf("private boolean rebuildIfNeeded()", draw);
		assertTrue("reviewed scene lifecycle methods exist", create >= 0 && draw > create && drawHelper > draw);
		String createBody = scene.substring(create, draw);
		String drawBody = scene.substring(draw, drawHelper);
		assertFalse("READY Home layout must not run inside scene creation", createBody.contains("rebuild();"));
		assertTrue("the first draw still builds pending READY content before drawing",
				drawBody.contains("if (rebuildIfNeeded()) Camera.refreshMatrices();")
						&& drawBody.indexOf("rebuildIfNeeded()") < drawBody.indexOf("super.draw();"));
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
	@Test
	public void theTablesPickerProvidesAnExactSceneRecreationRestorer() throws Exception {
		String scene = src("scenes/UimsdScene.java");
		int start = scene.indexOf("private void pickView()");
		int end = scene.indexOf("//---- the interactive tour ----", start);
		assertTrue("Tables picker method is present", start >= 0 && end > start);
		String picker = scene.substring(start, end);
		assertTrue("parameterized Tables picker must not fall through reflection restoration",
				picker.contains(".restoreWith(() -> {"));
		assertTrue("restoration targets only a rebuilt UIMSD scene",
				picker.contains("active instanceof UimsdScene") && picker.contains("((UimsdScene) active).pickView();"));
	}

	@Test
	public void searchResultsRestoreTheirExactQueryAndTargetTheRebuiltScene() throws Exception {
		String search = src("windows/WndUimsdSearch.java");
		assertTrue("parameterized search results must provide an exact scene-reset restorer",
				search.contains("}).restoreWith(() -> results(catalog,term,opener)))"));

		String scene = src("scenes/UimsdScene.java");
		assertTrue("search selections must not retain the destroyed scene instance",
				scene.contains("model.catalog(),UimsdScene::openSearchResult"));
		int start = scene.indexOf("private static void openSearchResult(String viewId)");
		int end = scene.indexOf("\n    }", start);
		assertTrue("rebuilt-scene search result router is present", start >= 0 && end > start);
		String router = scene.substring(start, end);
		assertTrue("result routing is limited to the active rebuilt UIMSD scene",
				router.contains("ShatteredPixelDungeon.scene() instanceof UimsdScene")
						&& router.contains("((UimsdScene)ShatteredPixelDungeon.scene()).openView(viewId)"));
	}

}

