package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulator.SimGame;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulator.SimPlatform;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulator.SimRuntime;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import com.watabou.utils.GameSettings;

import java.io.File;

//Developer tool: runs the APD simulator in a desktop window (used to test the bot on a computer or under a virtual display).
//Everything goes to its own folder (-Dapd.sim.dir, default ./apd-sim-desktop), never the desktop game's saves.
public class DesktopSimLauncher {

	public static void main(String[] args){
		File dir = new File(System.getProperty("apd.sim.dir", System.getProperty("user.home") + File.separator + "APD-Simulator")).getAbsoluteFile();
		dir.mkdirs();
		String base = dir.getPath() + File.separator;
		Game.version = System.getProperty("Specification-Version", "desktop-sim");
		try { Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "0")); } catch (NumberFormatException e){ Game.versionCode = 0; }
		ApdFeatures.parse("testBuild\ngrudge\ndemoUnlock\nclericPlayable\n");

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("APD Simulator");
		config.setPreferencesConfig(base, Files.FileType.Absolute);
		SPDSettings.set(new Lwjgl3Preferences(new Lwjgl3FileHandle(base + "simulator-settings.xml", Files.FileType.Absolute)));
		FileUtils.setDefaultFileProperties(Files.FileType.Absolute, base);
		config.setWindowedMode(Integer.getInteger("apd.sim.w", 540), Integer.getInteger("apd.sim.h", 960));
		config.setForegroundFPS(0);
		config.useVsync(false);

		final int runs = Integer.getInteger("apd.sim.runs", 0);
		SimRuntime.install(new SimPlatform(){
			@Override public void notifyBug(int id, String title, String text){ System.out.println("APDSIM_BUG " + title + " | " + text); }
			@Override public void share(File zip, String subject){ System.out.println("APDSIM_SHARE " + zip); }
			@Override public int batteryPercent(){ return -1; }
			@Override public boolean charging(){ return true; }
			@Override public boolean hot(){ return false; }
			@Override public void close(){ System.exit(0); }
		});
		if (System.getProperty("apd.sim.autostart") != null){
			SimRuntime.autostart = true;
			SimRuntime.exitAfterRuns = runs;
			SimRuntime.forcedSpeed = Integer.getInteger("apd.sim.speed", 3);
			SimRuntime.forcedAggression = System.getProperty("apd.sim.aggression");
			SimRuntime.forcedIntelligence = System.getProperty("apd.sim.intelligence");
			SimRuntime.forcedCheckpoint = Integer.getInteger("apd.sim.checkpoint", 0);
			SimRuntime.forcedReplay = System.getProperty("apd.sim.replay");
			SimRuntime.forcedVerify = Integer.getInteger("apd.sim.verify", -1);
			SimRuntime.forcedSeedBase = Long.getLong("apd.sim.seedbase", 0L);
			com.shatteredpixel.shatteredpixeldungeon.apd.simulator.TurnLog.echo = System.getProperty("apd.sim.trace") != null;
		}
		SimRuntime.realHistoryRoot(new File(System.getProperty("apd.sim.realHistory", defaultRealHistory())));
		new Lwjgl3Application(new SimGame(new DesktopPlatformSupport()), config);
	}

	//where the APD desktop game keeps its UIMSD (read only: the simulator never writes there)
	static String defaultRealHistory(){
		String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
		String home = System.getProperty("user.home");
		if (os.contains("win")) return home + "/AppData/Roaming/.charltonchris29/ADHD Pixel Dungeon/apd";
		if (os.contains("mac")) return home + "/Library/Application Support/ADHD Pixel Dungeon/apd";
		String xdg = System.getenv("XDG_DATA_HOME");
		if (xdg == null) xdg = home + "/.local/share";
		return xdg + "/.charltonchris29/adhd-pixel-dungeon/apd";
	}
}
