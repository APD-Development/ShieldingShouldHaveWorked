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
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.shatteredpixel.shatteredpixeldungeon.apd.qol.VoiceBrain;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.VoiceCommands;

import org.json.JSONArray;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Fast voice engine (Chris, Nov 23: "needs to be faster", "Full go"). Dev build only: this file and the
 * Vosk library live in the debug source set, so Play builds contain neither.
 *
 * Vosk (Apache 2.0) runs on the phone, listens without stopping and is limited to the game's own words,
 * so a command is understood a fraction of a second after it is said. If the bundled speech model is
 * missing or fails to load, the Android recognizer is used instead.
 */
final class VoskVoiceInput implements VoiceLifecycle {
	private static final String ASSET_DIR = "vosk-model";
	private static final String MODEL_VERSION = "small-en-us-0.15-a";
	/** Words must stay unchanged this long before they count as a finished command. */
	private static final long SETTLE_MS = 280;
	private static final long VOCAB_MS = 1500;

	private final Activity activity;
	private final VoiceLifecycle fallback;
	private final Handler main = new Handler(Looper.getMainLooper());

	private Model model;
	private Recognizer recognizer;
	private SpeechService service;
	private boolean wanted, loading, usingFallback, paused;
	private String grammarWords = "";
	private String partial;

	VoskVoiceInput(Activity activity, VoiceLifecycle fallback){
		this.activity = activity;
		this.fallback = fallback;
	}

	private static void say(final String text){
		com.watabou.noosa.Game.runOnRenderThread(() -> com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(text));
	}

