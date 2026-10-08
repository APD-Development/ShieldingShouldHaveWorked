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

package com.shatteredpixel.shatteredpixeldungeon.apd.humor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// What this player has been shown, kept in one small local text file that is never uploaded or exported: per line id, how many times and
// when (a running counter and the clock), plus a few state values for the popup budget. It lives apart from run logs and the save, so it
// can never touch either. A damaged or missing file simply reads as "nothing seen yet": it never throws into the game.
public final class Ledger {

	public static final class Seen {
		public int count;
		public long lastSeq;
		public long lastAt;
	}

	private final File file;
	private final Map<String, Seen> seen = new HashMap<>();
	private final Map<String, String> state = new HashMap<>();
	private long seq;

	// file == null keeps everything in memory (used by tests and when storage is unavailable)
	public Ledger( File file ){
		this.file = file;
		load();
	}

	private void load(){
		if (file == null || !file.isFile()) return;
		try (BufferedReader r = new BufferedReader( new InputStreamReader( new FileInputStream( file ), StandardCharsets.UTF_8 ) )){
			String row;
			while ((row = r.readLine()) != null){
				try {
					if (row.startsWith( "@seq=" )){
						seq = Math.max( seq, Long.parseLong( row.substring( 5 ) ) );
					} else if (row.startsWith( "@" )){
						int eq = row.indexOf( '=' );
						if (eq > 1) state.put( row.substring( 1, eq ), row.substring( eq + 1 ) );
					} else if (!row.startsWith( "#" ) && !row.isEmpty()){
						String[] p = row.split( "\t" );
						Seen s = new Seen();
						s.count = Integer.parseInt( p[1] );
						s.lastSeq = Long.parseLong( p[2] );
						s.lastAt = Long.parseLong( p[3] );
						if (s.count > 0){
							seen.put( p[0], s );
							seq = Math.max( seq, s.lastSeq );
						}
					}
				} catch (RuntimeException badRow){
					//one damaged row is skipped, the rest still count
				}
			}
		} catch (IOException e){
			//unreadable: start empty
		}
	}

	public synchronized void save(){
		if (file == null) return;
		try {
			File dir = file.getParentFile();
			if (dir != null) dir.mkdirs();
			File tmp = new File( file.getPath() + ".tmp" );
			try (Writer w = new OutputStreamWriter( new FileOutputStream( tmp ), StandardCharsets.UTF_8 )){
				w.write( "#apd-humor-ledger v1\n" );
				w.write( "@seq=" + seq + "\n" );
				for (Map.Entry<String, String> e : state.entrySet()) w.write( "@" + e.getKey() + "=" + e.getValue() + "\n" );
				for (Map.Entry<String, Seen> e : seen.entrySet()){
					Seen s = e.getValue();
					w.write( e.getKey() + "\t" + s.count + "\t" + s.lastSeq + "\t" + s.lastAt + "\n" );
				}
			}
			if (!tmp.renameTo( file )){
				file.delete();
				tmp.renameTo( file );
			}
		} catch (IOException e){
			//the history is a nicety: a failed save must never reach the game
		}
	}

	public synchronized void markShown( String id, long now ){
		Seen s = seen.get( id );
		if (s == null){
			s = new Seen();
			seen.put( id, s );
		}
		s.count++;
		s.lastSeq = ++seq;
		s.lastAt = now;
	}

	public synchronized Seen get( String id ){
		return seen.get( id );
	}

	public synchronized long seq(){
		return seq;
	}

	// how many of the given ids this player has been shown at least once
	public synchronized int distinctSeenAmong( Set<String> ids ){
		int n = 0;
		for (String id : ids) if (seen.containsKey( id )) n++;
		return n;
	}

	public synchronized String state( String key ){
		return state.get( key );
	}

	public synchronized long stateLong( String key, long dflt ){
		try {
			String v = state.get( key );
			return v == null ? dflt : Long.parseLong( v );
		} catch (NumberFormatException e){
			return dflt;
		}
	}

	public synchronized void setState( String key, String value ){
		state.put( key, value );
	}

	// the player's explicit reset
	public synchronized void clear(){
		seen.clear();
		state.clear();
		seq = 0;
		save();
	}
}
