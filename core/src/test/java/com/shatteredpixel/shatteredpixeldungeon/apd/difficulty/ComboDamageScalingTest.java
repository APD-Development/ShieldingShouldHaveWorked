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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import org.junit.Test;
import static org.junit.Assert.*;
public class ComboDamageScalingTest {
 private int roll(int raw,int count)throws Exception{
  java.lang.reflect.Method m=Combo.class.getDeclaredMethod("apdCrushSplashRoll",Char.class,int.class,int.class);
  m.setAccessible(true);return (Integer)m.invoke(null,new Hero(),raw,count);
 }
 private Bundle save(){Bundle b=new Bundle();Dungeon.apdStoreCustomRules(b);return b;}
 private void rules(int player){Bundle b=new Bundle(),r=new Bundle();CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.PLAYER_DAMAGE,player).with(CustomDungeonRules.Slider.ENEMY_DAMAGE,275).with(CustomDungeonRules.Slider.BOSS_DAMAGE,425).store(r);b.put("apd_custom_rules",r);Dungeon.apdRestoreCustomRules(b);}
 @Test public void crushScalesPlayerDamageBeforeFixedArmor()throws Exception{
  Bundle old=save();try{rules(200);assertEquals(33,roll(20,8)/2-7);rules(50);assertEquals(3,roll(20,8)/2-7);}finally{Dungeon.apdRestoreCustomRules(old);}
 }
 @Test public void neutralPreservesOriginalRoundingAndNextRng()throws Exception{
  Bundle old=save();try{rules(100);Random.pushGenerator(1234);long expected;try{expected=Random.Long();}finally{Random.popGenerator();}
  Random.pushGenerator(1234);try{for(int raw=0;raw<100;raw++)for(int c=1;c<25;c++)assertEquals(Math.round(raw*.25f*c),roll(raw,c));assertEquals(expected,Random.Long());}finally{Random.popGenerator();}}finally{Dungeon.apdRestoreCustomRules(old);}
 }
 @Test public void everyPermittedPercentageUsesPlayerRatherThanEnemyOrBoss()throws Exception{
  Bundle old=save();try{for(int p=25;p<=500;p+=25){rules(p);assertEquals(p,roll(100,4));}}finally{Dungeon.apdRestoreCustomRules(old);}
 }
}
