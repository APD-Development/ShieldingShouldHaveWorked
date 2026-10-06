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
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.SelfTest;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.FileUtils;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

//APD: settings that only exist in ADHD Pixel Dungeon
public class WndAPDSettings extends Window {

	private static final int WIDTH = 120;
	private static final int GAP = 4;
	//the slider moves in steps of 10%, the stored setting is the percent itself (0-80)
	private static final int STEP = 10;

	public WndAPDSettings() {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock( "APD Settings", 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width())/2f, 0 );
		add( title );

		OptionSlider optExotic = new OptionSlider( "Exotic potion/scroll chance",
				"0%", SPDSettings.APD_EXOTIC_CHANCE_MAX + "%",
				0, SPDSettings.APD_EXOTIC_CHANCE_MAX / STEP ) {
			@Override
			protected void onChange() {
				SPDSettings.apdExoticChance( getSelectedValue() * STEP );
			}
		};
		optExotic.setSelectedValue( SPDSettings.apdExoticChance() / STEP );
		optExotic.setRect( 0, title.bottom() + GAP, WIDTH, 24 );
		add( optExotic );

		RenderedTextBlock info = PixelScene.renderTextBlock(
				"Minimum chance that a generated potion or scroll is exotic. 0% is the normal game. Exotic Crystals can still raise it higher.", 5 );
		info.maxWidth( WIDTH );
		info.setPos( 0, optExotic.bottom() + GAP );
		add( info );

		//recording and diagnostics
		CheckBox chkRecord = new CheckBox( "Record run history" ) {
			@Override
			protected void onClick() {
				super.onClick();
				SPDSettings.apdRecording( checked() );
				ShatteredPixelDungeon.scene().addToFront( new WndMessage( "Takes effect the next time the game starts." ) );
			}
		};
		chkRecord.checked( SPDSettings.apdRecording() );
		chkRecord.setRect( 0, info.bottom() + GAP, WIDTH, 16 );
		add( chkRecord );

		RenderedTextBlock status = PixelScene.renderTextBlock( ApdRecorder.status().summary(), 5 );
		status.maxWidth( WIDTH );
		status.setPos( 0, chkRecord.bottom() + GAP );
		add( status );

		RedButton btnTest = new RedButton( "Run self-test" ) {
			@Override
			protected void onClick() {
				text( "Running..." );
				enable( false );
				final RedButton self = this;
				Thread t = new Thread( new Runnable() {
					@Override
					public void run() {
						String result;
						try {
							java.io.File base = ApdRecorder.historyRoot();
							if (base == null) base = FileUtils.getFileHandle( "apd" ).file();
							java.io.File work = new java.io.File( base, "selftest-work" );
							work.mkdirs();
							result = SelfTest.run( work ).text();
							work.delete();
						} catch (Throwable e) {
							result = "SELF-TEST FAILED\n" + e;
						}
						final String shown = result;
						Game.runOnRenderThread( new Callback() {
							@Override
							public void call() {
								self.text( "Run self-test" );
								self.enable( true );
								ShatteredPixelDungeon.scene().addToFront( new WndMessage( shown ) );
							}
						} );
					}
				} );
				t.start();
			}
		};
		btnTest.setRect( 0, status.bottom() + GAP, WIDTH, 16 );
		add( btnTest );

		resize( WIDTH, (int)btnTest.bottom() );
	}
}
