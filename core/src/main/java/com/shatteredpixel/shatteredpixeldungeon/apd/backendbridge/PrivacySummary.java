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

package com.shatteredpixel.shatteredpixeldungeon.apd.backendbridge;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.StatsStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Plain-language account of what APD holds on this device and what could ever leave it. Read-only; it touches no network.
public final class PrivacySummary {

	public final int runLogs;
	public final long runLogBytes;
	public final int queued;

	private PrivacySummary( int runLogs, long runLogBytes, int queued ){
		this.runLogs = runLogs;
		this.runLogBytes = runLogBytes;
		this.queued = queued;
	}

	public static PrivacySummary of( File historyRoot ){
		int logs = 0;
		long bytes = 0;
		if (historyRoot != null){
			File[] files = new StatsStore(historyRoot).runsDir().listFiles();
			if (files != null){
				for (File f : files){
					if (f.isFile() && f.getName().endsWith(com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunIndex.LOG_SUFFIX)){
						logs++;
						bytes += f.length();
					}
				}
			}
		}
		return new PrivacySummary(logs, bytes, SubmissionQueue.pending(historyRoot));
	}

	public List<String> lines(){
		return lines(false);
	}

	public List<String> lines( boolean sendingOn ){
		List<String> out = new ArrayList<>();
		out.add("Your run history (" + runLogs + " run" + (runLogs == 1 ? "" : "s") + ", " + (runLogBytes / 1024) + " KB) stays on this device.");
		out.add(sendingOn ? "Sending is on, but nothing is sent by itself: only what you queued, and only when you press \"Send queued runs now\"."
				: "Nothing is sent anywhere: sending is off.");
		if (queued == 0){
			out.add("No runs are queued for submission.");
		} else {
			out.add(queued + " finished run" + (queued == 1 ? " is" : "s are") + " queued on this device as a short summary only (never the full history).");
		}
		out.add("You can read and remove each queued item first.");
		return out;
	}
}
