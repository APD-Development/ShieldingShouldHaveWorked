package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import static org.junit.Assert.*;

public class ModInstallerTest {
	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	static String read(File f) throws Exception { return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8); }

	@Test public void goodDownloadInstallsUnderItsIdAndTheLibraryLoadsIt() throws Exception {
		File mods = new File(tmp.getRoot(), "mods");
		String json = HeroModTest.hero(null);
		String id = ModInstaller.installDownload(mods, json, Base64.getEncoder().encodeToString(ModLibraryTest.pngHeader(256, 128)));
		assertEquals("chris.battle-mage", id);
		assertEquals(json, read(new File(mods, id + "/hero.json")));
		ModLibrary lib = ModLibrary.scan(mods);
		assertEquals(1, lib.heroes.size());
		assertEquals(0, lib.refused.size());
		String[] left = mods.list();
		assertArrayEquals(new String[]{id}, left);
	}

	@Test public void refusedDownloadsNeverTouchTheInstalledMod() throws Exception {
		File mods = new File(tmp.getRoot(), "mods");
		String json = HeroModTest.hero(null);
		ModInstaller.install(mods, json, ModLibraryTest.pngHeader(256, 128));
		String[] bad = {
				json.replace("\"hp\":22", "\"hp\":-1"),
				"not json",
				null };
		for (String b : bad){
			try { ModInstaller.install(mods, b, ModLibraryTest.pngHeader(256, 128)); fail("installed " + b); }
			catch (ModInstaller.Refused expected){}
		}
		try { ModInstaller.install(mods, json, ModLibraryTest.pngHeader(32, 32)); fail(); }
		catch (ModInstaller.Refused e){ assertTrue(e.getMessage(), e.getMessage().contains("256x128")); }
		try { ModInstaller.install(mods, json, new byte[70 * 1024]); fail(); }
		catch (ModInstaller.Refused e){ assertTrue(e.getMessage().contains("64 KB")); }
		try { ModInstaller.installDownload(mods, json, "@@not base64@@"); fail(); }
		catch (ModInstaller.Refused e){ assertTrue(e.getMessage().contains("damaged")); }
		try { ModInstaller.installDownload(mods, json, null); fail(); }
		catch (ModInstaller.Refused expected){}

		assertEquals(json, read(new File(mods, "chris.battle-mage/hero.json")));
		assertEquals(1, ModLibrary.scan(mods).heroes.size());
		assertArrayEquals(new String[]{"chris.battle-mage"}, mods.list());
	}

	@Test public void reinstallReplaces_sameIdInAnotherFolderIsRefused_uninstallRemoves() throws Exception {
		File mods = new File(tmp.getRoot(), "mods");
		String v1 = HeroModTest.hero(null), v2 = v1.replace("\"hp\":22", "\"hp\":23");
		ModInstaller.install(mods, v1, ModLibraryTest.pngHeader(256, 128));
		ModInstaller.install(mods, v2, ModLibraryTest.pngHeader(256, 128));
		assertEquals(v2, read(new File(mods, "chris.battle-mage/hero.json")));
		assertEquals(1, mods.list().length);

		// A hand-copied folder with a different name holding another mod id is untouched; its id can't be doubled.
		String other = v1.replace("chris.battle-mage", "x.other");
		File hand = new File(mods, "my-copy"); hand.mkdirs();
		Files.write(new File(hand, "hero.json").toPath(), other.getBytes(StandardCharsets.UTF_8));
		Files.write(new File(hand, "sprite.png").toPath(), ModLibraryTest.pngHeader(256, 128));
		try { ModInstaller.install(mods, other, ModLibraryTest.pngHeader(256, 128)); fail(); }
		catch (ModInstaller.Refused e){ assertTrue(e.getMessage(), e.getMessage().contains("my-copy")); }
		assertFalse(new File(mods, "x.other").exists());

		assertTrue(ModInstaller.uninstall(mods, "chris.battle-mage"));
		assertFalse(new File(mods, "chris.battle-mage").exists());
		assertFalse(ModInstaller.uninstall(mods, "chris.battle-mage"));
		assertFalse(ModInstaller.uninstall(mods, "../mods"));
		assertTrue(new File(hand, "hero.json").isFile());
	}

	@Test public void leftoverStagingFoldersAreIgnoredByTheScanner() throws Exception {
		File mods = tmp.newFolder("mods");
		File stage = new File(mods, ".install-crashed"); stage.mkdirs();
		Files.write(new File(stage, "hero.json").toPath(), HeroModTest.hero(null).getBytes(StandardCharsets.UTF_8));
		ModLibrary lib = ModLibrary.scan(mods);
		assertEquals(0, lib.heroes.size());
		assertEquals(0, lib.refused.size());
	}

	@Test public void linkedTargetIsNeverWrittenThrough() throws Exception {
		File mods = tmp.newFolder("mods"), outside = tmp.newFolder("outside");
		try { Files.createSymbolicLink(new File(mods, "chris.battle-mage").toPath(), outside.toPath()); }
		catch (UnsupportedOperationException | java.io.IOException e){ return; }
		try { ModInstaller.install(mods, HeroModTest.hero(null), ModLibraryTest.pngHeader(256, 128)); fail(); }
		catch (ModInstaller.Refused expected){}
		assertEquals(0, outside.list().length);
		assertFalse(ModInstaller.uninstall(mods, "chris.battle-mage"));
	}
}
