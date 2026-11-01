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
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ShopRoom;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import org.junit.Test;import static org.junit.Assert.*;
public class ArmorShopTest {
 @Test public void shopsStockProgressingArmorWithoutChangingRates() throws Exception{
  HeadlessTestSupport.start();
  com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.WARRIOR;
  Dungeon.seed=20261012L;Dungeon.init();int original= Dungeon.depth;
  java.lang.reflect.Method method=ShopRoom.class.getDeclaredMethod("generateItems");method.setAccessible(true);
  try{for(int[] pair:new int[][]{{6,2},{11,3},{16,4},{21,5}}){Dungeon.depth=pair[0];int count=0;
   for(Object o:(java.util.List<?>)method.invoke(null))if(o instanceof Armor){assertEquals(pair[1],((Armor)o).tier);count++;}
   assertEquals(1,count);
  }}finally{Dungeon.depth=original;}
 }
}
