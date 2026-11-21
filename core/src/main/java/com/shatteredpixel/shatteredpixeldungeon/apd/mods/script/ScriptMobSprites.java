package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;

/** Existing monster sprites a mod monster may use, by class name (e.g. "RatSprite"). Mod-supplied art comes later. */
public final class ScriptMobSprites {
	private ScriptMobSprites(){}
	private static final String PKG = "com.shatteredpixel.shatteredpixeldungeon.sprites.";

	public static Class<? extends CharSprite> spriteClass(String name){
		if (name == null || !name.matches("[A-Z][A-Za-z0-9]*Sprite")) return null;
		try {
			Class<?> c = Class.forName(PKG + name);
			if (!MobSprite.class.isAssignableFrom(c) || java.lang.reflect.Modifier.isAbstract(c.getModifiers())) return null;
			c.getConstructor();
			@SuppressWarnings("unchecked") Class<? extends CharSprite> sc = (Class<? extends CharSprite>) c;
			return sc;
		} catch (Throwable t){ return null; }
	}

	public static boolean allowed(String name){ return spriteClass(name) != null; }
}
