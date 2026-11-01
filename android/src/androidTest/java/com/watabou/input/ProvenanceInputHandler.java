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
/** Diagnostic-only execution of the consolidated Back handler on the existing APK. */
public class ProvenanceInputHandler extends InputHandler {
 private final Input backend;
 private boolean accepted;
 private com.watabou.noosa.Scene acceptedScene;
 private void log(String message){android.util.Log.w("APDBACKPROTOTYPE",message+" scene="+(com.watabou.noosa.Game.instance==null?"none":String.valueOf(com.watabou.noosa.Game.scene()))+" accepted="+accepted+" pending="+KeyEvent.hasPendingBackPress());}
 public ProvenanceInputHandler(Input input){super(input);backend=input;ProvenanceBackGate.setSystemBackOwner(this);ProvenanceBackGate.reset();}
 private long stamp(){long n=backend.getCurrentEventTime();return n>0&&n<=System.nanoTime()?n:0;}
 @Override public synchronized boolean keyDown(int code){
  if(code!=Input.Keys.BACK)return super.keyDown(code);
  long source=stamp();
  log("raw-down source="+source);
  if(!ProvenanceBackGate.acceptLegacy(System.nanoTime(),source)){log("down-rejected");return true;}
  accepted=true;acceptedScene=com.watabou.noosa.Game.instance==null?null:com.watabou.noosa.Game.scene();log("down-accepted");
  if(KeyBindings.isKeyBound(code)){KeyEvent event=new KeyEvent(code,true);event.sourceEventNanos=source;KeyEvent.addKeyEvent(event);return true;}
  return false;
 }
 @Override public synchronized boolean keyUp(int code){
  if(code!=Input.Keys.BACK)return super.keyUp(code);
  long source=stamp();log("raw-up source="+source);ProvenanceBackGate.recordLegacyRelease(source);
  if(!accepted){log("up-orphan");return true;}accepted=false;acceptedScene=null;log("up-accepted");
  if(KeyBindings.isKeyBound(code)){KeyEvent event=new KeyEvent(code,false);event.sourceEventNanos=source;KeyEvent.addKeyEvent(event);return true;}
  return false;
 }
 @Override public synchronized void scenePresented(long completed){
  super.scenePresented(completed);
  boolean sameScene=com.watabou.noosa.Game.instance!=null&&acceptedScene==com.watabou.noosa.Game.scene();
  log("presented same="+sameScene);
  if(!sameScene&&!KeyEvent.hasPendingBackPress()){accepted=false;acceptedScene=null;}
 }
}
