package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

//The last N turns the bot played (and the last N game messages), for reports.
public final class TurnLog {

	public static final int KEEP = 50;

	private final ArrayDeque<String> turns = new ArrayDeque<>();
	private final ArrayDeque<String> messages = new ArrayDeque<>();
	private long actions;

	//desktop testing: print every turn (full traces for determinism comparisons)
	public static boolean echo;

	public synchronized void turn(String line){
		actions++;
		if (echo) System.out.println("APDSIM_TRACE " + actions + ". " + line);
		turns.addLast(actions + ". " + line);
		while (turns.size() > KEEP) turns.removeFirst();
	}

	public synchronized void message(String line){
		messages.addLast(line);
		while (messages.size() > KEEP) messages.removeFirst();
	}

	public synchronized long actions(){
		return actions;
	}

	public synchronized List<String> turns(){
		return new ArrayList<>(turns);
	}

	public synchronized List<String> messages(){
		return new ArrayList<>(messages);
	}

	public synchronized void clear(){
		turns.clear();
		messages.clear();
		actions = 0;
	}
}
