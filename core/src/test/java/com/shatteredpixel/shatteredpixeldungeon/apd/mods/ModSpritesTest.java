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
}
