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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.CellText;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdHome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.List;

//APD: the scrolling cards of the UIMSD Home page. It draws whatever UimsdHome it is given and reports which button was tapped; taps are
//tested against the buttons' rectangles in content coordinates, the way the table tests its rows.
public class UimsdHomePane extends ScrollPane {

	public interface Listener {
		void onLink( String viewId );
	}

	private static final float PAD = 3f;
	private static final int CARD_COLOR = 0x55000000;
	private static final int TITLE_COLOR = 0xFFFF88;
	private static final int HEADING_COLOR = 0xAAAAAA;
	private static final int BUTTON_COLOR = 0xCC7A2A2A;

	private Listener listener;
	private UimsdHome home;
	private float builtWidth = -1;
	private final List<float[]> boxes = new ArrayList<>();
	private final List<String> boxViews = new ArrayList<>();

	public UimsdHomePane(){
		super(new Component());
	}

	public void setListener( Listener l ){
		listener = l;
	}

	public void setHome( UimsdHome h ){
		home = h;
		builtWidth = -1;
		layout();
	}

	@Override
	protected void layout(){
		super.layout();
		if (home != null && width > 0 && builtWidth != width){
			rebuild();
			builtWidth = width;
		}
	}

	//where the button that opens a view is, in content coordinates {x, y, w, h}, or null: the tour points at it
	public float[] linkBox( String viewId ){
		for (int i = 0; i < boxes.size(); i++) if (boxViews.get(i).equals(viewId)) return boxes.get(i);
		return null;
	}

	public float contentHeight(){
		return content.height();
	}

	@Override
	public void onClick( float x, float y ){
		if (listener == null) return;
		for (int i = 0; i < boxes.size(); i++){
			float[] b = boxes.get(i);
			if (x >= b[0] && x <= b[0] + b[2] && y >= b[1] && y <= b[1] + b[3]){
				listener.onLink(boxViews.get(i));
				return;
			}
		}
	}

	private void rebuild(){
		content.clear();
		boxes.clear();
		boxViews.clear();
		float w = width;
		float y = 0;
		for (UimsdHome.Card card : home.cards){
			float top = y;
			ColorBlock bg = new ColorBlock(w, 10, CARD_COLOR);
			bg.y = top;
			content.add(bg);
			ColorBlock accent = new ColorBlock(2, 10, UimsdStyle.HEADING);
			accent.y = top;
			content.add(accent);
			y += PAD;
			RenderedTextBlock title = PixelScene.renderTextBlock(card.title, UimsdStyle.SIZE_TEXT + 1);
			title.hardlight(UimsdStyle.TITLE);
			title.maxWidth((int) (w - 2 * PAD - 2));
			title.setPos(PAD + 2, y);
			content.add(title);
			y = title.bottom() + 1;
			ColorBlock rule = new ColorBlock(w - 2 * PAD - 2, 1, UimsdStyle.RULE);
			rule.x = PAD + 2;
			rule.y = y;
			content.add(rule);
			y += 3;
			//labels in grey on the left, values in the colour of what kind of knowledge they are on the right, with the small print under the value
			float labelW = (w - 2 * PAD - 2) * 0.46f, valueW = (w - 2 * PAD - 2) - labelW - 2;
			for (UimsdHome.Line line : card.lines){
				if (line.heading){
					RenderedTextBlock h = PixelScene.renderTextBlock(line.label.toUpperCase(), UimsdStyle.SIZE_META);
					h.hardlight(UimsdStyle.HEADING);
					h.maxWidth((int) (w - 2 * PAD - 2));
					h.setPos(PAD + 2, y + 1);
					content.add(h);
					y = h.bottom() + 2;
					continue;
				}
				RenderedTextBlock label = PixelScene.renderTextBlock(line.label, UimsdStyle.SIZE_HELP);
				label.hardlight(UimsdStyle.MUTED);
				label.maxWidth((int) labelW);
				label.setPos(PAD + 2, y);
				content.add(label);
				RenderedTextBlock value = PixelScene.renderTextBlock(line.value, UimsdStyle.SIZE_HELP);
				value.hardlight(UimsdTable.color(line.state));
				value.maxWidth((int) valueW);
				value.setPos(PAD + 2 + labelW + 2, y);
				content.add(value);
				float lineH = Math.max(label.height(), value.height());
				if (!line.detail.isEmpty()){
					RenderedTextBlock detail = PixelScene.renderTextBlock(line.detail, UimsdStyle.SIZE_META);
					detail.hardlight(UimsdStyle.FAINT);
					detail.maxWidth((int) valueW);
					detail.setPos(PAD + 2 + labelW + 2, y + value.height() + 0.5f);
					content.add(detail);
					lineH = Math.max(label.height(), value.height() + 0.5f + detail.height());
				}
				y += lineH + 2;
			}
			y += 2;
			float bx = PAD;
			float rowTop = y;
			for (UimsdHome.Link link : card.links){
				float bw = link.label.length() * 4.4f + 10;
				if (bx + bw > w - PAD && bx > PAD){
					bx = PAD;
					rowTop += 15;
				}
				ColorBlock b = new ColorBlock(bw, 13, BUTTON_COLOR);
				b.x = bx;
				b.y = rowTop;
				content.add(b);
				RenderedTextBlock lt = PixelScene.renderTextBlock(link.label, 6);
				lt.hardlight(0xFFFFFF);
				lt.setPos(bx + (bw - lt.width()) / 2f, rowTop + (13 - lt.height()) / 2f);
				content.add(lt);
				boxes.add(new float[]{ bx, rowTop, bw, 13 });
				boxViews.add(link.viewId);
				bx += bw + 3;
			}
			y = rowTop + (card.links.isEmpty() ? 0 : 15) + PAD;
			bg.size(w, y - top);
			accent.size(2, y - top);
			y += 3;
		}
		content.setSize(w, y);
	}
}
