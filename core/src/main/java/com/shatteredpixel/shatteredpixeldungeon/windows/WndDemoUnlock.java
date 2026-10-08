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
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

// The demo-unlock warning, presented as a pop-up card in front of everything (it is NOT an advertisement and carries nothing but the
// warning and its two choices). It only ever appears when a player with fewer than ten genuine runs taps the locked UIMSD button. There is
// no close button; Back means "I'LL PLAY 10 RUNS".
public class WndDemoUnlock extends Window {

	public interface Choice {
		void confusion();
		void playRuns();
	}

	private static final int WIDTH_P = 126, WIDTH_L = 170, MARGIN = 6, BUTTON_HEIGHT = 22;

	private final Choice choice;

	public WndDemoUnlock( Choice choice ){
		super();
		this.choice = choice;
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		RenderedTextBlock title = PixelScene.renderTextBlock(UimsdGate.WARNING_TITLE, 10);
		title.hardlight(0xFF5555);
		title.maxWidth(width - MARGIN * 2);
		title.setPos((width - title.width()) / 2f, pos);
		add(title);
		pos = title.bottom() + MARGIN;

		RenderedTextBlock body = PixelScene.renderTextBlock(6);
		body.text(UimsdGate.WARNING_TEXT, width - MARGIN * 2);
		body.setPos(MARGIN, pos);
		add(body);
		pos = body.bottom() + MARGIN * 2;

		RedButton confusion = new RedButton(UimsdGate.CHOICE_CONFUSION){
			@Override
			protected void onClick(){
				hideCard();
				WndDemoUnlock.this.choice.confusion();
			}
		};
		confusion.setRect(MARGIN, pos, width - MARGIN * 2, BUTTON_HEIGHT);
		add(confusion);
		pos = confusion.bottom() + MARGIN / 2f;

		RedButton play = new RedButton(UimsdGate.CHOICE_PLAY){
			@Override
			protected void onClick(){
				hideCard();
				WndDemoUnlock.this.choice.playRuns();
			}
		};
		play.setRect(MARGIN, pos, width - MARGIN * 2, BUTTON_HEIGHT);
		add(play);
		pos = play.bottom() + MARGIN;

		resize(width, (int) pos);
	}

	private void hideCard(){
		hide();
	}

	@Override
	public void onBackPressed(){
		hide();
		choice.playRuns();
	}
}
