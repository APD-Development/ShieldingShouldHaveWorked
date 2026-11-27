package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//One line per simulated run in simlog.csv (a spreadsheet opens it): every death, win, stuck run and its numbers, in both modes.
//The export button zips this file with the run list and every bug report.
final class RunLog {
	private RunLog(){}

	static final String FILE = "simlog.csv";
	static final String HEADER = "run,time,mode,class,seed_code,seed,bot_seed,aggression,intelligence,difficulty,challenges,outcome,floor,deepest,hero_level,turn,bug_reports,crashes,real_ms,digest,build,checkpoint,replay,profile";

	private static String q(String s){
		return "\"" + (s == null ? "" : s.replace("\"", "'").replace("\n", " ").replace("\t", " ")) + "\"";
	}

	static void append(SimRuntime sim, String outcome, int floor, int heroLevel, long turn, int deepest, String digest, long realMs){
		try {
			FileHandle f = FileUtils.getFileHandle(FILE);
			StringBuilder b = new StringBuilder();
			if (!f.exists()) b.append(HEADER).append('\n');
			SimProfile p = sim.profileOfRun();
			b.append(sim.run).append(',').append(System.currentTimeMillis()).append(',').append(sim.headless ? "headless" : "visual").append(',')
					.append(sim.heroClass == null ? "" : sim.heroClass.name()).append(',').append(DungeonSeed.convertToCode(sim.seed)).append(',')
					.append(sim.seed).append(',').append(sim.botSeed).append(',').append(p.aggression.title).append(',').append(p.intelligence.title).append(',')
					.append(p.difficulty).append(',').append(p.challenges).append(',').append(q(outcome)).append(',').append(floor).append(',').append(deepest).append(',')
					.append(heroLevel).append(',').append(turn).append(',').append(sim.bugsThisRun).append(',').append(sim.crashesThisRun).append(',')
					.append(realMs).append(',').append(digest).append(',').append(q(Game.version)).append(',').append(sim.startedFrom.isEmpty() ? 0 : 1).append(',')
					.append(sim.replaying ? 1 : 0).append(',').append(q(p.encode().replace('\t', '|'))).append('\n');
			f.writeString(b.toString(), true, "UTF-8");
		} catch (Throwable ignored){
		}
	}

	//newest first; each row split into fields (quotes removed)
	static List<String[]> rows(int max){
		List<String[]> out = new ArrayList<>();
		try {
			FileHandle f = FileUtils.getFileHandle(FILE);
			if (!f.exists()) return out;
			String[] lines = f.readString("UTF-8").split("\n");
			for (int i = lines.length - 1; i >= 1 && out.size() < max; i--){
				String[] cells = lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				for (int c = 0; c < cells.length; c++) cells[c] = cells[c].replace("\"", "");
				if (cells.length >= 21) out.add(cells);
			}
		} catch (Throwable ignored){}
		return out;
	}

	//simlog.csv + the run list + every bug report, zipped for the share sheet
	static File exportZip(){
		try {
			FileHandle dir = FileUtils.getFileHandle("simshare/");
			dir.mkdirs();
			for (FileHandle old : dir.list()) old.delete();
			File out = dir.child("apd-simulator-log-" + System.currentTimeMillis() + ".zip").file();
			try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(out))){
				for (String name : new String[]{FILE, "simruns.txt"}){
					FileHandle f = FileUtils.getFileHandle(name);
					if (!f.exists()) continue;
					zip.putNextEntry(new ZipEntry(name));
					zip.write(f.readBytes());
					zip.closeEntry();
				}
				FileHandle reports = FileUtils.getFileHandle(ReportStore.DIR);
				if (reports.exists()) for (FileHandle r : reports.list(".txt")){
					zip.putNextEntry(new ZipEntry("reports/" + r.name()));
					zip.write(r.readString("UTF-8").getBytes(StandardCharsets.UTF_8));
					zip.closeEntry();
				}
			}
			return out;
		} catch (Throwable t){
			return null;
		}
	}
}
