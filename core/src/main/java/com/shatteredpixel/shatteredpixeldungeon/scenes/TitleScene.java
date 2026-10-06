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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVictoryCongrats;
import com.watabou.glwrap.Blending;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.RectF;

//APD: the title/home screen uses the APD art in assets (apd_logo, apd_logo_glow, apd_flame, apd_button,
//apd_bg_port, apd_bg_land). Portrait stacks the logo over the buttons, landscape puts them side by side.
public class TitleScene extends PixelScene {

	//art dimensions, see docs/APD-TITLE-SCREEN.md
	private static final int LOGO_W = 139;
	private static final int LOGO_H = 100;
	private static final int FLAME_SIZE = 47;
	private static final int FLAME_BASE_X = 23; //flame base is bottom-centre of the 47x47 cell
	private static final int FLAME_BASE_Y = 46;

	//brazier bowl anchors inside apd_logo.png, from the asset notes. Tweak these if the flames look off.
	private static final int LEFT_FLAME_X = 10;
	private static final int RIGHT_FLAME_X = 129;
	private static final int FLAME_Y = 67;

	private static final int BG_COLOR = 0xFF08110D;

	private static final int MAIN_GAP = 3;
	private static final int BOTTOM_H = 20;
	//APD: the full-width APD Settings row sits between the 2x3 grid and the News/Changes/About row
	private static final int APD_ROW_H = BOTTOM_H + MAIN_GAP;

	//APD: joke annotation pointing at the UIMSD button
	private static final String ANNOTATION_TEXT = "HEY, GO HERE FOR DRUGS";

	private Image logo;
	private Image glow;
	private APDFlame leftFlame;
	private APDFlame rightFlame;

	private APDMenuButton btnPlay;
	private APDMenuButton btnStatistics;
	private APDMenuButton btnBadges;
	private APDMenuButton btnJournal;
	private APDMenuButton btnOptions;
	private APDMenuButton btnQuit;
	private APDMenuButton btnNews;
	private APDMenuButton btnChanges;
	private APDMenuButton btnAbout;
	private APDMenuButton btnAPDSettings;
	private APDAnnotation annotation;

	private BitmapText version;
	private IconButton btnFade;
	private ExitButton btnExit;

	@Override
	public void create() {
		
		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;
		
		int fullW = Camera.main.width;
		int fullH = Camera.main.height;

		RectF insets = getCommonInsets();

		//flat fill for any space the background plate doesn't cover, the plate edges fade to this colour
		add( new ColorBlock( fullW, fullH, BG_COLOR ) );

		Image plate = new Image( landscape() ? "splashes/title/apd_bg_land.png" : "splashes/title/apd_bg_port.png" );
		plate.x = (fullW - plate.width()) / 2f;
		plate.y = (fullH - plate.height()) / 2f;
		align( plate );
		add( plate );

		int w = (int)(fullW - insets.left - insets.right);
		int h = (int)(fullH - insets.top - insets.bottom);

		logo = new Image( "interfaces/apd_logo.png" );
		add( logo );

		glow = new Image( "interfaces/apd_logo_glow.png" ){
			private float time = 0;
			@Override
			public void update() {
				super.update();
				am = Math.max(0f, (float)Math.sin( time += Game.elapsed ));
				am = Math.min(am, logo.am);
				if (time >= 1.5f*Math.PI) time = 0;
			}
			@Override
			public void draw() {
				Blending.setLightMode();
				super.draw();
				Blending.setNormalMode();
			}
		};
		add( glow );

		leftFlame = new APDFlame( false );
		rightFlame = new APDFlame( true );
		add( leftFlame );
		add( rightFlame );

		btnPlay = new APDMenuButton( "Play", Icons.ENTER, true ){
			@Override
			protected void onClick() {
				super.onClick();
				if (GamesInProgress.checkAll().size() == 0){
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
				} else {
					ShatteredPixelDungeon.switchNoFade( StartScene.class );
				}
			}

			@Override
			protected boolean onLongClick() {
				//making it easier to start runs quickly while debugging
				if (DeviceCompat.isDebug()) {
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
					return true;
				}
				return super.onLongClick();
			}
		};
		add( btnPlay );

		//UIMSD (Ultimate Infinite Mega Statistics Database) replaces Rankings, until the real database exists it opens the Rankings screen
		btnStatistics = new APDMenuButton( "UIMSD", Icons.STATS, true ){
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.switchNoFade( RankingsScene.class );
			}
		};
		add( btnStatistics );
		Dungeon.daily = Dungeon.dailyReplay = false;

