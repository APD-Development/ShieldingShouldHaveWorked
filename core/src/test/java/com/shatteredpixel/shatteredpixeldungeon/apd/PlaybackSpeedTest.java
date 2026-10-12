package com.shatteredpixel.shatteredpixeldungeon.apd;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.watabou.noosa.*;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.GameSettings;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class PlaybackSpeedTest {
 @Rule public TemporaryFolder folder=new TemporaryFolder();
 private Preferences previous; private File file; private float oldElapsed;
 @Before public void isolate() throws Exception {
  Field f=GameSettings.class.getDeclaredField("prefs"); f.setAccessible(true);
  previous=(Preferences)f.get(null);file=new File(folder.getRoot(),"settings.xml");
  GameSettings.set(new HeadlessPreferences(new FileHandle(file)));
  oldElapsed=Game.elapsed; PlaybackGroup.speed(1);
 }
 @After public void restore(){GameSettings.set(previous);Game.elapsed=oldElapsed;PlaybackGroup.speed(1);}
 @Test public void choicesSurviveActualPreferenceFileReload(){
  assertEquals(1,SPDSettings.playbackSpeed());
  for(int mode=0;mode<=2;mode++){SPDSettings.playbackSpeed(mode);GameSettings.set(new HeadlessPreferences(new FileHandle(file)));assertEquals(mode,SPDSettings.playbackSpeed());}
 }
 @Test public void invalidChoicesUseNormalSpeed(){
  for(int mode:new int[]{-1,3,Integer.MIN_VALUE,Integer.MAX_VALUE}){GameSettings.put("apd_playback_speed",mode);assertEquals(1,SPDSettings.playbackSpeed());SPDSettings.playbackSpeed(mode);assertEquals(1,SPDSettings.playbackSpeed());}
 }
 private Gizmo observe(final float[] observed){return new Gizmo(){@Override public void update(){observed[0]=Game.elapsed;}};}
 @Test public void scalesDungeonVisualsAtAllThreeSpeeds(){
  for(int mode=0;mode<=2;mode++){PlaybackGroup.speed(mode);Game.elapsed=0.08f;
   PlaybackGroup g=new PlaybackGroup();float[] got={0};g.add(observe(got));g.update();
   assertEquals(new float[]{0.04f,0.08f,0.16f}[mode],got[0],0.00001f);assertEquals(0.08f,Game.elapsed,0f);}
 }
 @Test public void nestedDungeonGroupsAreNotScaledTwice(){
  PlaybackGroup.speed(2);Game.elapsed=0.08f;
  PlaybackGroup outer=new PlaybackGroup(),inner=new PlaybackGroup();float[] got={0};
  outer.add(inner);inner.add(observe(got));outer.update();assertEquals(0.16f,got[0],0.00001f);assertEquals(0.08f,Game.elapsed,0f);
 }
 @Test public void hudSiblingsKeepRealTime(){
  PlaybackGroup.speed(2);Game.elapsed=0.08f;
  Group scene=new Group();PlaybackGroup world=new PlaybackGroup();float[] a={0},b={0};
  world.add(observe(a));scene.add(world);scene.add(observe(b));scene.update();
  assertEquals(0.16f,a[0],0.00001f);assertEquals(0.08f,b[0],0f);
 }
 @Test public void exceptionsRestoreClockAndNestedGuard(){
  PlaybackGroup.speed(2);Game.elapsed=0.08f;PlaybackGroup broken=new PlaybackGroup();
  broken.add(new Gizmo(){@Override public void update(){throw new IllegalStateException("fixture");}});
  try{broken.update();fail();}catch(IllegalStateException expected){}
  assertEquals(0.08f,Game.elapsed,0f);
  PlaybackGroup good=new PlaybackGroup();float[] got={0};good.add(observe(got));good.update();assertEquals(0.16f,got[0],0.00001f);
 }
 @Test public void nativeTweensFinishAtExpectedPlaybackDurationAcrossRefreshRates(){
  for(int mode=0;mode<=2;mode++)for(int fps:new int[]{30,60,90,120,144,165}){
   PlaybackGroup.speed(mode);Game.elapsed=1f/fps;PlaybackGroup g=new PlaybackGroup();
   final int[] complete={0};Tweener t=new Tweener(new Gizmo(),0.1f){@Override protected void updateValues(float progress){} @Override protected void onComplete(){complete[0]++;}};
   g.add(t);int frames=0;while(t.alive&&frames<100){g.update();frames++;}
   assertEquals(1,complete[0]);double expected=0.1/new double[]{0.5,1,2}[mode];
   assertEquals(expected,(double)frames/fps,1.01/fps);
  }
 }
 @Test public void updatesDoNotAdvanceActorClockOrGlobalTimescale(){
  float actor=Actor.now(),scale=Game.timeScale,total=Game.timeTotal;Game.elapsed=0.02f;
  for(int mode=0;mode<=2;mode++){PlaybackGroup.speed(mode);PlaybackGroup g=new PlaybackGroup();g.add(new Gizmo());g.update();}
  assertEquals(actor,Actor.now(),0f);assertEquals(scale,Game.timeScale,0f);assertEquals(total,Game.timeTotal,0f);
 }
}
