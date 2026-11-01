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

package com.shatteredpixel.shatteredpixeldungeon.apd.loot;
import org.junit.Test;
import static org.junit.Assert.*;
public class LootScrollRulesTest {
 @Test public void bothVariantsShareTheExactFloorNineteenCutoff(){
  assertFalse(LootScrollRules.canGenerate(-1));assertFalse(LootScrollRules.canGenerate(0));
  for(int depth=1;depth<=19;depth++)assertTrue("floor "+depth,LootScrollRules.canGenerate(depth));
  for(int depth=20;depth<=1000;depth++)assertFalse("floor "+depth,LootScrollRules.canGenerate(depth));
 }
 @Test public void routesSkipCurrentRegionAndDemonHallsReturnToFloorTwenty(){
  for(int depth=1;depth<=26;depth++){
   int expected=depth<=5?6:depth<=10?11:depth<=15?16:20;
   assertEquals("source "+depth,expected,LootScrollRules.destinationDepth(depth));
  }
  assertEquals(11,LootScrollRules.destinationDepth(8));
  assertEquals(-1,LootScrollRules.destinationDepth(0));
  assertEquals(-1,LootScrollRules.destinationDepth(27));
 }
}
