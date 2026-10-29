package com.shatteredpixel.shatteredpixeldungeon.android;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.badlogic.gdx.Gdx;
import com.watabou.input.PressTimingInputHandler;
import com.watabou.noosa.Game;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Prototype evidence, NOT shipped-APK acceptance. Existing APK bytes stay unchanged. */
@RunWith(AndroidJUnit4.class)
public class ButtonTimingPrototypeDeviceTest {
 @Test public void discardingPreDownFrameTimePassesTheUnchangedNativeMenuRegression()throws Exception{
  AtomicBoolean alive=new AtomicBoolean(true),installing=new AtomicBoolean(false),installed=new AtomicBoolean(false);
  Thread watcher=new Thread(()->{
   while(alive.get()){
    if(Game.instance!=null&&Game.scene() instanceof TitleScene&&!Game.switchingScene()
       &&Game.inputHandler!=null&&!(Game.inputHandler instanceof PressTimingInputHandler)&&installing.compareAndSet(false,true)){
     Game.runOnRenderThread(()->{
      if(alive.get()&&Game.scene() instanceof TitleScene){
       Game.inputHandler=new PressTimingInputHandler(Gdx.input);
       installed.set(true);
       Log.w("APDTAPPROTO","diagnostic-only candidate handler installed; APK unchanged");
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
   org.junit.Assert.assertTrue("At least one real native press discarded pre-DOWN time",PressTimingInputHandler.correctedPresses>0);
  }finally{
   alive.set(false);watcher.interrupt();watcher.join(3000);
   Log.w("APDTAPPROTO","Candidate injected by instrumentation; result is prototype evidence, not shipped340 verification");
  }
 }
}
