package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import java.io.File;
import java.util.List;

//Where the phone owner's REAL run history comes from, always read only: the Developer build's link on the phone,
//APD's save folder on the PC, or an imported UIMSD export ZIP. Implementations copy log files into the simulator's own
//cache and return those copies; they never write anything back.
public interface HistorySource {
	//a short human description: "Developer build link: 312 runs", "not connected", ...
	String status();
	//copies of the real run logs (*.apdlog), newest first; empty when nothing is reachable
	List<File> runLogs(int max);
}
