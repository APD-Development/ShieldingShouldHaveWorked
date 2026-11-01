/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
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

package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//How much room the recorded history takes and whether to say something about it. It only measures: nothing is ever deleted or
//limited, and recording never depends on it. The warnings suggest exporting the history (or freeing space on the device); the
//thresholds are the project's, they are not a limit.
public final class StorageMeter {

	private StorageMeter(){}

	//the history is "large" from here (about 1,500 runs at the estimated 70 MB per 1,000), and "very large" from here
	public static final long LARGE_BYTES = 100L * 1024 * 1024;
	public static final long VERY_LARGE_BYTES = 500L * 1024 * 1024;
	//the device is running low when this little is free, or less than twice the history is
	public static final long LOW_FREE_BYTES = 200L * 1024 * 1024;

	public static final class Report {
		public int runLogs;
		public long runLogBytes;
		public int importedFiles;
		public long importedBytes;
		public long derivedBytes;       //caches and the index: rebuildable
		public long damagedTailBytes;
		public long freeBytes = -1;     //-1: unknown
		public final List<String> warnings = new ArrayList<>();

		//what is kept as evidence (the part that cannot be rebuilt)
		public long historyBytes(){
			return runLogBytes + importedBytes + damagedTailBytes;
		}

		public long totalBytes(){
			return historyBytes() + derivedBytes;
		}

		public String summary(){
			StringBuilder b = new StringBuilder();
			b.append("History: ").append(runLogs).append(runLogs == 1 ? " run" : " runs").append(", ").append(Size.text(historyBytes()));
			if (importedFiles > 0) b.append(" (+").append(importedFiles).append(" imported)");
			b.append(". Caches: ").append(Size.text(derivedBytes)).append(" (rebuildable).");
			if (freeBytes >= 0) b.append(" Free on device: ").append(Size.text(freeBytes)).append('.');
			for (String w : warnings) b.append("\n! ").append(w);
			return b.toString();
		}
	}

	public static Report measure( File root ){
		Report r = new Report();
		if (root == null || !root.isDirectory()) return r;
		File[] runs = new File(root, "runs").listFiles();
		if (runs != null){
			for (File f : runs){
				if (!f.isFile()) continue;
				if (f.getName().endsWith(".apdlog")){
					r.runLogs++;
					r.runLogBytes += f.length();
				} else if (f.getName().endsWith(".damaged")){
					r.damagedTailBytes += f.length();
				}
			}
		}
		File[] legacy = new File(root, "legacy").listFiles();
		if (legacy != null){
			for (File f : legacy){
				if (f.isFile() && f.getName().endsWith(".apdlog")){
					r.importedFiles++;
					r.importedBytes += f.length();
				}
			}
		}
		r.derivedBytes = size(new File(root, "derived")) + new File(root, "index.apdi").length();
		try {
			long free = root.getUsableSpace();
			r.freeBytes = free > 0 ? free : -1;
		} catch (RuntimeException ignored){
		}
		long history = r.historyBytes();
		if (history >= VERY_LARGE_BYTES){
			r.warnings.add("The history is very large (" + Size.text(history) + "). Export it and keep a copy somewhere safe.");
		} else if (history >= LARGE_BYTES){
			r.warnings.add("The history is getting large (" + Size.text(history) + "). Exporting a copy is a good idea.");
		}
		if (r.freeBytes >= 0 && (r.freeBytes < LOW_FREE_BYTES || r.freeBytes < 2 * history)){
			r.warnings.add("This device is running low on free space (" + Size.text(r.freeBytes) + "). APD keeps recording, but a full disk will stop it.");
		}
		return r;
	}

	private static long size( File f ){
		if (f.isFile()) return f.length();
		long n = 0;
		File[] kids = f.listFiles();
		if (kids != null) for (File k : kids) n += size(k);
		return n;
	}
}
