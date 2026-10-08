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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// The Developer's Personal Vendetta Against Cleric: offline flavor text, nothing else. It decides what to say and when it may be said; the
// caller shows it. It reads no game state itself (the caller passes the recorded facts and whether the moment is quiet), uses its own random
// source, and writes only its own ledger file. Cleric is its only subject.
//
// Three kinds of appearance, all separate from play:
//   popup()           a surprise, at a quiet moment; at most one per session, rare, and off if the player says so
//   inline()          a small contextual line in a place the player is already looking (results, menu); never a window of its own
//   firstClericRun()  the one-time warning when the player picks Cleric to start a run
public final class ClericGrudge {

	// The owner's switch: the whole feature stays off until the content has been reviewed and tried on a phone.
	public static final boolean ENABLED = false;

	// nothing in the first twenty minutes after the feature first sees the player
	static final long GRACE_MS = 20L * 60 * 1000;
	// a contextual inline line: how often a moment shows one, and never twice within this time
	static final double INLINE_CHANCE = 0.25;
	static final long INLINE_GAP_MS = 5L * 60 * 1000;

	static final String ST_INSTALL = "installAt", ST_LAST_POPUP = "lastPopupAt", ST_LAST_INLINE = "lastInlineAt",
			ST_LAST_CATEGORY = "lastCategory", ST_FIRST_RUN = "firstRunShown", ST_COUNTER = "counterShown";

	public interface PackSource {
		List<String> names() throws IOException;
		String read( String name ) throws IOException;
	}

	private final List<Line> lines;
	private final Ledger ledger;
	private final Random rng;
	private final boolean enabled;
	private boolean popupThisSession;

	public ClericGrudge( List<Line> lines, Ledger ledger, Random rng, boolean enabled ){
		this.lines = lines;
		this.ledger = ledger;
		this.rng = rng;
		this.enabled = enabled;
	}

	// Loads every pack the source lists. A pack that fails its lint is refused whole and the others still load.
	public static List<Line> loadPacks( PackSource source ){
		List<Line> all = new ArrayList<>();
		Set<String> ids = new HashSet<>();
		try {
			for (String name : source.names()){
				try {
					List<Line> pack = LinePack.parse( source.read( name ), name );
					boolean clash = false;
					for (Line l : pack) if (ids.contains( l.id )) clash = true;
					if (clash) continue;
					for (Line l : pack) ids.add( l.id );
					all.addAll( pack );
				} catch (LinePack.PackException | IOException skip){
					//a broken pack is left out; the game goes on without it
				}
			}
		} catch (IOException e){
			//no packs: no humor
		}
		return all;
	}

	public Counter counter(){
		Set<String> counted = new HashSet<>();
		for (Line l : lines) if (l.counted) counted.add( l.id );
		return new Counter( counted.size(), ledger.distinctSeenAmong( counted ) );
	}

	// A surprise at a quiet moment, or null. Rolls once; at most one per session.
	public synchronized Shown popup( Moment moment, Context ctx, Safety safety, Frequency freq, boolean adult, long now ){
		if (!enabled || !moment.surprise || freq == Frequency.OFF || !safety.ok() || popupThisSession) return null;
		long install = ledger.stateLong( ST_INSTALL, -1 );
		if (install < 0){
			ledger.setState( ST_INSTALL, Long.toString( now ) );
			ledger.save();
			return null;
		}
		if (now - install < GRACE_MS) return null;
		long last = ledger.stateLong( ST_LAST_POPUP, -1 );
		if (last >= 0 && now - last < freq.minGapMs) return null;
		if (rng.nextDouble() >= freq.chance) return null;
		Shown s = choose( moment, ctx, adult, now );
		if (s == null) return null;
		popupThisSession = true;
		ledger.setState( ST_LAST_POPUP, Long.toString( now ) );
		ledger.save();
		return s;
	}

	// A small contextual line for a place the player is already looking at, or null.
	public synchronized Shown inline( Moment moment, Context ctx, boolean adult, long now ){
		if (!enabled || !moment.surprise) return null;
		long last = ledger.stateLong( ST_LAST_INLINE, -1 );
		if (last >= 0 && now - last < INLINE_GAP_MS) return null;
		if (rng.nextDouble() >= INLINE_CHANCE) return null;
		Shown s = choose( moment, ctx, adult, now );
		if (s == null) return null;
		ledger.setState( ST_LAST_INLINE, Long.toString( now ) );
		ledger.save();
		return s;
	}

	// A line on request, ignoring the chance and the gap (for a page the player opens on purpose, and for tests). Still unseen-first.
	synchronized Shown inlineAlways( Moment moment, Context ctx, long now ){
		if (!enabled) return null;
		return choose( moment, ctx, false, now );
	}

	// The one-time warning when the player first picks Cleric to start a run.
	public synchronized Shown firstClericRun( boolean adult, long now ){
		if (!enabled || ledger.state( ST_FIRST_RUN ) != null) return null;
		Shown s = choose( Moment.FIRST_CLERIC_RUN, Context.EMPTY, adult, now );
		if (s == null) return null;
		ledger.setState( ST_FIRST_RUN, "1" );
		ledger.save();
		return s;
	}

	// The one-time joke the first time the counter screen is opened.
	public synchronized Shown counterDiscovery( boolean adult, long now ){
		if (!enabled || ledger.state( ST_COUNTER ) != null) return null;
		Shown s = choose( Moment.COUNTER_SCREEN, Context.EMPTY, adult, now );
		if (s == null) return null;
		ledger.setState( ST_COUNTER, "1" );
		ledger.save();
		return s;
	}

	private Shown choose( Moment moment, Context ctx, boolean adult, long now ){
		Category avoid = null;
		String last = ledger.state( ST_LAST_CATEGORY );
		if (last != null){
			try {
				avoid = Category.valueOf( last );
			} catch (IllegalArgumentException ignored){
			}
		}
		Line l = Picker.pick( lines, moment, ctx, ledger, rng, avoid );
		if (l == null) return null;
		String text = l.render( ctx, adult );
		if (text == null) return null;
		ledger.markShown( l.id, now );
		ledger.setState( ST_LAST_CATEGORY, l.category.name() );
		ledger.save();
		return new Shown( l.id, l.category, text );
	}
}
