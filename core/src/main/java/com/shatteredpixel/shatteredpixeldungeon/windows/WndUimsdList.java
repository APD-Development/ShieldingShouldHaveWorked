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
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.List;

//APD: the one list window all the UIMSD pickers use (columns, operators, values, views, options and so on). Plain lines of text,
//scrolled when there are many, tapped with the same coordinate test the game's own scrolling lists use. A list is either a
//menu (tapping an item closes the window and reports it) or a set of switches (tapping toggles in place, "Done" closes).
public class WndUimsdList extends Window {

	public interface Picker {
		void onPick( int index, boolean checked );
	}

	private static final int MARGIN = 2;

	private final Picker picker;
	private final boolean switches;
	private final List<String> labels;
	private final List<Boolean> checks;
	private final List<RenderedTextBlock> texts = new ArrayList<>();
	private final List<float[]> spans = new ArrayList<>();   //top and height of each row inside the list
	private ListPane pane;

	//menu: checks == null. switches: one entry per label (the window stays open), an extra "Done" closes it.
	public WndUimsdList( String title, String message, List<String> labels, List<Boolean> checks, Picker picker ){
		super();
		this.picker = picker;
		this.switches = checks != null;
		this.labels = new ArrayList<>(labels);
		this.checks = checks == null ? null : new ArrayList<>(checks);
		int width = windowWidth();

		float pos = MARGIN;
		if (title != null && !title.isEmpty()){
			RenderedTextBlock t = PixelScene.renderTextBlock(title, 9);
			t.hardlight(TITLE_COLOR);
			t.maxWidth(width - 2 * MARGIN);
			t.setPos(MARGIN, pos);
			add(t);
			pos = t.bottom() + 2 * MARGIN;
		}
		if (message != null && !message.isEmpty()){
			RenderedTextBlock m = PixelScene.renderTextBlock(message, 6);
			m.maxWidth(width - 2 * MARGIN);
			m.setPos(MARGIN, pos);
			add(m);
			pos = m.bottom() + 2 * MARGIN;
		}

		//The window is sized BEFORE the list is placed: resizing a window moves its camera, and a scroll pane works out where it is on
		//screen when it is placed, so placing it first leaves its contents offset from the window.
		pane = new ListPane();
		Component content = pane.content();
		int n = labels.size() + (switches ? 1 : 0);
		float[] heights = new float[n];
		List<RenderedTextBlock> made = new ArrayList<>();
		for (int i = 0; i < n; i++){
			String text = i < labels.size() ? line(i) : "Done";
			RenderedTextBlock t = PixelScene.renderTextBlock(text, 7);
			t.maxWidth(WindowGeometry.rowTextWidth(width));
			heights[i] = WindowGeometry.rowHeight(t.height());
			made.add(t);
		}
		float[] tops = WindowGeometry.stack(heights);
		for (int i = 0; i < n; i++){
			RenderedTextBlock t = made.get(i);
			ColorBlock bg = new ColorBlock(width, heights[i] - 1, i < labels.size() ? 0x66332B27 : 0x66553322);
			bg.y = tops[i];
			content.add(bg);
			t.setPos(WindowGeometry.ROW_PAD + 1, tops[i] + (heights[i] - 1 - t.height()) / 2f);
			content.add(t);
			texts.add(t);
			spans.add(new float[]{tops[i], heights[i]});
		}
		content.setSize(width, tops[n]);
		float h = WindowGeometry.paneHeight(tops[n], WindowGeometry.maxPane(PixelScene.uiCamera.height, pos, 0));
		resize(width, (int) (pos + h));
		add(pane);
		pane.setRect(0, pos, width, h);
	}

	//the width every UIMSD dialog uses on this screen
	public static int windowWidth(){
		return WindowGeometry.width(PixelScene.landscape(), PixelScene.uiCamera.width);
	}

	private String line( int i ){
		if (!switches) return labels.get(i);
		return (checks.get(i) ? "[x] " : "[ ] ") + labels.get(i);
	}

	private class ListPane extends ScrollPane {

		ListPane(){
			super(new Component());
		}

		@Override
		public void onClick( float x, float y ){
			for (int i = 0; i < spans.size(); i++){
				float[] s = spans.get(i);
				if (y >= s[0] && y < s[0] + s[1]){
					picked(i);
					return;
				}
			}
		}
	}

	private void picked( int i ){
		if (switches){
			if (i >= labels.size()){
				hide();
				return;
			}
			checks.set(i, !checks.get(i));
			texts.get(i).text(line(i));
			if (picker != null) picker.onPick(i, checks.get(i));
		} else {
			hide();
			if (picker != null) picker.onPick(i, false);
		}
	}
}
