package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * An item a script mod declared (potion, scroll or stone). It stores only its mod id and item id; everything else
 * comes from the loaded mod. If the mod is missing or turned off, the item stays in the bag but does nothing, so a
 * removed or broken mod never breaks a save.
 */
public class ScriptItem extends Item {

	private String modId = "", itemId = "";

	{
		stackable = true;
		defaultAction = null;
	}

	public ScriptItem(){ }

	public ScriptItem(ScriptDefs.ItemDef d){
		modId = d.modId;
		itemId = d.id;
		refresh();
	}

	public String modId(){ return modId; }
	public String itemId(){ return itemId; }

	ScriptDefs.ItemDef def(){
		return ScriptModRuntime.item(modId, itemId);
	}

	private void refresh(){
		ScriptDefs.ItemDef d = def();
		if (d != null){
			image = ScriptSprites.image(d.kind, d.sprite);
			defaultAction = d.kind.action;
		}
	}

	private boolean usable(){
		ScriptDefs.ItemDef d = def();
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.mod(modId);
		return d != null && m != null && !m.disabled();
	}

	@Override public ArrayList<String> actions(Hero hero){
		ArrayList<String> a = super.actions(hero);
		ScriptDefs.ItemDef d = def();
		if (d != null && usable()) a.add(d.kind.action);
		return a;
	}

	@Override public String actionName(String action, Hero hero){
		ScriptDefs.ItemDef d = def();
		if (d != null && d.kind.action.equals(action)) return d.kind.action;
		return super.actionName(action, hero);
	}

	@Override public void execute(Hero hero, String action){
		super.execute(hero, action);
		ScriptDefs.ItemDef d = def();
		if (d == null || !d.kind.action.equals(action)) return;
		if (!usable()){
			GLog.w(missingText());
			return;
		}
		curUser = hero;
		detach(hero.belongings.backpack);
		hero.spend(1f);
		hero.busy();
		if (hero.sprite != null){
			hero.sprite.operate(hero.pos);
			Sample.INSTANCE.play(d.kind == ScriptDefs.Kind.POTION ? Assets.Sounds.DRINK : Assets.Sounds.READ);
		}
		use(hero, d);
	}

	/** Runs the mod's onUse with the hero object. Separate so tests can call it without sprites or sounds. */
	static void use(Hero hero, ScriptDefs.ItemDef d){
		ScriptApi.ApiObject h = ScriptModRuntime.build(d.modId, (cx, scope) -> HeroApi.make(scope, hero, d.modId));
		if (h != null) ScriptModRuntime.run(d.modId, d.onUse, h);
	}

	private String missingText(){
		ScriptModRuntime.LoadedMod m = ScriptModRuntime.mod(modId);
		if (m != null && m.disabled()) return m.disabledReason();
		return "This item comes from the mod " + modId + ", which isn't active in this run.";
	}

	@Override public String name(){
		ScriptDefs.ItemDef d = def();
		return d != null ? d.name : "mod item (" + itemId + ")";
	}

	@Override public String desc(){
		ScriptDefs.ItemDef d = def();
		String base = d != null ? d.description : "";
		String from = "From the mod _" + modId + "_.";
		if (!usable()) return (base.isEmpty() ? "" : base + "\n\n") + missingText() + "\n\n" + from;
		return (base.isEmpty() ? "" : base + "\n\n") + from;
	}

	@Override public boolean isSimilar(Item item){
		return item instanceof ScriptItem && modId.equals(((ScriptItem) item).modId) && itemId.equals(((ScriptItem) item).itemId);
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 20 * quantity; }

	private static final String MOD = "mod", ITEM = "item";

	@Override public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(MOD, modId);
		bundle.put(ITEM, itemId);
	}

	@Override public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		modId = bundle.getString(MOD);
		itemId = bundle.getString(ITEM);
		refresh();
	}
}
