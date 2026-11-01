/*
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

/** Direction lock for a slider in a vertical pane; no value or persistence logic. */
public final class SliderGesture {
    public enum Direction { PENDING, HORIZONTAL, VERTICAL }
    private final float startX,startY,dragSlop,tapSlop;
    private Direction direction=Direction.PENDING;
    public SliderGesture(float x,float y,float zoom) {
        startX=x;startY=y;dragSlop=4*zoom;tapSlop=zoom;
    }
    public Direction move(float x,float y) {return classify(x,y,dragSlop);}
    public Direction finish(float x,float y) {return classify(x,y,tapSlop);}
    private Direction classify(float x,float y,float slop) {
        if(direction==Direction.PENDING){
            float dx=Math.abs(x-startX),dy=Math.abs(y-startY);
            if(Math.max(dx,dy)>slop)direction=dx>dy?Direction.HORIZONTAL:Direction.VERTICAL;
        }
        return direction;
    }
}
