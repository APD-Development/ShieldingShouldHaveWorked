package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

//What the simulator needs from the device: a phone notification for new bugs, and a way to leave.
public interface SimPlatform {
	void notifyBug(int id, String title, String text);
	void close();
}
