package com.shatteredpixel.shatteredpixeldungeon.android;
import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.history.ApdHistory;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.*;
import com.watabou.noosa.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class ChangesVisualDeviceTest {
 private void render(Runnable action)throws Exception{
  CountDownLatch done=new CountDownLatch(1);AtomicReference<Throwable> error=new AtomicReference<>();
  Game.runOnRenderThread(()->{try{action.run();}catch(Throwable t){error.set(t);}finally{done.countDown();}});
  assertTrue(done.await(30,TimeUnit.SECONDS));if(error.get()!=null)throw new AssertionError(error.get());
 }
 private List<Gizmo> children(Group group)throws Exception{
  Field f=Group.class.getDeclaredField("members");f.setAccessible(true);return new ArrayList<>((List<Gizmo>)f.get(group));
 }
 private void waitScene(Class<?> type)throws Exception{
  long end=System.currentTimeMillis()+15000;while(!type.isInstance(Game.scene())&&System.currentTimeMillis()<end)Thread.sleep(100);assertTrue(type.isInstance(Game.scene()));Thread.sleep(500);
 }
 private void screenshot(String name)throws Exception{
  Bitmap bitmap=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();assertNotNull(bitmap);assertTrue(bitmap.getWidth()>=320&&bitmap.getHeight()>=320);
  File dir=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir(),"visual-review");assertTrue(dir.isDirectory()||dir.mkdirs());
  try(OutputStream out=new FileOutputStream(new File(dir,name))){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}finally{bitmap.recycle();}
  // Gradle may remove the target package after connected tests. Export while it still exists.
  String app=InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
  assertTrue(app.matches("[A-Za-z0-9_.]+"));assertTrue(name.matches("[a-z-]+\\.png"));
  String command="mkdir -p /sdcard/Download; run-as "+app+" cat files/visual-review/"+name+" > /sdcard/Download/"+name+"; wc -c /sdcard/Download/"+name;
  try(InputStream in=new android.os.ParcelFileDescriptor.AutoCloseInputStream(InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command))){
   ByteArrayOutputStream data=new ByteArrayOutputStream();byte[] b=new byte[1024];int n;while((n=in.read(b))!=-1)data.write(b,0,n);
   String result=new String(data.toByteArray(),java.nio.charset.StandardCharsets.UTF_8).trim();assertTrue(result,Integer.parseInt(result.split("\\s+")[0])>1000);
  }
 }
 @Test public void categoriesUseSpdColumnsAndEveryIconOpensItsExactBuildPopup()throws Exception{
  try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
   Field selected=APDTextScene.class.getDeclaredField("buildSelected");selected.setAccessible(true);selected.setInt(null,0);
   render(()->{APDTextScene.page=APDTextScene.Page.CHANGES;ShatteredPixelDungeon.switchScene(APDTextScene.class);});waitScene(APDTextScene.class);
   render(()->{try{
    List<ApdHistory.Build> builds=ApdHistory.builds(Gdx.files.internal("apd/history/index.tsv").readString("UTF-8"));
    ApdHistory.Build build=builds.get(0);List<ApdHistory.Entry> entries=ApdHistory.entries(Gdx.files.internal("apd/history/"+build.id+".tsv").readString("UTF-8"));
    Field f=APDTextScene.class.getDeclaredField("changes");f.setAccessible(true);List<ChangeInfo> infos=(List<ChangeInfo>)f.get(Game.scene());
    assertEquals(1+ApdHistory.categories(entries).size(),infos.size());
    if(infos.get(0).width()>=130&&infos.size()>2){assertEquals(0,infos.get(1).left(),0);assertEquals(infos.get(0).width()/2,infos.get(2).left(),0);assertEquals(infos.get(1).top(),infos.get(2).top(),0);}
    int count=0;Field messages=ChangeButton.class.getDeclaredField("messages");messages.setAccessible(true);Method click=ChangeButton.class.getDeclaredMethod("onClick");click.setAccessible(true);
    for(ChangeInfo info:infos)for(Gizmo child:children(info))if(child instanceof ChangeButton){
     ChangeButton button=(ChangeButton)child;assertTrue(button.left()>=info.left());assertTrue(button.right()<=info.right());
     assertTrue(((String[])messages.get(button))[0].startsWith(build.label+"\n\n"));assertTrue(info.onClick(button.centerX(),button.centerY()));
     WndChanges popup=null;for(Gizmo g:children(Game.scene()))if(g instanceof WndChanges&&g.alive)popup=(WndChanges)g;assertNotNull(popup);assertSame(Game.scene(),popup.parent);popup.hide();if(!info.major)count++;
    }
    assertEquals(entries.size(),count);
   }catch(Exception e){throw new RuntimeException(e);}});
   screenshot("apd-changes.png");
   render(()->{try{
    List<ApdHistory.Build> builds=ApdHistory.builds(Gdx.files.internal("apd/history/index.tsv").readString("UTF-8"));
    Method ranges=APDTextScene.class.getDeclaredMethod("showBuildRanges",List.class,int.class);ranges.setAccessible(true);ranges.invoke(Game.scene(),builds,0);
    com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList menu=null;
    for(Gizmo g:children(Game.scene()))if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList&&g.alive)menu=(com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList)g;
    assertNotNull(menu);Field entriesField=menu.getClass().getDeclaredField("entries");entriesField.setAccessible(true);assertTrue(((List<?>)entriesField.get(menu)).size()<=14);menu.hide();
   }catch(Exception e){throw new RuntimeException(e);}});

   render(()->{ChangesScene.changesSelected=0;ShatteredPixelDungeon.switchScene(ChangesScene.class);});waitScene(ChangesScene.class);screenshot("spd-changes-reference.png");
   render(()->{APDTextScene.page=APDTextScene.Page.CHANGES;ShatteredPixelDungeon.switchScene(APDTextScene.class);});waitScene(APDTextScene.class);
  }
 }
}
