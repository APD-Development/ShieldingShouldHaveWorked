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

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;

import java.util.ArrayList;

/** Chooser presentation only. Reading the context never starts or changes an action. */
public final class SearchControlContext {
    private SearchControlContext() { }

    public enum Action { SEARCH, WALL_SEARCH, RESUME_MOVEMENT, SHOW_SAVED, FORGET, CANCEL }

    // Reuse the chooser's fixed ordering instead of allocating an array on each open.
    private static final Action[] CHOICE_ORDER = {
            Action.SEARCH, Action.WALL_SEARCH, Action.SHOW_SAVED,
            Action.RESUME_MOVEMENT, Action.FORGET, Action.CANCEL
    };

    public static Action preferred(Hero hero, Level level) {
        if (SPDSettings.adaptiveSearchChoice() && hero != null
                && hero.wallSearch.matches(level) && hasMeaningfulWall(hero, level)) {
            return Action.WALL_SEARCH;
        }
        return hero != null && hero.lastAction != null ? Action.RESUME_MOVEMENT : Action.SEARCH;
    }

    public static Action[] choices(Hero hero, Level level) {
        ArrayList<Action> choices = new ArrayList<>();
        choices.add(preferred(hero, level));
        boolean saved = hero != null && hero.wallSearch.exists();
        // A remembered wall search is not proof that the current room has a
        // searchable wall. Keep saved-position management, but do not offer
        // Wall Search in open interiors, gated rooms, or exhausted contexts.
        boolean eligibleWall = saved && hero.wallSearch.matches(level) && hasMeaningfulWall(hero, level);
        for (Action action : CHOICE_ORDER) {
            if (action == Action.WALL_SEARCH && !eligibleWall) continue;
            if ((action == Action.SHOW_SAVED || action == Action.FORGET) && !saved) continue;
            if (action == Action.RESUME_MOVEMENT && (hero == null || hero.lastAction == null)) continue;
            if (!choices.contains(action)) choices.add(action);
        }
        return choices.toArray(new Action[0]);
    }

    /** Rooms whose walls can never hide a door from the inside.
     * Hidden doors only replace REGULAR doors between connected rooms. Every
     * special room (key/crystal vaults, wells, shops, altars, labs, gardens,
     * secret rooms, ...) has exactly one entrance, and the hero is standing
     * inside it, so that entrance is already known. Standard rooms and
     * hallways can still hide doors and stay eligible. */
    public static boolean noHiddenDoorRoom(Level level, int heroPos) {
        if (!(level instanceof RegularLevel) || !((RegularLevel) level).apdHasRooms()
                || heroPos < 0 || heroPos >= level.length()) return false;
        Room room = ((RegularLevel) level).room(heroPos);
        return room instanceof SpecialRoom;
    }

    public static boolean hasMeaningfulWall(Hero hero, Level level) {
        if (hero == null || level == null || hero.pos < 0 || hero.pos >= level.length()
                || !DoorSearchEligibility.supportsDoors(level)) return false;
        // A gated special room is never an adaptive wall-search context.
        // Keep ordinary paid Search and discovered-door evidence unchanged.
        if (noHiddenDoorRoom(level, hero.pos)) return false;
        // Room middles are handled by reach: Wall Search is only meaningful when
        // a door-capable wall lies inside the hero's real search footprint
        // (1 tile, 2 for Rogue, +1 with Wide Search). Otherwise it is a normal Search.

        // Bound the scan to the existing maximum footprint; covers retains the exact class,
        // acquired talent rank, rounded corners, visibility, and Foresight rules.
        int radius = hero.buff(Foresight.class) != null ? Foresight.DISTANCE : 3;
        int width = level.width();
        int x = hero.pos % width, y = hero.pos / width;
        for (int row = Math.max(0, y - radius); row <= Math.min(level.height() - 1, y + radius); row++) {
            for (int col = Math.max(0, x - radius); col <= Math.min(width - 1, x + radius); col++) {
                int cell = row * width + col;
                if (WallSearchSession.covers(hero, level, hero.pos, cell)
                        && DoorSearchEligibility.candidate(level, cell)
                        && !level.searchedWalls.confirmed(level, cell)) return true;
            }
        }
        return false;
    }
}
