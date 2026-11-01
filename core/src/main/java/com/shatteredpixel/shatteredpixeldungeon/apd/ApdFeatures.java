/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

package com.shatteredpixel.shatteredpixeldungeon.apd;

import com.badlogic.gdx.Gdx;

//The owner's feature switches. Every one is OFF in the normal build. A private test build adds the asset "apd/features.txt"
//(see android/build.gradle, -PapdTestBuild), whose lines name the switches to turn on:
//  testBuild        shows the "PRIVATE TEST BUILD" banner
//  grudge           the Cleric Grudge humor (ClericGrudge); enabled in private tests; public release remains gated
//  demoUnlock       the UIMSD unlock gate and its demo
//  clericPlayable   Cleric can be picked to start a run (the un-ban; the ruleset changes only for runs played this way)
//Unknown lines and '#' comments are ignored. Tests can set the switches directly.
public final class ApdFeatures {

	//THE central switch for the Cleric Grudge / Lore feature (its UI, settings, popups, counters, notifications and background tracking all
	//read grudge() and nothing else). It is postponed until the public release: while this is false normal builds remain off, even if
	//apd/features.txt names "grudge". Private test builds may opt in explicitly, as approved by the owner.
	//(Cleric as a playable class is a separate switch, clericPlayable, and is not affected.)
	public static final boolean GRUDGE_LORE_IN_THIS_RELEASE = false;

	private static boolean loaded;
	private static boolean testBuild, grudge, demoUnlock, clericPlayable;

	private ApdFeatures(){}

	public static synchronized boolean testBuild(){ ensure(); return testBuild; }
	public static synchronized boolean grudge(){ ensure(); return (GRUDGE_LORE_IN_THIS_RELEASE || testBuild) && grudge; }
	public static synchronized boolean demoUnlock(){ ensure(); return demoUnlock; }
	public static synchronized boolean clericPlayable(){ ensure(); return clericPlayable; }

	private static void ensure(){
		if (loaded) return;
		//An early settings/scene lookup can happen before Android has installed its file service.
		//Keep the switches off for that lookup, but retry once the packaged assets can be read.
		if (Gdx.files == null) return;
		loaded = true;
		try {
			if (Gdx.files.internal( "apd/features.txt" ).exists()){
				parse( Gdx.files.internal( "apd/features.txt" ).readString( "UTF-8" ) );
			}
		} catch (Exception e){
			//a missing or unreadable file means every switch stays off
		}
	}

	public static synchronized void parse( String text ){
		testBuild = grudge = demoUnlock = clericPlayable = false;
		loaded = true;
		for (String raw : text.split( "\\r?\\n" )){
			String s = raw.trim();
			if (s.startsWith( "#" )) continue;
			switch (s){
				case "testBuild": testBuild = true; break;
				case "grudge": grudge = true; break;
				case "demoUnlock": demoUnlock = true; break;
				case "clericPlayable": clericPlayable = true; break;
			}
		}
	}

	//tests only: back to the normal build
	public static synchronized void resetForTest(){
		testBuild = grudge = demoUnlock = clericPlayable = false;
		loaded = true;
	}
}

