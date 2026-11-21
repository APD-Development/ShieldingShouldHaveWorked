package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Finds installed script mods (work plan step b): {@code <files>/scriptmods/<folder>/mod.json} plus {@code main.js}.
 * mod.json, format 1: {"format":1, "id":"author.mod-name", "name":"Shown name", "author":"Name", "description":"..."}.
 * Only reads files (bounded, never through links); running a script is ScriptModRuntime's job, at run activation.
 * A broken mod is listed with the reason it was refused and never stops the others or the game.
 */
public final class ScriptModLibrary {

	public static final int FORMAT = 1;
	public static final String DIR = "scriptmods";
	public static final int MAX_JSON_BYTES = 16 * 1024;
	public static final int MAX_SCRIPT_BYTES = 64 * 1024;

	public static final class Installed {
		public final String id, name, author, description, script, folder;
		Installed(String id, String name, String author, String description, String script, String folder){
			this.id = id; this.name = name; this.author = author; this.description = description;
			this.script = script; this.folder = folder;
		}
	}

	public static final class Refused {
		public final String folder, reason;
		Refused(String folder, String reason){ this.folder = folder; this.reason = reason; }
	}

	static final class Bad extends Exception { Bad(String m){ super(m); } }

	public final List<Installed> mods;
	public final List<Refused> refused;

	private ScriptModLibrary(List<Installed> mods, List<Refused> refused){
		this.mods = Collections.unmodifiableList(mods);
		this.refused = Collections.unmodifiableList(refused);
	}

	public static ScriptModLibrary scan(File dir){
		List<Installed> ok = new ArrayList<>();
		List<Refused> bad = new ArrayList<>();
		File[] dirs = dir == null ? null : dir.listFiles(f -> f.isDirectory() && !f.getName().startsWith("."));
		if (dirs == null) return new ScriptModLibrary(ok, bad);
		Arrays.sort(dirs, (a, b) -> a.getName().compareTo(b.getName()));
		List<String> ids = new ArrayList<>();
		for (File d : dirs){
			try {
				if (Files.isSymbolicLink(d.toPath())) throw new Bad("the mod folder is a link");
				File json = new File(d, "mod.json"), js = new File(d, "main.js");
				if (Files.isSymbolicLink(json.toPath())) throw new Bad("mod.json is a link");
				if (Files.isSymbolicLink(js.toPath())) throw new Bad("main.js is a link");
				if (!json.isFile()) throw new Bad("mod.json is missing");
				if (!js.isFile()) throw new Bad("main.js is missing");
				if (json.length() > MAX_JSON_BYTES) throw new Bad("mod.json is larger than 16 KB");
				if (js.length() > MAX_SCRIPT_BYTES) throw new Bad("main.js is larger than 64 KB");
				byte[] rawJson = ModLibrary.readBounded(json, MAX_JSON_BYTES);
				if (rawJson == null) throw new Bad("mod.json changed while it was read or is larger than 16 KB");
				byte[] rawJs = ModLibrary.readBounded(js, MAX_SCRIPT_BYTES);
				if (rawJs == null) throw new Bad("main.js changed while it was read or is larger than 64 KB");
				Installed m = parse(new String(rawJson, StandardCharsets.UTF_8), new String(rawJs, StandardCharsets.UTF_8), d.getName());
				if (ids.contains(m.id)) throw new Bad("another installed mod already uses the id " + m.id);
				ids.add(m.id);
				ok.add(m);
			} catch (Bad e){
				bad.add(new Refused(d.getName(), e.getMessage()));
			} catch (RuntimeException e){
				bad.add(new Refused(d.getName(), "could not be read"));
			}
		}
		return new ScriptModLibrary(ok, bad);
	}

	static Installed parse(String json, String script, String folder) throws Bad {
		JsonValue v;
		try { v = new JsonReader().parse(json); } catch (RuntimeException e){ throw new Bad("mod.json is not valid JSON"); }
		if (v == null || !v.isObject()) throw new Bad("mod.json must be one JSON object");
		if (v.getInt("format", -1) != FORMAT) throw new Bad("unsupported format (this version reads format " + FORMAT + ")");
		String id = text(v, "id", 64);
		if (!id.matches("[a-z0-9][a-z0-9._-]*")) throw new Bad("id may only use a-z, 0-9, '.', '_' and '-'");
		String name = text(v, "name", 32), author = text(v, "author", 32);
		String description = v.has("description") ? text(v, "description", 600) : "";
		noControl("name", name, false); noControl("author", author, false); noControl("description", description, true);
		if (script.trim().isEmpty()) throw new Bad("main.js is empty");
		return new Installed(id, name, author, description, script, folder);
	}

	private static String text(JsonValue v, String key, int max) throws Bad {
		JsonValue f = v.get(key);
		if (f == null || !f.isString() || f.asString().trim().isEmpty()) throw new Bad(key + " is required");
		String s = f.asString().trim();
		if (s.length() > max) throw new Bad(key + " is longer than " + max + " characters");
		return s;
	}

	private static void noControl(String key, String s, boolean newlinesOk) throws Bad {
		for (int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if (newlinesOk && c == '\n') continue;
			if (Character.isISOControl(c) || c == ' ' || c == ' ')
				throw new Bad(key + " can't contain " + (newlinesOk ? "control characters (only plain line breaks)" : "line breaks, tabs or control characters"));
		}
	}

	public Installed find(String id){
		for (Installed i : mods) if (i.id.equals(id)) return i;
		return null;
	}
}
