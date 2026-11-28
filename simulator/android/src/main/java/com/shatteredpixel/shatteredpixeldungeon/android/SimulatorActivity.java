package com.shatteredpixel.shatteredpixeldungeon.android;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulator.SimGame;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulator.SimPlatform;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulator.SimRuntime;
import com.watabou.noosa.Game;

import java.io.File;

//APD Simulator's activity: the normal APD Android setup (this app has its own private storage and settings, so nothing here
//can reach the real game's files), with the simulator's game in place of the plain game.
public class SimulatorActivity extends AndroidLauncher {

	static final String CHANNEL_BUGS = "apdsim_bugs";
	static final String CHANNEL_RUNNING = "apdsim_running";

	@Override
	protected void onCreate(Bundle savedInstanceState){
		//the simulator plays the Developer build's version of the game
		ApdFeatures.parse("testBuild\ngrudge\ndemoUnlock\nclericPlayable\n");
		//which of the two apps this is comes from its package name (same code in both)
		SimRuntime.app = getPackageName().endsWith(".headless") ? "headless" : "visual";
		SimRuntime.install(new Platform(getApplicationContext()));
		//automated check on an emulator (CI) can start playing straight away
		if (getIntent() != null && getIntent().getBooleanExtra("apdsim.autostart", false)){
			SimRuntime.autostart = true;
			SimRuntime.forcedSpeed = getIntent().getIntExtra("apdsim.speed", 3);
		}
		SimRuntime.history(new AndroidHistoryLink(getApplicationContext()));
		createChannels();
		if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
			requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
		}
		super.onCreate(savedInstanceState);
		//never sign in, never ask Play anything
		com.shatteredpixel.shatteredpixeldungeon.apd.community.CommunityController.google = null;
		com.shatteredpixel.shatteredpixeldungeon.apd.age.CommunityAgeGate.play = null;
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	//"Send this run to the simulator" from the Developer build: seed, class and rules
	private void handleIntent(Intent intent){
		if (intent == null || !"com.charltonchris29.adhdpixeldungeon.simulator.PLAY_SEED".equals(intent.getAction())) return;
		//from the Headless app: an exact run to watch (class, seed, bot seed, simulator settings)
		if (intent.hasExtra("botSeed")){
			SimRuntime.handOver(intent.getStringExtra("heroClass"), intent.getLongExtra("seedNumber", 0), intent.getLongExtra("botSeed", 0),
					intent.getStringExtra("profile"));
			return;
		}
		SimRuntime.requestedRun(intent.getStringExtra("seed"), intent.getStringExtra("heroClass"),
				intent.getIntExtra("difficulty", 0), intent.getIntExtra("challenges", 0), intent.getStringExtra("customRules"),
				intent.getBooleanExtra("tryDeath", false));
	}

