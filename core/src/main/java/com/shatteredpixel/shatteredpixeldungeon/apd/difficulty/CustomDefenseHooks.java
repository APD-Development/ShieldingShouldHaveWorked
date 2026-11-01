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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
/** Scales an already-rolled DR once. Shields, source resistances and invulnerability remain separate. */
public final class CustomDefenseHooks {
    private CustomDefenseHooks() { }
    public static int scale(Char target,int roll) {
        int percent;
        if(target instanceof Hero) percent=CustomCombatHooks.percentage(CustomDungeonRules.Slider.PLAYER_DEFENSE);
        else if(target!=null && target.alignment==Char.Alignment.ENEMY)percent=CustomCombatHooks.percentage(CustomDungeonRules.Slider.ENEMY_DEFENSE);
        else return roll;
        if(percent==100)return roll;
        return (int)Math.max(0,Math.min(Integer.MAX_VALUE,Math.round((double)roll*percent/100.0)));
    }
}
