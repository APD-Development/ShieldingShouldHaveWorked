/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.UimsdUnlock;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.watabou.noosa.Game;

import java.io.File;

// The door to UIMSD. While the unlock gate is off (UimsdUnlock.ENFORCED) it simply opens UIMSD. When it is on and UIMSD is still closed, it
// says how many finished runs are needed and, once only, offers the demo route ("I LIKE CONFUSION") behind the warning below.
public final class UimsdGate {

	// the warning and the two choices, word for word as the owner wrote them
	static final String WARNING_TITLE = "ARE YOU SURE ABOUT THIS?";
	static final String WARNING_TEXT = "UIMSD is an absurdly detailed statistics database. You can unlock it immediately with 10 demo runs, but there's a very good chance you'll have absolutely no idea what you're looking at.\n\nI strongly recommend playing 10 actual runs first.";
	static final String CHOICE_CONFUSION = "I LIKE CONFUSION (generate demo runs)";
	static final String CHOICE_PLAY = "I'LL PLAY 10 RUNS (decline and return to normal gameplay)";

	// the two permanent flags, kept in the game's settings
	static final UimsdUnlock.Flags FLAGS = new UimsdUnlock.Flags(){
		@Override public boolean unlocked(){ return SPDSettings.apdUimsdUnlocked(); }
		@Override public void setUnlocked(){ SPDSettings.apdUimsdUnlocked(true); }
		@Override public boolean demoUsed(){ return SPDSettings.apdDemoUnlockUsed(); }
		@Override public void setDemoUsed(){ SPDSettings.apdDemoUnlockUsed(true); }
	};

	//Lets a device test switch the gate on without changing the shipped default. Nothing in the game calls it.
	private static volatile boolean forcedOn;

	public static void forceGateOnForTest( boolean on ){
		forcedOn = on;
	}

	private static boolean enforced(){
		return UimsdUnlock.ENFORCED || forcedOn;
	}

	private UimsdGate(){}

	public static void open( final Class<? extends PixelScene> from ){
		final File root = ApdRecorder.historyRoot();
		if (UimsdUnlock.isUnlocked(FLAGS, root, enforced())){
			UimsdScene.openNow(from);
			return;
		}
		int done = UimsdUnlock.genuineRuns(root, UimsdUnlock.TEN_RUNS);
		String message = "UIMSD, the statistics database, opens after you finish " + UimsdUnlock.TEN_RUNS + " runs. You have finished " + done + " so far.";
		if (UimsdUnlock.demoButtonAvailable(FLAGS, root, enforced())){
			ShatteredPixelDungeon.scene().addToFront(new WndOptions("UIMSD is locked", message, "Keep playing", "Unlock it now with demo runs...") {
				@Override
				protected void onSelect( int index ){
					if (index == 1) confirm(from, root);
				}
			});
		} else {
			ShatteredPixelDungeon.scene().addToFront(new WndMessage(message));
		}
	}

	private static void confirm( final Class<? extends PixelScene> from, final File root ){
		ShatteredPixelDungeon.scene().addToFront(new WndOptions(WARNING_TITLE, WARNING_TEXT, CHOICE_CONFUSION, CHOICE_PLAY) {
			@Override
			protected void onSelect( int index ){
				if (index == 0) generate(from, root);
			}
		});
	}

	//ten small made-up runs take a moment, so this is done off the game's thread
	private static void generate( final Class<? extends PixelScene> from, final File root ){
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				final UimsdUnlock.Generated result = UimsdUnlock.generateDemo(FLAGS, root, Ruleset.forNewRun(), System.currentTimeMillis(), enforced());
				Game.runOnRenderThread(new com.watabou.utils.Callback(){
					@Override
					public void call(){
						if (result == UimsdUnlock.Generated.DONE){
							DemoData.viewDemo(true);
							UimsdScene.openNow(from);
						} else {
							ShatteredPixelDungeon.scene().addToFront(new WndMessage("The demo runs could not be made, so UIMSD is still locked. Nothing was changed."));
						}
					}
				});
			}
		}, "apd-demo-unlock");
		t.setDaemon(true);
		t.start();
	}
}
