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

import com.shatteredpixel.shatteredpixeldungeon.apd.humor.Counter;
import com.shatteredpixel.shatteredpixeldungeon.apd.humor.Frequency;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;

// APD Settings > About / Developer > "The Developer's grudge": the four counters (Hatred and Appreciation, total and heard) and the player's control over the humor.
// Opening it never triggers a popup; the first time, one small joke is shown in the text.
public class WndClericGrudge extends Window {

	private static final int WIDTH = 120;
	private static final int MARGIN = 3;
	private static final int BTN = 16;

	// the popup shown at a quiet moment: the line, a plain acknowledgement, and a way to stop them
	static Window popup( String text ){
		return new WndOptions( "DEVELOPER NOTE", text, "Fine.", "Stop these." ) {
			@Override
			protected void onSelect( int index ){
				if (index == 1) GrudgeHooks.setFrequency( Frequency.OFF );
			}
		};
	}

	private RedButton btnFrequency;

	public WndClericGrudge(){
		super();
		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock( "THE DEVELOPER'S GRUDGE", 9 );
		title.hardlight( 0xFFFF44 );
		title.maxWidth( WIDTH - 2 * MARGIN );
		title.setPos( MARGIN, pos );
		add( title );
		pos = title.bottom() + 2 * MARGIN;

		Counter c = GrudgeHooks.counter();
		String[] counters = { c.totalText(), c.heardText(), c.appreciationTotalText(), c.appreciationHeardText() };
		for (String text : counters){
			RenderedTextBlock t = PixelScene.renderTextBlock( text, 6 );
			t.maxWidth( WIDTH - 2 * MARGIN );
			t.setPos( MARGIN, pos );
			add( t );
			pos = t.bottom() + MARGIN;
		}

		String joke = GrudgeHooks.discovery();
		if (joke != null){
			RenderedTextBlock j = PixelScene.renderTextBlock( joke, 5 );
			j.maxWidth( WIDTH - 2 * MARGIN );
			j.setPos( MARGIN, pos );
			add( j );
			pos = j.bottom() + MARGIN;
		}

		btnFrequency = new RedButton( frequencyLabel() ){
			@Override
			protected void onClick(){
				GrudgeHooks.setFrequency( GrudgeHooks.frequency().next() );
				text( frequencyLabel() );
			}
		};
		btnFrequency.setRect( MARGIN, pos, WIDTH - 2 * MARGIN, BTN );
		add( btnFrequency );
		pos = btnFrequency.bottom() + MARGIN;

		CheckBox inline = new CheckBox( "Contextual jokes" ){
			@Override
			protected void onClick(){
				super.onClick();
				SPDSettings.apdGrudgeInline( checked() );
			}
		};
		inline.checked( SPDSettings.apdGrudgeInline() );
		inline.setRect( MARGIN, pos, WIDTH - 2 * MARGIN, BTN );
		add( inline );
		pos = inline.bottom() + MARGIN;

		RedButton reset = new RedButton( "Forget what I've heard" ){
			@Override
			protected void onClick(){
				ShatteredPixelDungeon.scene().addToFront( new WndOptions( "FORGET EVERYTHING?",
						"This clears the list of complaints you have heard, so they can all come back. It cannot be undone.",
						"Forget it", "Keep it" ){
					@Override
					protected void onSelect( int index ){
						if (index == 0){
							GrudgeHooks.resetHistory();
							WndClericGrudge.this.hide();
						}
					}
				} );
			}
		};
		reset.setRect( MARGIN, pos, WIDTH - 2 * MARGIN, BTN );
		add( reset );
		pos = reset.bottom() + MARGIN;

		resize( WIDTH, (int)pos );
	}

	private static String frequencyLabel(){
		return "Surprise popups: " + GrudgeHooks.frequency().label;
	}
}
