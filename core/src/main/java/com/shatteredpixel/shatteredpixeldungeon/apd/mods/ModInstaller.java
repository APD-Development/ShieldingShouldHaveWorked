package com.shatteredpixel.shatteredpixeldungeon.apd.mods;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/**
 * Puts a workshop download into the mods folder as <mods>/<mod id>/hero.json + sprite.png (hero mods (e)).
 * The download is untrusted: it is checked with the same rules as {@link ModLibrary#scan} BEFORE anything in the
 * mods folder changes, written to a staging folder first, and only then swapped in. A refused download never
 * touches an installed mod. The folder name always comes from the validated mod id, never from the server.
 */
public final class ModInstaller {

	private ModInstaller(){}

	public static final class Refused extends Exception {
		public Refused(String why){ super(why); }
	}

	/** Install from the download reply's fields: the exact hero.json text and the base64 sprite. Returns the mod id. */
	public static String installDownload(File modsDir, String heroJson, String spriteBase64) throws Refused {
		if (spriteBase64 == null || spriteBase64.isEmpty()) throw new Refused("the download has no sprite");
		byte[] png;
		try { png = Base64.getDecoder().decode(spriteBase64); }
		catch (IllegalArgumentException e){ throw new Refused("the download's sprite is damaged"); }
		return install(modsDir, heroJson, png);
	}

	public static String install(File modsDir, String heroJson, byte[] spritePng) throws Refused {
		if (modsDir == null) throw new Refused("no mods folder");
		if (heroJson == null) throw new Refused("the download has no hero.json");
		if (heroJson.getBytes(StandardCharsets.UTF_8).length > ModLibrary.MAX_JSON_BYTES) throw new Refused("hero.json is larger than 16 KB");
		if (spritePng == null || spritePng.length == 0) throw new Refused("the download has no sprite");
		if (spritePng.length > ModLibrary.MAX_SPRITE_BYTES) throw new Refused("sprite.png is larger than 64 KB");
		HeroMod mod;
		try { mod = HeroMod.parse(heroJson); } catch (HeroMod.Invalid e){ throw new Refused(e.getMessage()); }

		if (Files.isSymbolicLink(modsDir.toPath())) throw new Refused("the mods folder is a link");
		if (!modsDir.isDirectory() && !modsDir.mkdirs()) throw new Refused("could not create the mods folder");
		File target = new File(modsDir, mod.id);
		if (Files.isSymbolicLink(target.toPath()) || (target.exists() && !Files.isDirectory(target.toPath(), LinkOption.NOFOLLOW_LINKS)))
			throw new Refused("something that isn't a mod folder is already named " + mod.id);
		// Another folder already holding this id would make the scanner refuse one of them: say so instead.
		ModLibrary lib = ModLibrary.scan(modsDir);
		for (ModLibrary.Installed i : lib.heroes){
			if (i.mod.id.equals(mod.id) && !i.sprite.getParentFile().getName().equals(mod.id))
				throw new Refused("the mod " + mod.id + " is already installed in the folder " + i.sprite.getParentFile().getName());
			// A hand-placed folder named like this id but holding a DIFFERENT mod would be silently replaced (deleted).
			if (!i.mod.id.equals(mod.id) && i.sprite.getParentFile().getName().equals(mod.id))
				throw new Refused("the folder " + mod.id + " already holds a different mod (" + i.mod.id + ")");
		}

		File stage = new File(modsDir, ".install-" + java.util.UUID.randomUUID());
		File old = null;
		try {
			if (!stage.mkdir()) throw new Refused("could not write to the mods folder");
			Files.write(new File(stage, "hero.json").toPath(), heroJson.getBytes(StandardCharsets.UTF_8));
			File png = new File(stage, "sprite.png");
			Files.write(png.toPath(), spritePng);
			try { ModLibrary.checkSprite(png); } catch (HeroMod.Invalid e){ throw new Refused(e.getMessage()); }

			if (target.exists()){
				old = new File(modsDir, ".old-" + java.util.UUID.randomUUID());
				Files.move(target.toPath(), old.toPath(), StandardCopyOption.ATOMIC_MOVE);
			}
			try {
				Files.move(stage.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e){
				if (old != null) Files.move(old.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
				old = null;
				throw e;
			}
			return mod.id;
		} catch (IOException e){
			throw new Refused("could not write to the mods folder");
		} finally {
			deleteFlat(stage);
			deleteFlat(old);
		}
	}

	/** Remove an installed mod's folder (named by id). Returns false if there was nothing to remove. */
	public static boolean uninstall(File modsDir, String id){
		if (modsDir == null || id == null || !id.matches("[a-z0-9][a-z0-9._-]*") || id.length() > 64) return false;
		return removeFolder(modsDir, id);
	}

	/** Remove a hand-placed mod folder by its folder name ("any folder name", docs/MODDING.md): a single plain name
	 *  directly in modsDir, never a path, never a dot folder (installer staging), never a link. */
	public static boolean uninstallFolder(File modsDir, String folder){
		if (modsDir == null || folder == null || folder.isEmpty() || folder.startsWith(".")
				|| folder.contains("/") || folder.contains("\\") || folder.indexOf('\0') >= 0) return false;
		if (!new File(folder).getName().equals(folder)) return false;
		return removeFolder(modsDir, folder);
	}

	private static boolean removeFolder(File modsDir, String name){
		File target = new File(modsDir, name);
		if (Files.isSymbolicLink(target.toPath()) || !Files.isDirectory(target.toPath(), LinkOption.NOFOLLOW_LINKS)) return false;
		File gone = new File(modsDir, ".old-" + java.util.UUID.randomUUID());
		try { Files.move(target.toPath(), gone.toPath(), StandardCopyOption.ATOMIC_MOVE); }
		catch (IOException e){ return false; }
		deleteFlat(gone);
		return true;
	}

	/** A mod folder only ever holds plain files; never follows links or recurses. */
	private static void deleteFlat(File dir){
		if (dir == null || !Files.isDirectory(dir.toPath(), LinkOption.NOFOLLOW_LINKS)) return;
		File[] files = dir.listFiles();
		if (files != null) for (File f : files){
			try { Files.deleteIfExists(f.toPath()); } catch (IOException ignored){}
		}
		dir.delete();
	}
}
