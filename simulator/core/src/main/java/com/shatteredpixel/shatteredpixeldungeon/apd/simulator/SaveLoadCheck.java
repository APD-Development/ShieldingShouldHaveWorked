package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Save/load stress: every N turns the game is saved and reloaded exactly the way it is when a player quits and resumes.
//The hero, the pack and the floor are compared before and after; any difference is a bug.
final class SaveLoadCheck {

	private String before;
	private int beforeTurn;
	boolean pending;

	static List<String> snapshot(){
		List<String> out = new ArrayList<>();
		Hero h = Dungeon.hero;
		out.add("floor " + Dungeon.depth + " branch " + Dungeon.branch);
		out.add("hero at " + h.pos + " HP " + h.HP + "/" + h.HT + " level " + h.lvl + " exp " + h.exp + " STR " + h.STR + " class " + h.heroClass + " subclass " + h.subClass);
		out.add("gold " + Dungeon.gold + " energy " + Dungeon.energy);
		List<String> items = new ArrayList<>();
		for (Item i : h.belongings) items.add(i.getClass().getSimpleName() + " x" + i.quantity() + " lvl " + i.level() + (i.isEquipped(h) ? " equipped" : "") + (i.cursed ? " cursed" : ""));
		Collections.sort(items);
		out.add("pack: " + items);
		List<String> buffs = new ArrayList<>();
		for (Buff b : h.buffs()) buffs.add(b.getClass().getSimpleName());
		Collections.sort(buffs);
		out.add("hero effects: " + buffs);
		List<String> mobs = new ArrayList<>();
		for (Mob m : Dungeon.level.mobs) mobs.add(m.getClass().getSimpleName() + "@" + m.pos + " HP " + m.HP + "/" + m.HT);
		Collections.sort(mobs);
		out.add("monsters: " + mobs);
		List<String> heaps = new ArrayList<>();
		for (Heap hp : Dungeon.level.heaps.valueList()) heaps.add(hp.pos + ":" + hp.type + ":" + hp.items.size());
		Collections.sort(heaps);
		out.add("item piles: " + heaps);
		return out;
	}

	//saves and starts the reload; the comparison happens on the hero's next turn after loading
	void start(SimRuntime sim){
		try {
			Dungeon.saveAll();
			before = String.join("\n", snapshot());
			beforeTurn = (int) com.shatteredpixel.shatteredpixeldungeon.Statistics.apdTurn();
			pending = true;
			GamesInProgress.curSlot = 1;
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
		} catch (Throwable t){
			pending = false;
			sim.report(BugReport.Kind.SAVELOAD, "savefail:" + SimRuntime.signature(t), "Saving the game failed: " + t, SimRuntime.stack(t));
		}
	}

	void compare(SimRuntime sim){
		pending = false;
		String after = String.join("\n", snapshot());
		if (after.equals(before)) return;
		String[] a = before.split("\n"), b = after.split("\n");
		StringBuilder diff = new StringBuilder();
		String first = "";
		for (int i = 0; i < Math.max(a.length, b.length); i++){
			String x = i < a.length ? a[i] : "", y = i < b.length ? b[i] : "";
			if (!x.equals(y)){
				diff.append("before: ").append(x).append("\nafter:  ").append(y).append("\n\n");
				if (first.isEmpty()) first = y.replaceAll(" .*", "");
			}
		}
		sim.report(BugReport.Kind.SAVELOAD, "saveload:" + first, "Saving and reloading at turn " + beforeTurn + " changed the game (" + first + ").", diff.toString());
	}
}
