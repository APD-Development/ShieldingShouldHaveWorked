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
		SimRuntime.install(new Platform(getApplicationContext()));
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
		SimRuntime.requestedRun(intent.getStringExtra("seed"), intent.getStringExtra("heroClass"),
				intent.getIntExtra("difficulty", 0), intent.getIntExtra("challenges", 0), intent.getStringExtra("customRules"),
				intent.getBooleanExtra("tryDeath", false));
	}

	//the plain game is created by the shared setup; the simulator's game takes its place
	@Override
	public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config){
		super.initialize(new SimGame(Game.platform), config);
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

		@Override public void close(){
			if (AndroidLauncher.instance != null) AndroidLauncher.instance.finishAndRemoveTask();
		}
	}
}
