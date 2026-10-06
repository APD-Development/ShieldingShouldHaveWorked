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


package com.shatteredpixel.shatteredpixeldungeon.apd;

//Title screen arithmetic with no game or graphics classes in it, so it can be tested on any screen size.
//TitleScene uses these numbers for the vertical layout of the button grid and for the UIMSD annotation arrow.
public final class TitleGeometry {

	private TitleGeometry(){}

	public static final int LOGO_W = 139;
	public static final int LOGO_H = 100;
	public static final int MAIN_GAP = 3;
	public static final int BOTTOM_H = 20;
	//the full-width APD Settings row sits between the 2x3 grid and the News/Changes/About row
	public static final int APD_ROW_H = BOTTOM_H + MAIN_GAP;

	//--- the annotation arrow: a 5 wide shaft, then a head that goes 15, 13, 11 ... 1 wide ---
	public static final int SHAFT_W = 5;
	public static final int SHAFT_H = 5;
	public static final int HEAD_ROWS = 8;
	public static final int HEAD_W = 15;
	public static final int ARROW_H = SHAFT_H + HEAD_ROWS;
	public static final int GLOW = 2;  //the glow reaches this many px past the 1px outline
	public static final int GAP = 3;   //between the tip's glow and the button
	public static final int BOB = 3;   //the arrow only bobs upwards, so it never reaches the button

	//height of each of the three main button rows, given the vertical space available for the whole grid
	public static float mainHeight( float availableForGrid ){
		float h = (availableForGrid - APD_ROW_H - BOTTOM_H - 3*MAIN_GAP) / 3f;
		return Math.max(24, Math.min(36, (float)Math.floor(h)));
	}

	//the main rows keep their normal height when the screen has room for the annotation, otherwise they give up the space it needs
	public static float mainHeightWith( float availableForGrid, float annotationH ){
		float normal = mainHeight( availableForGrid );
		float used = 3*normal + 3*MAIN_GAP + APD_ROW_H + BOTTOM_H;
		if (availableForGrid - used >= annotationH) return normal;
		return mainHeight( availableForGrid - annotationH );
	}

	//the annotation strip: text, the arrow with its outline and glow, the gap above the button, and room for the bob
	public static float annotationHeight( float textHeight ){
		return (float)Math.ceil( textHeight + 2 + (2 + ARROW_H + 2*GLOW) + GAP + BOB );
	}

	public static float gridHeight( float annotationH, float mainH ){
		return annotationH + 3*mainH + 3*MAIN_GAP + APD_ROW_H + BOTTOM_H;
	}

	//column width of the 2x3 grid, and where the right column starts
	public static float colWidth( float gridWidth ){
		return (float)Math.floor((gridWidth - MAIN_GAP) / 2f);
	}

	//the vertical layout result for one orientation (x positions of the logo are not needed here)
	public static final class Layout {
		public float logoY, gridLeft, gridWidth, gridTop, mainH, annotationH;
		//where the version text sits: just under the logo
		public float versionY;
	}

	//w/h: usable screen size, left/top: its insets. versionH: height of the version text. textH: annotation text height.
	public static Layout landscape( float left0, float top0, float w, float h, float versionH, float textH ){
		Layout r = new Layout();
		float versionGap = versionH + 2;
		r.gridWidth = Math.min(150, w - LOGO_W - 12);
		float totalW = LOGO_W + 8 + r.gridWidth;
		float left = left0 + (w - totalW) / 2f;
		r.annotationH = annotationHeight( textH );
		r.mainH = mainHeightWith( h - 8, r.annotationH );
		float gridH = gridHeight( r.annotationH, r.mainH );
		r.logoY = top0 + (h - (LOGO_H + versionGap)) / 2f;
		r.gridLeft = left + LOGO_W + 8;
		r.gridTop = top0 + (h - gridH) / 2f + r.annotationH;
		r.versionY = r.logoY + LOGO_H - 1;
		return r;
	}

	public static Layout portrait( float left0, float top0, float w, float h, float versionH, float textH ){
		Layout r = new Layout();
		float versionGap = versionH + 2;
		r.gridWidth = Math.min(w - 8, 150);
		r.gridLeft = left0 + (w - r.gridWidth) / 2f;
		float topPad = 6;
		float availForGrid = h - topPad - LOGO_H - versionGap - 6 - 4;
		r.annotationH = annotationHeight( textH );
		r.mainH = mainHeightWith( availForGrid, r.annotationH );
		float gridH = gridHeight( r.annotationH, r.mainH );
		float contentH = LOGO_H + versionGap + 4 + gridH;
		r.logoY = top0 + Math.max(topPad, (h - contentH) * 0.35f);
		r.gridTop = r.logoY + LOGO_H + versionGap + 4 + r.annotationH;
		r.versionY = r.logoY + LOGO_H - 1;
		return r;
	}
}
