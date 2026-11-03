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
package com.shatteredpixel.shatteredpixeldungeon.apd;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.*;
import org.junit.*;
import static org.junit.Assert.*;
public class MissileLineOfFireTest {
    private Hero previousHero,hero;private Level previousLevel,arena;private Rat enemy,blocker;
    private int at(int x,int y){return y*arena.width()+x;}
    private void sight(){ShadowCaster.castShadow(hero.pos%arena.width(),hero.pos/arena.width(),arena.width(),hero.fieldOfView,arena.losBlocking,20);}
    @Before public void setup(){
        HeadlessTestSupport.start();previousHero=Dungeon.hero;previousLevel=Dungeon.level;
        arena=new SewerLevel();arena.blobs=new java.util.HashMap<>();arena.traps=new com.watabou.utils.SparseArray<>();arena.setSize(32,32);java.util.Arrays.fill(arena.map,Terrain.EMPTY);
        for(int y=0;y<32;y++)for(int x=0;x<32;x++)if(x==0||y==0||x==31||y==31)arena.map[at(x,y)]=Terrain.WALL;
        arena.buildFlagMaps();Dungeon.level=arena;hero=new Hero();hero.pos=at(2,2);hero.fieldOfView=new boolean[arena.length()];Dungeon.hero=hero;
        enemy=new Rat();enemy.pos=at(6,5);Actor.add(enemy);
    }
    @After public void cleanup(){Actor.remove(enemy);if(blocker!=null)Actor.remove(blocker);Dungeon.hero=previousHero;Dungeon.level=previousLevel;}
    @Test public void visibleEnemyAtTieSlopeHasAContinuousClearStraightRay(){
        Level.set(at(4,4),Terrain.WALL);sight();assertTrue("enemy is legitimately visible",hero.fieldOfView[enemy.pos]);
        assertNotEquals("forward rounding hits the neighboring wall",enemy.pos,new Ballistica(hero.pos,enemy.pos,Ballistica.PROJECTILE).collisionPos.intValue());
        assertEquals("same straight line in reverse rounding is clear",hero.pos,new Ballistica(enemy.pos,hero.pos,Ballistica.PROJECTILE).collisionPos.intValue());
        assertEquals("visible enemy with clear ray remains throwable",enemy.pos,new ThrowingKnife().throwPos(hero,enemy.pos));
    }
    @Test public void clearVisibleShotsHaveNoArtificialMeleeOrShortRangeLimit(){
        enemy.pos=at(20,2);sight();assertTrue(hero.fieldOfView[enemy.pos]);assertEquals(enemy.pos,new ThrowingKnife().throwPos(hero,enemy.pos));
        enemy.pos=at(14,14);sight();assertTrue(hero.fieldOfView[enemy.pos]);assertEquals(enemy.pos,new ThrowingKnife().throwPos(hero,enemy.pos));
    }
    @Test public void seeingAnEnemyDoesNotAllowBendingAroundAClosedCorner(){
        Level.set(at(4,3),Terrain.WALL);Level.set(at(4,4),Terrain.WALL);hero.fieldOfView[enemy.pos]=true;
        assertNotEquals(enemy.pos,new ThrowingKnife().throwPos(hero,enemy.pos));
    }
    @Test public void interveningCreaturesStillBlockTheShot(){
        Level.set(at(4,4),Terrain.WALL);sight();blocker=new Rat();blocker.pos=at(3,3);Actor.add(blocker);
        assertNotEquals(enemy.pos,new ThrowingKnife().throwPos(hero,enemy.pos));
    }
    @Test public void unseenEnemiesKeepNormalCollisionRules(){
        Level.set(at(4,4),Terrain.WALL);assertFalse(hero.fieldOfView[enemy.pos]);
        assertEquals(new Ballistica(hero.pos,enemy.pos,Ballistica.PROJECTILE).collisionPos.intValue(),new ThrowingKnife().throwPos(hero,enemy.pos));
    }
    @Test public void intentionalProjectingEnchantmentRetainsItsExistingWallBypass(){
        for(int y=1;y<31;y++)Level.set(at(4,y),Terrain.WALL);
        ThrowingKnife weapon=new ThrowingKnife();weapon.enchantment=new Projecting();
        assertEquals(enemy.pos,weapon.throwPos(hero,enemy.pos));
    }
}
