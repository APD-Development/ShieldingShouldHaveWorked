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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Reads line packs: plain text, one line per record, fields separated by '|':
//
//   id | category | flags | requires | weight | clean text | adult text
//
// flags: '-' or a comma list of  since=1.10  joke  meta.   requires: '-' or conditions joined by '&' (see Term).   weight: 1-100.
// adult text may be empty (the clean text is then used for everyone). Lines starting with '#' and blank lines are ignored.
//
// Parsing is also the lint: a pack with a duplicate id, a missing clean text, profanity in the clean text, an unknown key, a joke that
// mixes in recorded data, or anything that is not about Cleric is refused as a whole with the line number, so a bad pack never half-loads.
public final class LinePack {

	public static final class PackException extends RuntimeException {
		public PackException( String message ){
			super( message );
		}
	}

	static final Pattern ID = Pattern.compile( "[a-z][a-z0-9]*(\\.[a-z0-9]+)+" );
	static final int MAX_CLEAN = 240, MAX_ADULT = 280;

	// words that may not appear in the clean wording (whole-word, any case)
	static final Pattern PROFANITY = Pattern.compile(
			"\\b(fuck\\w*|shit\\w*|bitch\\w*|bastard\\w*|asshole\\w*|dick\\w*|piss\\w*|cunt\\w*|goddamn\\w*|bullshit|wank\\w*|twat\\w*|motherfuck\\w*|arse\\w*|slut\\w*|whore\\w*)\\b",
			Pattern.CASE_INSENSITIVE );

	// the humor never says the game was changed: no buff/nerf/patch claims in a line (real patch notes are written separately and truthfully)
	static final Pattern BALANCE_CLAIM = Pattern.compile( "\\b(nerf(ed|s)?|buff(ed|s)?|patch(ed)?|hotfix(ed)?|rebalanc\\w*|next update|next patch)\\b", Pattern.CASE_INSENSITIVE );

	// every line is about Cleric: it names Cleric or something that only Cleric has
	static final Pattern ABOUT_CLERIC = Pattern.compile(
			"cleric|priest|paladin|holy tome|\\btome\\b|guiding light|ascended form|trinity|power of many",
			Pattern.CASE_INSENSITIVE );
	// no other class is ever the target: other class names may only arrive through a {placeholder}, never written into a line
	static final Pattern OTHER_CLASS = Pattern.compile( "\\b(warrior|mage|rogue|huntress|duelist)\\b", Pattern.CASE_INSENSITIVE );

	private LinePack(){}

	public static List<Line> parse( String text, String source ){
		List<Line> lines = new ArrayList<>();
		Set<String> ids = new HashSet<>();
		StringBuilder errors = new StringBuilder();
		String[] rows = text.split( "\r?\n", -1 );
		for (int i = 0; i < rows.length; i++){
			String row = rows[i];
			if (row.trim().isEmpty() || row.startsWith( "#" )) continue;
			try {
				Line l = parseLine( row );
				if (!ids.add( l.id )) throw new IllegalArgumentException( "duplicate id " + l.id );
				lines.add( l );
			} catch (IllegalArgumentException e){
				//every problem in the pack is reported, so an author fixes them in one pass
				if (errors.length() > 0) errors.append( '\n' );
				errors.append( source ).append( ':' ).append( i + 1 ).append( ": " ).append( e.getMessage() );
			}
		}
		if (errors.length() > 0) throw new PackException( errors.toString() );
		return lines;
	}

	// Problems with how lines point at each other: a prerequisite that does not exist, a story with no second part, a loop.
	// Returns the ids of lines that can never be shown (their prerequisite is missing, directly or further back).
	public static Set<String> unreachable( List<Line> lines, List<String> problems ){
		java.util.Map<String, Line> byId = new java.util.HashMap<>();
		for (Line l : lines) byId.put( l.id, l );
		Set<String> followed = new HashSet<>();
		Set<String> bad = new HashSet<>();
		for (Line l : lines){
			if (l.after == null) continue;
			followed.add( l.after );
			Line p = byId.get( l.after );
			if (p == null){
				problems.add( l.id + " follows " + l.after + ", which does not exist" );
				bad.add( l.id );
			} else if (p.pool != l.pool){
				problems.add( l.id + " follows a line of the other pool (" + p.id + ")" );
				bad.add( l.id );
			}
			//a loop never starts
			Set<String> seen = new HashSet<>();
			Line cur = l;
			while (cur != null && cur.after != null){
				if (!seen.add( cur.id )){
					problems.add( l.id + " is part of a loop" );
					bad.add( l.id );
					break;
				}
				cur = byId.get( cur.after );
			}
		}
		for (Line l : lines){
			if (l.setup && !followed.contains( l.id )) problems.add( l.id + " sets up a story but nothing follows it" );
		}
		//everything that depends on a bad line is unreachable too
		boolean grew = true;
		while (grew){
			grew = false;
			for (Line l : lines) if (l.after != null && bad.contains( l.after ) && bad.add( l.id )) grew = true;
		}
		return bad;
	}

