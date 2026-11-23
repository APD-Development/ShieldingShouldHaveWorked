/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Voice controls (Chris, Nov 23): Chris's dev build only. Spoken words become the same game actions
 * as keyboard keys, so they go through the exact same code paths as a key press. Play builds never
 * set a platform, so nothing here runs there.
 */
public final class VoiceCommands {
	private VoiceCommands(){}

	/** The microphone side, set by the Android launcher in debug (dev) builds only. */
	public interface Platform {
		void start();
		void stop();
	}

	private static Platform platform;
	private static boolean listening;

	/** Private key codes for voice; 256-999 is treated as unbound by KeyBindings, so start well above it. */
	static final int BASE_CODE = 7000;

	static final GameAction[] ACTIONS = {
			SPDAction.N, SPDAction.S, SPDAction.E, SPDAction.W,
			SPDAction.NE, SPDAction.NW, SPDAction.SE, SPDAction.SW,
			SPDAction.WAIT, SPDAction.REST, SPDAction.WAIT_OR_PICKUP,
			SPDAction.TAG_ATTACK, SPDAction.TAG_LOOT, SPDAction.TAG_RESUME, SPDAction.TAG_ACTION,
			SPDAction.INVENTORY, SPDAction.JOURNAL, SPDAction.HERO_INFO, SPDAction.EXAMINE,
			SPDAction.QUICKSLOT_1, SPDAction.QUICKSLOT_2, SPDAction.QUICKSLOT_3,
			SPDAction.QUICKSLOT_4, SPDAction.QUICKSLOT_5, SPDAction.QUICKSLOT_6,
			SPDAction.BACK, SPDAction.ZOOM_IN, SPDAction.ZOOM_OUT, SPDAction.CYCLE
	};

	public static void setPlatform(Platform p){
		platform = p;
		if (p != null) for (int i = 0; i < ACTIONS.length; i++) KeyBindings.addHardBinding(BASE_CODE + i, ACTIONS[i]);
	}

	public static boolean available(){ return platform != null; }
	public static boolean listening(){ return listening; }

	public static void setListening(boolean on){
		if (platform == null) return;
		listening = on;
		if (on) platform.start(); else platform.stop();
	}

	/** Phrase -> action, checked in order so "north east" wins over "north". */
	private static final Object[][] PHRASES = {
			{"stop listening", null}, {"voice off", null},
			// before the directions, so "pick up" isn't heard as "up"
			{"pick up", SPDAction.WAIT_OR_PICKUP}, {"pickup", SPDAction.WAIT_OR_PICKUP}, {"grab", SPDAction.WAIT_OR_PICKUP}, {"take", SPDAction.WAIT_OR_PICKUP},
			{"north east", SPDAction.NE}, {"northeast", SPDAction.NE}, {"up right", SPDAction.NE},
			{"north west", SPDAction.NW}, {"northwest", SPDAction.NW}, {"up left", SPDAction.NW},
			{"south east", SPDAction.SE}, {"southeast", SPDAction.SE}, {"down right", SPDAction.SE},
			{"south west", SPDAction.SW}, {"southwest", SPDAction.SW}, {"down left", SPDAction.SW},
			{"north", SPDAction.N}, {"up", SPDAction.N},
			{"south", SPDAction.S}, {"down", SPDAction.S},
			{"east", SPDAction.E}, {"right", SPDAction.E},
			{"west", SPDAction.W}, {"left", SPDAction.W},
			{"attack", SPDAction.TAG_ATTACK}, {"fight", SPDAction.TAG_ATTACK}, {"hit", SPDAction.TAG_ATTACK},
			{"loot", SPDAction.TAG_LOOT},
			{"continue", SPDAction.TAG_RESUME}, {"resume", SPDAction.TAG_RESUME},
			{"action", SPDAction.TAG_ACTION},
			{"rest", SPDAction.REST}, {"sleep", SPDAction.REST},
			{"wait", SPDAction.WAIT},
			{"inventory", SPDAction.INVENTORY}, {"backpack", SPDAction.INVENTORY}, {"bag", SPDAction.INVENTORY},
			{"journal", SPDAction.JOURNAL},
			{"hero", SPDAction.HERO_INFO}, {"stats", SPDAction.HERO_INFO},
			{"examine", SPDAction.EXAMINE}, {"look", SPDAction.EXAMINE},
			{"slot one", SPDAction.QUICKSLOT_1}, {"slot 1", SPDAction.QUICKSLOT_1},
			{"slot two", SPDAction.QUICKSLOT_2}, {"slot to", SPDAction.QUICKSLOT_2}, {"slot too", SPDAction.QUICKSLOT_2}, {"slot 2", SPDAction.QUICKSLOT_2},
			{"slot three", SPDAction.QUICKSLOT_3}, {"slot 3", SPDAction.QUICKSLOT_3},
			{"slot four", SPDAction.QUICKSLOT_4}, {"slot for", SPDAction.QUICKSLOT_4}, {"slot 4", SPDAction.QUICKSLOT_4},
			{"slot five", SPDAction.QUICKSLOT_5}, {"slot 5", SPDAction.QUICKSLOT_5},
			{"slot six", SPDAction.QUICKSLOT_6}, {"slot 6", SPDAction.QUICKSLOT_6},
			{"zoom in", SPDAction.ZOOM_IN}, {"zoom out", SPDAction.ZOOM_OUT},
			{"next", SPDAction.CYCLE}, {"cycle", SPDAction.CYCLE},
			{"back", SPDAction.BACK}, {"cancel", SPDAction.BACK}, {"close", SPDAction.BACK},
	};

	static String normalize(String s){
		if (s == null) return "";
		String n = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
		n = n.replaceAll("\\bquick ?slot\\b", "slot");
		return n.replaceAll("^(go|move|walk|step) ", "");
	}

	/** Result of hearing one phrase: an action, a "stop listening" request, or nothing recognised. */
	public static final class Match {
		public final GameAction action; public final boolean stop;
		Match(GameAction a, boolean s){ action = a; stop = s; }
	}

	/** Whole-word match of the first phrase found. Returns null if nothing matched. */
	public static Match parse(String heard){
		String n = " " + normalize(heard) + " ";
		for (Object[] p : PHRASES){
			if (n.contains(" " + p[0] + " ")) return new Match((GameAction) p[1], p[1] == null);
		}
		return null;
	}

	/** Called by the platform (any thread) with the recognizer's guesses, best first. The understanding runs on the game thread. */
	public static void heard(final List<String> alternatives){
		if (alternatives == null || alternatives.isEmpty()) return;
		final List<String> copy = new ArrayList<>(alternatives);
		com.watabou.noosa.Game.runOnRenderThread(() -> VoiceBrain.understand(copy));
	}

	static void stopFromVoice(){
		setListening(false);
		com.shatteredpixel.shatteredpixeldungeon.SPDSettings.apdVoiceControls(false);
	}

	/** Queue a press and release, exactly like tapping that key; safe to call from any thread. */
	static void press(GameAction action){
		for (int i = 0; i < ACTIONS.length; i++){
			if (ACTIONS[i] == action){
				KeyEvent.addKeyEvent(new KeyEvent(BASE_CODE + i, true));
				KeyEvent.addKeyEvent(new KeyEvent(BASE_CODE + i, false));
				return;
			}
		}
	}

	/** Every phrase the game understands, for the settings note. */
	public static List<String> phrases(){
		ArrayList<String> out = new ArrayList<>();
		for (Object[] p : PHRASES) out.add((String) p[0]);
		return out;
	}
}
