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

	public enum Page {
		NEWS("APD News",
				"_ADHD Pixel Dungeon_ is a private fork of Shattered Pixel Dungeon, built on v4.0.0.\n\n" +
				"This is an INDEV build. Expect rough edges, and expect things to change.\n\n" +
				"Plain text title for now, there is no APD art yet."),
		CHANGES("APD Changes",
				"_APD 4.0.8-apd_\n\n" +
				"_APD-0001:_ starvation damage now consumes shielding before HP. Shielding does not cure hunger.\n\n" +
				"_Sad Ghost:_ the offered weapon and armor now show their real upgrade level.\n\n" +
				"_Exotic chance:_ new APD setting (pause menu > APD Settings), 0% to 80%, a minimum chance that generated potions and scrolls are exotic. Exotic Crystals can still raise it higher.\n\n" +
				"_Cleric:_ removed. Do not try it.\n\n" +
				"_Menus:_ APD settings, APD News and APD Changes screens, APD title text and INDEV version string.");

		final String title;
		final String text;

		Page( String title, String text ){
			this.title = title;
			this.text = text;
		}
	}

	public static Page page = Page.NEWS;

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

		RenderedTextBlock body = PixelScene.renderTextBlock( page.text, 6 );
		body.setHightlighting( true, Window.TITLE_COLOR );
		body.maxWidth( (int)colWidth );
		body.setPos( (w - colWidth)/2f, title.bottom() + 8 );
		content.add( body );

		content.setSize( w, body.bottom() + 10 + insets.bottom );

		list.setRect( 0, 0, w, h );
		list.scrollTo( 0, 0 );

		ExitButton btnExit = new ExitButton();
		int ofs = PixelScene.landscape() ? 0 : 4;
		btnExit.setPos( Camera.main.width - btnExit.width() - ofs, ofs );
		add( btnExit );
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchScene( AboutScene.class );
	}
}
