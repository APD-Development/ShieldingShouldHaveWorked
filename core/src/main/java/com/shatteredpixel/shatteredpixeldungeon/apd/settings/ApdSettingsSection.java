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

package com.shatteredpixel.shatteredpixeldungeon.apd.settings;

/** Stable navigation categories; contains no preferences and changes no defaults. */
public enum ApdSettingsSection {
    GAMEPLAY("Gameplay", "Game speed, healing confirmations and humor."),
    RULES("Difficulty & Rules", "Healing-to-Shielding chance and Custom Dungeon Rules for new runs; inspect current rules."),
    HOTBAR("Hotbar & Controls", "Hotbar layouts, starting Waterskin position and Cleric quick-casts."),
    VISIBILITY("Visibility & Navigation", "Tile visibility, damage-over-time details and stair shortcuts."),
    HISTORY("Statistics & History", "UIMSD, recording, imports and history backups."),
    ADVANCED("Advanced", "Recording diagnostics, storage, self-tests and available developer tools.");
    public final String title,description;
    ApdSettingsSection(String title,String description){this.title=title;this.description=description;}
}
