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

package com.shatteredpixel.shatteredpixeldungeon.apd.demo;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.ArrayList;
public class TitleUnlockProbeTest {
 @Test public void renderingDoesNotWaitForCountAndRepeatedRebuildsCoalesce(){
  ArrayList<Runnable> tasks=new ArrayList<>();int[] reads={0};
  TitleUnlockProbe probe=new TitleUnlockProbe(tasks::add,root->{reads[0]++;return 2;});File root=new File("history");
  for(int i=0;i<20;i++){probe.request(root);assertFalse(probe.earned(root));}
  assertEquals(0,reads[0]);assertEquals(1,tasks.size());tasks.remove(0).run();assertEquals(1,reads[0]);
  probe.request(root);assertEquals(0,tasks.size());assertFalse(probe.earned(root));
 }
 @Test public void staleRootCannotUnlockNewHistoryAndNewRunInvalidatesCount(){
  ArrayList<Runnable> tasks=new ArrayList<>();TitleUnlockProbe probe=new TitleUnlockProbe(tasks::add,root->10);
  File a=new File("a"),b=new File("b");probe.request(a);probe.request(b);tasks.remove(0).run();assertFalse(probe.earned(b));
  tasks.remove(0).run();assertTrue(probe.earned(b));assertFalse(probe.earned(a));probe.invalidate();assertFalse(probe.earned(b));
  probe.request(b);assertEquals(1,tasks.size());
 }
 @Test public void readFailureNeverGrantsUnlockAndAllowsRetry(){
  ArrayList<Runnable> tasks=new ArrayList<>();TitleUnlockProbe probe=new TitleUnlockProbe(tasks::add,root->{throw new IllegalStateException("corrupt history");});
  File root=new File("history");probe.request(root);tasks.remove(0).run();assertFalse(probe.earned(root));probe.request(root);assertEquals(1,tasks.size());
 }
}
