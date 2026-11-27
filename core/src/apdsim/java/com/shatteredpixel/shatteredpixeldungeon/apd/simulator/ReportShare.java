package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//Zips one report (seed, settings, last 50 turns, error) so it can be shared to any app the phone offers.
final class ReportShare {
	private ReportShare(){}

	static File zip(FileHandle report, String text){
		try {
			FileHandle dir = FileUtils.getFileHandle("simshare/");
			dir.mkdirs();
			for (FileHandle old : dir.list()) old.delete();
			File out = dir.child(report.nameWithoutExtension() + ".zip").file();
			try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(out))){
				zip.putNextEntry(new ZipEntry("apd-sim-report-" + report.nameWithoutExtension() + ".txt"));
				zip.write(text.getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
			return out;
		} catch (Exception e){
			return null;
		}
	}
}
