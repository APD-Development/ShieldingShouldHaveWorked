package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Script mods for one run (work plan step c, core part): the mods picked before a run are loaded at run start, their
 * starting kits given, their ids saved in the run; loading the run re-reads the installed scripts. A saved mod that is
 * no longer installed (or no longer loads) is listed as missing; its items and effects stay inert, never a crash.
 * Any run with script mods is a mod run (rule 5: out of rankings, badges, tournament and reviews).
 * The hero-select picker that calls {@link #choose} is not built yet, so nothing here runs in a normal game.
 */
public final class ScriptModRun {
	private ScriptModRun(){}

	private static final String NODE = "apd_script_mods";

	/** Tests set this; otherwise {@code <files>/scriptmods}, next to the hero-mods folder. */
	public static volatile File dirOverride;

	private static volatile List<String> starting = Collections.emptyList();
	private static volatile List<String> active = Collections.emptyList();
	private static volatile List<String> missing = Collections.emptyList();

	public static File dir(){
		if (dirOverride != null) return dirOverride;
		try { return com.watabou.utils.FileUtils.getFileHandle(ScriptModLibrary.DIR).file(); }
		catch (RuntimeException e){ return null; }
	}

	/**
	 * Hero-select picker: test (dev) builds only, and this whole system lives on feature/script-mods (rule 7);
	 * never in a Play build until Chris has tried it on his phone.
	 */
	public static boolean pickerEnabled(){ return com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures.testBuild(); }

	/** Bundled sample mods (test builds only; the asset exists only there), by folder name. */
	public static final String[] SAMPLES = {"sample.items"};

	/** Test builds: copies each bundled sample into the script mods folder once (never overwrites a folder). */
	public static void installBundledSamples(){
		if (!pickerEnabled()) return;
		File root = dir();
		if (root == null) return;
		for (String s : SAMPLES){
			try {
				com.badlogic.gdx.files.FileHandle json = com.badlogic.gdx.Gdx.files.internal("apd/scriptmods/" + s + "/mod.json");
				com.badlogic.gdx.files.FileHandle js = com.badlogic.gdx.Gdx.files.internal("apd/scriptmods/" + s + "/main.js");
				if (!json.exists() || !js.exists()) continue;
				installSample(root, s, json.readBytes(), js.readBytes());
			} catch (Exception e){
				com.watabou.noosa.Game.reportException(e);
			}
		}
	}

	/**
	 * Writes one sample into {@code root/name} through a hidden staging folder renamed into place, so a write that
	 * fails halfway never leaves a half-installed folder (which would be refused forever and never re-copied).
	 * An existing folder is never overwritten. Returns true when the sample was installed.
	 */
	static boolean installSample(File root, String name, byte[] json, byte[] js) throws java.io.IOException {
		File target = new File(root, name);
		if (target.exists() || java.nio.file.Files.isSymbolicLink(target.toPath())) return false;
		if (!root.isDirectory() && !root.mkdirs()) return false;
		File stage = new File(root, "." + name + ".installing");
		deleteStage(stage);
		if (!stage.mkdir()) return false;
		try {
			java.nio.file.Files.write(new File(stage, "mod.json").toPath(), json);
			java.nio.file.Files.write(new File(stage, "main.js").toPath(), js);
			java.nio.file.Files.move(stage.toPath(), target.toPath());
			return true;
		} finally {
			deleteStage(stage);
		}
	}

	private static void deleteStage(File stage){
		if (java.nio.file.Files.isSymbolicLink(stage.toPath())){ stage.delete(); return; }
		File[] fs = stage.listFiles();
		if (fs != null) for (File f : fs) f.delete();
		stage.delete();
	}

	/** Hero select's toggled mods; kept across hero changes, handed to {@link #choose} only when a normal run starts. */
	private static final List<String> picked = new ArrayList<>();
	public static synchronized boolean isPicked(String id){ return picked.contains(id); }
	public static synchronized void togglePick(String id){ if (!picked.remove(id)) picked.add(id); }
	public static synchronized List<String> picks(){ return new ArrayList<>(picked); }
	/** A normal new run is starting from hero select: queue the toggled mods (dropping any no longer installed). */
	public static synchronized void confirmForStart(ScriptModLibrary lib){
		List<String> ids = new ArrayList<>();
		for (String id : picked) if (lib == null || lib.find(id) != null) ids.add(id);
		picked.retainAll(ids);
		choose(ids);
	}

	/** Mods for the next new run (hero select). */
	public static void choose(List<String> ids){ starting = ids == null ? Collections.emptyList() : new ArrayList<>(ids); }
	public static void cancelStarting(){ starting = Collections.emptyList(); startingHero = null; }

	public static boolean isScriptRun(){ return !active.isEmpty(); }
	public static List<String> activeIds(){ return Collections.unmodifiableList(active); }
	public static List<String> missingIds(){ return Collections.unmodifiableList(missing); }

	private static final String HERO_NODE = "apd_script_hero";
	private static volatile String startingHero; // "modId:heroId"
	private static volatile String activeHero;

	/** Hero select: play this script mod's hero (its mod must also be among the picked mods). Null = a normal hero. */
	public static void chooseHero(String modId, String heroId){
		startingHero = modId == null || heroId == null ? null : modId + ":" + heroId;
	}

	/** This run's script hero, or null. */
	public static ScriptDefs.HeroDef activeHero(){
		String k = activeHero;
		if (k == null) return null;
		int c = k.indexOf(':');
		return c <= 0 ? null : ScriptModRuntime.hero(k.substring(0, c), k.substring(c + 1));
	}

	/** New run: take the picked mods (a normal new run clears any old script-mod state). */
	public static void beginRun(){ beginRun(null); }

	/** New run as {@code cls}: also makes a picked script hero this run's hero when its mod loaded and its base class
	 *  is the class starting (hero select starts the base class); otherwise the run is a normal hero. */
	public static void beginRun(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass cls){
		List<String> ids = starting;
		String hero = startingHero;
		starting = Collections.emptyList();
		startingHero = null;
		activate(ids);
		activeHero = null;
		if (hero == null) return;
		int c = hero.indexOf(':');
		if (c <= 0 || !active.contains(hero.substring(0, c))) return;
		ScriptDefs.HeroDef d = ScriptModRuntime.hero(hero.substring(0, c), hero.substring(c + 1));
		if (d == null || (cls != null && d.mod.baseClass != cls)) return;
		if (com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModRun.activateScriptHero(d.mod, d.json)) activeHero = hero;
	}

	/** After the class kit (and any hero mod's items): every loaded mod's starting items, the script hero's own mod
	 *  items, then the script hero's onStart. */
	public static void applyStart(Hero hero){
		if (!isScriptRun()) return;
		ScriptModRuntime.giveStartingItems(hero);
		ScriptDefs.HeroDef d = activeHero();
		if (d == null) return;
		for (java.util.Map.Entry<String, Integer> e : d.modItems.entrySet()){
			ScriptDefs.ItemDef item = ScriptModRuntime.item(d.modId, e.getKey());
			if (item != null) new ScriptItem(item).quantity(e.getValue()).collect(hero.belongings.backpack);
		}
		if (d.has("onTurn")) com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, HeroTurns.class);
		heroTrigger(hero, "onStart");
	}

	private static boolean heroFiring = false;

	/** Calls one of the script hero's triggers with the bounded hero object plus plain facts. The return value is
	 *  ignored and it is not re-entrant (a trigger set off by another one's hero calls is skipped), so scripts can't
	 *  loop. A dead hero's triggers never run. */
	public static void heroTrigger(Hero hero, String name, Object... extra){
		if (heroFiring || activeHero == null) return;
		ScriptDefs.HeroDef d = activeHero();
		if (d == null || hero == null || !hero.isAlive() || !d.has(name)) return;
		heroFiring = true;
		try {
			ScriptApi.ApiObject h = ScriptModRuntime.build(d.modId, (cx, scope) -> HeroApi.make(scope, hero, d.modId));
			if (h == null) return;
			Object[] args = new Object[1 + extra.length];
			args[0] = h;
			System.arraycopy(extra, 0, args, 1, extra.length);
			ScriptModRuntime.run(d.modId, d.triggers.get(name), args);
		} finally {
			heroFiring = false;
		}
	}

	/** Once per hero turn while a script hero with onTurn is active (a buff, so it ticks with game time and not
	 *  with each time the game waits for the player's input). Saved with the hero; inert without its mod. */
	public static class HeroTurns extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff {
		{ revivePersists = true; }
		@Override public boolean act(){
			if (target instanceof Hero) heroTrigger((Hero) target, "onTurn");
			spend(TICK);
			return true;
		}
	}

	public static void store(Bundle bundle){
		if (isScriptRun()) bundle.put(NODE, active.toArray(new String[0]));
		if (isScriptRun() && activeHero != null) bundle.put(HERO_NODE, activeHero);
	}

	/** Loading a hero: re-reads the saved mods' scripts from the installed files. */
	public static void restore(Bundle bundle){
		String[] ids = bundle == null || !bundle.contains(NODE) ? null : bundle.getStringArray(NODE);
		List<String> list = new ArrayList<>();
		if (ids != null) for (String s : ids) if (s != null && !s.isEmpty() && !list.contains(s)) list.add(s);
		activate(list);
		// the hero itself (stats, talents, kit record) comes back through HeroModRun's own saved record
		activeHero = bundle != null && bundle.contains(HERO_NODE) && !list.isEmpty() ? bundle.getString(HERO_NODE) : null;
	}

	private static void activate(List<String> ids){
		ScriptModRuntime.clear();
		List<String> act = new ArrayList<>(), miss = new ArrayList<>();
		if (!ids.isEmpty()){
			ScriptModLibrary lib = ScriptModLibrary.scan(dir());
			for (String id : ids){
				if (act.contains(id)) continue;
				act.add(id);
				ScriptModLibrary.Installed i = lib.find(id);
				if (i == null){ miss.add(id); continue; }
				if (ScriptModRuntime.load(id, i.script).disabled()) miss.add(id);
			}
		}
		active = act;
		missing = miss;
	}

	/** Tests only. */
	public static void reset(){ synchronized (ScriptModRun.class){ picked.clear(); } starting = Collections.emptyList(); startingHero = null; activeHero = null; active = Collections.emptyList(); missing = Collections.emptyList(); ScriptModRuntime.clear(); }
}
