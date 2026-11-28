package com.shatteredpixel.shatteredpixeldungeon.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//"Several at once" for the PC programs: -Dapd.sim.workers=N starts N copies of the simulator, each in its own
//folder (worker-1, worker-2, ...) on its own seeds, waits for them, and merges their run lists and logs into the
//main folder with a summary. The game keeps its state in one place per program, so each copy is its own program.
final class SimWorkers {
	private SimWorkers(){}

	static int requested(){
		if (System.getProperty("apd.sim.worker") != null) return 1; //this is one of the copies
		return Math.max(1, Integer.getInteger("apd.sim.workers", 1));
	}

	static int run(File dir, int workers) throws IOException, InterruptedException {
		int runs = Integer.getInteger("apd.sim.runs", 10);
		int each = (runs + workers - 1) / workers;
		long seedBase = Long.getLong("apd.sim.seedbase", 0L);
		String java = ProcessHandle.current().info().command().orElse("java");
		long stallMillis = Math.max(20, Long.getLong("apd.sim.stallsec", 180)) * 1000L;
		Process[] procs = new Process[workers];
		int[] restarts = new int[workers];
		List<File> folders = new ArrayList<>();
		long begun = System.currentTimeMillis();
		for (int k = 1; k <= workers; k++){
			File folder = new File(dir, "worker-" + k);
			folder.mkdirs();
			folders.add(folder);
			procs[k - 1] = start(java, folder, k, each, seedBase);
		}
		System.out.println("APD Sim: " + workers + " at once, " + each + " runs each, in " + dir);
		int worst = 0, frozen = 0;
		//Wait for the copies. A copy that writes nothing for a few minutes has frozen (the whole program, so it cannot
		//report it itself): it is stopped, the run it was on is noted in simfrozen.txt with its seed, and the copy is
		//started again. The run counter is kept in the copy's folder, so it carries on with the run after the frozen one.
		while (true){
			boolean alive = false;
			for (int i = 0; i < workers; i++){
				Process p = procs[i];
				if (p == null) continue;
				File folder = folders.get(i);
				if (!p.isAlive()){
					worst = Math.max(worst, p.exitValue());
					procs[i] = null;
					continue;
				}
				alive = true;
				long last = Math.max(new File(folder, "output.log").lastModified(), new File(folder, "simruns.txt").lastModified());
				if (last == 0 || System.currentTimeMillis() - last < stallMillis) continue;
				p.destroyForcibly().waitFor();
				frozen++;
				int done = lines(new File(folder, "simruns.txt"));
				String seed = lastLine(new File(folder, "simseeds.txt"));
				try (PrintWriter out = new PrintWriter(new java.io.FileOutputStream(new File(dir, "simfrozen.txt"), true))){
					out.println(folder.getName() + " froze after " + done + " finished runs, on the run with seed number " + seed
							+ " (nothing written for " + stallMillis / 1000 + " s)");
				}
				System.out.println("APD Sim: " + folder.getName() + " froze on seed number " + seed + ", restarted. See simfrozen.txt.");
				int left = each - done - (++restarts[i]);
				procs[i] = left > 0 && restarts[i] <= 20 ? start(java, folder, i + 1, left, seedBase) : null;
			}
			if (!alive) break;
			Thread.sleep(2000);
		}
		if (frozen > 0) System.out.println("APD Sim: " + frozen + " run(s) froze and were skipped. See simfrozen.txt.");
		merge(dir, folders, "simruns.txt", false);
		merge(dir, folders, "simlog.csv", true);
		System.out.println(summary(new File(dir, "simruns.txt"), (System.currentTimeMillis() - begun) / 1000));
		return worst;
	}

