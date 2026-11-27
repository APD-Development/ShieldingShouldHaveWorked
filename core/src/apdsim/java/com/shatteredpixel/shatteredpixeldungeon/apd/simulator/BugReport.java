package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//One bug the simulator found, with everything needed to replay it: seed, class, bot seed, floor, turn and the last turns.
public final class BugReport {

	public enum Kind {
		CRASH("Crash"),
		EXCEPTION("Exception"),
		STUCK("Stuck"),
		TEXT("Broken text"),
		STATE("Impossible state"),
		RECORDER("Recorder disagrees with the game"),
		SLOW("Slow turn or frame"),
		BOT("Simulator problem (not a game bug)");

		public final String title;
		Kind(String title){ this.title = title; }
	}

	public String id;
	public Kind kind;
	public String signature;
	public String summary;
	public String details = "";
	public String seedCode = "";
	public long seed;
	public long botSeed;
	public String heroClass = "";
	public String aggression = "", intelligence = "";
	public int difficulty, challenges;
	public String customRules = "", apdSettings = "", profileText = "";
	public int depth;
	public int branch;
	public float turn;
	public int heroLevel;
	public int run;
	public String version = "";
	public long time;
	public int repeats = 1;
	public List<String> lastTurns = new ArrayList<>();
	public List<String> lastMessages = new ArrayList<>();

	public String headline(){
		return kind.title + ": " + summary;
	}

	public String text(){
		StringBuilder b = new StringBuilder();
		b.append("APD SIMULATOR BUG REPORT\n");
		b.append("Kind: ").append(kind.title).append('\n');
		b.append("What: ").append(summary).append('\n');
		b.append("Found: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date(time)));
		if (repeats > 1) b.append(" (seen ").append(repeats).append(" times)");
		b.append('\n');
		b.append("Seed: ").append(seedCode).append(" (").append(seed).append(")\n");
		b.append("Class: ").append(heroClass).append('\n');
		b.append("Aggression: ").append(aggression).append('\n');
		b.append("Intelligence: ").append(intelligence).append('\n');
		b.append("Difficulty: ").append(difficulty).append(", challenges ").append(challenges).append('\n');
		b.append("Custom rules: ").append(customRules.isEmpty() ? "default" : customRules).append('\n');
		b.append("APD Settings: ").append(apdSettings).append('\n');
		b.append("Floor: ").append(depth).append(branch != 0 ? " (branch " + branch + ")" : "").append('\n');
		b.append("Turn: ").append(String.format(Locale.ROOT, "%.1f", turn)).append('\n');
		b.append("Hero level: ").append(heroLevel).append('\n');
		b.append("Simulator run: ").append(run).append(", bot seed ").append(botSeed).append('\n');
		b.append("Build: ").append(version).append('\n');
		b.append("Simulator settings: ").append(profileText.replace('\n', ' ')).append('\n');
		b.append("Report id: ").append(id).append('\n');
		b.append("Signature: ").append(signature).append('\n');
		b.append("\nDETAILS\n").append(details).append('\n');
		b.append("\nLAST ").append(lastTurns.size()).append(" TURNS (oldest first)\n");
		for (String t : lastTurns) b.append(t).append('\n');
		b.append("\nLAST ").append(lastMessages.size()).append(" GAME MESSAGES\n");
		for (String m : lastMessages) b.append(m).append('\n');
		b.append("\nREPLAY: Simulator > Reports > this report > Replay starts the same seed, class and bot seed.\n");
		return b.toString();
	}

	//The first lines of text() are enough to list reports without keeping every report in memory.
	public static String[] header(String text){
		String kind = "", what = "", found = "", cls = "", floor = "";
		for (String line : text.split("\n", 20)){
			if (line.startsWith("Kind: ")) kind = line.substring(6);
			else if (line.startsWith("What: ")) what = line.substring(6);
			else if (line.startsWith("Found: ")) found = line.substring(7);
			else if (line.startsWith("Class: ")) cls = line.substring(7);
			else if (line.startsWith("Floor: ")) floor = line.substring(7);
		}
		return new String[]{kind, what, found, cls, floor};
	}

	public static String field(String text, String name){
		for (String line : text.split("\n")){
			if (line.startsWith(name + ": ")) return line.substring(name.length() + 2);
			if (line.equals("DETAILS")) break;
		}
		return "";
	}
}
