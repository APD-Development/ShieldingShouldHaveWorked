package com.shatteredpixel.shatteredpixeldungeon.apd;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import java.io.File;

//Starts libGDX without a window so game code that needs Gdx.files / Gdx.app preferences can run in tests.
//Everything the game writes goes to a throw-away temp folder, never into the repo.
public final class HeadlessTestSupport {

	private static boolean started = false;
	private static File tempDir;

	private HeadlessTestSupport(){}

	public static synchronized void start(){
		if (started) return;

		try {
			tempDir = java.nio.file.Files.createTempDirectory("apd-headless").toFile();
			tempDir.deleteOnExit();
		} catch (java.io.IOException e){
			throw new RuntimeException(e);
		}

		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		//negative: no render loop, we only need the services
		config.updatesPerSecond = -1;
		config.preferencesDirectory = new File(tempDir, "prefs").getAbsolutePath() + File.separator;
		HeadlessApplication app = new HeadlessApplication(new ApplicationAdapter(){}, config);
		Runtime.getRuntime().addShutdownHook(new Thread(app::exit));

		//game save files go to the temp folder as well
		FileUtils.setDefaultFileProperties(Files.FileType.Absolute, new File(tempDir, "save").getAbsolutePath() + File.separator);
		new File(tempDir, "save").mkdirs();

		//the version a real APD build reports
		Game.versionCode = 920;
		Game.version = "4.0.8-apd-INDEV";

		started = true;
	}

	public static File tempDir(){
		return tempDir;
	}

	public static boolean isReady(){
		return started && Gdx.app != null;
	}
}
