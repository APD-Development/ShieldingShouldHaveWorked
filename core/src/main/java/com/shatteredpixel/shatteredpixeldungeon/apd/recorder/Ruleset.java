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


package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;

import java.security.MessageDigest;

//The gameplay-affecting rules in force, as an identity. Two runs with the same ruleset id were played under the same
//rules, runs with different ids must not be silently mixed in statistics.
//
//What counts (decided with the statistics architecture): APD-0001 (starvation consumes shielding), Cleric blocked,
//the exotic crystal conversion chance, the challenge configuration, and future gameplay-affecting APD settings and
//mods. Changing an OFFICIAL APD setting changes the ruleset id but is not "modded": externalModsPresent is a separate
//flag (always false until a mod system exists).
//
//The id is "r1-" plus the first 12 hex digits of the SHA-256 of the rule text. "r1" is the version of this rule list:
//if a rule is ever added to the text, bump it so old and new ids can never collide.
public final class Ruleset {

	public static final String BASE_GAME = "Shattered Pixel Dungeon 4.0.0";
	private static final String ID_VERSION = "r1";

	public final String id;
	public final String text;
	public final int exoticChance;

	private Ruleset( String id, String text, int exoticChance ){
		this.id = id;
		this.text = text;
		this.exoticChance = exoticChance;
	}

	//reads the rules from the game right now
	public static Ruleset current(){
		return of(SPDSettings.apdExoticChance(), Dungeon.challenges);
	}

	public static Ruleset of( int exoticChance, int challenges ){
		String text = "base=spd-4.0.0;apd0001=on;cleric=blocked;exotic=" + exoticChance + ";challenges=" + challenges;
		return new Ruleset(ID_VERSION + "-" + hash12(text), text, exoticChance);
	}

	private static String hash12( String text ){
		try {
			byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 6; i++){
				sb.append(Character.forDigit((d[i] >> 4) & 0xF, 16)).append(Character.forDigit(d[i] & 0xF, 16));
			}
			return sb.toString();
		} catch (Exception e){
			//SHA-256 and UTF-8 always exist, but never let a recorder problem escape
			return "unknown00000";
		}
	}
}
