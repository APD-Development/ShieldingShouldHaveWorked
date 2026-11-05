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
import com.shatteredpixel.shatteredpixeldungeon.levels.*;

/** Public generation rules and observed terrain only. Never asks whether a secret actually exists. */
public final class DoorSearchEligibility {
    private DoorSearchEligibility() { }
    public static boolean wallDisguise(int tile) {
        return tile==Terrain.WALL || tile==Terrain.WALL_DECO || tile==Terrain.SECRET_DOOR;
    }
    public static boolean supportsDoors(Level level,int depth,int branch) {
        if(level==null)return false;
        // These public main-dungeon floors have no hidden-door generator. Floor 5 is deliberately NOT excluded.
        if(branch==0&&(depth==1||depth==10||depth==15||depth==20||depth==25||depth==26))return false;
        // Scripted maps and the vault have no hidden doors. Mining paints concealed connections as diggable WALL, not SECRET_DOOR.
        return !(level instanceof PrisonBossLevel || level instanceof CavesBossLevel || level instanceof CityBossLevel
                || level instanceof HallsBossLevel || level instanceof LastLevel || level instanceof DeadEndLevel
                || level instanceof VaultLevel || level instanceof MiningLevel);
    }
    public static boolean supportsDoors(Level level){return supportsDoors(level,Dungeon.depth,Dungeon.branch);}
    private static boolean known(Level l,int p){return p>=0&&p<l.length()&&(l.heroFOV[p]||l.visited[p]||l.mapped[p]);}
    private static boolean frame(Level l,int p){
        if(!known(l,p))return true;
        int t=l.map[p];
        return wallDisguise(t)||t==Terrain.DOOR||t==Terrain.OPEN_DOOR||t==Terrain.LOCKED_DOOR
                ||t==Terrain.HERO_LKD_DR||t==Terrain.CRYSTAL_DOOR||t==Terrain.BARRICADE;
    }
    private static boolean passage(Level l,int p){
        // Unobserved terrain is unknown, never a licence to inspect the actual map behind the wall.
        return !known(l,p)||(Terrain.flags[l.map[p]]&Terrain.SOLID)==0;
    }
    public static boolean candidate(Level l,int p){
        if(!supportsDoors(l)||!known(l,p)||!wallDisguise(l.map[p]))return false;
        int w=l.width();
        if(w<3||p<w||p>=l.length()-w||p%w==0||p%w==w-1)return false;
        return frame(l,p-1)&&frame(l,p+1)&&passage(l,p-w)&&passage(l,p+w)
                ||frame(l,p-w)&&frame(l,p+w)&&passage(l,p-1)&&passage(l,p+1);
    }
}
