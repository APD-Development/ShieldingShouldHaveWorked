package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

//Keeps the Headless simulator alive while it is off the screen: a foreground service (with its notification) so Android
//does not stop the app, and a wake lock so the processor keeps working with the screen off. The game itself is run by
//SimulatorActivity's background loop; this class only holds the phone awake and shows what is going on.
public class SimulatorService extends Service {

	static final int NOTIFICATION = 7001;
	private PowerManager.WakeLock wakeLock;

	static void start(Context c){
		Intent i = new Intent(c, SimulatorService.class);
		if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i);
		else c.startService(i);
	}

	static void stop(Context c){
		c.stopService(new Intent(c, SimulatorService.class));
	}

	//new text in the notification (called every few seconds by the background loop)
	static void show(Context c, String text){
		try {
			c.getSystemService(NotificationManager.class).notify(NOTIFICATION, build(c, text));
		} catch (RuntimeException ignored){
			//notifications turned off: the simulator keeps running anyway
		}
	}

	private static Notification build(Context c, String text){
		Intent open = new Intent(c, SimulatorActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(c, 1, open, PendingIntent.FLAG_IMMUTABLE);
		Notification.Builder b = Build.VERSION.SDK_INT >= 26
				? new Notification.Builder(c, SimulatorActivity.CHANNEL_RUNNING) : new Notification.Builder(c);
		String first = text == null || text.isEmpty() ? "Running" : text.split("\n")[0];
		b.setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle("APD Sim Headless is running")
				.setContentText(first).setStyle(new Notification.BigTextStyle().bigText(text == null ? "" : text))
				.setContentIntent(pi).setOngoing(true).setOnlyAlertOnce(true);
		return b.build();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Notification n = build(this, "Running with the screen away");
		//34 = Android 14, where the kind of foreground service must be named (1 << 30 is "special use")
		if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIFICATION, n, 1 << 30);
		else startForeground(NOTIFICATION, n);
		if (wakeLock == null){
			PowerManager pm = getSystemService(PowerManager.class);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "apdsim:running");
			wakeLock.setReferenceCounted(false);
		}
		wakeLock.acquire();
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy(){
		if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
		super.onDestroy();
	}

	//the app was swiped away: nothing is left to keep alive
	@Override
	public void onTaskRemoved(Intent rootIntent){
		stopSelf();
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
}
