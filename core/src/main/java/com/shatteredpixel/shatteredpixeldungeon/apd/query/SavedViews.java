/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//The player's own named views: a saved query (QueryCodec text) under a name they chose, so an eleven-condition question is built once.
//Stays on the device with the history it belongs to (never sent anywhere). A saved view is only a QUESTION, never data: opening it asks the
//question again of the current history, so it stays correct as history grows and can never hold stale or hidden numbers.
//The file is one line per view, written to a temporary file and moved over the old one (a crash leaves the old complete file); a damaged line
//is skipped on reading, the rest is kept.
public final class SavedViews {

	public static final int MAX_VIEWS = 100, MAX_NAME = 60;
	public static final String FILE_NAME = "saved_views.txt";

	public static final class View {
		public final String name, encodedQuery;
		public final long createdAtMillis;

		View( String name, String encodedQuery, long createdAtMillis ){
			this.name = name;
			this.encodedQuery = encodedQuery;
			this.createdAtMillis = createdAtMillis;
		}

		//the question; null if this build cannot read it any more
		public Query query(){
			try {
				return QueryCodec.decode(encodedQuery);
			} catch (RuntimeException e){
				return null;
			}
		}
	}

	public enum Result { SAVED, REPLACED, TOO_MANY, BAD_NAME }

	private final List<View> views = new ArrayList<>();
	public int skippedOnLoad;

	public static File fileIn( File historyRoot ){
		return new File(historyRoot, FILE_NAME);
	}

	//the name as it will be kept (trimmed, one line), or null if it cannot be a name
	public static String cleanName( String raw ){
		if (raw == null) return null;
		String n = raw.trim();
		if (n.isEmpty() || n.length() > MAX_NAME) return null;
		for (int i = 0; i < n.length(); i++) if (Character.isISOControl(n.charAt(i))) return null;
		return n;
	}

	public List<View> list(){
		List<View> out = new ArrayList<>(views);
		Collections.sort(out, new Comparator<View>(){
			@Override
			public int compare( View a, View b ){
				return a.name.compareToIgnoreCase(b.name);
			}
		});
		return out;
	}

	public View get( String name ){
		String n = cleanName(name);
		if (n == null) return null;
		for (View v : views) if (v.name.equalsIgnoreCase(n)) return v;
		return null;
	}

	//saving under a name that exists replaces that view (the player chose the name)
	public Result save( String name, Query q, long nowMillis ){
		String n = cleanName(name);
		if (n == null) return Result.BAD_NAME;
		View old = get(n);
		if (old == null && views.size() >= MAX_VIEWS) return Result.TOO_MANY;
		View v = new View(n, QueryCodec.encode(q), nowMillis);
		if (old != null){
			views.set(views.indexOf(old), v);
			return Result.REPLACED;
		}
		views.add(v);
		return Result.SAVED;
	}

	public boolean delete( String name ){
		View v = get(name);
		return v != null && views.remove(v);
	}

	//false if the new name is not valid or is already taken by another view
	public boolean rename( String from, String to ){
		View v = get(from);
		String n = cleanName(to);
		if (v == null || n == null) return false;
		View other = get(n);
		if (other != null && other != v) return false;
		views.set(views.indexOf(v), new View(n, v.encodedQuery, v.createdAtMillis));
		return true;
	}

	public void store( File file ) throws IOException {
		File tmp = new File(file.getPath() + ".tmp");
		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8))){
			for (View v : views){
				w.write(b64(v.name) + "\t" + v.createdAtMillis + "\t" + b64(v.encodedQuery));
				w.newLine();
			}
		}
		//renaming over the old file replaces it in one step; if this system refuses, replace it the long way
		if (!tmp.renameTo(file)){
			if (file.exists() && !file.delete()) throw new IOException("cannot replace " + file);
			if (!tmp.renameTo(file)) throw new IOException("cannot write " + file);
		}
	}

	//a missing file is no views; never throws for damaged content
	public static SavedViews load( File file ){
		SavedViews s = new SavedViews();
		if (file == null || !file.exists()) return s;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
			String line;
			while ((line = in.readLine()) != null){
				if (line.isEmpty()) continue;
				try {
					String[] p = line.split("\t", -1);
					if (p.length != 3) throw new IllegalArgumentException("fields");
					String name = cleanName(unb64(p[0]));
					if (name == null || s.get(name) != null || s.views.size() >= MAX_VIEWS) throw new IllegalArgumentException("name");
					s.views.add(new View(name, unb64(p[2]), Long.parseLong(p[1])));
				} catch (RuntimeException e){
					s.skippedOnLoad++;
				}
			}
		} catch (IOException e){
			s.skippedOnLoad++;
		}
		return s;
	}

	private static String b64( String s ){
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private static String unb64( String s ){
		return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
	}
}