	private static Process start(String java, File folder, int k, int runs, long seedBase) throws IOException {
		List<String> cmd = new ArrayList<>();
		cmd.add(java);
		for (Map.Entry<Object, Object> e : System.getProperties().entrySet()){
			String key = String.valueOf(e.getKey());
			if (!key.startsWith("apd.sim.") || key.equals("apd.sim.dir") || key.equals("apd.sim.workers")
					|| key.equals("apd.sim.runs") || key.equals("apd.sim.seedbase")) continue;
			cmd.add("-D" + key + "=" + e.getValue());
		}
		cmd.add("-Dapd.sim.worker=" + k);
		cmd.add("-Dapd.sim.dir=" + folder.getAbsolutePath());
		cmd.add("-Dapd.sim.runs=" + runs);
		cmd.add("-Dapd.sim.autostart=1");
		//with a seed base every copy plays fixed seeds of its own (repeatable); without one, random seeds
		if (seedBase > 0) cmd.add("-Dapd.sim.seedbase=" + (seedBase + (k - 1) * 1000L));
		cmd.add("-cp");
		cmd.add(System.getProperty("java.class.path"));
		cmd.add(DesktopSimLauncher.class.getName());
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(folder, "output.log")));
		return pb.start();
	}

	private static int lines(File f) throws IOException {
		if (!f.exists()) return 0;
		int n = 0;
		for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) if (!line.isEmpty()) n++;
		return n;
	}

	private static String lastLine(File f) throws IOException {
		if (!f.exists()) return "unknown";
		String last = "unknown";
		for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) if (!line.isEmpty()) last = line.trim();
		return last;
	}

	//one file from all the copies; for the log, the column names once
	private static void merge(File dir, List<File> folders, String name, boolean header) throws IOException {
		try (PrintWriter out = new PrintWriter(new File(dir, name), "UTF-8")){
			boolean first = true;
			for (File folder : folders){
				File f = new File(folder, name);
				if (!f.exists()) continue;
				try (BufferedReader in = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)){
					String line;
					boolean top = true;
					while ((line = in.readLine()) != null){
						if (header && top && !first){ top = false; continue; }
						top = false;
						if (!line.isEmpty()) out.println(header ? line : folder.getName() + " " + line);
					}
				}
				first = false;
			}
		}
	}

	private static final Pattern DEEPEST = Pattern.compile("deepest (\\d+)");
	private static final Pattern ENDED = Pattern.compile(": (died: [^,]*?|stuck[^,]*|won[^,]*|TEST [^,]*?|ended[^,]*?) on floor");

	static String summary(File runList, long seconds) throws IOException {
		if (!runList.exists()) return "APD Sim: no runs were recorded.";
		int runs = 0, floors = 0, deepest = 0, past5 = 0, past10 = 0, wins = 0;
		Map<String, Integer> ends = new TreeMap<>();
		for (String line : Files.readAllLines(runList.toPath(), StandardCharsets.UTF_8)){
			Matcher d = DEEPEST.matcher(line);
			if (!d.find()) continue;
			int depth = Integer.parseInt(d.group(1));
			runs++;
			floors += depth;
			deepest = Math.max(deepest, depth);
			if (depth >= 6) past5++;
			if (depth >= 11) past10++;
			Matcher e = ENDED.matcher(line);
			if (e.find()){
				String how = e.group(1).replace("died: ", "").replace("Slain by: ", "");
				if (how.startsWith("won")) wins++;
				ends.merge(how, 1, Integer::sum);
			}
		}
		List<Map.Entry<String, Integer>> top = new ArrayList<>(ends.entrySet());
		top.sort((a, b) -> b.getValue() - a.getValue());
		StringBuilder b = new StringBuilder(String.format(Locale.ROOT,
				"APD Sim: %d runs in %d s. Average deepest floor %.2f, deepest %d, past floor 5: %d, past floor 10: %d, wins: %d.\nHow they ended:",
				runs, seconds, runs == 0 ? 0f : floors / (float) runs, deepest, past5, past10, wins));
		for (int i = 0; i < Math.min(10, top.size()); i++) b.append(' ').append(top.get(i).getKey()).append(' ').append(top.get(i).getValue()).append(i < Math.min(10, top.size()) - 1 ? "," : "");
		return b.toString();
	}
}
