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
				// Never read through links: a linked folder or file could point anywhere on the phone (ModInstaller refuses them too).
				if (Files.isSymbolicLink(d.toPath())) throw new HeroMod.Invalid("the mod folder is a link");
				if (Files.isSymbolicLink(json.toPath())) throw new HeroMod.Invalid("hero.json is a link");
				if (Files.isSymbolicLink(png.toPath())) throw new HeroMod.Invalid("sprite.png is a link");
				if (!json.isFile()) throw new HeroMod.Invalid("hero.json is missing");
				if (json.length() > MAX_JSON_BYTES) throw new HeroMod.Invalid("hero.json is larger than 16 KB");
				// bounded, no-follow read: the file can change after the checks above (readAllBytes loaded it whole)
				byte[] raw = readBounded(json, MAX_JSON_BYTES);
				if (raw == null) throw new HeroMod.Invalid("hero.json changed while it was read or is larger than 16 KB");
				String text = new String(raw, StandardCharsets.UTF_8);
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

	/**
	 * A mod file's bytes, or null when it is not a plain file, is a link, or is (or grows) over max bytes.
	 * Never loads more than max + 1 bytes into memory, never follows a link.
	 */
	public static byte[] readBounded(File f, int max){
		try {
			if (f == null || !f.isFile() || Files.isSymbolicLink(f.toPath()) || f.length() > max) return null;
			try (InputStream in = Files.newInputStream(f.toPath(), java.nio.file.LinkOption.NOFOLLOW_LINKS)){
				byte[] buf = new byte[max + 1];
				int n = 0, r;
				while (n < buf.length && (r = in.read(buf, n, buf.length - n)) > 0) n += r;
				if (n > max) return null;
				return Arrays.copyOf(buf, n);
			}
		} catch (Throwable t){ return null; }
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
