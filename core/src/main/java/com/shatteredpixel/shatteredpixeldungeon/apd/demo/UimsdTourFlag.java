package com.shatteredpixel.shatteredpixeldungeon.apd.demo;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;

/** Chris, Nov 26: the tour hides after the first view. Finishing it, exiting it or backing out of it marks it taken
 *  for good on this install, so the welcome card never offers it again, however many runs there are. The Guide's
 *  "Take the tour" entry still replays it on purpose. */
public final class UimsdTourFlag {
	private UimsdTourFlag(){}

	/** Called whenever a tour that was running ends, however it ended. */
	public static void tourEnded(){
		SPDSettings.apdUimsdTourDone(true);
	}

	/** The welcome card's "Take the tour" button. */
	public static boolean showWelcomeTourButton(){
		return !SPDSettings.apdUimsdTourDone();
	}
}
