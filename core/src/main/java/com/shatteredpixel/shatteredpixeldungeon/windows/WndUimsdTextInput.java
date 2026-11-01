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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;

//A UIMSD input keeps its typed value and callback when the scene is rebuilt.
public class WndUimsdTextInput extends WndTextInput {
    public interface Selection { void select(boolean positive, String value); }

    private final String title, body, positiveText, negativeText;
    private final int limit;
    private final boolean multiline;
    private final Selection selection;

    public WndUimsdTextInput(String title, String body, String initial, int limit,
                             boolean multiline, String positive, String negative, Selection selection){
        super(title, body, initial, limit, multiline, positive, negative);
        this.title = title;
        this.body = body;
        this.limit = limit;
        this.multiline = multiline;
        positiveText = positive;
        negativeText = negative;
        this.selection = selection;
    }

    @Override protected int inputWidth(boolean multiline, boolean hasBody){
        return WndUimsdList.windowWidth();
    }

    @Override protected boolean buttonsUseFullWidth(){ return true; }

    @Override public void onSelect(boolean positive, String value){
        if (selection != null) selection.select(positive, value);
    }

    @Override public Runnable restoreOnSceneReset(){
        final String savedTitle = title, savedBody = body, savedText = textBox.getText();
        final String positive = positiveText, negative = negativeText;
        final int savedLimit = limit;
        final boolean savedMultiline = multiline;
        final Selection savedSelection = selection;
        return () -> ShatteredPixelDungeon.scene().addToFront(new WndUimsdTextInput(savedTitle,
                savedBody, savedText, savedLimit, savedMultiline, positive, negative, savedSelection));
    }
}
