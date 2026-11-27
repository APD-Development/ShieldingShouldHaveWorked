package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;

//The game's own visual playback speed (simulator settings file only). Written only when it changes.
final class PlaybackSpeed {
	private PlaybackSpeed(){}
	private static int last = -1;
	static void apply(int selection){
		if (selection == last) return;
		last = selection;
		if (SPDSettings.playbackSpeed() != selection) SPDSettings.playbackSpeed(selection);
	}
}
