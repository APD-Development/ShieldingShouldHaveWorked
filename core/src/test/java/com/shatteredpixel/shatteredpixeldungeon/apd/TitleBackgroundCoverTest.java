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
import org.junit.Test;
import static org.junit.Assert.*;
public class TitleBackgroundCoverTest {
 @Test public void backgroundCoversPhoneCompactLandscapeAndSquareAtAllScales(){
  int[][] screens={{1216,2688},{1080,2400},{480,800},{2400,1080},{800,800}};
  for(int[] screen:screens)for(int zoom=2;zoom<=9;zoom++){
   float w=screen[0]/(float)zoom,h=screen[1]/(float)zoom;boolean landscape=w>h;
   int iw=landscape?400:200,ih=landscape?200:360;
   float scale=TitleGeometry.backgroundCoverScale(w,h,iw,ih,zoom);
   assertTrue(iw*scale+.001f>=w);assertTrue(ih*scale+.001f>=h);
   assertEquals(Math.round(scale*zoom),scale*zoom,.0001f);
  }
 }
 @Test public void changingUiScaleDoesNotShrinkThePhysicalBackground(){
  float six=TitleGeometry.backgroundCoverScale(1216f/6,2688f/6,200,360,6)*6;
  float seven=TitleGeometry.backgroundCoverScale(1216f/7,2688f/7,200,360,7)*7;
  assertEquals(six,seven,.0001f);
 }
}
