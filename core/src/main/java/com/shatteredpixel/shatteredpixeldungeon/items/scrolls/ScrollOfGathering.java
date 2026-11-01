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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.apd.loot.LootTransfers;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** Working-name prototype; test-build acquisition uses approved provisional Teleportation weights. */
public class ScrollOfGathering extends Scroll {
    { icon = -1; }
    @Override public void reset() {
        super.reset();
        // A fixed, always-known scroll: never borrow a randomized vanilla rune.
        image = ItemSpriteSheet.SCROLL_GATHERING;
    }
    @Override public boolean isKnown() { return true; }
    @Override public void setKnown() { }
    @Override public String name() { return "Scroll of Gathering"; }
    @Override public String desc() {
        return "Gathers loose ground items on this floor into one pile beside the downstairs. "
                + "Leaves unopened containers, shop stock, keys, quest items and lost backpacks untouched. "
                + "If there is no safe destination, the scroll is not consumed.";
    }
    protected boolean sendsAhead() { return false; }
    @Override public void doRead() {
        if (curUser == null || (!anonymous && !curUser.belongings.backpack.contains(this)) || quantity() < 1) return;
        String failure = LootTransfers.cast(sendsAhead(), () -> {
            if (!anonymous) detach(curUser.belongings.backpack);
            identify();
            readAnimation();
        });
        if (failure != null) GLog.w(failure);
        else GLog.i(sendsAhead() ? "Your loose loot is bound for the shop outside the next region."
                : "Your loose loot gathers beside the downstairs.");
    }
}
