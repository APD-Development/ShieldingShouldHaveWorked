package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;

//Guards against the Phase 2 regression where the APD controls existed but could not be found on a phone: they were only
//in the in-game menu, not in Options. Windows can't be rendered headless, so this checks the wiring in the source.
public class UiEntryPointsTest {

	private static String windows( String name ) throws Exception {
		File f = new File("../core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/" + name);
		if (!f.exists()) f = new File("src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/" + name);
		if (!f.exists()) f = new File(System.getProperty("apd.windows.src"), name);
		java.io.FileInputStream in = new java.io.FileInputStream(f);
		try {
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int n;
			while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
			return new String(out.toByteArray(), Charset.forName("UTF-8"));
		} finally {
			in.close();
		}
	}

	@Test
	public void theApdPaneHasTheRecordingControls() throws Exception {
		String pane = windows("APDSettingsPane.java");
		assertTrue(pane.contains("\"Record run history\""));
		assertTrue(pane.contains("ApdRecorder.status().summary()"));
		assertTrue(pane.contains("\"Run self-test\""));
		assertTrue(pane.contains("SPDSettings.apdRecording("));
		//every control must be added to the pane and positioned
		for (String c : new String[]{"optExotic", "info", "chkRecord", "status", "btnTest"}){
			assertTrue(c + " not added", pane.contains("add( " + c + " )"));
			assertTrue(c + " not laid out", pane.contains(c + ".setRect(") || pane.contains(c + ".setPos("));
		}
	}

	@Test
	public void optionsHasAnApdTabThatShowsThePane() throws Exception {
		String s = windows("WndSettings.java");
		assertTrue(s.contains("new APDSettingsPane()"));
		assertTrue(s.contains("add( apd )"));
		assertTrue(s.contains("new LabeledTab(\"APD\")"));
	}

	@Test
	public void theInGameMenuAlsoOpensThePane() throws Exception {
		assertTrue(windows("WndGame.java").contains("new WndAPDSettings()"));
		assertTrue(windows("WndAPDSettings.java").contains("new APDSettingsPane()"));
	}
}
