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

package com.shatteredpixel.shatteredpixeldungeon.apd.stats;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
public class DerivedCacheConcurrentWriterTest {
 @Rule public TemporaryFolder tmp=new TemporaryFolder();
 @Test public void overlappingFactStoresMustLeaveACompleteCache()throws Exception{
  File root=tmp.newFolder("facts");DerivedStore firstStore=new DerivedStore(root),secondStore=new DerivedStore(root);
  assertTrue(firstStore.cacheFile().getParentFile().mkdirs());
  CountDownLatch opened=new CountDownLatch(1),release=new CountDownLatch(1);
  List<String> firstPrints=new AbstractList<String>(){
   @Override public int size(){return 1;}
   @Override public String get(int index){if(index!=0)throw new IndexOutOfBoundsException();opened.countDown();try{if(!release.await(5,TimeUnit.SECONDS))throw new AssertionError("release timeout");}catch(InterruptedException e){throw new AssertionError(e);}return "first";}
  };
  Method write=DerivedStore.class.getDeclaredMethod("writeCache",FactTable.class,List.class);write.setAccessible(true);
  ExecutorService pool=Executors.newFixedThreadPool(2);
  try{
   Future<?> first=pool.submit(()->{try{write.invoke(firstStore,new FactTable(),firstPrints);}catch(Exception e){throw new RuntimeException(e);}});
   assertTrue(opened.await(5,TimeUnit.SECONDS));
   Future<?> second=pool.submit(()->{try{write.invoke(secondStore,new FactTable(),Collections.singletonList("second"));}catch(Exception e){throw new RuntimeException(e);}});
   second.get(5,TimeUnit.SECONDS);release.countDown();first.get(5,TimeUnit.SECONDS);
   assertTrue("Both completed writers must leave a usable fact cache",firstStore.cacheFile().isFile());
   Method read=DerivedStore.class.getDeclaredMethod("readCache",File.class,List.class);read.setAccessible(true);
   assertNotNull(read.invoke(firstStore,firstStore.cacheFile(),Collections.singletonList("first")));
   File[] leftovers=firstStore.cacheFile().getParentFile().listFiles((d,n)->n.endsWith(".tmp"));assertNotNull(leftovers);assertEquals(0,leftovers.length);
  }finally{release.countDown();pool.shutdownNow();}
 }
}