	private static Line parseLine( String row ){
		String[] f = row.split( "\\|", -1 );
		if (f.length != 7) throw new IllegalArgumentException( "expected 7 fields separated by '|', found " + f.length );
		for (int i = 0; i < f.length; i++) f[i] = f[i].trim();

		String id = f[0];
		if (!ID.matcher( id ).matches()) throw new IllegalArgumentException( "bad id '" + id + "'" );
		Category cat;
		try {
			cat = Category.valueOf( f[1].toUpperCase( Locale.ROOT ) );
		} catch (IllegalArgumentException e){
			throw new IllegalArgumentException( "unknown category '" + f[1] + "'" );
		}

		int since = 0;
		boolean joke = false, meta = false, appreciation = false, setup = false, nextSession = false;
		String after = null;
		if (!f[2].equals( "-" )){
			for (String flag : f[2].split( "," )){
				flag = flag.trim();
				if (flag.equals( "joke" )) joke = true;
				else if (flag.equals( "meta" )) meta = true;
				else if (flag.equals( "appreciation" )) appreciation = true;
				else if (flag.equals( "setup" )) setup = true;
				else if (flag.equals( "nextsession" )) nextSession = true;
				else if (flag.startsWith( "after=" )){
					after = flag.substring( 6 ).trim();
					if (!ID.matcher( after ).matches()) throw new IllegalArgumentException( "bad prerequisite id '" + after + "'" );
					if (after.equals( id )) throw new IllegalArgumentException( "a line cannot follow itself" );
				}
				else if (flag.startsWith( "since=" )) since = version( flag.substring( 6 ) );
				else throw new IllegalArgumentException( "unknown flag '" + flag + "'" );
			}
		}
		if (nextSession && after == null) throw new IllegalArgumentException( "nextsession needs after=<id>" );
		if (appreciation != id.startsWith( "app." )) throw new IllegalArgumentException( "appreciation lines, and only those, have ids starting 'app.'" );
		if (meta && (appreciation || after != null || setup)) throw new IllegalArgumentException( "a meta line stands alone" );
		if (meta != (cat == Category.META)) throw new IllegalArgumentException( "the meta flag and the META category go together" );

		List<Term> requires = new ArrayList<>();
		if (!f[3].equals( "-" )){
			for (String t : f[3].split( "&" )){
				Term term = Term.parse( t.trim() );
				if ((term.key.equals( "hero" ) || term.key.equals( "lastHero" )) && !term.value.equalsIgnoreCase( "CLERIC" ))
					throw new IllegalArgumentException( "conditions on the class may only name CLERIC" );
				requires.add( term );
			}
		}

		int weight;
		try {
			weight = Integer.parseInt( f[4] );
		} catch (NumberFormatException e){
			throw new IllegalArgumentException( "bad weight '" + f[4] + "'" );
		}
		if (weight < 1 || weight > 100) throw new IllegalArgumentException( "weight must be 1-100" );

		String clean = f[5], adult = f[6].isEmpty() ? null : f[6];
		if (clean.isEmpty()) throw new IllegalArgumentException( "every line needs clean wording" );
		if (clean.length() > MAX_CLEAN) throw new IllegalArgumentException( "clean wording is longer than " + MAX_CLEAN );
		if (adult != null && adult.length() > MAX_ADULT) throw new IllegalArgumentException( "adult wording is longer than " + MAX_ADULT );
		for (String t : new String[]{ clean, adult == null ? "" : adult }){
			Matcher claim = BALANCE_CLAIM.matcher( t );
			if (claim.find()) throw new IllegalArgumentException( "a line must not claim a balance or patch change ('" + claim.group() + "')" );
		}
		Matcher bad = PROFANITY.matcher( clean );
		if (bad.find()) throw new IllegalArgumentException( "clean wording contains '" + bad.group() + "'" );

		if (cat != Category.META){
			for (String s : new String[]{ clean, adult == null ? "" : adult }){
				if (!s.isEmpty() && !ABOUT_CLERIC.matcher( s ).find()) throw new IllegalArgumentException( "a line must be about Cleric" );
				if (OTHER_CLASS.matcher( s ).find()) throw new IllegalArgumentException( "other classes are never named in a line" );
			}
		}

		Set<String> cleanKeys = placeholders( clean );
		if (adult != null && !placeholders( adult ).equals( cleanKeys ))
			throw new IllegalArgumentException( "the clean and adult wording must use the same {placeholders}" );
		for (String k : cleanKeys)
			if (!Context.KEYS.contains( k )) throw new IllegalArgumentException( "unknown placeholder {" + k + "}" );
		if (joke && !cleanKeys.isEmpty()) throw new IllegalArgumentException( "a joke with invented numbers cannot use recorded {placeholders}" );

		return new Line( id, cat, since, !meta, joke, weight, appreciation ? Pool.APPRECIATION : Pool.HATRED, after, nextSession, setup, requires, clean, adult );
	}

	private static Set<String> placeholders( String text ){
		Set<String> keys = new HashSet<>();
		Matcher m = Line.PLACEHOLDER.matcher( text );
		while (m.find()) keys.add( m.group( 1 ) );
		return keys;
	}

	private static int version( String v ){
		String[] p = v.split( "\\." );
		if (p.length != 2) throw new IllegalArgumentException( "bad version '" + v + "'" );
		try {
			return Integer.parseInt( p[0] ) * 1000 + Integer.parseInt( p[1] );
		} catch (NumberFormatException e){
			throw new IllegalArgumentException( "bad version '" + v + "'" );
		}
	}
}
