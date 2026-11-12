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
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalChoiceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalVaultRoom;


/** Chooser presentation only. Reading the context never starts or changes an action. */
public final class SearchControlContext {
    private SearchControlContext() { }

    public enum Action { SEARCH, WALL_SEARCH, RESUME_MOVEMENT, SHOW_SAVED, FORGET, CANCEL }

    // Reuse the chooser's fixed ordering instead of allocating an array on each open.
    private static final Action[] CHOICE_ORDER = {
            Action.SEARCH, Action.WALL_SEARCH, Action.SHOW_SAVED,
            Action.RESUME_MOVEMENT, Action.FORGET, Action.CANCEL
    };

    /** Malformed restored level data must not crash a read-only chooser or reveal hidden tiles. */
    private static boolean completeObservedMetadata(Level level) {
        if (level == null || level.width() <= 0 || level.height() <= 0) return false;
        int length = level.length();
        return level.map != null && level.map.length >= length
                && level.heroFOV != null && level.heroFOV.length >= length
                && level.visited != null && level.visited.length >= length
                && level.mapped != null && level.mapped.length >= length;
    }

    public static Action preferred(Hero hero, Level level) {
        if (SPDSettings.adaptiveSearchChoice() && hero != null
                && hero.wallSearch.matches(level) && hasMeaningfulWall(hero, level)) {
            return Action.WALL_SEARCH;
        }
        return hero != null && hero.lastAction != null ? Action.RESUME_MOVEMENT : Action.SEARCH;
    }

    public static Action[] choices(Hero hero, Level level) {
        Action first = preferred(hero, level);
        boolean saved = hero != null && hero.wallSearch.exists();
        boolean wallResume = saved && DoorSearchEligibility.supportsDoors(level);
        boolean movement = hero != null && hero.lastAction != null;
        // On floors where concealed doors cannot generate, ordinary Search and
        // saved-position management remain available, but wall resume must not
        // appear merely because a previous floor left a remembered session.
        // Allocate only the caller-owned result, not a list and its backing array.
        Action[] options = new Action[2 + (saved ? 2 : 0) + (wallResume ? 1 : 0) + (movement ? 1 : 0)];
        int next = 0;
        options[next++] = first;
        for (Action action : CHOICE_ORDER) {
            if (action == Action.WALL_SEARCH && !wallResume) continue;
            if ((action == Action.SHOW_SAVED || action == Action.FORGET) && !saved) continue;
            if (action == Action.RESUME_MOVEMENT && !movement) continue;
            if (action != first) options[next++] = action;
        }
        return options;
    }

    /** A known, open part of a generated room is not a wall-search position.
     * Only observed cardinal walls can override the room-center rule; a distant
     * wall reached through Wide Search does not turn the whole room into wall mode.
     * This is read-only and never inspects hidden terrain behind a wall. */
    public static boolean openRoomInterior(Hero hero, Level level) {
        if (hero == null || !completeObservedMetadata(level)
                || hero.pos < 0 || hero.pos >= level.length()
                || !(level instanceof RegularLevel) || !((RegularLevel)level).apdHasRooms())
            return false;
        Room room = ((RegularLevel)level).room(hero.pos);
        if (room == null) return false;
        int width = level.width(), x = hero.pos % width, y = hero.pos / width;
        if (x < room.left + 2 || x > room.right - 2
                || y < room.top + 2 || y > room.bottom - 2) return false;
        // No temporary array in this frequently queried UI and Search path.
        // Keep the same north, south, west, east order and visibility checks.
        for (int direction=0; direction<4; direction++) {
            int neighbour=direction==0 ? hero.pos-width
                    : direction==1 ? hero.pos+width
                    : direction==2 ? hero.pos-1 : hero.pos+1;
            if (neighbour >= 0 && neighbour < level.length()
                    && level.heroFOV[neighbour]
                    && DoorSearchEligibility.candidate(level, neighbour)) return false;
        }
        return true;
    }

    public static boolean hasMeaningfulWall(Hero hero, Level level) {
        if (hero == null || !completeObservedMetadata(level)
                || hero.pos < 0 || hero.pos >= level.length()
                || !DoorSearchEligibility.supportsDoors(level)) return false;

        // Only the room the hero currently occupies affects this presentation decision.
        if (level instanceof RegularLevel && ((RegularLevel) level).apdHasRooms()) {
            Room room = ((RegularLevel) level).room(hero.pos);
            if (room instanceof CrystalVaultRoom || room instanceof CrystalChoiceRoom) return false;
            if (openRoomInterior(hero, level)) return false;
        }

        // Scan only the existing class/acquired-talent footprint rather than always
        // walking a Rogue rank-2 square. covers still enforces rounded corners,
        // visibility and Foresight's unchanged observed/mapped-terrain rules.
        boolean foresight = hero.buff(Foresight.class) != null;
        int radius = WallSearchSession.searchRadius(hero, foresight);
        int width = level.width();
        int x = hero.pos % width, y = hero.pos / width;
        for (int row = Math.max(0, y - radius); row <= Math.min(level.height() - 1, y + radius); row++) {
            for (int col = Math.max(0, x - radius); col <= Math.min(width - 1, x + radius); col++) {
                int cell = row * width + col;
                if (WallSearchSession.covers(hero, level, hero.pos, cell, foresight)
                        && DoorSearchEligibility.candidate(level, cell)
                        && !level.searchedWalls.confirmed(level, cell)) return true;
            }
        }
        return false;
    }
}
