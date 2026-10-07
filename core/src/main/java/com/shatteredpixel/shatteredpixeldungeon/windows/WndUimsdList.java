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

	public static final int WIDTH_P = 130, WIDTH_L = 190;
	private static final int MARGIN = 2, PAD = 3;

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
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

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

		pane = new ListPane();
		add(pane);
		Component content = pane.content();
		float y = 0;
		int n = labels.size() + (switches ? 1 : 0);
		for (int i = 0; i < n; i++){
			String text = i < labels.size() ? line(i) : "Done";
			RenderedTextBlock t = PixelScene.renderTextBlock(text, 7);
			t.maxWidth(width - 2 * PAD - 4);
			float h = Math.max(15, t.height() + 2 * PAD);
			ColorBlock bg = new ColorBlock(width, h - 1, i < labels.size() ? 0x66332B27 : 0x66553322);
			bg.y = y;
			content.add(bg);
			t.setPos(PAD + 1, y + (h - 1 - t.height()) / 2f);
			content.add(t);
			texts.add(t);
			spans.add(new float[]{y, h});
			y += h;
		}
		content.setSize(width, y);
		float maxH = PixelScene.uiCamera.height * 0.78f - pos;
		float h = Math.min(y, Math.max(40, maxH));
		pane.setRect(0, pos, width, h);
		resize(width, (int) (pos + h));
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
