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


package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.community.CommunityPreview;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.watabou.noosa.Camera;
import java.util.ArrayList;
import java.util.List;

// Separate from UIMSD and its unlock gate; reuse the game's wrapped, scrolling list UI.
public class CommunityScene extends PixelScene {
    private static CommunityPreview.Page selected;
    public static void open() {
        selected = null;
        ShatteredPixelDungeon.switchNoFade(CommunityScene.class);
    }
    @Override public void create() {
        super.create();
        add(new TitleBackground(Camera.main.width, Camera.main.height));
        showPage();
    }
    private void showPage() {
        List<WndUimsdList.Entry> entries = new ArrayList<>();
        String title;
        String message;
        if (selected == null) {
            title = "Community";
            message = "Offline alpha preview: explore APD's planned community hub. Online features are not available here yet.";
            for (CommunityPreview.Page page : CommunityPreview.Page.values())
                entries.add(WndUimsdList.Entry.item(page.title, page.summary));
        } else {
            title = selected.title;
            message = "Planned feature — offline preview";
            entries.add(WndUimsdList.Entry.note(selected.explanation));
            if (selected == CommunityPreview.Page.SUBMISSIONS) {
                entries.add(WndUimsdList.Entry.heading("Submission"));
                entries.add(WndUimsdList.Entry.note("The author's idea and description will appear here. This preview contains no example pitches or player submissions."));
                entries.add(WndUimsdList.Entry.heading("Comments and replies — planned"));
                entries.add(WndUimsdList.Entry.note(CommunityPreview.COMMENTS));
                entries.add(WndUimsdList.Entry.heading("Comment threads"));
                entries.add(WndUimsdList.Entry.note(CommunityPreview.COMMENT_PLACEHOLDER));
            }
            entries.add(WndUimsdList.Entry.heading("Availability"));
            entries.add(WndUimsdList.Entry.note(selected.availability()));
        }
        entries.add(WndUimsdList.Entry.item(selected == null ? "Main menu" : "Back to Community"));
        addToFront(new WndUimsdList(title, message, false, entries, (index, checked) -> {
            if (selected == null && index < CommunityPreview.Page.values().length) {
                selected = CommunityPreview.Page.values()[index];
                showPage();
            } else onBackPressed();
        }) {
            @Override public void onBackPressed() {
                hide();
                CommunityScene.this.onBackPressed();
            }
        });
    }
    @Override protected void onBackPressed() {
        if (selected != null) { selected = null; showPage(); }
        else ShatteredPixelDungeon.switchNoFade(TitleScene.class);
    }
}
