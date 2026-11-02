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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/** Compact native settings: no extra full-width explanation or checkbox rows on the parent tab. */
public class WndGrassDewSettings extends Window {
    public WndGrassDewSettings() {
        super();
        int width = 122;
        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.hardlight(TITLE_COLOR);
        title.maxWidth(width);
        add(title);
        RenderedTextBlock description = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
        description.maxWidth(width);
        description.setPos(0, title.bottom() + 4);
        add(description);
        final CheckBox withEnemies = new CheckBox(Messages.get(this, "with_enemies")) {
            @Override protected void onClick() {
                super.onClick();
                SPDSettings.autoCollectDewWithEnemies(checked());
            }
        };
        CheckBox enabled = new CheckBox(Messages.get(this, "enabled")) {
            @Override protected void onClick() {
                super.onClick();
                SPDSettings.autoCollectGrassDew(checked());
                withEnemies.enable(checked());
            }
        };
        enabled.checked(SPDSettings.autoCollectGrassDew());
        withEnemies.checked(SPDSettings.autoCollectDewWithEnemies());
        withEnemies.enable(enabled.checked());
        enabled.setRect(0, description.bottom() + 4, width, 16);
        withEnemies.setRect(0, enabled.bottom() + 2, width, 16);
        add(enabled);
        add(withEnemies);
        resize(width, (int) withEnemies.bottom());
    }
}
