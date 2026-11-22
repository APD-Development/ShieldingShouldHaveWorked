package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * A monster a script mod declared (APD.monster). Stats come from the definition; the mod's onSpawn / onTurn /
 * onAttack / onHurt / onDeath run in its sandbox. Saves keep only mod id + monster id: if the mod is missing or
 * turned off, the monster quietly leaves the floor on its first turn instead of breaking the save.
 */
public class ScriptMob extends Mob {

	private String modId = "", monsterId = "";
	private boolean spawned;

	{
		spriteClass = RatSprite.class; // replaced by the definition's sprite
	}

	public ScriptMob(){ }

	public ScriptMob(ScriptDefs.MonsterDef d){
		modId = d.modId; monsterId = d.id;
		apply(d, true);
	}

	public String modId(){ return modId; }
	public String monsterId(){ return monsterId; }

	ScriptDefs.MonsterDef def(){ return ScriptModRuntime.monster(modId, monsterId); }

	private boolean usable(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.mod(modId);
		return m != null && !m.disabled() && def() != null;
	}

	private void apply(ScriptDefs.MonsterDef d, boolean fresh){
		spriteClass = ScriptMobSprites.spriteClass(d.sprite);
		if (spriteClass == null) spriteClass = RatSprite.class;
		if (fresh){ HP = HT = d.hp; }
		defenseSkill = d.evasion;
		EXP = d.xp;
		baseSpeed = (float) d.speed;
		if (d.loot != null){
			ScriptDefs.ItemDef item = ScriptModRuntime.item(modId, d.loot);
			loot = null;
			lootChance = item != null ? (float) d.lootChance : 0;
		} else lootChance = 0;
	}

	@Override public String name(){ ScriptDefs.MonsterDef d = def(); return d != null ? d.name : "mod monster"; }
	@Override public String description(){
		ScriptDefs.MonsterDef d = def();
		return (d != null && !d.description.isEmpty() ? d.description + "\n\n" : "") + "From the mod _" + modId + "_.";
	}

	@Override public int damageRoll(){ ScriptDefs.MonsterDef d = def(); return d == null ? 0 : Random.NormalIntRange(d.attackMin, d.attackMax); }
	@Override public int attackSkill(Char target){ ScriptDefs.MonsterDef d = def(); return d == null ? 0 : d.accuracy; }
	@Override public int drRoll(){ ScriptDefs.MonsterDef d = def(); return super.drRoll() + (d == null ? 0 : Random.NormalIntRange(d.armorMin, d.armor)); }

	@Override public Item createLoot(){
		ScriptDefs.MonsterDef d = def();
		if (d == null || d.loot == null) return null;
		ScriptDefs.ItemDef item = ScriptModRuntime.item(modId, d.loot);
		return item == null ? null : new ScriptItem(item);
	}

	Object trigger(String name, Object... extra){
		ScriptDefs.MonsterDef d = def();
		if (d == null || !d.has(name) || !usable()) return null;
		ScriptApi.ApiObject self = ScriptModRuntime.build(modId, (cx, scope) -> MonsterApi.make(scope, this));
		if (self == null) return null;
		Object[] args = new Object[1 + extra.length];
		args[0] = self;
		System.arraycopy(extra, 0, args, 1, extra.length);
		return ScriptModRuntime.run(modId, d.triggers.get(name), args);
	}

	private Object heroArg(Char c){
		if (!(c instanceof Hero)) return null;
		Hero h = (Hero) c;
		return ScriptModRuntime.build(modId, (cx, scope) -> HeroApi.make(scope, h, modId));
	}

	@Override protected boolean act(){
		if (!usable()){
			// its mod is gone or turned off: leave quietly, never break the save
			destroy();
			if (sprite != null) sprite.killAndErase();
			return true;
		}
		if (!turnTriggers()) return true;
		return super.act();
	}

	/** Runs onSpawn (first turn) and onTurn; false when a script killed this monster, so it must not act afterwards. */
	boolean turnTriggers(){
		if (!spawned){ spawned = true; trigger("onSpawn"); }
		if (!isAlive()) return false;
		trigger("onTurn");
		return isAlive();
	}

	@Override public int attackProc(Char enemy, int damage){
		damage = super.attackProc(enemy, damage);
		Object h = heroArg(enemy);
		if (h != null) trigger("onAttack", h, (double) damage); else trigger("onAttack", null, (double) damage);
		return damage;
	}

	@Override public void damage(int dmg, Object src){
		super.damage(dmg, src);
		if (isAlive() && !(src instanceof HeroApi.ScriptSource)) trigger("onHurt", (double) dmg);
	}

	@Override public void die(Object cause){
		trigger("onDeath");
		super.die(cause);
	}

	private static final String MOD = "mod", MONSTER = "monster", SPAWNED = "spawned";

	@Override public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(MOD, modId); bundle.put(MONSTER, monsterId); bundle.put(SPAWNED, spawned);
	}

	@Override public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		modId = bundle.getString(MOD); monsterId = bundle.getString(MONSTER); spawned = bundle.getBoolean(SPAWNED);
		ScriptDefs.MonsterDef d = def();
		if (d != null) apply(d, false);
	}
}
