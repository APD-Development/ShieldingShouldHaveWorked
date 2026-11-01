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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import org.junit.Test;
import static org.junit.Assert.*;
public class OffensiveInvocationTest {
 @Test public void delayedCallbackUsesCastTimeOriginEvenAfterAllegianceChangesAndReleasesIt(){
  Rat actor=new Rat();OffensiveDamageOrigin captured=OffensiveDamageOrigin.capture(actor,null);actor.alignment=Char.Alignment.ALLY;
  try(OffensiveInvocation scope=OffensiveInvocation.enter(actor,captured)){assertEquals(OffensiveDamageOrigin.ENEMY,OffensiveDamageOrigin.capture(actor,null));}
  assertNull(OffensiveInvocation.captured(actor));assertEquals(OffensiveDamageOrigin.UNATTRIBUTED,OffensiveDamageOrigin.capture(actor,null));
 }
 @Test public void nestedAndThrowingCallbacksRestorePreviousScopeAndDoNotLeakAcrossThreads()throws Exception {
  Rat first=new Rat(),second=new Rat();
  try(OffensiveInvocation outer=OffensiveInvocation.enter(first,OffensiveDamageOrigin.PLAYER)){
   assertThrows(IllegalStateException.class,()->{try(OffensiveInvocation inner=OffensiveInvocation.enter(first,OffensiveDamageOrigin.BOSS)){assertEquals(OffensiveDamageOrigin.BOSS,OffensiveDamageOrigin.capture(first,null));throw new IllegalStateException("callback failed");}});
   assertEquals(OffensiveDamageOrigin.PLAYER,OffensiveDamageOrigin.capture(first,null));
   try(OffensiveInvocation other=OffensiveInvocation.enter(second,OffensiveDamageOrigin.BOSS)){assertEquals(OffensiveDamageOrigin.PLAYER,OffensiveDamageOrigin.capture(first,null));}
   java.util.concurrent.atomic.AtomicReference<OffensiveDamageOrigin> result=new java.util.concurrent.atomic.AtomicReference<>();Thread t=new Thread(()->result.set(OffensiveDamageOrigin.capture(first,null)));t.start();t.join();assertEquals(OffensiveDamageOrigin.ENEMY,result.get());
  }
  assertNull(OffensiveInvocation.captured(first));
 }
}
