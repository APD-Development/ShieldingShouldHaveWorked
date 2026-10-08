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

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;

//APD: a plain scrolling text screen used for APD News and APD Changes
public class APDTextScene extends PixelScene {

	//the text of each page is a plain file in core/src/main/assets/apd, add a new block at the top of
	//apd_changes.txt for every build
	public enum Page {
		NEWS("APD News", "apd/apd_news.txt"),
		CHANGES("APD Changes", "apd/apd_changes.txt");

		final String title;
		final String file;

		Page( String title, String file ){
			this.title = title;
			this.file = file;
		}

		String text(){
			try {
				return Gdx.files.internal(file).readString("UTF-8");
			} catch (Exception e) {
				return "Could not read " + file;
			}
		}
	}

	public static Page page = Page.NEWS;
	public static Class<? extends PixelScene> backTo = AboutScene.class;

	@Override
	public void create() {
		super.create();

		int w = Camera.main.width;
		int h = Camera.main.height;

		RectF insets = getCommonInsets();

		TitleBackground BG = new TitleBackground( w, h );
		add( BG );

		//darkens the arches
		add(new ColorBlock(w, h, 0x44000000));

		final float colWidth = Math.min(landscape() ? 200 : 130, w - insets.left - insets.right - 8);

		ScrollPane list = new ScrollPane( new Component() );
		add( list );

		Component content = list.content();
		content.clear();

		RenderedTextBlock title = PixelScene.renderTextBlock( page.title, 12 );
		title.hardlight( Window.TITLE_COLOR );
		title.setPos( (w - title.width())/2f, insets.top + 8 );
		align( title );
		content.add( title );

		RenderedTextBlock body = PixelScene.renderTextBlock( page.text(), 6 );
		body.maxWidth( (int)colWidth );
		body.setPos( (w - colWidth)/2f, title.bottom() + 8 );
		content.add( body );

		content.setSize( w, body.bottom() + 10 + insets.bottom );

		list.setRect( 0, 0, w, h );
		list.scrollTo( 0, 0 );

		//the visible exit and the Back key are the same: back to where this screen was opened from (the default exit jumps to the title)
		ExitButton btnExit = new ExitButton(){
			@Override
			protected void onClick(){
				onBackPressed();
			}
		};
		int ofs = PixelScene.landscape() ? 0 : 4;
		btnExit.setPos( Camera.main.width - btnExit.width() - ofs, ofs );
		add( btnExit );
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchScene( backTo );
	}
}
