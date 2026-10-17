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
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

//APD: the in-game menu's "APD Settings" window. The same pane is also the "APD" tab of Options (WndSettings).
public class WndAPDSettings extends Window {

	private static final int WIDTH = 120;

	public WndAPDSettings() {
		super();

		//ORDER MATTERS. The newest touch area is asked first, and a scrolling pane's own touch area covers everything inside it. So the pane
		//is made FIRST (empty) and the controls are created AFTER it: then every button, slider and tick box is asked before the pane is, and
		//the pane only gets what the controls do not take (the drag that scrolls it). Built the other way round, the pane swallows every touch
		//and every control is dead (ApdSettingsClickDeviceTest touches each one).
		Component content = new Component();
		ScrollPane scroll = new ScrollPane( content );
		APDSettingsPane pane = new APDSettingsPane();
		pane.setSize( WIDTH, 0 );

		//the pane has grown with the storage meter and the history buttons: when it is taller than the screen it scrolls
		content.add( pane );
		float contentHeight = (float)Math.ceil( pane.height() );
		content.setSize( WIDTH, contentHeight );
		float height = WindowGeometry.paneHeight( contentHeight, WindowGeometry.maxPane( PixelScene.uiCamera.height, 0, 0 ) );
		//size the window first: it moves the window's camera, and the pane works out its screen position when it is placed
		resize( WIDTH, (int)height );
		add( scroll );
		scroll.setRect( 0, 0, WIDTH, height );
		scroll.allowDragFromButtons();
	}
}
