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


package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

//The registry of event type IDs.
//
//RULES (checked by a test against core/src/test/resources/apd/event_type_manifest.txt):
// - an ID is never reused for a different meaning, and never removed from this list
// - new event types only ever get new IDs
// - ID 0 is invalid
//
//Ranges: 1-255 storage infrastructure, 256 and up are for gameplay events, added in later phases.
//A reader that meets an ID it does not know skips the event, it is never an error.
public final class EventTypes {

	private EventTypes(){}

	public static final int INVALID = 0;

	//Recording was interrupted. payload: varint reason (GapReason), varint amount
	public static final int GAP = 1;

	//GapReason values, the first payload number of a GAP event
	public static final int GAP_DROPPED_EVENTS = 1;  //amount = events lost because the write queue was full
	public static final int GAP_RECOVERED_TAIL = 2;  //amount = bytes of damaged tail cut off when the log was reopened
	public static final int GAP_WRITER_FAILED = 3;   //amount = events lost after the writer failed
	public static final int GAP_RECORDING_BEHIND_SAVE = 4; //the game save is further on than the log, amount = game turns missing

	//---- run lifecycle events (telemetry 1.1), payloads are defined and tested in LifecycleEvents ----
	public static final int RUN_START = 256;
	public static final int RUN_END = 257;
	public static final int SAVE = 258;
	public static final int LEVEL_ENTER = 259;
	public static final int LEVEL_EXIT = 260;
	public static final int CHECKPOINT = 261;
	public static final int SESSION_START = 262;
	public static final int ROLLBACK = 263;
	public static final int RULESET_CHANGE = 264;

	private static final Map<Integer, String> NAMES;
	static {
		Map<Integer, String> m = new LinkedHashMap<>();
		m.put(GAP, "GAP");
		m.put(RUN_START, "RUN_START");
		m.put(RUN_END, "RUN_END");
		m.put(SAVE, "SAVE");
		m.put(LEVEL_ENTER, "LEVEL_ENTER");
		m.put(LEVEL_EXIT, "LEVEL_EXIT");
		m.put(CHECKPOINT, "CHECKPOINT");
		m.put(SESSION_START, "SESSION_START");
		m.put(ROLLBACK, "ROLLBACK");
		m.put(RULESET_CHANGE, "RULESET_CHANGE");
		NAMES = Collections.unmodifiableMap(m);
	}

	public static boolean isKnown( int type ){
		return NAMES.containsKey(type);
	}

	public static String name( int type ){
		String n = NAMES.get(type);
		return n != null ? n : "UNKNOWN_" + type;
	}

	//id -> name, in registration order
	public static Map<Integer, String> all(){
		return NAMES;
	}
}
