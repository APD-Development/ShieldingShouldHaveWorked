/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.android;

import com.shatteredpixel.shatteredpixeldungeon.apd.qol.VoiceCommands;

/** A voice engine plus the activity events it needs (dev build only). */
interface VoiceLifecycle extends VoiceCommands.Platform {
	int PERMISSION_REQUEST = 7341;
	void onPermission(boolean granted);
	void onPause();
	void onResume();
}
