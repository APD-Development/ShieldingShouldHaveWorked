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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;

/** Permission belongs to one exact voluntary step, trap, level and action; never serialized. */
public final class TrapStepPermit {
    private Level level; private Trap trap; private HeroAction action; private int from,to;
    public void allow(Level level,Trap trap,HeroAction action,int from,int to){this.level=level;this.trap=trap;this.action=action;this.from=from;this.to=to;}
    public void clear(){level=null;trap=null;action=null;}
    public boolean consume(Level level,Trap trap,HeroAction action,int from,int to){
        boolean allowed=this.level==level && this.trap==trap && this.action==action && this.from==from && this.to==to && this.action!=null;
        clear();return allowed;
    }
    public static boolean needsWarning(boolean enabled,Level level,Hero hero,int step){
        if(!enabled || level==null || hero==null || hero.flying || hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness.class)!=null || step<0 || step>=level.length())return false;
        Trap trap=level.traps.get(step);
        // A revealed TRAP tile is SPD's identified trap representation. Knowledge in fog is insufficient.
        return trap!=null && trap.visible && trap.active && level.map[step]==Terrain.TRAP
                && level.heroFOV!=null && level.heroFOV[step];
    }
}
