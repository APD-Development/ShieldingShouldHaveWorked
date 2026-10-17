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
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoGenerator;
import com.shatteredpixel.shatteredpixeldungeon.apd.archive.StorageMeter;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.HistoryTransfer;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.SelfTest;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
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
	private CheckBox chkAdult;
    private CheckBox chkTileVisibility;
    private CheckBox chkStairNavigation;
    private CheckBox chkAutoEnterStairs;
	private RenderedTextBlock status;
	private RenderedTextBlock storage;
	private RedButton btnClericQuickCasts;
    private RedButton btnPlaybackSpeed;
    private RedButton btnHealingConfirmation;
    private RedButton btnCustomRules;
    private RedButton btnCurrentCustomRules;
	private RedButton btnTest;
	private RedButton btnExportHistory;
	private RedButton btnImportHistory;
	private RedButton btnImport;
	private RedButton btnUimsd;
	//About / Developer: the Cleric grudge's page (null while the feature is switched off)
	private RedButton btnGrudge;
	//INDEV builds only (null in other builds): synthetic demo data for testing UIMSD, see DemoData
	private RedButton btnDemoGenerate;
	private RedButton btnDemoRemove;
	private RedButton btnDemoView;

	@Override
	protected void createChildren() {
		title = PixelScene.renderTextBlock( "APD Settings", 9 );
		title.hardlight( Window.TITLE_COLOR );
		add( title );

		optExotic = new OptionSlider( "Healing to Shielding chance (new runs)",
				"0%", SPDSettings.APD_EXOTIC_CHANCE_MAX + "%",
				0, SPDSettings.APD_EXOTIC_CHANCE_MAX / STEP ) {
			@Override
			protected void onChange() {
				SPDSettings.apdExoticChance( getSelectedValue() * STEP );
			}
		};
		optExotic.setSelectedValue( SPDSettings.apdExoticSetting() / STEP );
		add( optExotic );

		info = PixelScene.renderTextBlock(
				"Minimum chance that a generated Potion of Healing becomes Shielding. Other potions and scrolls keep normal SPD behavior. 0% keeps normal behavior.", 5 );
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

		//Unknown age leaves the choice usable; verified underage players receive Clean.
		chkAdult = new CheckBox( "Adult humor (18+)" ) {
			@Override
			protected void onClick() {
				if (!com.shatteredpixel.shatteredpixeldungeon.apd.age.AgeEligibility.permitsAdult(
						com.shatteredpixel.shatteredpixeldungeon.apd.age.AgeEligibility.current())){
					checked(false);
					ShatteredPixelDungeon.scene().addToFront(new WndMessage(
							"Adult humor is unavailable for players verified as under 18."));
					return;
				}
				super.onClick();
				SPDSettings.apdAdultHumor( checked() );
			}
		};
		chkAdult.checked( SPDSettings.apdAdultHumor() );
		add( chkAdult );

        chkTileVisibility=new CheckBox("Enhanced Tile Visibility"){
            @Override protected void onClick(){super.onClick();SPDSettings.enhancedTileVisibility(checked());}
        };
        chkTileVisibility.checked(SPDSettings.enhancedTileVisibility());add(chkTileVisibility);
        chkStairNavigation=new CheckBox("Automatic Stair Navigation"){
            @Override protected void onClick(){super.onClick();SPDSettings.stairNavigationShortcut(checked());
                if(checked()) ShatteredPixelDungeon.scene().addToFront(new WndMessage(
                    "Tap the staircase icon beside the hotbar, above the side controls, and choose Stairs Up or Stairs Down. Only discovered stairs are available. Enable Auto-enter Stairs below to also change floors on arrival."));
            }
        };
        chkStairNavigation.checked(SPDSettings.stairNavigationShortcut());add(chkStairNavigation);
        chkAutoEnterStairs=new CheckBox("Auto-enter Stairs on Arrival"){
            @Override protected void onClick(){super.onClick();SPDSettings.autoEnterStairs(checked());}
        };
        chkAutoEnterStairs.checked(SPDSettings.autoEnterStairs());add(chkAutoEnterStairs);

        btnPlaybackSpeed = new RedButton("Game speed: "
                + com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup.label(SPDSettings.playbackSpeed())) {
            @Override protected void onClick() {
                if (com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures.testBuild()) {
                    final java.util.List<WndUimsdList.Entry> choices = new java.util.ArrayList<>();
                    for (int i = 0; i < com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup.choiceCount(); i++) {
                        choices.add(WndUimsdList.Entry.item(
                                com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup.label(i))
                                .current(i == SPDSettings.playbackSpeed()));
                    }
                    ShatteredPixelDungeon.scene().addToFront(WndUimsdList.menu("Game speed",
                            "PRIVATE TEST BUILD: extreme visual playback only. Combat turns remain unchanged.",
                            choices, (index, checked) -> {
                                if (!com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup.allowed(index)) return;
                                SPDSettings.playbackSpeed(index);
                                btnPlaybackSpeed.text("Game speed: "
                                        + com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup.label(SPDSettings.playbackSpeed()));
                            }));
                } else {
                    ShatteredPixelDungeon.scene().addToFront(new WndOptions("Game speed",
                            "Playback only. Turn costs and combat stay unchanged.", "0.5x", "1x", "2x") {
                        @Override protected void onSelect(int index) {
                            if (!com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup.allowed(index)) return;
                            SPDSettings.playbackSpeed(index);
                            btnPlaybackSpeed.text("Game speed: "
                                    + com.shatteredpixel.shatteredpixeldungeon.apd.PlaybackGroup.label(SPDSettings.playbackSpeed()));
                        }
                    });
                }
            }
        };
        add(btnPlaybackSpeed);
        btnHealingConfirmation = new RedButton("Healing confirmation") {
            @Override protected void onClick() { WndHealingConfirmation.settings(); }
        };
        add(btnHealingConfirmation);
        btnCustomRules = new RedButton("Custom Dungeon Rules", 6) {
            @Override protected void onClick() {
                ShatteredPixelDungeon.scene().addToFront(new WndCustomDungeonRules());
            }
        };
        add(btnCustomRules);
        btnCurrentCustomRules = new RedButton("View current run rules", 6) {
            @Override protected void onClick() {
                ShatteredPixelDungeon.scene().addToFront(new WndCustomDungeonRules(true));
            }
        };
        btnCurrentCustomRules.visible = Game.scene() instanceof GameScene;
        btnCurrentCustomRules.active = btnCurrentCustomRules.visible;
        add(btnCurrentCustomRules);
		btnClericQuickCasts = new RedButton("Cleric quick-cast buttons") {
            @Override protected void onClick() { WndClericQuickCasts.settings(); }
        };
        add(btnClericQuickCasts);

		status = PixelScene.renderTextBlock( ApdRecorder.status().summary(), 5 );
		add( status );

		//measured once when the pane opens (a few directory listings), so the window is sized for the text it holds
		String meter;
		try {
			meter = HistoryTransfer.storage().summary();
		} catch (Throwable e) {
			meter = "History: could not be measured (" + e + ")";
		}
		storage = PixelScene.renderTextBlock( meter, 5 );
		add( storage );

		btnTest = new RedButton( "Run self-test" ) {
			@Override
			protected void onClick() {
				runSelfTest( this );
			}
		};
		add( btnTest );

		btnImport = new RedButton( "Import APD rankings" ) {
			@Override
			protected void onClick() {
				runImport( this );
			}
		};
		add( btnImport );

		btnExportHistory = new RedButton( "Export history (ZIP)" ) {
			@Override
			protected void onClick() {
				runTransfer( this, "Export history (ZIP)", true );
			}
		};
		add( btnExportHistory );

		btnImportHistory = new RedButton( "Import history (ZIP)" ) {
			@Override
			protected void onClick() {
				runTransfer( this, "Import history (ZIP)", false );
			}
		};
		add( btnImportHistory );

		btnUimsd = new RedButton( "Open UIMSD (preview)" ) {
			@Override
			protected void onClick() {
				UimsdScene.open( ShatteredPixelDungeon.scene().getClass() == GameScene.class ? GameScene.class : TitleScene.class );
			}
		};
		add( btnUimsd );

		if (GrudgeHooks.available()) {
			btnGrudge = new RedButton( "About / Developer: The Grudge" ) {
				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront( new WndClericGrudge() );
				}
			};
			add( btnGrudge );
		}

		if (DemoData.available()) {
			btnDemoGenerate = new RedButton( "DEMO data: generate / reset" ) {
				@Override
				protected void onClick() {
					generateDemo( this );
				}
			};
			add( btnDemoGenerate );

			btnDemoRemove = new RedButton( "DEMO data: remove all" ) {
				@Override
				protected void onClick() {
					DemoData.viewDemo( false );
					boolean gone = DemoData.delete( demoRoot() );
					btnDemoView.text( demoViewText() );
					ShatteredPixelDungeon.scene().addToFront( new WndMessage( gone
							? "All demo data removed. Your real history was not touched. UIMSD shows real data."
							: "Nothing was removed: that folder is not demo data." ) );
				}
			};
			add( btnDemoRemove );

			btnDemoView = new RedButton( demoViewText() ) {
				@Override
				protected void onClick() {
					DemoData.viewDemo( !DemoData.viewingDemo() );
					text( demoViewText() );
				}
			};
			add( btnDemoView );
		}
	}

	private static String demoViewText() {
		return DemoData.viewingDemo() ? "UIMSD shows: DEMO data" : "UIMSD shows: REAL data";
	}

	private static java.io.File demoRoot() {
		java.io.File base = ApdRecorder.historyRoot();
		if (base == null) base = FileUtils.getFileHandle( "apd" ).file();
		return DemoData.demoRootFor( base );
	}

	private void generateDemo( final RedButton button ) {
		button.text( "Generating..." );
		button.enable( false );
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
				String result;
				try {
					int runs = DemoGenerator.generate( demoRoot(), Ruleset.forNewRun(), System.currentTimeMillis() );
					DemoData.viewDemo( true );
					result = runs + " made-up runs written. UIMSD now shows DEMO data (the button below switches back to your real data). Your real history was not touched.";
				} catch (Throwable e) {
					result = "Demo data failed: " + e;
				}
				final String shown = result;
				Game.runOnRenderThread( new Callback() {
					@Override
					public void call() {
						button.text( "DEMO data: generate / reset" );
						button.enable( true );
						btnDemoView.text( demoViewText() );
						ShatteredPixelDungeon.scene().addToFront( new WndMessage( shown ) );
					}
				} );
			}
		} );
		t.start();
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
		chkAdult.setRect( x, pos, width, 16 );
		pos = chkAdult.bottom() + GAP;
        chkTileVisibility.setRect(x,pos,width,16);pos=chkTileVisibility.bottom()+GAP;
        chkStairNavigation.setRect(x,pos,width,16);pos=chkStairNavigation.bottom()+GAP;
        chkAutoEnterStairs.setRect(x,pos,width,16);pos=chkAutoEnterStairs.bottom()+GAP;
        btnPlaybackSpeed.setRect(x,pos,width,16);pos=btnPlaybackSpeed.bottom()+GAP;
        btnHealingConfirmation.setRect(x,pos,width,16);pos=btnHealingConfirmation.bottom()+GAP;
        btnCustomRules.setRect(x,pos,width,20);pos=btnCustomRules.bottom()+GAP;
        if(btnCurrentCustomRules.visible){btnCurrentCustomRules.setRect(x,pos,width,20);pos=btnCurrentCustomRules.bottom()+GAP;}
        btnClericQuickCasts.setRect(x,pos,width,16);
        pos = btnClericQuickCasts.bottom() + GAP;
		status.maxWidth( (int)width );
		status.setPos( x, pos );
		pos = status.bottom() + GAP;
		storage.maxWidth( (int)width );
		storage.setPos( x, pos );
		pos = storage.bottom() + GAP;
		btnTest.setRect( x, pos, width, 16 );
		pos = btnTest.bottom() + GAP;
		btnImport.setRect( x, pos, width, 16 );
		pos = btnImport.bottom() + GAP;
		btnExportHistory.setRect( x, pos, width, 16 );
		pos = btnExportHistory.bottom() + GAP;
		btnImportHistory.setRect( x, pos, width, 16 );
		pos = btnImportHistory.bottom() + GAP;
		btnUimsd.setRect( x, pos, width, 16 );
		height = btnUimsd.bottom() - y;
		if (btnGrudge != null) {
			pos = btnUimsd.bottom() + GAP;
			btnGrudge.setRect( x, pos, width, 16 );
			height = btnGrudge.bottom() - y;
		}
		if (btnDemoGenerate != null) {
			pos = (btnGrudge != null ? btnGrudge : btnUimsd).bottom() + GAP;
			btnDemoGenerate.setRect( x, pos, width, 16 );
			pos = btnDemoGenerate.bottom() + GAP;
			btnDemoRemove.setRect( x, pos, width, 16 );
			pos = btnDemoRemove.bottom() + GAP;
			btnDemoView.setRect( x, pos, width, 16 );
			height = btnDemoView.bottom() - y;
		}
	}

	//export or import the whole history through the platform's file chooser; the result is shown in plain words
	private void runTransfer( final RedButton button, final String label, boolean export ) {
		button.enable( false );
		HistoryTransfer.Listener done = new HistoryTransfer.Listener() {
			@Override
			public void finished( final String message ) {
				Game.runOnRenderThread( new Callback() {
					@Override
					public void call() {
						button.enable( true );
						ShatteredPixelDungeon.scene().addToFront( new WndMessage( message ) );
					}
				} );
			}
		};
		if (export) HistoryTransfer.export( done );
		else HistoryTransfer.importHistory( done );
	}

	private static void runImport( final RedButton button ) {
		button.text( "Importing..." );
		button.enable( false );
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
				String result;
				try {
					result = ApdRecorder.importApdRankings() + "\n\n" + ApdRecorder.historyOverview();
				} catch (Throwable e) {
					result = "Import failed: " + e;
				}
				final String shown = result;
				Game.runOnRenderThread( new Callback() {
					@Override
					public void call() {
						button.text( "Import APD rankings" );
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
					SelfTest.Report report = SelfTest.run( work );
					com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdSelfTest.run( work, report );
					com.shatteredpixel.shatteredpixeldungeon.apd.archive.ArchiveSelfTest.run( work, report );
					result = report.text();
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


