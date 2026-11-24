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

import com.shatteredpixel.shatteredpixeldungeon.apd.query.RunReplay;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FloorMaps;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;

//UIMSD item 10, run replay screen: a finished run's floors one at a time, the true layout drawn as plain blocks and the hero's recorded
//steps played back in order. Facts only (RunReplay never serves a run still being played).
public class WndRunReplay extends Window {

	private static final int WALL = 0xFF3A3A3A, FLOOR = 0xFF8A7F6A, DOOR = 0xFFB07A3A, WATER = 0xFF3A6AB0, CHASM = 0xFF000000,
			STAIRS = 0xFFE0E0E0, VISITED = 0xFFE0C040, HERO = 0xFFFF3030;
	private static final float STEP_SECONDS = 0.05f;

	private final RunReplay replay;
	private int floorIndex, shown;
	private float timer;
	private boolean playing = true;
	private RenderedTextBlock title;
	private Group map, trail;
	private ColorBlock hero;
	private boolean[] marked;
	private float cell, top, left;
	private RedButton play;

	public static void show( RunReplay r ){
		if (r == null || !r.available || r.floors.isEmpty()){
			com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene().addToFront(new WndMessage(
					"No replay for this run: it is still going, or it was recorded before floor maps were kept."));
			return;
		}
		com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene().addToFront(new WndRunReplay(r));
	}

	private WndRunReplay( RunReplay r ){
		replay = r;
		int width = WndUimsdList.windowWidth();
		title = PixelScene.renderTextBlock("", 9);
		title.maxWidth(width - 4);
		add(title);
		map = new Group();
		add(map);
		trail = new Group();
		add(trail);
		top = 14;
		float mapMax = Math.min(PixelScene.uiCamera.height * 0.7f, width * 1.2f);
		float tallest = 1, widest = 1;
		for (RunReplay.Floor f : r.floors){ widest = Math.max(widest, f.width); tallest = Math.max(tallest, f.height); }
		cell = Math.min((width - 4) / widest, mapMax / tallest);
		float bottom = top + cell * tallest + 3;
		float bw = (width - 8) / 4f;
		RedButton prev = new RedButton("< Floor", 8){ @Override protected void onClick(){ floor(floorIndex - 1); } };
		play = new RedButton("Pause", 8){ @Override protected void onClick(){ toggle(); } };
		RedButton restart = new RedButton("Restart", 8){ @Override protected void onClick(){ floor(floorIndex); } };
		RedButton next = new RedButton("Floor >", 8){ @Override protected void onClick(){ floor(floorIndex + 1); } };
		RedButton[] row = { prev, play, restart, next };
		for (int i = 0; i < row.length; i++){ row[i].setRect(2 + i * (bw + 1.33f), bottom, bw, 18); add(row[i]); }
		resize(width, (int) (bottom + 20));
		left = 0;
		floor(0);
	}

	private void toggle(){
		if (restartOnPlay(playing, shown, replay.floors.get(floorIndex).path.size())){ floor(floorIndex); return; }
		playing = !playing;
		play.text(playing ? "Pause" : "Play");
	}

	private void floor( int index ){
		floorIndex = Math.max(0, Math.min(index, replay.floors.size() - 1));
		RunReplay.Floor f = replay.floors.get(floorIndex);
		title.text(f.title() + "  (" + (floorIndex + 1) + " of " + replay.floors.size() + ")");
		title.setPos(2, 2);
		map.clear();
		trail.clear();
		left = (width - cell * f.width) / 2f;
		for (int y = 0; y < f.height; y++) for (int x = 0; x < f.width; x++){
			ColorBlock b = new ColorBlock(cell, cell, color(f.cell(x, y)));
			b.x = left + x * cell; b.y = top + y * cell;
			map.add(b);
		}
		marked = new boolean[f.cells.length()];
		hero = null;
		shown = 0;
		timer = 0;
		//set directly: going through toggle() on a floor with no recorded steps restarted the floor forever (stack overflow)
		playing = true;
		play.text("Pause");
	}

	//Play on a finished floor restarts it; a floor with no recorded steps has nothing to restart
	static boolean restartOnPlay( boolean playing, int shown, int steps ){
		return !playing && steps > 0 && shown >= steps;
	}

	private static int color( char c ){
		switch (c){
			case FloorMaps.WALL: return WALL;
			case FloorMaps.DOOR: return DOOR;
			case FloorMaps.WATER: return WATER;
			case FloorMaps.CHASM: return CHASM;
			case FloorMaps.ENTRANCE: case FloorMaps.EXIT: return STAIRS;
			default: return FLOOR;
		}
	}

	@Override
	public void update(){
		super.update();
		if (!playing) return;
		RunReplay.Floor f = replay.floors.get(floorIndex);
		timer += Game.elapsed;
		while (timer >= STEP_SECONDS && shown < f.path.size()){
			timer -= STEP_SECONDS;
			int c = f.path.get(shown++);
			if (!marked[c]){
				marked[c] = true;
				ColorBlock v = new ColorBlock(cell, cell, VISITED);
				v.x = left + (c % f.width) * cell; v.y = top + (c / f.width) * cell;
				trail.add(v);
			}
			if (hero == null){ hero = new ColorBlock(cell, cell, HERO); trail.add(hero); }
			else trail.bringToFront(hero);
			hero.x = left + (c % f.width) * cell; hero.y = top + (c / f.width) * cell;
		}
		if (shown >= f.path.size()) toggle();
	}
}
