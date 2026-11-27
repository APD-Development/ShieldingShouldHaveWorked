package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import java.io.File;

//What the simulator needs from the device.
public interface SimPlatform {
	//a phone notification for a NEW bug (the same bug never notifies twice)
	void notifyBug(int id, String title, String text);
	//hand a zipped report to the phone's share sheet
	void share(File zip, String subject);
	//battery percent 0-100, or -1 when unknown
	int batteryPercent();
	boolean charging();
	//true when the phone reports it is getting hot
	boolean hot();
	//Headless app: hand a run to the Visual app to watch (same class, seed, bot seed, settings). False when the Visual app is missing.
	boolean openInVisual(String heroClass, long seed, long botSeed, String profileText);
	void close();
}
