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
/** Hunger advances and ordinary spawn supply; food restoration, XP and loot rewards are unchanged. */
public final class CustomResourceHooks {
    private CustomResourceHooks() { }
    public static float hungerAdvance(float value) {
        int p=CustomCombatHooks.percentage(CustomDungeonRules.Slider.HUNGER);
        return p==100 ? value : (float)((double)value*p/100.0);
    }
    public static float hungerEnergy(float energy) { return energy<0 ? -hungerAdvance(-energy) : energy; }
    public static int spawnCount(int value) {
        int p=CustomCombatHooks.percentage(CustomDungeonRules.Slider.ENEMY_SPAWN);
        if(p==100 || value<=0)return value;
        return (int)Math.max(1,Math.min(Integer.MAX_VALUE,Math.round((double)value*p/100.0)));
    }
    public static float respawnCooldown(float value) {
        int p=CustomCombatHooks.percentage(CustomDungeonRules.Slider.ENEMY_SPAWN);
        return p==100 ? value : (float)((double)value*100.0/p);
    }
}
