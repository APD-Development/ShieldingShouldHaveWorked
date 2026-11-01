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

package com.watabou.input;
import com.badlogic.gdx.Input;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.watabou.noosa.Game;
import java.lang.reflect.Field;
/** Test-only equivalent of skipping pre-DOWN frame time on frozen340. */
public class PressTimingInputHandler extends InputHandler {
 private static final Field PRESSED,TIME;
 public static volatile int correctedPresses;
 static {try{PRESSED=Button.class.getDeclaredField("pressedButton");TIME=Button.class.getDeclaredField("pressTime");PRESSED.setAccessible(true);TIME.setAccessible(true);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 public PressTimingInputHandler(Input input){super(input);correctedPresses=0;}
 @Override public void processAllEvents(){
  try{
   Object before=PRESSED.get(null);
   super.processAllEvents();
   Object after=PRESSED.get(null);
   if(after!=null&&before!=after){
    // Frozen Button.update will add this same frame interval once. Cancel
    // that pre-press credit; never call onClick or synthesize an input edge.
    TIME.setFloat(after,-Game.elapsed);correctedPresses++;
    android.util.Log.w("APDTAPPROTO","discard_pre_down_frame_ms="+(Game.elapsed*1000)+" button="+after.getClass().getName());
   }
  }catch(IllegalAccessException e){throw new RuntimeException(e);}
 }
}
