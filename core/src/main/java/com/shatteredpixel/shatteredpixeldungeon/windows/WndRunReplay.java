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
import com.watabou.noosa.PointerArea;
import com.watabou.input.PointerEvent;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.List;

//UIMSD item 10, run replay screen: a finished run's floors one at a time, the true layout drawn as plain blocks and the hero's recorded
//steps played back in order. Facts only (RunReplay never serves a run still being played).
public class WndRunReplay extends Window {

	private static final int WALL = 0xFF3A3A3A, FLOOR = 0xFF8A7F6A, DOOR = 0xFFB07A3A, WATER = 0xFF3A6AB0, CHASM = 0xFF000000,
			STAIRS = 0xFFE0E0E0, VISITED = 0xFFE0C040, HERO = 0xFFFF3030,
			//door kinds (Pie, Nov 25): locked, crystal and hidden doors look different from ordinary ones
			LOCKED_DOOR = 0xFF8A1E1E, CRYSTAL_DOOR = 0xFF8FE3FF, HIDDEN_DOOR = 0xFF7A3FA0,
			//creatures: hostile, boss or unique, friendly NPC, ally, neutral
			HOSTILE = 0xFFFF8C1A, BOSS = 0xFFD040FF, FRIENDLY = 0xFF30D050, ALLY = 0xFF20E0C0, NEUTRAL = 0xFFFF9AD0;
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
	private Group creatures;
	private RenderedTextBlock info;
	private int creatureFrame = -1;
	private List<RunReplay.Floor.Creature> shownCreatures = new ArrayList<>();
	private TypeColors typeColors = new TypeColors(new ArrayList<String>());

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
		creatures = new Group();
		add(creatures);
		//a long title wraps to two lines; the map starts below the tallest one so it never covers text
		float titleHeight = 0;
		for (int i = 0; i < r.floors.size(); i++){
			title.text(titleOf(i));
			titleHeight = Math.max(titleHeight, title.height());
		}
		top = 2 + titleHeight + 3;
		float mapMax = Math.min(PixelScene.uiCamera.height * 0.7f, width * 1.2f);
		float tallest = 1, widest = 1;
		for (RunReplay.Floor f : r.floors){ widest = Math.max(widest, f.width); tallest = Math.max(tallest, f.height); }
		cell = Math.min((width - 4) / widest, mapMax / tallest);
		float mapBottom = top + cell * tallest;
		info = PixelScene.renderTextBlock("", 7);
		info.maxWidth(width - 4);
		add(info);
		info.text("Tap a creature to see what it is.");
		info.setPos(2, mapBottom + 2);
		float bottom = mapBottom + 4 + info.height() + 3;
		PointerArea tap = new PointerArea(0, top, width, cell * tallest){
			@Override
			protected void onClick( PointerEvent event ){
				PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
				tapped(p.x, p.y);
			}
		};
		add(tap);
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
		title.text(titleOf(floorIndex));
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
		creatures.clear();
		creatureFrame = -1;
		shownCreatures = new ArrayList<>();
		info.text(f.hasCreatures() ? "Tap a creature to see what it is." : "No creatures were recorded on this floor.");
		typeColors = new TypeColors(f.creatureTypes());
		drawCreatures(f);
		shown = 0;
		timer = 0;
		//set directly: going through toggle() on a floor with no recorded steps restarted the floor forever (stack overflow)
		playing = true;
		play.text("Pause");
	}

	private String titleOf( int index ){
		return replay.floors.get(index).title() + "  (" + (index + 1) + " of " + replay.floors.size() + ")";
	}

	//Play on a finished floor restarts it; a floor with no recorded steps has nothing to restart
	static boolean restartOnPlay( boolean playing, int shown, int steps ){
		return !playing && steps > 0 && shown >= steps;
	}

	private static int color( char c ){
		switch (c){
			case FloorMaps.WALL: return WALL;
			case FloorMaps.DOOR: return DOOR;
			case FloorMaps.LOCKED_DOOR: return LOCKED_DOOR;
			case FloorMaps.CRYSTAL_DOOR: return CRYSTAL_DOOR;
			case FloorMaps.HIDDEN_DOOR: return HIDDEN_DOOR;
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
		drawCreatures(f);
		if (shown >= f.path.size()) toggle();
	}

	//redraws the creature dots when the shown step moves to a new snapshot
	private void drawCreatures( RunReplay.Floor f ){
		if (!f.hasCreatures()) return;
		List<RunReplay.Floor.Creature> now = f.creatures(shown);
		StringBuilder key = new StringBuilder();
		for (RunReplay.Floor.Creature c : now) key.append(c.cell).append(c.kind).append(c.type).append(';');
		int frame = key.toString().hashCode();
		if (frame == creatureFrame && !shownCreatures.isEmpty() == !now.isEmpty()) return;
		creatureFrame = frame;
		shownCreatures = now;
		creatures.clear();
		float small = Math.max(1, cell * 0.7f);
		for (RunReplay.Floor.Creature c : now){
			//bosses and uniques fill the whole tile so they stand out from ordinary creatures
			float s = c.kind == 'B' ? cell : small, pad = (cell - s) / 2f;
			ColorBlock b = new ColorBlock(s, s, typeColors.color(c.type));
			b.x = left + (c.cell % f.width) * cell + pad;
			b.y = top + (c.cell / f.width) * cell + pad;
			creatures.add(b);
		}
		if (hero != null) trail.bringToFront(hero);
	}

	private void tapped( float x, float y ){
		RunReplay.Floor f = replay.floors.get(floorIndex);
		int cx = (int) Math.floor((x - left) / cell), cy = (int) Math.floor((y - top) / cell);
		if (cx < 0 || cy < 0 || cx >= f.width || cy >= f.height) return;
		RunReplay.Floor.Creature best = null;
		int bestD = Integer.MAX_VALUE;
		//tiles are tiny on a phone: take the nearest creature within one tile of the tap
		for (RunReplay.Floor.Creature c : shownCreatures){
			int d = Math.max(Math.abs(c.cell % f.width - cx), Math.abs(c.cell / f.width - cy));
			if (d <= 1 && d < bestD){ best = c; bestD = d; }
		}
		info.text(best == null ? "Nothing there." : creatureName(best.type) + " (" + kindWord(best.kind) + ")");
	}

	//Chris, Nov 25: each kind of creature (rat, gnoll, ...) has its own color. A type keeps the same color on every floor where it can;
	//when two types on one floor would share a color, the later one moves to the next free color so they never look alike.
	static final int[] PALETTE = { 0xFFFF8C1A, 0xFFD040FF, 0xFF30D050, 0xFF20E0C0, 0xFFFF9AD0, 0xFF40C8FF, 0xFFB0FF30,
			0xFFFF40A0, 0xFFC08040, 0xFF8060FF, 0xFF90FFB0, 0xFFFF7060, 0xFFA0A030, 0xFF80B0FF };

	static final class TypeColors {
		private final java.util.Map<String, Integer> colors = new java.util.HashMap<>();

		TypeColors( List<String> types ){
			boolean[] used = new boolean[PALETTE.length];
			int extra = 0;
			for (String t : types){
				if (colors.containsKey(t)) continue;
				int i = Math.floorMod(t.hashCode(), PALETTE.length), tries = 0;
				while (used[i] && tries < PALETTE.length){ i = (i + 1) % PALETTE.length; tries++; }
				if (tries < PALETTE.length){ used[i] = true; colors.put(t, PALETTE[i]); }
				//more types on one floor than colors: spread the rest around the color wheel
				else colors.put(t, hue((extra++ * 0.618034f) % 1f));
			}
		}

		//a bright color at hue h (0..1), saturation 0.6
		static int hue( float h ){
			float r = Math.abs(h * 6 - 3) - 1, g = 2 - Math.abs(h * 6 - 2), b = 2 - Math.abs(h * 6 - 4);
			r = Math.max(0, Math.min(1, r)); g = Math.max(0, Math.min(1, g)); b = Math.max(0, Math.min(1, b));
			int R = Math.round((0.4f + 0.6f * r) * 255), G = Math.round((0.4f + 0.6f * g) * 255), B = Math.round((0.4f + 0.6f * b) * 255);
			return 0xFF000000 | (R << 16) | (G << 8) | B;
		}

		int color( String type ){
			Integer c = colors.get(type);
			return c == null ? PALETTE[Math.floorMod(type.hashCode(), PALETTE.length)] : c;
		}
	}

	static int creatureColor( char kind ){
		switch (kind){
			case 'B': return BOSS;
			case 'F': return FRIENDLY;
			case 'A': return ALLY;
			case 'D': return NEUTRAL;
			default: return HOSTILE;
		}
	}

	static String kindWord( char kind ){
		switch (kind){
			case 'B': return "boss or unique";
			case 'F': return "friendly";
			case 'A': return "ally";
			case 'D': return "neutral";
			default: return "hostile";
		}
	}

	static String creatureName( String type ){
		try {
			Class<?> c = Class.forName("com.shatteredpixel.shatteredpixeldungeon." + type);
			String n = com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(c, "name");
			if (n != null && !n.isEmpty() && !n.startsWith("!!!")) return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(n);
		} catch (Throwable ignored){
		}
		String simple = type.substring(type.lastIndexOf('.') + 1);
		return simple.substring(simple.lastIndexOf('$') + 1);
	}
}
