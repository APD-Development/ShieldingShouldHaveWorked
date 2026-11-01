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

package com.shatteredpixel.shatteredpixeldungeon.android;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.badlogic.gdx.Gdx;
import com.watabou.input.ProvenanceInputHandler;
import com.watabou.noosa.Game;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Prototype evidence, NOT shipped-APK acceptance. Existing APK bytes stay unchanged. */
@RunWith(AndroidJUnit4.class)
public class BackReleasePrototypeDeviceTest {
 @Test public void proposedPairedBackPassesTheUnchangedNativeMenuRegression()throws Exception{
  AtomicBoolean alive=new AtomicBoolean(true),installing=new AtomicBoolean(false),installed=new AtomicBoolean(false);
  Thread watcher=new Thread(()->{
   while(alive.get()){
    if(Game.instance!=null&&Game.scene() instanceof TitleScene&&!Game.switchingScene()
       &&Game.inputHandler!=null&&!(Game.inputHandler instanceof ProvenanceInputHandler)&&installing.compareAndSet(false,true)){
     Game.runOnRenderThread(()->{
      if(alive.get()&&Game.scene() instanceof TitleScene){
       Game.inputHandler=new ProvenanceInputHandler(Gdx.input);
       installed.set(true);
       Log.w("APDBACKPROTOTYPE","diagnostic-only candidate handler installed; APK unchanged");
      }
      installing.set(false);
     });
    }
    try{Thread.sleep(10);}catch(InterruptedException done){return;}
   }
  },"APD-prototype-install");
  watcher.start();
  try{
   new TitleMenuLayoutDeviceTest().titleControlsStaySeparateAndNativeChangesAndHideUiRemainUsable();
   org.junit.Assert.assertTrue("The proposed handler was actually exercised",installed.get());
  }finally{
   alive.set(false);watcher.interrupt();watcher.join(3000);
   Log.w("APDBACKPROTOTYPE","Candidate injected by instrumentation; result is prototype evidence, not shipped338 verification");
  }
 }
}
