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

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/** Native controls; viewing or cancelling never changes the preference. */
public final class WndHealingConfirmation {
    private WndHealingConfirmation() {}
    public static void settings() {
        int current = SPDSettings.healingConfirmationThreshold();
        ShatteredPixelDungeon.scene().addToFront(new WndOptions("Healing confirmation",
                "Identified Healing potions only. Current: " + (current < 0 ? "Off (one tap)." : current + "% HP.")
                        + " At or above the threshold, confirm before drinking.", "Choose HP threshold", "Off (one tap)", "Cancel") {
            @Override protected void onSelect(int index) {
                if (index == 1) SPDSettings.healingConfirmationThreshold(-1);
                else if (index == 0) ShatteredPixelDungeon.scene().addToFront(new ThresholdWindow());
            }
        });
    }
    public static final class ThresholdWindow extends Window {
        private final OptionSlider percentage;
        private final RenderedTextBlock value;
        public ThresholdWindow() {
            super();
            final int width=140;
            value=PixelScene.renderTextBlock("Healing confirmation",9);
            value.hardlight(TITLE_COLOR);value.maxWidth(width);value.setPos(0,0);add(value);
            percentage=new OptionSlider("HP threshold", "0%", "100%",0,100) {
                @Override protected void onChange() { value.text("Confirm at " + getSelectedValue() + "% HP"); }
            };
            int current=SPDSettings.healingConfirmationThreshold();
            percentage.setSelectedValue(current<0?50:current);
            value.text("Confirm at " + percentage.getSelectedValue() + "% HP");
            percentage.setRect(0,value.bottom()+4,width,30);add(percentage);
            RenderedTextBlock note=PixelScene.renderTextBlock(
                    "Below this HP percentage, drink with one tap. Potions are never used automatically.",6);
            note.maxWidth(width);note.setPos(0,percentage.bottom()+4);add(note);
            RedButton save=new RedButton("Save") {
                @Override protected void onClick() { SPDSettings.healingConfirmationThreshold(percentage.getSelectedValue());hide(); }
            };
            RedButton cancel=new RedButton("Cancel") {
                @Override protected void onClick() { hide(); }
            };
            save.setRect(0,note.bottom()+4,68,18);cancel.setRect(72,note.bottom()+4,68,18);
            add(save);add(cancel);resize(width,(int)save.bottom());
        }
    }
}