		btnBadges = new APDMenuButton( "Badges", Icons.BADGES, true ){
			@Override
			protected void onClick() {
				super.onClick();
				JournalScene.lastIDX = 0; //badges tab
				ShatteredPixelDungeon.switchNoFade( JournalScene.class );
			}
		};
		add( btnBadges );

		btnJournal = new APDMenuButton( "Journal", Icons.JOURNAL, true ){
			@Override
			protected void onClick() {
				super.onClick();
				if (JournalScene.lastIDX == 0) JournalScene.lastIDX = 1; //catalog tab, badges has its own button
				ShatteredPixelDungeon.switchNoFade( JournalScene.class );
			}
		};
		add( btnJournal );

		btnOptions = new APDMenuButton( "Options", Icons.PREFS, true ){
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.scene().add(new WndSettings());
			}
		};
		add( btnOptions );

		btnQuit = new APDMenuButton( "Quit", Icons.EXIT, true ){
			@Override
			protected void onClick() {
				super.onClick();
				Game.instance.finish();
			}
		};
		//iOS apps don't quit themselves
		if (DeviceCompat.isiOS()) btnQuit.visible = btnQuit.active = false;
		add( btnQuit );

		//APD: APD-specific settings have their own entry, separate from Options
		btnAPDSettings = new APDMenuButton( "APD Settings", Icons.PREFS, false ){
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.scene().add(new WndAPDSettings());
			}
		};
		add( btnAPDSettings );

		//APD: News and Changes open the offline APD screens, not the stock internet/upstream ones
		btnNews = new APDMenuButton( "News", Icons.NEWS, false ){
			@Override
			protected void onClick() {
				super.onClick();
				APDTextScene.page = APDTextScene.Page.NEWS;
				APDTextScene.backTo = TitleScene.class;
				ShatteredPixelDungeon.switchNoFade( APDTextScene.class );
			}
		};
		add( btnNews );

		btnChanges = new APDMenuButton( "Changes", Icons.CHANGES, false ){
			@Override
			protected void onClick() {
				super.onClick();
				APDTextScene.page = APDTextScene.Page.CHANGES;
				APDTextScene.backTo = TitleScene.class;
				ShatteredPixelDungeon.switchNoFade( APDTextScene.class );
			}
		};
		add( btnChanges );

		btnAbout = new APDMenuButton( "About", Icons.INFO, false ){
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.switchScene( AboutScene.class );
			}
		};
		add( btnAbout );

		version = new BitmapText( "v4.0.8-apd-INDEV", pixelFont);
		version.measure();
		version.hardlight( 0x8C938C );
		add( version );

		//*** layout ***

		float logoX, logoY;
		float gridLeft, gridWidth, gridTop;
		float versionGap = version.height() + 2;

		if (landscape()){
			//logo on the left, button grid on the right
			float gridMax = 150;
			gridWidth = Math.min(gridMax, w - LOGO_W - 12);
			float totalW = LOGO_W + 8 + gridWidth;
			float left = insets.left + (w - totalW) / 2f;

			annotation = new APDAnnotation( ANNOTATION_TEXT, gridWidth );
			add( annotation );
			float mainH = mainHeightWith( h - 8, annotation.height() );
			float gridH = annotation.height() + 3*mainH + 3*MAIN_GAP + APD_ROW_H + BOTTOM_H;

			logoX = left;
			logoY = insets.top + (h - (LOGO_H + versionGap)) / 2f;
			gridLeft = left + LOGO_W + 8;
			gridTop = insets.top + (h - gridH) / 2f + annotation.height();
			layoutGrid( gridLeft, gridTop, gridWidth, mainH );
		} else {
			gridWidth = Math.min(w - 8, 150);
			gridLeft = insets.left + (w - gridWidth) / 2f;

			float topPad = 6;
			float availForGrid = h - topPad - LOGO_H - versionGap - 6 - 4;
			annotation = new APDAnnotation( ANNOTATION_TEXT, gridWidth );
			add( annotation );
			float mainH = mainHeightWith( availForGrid, annotation.height() );
			float gridH = annotation.height() + 3*mainH + 3*MAIN_GAP + APD_ROW_H + BOTTOM_H;
			float contentH = LOGO_H + versionGap + 4 + gridH;

			logoX = insets.left + (w - LOGO_W) / 2f;
			logoY = insets.top + Math.max(topPad, (h - contentH) * 0.35f);
			gridTop = logoY + LOGO_H + versionGap + 4 + annotation.height();
			layoutGrid( gridLeft, gridTop, gridWidth, mainH );
		}

		logo.x = logoX;
		logo.y = logoY;
		align( logo );
		glow.x = logo.x;
		glow.y = logo.y;

		leftFlame.x = logo.x + LEFT_FLAME_X - FLAME_BASE_X;
		leftFlame.y = logo.y + FLAME_Y - FLAME_BASE_Y;
		rightFlame.x = logo.x + RIGHT_FLAME_X - FLAME_BASE_X;
		rightFlame.y = logo.y + FLAME_Y - FLAME_BASE_Y;
		align( leftFlame );
		align( rightFlame );

		version.x = logo.x + (LOGO_W - version.width()) / 2f;
		version.y = logo.y + LOGO_H - 1;
		align( version );

		btnFade = new IconButton(Icons.CHEVRON.get()){
			@Override
			protected void onClick() {
				enable(false);
				parent.add(new Tweener(parent, 0.5f) {
					@Override
					protected void updateValues(float progress) {
						if (!btnFade.active) {
							uiAlpha = 1 - progress;
							updateFade();
						}
					}
				});
			}
		};
		btnFade.icon().originToCenter();
		btnFade.icon().angle = 180f;
		btnFade.setRect(insets.left + (w-16)/2f, Camera.main.height - 16 - insets.bottom, 16, 16);
		add(btnFade);

		PointerArea fadeResetter = new PointerArea(0, 0, Camera.main.width, Camera.main.height){
			@Override
			public boolean onSignal(PointerEvent event) {
				if (event != null && event.type == PointerEvent.Type.UP && !btnPlay.active){
					parent.add(new Tweener(parent, 0.5f) {
						@Override
						protected void updateValues(float progress) {
							uiAlpha = progress;
							updateFade();
							if (progress >= 1){
								btnFade.enable(true);
							}
						}
					});
				}
				return false;
			}
		};
		add(fadeResetter);

		if (DeviceCompat.isDesktop()) {
			btnExit = new ExitButton();
			btnExit.setPos( insets.left + w - btnExit.width(), insets.top );
			add( btnExit );
		}

		Badges.loadGlobal();
		if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		fadeIn();
	}

	//height of each of the three main button rows, given the vertical space available for the whole grid
	private static float mainHeight( float availableForGrid ){
		float h = (availableForGrid - APD_ROW_H - BOTTOM_H - 3*MAIN_GAP) / 3f;
		return GameMath.gate( 24, (float)Math.floor(h), 36 );
	}

	//the main rows keep their normal height when the screen has room for the annotation, otherwise they give up the space it needs
	private static float mainHeightWith( float availableForGrid, float annotationH ){
		float normal = mainHeight( availableForGrid );
		float used = 3*normal + 3*MAIN_GAP + APD_ROW_H + BOTTOM_H;
		if (availableForGrid - used >= annotationH) return normal;
		return mainHeight( availableForGrid - annotationH );
	}

	//2 columns x 3 rows, then a row of three smaller buttons
	private void layoutGrid( float left, float top, float width, float mainH ){
		float colW = (float)Math.floor((width - MAIN_GAP) / 2f);

		APDMenuButton[] main = { btnPlay, btnStatistics, btnBadges, btnJournal, btnOptions, btnQuit };
		for (int i = 0; i < main.length; i++){
			int col = i % 2, row = i / 2;
			main[i].setRect( left + col*(colW + MAIN_GAP), top + row*(mainH + MAIN_GAP), colW, mainH );
			align( main[i] );
		}

		//the arrow points down at the UIMSD button (top row, right column), the text sits above it in the space reserved over the grid
		annotation.place( left, width, (float)Math.floor((width - MAIN_GAP) / 2f) + MAIN_GAP, top );

		float apdY = top + 3*(mainH + MAIN_GAP);
		btnAPDSettings.setRect( left, apdY, width, BOTTOM_H );
		align( btnAPDSettings );

		float bottomY = apdY + APD_ROW_H;
		float smallW = (float)Math.floor((width - 2*MAIN_GAP) / 3f);
		APDMenuButton[] bottom = { btnNews, btnChanges, btnAbout };
		for (int i = 0; i < bottom.length; i++){
			bottom[i].setRect( left + i*(smallW + MAIN_GAP), bottomY, smallW, BOTTOM_H );
			align( bottom[i] );
		}
	}

	private float uiAlpha;

	public void updateFade() {
		float alpha = GameMath.gate(0f, uiAlpha, 1f);

		logo.am = alpha;
		leftFlame.am = alpha;
		rightFlame.am = alpha;
		//glow handles its own alpha

		APDMenuButton[] all = { btnPlay, btnStatistics, btnBadges, btnJournal, btnOptions, btnQuit,
				btnAPDSettings, btnNews, btnChanges, btnAbout };
		for (APDMenuButton b : all){
			//Quit is hidden on iOS, enable() would make it tappable again
			if (b == btnQuit && DeviceCompat.isiOS()) continue;
			b.enable(alpha != 0);
			b.alpha(alpha);
		}

		annotation.alpha(alpha);
		version.alpha(alpha);
		btnFade.icon().alpha(alpha);
		if (btnExit != null){
			btnExit.enable(alpha != 0);
			btnExit.icon().alpha(alpha);
		}

	}

	//animated green flame, 24 frames of 47x47 in a 5x5 sheet. The second flame is mirrored and offset.
	private static class APDFlame extends MovieClip {

		APDFlame( boolean second ){
			texture( "effects/apd_flame.png" );
			TextureFilm frames = new TextureFilm( texture, FLAME_SIZE, FLAME_SIZE );
			MovieClip.Animation anim = new MovieClip.Animation( 24, true );
			anim.frames( frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 );
			play( anim );

			if (second){
				flipHorizontal = true;
				curFrame = 12;
				frame( curAnim.frames[curFrame] );
			}
		}
	}

	//Deliberately out of place: hot pink text and a bobbing arrow pointing down at the UIMSD button. The text breaks into
	//two lines only when one line is wider than the grid. It is not interactive and sits in its own strip above the grid.
	private static class APDAnnotation extends Component {

		private static final int COLOR = 0xFF4FD8;
		private static final int SHADOW = 0xFF000000;
		private static final int HEAD_ROWS = 4;  //arrow head is 7,5,3,1 pixels wide
		private static final int SHAFT_H = 3;
		private static final int ARROW_H = HEAD_ROWS + SHAFT_H;
		private static final int BOB = 2;        //the arrow only bobs upwards, so it never touches the button

		private final BitmapText[] lines;
		private final BitmapText[] shadows;
		private final ColorBlock[] arrow;
		private final ColorBlock[] arrowShadow;
		private final float textH;
		private float arrowX, arrowY;
		private float time;

		APDAnnotation( String text, float maxWidth ){
			String[] parts = { text };
			BitmapText probe = new BitmapText( text, pixelFont );
			probe.measure();
			if (probe.width() > maxWidth){
				int cut = text.indexOf( ' ', text.indexOf( "HERE" ) );
				parts = new String[]{ text.substring( 0, cut ), text.substring( cut + 1 ) };
			}
			lines = new BitmapText[parts.length];
			shadows = new BitmapText[parts.length];
			float lh = 0;
			for (int i = 0; i < parts.length; i++){
				shadows[i] = new BitmapText( parts[i], pixelFont );
				shadows[i].hardlight( SHADOW );
				shadows[i].measure();
				add( shadows[i] );
				lines[i] = new BitmapText( parts[i], pixelFont );
				lines[i].hardlight( COLOR );
				lines[i].measure();
				add( lines[i] );
				lh = lines[i].height();
			}
			textH = lh * parts.length + 1;

			arrow = new ColorBlock[HEAD_ROWS + 1];
			arrowShadow = new ColorBlock[HEAD_ROWS + 1];
			for (int i = 0; i <= HEAD_ROWS; i++){
				int wid = i < HEAD_ROWS ? 7 - 2*i : 3; //shaft 3 wide, then head rows 7,5,3,1 wide (pointing down)
				arrowShadow[i] = new ColorBlock( wid, i < HEAD_ROWS ? 1 : SHAFT_H, SHADOW );
				add( arrowShadow[i] );
				arrow[i] = new ColorBlock( wid, i < HEAD_ROWS ? 1 : SHAFT_H, COLOR );
				add( arrow[i] );
			}
			setSize( maxWidth, height() );
		}

		//the strip this takes above the grid, including the room the arrow bobs in
		@Override
		public float height() {
			return (float)Math.ceil( textH + 1 + ARROW_H + 1 + BOB );
		}

		//gridLeft/gridWidth: the grid's span. rightColOffset: where the right column starts. gridTop: top of the buttons.
		void place( float gridLeft, float gridWidth, float rightColOffset, float gridTop ){
			float colW = gridWidth - rightColOffset;
			float centre = gridLeft + rightColOffset + colW / 2f;
			arrowX = (float)Math.floor( centre - 3.5f );
			//rest position: the tip is 1px above the button
			arrowY = gridTop - 1 - ARROW_H;
			float textRight = gridLeft + gridWidth;
			float top = gridTop - height();
			for (int i = 0; i < lines.length; i++){
				float x = Math.max( gridLeft, textRight - lines[i].width() );
				float y = top + i * lines[i].height();
				lines[i].x = (float)Math.floor( x );
				lines[i].y = (float)Math.floor( y );
				shadows[i].x = lines[i].x + 1;
				shadows[i].y = lines[i].y + 1;
			}
			positionArrow( 0 );
		}

		private void positionArrow( float lift ){
			float y = arrowY - lift;
			for (int i = 0; i < arrow.length; i++){
				//the shaft is on top, the head rows hang below it
				float rowY = i < HEAD_ROWS ? y + SHAFT_H + i : y;
				float wid = arrow[i].width();
				float x = arrowX + (7 - wid) / 2f;
				arrow[i].x = (float)Math.floor( x );
				arrow[i].y = (float)Math.floor( rowY );
				arrowShadow[i].x = arrow[i].x + 1;
				arrowShadow[i].y = arrow[i].y + 1;
			}
		}

		void alpha( float value ){
			for (BitmapText t : lines) t.alpha( value );
			for (BitmapText t : shadows) t.alpha( value );
			for (ColorBlock b : arrow) b.am = value;
			for (ColorBlock b : arrowShadow) b.am = value;
		}

		@Override
		public void update() {
			super.update();
			time += Game.elapsed;
			positionArrow( Math.round( Math.abs( (float)Math.sin( time * 4f ) ) * BOB ) );
		}
	}

	//stone button using apd_button.png as a 9-patch (8px margins). Main buttons stack the icon above the
	//label, the bottom row puts the icon beside it.
	private static class APDMenuButton extends StyledButton {

		private final boolean stacked;

		APDMenuButton( String label, Icons icon, boolean stacked ){
			super( Chrome.Type.GREY_BUTTON_TR, label, 6 );
			this.stacked = stacked;

			remove( bg );
			bg = new NinePatch( "interfaces/apd_button.png", 8 );
			addToBack( bg );

			icon( Icons.get( icon ) );
		}

		@Override
		protected void layout() {
			super.layout();

			if (stacked && icon != null && text != null){
				float contentH = icon.height() + 2 + text.height();
				float top = y + (height - contentH) / 2f;

				icon.x = x + (width - icon.width()) / 2f;
				icon.y = top;
				PixelScene.align( icon );

				text.setPos( x + (width - text.width()) / 2f, top + icon.height() + 2 );
				PixelScene.align( text );
			}
		}
	}
}
