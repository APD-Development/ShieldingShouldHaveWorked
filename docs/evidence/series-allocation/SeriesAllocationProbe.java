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

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.*;
import java.io.*;import java.lang.management.ManagementFactory;import java.util.*;
public class SeriesAllocationProbe {
 public static void main(String[] args)throws Exception {
  int n=10000,nv=4;long[] clocks=new long[n];long[][] rows=new long[n][nv];
  for(int i=0;i<n;i++){clocks[i]=i;rows[i]=new long[]{i,i/20,-i, i%2==0?Long.MIN_VALUE:Long.MAX_VALUE};}
  File f=File.createTempFile("series-allocation-",".apdlog");
  f.delete();
  RunLogWriter w=RunLogWriter.create(f,TelemetryVersion.CURRENT,"allocation","test",1);
  if(!FactEvents.emitSeries(w,EventTypes.FACT_SERIES,"test.values","","",new String[0],nv,clocks,rows,n))throw new AssertionError("writer refused event size");w.close(false,5000);
  ReadResult r=RunLogReader.read(f);ApdEvent e=null;for(ApdEvent event:r.events)if(FactEvents.isSeries(event.type))e=event;
  if(e==null)throw new AssertionError("no series");
  for(int j=0;j<10;j++)FactEvents.decode(e,r);
  com.sun.management.ThreadMXBean bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
  long thread=Thread.currentThread().getId(),before=bean.getThreadAllocatedBytes(thread),started=System.nanoTime(),hash=0;
  for(int j=0;j<25;j++)for(FactEvents.Observation o:FactEvents.decode(e,r)){hash=31*hash+o.clock;for(long value:o.values)hash=31*hash+value;}
  System.out.println("allocated="+(bean.getThreadAllocatedBytes(thread)-before)+" ns="+(System.nanoTime()-started)+" hash="+hash);f.delete();
 }
}
