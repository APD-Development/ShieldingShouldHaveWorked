package com.shatteredpixel.shatteredpixeldungeon.apd.mods;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Installed hero mods: <mods>/<folder>/hero.json + sprite.png (Chris, Nov 19).
 * Reads files only; a broken mod is listed with the reason it was refused and never stops the others or the game.
 */
public final class ModLibrary {

	public static final int MAX_JSON_BYTES = 16 * 1024;
	public static final int MAX_SPRITE_BYTES = 64 * 1024;
	/** Same sheet layout as the built-in hero sprites (sprites/warrior.png etc.). */
	public static final int SPRITE_W = 256, SPRITE_H = 128;

	public static final class Installed {
		public final HeroMod mod;
		public final File sprite;
		/** The exact hero.json text, stored in the save when this hero is played. */
		public final String json;
		Installed(HeroMod mod, File sprite, String json){ this.mod = mod; this.sprite = sprite; this.json = json; }
	}

	public static final class Refused {
		public final String folder, reason;
		Refused(String folder, String reason){ this.folder = folder; this.reason = reason; }
	}

	public final List<Installed> heroes;
	public final List<Refused> refused;

	private ModLibrary(List<Installed> heroes, List<Refused> refused){
		this.heroes = Collections.unmodifiableList(heroes);
		this.refused = Collections.unmodifiableList(refused);
	}

	public static ModLibrary scan(File modsDir){
		List<Installed> ok = new ArrayList<>();
		List<Refused> bad = new ArrayList<>();
		// Dot folders are the installer's staging/leftovers, never mods.
		File[] dirs = modsDir == null ? null : modsDir.listFiles(f -> f.isDirectory() && !f.getName().startsWith("."));
		if (dirs == null) return new ModLibrary(ok, bad);
		Arrays.sort(dirs, (a, b) -> a.getName().compareTo(b.getName()));
		List<String> ids = new ArrayList<>();
		for (File d : dirs){
			try {
				File json = new File(d, "hero.json"), png = new File(d, "sprite.png");
				if (!json.isFile()) throw new HeroMod.Invalid("hero.json is missing");
				if (json.length() > MAX_JSON_BYTES) throw new HeroMod.Invalid("hero.json is larger than 16 KB");
				String text = new String(Files.readAllBytes(json.toPath()), StandardCharsets.UTF_8);
				HeroMod m = HeroMod.parse(text);
				checkSprite(png);
				if (ids.contains(m.id)) throw new HeroMod.Invalid("another installed mod already uses the id " + m.id);
				ids.add(m.id);
				ok.add(new Installed(m, png, text));
			} catch (HeroMod.Invalid e){
				bad.add(new Refused(d.getName(), e.getMessage()));
			} catch (IOException | RuntimeException e){
				bad.add(new Refused(d.getName(), "could not be read"));
			}
		}
		return new ModLibrary(ok, bad);
	}

	public Installed find(String id){
		for (Installed i : heroes) if (i.mod.id.equals(id)) return i;
		return null;
	}

	/** PNG of exactly the hero sheet size, read from the header only (no image decoding of untrusted files here). */
	static void checkSprite(File png) throws HeroMod.Invalid, IOException {
		if (!png.isFile()) throw new HeroMod.Invalid("sprite.png is missing");
		if (png.length() > MAX_SPRITE_BYTES) throw new HeroMod.Invalid("sprite.png is larger than 64 KB");
		try (InputStream in = new FileInputStream(png); DataInputStream d = new DataInputStream(in)){
			byte[] sig = new byte[8];
			d.readFully(sig);
			if (!Arrays.equals(sig, new byte[]{(byte)0x89, 'P', 'N', 'G', 13, 10, 26, 10})) throw new HeroMod.Invalid("sprite.png is not a PNG");
			d.readInt(); // IHDR length
			byte[] type = new byte[4]; d.readFully(type);
			if (!new String(type, StandardCharsets.US_ASCII).equals("IHDR")) throw new HeroMod.Invalid("sprite.png is not a PNG");
			int w = d.readInt(), h = d.readInt();
			if (w != SPRITE_W || h != SPRITE_H)
				throw new HeroMod.Invalid("sprite.png must be " + SPRITE_W + "x" + SPRITE_H + " (same layout as the built-in hero sprites), not " + w + "x" + h);
		} catch (java.io.EOFException e){
			throw new HeroMod.Invalid("sprite.png is not a PNG");
		}
	}
}
