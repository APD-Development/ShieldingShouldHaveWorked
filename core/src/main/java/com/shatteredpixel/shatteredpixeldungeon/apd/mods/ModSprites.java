package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.gltextures.SmartTexture;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Preview of an installed mod hero's own sprite.png (same sheet layout as the built-in hero sprites):
 * the first idle frame of the given armor tier, like HeroSprite.avatar. Textures are cached per file
 * and modification time, so a reinstalled mod shows its new sprite. Any read problem returns null and
 * the caller falls back to the base class avatar; it never crashes hero select.
 */
public final class ModSprites {

	private ModSprites(){}

	private static final Map<String, SmartTexture> cache = new HashMap<>();

	/** Cache key: same file and same modification time = same texture. */
	static String key(File png){
		return png == null ? null : png.getAbsolutePath() + "@" + png.lastModified() + ":" + png.length();
	}

	public static synchronized Image avatar(ModLibrary.Installed i, int armorTier){
		if (i == null || i.sprite == null || !i.sprite.isFile()) return null;
		try {
			String k = key(i.sprite);
			SmartTexture tx = cache.get(k);
			if (tx == null){
				tx = texture(Files.readAllBytes(i.sprite.toPath()));
				if (tx == null) return null;
				cache.put(k, tx);
			}
			return frame(tx, armorTier);
		} catch (Throwable t){
			Game.reportException(t);
			return null;
		}
	}

	/** Same preview from PNG bytes not on disk (a workshop download); cacheKey names those exact bytes. */
	public static synchronized Image avatar(byte[] png, String cacheKey, int armorTier){
		if (png == null || cacheKey == null) return null;
		try {
			SmartTexture tx = cache.get(cacheKey);
			if (tx == null){
				tx = texture(png);
				if (tx == null) return null;
				cache.put(cacheKey, tx);
			}
			return frame(tx, armorTier);
		} catch (Throwable t){
			Game.reportException(t);
			return null;
		}
	}

	/**
	 * The installed sprite.png for this run's mod (matched by mod id in modsDir), or null when the mod is gone,
	 * damaged or there is no mod. Chris, Nov 20: the mod sprite IS shown in the dungeon.
	 */
	static File spriteFile(File modsDir, HeroMod mod){
		if (modsDir == null || mod == null) return null;
		try {
			ModLibrary.Installed i = ModLibrary.scan(modsDir).find(mod.id);
			return i == null || i.sprite == null || !i.sprite.isFile() ? null : i.sprite;
		} catch (Throwable t){ return null; }
	}

	/** Full sheet for the dungeon hero sprite of the active mod run; null = use the base class sheet. */
	public static synchronized SmartTexture dungeonSheet(){
		HeroMod m = HeroModRun.active;
		if (m == null) return null;
		try {
			File png = spriteFile(com.watabou.utils.FileUtils.getFileHandle(ModHeroSelection.FOLDER).file(), m);
			if (png == null) return null;
			String k = key(png);
			SmartTexture tx = cache.get(k);
			if (tx == null){
				tx = texture(Files.readAllBytes(png.toPath()));
				if (tx == null) return null;
				cache.put(k, tx);
			}
			return tx;
		} catch (Throwable t){
			Game.reportException(t);
			return null;
		}
	}

	private static SmartTexture texture(byte[] bytes){
		if (bytes.length > ModLibrary.MAX_SPRITE_BYTES) return null;
		Pixmap p = new Pixmap(bytes, 0, bytes.length);
		if (p.getWidth() != ModLibrary.SPRITE_W || p.getHeight() != ModLibrary.SPRITE_H){ p.dispose(); return null; }
		return new SmartTexture(p);
	}

	private static Image frame(SmartTexture tx, int armorTier){
		RectF patch = HeroSprite.tiers().get(armorTier);
		Image avatar = new Image(tx);
		RectF frame = avatar.texture.uvRect(1, 0, 12, 15);
		frame.shift(patch.left, patch.top);
		avatar.frame(frame);
		return avatar;
	}
}
