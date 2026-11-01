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

//A presentation page, never a limit on the available comparison records.
public final class ChoicePage {
 public static final int PAGE_SIZE = 40;
 public final int page, pageCount, start, end;
 public ChoicePage(int total, int requestedPage){
  if(total<0)throw new IllegalArgumentException("negative choice count");
  pageCount=Math.max(1, total/PAGE_SIZE + (total%PAGE_SIZE==0?0:1));
  page=Math.max(0,Math.min(requestedPage,pageCount-1));
  start=page*PAGE_SIZE;
  end=start+Math.min(PAGE_SIZE,total-start);
 }
 public boolean hasPrevious(){return page>0;}
 public boolean hasNext(){return page+1<pageCount;}
}
