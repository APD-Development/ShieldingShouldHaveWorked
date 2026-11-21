package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import org.mozilla.javascript.Scriptable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The hero as a script sees it. Every action is bounded so a mod can't break a run: amounts and turns are clamped,
 * only listed existing buffs can be given, and nothing here can reach other game objects.
 */
final class HeroApi {
	private HeroApi(){}

	static final int MAX_AMOUNT = 999, MAX_BUFF_TURNS = 200, MAX_MESSAGE = 200;

	/** Existing timed buffs a mod may give, by the name scripts use. */
	static final Map<String, Class<? extends FlavourBuff>> BUFFS = new LinkedHashMap<>();
	static {
		BUFFS.put("haste", Haste.class);
		BUFFS.put("bless", Bless.class);
		BUFFS.put("invisibility", Invisibility.class);
		BUFFS.put("mindvision", MindVision.class);
		BUFFS.put("levitation", Levitation.class);
		BUFFS.put("light", Light.class);
		BUFFS.put("recharging", Recharging.class);
		BUFFS.put("stamina", Stamina.class);
		BUFFS.put("adrenaline", Adrenaline.class);
	}

	/** Builds the hero object for one call. The source names the mod for damage records. */
	static ScriptApi.ApiObject make(Scriptable scope, Hero hero, String modId){
		ScriptApi.ApiObject h = new ScriptApi.ApiObject(scope, "Hero");
		h.value("hp", hero.HP).value("maxHp", hero.HT).value("level", hero.lvl).value("depth",
				com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth);
		h.fn("heal", a -> {
			int n = ScriptApi.intIn(a, 0, 0, 0, MAX_AMOUNT);
			int healed = hero.isAlive() ? Math.min(n, hero.HT - hero.HP) : 0; // never revives a dead hero
			if (healed > 0){
				hero.HP += healed;
				if (hero.sprite != null) hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healed), FloatingText.HEALING);
			}
			return (double) healed;
		});
		h.fn("damage", a -> {
			int n = ScriptApi.intIn(a, 0, 0, 0, MAX_AMOUNT);
			if (n > 0 && hero.isAlive()) hero.damage(n, new ScriptSource(modId));
			return null;
		});
		h.fn("shield", a -> {
			int n = ScriptApi.intIn(a, 0, 0, 0, MAX_AMOUNT);
			if (n > 0 && hero.isAlive()){
				Buff.affect(hero, Barrier.class).incShield(n);
				if (hero.sprite != null) hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(n), FloatingText.SHIELDING);
			}
			return null;
		});
		h.fn("addBuff", a -> {
			String name = ScriptApi.str(a, 0, "", 30).toLowerCase(Locale.ROOT);
			Class<? extends FlavourBuff> c = BUFFS.get(name);
			if (c == null) throw new ScriptDefs.BadDef("asked for buff \"" + name + "\"; allowed: " + String.join(", ", BUFFS.keySet()));
			int turns = ScriptApi.intIn(a, 1, 10, 1, MAX_BUFF_TURNS);
			if (hero.isAlive()) Buff.prolong(hero, c, turns);
			return null;
		});
		h.fn("addEffect", a -> {
			String id = ScriptApi.str(a, 0, "", 32);
			ScriptDefs.EffectDef d = ScriptModRuntime.effect(modId, id);
			if (d == null) throw new ScriptDefs.BadDef("asked for effect \"" + id + "\", which it never declared");
			int turns = ScriptApi.intIn(a, 1, d.turns, 1, ScriptDefs.MAX_TURNS);
			if (hero.isAlive()) ScriptEffect.give(hero, d, turns);
			return null;
		});
		h.fn("message", a -> {
			GLog.i(ScriptApi.str(a, 0, "", MAX_MESSAGE));
			return null;
		});
		return h;
	}

	/** Damage source for mod damage (records and death messages name the mod). */
	public static final class ScriptSource {
		public final String modId;
		ScriptSource(String modId){ this.modId = modId; }
		@Override public String toString(){ return "mod " + modId; }
	}
}
