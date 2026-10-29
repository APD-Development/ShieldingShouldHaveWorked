package com.shatteredpixel.shatteredpixeldungeon.ui;
import com.badlogic.gdx.Input;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.watabou.input.*;
import com.watabou.noosa.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ButtonPressTimingTest {
 static class Probe extends Button {
  int clicks,longs;final boolean handlesLong;
  Probe(boolean handles){handlesLong=handles;camera=new Camera(0,0,64,64,1);setRect(0,0,32,32);}
  @Override protected void onClick(){clicks++;}
  @Override protected boolean onLongClick(){longs++;return handlesLong;}
  @Override public GameAction keyAction(){return GameAction.BACK;}
 }
 com.watabou.utils.PlatformSupport savedPlatform;float savedElapsed,savedLong;
 @Before public void start(){HeadlessTestSupport.start();savedElapsed=Game.elapsed;savedLong=Button.longClick;Button.longClick=.5f;savedPlatform=Game.platform;Game.platform=new com.watabou.utils.PlatformSupport(){
 public void updateDisplaySize(){} public void updateSystemUI(){} public boolean connectedToUnmeteredNetwork(){return false;} public boolean supportsVibration(){return false;} public void setupFontGenerators(int p,boolean s){} protected com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator getGeneratorForString(String s){return null;} public String[] splitforTextBlock(String s,boolean m){return new String[]{s};} public void setOnscreenKeyboardVisible(boolean v,boolean m){}
 };InputHandler.discardPendingEvents();KeyBindings.addHardBinding(Input.Keys.BACK,GameAction.BACK);}
 @After public void clean(){Button.cancelPressedButton();InputHandler.discardPendingEvents();Game.elapsed=savedElapsed;Button.longClick=savedLong;Game.platform=savedPlatform;}
 private void pointer(boolean down){PointerEvent.addPointerEvent(new PointerEvent(16,16,871,down?PointerEvent.Type.DOWN:PointerEvent.Type.UP));PointerEvent.processPointerEvents();}
 private void frame(Probe p,float elapsed){Game.elapsed=elapsed;p.update();}
 @Test public void aFrameBeforePointerDownMustNotTurnAShortPressIntoALongPress(){
  Probe p=new Probe(true);try{
   Game.elapsed=.2f;pointer(true);p.update();
   frame(p,.19f);frame(p,.185f);pointer(false);
   assertEquals("Only375ms followed DOWN; previous frame belongs before the press",0,p.longs);
   assertEquals(1,p.clicks);
  }finally{p.destroy();}
 }
 @Test public void shortTapOnAButtonWithoutALongActionMustStillClickAfterASlowFrame(){
  Probe p=new Probe(false);try{
   Game.elapsed=.2f;pointer(true);p.update();frame(p,.19f);frame(p,.185f);pointer(false);
   assertEquals(0,p.longs);assertEquals("Short tap must not be silently discarded",1,p.clicks);
  }finally{p.destroy();}
 }
 @Test public void keyboardPressAlsoStartsAfterItsArrivalFrame(){
  Probe p=new Probe(true);try{
   Game.elapsed=.2f;KeyEvent.addKeyEvent(new KeyEvent(Input.Keys.BACK,true));KeyEvent.processKeyEvents();p.update();
   frame(p,.19f);frame(p,.185f);KeyEvent.addKeyEvent(new KeyEvent(Input.Keys.BACK,false));KeyEvent.processKeyEvents();
   assertEquals(0,p.longs);assertEquals(1,p.clicks);
  }finally{p.destroy();}
 }
 @Test public void anActualHalfSecondHoldStillRunsItsLongActionExactlyOnce(){
  Probe p=new Probe(true);try{
   Game.elapsed=.2f;pointer(true);p.update();frame(p,.2f);frame(p,.2f);frame(p,.101f);frame(p,.2f);pointer(false);
   assertEquals(1,p.longs);assertEquals(0,p.clicks);
  }finally{p.destroy();}
 }
}
