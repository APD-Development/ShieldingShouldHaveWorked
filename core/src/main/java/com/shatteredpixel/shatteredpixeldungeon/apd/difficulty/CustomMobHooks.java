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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
/** Apply the selected enemy or boss health once, including later summons, without scaling allies. */
public final class CustomMobHooks {
    private CustomMobHooks() { }
    public static void applyHealth(Mob mob) {
        if(mob.apdCustomHealthApplied || mob.alignment!=Char.Alignment.ENEMY) return;
        int p=CustomCombatHooks.percentage(Char.hasProp(mob,Char.Property.BOSS)
            ? CustomDungeonRules.Slider.BOSS_HP : CustomDungeonRules.Slider.ENEMY_HP);
        if(p==100)return;
        mob.HT=(int)Math.max(1,Math.min(Integer.MAX_VALUE,Math.round((double)mob.HT*p/100.0)));
        mob.HP=(int)Math.max(0,Math.min(mob.HT,Math.round((double)mob.HP*p/100.0)));
        mob.apdCustomHealthApplied=true;
    }
}
