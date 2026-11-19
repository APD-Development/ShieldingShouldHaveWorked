package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ModLibraryTest {
	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	static byte[] pngHeader(int w, int h) throws Exception {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream d = new DataOutputStream(b);
		d.write(new byte[]{(byte)0x89, 'P', 'N', 'G', 13, 10, 26, 10});
		d.writeInt(13); d.writeBytes("IHDR"); d.writeInt(w); d.writeInt(h);
		d.write(new byte[]{8, 6, 0, 0, 0}); d.writeInt(0);
		return b.toByteArray();
	}

	File mod(File root, String folder, String json, byte[] png) throws Exception {
		File d = new File(root, folder); d.mkdirs();
		if (json != null) Files.write(new File(d, "hero.json").toPath(), json.getBytes(StandardCharsets.UTF_8));
		if (png != null) Files.write(new File(d, "sprite.png").toPath(), png);
		return d;
	}

	@Test public void goodModsLoad_brokenOnesAreListedWithReasons_andNeverStopTheOthers() throws Exception {
		File root = tmp.newFolder("mods");
		String good = HeroModTest.hero(null);
		mod(root, "a-good", good, pngHeader(256, 128));
		mod(root, "b-no-json", null, pngHeader(256, 128));
		mod(root, "c-bad-json", good.replace("\"hp\":22", "\"hp\":-1"), pngHeader(256, 128));
		mod(root, "d-wrong-size", good.replace("chris.battle-mage", "x.other"), pngHeader(32, 32));
		mod(root, "e-not-png", good.replace("chris.battle-mage", "x.third"), "hello".getBytes());
		mod(root, "f-no-sprite", good.replace("chris.battle-mage", "x.fourth"), null);
		mod(root, "g-duplicate-id", good, pngHeader(256, 128));
		mod(root, "h-huge", good.replace("chris.battle-mage", "x.huge"), new byte[70 * 1024]);
		new File(root, "stray-file.txt").createNewFile();

		ModLibrary lib = ModLibrary.scan(root);
		assertEquals(1, lib.heroes.size());
		assertEquals("chris.battle-mage", lib.heroes.get(0).mod.id);
		assertNotNull(lib.find("chris.battle-mage"));
		assertNull(lib.find("x.other"));
		assertEquals(7, lib.refused.size());
		String all = "";
		for (ModLibrary.Refused r : lib.refused) all += r.folder + ": " + r.reason + "\n";
		assertTrue(all, all.contains("b-no-json: hero.json is missing"));
		assertTrue(all, all.contains("c-bad-json: hp must be between"));
		assertTrue(all, all.contains("d-wrong-size: sprite.png must be 256x128"));
		assertTrue(all, all.contains("e-not-png: sprite.png is not a PNG"));
		assertTrue(all, all.contains("f-no-sprite: sprite.png is missing"));
		assertTrue(all, all.contains("g-duplicate-id: another installed mod already uses the id"));
		assertTrue(all, all.contains("h-huge: sprite.png is larger than 64 KB"));
	}

	@Test public void noModsFolderIsJustEmpty(){
		ModLibrary lib = ModLibrary.scan(new File(tmp.getRoot(), "missing"));
		assertTrue(lib.heroes.isEmpty() && lib.refused.isEmpty());
		assertTrue(ModLibrary.scan(null).heroes.isEmpty());
	}
}
