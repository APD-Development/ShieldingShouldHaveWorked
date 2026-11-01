/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.apd.TitleGeometry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

/** Original APD backdrop; no upstream title artwork, shared static objects or animated atlas cache. */
public class TitleBackground extends Component {
    // Kept as a source compatibility constant; the new original backdrop is static.
    public static float SCROLL_SPEED = 15f;
    public static void reset() { /* No shared rendered images survive a scene. */ }
    public TitleBackground(int width, int height) {
        super();
        this.width=width;this.height=height;
        Image plate=new Image(width>height?"splashes/title/apd_bg_land.png":"splashes/title/apd_bg_port.png");
        plate.scale.set(TitleGeometry.backgroundCoverScale(width,height,plate.width(),plate.height(),Camera.main.zoom));
        plate.x=(width-plate.width())/2f;plate.y=(height-plate.height())/2f;
        PixelScene.align(plate);add(plate);
    }
}
