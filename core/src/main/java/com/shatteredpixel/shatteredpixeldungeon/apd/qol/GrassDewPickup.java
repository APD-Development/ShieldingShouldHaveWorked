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
package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdItems;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/** One-shot, unsaved pickup request for dew just generated beneath the hero by grass. */
public final class GrassDewPickup {
    private Level level;
    private Heap heap;
    private Dewdrop dew;
    private HeroAction sourceAction;
    private int cell;

    public void offer(Hero hero, Level level, int cell, Heap heap) {
        clear();
        if (!SPDSettings.autoCollectGrassDew() || hero != Dungeon.hero || level != Dungeon.level
                || hero == null || level == null || hero.pos != cell || heap == null || heap.pos != cell
                || heap.type != Heap.Type.HEAP || level.heaps.get(cell) != heap || !hasRoom(hero)) return;
        // Grass uses ordinary drop/merge rules. Remember the actual resulting dew, not its stack index.
        for (Item item : heap.items) {
            if (item instanceof Dewdrop && item.quantity() > 0) {
                this.level = level;
                this.cell = cell;
                this.heap = heap;
                this.dew = (Dewdrop) item;
                this.sourceAction = hero.curAction;
                return;
            }
        }
    }

    public void clear() {
        level = null;
        heap = null;
        dew = null;
        sourceAction = null;
    }

    /** Called after the next normal hero-turn FOV update, before continuing a travel action. */
    public boolean collectIfSafe(Hero hero) {
        Level pendingLevel = level;
        Heap pendingHeap = heap;
        Dewdrop pendingDew = dew;
        HeroAction pendingAction = sourceAction;
        int pendingCell = cell;
        clear(); // Cancelled/failed requests must never retry on a later turn or a different item.
        if (pendingDew == null || !SPDSettings.autoCollectGrassDew() || hero != Dungeon.hero
                || pendingLevel != Dungeon.level || hero == null || !hero.isAlive()
                || hero.paralysed > 0 || hero.resting || hero.pos != pendingCell
                || (hero.curAction != null && hero.curAction != pendingAction)
                || pendingLevel.heaps.get(pendingCell) != pendingHeap
                || pendingHeap.type != Heap.Type.HEAP || pendingHeap.hidden || !pendingHeap.items.contains(pendingDew)
                || pendingDew.quantity() <= 0 || !hasRoom(hero)) return false;
        if (!SPDSettings.autoCollectDewWithEnemies() && hasVisibleEnemy(pendingLevel)) return false;
        // Reuse normal discovery, vial filling, sound, feedback and exactly one pickupDelay().
        // Never call consumeDew when the Waterskin is full or absent.
        ApdItems.noteAcquireContext(ItemEvents.ACQ_PICKUP);
        if (!pendingDew.doPickUp(hero, pendingCell)) return false;
        pendingHeap.remove(pendingDew); // Other loot (including a seed above the dew) stays untouched.
        return true;
    }

    private static boolean hasRoom(Hero hero) {
        Waterskin skin = hero.belongings.getItem(Waterskin.class);
        return skin != null && !skin.isFull();
    }

    private static boolean hasVisibleEnemy(Level level) {
        // Same known-FOV/alignment criterion as Hero.checkVisibleMobs, without scanning unseen contents.
        for (Mob mob : level.mobs) {
            if (mob.alignment == Char.Alignment.ENEMY && mob.isAlive() && mob.pos >= 0
                    && mob.pos < level.heroFOV.length && level.heroFOV[mob.pos]) return true;
        }
        return false;
    }
}
