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

package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadStatus;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.StatsStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//Adds the history of an archive to this device's history. It only ever ADDS:
//  - the manifest is checked first (its own checksum, format, totals); a damaged or newer one stops everything before anything is written;
//  - every file is streamed to a temporary file and must match the manifest's size and SHA-256 and be a readable APD log of the run the
//    manifest says (a file that does not is dropped and reported, the others continue);
//  - a log that already exists is NEVER replaced: an identical one is skipped, a different one is kept as it is and reported;
//  - only "runs/<id>.apdlog" and "legacy/<id>.apdlog" with plain ids can be written, whatever the archive's entry names say;
//  - synthetic demo data is rejected, and a folder that carries the demo marker is refused;
//  - the index is rebuilt afterwards, and the derived caches rebuild themselves from the new logs.
//Reading is streamed, a file at a time, so a large archive does not need to fit in memory. Never throws: problems are in the Report.
public final class ArchiveImporter {

	private ArchiveImporter(){}

	//no single log can be larger than this (the biggest real run logs are a few MB)
	public static final long MAX_FILE_BYTES = 512L * 1024 * 1024;
	//room to keep free on the device while importing
	public static final long FREE_SPACE_MARGIN = 16L * 1024 * 1024;

	public static final class Report {
		public boolean ok;
		public String problem = "";
		public int imported;
		public int skippedAlreadyHere;
		public int keptDifferentExisting;
		public int rejected;
		public int newerThanThisBuild;
		public int redactedImported;
		public final List<String> details = new ArrayList<>();

		public String summary(){
			if (!ok && imported == 0 && skippedAlreadyHere == 0) return "Nothing was imported: " + problem;
			StringBuilder b = new StringBuilder();
			b.append("Imported ").append(imported).append(imported == 1 ? " log" : " logs").append(".");
			if (skippedAlreadyHere > 0) b.append("\n").append(skippedAlreadyHere).append(" already here (identical), skipped.");
			if (keptDifferentExisting > 0) b.append("\n").append(keptDifferentExisting).append(" differ from a log already here: yours was kept, nothing was replaced.");
			if (redactedImported > 0) b.append("\n").append(redactedImported).append(" belong to runs that were not finished when exported (hidden information left out).");
			if (newerThanThisBuild > 0) b.append("\n").append(newerThanThisBuild).append(" were recorded by a newer telemetry version: kept, but this version may not show all of it.");
			if (rejected > 0) b.append("\n").append(rejected).append(" were not imported (damaged, not matching the manifest, or not allowed).");
			if (!ok) b.append("\nStopped early: ").append(problem);
			for (String d : details) b.append("\n- ").append(d);
			return b.toString();
		}
	}

	private static final Object LOCK = new Object();

	public static Report importArchive( File root, InputStream in ){
		Report report = new Report();
		File tmpDir = null;
		synchronized (LOCK){
			try {
				if (root == null){
					report.problem = "history is not set up.";
					return report;
				}
				if (new File(root, DatasetStore.DEMO_MARKER_FILE).isFile()){
					report.problem = "this folder holds demo data, which never receives history.";
					return report;
				}
				ZipInputStream zip = new ZipInputStream(in);
				ZipEntry first = zip.getNextEntry();
				if (first == null || !ArchiveManifest.ENTRY_NAME.equals(first.getName())){
					report.problem = "this is not an APD history archive (no manifest first).";
					return report;
				}
				byte[] manifestBytes = readLimited(zip, ArchiveManifest.MAX_BYTES);
				if (manifestBytes == null){
					report.problem = "the manifest is too large.";
					return report;
				}
				ArchiveManifest manifest;
				try {
					manifest = ArchiveManifest.parse(manifestBytes);
				} catch (IllegalArgumentException bad){
					report.problem = bad.getMessage();
					return report;
				}
				//every entry the manifest lists must be a path we would ever write, a plain size, and not demo data
				for (ArchiveManifest.Entry e : manifest.files){
					if (!ArchiveManifest.isValidPath(e.path)){
						report.problem = "the archive lists a file it is not allowed to write: " + e.path;
						return report;
					}
					if (e.size > MAX_FILE_BYTES){
						report.problem = "the archive lists a file that is too large: " + e.path;
						return report;
					}
					if (e.logId().startsWith(DatasetStore.DEMO_LOG_PREFIX) || e.runId.startsWith(DatasetStore.DEMO_LOG_PREFIX)){
						report.problem = "the archive contains demo data, which is never imported.";
						return report;
					}
				}
				if (root.getUsableSpace() > 0 && root.getUsableSpace() < manifest.totalBytes() + FREE_SPACE_MARGIN){
					report.problem = "there is not enough free space (" + Size.text(manifest.totalBytes()) + " needed).";
					return report;
				}

				tmpDir = new File(root, "import-tmp");
				deleteTree(tmpDir);
				if (!tmpDir.mkdirs() && !tmpDir.isDirectory()){
					report.problem = "cannot create a working folder.";
					return report;
				}
				Set<String> seen = new HashSet<>();
				ZipEntry entry;
				while ((entry = zip.getNextEntry()) != null){
					String name = entry.getName();
					ArchiveManifest.Entry e = manifest.find(name);
					if (e == null || !seen.add(name)){
						report.rejected++;
						report.details.add("not in the manifest or repeated, ignored: " + safe(name));
						continue;
					}
					handle(root, tmpDir, zip, e, report);
				}
				for (ArchiveManifest.Entry e : manifest.files){
					if (!seen.contains(e.path)){
						report.rejected++;
						report.details.add("listed but missing from the archive: " + e.path);
					}
				}
				report.ok = true;
				rebuildIndex(root);
				return report;
			} catch (Throwable t){
				report.ok = false;
				report.problem = String.valueOf(t);
				try { rebuildIndex(root); } catch (Throwable ignored){}
				return report;
			} finally {
				if (tmpDir != null) deleteTree(tmpDir);
			}
		}
	}

