package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class ModSpritesTest {

	@Test
	public void reinstalledSpriteGetsANewCacheKeyAndMissingSpriteHasNoPreview() throws Exception {
		File dir = Files.createTempDirectory("modsprite").toFile();
		File png = new File(dir, "sprite.png");
		Files.write(png.toPath(), new byte[]{1, 2, 3});
		png.setLastModified(1_000_000L);
		String first = ModSprites.key(png);
		assertEquals(first, ModSprites.key(png));
		Files.write(png.toPath(), new byte[]{1, 2, 3, 4});
		png.setLastModified(2_000_000L);
		assertNotEquals(first, ModSprites.key(png));
		assertNull(ModSprites.key(null));
		assertNull(ModSprites.avatar(null, 0));
	}

	@Test
	public void dungeonSpriteIsTheInstalledModsOwnSheet_missingModFallsBack() throws Exception {
		File root = Files.createTempDirectory("modsheet").toFile();
		String json = HeroModTest.hero(null);
		File d = new File(root, "folder-x"); d.mkdirs();
		Files.write(new File(d, "hero.json").toPath(), json.getBytes("UTF-8"));
		Files.write(new File(d, "sprite.png").toPath(), ModLibraryTest.pngHeader(256, 128));
		HeroMod m = HeroMod.parse(json);
		assertEquals(new File(d, "sprite.png").getCanonicalFile(), ModSprites.spriteFile(root, m).getCanonicalFile());
		assertNull(ModSprites.spriteFile(root, null));
		assertNull(ModSprites.spriteFile(null, m));
		new File(d, "sprite.png").delete();
		assertNull("uninstalled/broken mod: base class sheet", ModSprites.spriteFile(root, m));
	}

	@Test
	public void disguiseEndingAndMirrorImagesKeepTheModSprite() throws Exception {
		String hero = new String(Files.readAllBytes(src("sprites/HeroSprite.java").toPath()), "UTF-8");
		String mirror = new String(Files.readAllBytes(src("sprites/MirrorSprite.java").toPath()), "UTF-8");
		assertTrue("disguise back to own class uses the mod sheet", hero.contains("cls == Dungeon.hero.heroClass ? ownSheet() : cls.spritesheet()"));
		assertTrue("ownSheet prefers the mod sheet", hero.contains("ModSprites.dungeonSheet()"));
		assertTrue("mirror images copy the hero's own look", mirror.contains("HeroSprite.ownSheet()"));
	}

	private static File src(String rel){
		File f = new File(System.getProperty("apd.game.src"), rel);
		if (!f.isFile()) f = new File("src/main/java/com/shatteredpixel/shatteredpixeldungeon/" + rel);
		return f;
	}
}