	//the plain game is created by the shared setup; the simulator's game takes its place
	@Override
	public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config){
		super.initialize(new SimGame(Game.platform), config);
	}

	//---- running with the screen away -----------------------------------------------------------------------------
	//Android stops drawing when the app leaves the screen, and the game only moves when a frame is drawn. While a
	//headless run is playing, a thread here borrows the game's graphics context (free while Android is not drawing) and
	//keeps running the same game frames without drawing them. When the app comes back the thread hands everything
	//back before Android draws again. A foreground service keeps the app and the processor awake meanwhile.
	//If the phone will not lend the context, the simulator simply waits, as it did before, and the notification says so.

	private android.opengl.EGLDisplay eglDisplay;
	private android.opengl.EGLContext eglContext;
	private volatile boolean backgroundOn;
	private Thread backgroundThread;
	//for the automated check: how the last attempt went ("" = never tried)
	static volatile String backgroundResult = "";

	@Override
	protected void onResume(){
		stopBackground();
		super.onResume();
		//on the drawing thread: note the graphics context (it can be a new one after a long time away) and take the
		//run's random numbers back
		com.badlogic.gdx.Gdx.app.postRunnable(() -> {
			eglDisplay = android.opengl.EGL14.eglGetCurrentDisplay();
			eglContext = android.opengl.EGL14.eglGetCurrentContext();
			SimGame.adoptThread();
		});
	}

	@Override
	protected void onPause(){
		boolean carryOn = SimGame.wantsBackground() && eglContext != null
				&& eglContext != android.opengl.EGL14.EGL_NO_CONTEXT && !isFinishing();
		//the service must be started while the app is still on the screen
		if (carryOn){
			try { SimulatorService.start(getApplicationContext()); }
			catch (RuntimeException notAllowed){ carryOn = false; backgroundResult = "service refused: " + notAllowed; }
		}
		super.onPause();
		if (carryOn) startBackground();
	}

	@Override
	protected void onDestroy(){
		stopBackground();
		super.onDestroy();
	}

	private void startBackground(){
		if (backgroundThread != null) return;
		backgroundOn = true;
		final android.opengl.EGLDisplay display = eglDisplay;
		final android.opengl.EGLContext context = eglContext;
		final Context app = getApplicationContext();
		backgroundThread = new Thread(() -> {
			android.opengl.EGLSurface surface = offscreenSurface(display, context);
			//Android lets go of the context a moment after the pause: try for a few seconds
			boolean mine = false;
			for (int tries = 0; tries < 60 && backgroundOn && !mine; tries++){
				mine = android.opengl.EGL14.eglMakeCurrent(display, surface, surface, context);
				if (!mine) try { Thread.sleep(50); } catch (InterruptedException e){ break; }
			}
			if (!mine){
				backgroundResult = "no context (error " + android.opengl.EGL14.eglGetError() + ")";
				android.util.Log.i("APDSIM", "APDSIM_BACKGROUND " + backgroundResult);
				SimulatorService.show(app, "Waiting: this phone cannot run the simulator with the screen away. Open the app to continue.");
				destroy(display, surface);
				return;
			}
			backgroundResult = "running";
			android.util.Log.i("APDSIM", "APDSIM_BACKGROUND started, surface " + (surface != android.opengl.EGL14.EGL_NO_SURFACE));
			try {
				SimGame.adoptThread();
				SimGame game = (SimGame) Game.instance;
				long shown = 0, frames = 0;
				while (backgroundOn){
					long before = System.nanoTime();
					game.backgroundFrame();
					frames++;
					long now = System.currentTimeMillis();
					if (now - shown > 5000){
						shown = now;
						backgroundResult = "running, " + frames + " batches";
						android.util.Log.i("APDSIM", "APDSIM_BACKGROUND " + backgroundResult + " | " + SimGame.backgroundStatus().replace('\n', ' '));
						SimulatorService.show(app, SimGame.backgroundStatus());
					}
					//nothing to play (paused by battery or heat, or the run list is done): do not spin
					if (!SimGame.wantsBackground()){
						try { Thread.sleep(250); } catch (InterruptedException e){ break; }
					} else if (System.nanoTime() - before < 2_000_000L){
						try { Thread.sleep(2); } catch (InterruptedException e){ break; }
					}
				}
			} finally {
				android.opengl.EGL14.eglMakeCurrent(display, android.opengl.EGL14.EGL_NO_SURFACE,
						android.opengl.EGL14.EGL_NO_SURFACE, android.opengl.EGL14.EGL_NO_CONTEXT);
				destroy(display, surface);
				android.util.Log.i("APDSIM", "APDSIM_BACKGROUND handed back");
			}
		}, "apdsim-background");
		backgroundThread.start();
	}

	//before Android draws again: stop the loop and wait until it has given the context back
	private void stopBackground(){
		Thread t = backgroundThread;
		backgroundThread = null;
		backgroundOn = false;
		if (t != null){
			t.interrupt();
			try { t.join(15000); } catch (InterruptedException ignored){}
		}
		SimulatorService.stop(getApplicationContext());
	}

	//a 1x1 surface nobody sees, of the same kind as the context; none at all if the phone will not make one (most
	//phones can run a context without a surface)
	private static android.opengl.EGLSurface offscreenSurface(android.opengl.EGLDisplay display, android.opengl.EGLContext context){
		try {
			int[] id = new int[1];
			if (!android.opengl.EGL14.eglQueryContext(display, context, android.opengl.EGL14.EGL_CONFIG_ID, id, 0))
				return android.opengl.EGL14.EGL_NO_SURFACE;
			android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
			int[] count = new int[1];
			if (!android.opengl.EGL14.eglChooseConfig(display, new int[]{android.opengl.EGL14.EGL_CONFIG_ID, id[0], android.opengl.EGL14.EGL_NONE}, 0,
					configs, 0, 1, count, 0) || count[0] < 1) return android.opengl.EGL14.EGL_NO_SURFACE;
			android.opengl.EGLSurface s = android.opengl.EGL14.eglCreatePbufferSurface(display, configs[0],
					new int[]{android.opengl.EGL14.EGL_WIDTH, 1, android.opengl.EGL14.EGL_HEIGHT, 1, android.opengl.EGL14.EGL_NONE}, 0);
			return s == null ? android.opengl.EGL14.EGL_NO_SURFACE : s;
		} catch (RuntimeException e){
			return android.opengl.EGL14.EGL_NO_SURFACE;
		}
	}

	private static void destroy(android.opengl.EGLDisplay display, android.opengl.EGLSurface surface){
		if (surface != null && surface != android.opengl.EGL14.EGL_NO_SURFACE) android.opengl.EGL14.eglDestroySurface(display, surface);
	}

	private void createChannels(){
		if (Build.VERSION.SDK_INT < 26) return;
		NotificationManager nm = getSystemService(NotificationManager.class);
		nm.createNotificationChannel(new NotificationChannel(CHANNEL_BUGS, "Bugs found", NotificationManager.IMPORTANCE_DEFAULT));
		nm.createNotificationChannel(new NotificationChannel(CHANNEL_RUNNING, "Simulator running", NotificationManager.IMPORTANCE_LOW));
	}

	static final class Platform implements SimPlatform {
		private final Context context;
		Platform(Context c){ context = c; }

		@Override public void notifyBug(int id, String title, String text){
			try {
				Intent open = new Intent(context, SimulatorActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent pi = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_IMMUTABLE);
				android.app.Notification.Builder b = Build.VERSION.SDK_INT >= 26
						? new android.app.Notification.Builder(context, CHANNEL_BUGS) : new android.app.Notification.Builder(context);
				b.setSmallIcon(android.R.drawable.stat_notify_error).setContentTitle(title).setContentText(text)
						.setStyle(new android.app.Notification.BigTextStyle().bigText(text)).setContentIntent(pi).setAutoCancel(true);
				context.getSystemService(NotificationManager.class).notify(id, b.build());
			} catch (RuntimeException ignored){
				//no permission or notifications turned off: the report is still kept in the app
			}
		}

		@Override public void share(File zip, String subject){
			if (zip == null) return;
			Uri uri = androidx.core.content.FileProvider.getUriForFile(context, context.getPackageName() + ".share", zip);
			Intent send = new Intent(Intent.ACTION_SEND).setType("application/zip").putExtra(Intent.EXTRA_STREAM, uri)
					.putExtra(Intent.EXTRA_SUBJECT, subject).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			context.startActivity(Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}

		@Override public int batteryPercent(){
			BatteryManager bm = context.getSystemService(BatteryManager.class);
			return bm == null ? -1 : bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
		}

		@Override public boolean charging(){
			Intent status = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			int plugged = status == null ? 0 : status.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			return plugged != 0;
		}

		@Override public boolean hot(){
			if (Build.VERSION.SDK_INT < 29) return false;
			PowerManager pm = context.getSystemService(PowerManager.class);
			return pm != null && pm.getCurrentThermalStatus() >= PowerManager.THERMAL_STATUS_SEVERE;
		}

		@Override public boolean openInVisual(String heroClass, long seed, long botSeed, String profileText){
			try {
				Intent open = new Intent("com.charltonchris29.adhdpixeldungeon.simulator.PLAY_SEED")
						.setClassName("com.charltonchris29.adhdpixeldungeon.simulator.visual", SimulatorActivity.class.getName())
						.putExtra("heroClass", heroClass).putExtra("seedNumber", seed).putExtra("botSeed", botSeed).putExtra("profile", profileText)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(open);
				return true;
			} catch (RuntimeException notInstalled){
				return false;
			}
		}

		@Override public void close(){
			if (AndroidLauncher.instance != null) AndroidLauncher.instance.finishAndRemoveTask();
		}
	}
}