	private static void handle( File root, File tmpDir, ZipInputStream zip, ArchiveManifest.Entry e, Report report ) throws IOException {
		File tmp = new File(tmpDir, "entry-" + report.imported + "-" + report.rejected + "-" + report.skippedAlreadyHere + ".tmp");
		MessageDigest digest = newDigest();
		long size = 0;
		boolean tooBig = false;
		OutputStream out = new FileOutputStream(tmp);
		try {
			byte[] buf = new byte[16 * 1024];
			int n;
			while ((n = zip.read(buf)) > 0){
				size += n;
				if (size > e.size + (64L << 20)) throw new IOException("a file in the archive is far larger than the manifest says");
				if (size > e.size || size > MAX_FILE_BYTES){
					tooBig = true;   //keep reading to move past it, but write no more
					continue;
				}
				digest.update(buf, 0, n);
				out.write(buf, 0, n);
			}
		} finally {
			out.close();
		}
		if (tooBig || size != e.size || !ArchiveManifest.hex(digest.digest()).equalsIgnoreCase(e.sha256)){
			tmp.delete();
			report.rejected++;
			report.details.add(e.path + ": does not match the manifest (size or checksum), not imported.");
			return;
		}
		//it must be a readable APD log, of the run the manifest says, and never demo data
		ReadResult r;
		try {
			r = RunLogReader.read(tmp);
		} catch (IOException unreadable){
			r = null;
		}
		if (r == null || r.header == null || r.status == ReadStatus.BAD_HEADER || r.status == ReadStatus.UNSUPPORTED_CONTAINER){
			tmp.delete();
			report.rejected++;
			report.details.add(e.path + ": not a readable APD log, not imported.");
			return;
		}
		String startedRun = null;
		for (com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent ev : r.events){
			if (ev.type == com.shatteredpixel.shatteredpixeldungeon.apd.stats.EventTypes.RUN_START){
				try {
					startedRun = com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents.decodeRunStart(ev, r).runId;
				} catch (RuntimeException ignored){
				}
				break;
			}
		}
		boolean runMatches = !e.kind.equals(ArchiveManifest.KIND_RUN)
				|| (startedRun != null && startedRun.length() > 0 ? startedRun.equals(e.runId) : r.header.runId.equals(e.runId));
		if (!StatsStore.isValidRunId(r.header.runId) || r.header.runId.startsWith(DatasetStore.DEMO_LOG_PREFIX)
				|| (startedRun != null && startedRun.startsWith(DatasetStore.DEMO_LOG_PREFIX))
				|| r.header.writerBuild.contains("SYNTHETIC-DEMO") || !runMatches){
			tmp.delete();
			report.rejected++;
			report.details.add(e.path + ": its header does not match the manifest or it is demo data, not imported.");
			return;
		}
		File target = new File(root, e.path);
		File folder = target.getParentFile();
		if (!folder.isDirectory() && !folder.mkdirs()){
			tmp.delete();
			report.rejected++;
			report.details.add(e.path + ": cannot create its folder.");
			return;
		}
		//claim the name atomically: an existing log is never replaced
		if (!target.createNewFile()){
			if (sameContent(target, e.sha256)) report.skippedAlreadyHere++;
			else {
				report.keptDifferentExisting++;
				report.details.add(e.path + ": a different log with this name is already here, yours was kept.");
			}
			tmp.delete();
			return;
		}
		if (!tmp.renameTo(target)){
			//the claim is an empty file we created ourselves: replace it by copying
			copy(tmp, target);
			tmp.delete();
		}
		report.imported++;
		if (e.redacted) report.redactedImported++;
		if (r.newerThanReader()) report.newerThanThisBuild++;
	}

	private static boolean sameContent( File f, String sha256 ){
		try {
			MessageDigest d = newDigest();
			InputStream in = new FileInputStream(f);
			try {
				byte[] buf = new byte[16 * 1024];
				int n;
				while ((n = in.read(buf)) > 0) d.update(buf, 0, n);
			} finally {
				in.close();
			}
			return ArchiveManifest.hex(d.digest()).equalsIgnoreCase(sha256);
		} catch (IOException e){
			return false;
		}
	}

	private static void copy( File from, File to ) throws IOException {
		InputStream in = new FileInputStream(from);
		try {
			OutputStream out = new FileOutputStream(to);
			try {
				byte[] buf = new byte[16 * 1024];
				int n;
				while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	private static void rebuildIndex( File root ){
		try {
			if (root != null && root.isDirectory()){
				com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunIndex idx = new StatsStore(root).index();
				idx.rebuild();
				idx.save();
			}
		} catch (Throwable ignored){
			//the index is derived and rebuilds itself when it is next read
		}
	}

	private static byte[] readLimited( InputStream in, int max ) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = in.read(buf)) > 0){
			if (out.size() + n > max) return null;
			out.write(buf, 0, n);
		}
		return out.toByteArray();
	}

	private static MessageDigest newDigest(){
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (java.security.NoSuchAlgorithmException e){
			throw new IllegalStateException(e);
		}
	}

	private static String safe( String name ){
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < name.length() && i < 80; i++){
			char c = name.charAt(i);
			b.append(c >= 32 && c < 127 ? c : '?');
		}
		return b.toString();
	}

	private static void deleteTree( File f ){
		File[] kids = f.listFiles();
		if (kids != null) for (File k : kids) deleteTree(k);
		f.delete();
	}
}