	@Override public void start(){
		main.post(() -> {
			wanted = true;
			if (usingFallback){ fallback.start(); return; }
			if (Build.VERSION.SDK_INT >= 23 && activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
				activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST);
				return;
			}
			if (model != null){ begin(); return; }
			if (loading) return;
			loading = true;
			say("Voice: loading the speech model...");
			new Thread(() -> {
				Model loaded = null; String problem = null;
				try { loaded = new Model(unpack().getAbsolutePath()); }
				catch (Throwable t){ problem = t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage()); }
				final Model m = loaded; final String why = problem;
				main.post(() -> {
					loading = false;
					if (m == null){
						usingFallback = true;
						say("Voice: the fast engine couldn't load (" + why + "), using the phone's recognizer instead.");
						if (wanted) fallback.start();
						return;
					}
					model = m;
					if (wanted) begin();
				});
			}, "apd-voice-model").start();
		});
	}

	@Override public void stop(){
		main.post(() -> {
			wanted = false;
			if (usingFallback){ fallback.stop(); return; }
			end();
		});
	}

	@Override public void onPermission(boolean granted){
		if (usingFallback){ fallback.onPermission(granted); return; }
		if (granted && wanted) start();
		else if (!granted){ wanted = false; say("Voice: microphone permission was denied."); }
	}

	@Override public void onPause(){
		if (usingFallback){ fallback.onPause(); return; }
		main.post(() -> { paused = true; end(); });
	}

	@Override public void onResume(){
		if (usingFallback){ fallback.onResume(); return; }
		main.post(() -> { paused = false; if (wanted && model != null && service == null) begin(); });
	}

	/** Copies the bundled model out of the APK once (Vosk needs real files) and returns its folder. */
	private File unpack() throws Exception {
		File dir = new File(activity.getFilesDir(), ASSET_DIR);
		File marker = new File(dir, "apd-version.txt");
		if (marker.exists()){
			try (BufferedReader r = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(marker)))){
				if (MODEL_VERSION.equals(r.readLine())) return dir;
			}
		}
		AssetManager assets = activity.getAssets();
		ArrayList<String> files = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(assets.open(ASSET_DIR + "/files.txt")))){
			String line;
			while ((line = r.readLine()) != null) if (!line.trim().isEmpty()) files.add(line.trim());
		}
		if (files.isEmpty()) throw new java.io.FileNotFoundException("speech model not in this build");
		byte[] buffer = new byte[1 << 16];
		for (String name : files){
			File out = new File(dir, name);
			File parent = out.getParentFile();
			if (parent != null && !parent.isDirectory() && !parent.mkdirs()) throw new java.io.IOException("can't create " + parent);
			try (InputStream in = assets.open(ASSET_DIR + "/" + name); OutputStream o = new FileOutputStream(out)){
				int n;
				while ((n = in.read(buffer)) > 0) o.write(buffer, 0, n);
			}
		}
		try (OutputStream o = new FileOutputStream(marker)){ o.write(MODEL_VERSION.getBytes("UTF-8")); }
		return dir;
	}

	private static String grammarJson(String words){
		JSONArray a = new JSONArray();
		a.put(words);
		a.put("[unk]");
		return a.toString();
	}

	private void begin(){
		if (!wanted || paused || service != null || model == null) return;
		try {
			VoiceBrain.refreshVocabulary();
			grammarWords = VoiceBrain.vocabulary;
			recognizer = new Recognizer(model, 16000.0f, grammarJson(grammarWords));
			service = new SpeechService(recognizer, 16000.0f);
			service.startListening(listener);
			partial = null;
			say("Voice: listening (fast engine).");
			main.removeCallbacks(vocab);
			main.postDelayed(vocab, VOCAB_MS);
		} catch (Throwable t){
			end();
			usingFallback = true;
			say("Voice: the fast engine couldn't start (" + t.getClass().getSimpleName() + "), using the phone's recognizer instead.");
			fallback.start();
		}
	}

	private void end(){
		main.removeCallbacks(vocab);
		main.removeCallbacks(settle);
		if (service != null){
			try { service.stop(); service.shutdown(); } catch (Throwable ignored){}
			service = null;
		}
		if (recognizer != null){
			try { recognizer.close(); } catch (Throwable ignored){}
			recognizer = null;
		}
	}

	/** Keeps the recognizer's word list in step with the game: item and enemy names, button labels. */
	private final Runnable vocab = new Runnable(){
		@Override public void run(){
			if (service == null) return;
			com.watabou.noosa.Game.runOnRenderThread(VoiceBrain::refreshVocabulary);
			String now = VoiceBrain.vocabulary;
			if (!now.equals(grammarWords) && partial == null && recognizer != null){
				try {
					service.stop();
					recognizer.setGrammar(grammarJson(now));
					grammarWords = now;
					service.startListening(listener);
				} catch (Throwable t){
					say("Voice: couldn't update the word list (" + t.getClass().getSimpleName() + ").");
				}
			}
			main.postDelayed(this, VOCAB_MS);
		}
	};

	private static String words(String json, String key){
		try {
			String text = new JSONObject(json).optString(key, "");
			return text.replace("[unk]", " ").replaceAll("\\s+", " ").trim();
		} catch (Exception e){
			return "";
		}
	}

	private void act(String text){
		ArrayList<String> one = new ArrayList<>();
		one.add(text);
		VoiceCommands.heard(one);
	}

	private final Runnable settle = new Runnable(){
		@Override public void run(){
			if (service == null || partial == null) return;
			String text = partial;
			partial = null;
			service.reset();
			act(text);
		}
	};

	private final RecognitionListener listener = new RecognitionListener(){
		@Override public void onPartialResult(String hypothesis){
			String text = words(hypothesis, "partial");
			if (text.isEmpty() || text.equals(partial)) return;
			partial = text;
			main.removeCallbacks(settle);
			main.postDelayed(settle, SETTLE_MS);
		}
		@Override public void onResult(String hypothesis){
			// the engine's own end-of-phrase: only used if the quick path above hasn't already acted
			String text = words(hypothesis, "text");
			if (partial == null || text.isEmpty()) return;
			main.removeCallbacks(settle);
			partial = null;
			act(text);
		}
		@Override public void onFinalResult(String hypothesis){}
		@Override public void onError(Exception exception){
			say("Voice: microphone error (" + exception.getClass().getSimpleName() + ").");
			end();
			if (wanted) main.postDelayed(VoskVoiceInput.this::begin, 2000);
		}
		@Override public void onTimeout(){}
	};
}
