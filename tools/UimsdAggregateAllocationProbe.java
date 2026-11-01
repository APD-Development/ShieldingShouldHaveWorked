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
import java.lang.management.ManagementFactory;
import java.util.*;
public class AggregateAllocationProbe {
 public static void main(String[] args) {
  DatasetDef d=QuerySupport.def(); List<Row> rows=new ArrayList<>();
  for(int i=0;i<1000;i++) rows.add(QuerySupport.row(d,""+i,"r"+i,(long)i,0L,0L,true,"r"));
  Query q=Query.of("things"); Aggregate a=Aggregate.of(Aggregate.Fn.SUM,"score");
  for(int i=0;i<10000;i++) if(QueryEngine.evaluate(d,q,a,rows).value!=499500) throw new AssertionError();
  com.sun.management.ThreadMXBean bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
  bean.setThreadAllocatedMemoryEnabled(true); long id=Thread.currentThread().getId();
  long before=bean.getThreadAllocatedBytes(id), start=System.nanoTime();
  for(int i=0;i<20000;i++) if(QueryEngine.evaluate(d,q,a,rows).value!=499500) throw new AssertionError();
  System.out.println("calls=20000 allocatedBytes="+(bean.getThreadAllocatedBytes(id)-before)+" elapsedNanos="+(System.nanoTime()-start));
 }
}

