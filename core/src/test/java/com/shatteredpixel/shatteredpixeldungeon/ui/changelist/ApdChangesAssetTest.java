package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApdChangesAssetTest {

	static String asset() throws Exception {
		Path p = Paths.get("src/main/assets/apd/changes.tsv");
		if (!Files.exists(p)) p = Paths.get("core/src/main/assets/apd/changes.tsv");
		return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
	}

	@Test
	public void everyLineParsesAndEveryIconResolves() throws Exception {
		String text = asset();
		assertTrue(ApdChanges.tabs(text).size() >= 2);
		boolean header = false;
		int buttons = 0;
		for (String raw : text.split("\n")){
			if (raw.isEmpty() || raw.startsWith("#")) continue;
			String[] f = raw.split("\t", -1);
			switch (f[0]){
				case "TAB": assertEquals(raw, 2, f.length); header = false; break;
				case "MAJOR": case "BUILD": assertEquals(raw, 2, f.length); header = true; break;
				case "BTN":
					assertEquals(raw, 4, f.length);
					assertTrue("button before any header: " + raw, header);
					assertFalse(f[2].isEmpty());
					assertFalse(f[3].isEmpty());
					String icon = f[1];
					if (icon.startsWith("i:")) Icons.valueOf(icon.substring(2));
					else if (icon.startsWith("c:")) ChangeIcons.valueOf(icon.substring(2));
					else if (icon.startsWith("s:")) ApdChanges.sprite(icon.substring(2));
					else fail("unknown icon " + icon);
					buttons++;
					break;
				default: fail("unknown line " + raw);
			}
		}
		assertTrue(buttons > 50);
	}
}
