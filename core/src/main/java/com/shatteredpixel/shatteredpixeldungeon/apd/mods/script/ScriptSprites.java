package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Existing item sprites a mod item may use, by name (e.g. "POTION_AMBER"). Mod-supplied art comes later. */
public final class ScriptSprites {
	private ScriptSprites(){}

	private static final Map<String, Integer> SPRITES = new HashMap<>();
	static {
		for (Field f : ItemSpriteSheet.class.getFields()){
			int m = f.getModifiers();
			if (!Modifier.isStatic(m) || f.getType() != int.class) continue;
			String n = f.getName();
			if (!(n.startsWith("POTION_") || n.startsWith("SCROLL_") || n.startsWith("STONE_")) || n.endsWith("_HOLDER")) continue;
			try { SPRITES.put(n, f.getInt(null)); } catch (IllegalAccessException ignored){}
		}
	}

	public static boolean allowed(ScriptDefs.Kind kind, String name){
		return name.startsWith(kind.name() + "_") && SPRITES.containsKey(name);
	}

	public static int image(ScriptDefs.Kind kind, String name){
		if (name != null){
			Integer i = SPRITES.get(name.toUpperCase(Locale.ROOT));
			if (i != null) return i;
		}
		switch (kind){
			case SCROLL: return ItemSpriteSheet.SCROLL_HOLDER;
			case STONE: return ItemSpriteSheet.STONE_HOLDER;
			default: return ItemSpriteSheet.POTION_HOLDER;
		}
	}
}
