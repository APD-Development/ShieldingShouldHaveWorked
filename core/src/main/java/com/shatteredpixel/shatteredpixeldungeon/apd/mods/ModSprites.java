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
				byte[] bytes = Files.readAllBytes(i.sprite.toPath());
				if (bytes.length > ModLibrary.MAX_SPRITE_BYTES) return null;
				Pixmap p = new Pixmap(bytes, 0, bytes.length);
				if (p.getWidth() != ModLibrary.SPRITE_W || p.getHeight() != ModLibrary.SPRITE_H){ p.dispose(); return null; }
				tx = new SmartTexture(p);
				cache.put(k, tx);
			}
			RectF patch = HeroSprite.tiers().get(armorTier);
			Image avatar = new Image(tx);
			RectF frame = avatar.texture.uvRect(1, 0, 12, 15);
			frame.shift(patch.left, patch.top);
			avatar.frame(frame);
			return avatar;
		} catch (Throwable t){
			Game.reportException(t);
			return null;
		}
	}
}
