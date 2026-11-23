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
			wanted = true; announced = false; lastShownError = -1; downloadAsked = false;
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

	private boolean announced;
	private int lastShownError = -1;

	private static void say(final String text){
		com.watabou.noosa.Game.runOnRenderThread(() -> com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(text));
	}

	private static String explain(int error){
		switch (error){
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "microphone permission is off for APD (error 9).";
			case SpeechRecognizer.ERROR_AUDIO: return "the microphone couldn't be opened, another app may be using it (error 3).";
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "the phone's recognizer is busy (error 8).";
			case SpeechRecognizer.ERROR_CLIENT: return "recognizer client error (error 5).";
			case 12: return "the phone's on-device recognizer doesn't support this language (error 12).";
			case 13: return "the offline speech language pack isn't downloaded on this phone (error 13).";
			case SpeechRecognizer.ERROR_SERVER: case 11: return "the phone's recognizer service failed (error " + error + ").";
			case SpeechRecognizer.ERROR_NETWORK: case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "the recognizer wanted the network, which voice controls never use (error " + error + ").";
			default: return "recognizer error " + error + ".";
		}
	}

	/** Called by the launcher when the microphone permission answer comes back. */
	void onPermission(boolean granted){
		if (granted && wanted) start();
		else if (!granted){ wanted = false; say("Voice: microphone permission was denied."); }
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
					main.removeCallbacks(settle);
					ArrayList<String> heard = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
					if (!acted) VoiceCommands.heard(heard);
					again(30);
				}
				@Override public void onError(int error){
					// 6 = heard nothing, 7 = no words matched: normal while idle. Anything else is shown so it can be fixed.
					main.removeCallbacks(settle);
					if (error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT && error != SpeechRecognizer.ERROR_NO_MATCH
							&& error != SpeechRecognizer.ERROR_CLIENT && error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY && error != lastShownError){
						lastShownError = error;
						say("Voice: " + explain(error));
					}
					if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS){ wanted = false; return; }
					if (error == 13 && Build.VERSION.SDK_INT >= 33 && recognizer != null && !downloadAsked){
						// the phone has the recognizer but not the offline language pack: ask Android to fetch it
						downloadAsked = true;
						try {
							recognizer.triggerModelDownload(speechIntent());
							say("Voice: asked the phone to download the offline speech pack. Accept the download if it asks, then voice starts by itself.");
						} catch (RuntimeException e){ say("Voice: couldn't ask for the download (" + e.getClass().getSimpleName() + ")."); }
						lastShownError = -1;
						again(20000);
						return;
					}
					if (error == 13 && downloadAsked){ again(20000); return; }
					if (error == 12 || error == 13){ wanted = false; return; }
					again(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ? 300 : 40);
				}
				@Override public void onBeginningOfSpeech(){ acted = false; partial = null; }
				@Override public void onReadyForSpeech(Bundle params){
					acted = false; partial = null;
					if (!announced){ announced = true; say("Voice: listening."); }
				}
				@Override public void onRmsChanged(float rmsdB){}
				@Override public void onBufferReceived(byte[] buffer){}
				@Override public void onEndOfSpeech(){}
				@Override public void onPartialResults(Bundle partialResults){
					// Speed (Chris, Nov 23): don't wait for the recognizer's long end-of-speech pause.
					// Once the words stop changing for a moment, act on them and start listening again.
					ArrayList<String> heard = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
					if (acted || heard == null || heard.isEmpty() || heard.get(0).trim().isEmpty()) return;
					if (heard.get(0).equals(partial)) return;
					partial = heard.get(0);
					main.removeCallbacks(settle);
					main.postDelayed(settle, SETTLE_MS);
				}
				@Override public void onEvent(int eventType, Bundle params){}
			});
		}
		Intent intent = speechIntent();
		try { recognizer.startListening(intent); }
		catch (RuntimeException e){ say("Voice: couldn't start the recognizer (" + e.getClass().getSimpleName() + ")."); again(3000); }
	}

	private boolean downloadAsked;

	/** How long the words must stay unchanged before they count as a finished command. */
	private static final long SETTLE_MS = 400;
	private boolean acted;
	private String partial;
	private final Runnable settle = new Runnable(){
		@Override public void run(){
			if (!wanted || acted || partial == null || recognizer == null) return;
			acted = true;
			ArrayList<String> one = new ArrayList<>();
			one.add(partial);
			VoiceCommands.heard(one);
			try { recognizer.cancel(); } catch (RuntimeException ignored){}
			again(40);
		}
	};

	private Intent speechIntent(){
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
		intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 500L);
		intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 400L);
		intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault().toLanguageTag());
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
		return intent;
	}

	private void again(long delayMs){
		if (!wanted) return;
		main.postDelayed(this::listen, delayMs);
	}
}
