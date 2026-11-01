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

package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
/** Retain vanilla boss phase fractions when their original thresholds use fixed HP amounts. */
public final class CustomBossHealthHooks {
    private CustomBossHealthHooks() { }
    public static int threshold(int original) {
        int p=CustomCombatHooks.percentage(CustomDungeonRules.Slider.BOSS_HP);
        if(p==100 || original<=0)return original;
        return (int)Math.max(1,Math.min(Integer.MAX_VALUE,Math.round((double)original*p/100.0)));
    }
}
