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
  oldElapsed=Game.elapsed; ApdFeatures.resetForTest(); PlaybackGroup.speed(1);
 }
 @After public void restore(){ApdFeatures.resetForTest();GameSettings.set(previous);Game.elapsed=oldElapsed;PlaybackGroup.speed(1);}
 @Test public void choicesSurviveActualPreferenceFileReload(){
  assertEquals(1,SPDSettings.playbackSpeed());
  for(int mode=0;mode<=2;mode++){SPDSettings.playbackSpeed(mode);GameSettings.set(new HeadlessPreferences(new FileHandle(file)));assertEquals(mode,SPDSettings.playbackSpeed());}
 }
 @Test public void extraSpeedsArePrivateAndNeverLeakIntoNormalBuilds() {
  assertEquals(3,PlaybackGroup.choiceCount());
  assertEquals("2x",PlaybackGroup.label(2));
  assertFalse(PlaybackGroup.allowed(3));
  SPDSettings.playbackSpeed(9);
  assertEquals(1,SPDSettings.playbackSpeed());
  GameSettings.put("apd_playback_speed",9);
  assertEquals("Developer-only preference cannot activate in normal build",1,SPDSettings.playbackSpeed());
  PlaybackGroup.speed(9);
  Game.elapsed=0.016f; PlaybackGroup group=new PlaybackGroup(); float[] observed={0};
  group.add(observe(observed));group.update();
  assertEquals(0.016f,observed[0],0.00001f);
 }
 @Test public void developerSpeedsPersistAndScaleVisualsFromFourToOneThousand(){
  ApdFeatures.parse("testBuild\n");
  String[] labels={"0.5x","1x","2x","4x","8x","16x","32x","64x","100x","1000x"};
  float[] factors={.5f,1f,2f,4f,8f,16f,32f,64f,100f,1000f};
  assertEquals(labels.length,PlaybackGroup.choiceCount());
  for(int i=0;i<labels.length;i++){
   assertTrue(PlaybackGroup.allowed(i));assertEquals(labels[i],PlaybackGroup.label(i));
   SPDSettings.playbackSpeed(i);
   GameSettings.set(new HeadlessPreferences(new FileHandle(file)));
   assertEquals(i,SPDSettings.playbackSpeed());
   PlaybackGroup.speed(SPDSettings.playbackSpeed());
   Game.elapsed=.016f; float[] observed={0};
   PlaybackGroup g=new PlaybackGroup();g.add(observe(observed));g.update();
   assertEquals(.016f*factors[i],observed[0],.0001f);
   assertEquals(.016f,Game.elapsed,0f);
  }
  assertFalse(PlaybackGroup.allowed(labels.length));
  // Simulate opening the same preferences in a production build.
  ApdFeatures.parse("");
  assertEquals(3,PlaybackGroup.choiceCount());
  assertEquals(1,SPDSettings.playbackSpeed());
  PlaybackGroup.speed(9);
  Game.elapsed=.016f;float[] observed={0};PlaybackGroup g=new PlaybackGroup();g.add(observe(observed));g.update();
  assertEquals(.016f,observed[0],0.00001f);
 }
 @Test public void extremeVisualPlaybackPreservesNormalHeroIdleTiming(){
  ApdFeatures.parse("testBuild\\n");
  for(int mode=0;mode<PlaybackGroup.choiceCount();mode++){
   PlaybackGroup.speed(mode);
   float[] seen={0f,0f};
   Game.elapsed=.016f;
   assertEquals("Outside a PlaybackGroup the idle animation is unscaled",.016f,PlaybackGroup.heroIdleElapsed(),0f);
   PlaybackGroup visuals=new PlaybackGroup();
   visuals.add(new Gizmo(){@Override public void update(){
    seen[0]=Game.elapsed;
    seen[1]=PlaybackGroup.heroIdleElapsed();
   }});
   visuals.update();
   float factor=new float[]{.5f,1f,2f,4f,8f,16f,32f,64f,100f,1000f}[mode];
   assertEquals(.016f*factor,seen[0],.0001f);
   assertEquals("Preserve 0.5x/1x/2x idle; cap developer-only extremes at 1x",
     .016f*(factor<=2f?factor:1f),seen[1],.0001f);
   assertEquals(.016f,Game.elapsed,0f);
  }
 }

 @Test public void extremeModesNeverChangeActorClockGlobalTimescaleOrHud(){
  ApdFeatures.parse("testBuild\n");
  float clock=Actor.now(),timeScale=Game.timeScale,timeTotal=Game.timeTotal;
  for(int mode:new int[]{3,4,5,6,7,8,9}){
   PlaybackGroup.speed(mode);Game.elapsed=.02f;
   Group parent=new Group();PlaybackGroup visuals=new PlaybackGroup();float[] visual={0},hud={0};
   visuals.add(observe(visual));parent.add(visuals);parent.add(observe(hud));parent.update();
   assertTrue("Even 1000x should advance visuals",visual[0]>.02f);
   assertEquals(.02f,hud[0],0f);
   assertEquals(.02f,Game.elapsed,0f);
   assertEquals(clock,Actor.now(),0f);assertEquals(timeScale,Game.timeScale,0f);
   assertEquals(timeTotal,Game.timeTotal,0f);
  }
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
