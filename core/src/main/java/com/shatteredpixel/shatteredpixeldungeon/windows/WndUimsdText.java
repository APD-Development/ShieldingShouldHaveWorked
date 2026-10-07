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

import com.shatteredpixel.shatteredpixeldungeon.apd.query.WindowGeometry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

//APD: a title and a scrolling block of text, for the UIMSD explanations (coverage, what the markers mean).
public class WndUimsdText extends Window {

	private static final int MARGIN = 2;

	public WndUimsdText( String title, String text ){
		super();
		int width = WndUimsdList.windowWidth();
		RenderedTextBlock tt = PixelScene.renderTextBlock(title, 9);
		tt.hardlight(TITLE_COLOR);
		tt.maxWidth(width - 2 * MARGIN);
		tt.setPos(MARGIN, MARGIN);
		add(tt);
		float pos = tt.bottom() + 2 * MARGIN;

		Component content = new Component();
		RenderedTextBlock body = PixelScene.renderTextBlock(text, 6);
		body.maxWidth(width - 2 * MARGIN - 2);
		body.setPos(MARGIN, 0);
		content.add(body);
		content.setSize(width, body.bottom() + 2);
		ScrollPane pane = new ScrollPane(content);
		float h = WindowGeometry.paneHeight(content.height(), WindowGeometry.maxPane(PixelScene.uiCamera.height, pos, 0));
		//size the window first: it moves the window's camera, and the pane works out its screen position when it is placed
		resize(width, (int) (pos + h));
		add(pane);
		pane.setRect(0, pos, width, h);
	}
}
