package com.shatteredpixel.shatteredpixeldungeon.android;
import static org.junit.Assert.*;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.InputDevice;
import android.os.SystemClock;
import com.watabou.utils.Point;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndScoreBreakdown;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.*;
@RunWith(AndroidJUnit4.class)
public class CustomScoreDeviceTest{
 private static void swipe(Point from,Point to)throws Exception{
  android.app.Instrumentation in=InstrumentationRegistry.getInstrumentation();long down=SystemClock.uptimeMillis();
  for(int step=0;step<=6;step++){int action=step==0?MotionEvent.ACTION_DOWN:step==6?MotionEvent.ACTION_UP:MotionEvent.ACTION_MOVE;float part=step/6f;MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,from.x+(to.x-from.x)*part,from.y+(to.y-from.y)*part,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);in.sendPointerSync(event);event.recycle();Thread.sleep(70);}Thread.sleep(500);
 }
 @Test public void nativeScorePopupsShowRecordedNeutralPendingAndUnknownThenCloseWithAndroidBack()throws Exception{
  try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
   long end=System.currentTimeMillis()+120000;while((Game.scene()==null||PixelScene.uiCamera==null)&&System.currentTimeMillis()<end)Thread.sleep(200);assertNotNull(Game.scene());
   TopScoresDeviceTest.render(()->ShatteredPixelDungeon.switchNoFade(TitleScene.class));end=System.currentTimeMillis()+30000;while(!(Game.scene() instanceof TitleScene)&&System.currentTimeMillis()<end)Thread.sleep(100);assertTrue(Game.scene() instanceof TitleScene);Thread.sleep(600);
   Bundle savedStats=new Bundle();int[] savedVersion=new int[1];int[] savedViewport=new int[2];
   TopScoresDeviceTest.render(()->{Statistics.storeInBundle(savedStats);savedVersion[0]=Dungeon.initialVersion;savedViewport[0]=PixelScene.uiCamera.width;savedViewport[1]=PixelScene.uiCamera.height;Dungeon.initialVersion=Game.versionCode;Statistics.winMultiplier=1;Statistics.chalMultiplier=1;Statistics.totalScore=12345;});
   try{
    for(int state=0;state<4;state++){
     final float[] dimensions=new float[2];if(state==3)TopScoresDeviceTest.render(()->{dimensions[0]=PixelScene.uiCamera.width;dimensions[1]=PixelScene.uiCamera.height;PixelScene.uiCamera.width=(int)Math.min(dimensions[0],140);PixelScene.uiCamera.height=(int)Math.min(dimensions[1],120);});
     final Bundle record=new Bundle();if(state<2 || state==3){Bundle settings=new Bundle();(state==0?CustomDungeonRules.defaults():CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.UPGRADE_SCROLLS,60)).store(settings);record.put("apd_custom_rules",settings);com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomScoreSnapshot.forNewRun(CustomDungeonRules.restore(settings)).store(record);}
     final String expected=state==0?"1.0000×":state==1 || state==3?"0.5609×":"?";final WndScoreBreakdown[] window=new WndScoreBreakdown[1];
     TopScoresDeviceTest.render(()->{window[0]=new WndScoreBreakdown(record);Game.scene().add(window[0]);});Thread.sleep(700);
     TopScoresDeviceTest.render(()->{try{
      List<RenderedTextBlock> blocks=TopScoresDeviceTest.find(window[0],RenderedTextBlock.class);RenderedTextBlock custom=null,challenge=null;boolean value=false,original=false;
      for(RenderedTextBlock text:blocks){if("Custom Rules Multiplier".equals(text.text()))custom=text;if(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(window[0],"challenge_multiplier").equals(text.text()))challenge=text;if(expected.equals(text.text()))value=true;if("Original score".equals(text.text()))original=true;assertTrue(text.text(),text.left()>=-0.01f);assertTrue(text.text(),text.right()<=115.01f);}
      assertNotNull(custom);assertNotNull(challenge);assertTrue("Original score is not labeled",original);assertTrue(value);assertTrue(custom.top()>=challenge.bottom());assertTrue("Score popup exceeds Android viewport",window[0].camera.height<=PixelScene.uiCamera.height);assertEquals(12345,Statistics.totalScore);
     }catch(Exception e){throw new RuntimeException(e);}});
     if(state==3){
      final Point[] points=new Point[2];final ScrollPane[] pane=new ScrollPane[1];TopScoresDeviceTest.render(()->{try{pane[0]=TopScoresDeviceTest.find(window[0],ScrollPane.class).get(0);assertTrue("Constrained score fixture must overflow: content="+pane[0].content().height()+" viewport="+pane[0].height(),pane[0].content().height()>pane[0].height());points[0]=pane[0].camera().cameraToScreen(pane[0].left()+pane[0].width()/2,pane[0].bottom()-12);points[1]=pane[0].camera().cameraToScreen(pane[0].left()+pane[0].width()/2,pane[0].top()+12);}catch(Exception e){throw new RuntimeException(e);}});
      TopScoresDeviceTest.screenshot("custom-score-narrow-before.png");swipe(points[0],points[1]);swipe(points[0],points[1]);TopScoresDeviceTest.render(()->assertTrue("Native touchscreen did not meaningfully scroll score details: offset="+pane[0].content().camera.scroll.y,pane[0].content().camera.scroll.y>=Math.min(12f,pane[0].content().height()-pane[0].height())));
     }
     TopScoresDeviceTest.screenshot("custom-score-"+(state==0?"neutral":state==1?"pending":state==2?"unknown":"narrow")+".png");InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);Thread.sleep(500);
     TopScoresDeviceTest.render(()->{assertNull(window[0].parent);assertTrue(Game.scene() instanceof TitleScene);assertEquals(12345,Statistics.totalScore);if(dimensions[1]>0){PixelScene.uiCamera.width=(int)dimensions[0];PixelScene.uiCamera.height=(int)dimensions[1];}});
    }
   }finally{TopScoresDeviceTest.render(()->{Statistics.restoreFromBundle(savedStats);Dungeon.initialVersion=savedVersion[0];PixelScene.uiCamera.width=savedViewport[0];PixelScene.uiCamera.height=savedViewport[1];});}
  }
 }
}
