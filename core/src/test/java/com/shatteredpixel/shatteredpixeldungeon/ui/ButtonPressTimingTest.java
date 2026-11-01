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
 float savedElapsed,savedLong;boolean savedVibration;
 @Before public void start(){HeadlessTestSupport.start();savedElapsed=Game.elapsed;savedLong=Button.longClick;Button.longClick=.5f;savedVibration=com.shatteredpixel.shatteredpixeldungeon.SPDSettings.vibration();com.shatteredpixel.shatteredpixeldungeon.SPDSettings.vibration(false);InputHandler.discardPendingEvents();KeyBindings.addHardBinding(Input.Keys.BACK,GameAction.BACK);}
 @After public void clean(){Button.cancelPressedButton();InputHandler.discardPendingEvents();Game.elapsed=savedElapsed;Button.longClick=savedLong;com.shatteredpixel.shatteredpixeldungeon.SPDSettings.vibration(savedVibration);}
 private void pointer(boolean down){
  com.watabou.utils.Signal.Listener<PointerEvent> observer=e->{PointerEvent.clearKeyboardThisPress=false;return false;};
  PointerEvent.addPointerListener(observer);
  try{PointerEvent.addPointerEvent(new PointerEvent(16,16,871,down?PointerEvent.Type.DOWN:PointerEvent.Type.UP));PointerEvent.processPointerEvents();}
  finally{PointerEvent.removePointerListener(observer);}
 }
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
