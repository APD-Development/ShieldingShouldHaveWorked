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
				File target = new File(root, s);
				if (!json.exists() || !js.exists() || target.exists()) continue;
				if (!target.mkdirs()) continue;
				java.nio.file.Files.write(new File(target, "mod.json").toPath(), json.readBytes());
				java.nio.file.Files.write(new File(target, "main.js").toPath(), js.readBytes());
			} catch (Exception e){
				com.watabou.noosa.Game.reportException(e);
			}
		}
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
	public static void cancelStarting(){ starting = Collections.emptyList(); }

	public static boolean isScriptRun(){ return !active.isEmpty(); }
	public static List<String> activeIds(){ return Collections.unmodifiableList(active); }
	public static List<String> missingIds(){ return Collections.unmodifiableList(missing); }

	/** New run: take the picked mods (a normal new run clears any old script-mod state). */
	public static void beginRun(){
		List<String> ids = starting;
		starting = Collections.emptyList();
		activate(ids);
	}

	/** After the class kit (and any hero mod's items): every loaded mod's starting items. */
	public static void applyStart(Hero hero){
		if (isScriptRun()) ScriptModRuntime.giveStartingItems(hero);
	}

	public static void store(Bundle bundle){
		if (isScriptRun()) bundle.put(NODE, active.toArray(new String[0]));
	}

	/** Loading a hero: re-reads the saved mods' scripts from the installed files. */
	public static void restore(Bundle bundle){
		String[] ids = bundle == null || !bundle.contains(NODE) ? null : bundle.getStringArray(NODE);
		List<String> list = new ArrayList<>();
		if (ids != null) for (String s : ids) if (s != null && !s.isEmpty() && !list.contains(s)) list.add(s);
		activate(list);
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
	public static void reset(){ synchronized (ScriptModRun.class){ picked.clear(); } starting = Collections.emptyList(); active = Collections.emptyList(); missing = Collections.emptyList(); ScriptModRuntime.clear(); }
}
