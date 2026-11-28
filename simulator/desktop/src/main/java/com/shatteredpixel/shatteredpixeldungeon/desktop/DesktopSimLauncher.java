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
		//two programs from the same code: APD-Sim-Headless and APD-Sim-Visual (their start scripts set apd.sim.app)
		SimRuntime.app = "headless".equals(System.getProperty("apd.sim.app")) ? "headless" : "visual";
		File dir = new File(System.getProperty("apd.sim.dir", System.getProperty("user.home") + File.separator
				+ (SimRuntime.headlessApp() ? "APD-Sim-Headless" : "APD-Sim-Visual"))).getAbsoluteFile();
		dir.mkdirs();
		//several at once: this program only starts the copies, waits and merges their logs
		int workers = SimWorkers.requested();
		if (workers > 1){
			try {
				System.exit(SimWorkers.run(dir, workers));
			} catch (Exception e){
				System.err.println("APD Sim: could not run several at once: " + e);
				System.exit(2);
			}
			return;
		}
		String base = dir.getPath() + File.separator;
		//from this program's jar; the system properties are only for running straight from the build tool
		Package pkg = DesktopSimLauncher.class.getPackage();
		String v = pkg == null ? null : pkg.getSpecificationVersion();
		String c = pkg == null ? null : pkg.getImplementationVersion();
		Game.version = v != null ? v : System.getProperty("Specification-Version", "desktop-sim");
		try { Game.versionCode = Integer.parseInt(c != null ? c : System.getProperty("Implementation-Version", "0")); } catch (NumberFormatException e){ Game.versionCode = 0; }
		ApdFeatures.parse("testBuild\ngrudge\ndemoUnlock\nclericPlayable\n");

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle(SimRuntime.appTitle());
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
			@Override public boolean openInVisual(String heroClass, long seed, long botSeed, String profileText){
				//starts the Visual program that sits next to this one, replaying the run at watching speed
				try {
					String home = System.getProperty("apd.sim.home");
					if (home == null) return false;
					boolean win = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
					File script = new File(new File(home, "bin"), win ? "APD-Sim-Visual.bat" : "APD-Sim-Visual");
					if (!script.exists()) return false;
					ProcessBuilder pb = new ProcessBuilder(script.getAbsolutePath());
					pb.environment().put("JAVA_OPTS", "-Dapd.sim.autostart=1 -Dapd.sim.speed=0 -Dapd.sim.runs=0 -Dapd.sim.replay=" + heroClass + ":" + seed + ":" + botSeed);
					pb.inheritIO().start();
					return true;
				} catch (Exception e){
					return false;
				}
			}
			@Override public void close(){ System.exit(0); }
		});
		if (System.getProperty("apd.sim.autostart") != null){
			SimRuntime.autostart = true;
			SimRuntime.exitAfterRuns = runs;
			SimRuntime.forcedSpeed = Integer.getInteger("apd.sim.speed", 3);
			SimRuntime.forcedAggression = System.getProperty("apd.sim.aggression");
			SimRuntime.forcedIntelligence = System.getProperty("apd.sim.intelligence");
			SimRuntime.forcedCheckpoint = Integer.getInteger("apd.sim.checkpoint", 0);
			SimRuntime.forcedBossTest = Boolean.getBoolean("apd.sim.bosstest");
			SimRuntime.forcedNoJudge = Boolean.getBoolean("apd.sim.nojudge");
			SimRuntime.cornerTrick = Boolean.getBoolean("apd.sim.corner");
			com.shatteredpixel.shatteredpixeldungeon.apd.simulator.SimGame.headlessBatchMillis = Math.max(50, Long.getLong("apd.sim.batchms", 200));
			SimRuntime.checkpointNameHas = System.getProperty("apd.sim.checkpointName");
			SimRuntime.forcedReplay = System.getProperty("apd.sim.replay");
			SimRuntime.forcedVerify = Integer.getInteger("apd.sim.verify", -1);
			SimRuntime.forcedSeedBase = Long.getLong("apd.sim.seedbase", 0L);
			SimRuntime.forcedHeadless = System.getProperty("apd.sim.headless") != null;
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
