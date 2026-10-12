package com.shatteredpixel.shatteredpixeldungeon.android;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdText;
import com.watabou.noosa.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class AboutDeviceTest {
 private void render(Runnable r)throws Exception{
  CountDownLatch done=new CountDownLatch(1);AtomicReference<Throwable> err=new AtomicReference<>();
  Game.runOnRenderThread(()->{try{r.run();}catch(Throwable t){err.set(t);}finally{done.countDown();}});
  assertTrue(done.await(30,TimeUnit.SECONDS));if(err.get()!=null)throw new AssertionError(err.get());
 }
 private List<Gizmo> children(Group group)throws Exception{
  Field f=Group.class.getDeclaredField("members");f.setAccessible(true);
  return new ArrayList<>((List<Gizmo>)f.get(group));
 }
 private RedButton find(Group group,String label)throws Exception{
  for(Gizmo g:children(group)){
   if(g instanceof RedButton && label.equals(((RedButton)g).text()))return (RedButton)g;
   if(g instanceof Group){RedButton b=find((Group)g,label);if(b!=null)return b;}
  }return null;
 }
 @Test public void everyAboutPopupOpensScrollsAndCloses()throws Exception{
  try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
   render(()->ShatteredPixelDungeon.switchScene(AboutScene.class));
   long end=System.currentTimeMillis()+15000;
   while(!(Game.scene() instanceof AboutScene)&&System.currentTimeMillis()<end)Thread.sleep(100);
   assertTrue(Game.scene() instanceof AboutScene);
   for(String label:new String[]{"Why APD Exists","Features & Vision","Community & Voting","Developer's Note","Development Roadmap","Source & Licenses"}){
    render(()->{try{
     RedButton button=find(Game.scene(),label);assertNotNull(label,button);
     assertTrue(button.width()>=40);assertTrue(button.height()>=20);
     Method click=button.getClass().getDeclaredMethod("onClick");click.setAccessible(true);click.invoke(button);
     WndUimsdText popup=null;
     for(Gizmo g:children(Game.scene()))if(g instanceof WndUimsdText && g.alive)popup=(WndUimsdText)g;
     assertNotNull(popup);assertSame(Game.scene(),popup.parent);assertTrue(popup.camera.width>0);
     for(Gizmo g:children(popup))if(g instanceof ScrollPane)((ScrollPane)g).scrollTo(0,1000);
     popup.hide();
    }catch(Exception e){throw new RuntimeException(e);}});
   }
   render(()->{try{assertNotNull(find(Game.scene(),"APD News"));assertNotNull(find(Game.scene(),"APD Changes"));}catch(Exception e){throw new RuntimeException(e);}});
  }
 }
 @Test public void offlineNewsCardsOpenDatedSpritePopups()throws Exception{
  try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
   render(()->{com.shatteredpixel.shatteredpixeldungeon.scenes.APDTextScene.page=com.shatteredpixel.shatteredpixeldungeon.scenes.APDTextScene.Page.NEWS;ShatteredPixelDungeon.switchScene(com.shatteredpixel.shatteredpixeldungeon.scenes.APDTextScene.class);});
   long end=System.currentTimeMillis()+15000;
   while(!(Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.APDTextScene)&&System.currentTimeMillis()<end)Thread.sleep(100);
   assertTrue(Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.APDTextScene);
   render(()->{try{
    int[] cards={0};checkNews(Game.scene(),cards);assertEquals(2,cards[0]);
   }catch(Exception e){throw new RuntimeException(e);}});
  }
 }
 private void checkNews(Group group,int[] count)throws Exception{
  for(Gizmo g:children(group)){
   if(g.getClass().getName().endsWith("$NewsCard")){
    count[0]++;assertTrue(((com.watabou.noosa.ui.Component)g).width()>0);assertTrue(((com.watabou.noosa.ui.Component)g).height()>=32);
    Method click=g.getClass().getDeclaredMethod("onClick");click.setAccessible(true);click.invoke(g);
    com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage popup=null;
    for(Gizmo w:children(Game.scene()))if(w instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage&&w.alive)popup=(com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage)w;
    assertNotNull(popup);assertSame(Game.scene(),popup.parent);popup.hide();
   }else if(g instanceof Group)checkNews((Group)g,count);
  }
 }
}
