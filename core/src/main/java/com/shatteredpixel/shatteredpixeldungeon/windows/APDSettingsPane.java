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
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.SelfTest;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Callback;
import com.watabou.utils.FileUtils;

//APD: the APD settings, shown as the "APD" tab of Options (WndSettings) and in the in-game menu's "APD Settings" window.
//Both use the same pane, so the controls are the same wherever you open them.
public class APDSettingsPane extends Component {

	private static final int GAP = 4;
	//the slider moves in steps of 10%, the stored setting is the percent itself (0-80)
	private static final int STEP = 10;

	private RenderedTextBlock title;
	private OptionSlider optExotic;
	private RenderedTextBlock info;
	private CheckBox chkRecord;
	private RenderedTextBlock status;
	private RedButton btnTest;
	private RedButton btnImport;

	@Override
	protected void createChildren() {
		title = PixelScene.renderTextBlock( "APD Settings", 9 );
		title.hardlight( Window.TITLE_COLOR );
		add( title );

		optExotic = new OptionSlider( "Exotic potion/scroll chance",
				"0%", SPDSettings.APD_EXOTIC_CHANCE_MAX + "%",
				0, SPDSettings.APD_EXOTIC_CHANCE_MAX / STEP ) {
			@Override
			protected void onChange() {
				SPDSettings.apdExoticChance( getSelectedValue() * STEP );
			}
		};
		optExotic.setSelectedValue( SPDSettings.apdExoticChance() / STEP );
		add( optExotic );

		info = PixelScene.renderTextBlock(
				"Minimum chance that a generated potion or scroll is exotic. 0% is the normal game.", 5 );
		add( info );

		chkRecord = new CheckBox( "Record run history" ) {
			@Override
			protected void onClick() {
				super.onClick();
				SPDSettings.apdRecording( checked() );
				ShatteredPixelDungeon.scene().addToFront( new WndMessage( "Takes effect the next time the game starts." ) );
			}
		};
		chkRecord.checked( SPDSettings.apdRecording() );
		add( chkRecord );

		status = PixelScene.renderTextBlock( ApdRecorder.status().summary(), 5 );
		add( status );

		btnTest = new RedButton( "Run self-test" ) {
			@Override
			protected void onClick() {
				runSelfTest( this );
			}
		};
		add( btnTest );

		btnImport = new RedButton( "Import SPD records" ) {
			@Override
			protected void onClick() {
				runImport( this );
			}
		};
		add( btnImport );
	}

	@Override
	protected void layout() {
		title.setPos( (width - title.width())/2f, y );
		float pos = title.bottom() + GAP;
		optExotic.setRect( x, pos, width, 24 );
		pos = optExotic.bottom() + GAP;
		info.maxWidth( (int)width );
		info.setPos( x, pos );
		pos = info.bottom() + GAP;
		chkRecord.setRect( x, pos, width, 16 );
		pos = chkRecord.bottom() + GAP;
		status.maxWidth( (int)width );
		status.setPos( x, pos );
		pos = status.bottom() + GAP;
		btnTest.setRect( x, pos, width, 16 );
		pos = btnTest.bottom() + GAP;
		btnImport.setRect( x, pos, width, 16 );
		height = btnImport.bottom() - y;
	}

	private static void runImport( final RedButton button ) {
		button.text( "Importing..." );
		button.enable( false );
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
				String result;
				try {
					result = ApdRecorder.importLegacyRecords() + "\n\n" + ApdRecorder.historyOverview();
				} catch (Throwable e) {
					result = "Import failed: " + e;
				}
				final String shown = result;
				Game.runOnRenderThread( new Callback() {
					@Override
					public void call() {
						button.text( "Import SPD records" );
						button.enable( true );
						ShatteredPixelDungeon.scene().addToFront( new WndMessage( shown ) );
					}
				} );
			}
		} );
		t.start();
	}

	private static void runSelfTest( final RedButton button ) {
		button.text( "Running..." );
		button.enable( false );
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
						button.text( "Run self-test" );
						button.enable( true );
						ShatteredPixelDungeon.scene().addToFront( new WndMessage( shown ) );
					}
				} );
			}
		} );
		t.start();
	}
}
