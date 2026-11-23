/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.shatteredpixel.shatteredpixeldungeon.apd.qol.VoiceCommands;

import java.util.ArrayList;

/** Chris's dev build only (Nov 23): keeps Android's speech recognizer listening and hands each phrase to VoiceCommands. */
final class AndroidVoiceInput implements VoiceCommands.Platform {
	static final int PERMISSION_REQUEST = 7341;
	private final Activity activity;
	private final Handler main = new Handler(Looper.getMainLooper());
	private SpeechRecognizer recognizer;
	private boolean wanted;

	AndroidVoiceInput(Activity activity){ this.activity = activity; }

	@Override public void start(){
		main.post(() -> {
			wanted = true;
			if (Build.VERSION.SDK_INT >= 23 && activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
				activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST);
				return;
			}
			if (!onDevice()){
				com.watabou.utils.SupportDiagnostics.event("voice.unavailable", "no on-device recognizer on this phone");
				com.watabou.noosa.Game.runOnRenderThread(() -> com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("Voice: this phone has no on-device speech recognizer, so voice controls can't run."));
				return;
			}
			listen();
		});
	}

	@Override public void stop(){
		main.post(() -> {
			wanted = false;
			main.removeCallbacksAndMessages(null);
			if (recognizer != null){ recognizer.cancel(); recognizer.destroy(); recognizer = null; }
		});
	}

	/** Chris, Nov 23: everything runs on the phone itself. Android 12+ on-device recognizer only, never the network. */
	private boolean onDevice(){
		return Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(activity);
	}

	/** Called by the launcher when the microphone permission answer comes back. */
	void onPermission(boolean granted){
		if (granted && wanted) start();
		else if (!granted){ wanted = false; com.watabou.utils.SupportDiagnostics.event("voice.permission", "denied"); }
	}

	/** Pause listening while the app is in the background; resume if it was on. */
	void onPause(){ boolean was = wanted; stop(); main.post(() -> wanted = was); }
	void onResume(){ if (wanted) start(); }

	private void listen(){
		if (!wanted) return;
		if (recognizer == null){
			recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(activity);
			recognizer.setRecognitionListener(new RecognitionListener() {
				@Override public void onResults(Bundle results){
					ArrayList<String> heard = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
					VoiceCommands.heard(heard);
					again(150);
				}
				@Override public void onError(int error){
					if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS){ wanted = false; return; }
					again(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ? 800 : 250);
				}
				@Override public void onReadyForSpeech(Bundle params){}
				@Override public void onBeginningOfSpeech(){}
				@Override public void onRmsChanged(float rmsdB){}
				@Override public void onBufferReceived(byte[] buffer){}
				@Override public void onEndOfSpeech(){}
				@Override public void onPartialResults(Bundle partialResults){}
				@Override public void onEvent(int eventType, Bundle params){}
			});
		}
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
		intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault().toLanguageTag());
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
		try { recognizer.startListening(intent); }
		catch (RuntimeException e){ again(1000); }
	}

	private void again(long delayMs){
		if (!wanted) return;
		main.postDelayed(this::listen, delayMs);
	}
}
