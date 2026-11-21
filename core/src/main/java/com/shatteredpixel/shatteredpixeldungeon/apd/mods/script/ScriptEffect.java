package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * A timed effect a script mod declared (APD.effect). Lasts its turns, calls the mod's onStart / onTurn / onEnd,
 * and the other triggers (hits, zaps, damage, kills) fire from the game through {@link #fire}. Stores only mod id,
 * effect id and turns left; if the mod is gone or turned off the effect quietly ends.
 */
public class ScriptEffect extends Buff {

	private String modId = "", effectId = "";
	private int left, total;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public String modId(){ return modId; }
	public String effectId(){ return effectId; }
	public int turnsLeft(){ return left; }

	ScriptDefs.EffectDef def(){ return ScriptModRuntime.effect(modId, effectId); }

	/** Gives (or refreshes to the longer duration) this mod effect on the hero. */
	static ScriptEffect give(Hero hero, ScriptDefs.EffectDef d, int turns){
		for (ScriptEffect e : hero.buffs(ScriptEffect.class)){
			if (e.modId.equals(d.modId) && e.effectId.equals(d.id)){
				e.left = Math.max(e.left, turns);
				e.total = Math.max(e.total, e.left);
				return e;
			}
		}
		ScriptEffect e = new ScriptEffect();
		e.modId = d.modId; e.effectId = d.id; e.left = turns; e.total = turns;
		e.attachTo(hero);
		e.trigger("onStart");
		return e;
	}

	/** Calls one of this effect's triggers with the hero object plus extra plain or API arguments. */
	Object trigger(String name, Object... extra){
		ScriptDefs.EffectDef d = def();
		if (d == null || !d.has(name) || !(target instanceof Hero)) return null;
		Hero hero = (Hero) target;
		ScriptApi.ApiObject h = ScriptModRuntime.build(modId, (cx, scope) -> HeroApi.make(scope, hero, modId));
		if (h == null) return null;
		Object[] args = new Object[1 + extra.length];
		args[0] = h;
		System.arraycopy(extra, 0, args, 1, extra.length);
		return ScriptModRuntime.run(modId, d.triggers.get(name), args);
	}

	/** Fires a trigger on every active mod effect the character has. */
	public static void fire(Char ch, String trigger, Object... extra){
		if (ch == null || !ScriptModRuntime.active()) return;
		for (ScriptEffect e : ch.buffs(ScriptEffect.class)) e.trigger(trigger, extra);
	}

	@Override public boolean act(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.mod(modId);
		if (def() == null || m == null || m.disabled()){ detach(); return true; }
		trigger("onTurn", (double) left);
		left--;
		if (left <= 0){
			trigger("onEnd");
			detach();
		} else {
			spend(TICK);
		}
		return true;
	}

	@Override public int icon(){ return BuffIndicator.UPGRADE; }
	@Override public float iconFadePercent(){ return total <= 0 ? 0 : Math.max(0, (total - left) / (float) total); }
	@Override public String iconTextDisplay(){ return Integer.toString(left); }

	@Override public String name(){
		ScriptDefs.EffectDef d = def();
		return d != null ? d.name : "mod effect";
	}

	@Override public String desc(){
		ScriptDefs.EffectDef d = def();
		String base = d != null ? d.description : "";
		return (base.isEmpty() ? "" : base + "\n\n") + "Turns left: " + left + "\n\nFrom the mod _" + modId + "_.";
	}

	private static final String MOD = "mod", EFFECT = "effect", LEFT = "left", TOTAL = "total";

	@Override public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(MOD, modId); bundle.put(EFFECT, effectId); bundle.put(LEFT, left); bundle.put(TOTAL, total);
	}

	@Override public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		modId = bundle.getString(MOD); effectId = bundle.getString(EFFECT);
		left = bundle.getInt(LEFT); total = bundle.getInt(TOTAL);
	}
}
