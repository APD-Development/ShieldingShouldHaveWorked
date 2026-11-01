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

package com.shatteredpixel.shatteredpixeldungeon.apd.query;
import org.junit.Test;
import static org.junit.Assert.*;
public class ChoicePageTest {
 @Test public void boundariesKeepTheFortyFirstAndLastChoicesReachable(){
  for(int total:new int[]{0,1,39,40,41,80,81,1000}){
   boolean[] seen=new boolean[total];ChoicePage first=new ChoicePage(total,0);
   for(int p=0;p<first.pageCount;p++){
    ChoicePage page=new ChoicePage(total,p);
    assertTrue(page.end-page.start<=40);assertEquals(p>0,page.hasPrevious());assertEquals(p+1<page.pageCount,page.hasNext());
    for(int i=page.start;i<page.end;i++){assertFalse("duplicate choice "+i,seen[i]);seen[i]=true;}
   }
   for(boolean present:seen)assertTrue("every available comparison must remain reachable",present);
  }
 }
 @Test public void invalidPageClampsWithoutDroppingTheFinalPartialPage(){
  assertEquals(0,new ChoicePage(81,-1).start);ChoicePage last=new ChoicePage(81,Integer.MAX_VALUE);
  assertEquals(80,last.start);assertEquals(81,last.end);assertFalse(last.hasNext());assertTrue(last.hasPrevious());
 }
 @Test public void largeCountsDoNotOverflowOrImposeATotalChoiceLimit(){
  ChoicePage last=new ChoicePage(Integer.MAX_VALUE,Integer.MAX_VALUE);
  assertEquals(Integer.MAX_VALUE,last.end);assertTrue(last.start>=0);assertTrue(last.end-last.start<=40);
 }
}
