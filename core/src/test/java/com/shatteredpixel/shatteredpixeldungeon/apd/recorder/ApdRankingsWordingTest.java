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

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

//The import reads APD's own rankings.dat. Official Shattered Pixel Dungeon is a different app whose data APD cannot read, so
//nothing the player can read may suggest that it imports from the official game.
public class ApdRankingsWordingTest {

	private static void collect( File f, List<File> out ){
		File[] kids = f.listFiles();
		if (kids == null){
			String n = f.getName();
			if (n.endsWith(".java") || n.endsWith(".txt")) out.add(f);
			return;
		}
		for (File k : kids) collect(k, out);
	}

	@Test
	public void noTextClaimsToImportFromOfficialSpd() throws Exception {
		File main = new File(System.getProperty("apd.game.src"));
		while (main != null && !main.getName().equals("main")) main = main.getParentFile();
		assertTrue(main != null);
		List<File> files = new ArrayList<>();
		collect(new File(main, "java/com/shatteredpixel/shatteredpixeldungeon/apd"), files);
		collect(new File(main, "java/com/shatteredpixel/shatteredpixeldungeon/windows"), files);
		collect(new File(main, "assets/apd"), files);
		String[] forbidden = { "Import SPD records", "No SPD rankings", "Your SPD rankings", "imported SPD records",
				"Import from SPD", "official SPD records" };
		for (File f : files){
			String text = new String(Files.readAllBytes(f.toPath()), "UTF-8");
			for (String bad : forbidden) assertFalse(f.getName() + " says \"" + bad + "\"", text.contains(bad));
		}
	}

	@Test
	public void theButtonAndTheMessagesSayAPD() throws Exception {
		File pane = new File(System.getProperty("apd.game.src"), "windows/APDSettingsPane.java");
		String text = new String(Files.readAllBytes(pane.toPath()), "UTF-8");
		assertTrue(text.contains("\"Import APD rankings\""));
		LegacyImporter.Report none = new LegacyImporter.Report();
		assertTrue(none.summary(), none.summary().contains("APD rankings file"));
		LegacyImporter.Report some = new LegacyImporter.Report();
		some.fileFound = true;
		some.records = 3;
		some.imported = 2;
		some.alreadyKnown = 1;
		assertTrue(some.summary(), some.summary().contains("APD ranking record"));
		assertEquals(-1, some.summary().indexOf("SPD"));
	}
}
