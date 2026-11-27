package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.FileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Reports live only on this device, in the simulator's own folder (simreports/). Nothing is uploaded.
public final class ReportStore {

	public static final String DIR = "simreports/";
	public static final int MAX_KEPT = 300;

	//signature -> report file name, so a bug seen again bumps a counter instead of making a new report
	private final Map<String, String> bySignature = new HashMap<>();
	private boolean loaded;

	private FileHandle dir(){
		FileHandle d = FileUtils.getFileHandle(DIR);
		d.mkdirs();
		return d;
	}

	private synchronized void load(){
		if (loaded) return;
		loaded = true;
		for (FileHandle f : files()){
			try {
				String sig = BugReport.field(f.readString("UTF-8"), "Signature");
				if (!sig.isEmpty()) bySignature.put(sig, f.name());
			} catch (Exception ignored){}
		}
	}

	public synchronized List<FileHandle> files(){
		FileHandle[] list = dir().list(".txt");
		List<FileHandle> out = new ArrayList<>(Arrays.asList(list));
		out.sort(Comparator.comparing(FileHandle::name).reversed());
		return out;
	}

	//true when this is a NEW bug (the caller notifies); false when it was seen before (its count goes up)
	public synchronized boolean add(BugReport r){
		load();
		String existing = bySignature.get(r.signature);
		if (existing != null){
			FileHandle f = dir().child(existing);
			if (f.exists()){
				try {
					String text = f.readString("UTF-8");
					int seen = 1;
					java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(seen (\\d+) times\\)").matcher(text);
					if (m.find()) seen = Integer.parseInt(m.group(1));
					String found = BugReport.field(text, "Found");
					String base = found.replaceAll(" \\(seen \\d+ times\\)", "");
					text = text.replace("Found: " + found, "Found: " + base + " (seen " + (seen + 1) + " times)");
					f.writeString(text, false, "UTF-8");
				} catch (Exception ignored){}
				return false;
			}
		}
		r.id = String.format(java.util.Locale.ROOT, "%013d-%s", r.time, Integer.toHexString(r.signature.hashCode()));
		dir().child(r.id + ".txt").writeString(r.text(), false, "UTF-8");
		bySignature.put(r.signature, r.id + ".txt");
		trim();
		return true;
	}

	private void trim(){
		List<FileHandle> all = files();
		for (int i = MAX_KEPT; i < all.size(); i++) all.get(i).delete();
	}

	public synchronized int count(){
		return files().size();
	}

	public synchronized void deleteAll(){
		for (FileHandle f : files()) f.delete();
		bySignature.clear();
	}
}
