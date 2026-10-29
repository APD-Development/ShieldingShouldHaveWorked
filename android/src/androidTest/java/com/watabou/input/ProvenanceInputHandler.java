package com.watabou.input;
import com.badlogic.gdx.Input;
/** Diagnostic-only execution of the consolidated Back handler on the existing APK. */
public class ProvenanceInputHandler extends InputHandler {
 private final Input backend;
 private boolean accepted;
 public ProvenanceInputHandler(Input input){super(input);backend=input;ProvenanceBackGate.setSystemBackOwner(this);ProvenanceBackGate.reset();}
 private long stamp(){long n=backend.getCurrentEventTime();return n>0&&n<=System.nanoTime()?n:0;}
 @Override public synchronized boolean keyDown(int code){
  if(code!=Input.Keys.BACK)return super.keyDown(code);
  long source=stamp();
  if(!ProvenanceBackGate.acceptLegacy(System.nanoTime(),source))return true;
  accepted=true;
  if(KeyBindings.isKeyBound(code)){KeyEvent event=new KeyEvent(code,true);event.sourceEventNanos=source;KeyEvent.addKeyEvent(event);return true;}
  return false;
 }
 @Override public synchronized boolean keyUp(int code){
  if(code!=Input.Keys.BACK)return super.keyUp(code);
  long source=stamp();ProvenanceBackGate.recordLegacyRelease(source);
  if(!accepted)return true;accepted=false;
  if(KeyBindings.isKeyBound(code)){KeyEvent event=new KeyEvent(code,false);event.sourceEventNanos=source;KeyEvent.addKeyEvent(event);return true;}
  return false;
 }
 @Override public synchronized void scenePresented(long completed){
  super.scenePresented(completed);
  if(!KeyEvent.hasPendingBackPress())accepted=false;
 }
}
