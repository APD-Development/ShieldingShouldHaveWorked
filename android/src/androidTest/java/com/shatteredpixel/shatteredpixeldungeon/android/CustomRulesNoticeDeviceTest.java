package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;
import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRulesPresentation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCustomDungeonRules;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.*;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class CustomRulesNoticeDeviceTest {
    private static void render(Runnable task)throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<Throwable> failure=new AtomicReference<>();
        Game.runOnRenderThread(()->{try{task.run();}catch(Throwable t){failure.set(t);}finally{done.countDown();}});
        assertTrue(done.await(60,TimeUnit.SECONDS));if(failure.get()!=null)throw new AssertionError(failure.get());
    }
    @SuppressWarnings("unchecked") private static List<Gizmo> members(Group group)throws Exception {
        Field f=Group.class.getDeclaredField("members");f.setAccessible(true);
        return new ArrayList<>((List<Gizmo>)f.get(group));
    }
    private static <T> List<T> find(Group group,Class<T> type)throws Exception {
        List<T> found=new ArrayList<>();
        for(Gizmo child:members(group)){if(type.isInstance(child))found.add(type.cast(child));if(child instanceof Group)found.addAll(find((Group)child,type));}
        return found;
    }
    private static Object field(Object object,String name)throws Exception {
        Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
    }
    private static void tap(Point point)throws Exception {
        PointerEvent.addPointerEvent(new PointerEvent(point.x,point.y,11,PointerEvent.Type.DOWN,PointerEvent.LEFT));
        Thread.sleep(150);
        PointerEvent.addPointerEvent(new PointerEvent(point.x,point.y,11,PointerEvent.Type.UP,PointerEvent.LEFT));
        Thread.sleep(500);
    }
    private static void screenshot(String name)throws Exception {
        android.content.Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap bitmap=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();assertNotNull(bitmap);
        android.content.ContentValues values=new android.content.ContentValues();
        values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME,name);
        values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE,"image/png");
        values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,"Pictures/APD-Test-Visuals");
        android.net.Uri uri=context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);assertNotNull(uri);
        try(java.io.OutputStream out=context.getContentResolver().openOutputStream(uri)) {assertNotNull(out);assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
        finally{bitmap.recycle();}
    }
    @Test public void noticeStaysAboveUsableControlsAndDoesNotChangeLockedRunOrScores()throws Exception {
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)) {
            long deadline=System.currentTimeMillis()+120000;
            while((Game.scene()==null || PixelScene.uiCamera==null)&&System.currentTimeMillis()<deadline)Thread.sleep(200);
            assertNotNull(Game.scene());Thread.sleep(1500);
            CustomDungeonRules saved=SPDSettings.apdCustomRules();Bundle run=new Bundle();
            final WndCustomDungeonRules[] windows=new WndCustomDungeonRules[1];final int[] dimensions=new int[2];
            final int[] originalScore=new int[1];final CustomDungeonRules[] lockedRules=new CustomDungeonRules[1];
            final AtomicReference<Point> touch=new AtomicReference<>();
            try {
                render(()->{
                    Dungeon.apdStoreCustomRules(run);dimensions[0]=PixelScene.uiCamera.width;dimensions[1]=PixelScene.uiCamera.height;
                    originalScore[0]=com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore;lockedRules[0]=Dungeon.customRules();
                    SPDSettings.apdCustomRules(CustomDungeonRules.defaults());
                    windows[0]=new WndCustomDungeonRules();Game.scene().addToFront(windows[0]);
                    try {
                        RenderedTextBlock notice=(RenderedTextBlock)field(windows[0],"scoringNotice");
                        ScrollPane scroll=(ScrollPane)field(windows[0],"scroll");
                        assertEquals(CustomRulesPresentation.noticeText(),notice.text());
                        assertTrue(notice.visible);assertEquals(0,notice.top(),0);
                        List<OptionSlider> sliders=find(windows[0],OptionSlider.class);assertEquals(18,sliders.size());
                        assertTrue(sliders.get(0).active);assertFalse(sliders.get(1).active);
                        OptionSlider first=sliders.get(0);assertTrue(notice.bottom()<first.top());
                        scroll.scrollTo(0,Math.max(0,first.top()-20));
                        touch.set(first.camera().cameraToScreen(first.right()-3,first.bottom()-7));
                    }catch(Exception e){throw new RuntimeException(e);}
                });
                tap(touch.get());
                render(()->{try {
                    assertEquals(19,find(windows[0],OptionSlider.class).get(0).getSelectedValue());
                    assertTrue("Draft changes do not persist without saving",SPDSettings.apdCustomRules().isNeutral());
                    ScrollPane scroll=(ScrollPane)field(windows[0],"scroll");
                    OptionSlider last=find(windows[0],OptionSlider.class).get(17);scroll.scrollTo(0,Math.max(0,last.top()-20));
                    assertTrue(((RenderedTextBlock)field(windows[0],"scoringNotice")).bottom()<last.top());
                    scroll.scrollTo(0,0);
                }catch(Exception e){throw new RuntimeException(e);}});
                Thread.sleep(750);screenshot("custom-rules-notice.png");
                render(()->{
                    windows[0].onBackPressed();
                    assertTrue(SPDSettings.apdCustomRules().isNeutral());
                    assertSame("Editing next-run draft does not replace active rules",lockedRules[0],Dungeon.customRules());
                    assertEquals(originalScore[0],com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore);
                    PixelScene.uiCamera.width=102;PixelScene.uiCamera.height=160;
                    windows[0]=new WndCustomDungeonRules(true);Game.scene().addToFront(windows[0]);
                    try {
                        RenderedTextBlock notice=(RenderedTextBlock)field(windows[0],"scoringNotice");
                        ScrollPane scroll=(ScrollPane)field(windows[0],"scroll");
                        assertEquals(CustomRulesPresentation.noticeText(),notice.text());
                        assertTrue(notice.width()<=74);assertEquals(0,notice.top(),0);
                        for(OptionSlider slider:find(windows[0],OptionSlider.class))assertFalse(slider.active);
                    }catch(Exception e){throw new RuntimeException(e);}
                });
                Thread.sleep(750);screenshot("custom-rules-notice-narrow.png");
                render(()->{try {
                    ScrollPane scroll=(ScrollPane)field(windows[0],"scroll");
                    RenderedTextBlock notice=(RenderedTextBlock)field(windows[0],"scoringNotice");
                    RenderedTextBlock scoring=PixelScene.renderTextBlock(CustomRulesPresentation.SCORING_NOTICE,6);
                    scoring.maxWidth((int)scroll.width());
                    float scoreTop=notice.bottom()-scoring.height();
                    scroll.scrollTo(0,Math.max(0,scoreTop-2));
                    assertTrue("The whole scoring paragraph fits the narrow scroll viewport",notice.bottom()<=scroll.content().camera().scroll.y+scroll.height());
                    assertEquals(CustomRulesPresentation.noticeText(),notice.text());
                    scoring.destroy();
                }catch(Exception e){throw new RuntimeException(e);}});
                Thread.sleep(750);screenshot("custom-rules-notice-narrow-scrolled.png");
            } finally {
                render(()->{
                    if(windows[0]!=null)windows[0].hide();
                    PixelScene.uiCamera.width=dimensions[0];PixelScene.uiCamera.height=dimensions[1];
                    SPDSettings.apdCustomRules(saved);Dungeon.apdRestoreCustomRules(run);
                });
            }
        }
    }
}
