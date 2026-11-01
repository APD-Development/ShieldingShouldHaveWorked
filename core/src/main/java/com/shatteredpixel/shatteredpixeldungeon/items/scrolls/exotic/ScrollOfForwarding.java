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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic;

import com.shatteredpixel.shatteredpixeldungeon.apd.loot.LootTransfers;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** Approved provisional exotic: six-energy conversion and generation only through depth 19. */
public class ScrollOfForwarding extends ExoticScroll {
    { image = ItemSpriteSheet.SCROLL_KAUNAN + 16; icon = ItemSpriteSheet.Icons.SCROLL_TELEPORT; }
    @Override public boolean isKnown() { return true; }
    @Override public void reset() {
        // Match Item.reset without asking the twelve-rune handler about this independent prototype.
        keptThoughLostInvent = false; apdUid = null; apdCopyOf = null;
        image = ItemSpriteSheet.SCROLL_KAUNAN + 16; rune = "KAUNAN";
    }
    @Override public void setKnown() { }
    @Override public String name() { return "Scroll of Forwarding"; }
    @Override public String desc() {
        return "Sends loose ground loot to one ordinary pile immediately outside the next region's shop entrance. "
                + "The current region's shop is skipped; on floor 20 or later the destination is the Imp's floor-20 shop. "
                + "Items wait safely if the shop is not open or its approach is blocked. Nothing is sold. "
                + "Unopened containers, shop stock, carried items, keys, quest items and a Lost Backpack stay put. "
                + "Armed bombs prevent casting.";
    }
    @Override public void doRead() {
        if (curUser == null || (!anonymous && !curUser.belongings.backpack.contains(this)) || quantity() < 1) return;
        String failure = LootTransfers.cast(true, () -> {
            if (!anonymous) detach(curUser.belongings.backpack);
            identify();
            readAnimation();
        });
        if (failure != null) GLog.w(failure);
        else GLog.i("Your loose loot has been forwarded to the destination shop.");
    }
}
