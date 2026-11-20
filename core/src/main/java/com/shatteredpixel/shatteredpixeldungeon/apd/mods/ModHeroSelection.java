package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;

import java.io.File;

/**
 * Hero select side of hero mods (plan step c, Chris Nov 19): installed mod heroes are offered after the normal
 * classes. Picking one selects its base class and queues the mod for the next new run; picking a normal hero
 * afterwards drops it again, so a mod can never leak into a normal run.
 */
public final class ModHeroSelection {

	private ModHeroSelection(){}

	/** Folder name under the game's files dir: <files>/mods/<id>/hero.json + sprite.png. */
	public static final String FOLDER = "mods";

	private static volatile ModLibrary.Installed picked;

	public static ModLibrary scan(File modsDir){ return ModLibrary.scan(modsDir); }

	/** Why this mod can't be picked right now, or null if it can. */
	public static String blockedReason(ModLibrary.Installed i, boolean baseClassUnlocked){
		if (i == null) return "this mod is no longer installed";
		if (!baseClassUnlocked) return "its base class (" + i.mod.baseClass.title() + ") is still locked";
		return null;
	}

	/** Queue the mod for the next new run; returns the base class hero select must show. */
	public static HeroClass pick(ModLibrary.Installed i){
		picked = i;
		HeroModRun.choose(i.mod, i.json);
		return i.mod.baseClass;
	}

	/** A normal hero was picked (or hero select reset): no mod for the next run. */
	public static void clear(){
		picked = null;
		HeroModRun.cancelStarting();
	}

	public static ModLibrary.Installed picked(){ return picked; }

	/**
	 * Remove an installed mod from this phone: deletes only the folder the library loaded it from, and only when
	 * that folder sits directly in modsDir. Drops the pick if it was this mod. Saves keep their own copy of the
	 * mod record, so runs already started are unaffected. Returns false if nothing was removed.
	 */
	public static boolean remove(File modsDir, ModLibrary.Installed i){
		if (modsDir == null || i == null || i.sprite == null) return false;
		File folder = i.sprite.getParentFile();
		if (folder == null) return false;
		try {
			File parent = folder.getParentFile();
			if (parent == null || !parent.getCanonicalFile().equals(modsDir.getCanonicalFile())) return false;
		} catch (java.io.IOException e){ return false; }
		if (!ModInstaller.uninstall(modsDir, folder.getName())) return false;
		if (picked == i) clear();
		return true;
	}

	/** The run is starting: the queued mod must still match the selected class, otherwise start a normal hero. */
	public static void confirmForStart(HeroClass selected){
		ModLibrary.Installed i = picked;
		if (i == null || selected != i.mod.baseClass){ clear(); return; }
		HeroModRun.choose(i.mod, i.json); // re-queue in case anything cleared it since the pick
		picked = null;
	}

	/** One line for the mod list: name and author. */
	public static String label(ModLibrary.Installed i){
		return i.mod.name + " (by " + i.mod.author + ")";
	}

	/** Shown before playing: description, author, base class and the plain numbers. */
	public static String details(ModLibrary.Installed i){
		HeroMod m = i.mod;
		return m.description + "\n\nBy " + m.author + ". Runs on the " + m.baseClass.title() + " class. Starts with "
				+ m.hp + " HP and " + m.str + " strength.\n\nMod runs stay on this phone: they are marked Mod in"
				+ " local rankings, never sent to Community and never unlock normal badges.";
	}
}
