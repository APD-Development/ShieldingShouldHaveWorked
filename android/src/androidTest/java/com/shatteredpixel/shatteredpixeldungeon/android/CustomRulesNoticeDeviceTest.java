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
        for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass()) {
            try {Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(object);}
            catch(NoSuchFieldException inherited) { }
        }
        throw new NoSuchFieldException(name);
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
                        for(OptionSlider slider:sliders)assertTrue("Every next-run control is implemented",slider.active);
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
                    scroll.scrollTo(0,Math.max(0,notice.bottom()-scroll.height()));
                    assertTrue("The final scoring notice line remains reachable in the narrow scroll viewport",notice.bottom()<=scroll.content().camera().scroll.y+scroll.height());
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
    @Test public void equipmentControlAcceptsTouchAndSavesOnlyNextRunPreferences() throws Exception {
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)) {
            long deadline=System.currentTimeMillis()+120000;
            while((Game.scene()==null || PixelScene.uiCamera==null)&&System.currentTimeMillis()<deadline)Thread.sleep(200);
            assertNotNull(Game.scene());Thread.sleep(1500);
            CustomDungeonRules saved=SPDSettings.apdCustomRules();Bundle run=new Bundle();
            final WndCustomDungeonRules[] window=new WndCustomDungeonRules[1];
            final CustomDungeonRules[] active=new CustomDungeonRules[1];final int[] score=new int[1];
            AtomicReference<Point> sliderTouch=new AtomicReference<>(),saveTouch=new AtomicReference<>();
            try {
                render(()->{try {
                    Dungeon.apdStoreCustomRules(run);active[0]=Dungeon.customRules();score[0]=com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore;
                    SPDSettings.apdCustomRules(CustomDungeonRules.defaults());window[0]=new WndCustomDungeonRules();Game.scene().addToFront(window[0]);
                    OptionSlider equipment=find(window[0],OptionSlider.class).get(CustomDungeonRules.Slider.EQUIPMENT.ordinal());
                    assertTrue(equipment.active);assertTrue(equipment.visible);
                    ((ScrollPane)field(window[0],"scroll")).scrollTo(0,Math.max(0,equipment.top()-20));
                    sliderTouch.set(equipment.camera().cameraToScreen(equipment.right()-3,equipment.bottom()-7));
                    for(RedButton button:find(window[0],RedButton.class))if(button.text().equals("Save for new runs"))
                        saveTouch.set(button.camera().cameraToScreen(button.left()+button.width()/2,button.top()+button.height()/2));
                    assertNotNull(saveTouch.get());
                }catch(Exception e){throw new RuntimeException(e);}});
                tap(sliderTouch.get());
                render(()->{try {
                    assertEquals(7,find(window[0],OptionSlider.class).get(CustomDungeonRules.Slider.EQUIPMENT.ordinal()).getSelectedValue());
                    assertEquals(100,SPDSettings.apdCustomRules().get(CustomDungeonRules.Slider.EQUIPMENT));
                    assertSame(active[0],Dungeon.customRules());
                }catch(Exception e){throw new RuntimeException(e);}});
                screenshot("custom-rules-equipment.png");tap(saveTouch.get());
                render(()->{
                    assertEquals(200,SPDSettings.apdCustomRules().get(CustomDungeonRules.Slider.EQUIPMENT));
                    assertSame(active[0],Dungeon.customRules());assertEquals(score[0],com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore);
                });
            } finally {
                render(()->{if(window[0]!=null)window[0].hide();SPDSettings.apdCustomRules(saved);Dungeon.apdRestoreCustomRules(run);});
            }
        }
    }

    private static void androidSwipe(Point from,Point to,int duration,int steps,boolean cancel)throws Exception {
        android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
        long down=android.os.SystemClock.uptimeMillis();
        android.view.MotionEvent event=android.view.MotionEvent.obtain(down,down,android.view.MotionEvent.ACTION_DOWN,from.x,from.y,0);
        event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);instrumentation.sendPointerSync(event);event.recycle();
        for(int i=1;i<=steps;i++){
            Thread.sleep(Math.max(1,duration/steps));
            event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),android.view.MotionEvent.ACTION_MOVE,
                    from.x+(to.x-from.x)*i/(float)steps,from.y+(to.y-from.y)*i/(float)steps,0);
            event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);instrumentation.sendPointerSync(event);event.recycle();
        }
        event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),cancel?android.view.MotionEvent.ACTION_CANCEL:android.view.MotionEvent.ACTION_UP,to.x,to.y,0);
        event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);instrumentation.sendPointerSync(event);event.recycle();Thread.sleep(400);
    }
    @Test public void touchscreenSliderGesturesLockDirectionAndPreserveDraftAndSavedRules()throws Exception {
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)) {
            long deadline=System.currentTimeMillis()+120000;
            while((Game.scene()==null || PixelScene.uiCamera==null)&&System.currentTimeMillis()<deadline)Thread.sleep(200);
            assertNotNull(Game.scene());Thread.sleep(1500);
            CustomDungeonRules saved=SPDSettings.apdCustomRules();final WndCustomDungeonRules[] window=new WndCustomDungeonRules[1];
            final CustomDungeonRules active=Dungeon.customRules();final int score=com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore;
            final Point[] points=new Point[2];final float[] beforeScroll=new float[1];final int[] beforeValue=new int[1];
            // Vertical, vertical diagonal, long, short, rapid; horizontal and horizontal diagonal; tap and cancel.
            int[][] gestures={{0,-18,180,12},{10,-25,180,12},{2,-70,220,16},{0,-2,25,1},
                    {1,-18,30,3},{35,0,180,12},{35,-9,180,12},{0,0,30,1},{35,0,180,12}};
            try {
                for(int n=0;n<gestures.length;n++){
                    final int[] gesture=gestures[n];final boolean cancelled=n==gestures.length-1;
                    render(()->{try {
                        if(window[0]!=null)window[0].hide();SPDSettings.apdCustomRules(CustomDungeonRules.defaults());
                        window[0]=new WndCustomDungeonRules();Game.scene().addToFront(window[0]);
                        OptionSlider slider=find(window[0],OptionSlider.class).get(0);ScrollPane scroll=(ScrollPane)field(window[0],"scroll");
                        scroll.scrollTo(0,Math.max(0,slider.top()-24));beforeScroll[0]=scroll.content().camera().scroll.y;beforeValue[0]=slider.getSelectedValue();
                        points[0]=slider.camera().cameraToScreen(slider.left()+slider.width()*0.35f,slider.bottom()-7);
                        points[1]=slider.camera().cameraToScreen(slider.left()+slider.width()*0.35f+gesture[0],slider.bottom()-7+gesture[1]);
                    }catch(Exception e){throw new RuntimeException(e);}});
                    androidSwipe(points[0],points[1],gesture[2],gesture[3],cancelled);
                    final boolean vertical=n<5;
                    render(()->{try {
                        OptionSlider slider=find(window[0],OptionSlider.class).get(0);ScrollPane scroll=(ScrollPane)field(window[0],"scroll");
                        if(vertical){assertEquals("Vertical touch must not adjust a slider",beforeValue[0],slider.getSelectedValue());
                            assertTrue("Vertical touch over slider scrolls the pane",scroll.content().camera().scroll.y>beforeScroll[0]);}
                        else {assertEquals("Horizontal/tap touch must not scroll",beforeScroll[0],scroll.content().camera().scroll.y,0.01f);
                            if(cancelled)assertEquals(beforeValue[0],slider.getSelectedValue());else assertNotEquals(beforeValue[0],slider.getSelectedValue());}
                        assertTrue("Draft stays unsaved",SPDSettings.apdCustomRules().isNeutral());assertSame(active,Dungeon.customRules());
                        assertEquals(score,com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore);
                    }catch(Exception e){throw new RuntimeException(e);}});
                }
                screenshot("custom-rules-touch-gestures.png");
            }finally{render(()->{if(window[0]!=null)window[0].hide();SPDSettings.apdCustomRules(saved);});}
        }
    }

    private static void androidActivityTap(ActivityScenario<AndroidLauncher> scenario,Point point) throws Exception {
        final java.util.concurrent.atomic.AtomicBoolean accepted=new java.util.concurrent.atomic.AtomicBoolean();
        final long down=android.os.SystemClock.uptimeMillis();
        scenario.onActivity(activity->{
            android.view.MotionEvent event=android.view.MotionEvent.obtain(down,down,android.view.MotionEvent.ACTION_DOWN,point.x,point.y,0);
            event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);
            accepted.set(activity.dispatchTouchEvent(event));event.recycle();
        });
        assertTrue("Android activity accepted native touch down",accepted.get());
        Thread.sleep(150);
        accepted.set(false);
        scenario.onActivity(activity->{
            android.view.MotionEvent event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),android.view.MotionEvent.ACTION_UP,point.x,point.y,0);
            event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);
            accepted.set(activity.dispatchTouchEvent(event));event.recycle();
        });
        assertTrue("Android activity accepted native touch up",accepted.get());
        Thread.sleep(400);
    }

    @Test public void allEighteenControlsAcceptNativeTouchSaveAndReopenWithoutChangingActiveRun() throws Exception {
        try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
            long deadline = System.currentTimeMillis() + 120000;
            while ((Game.scene() == null || PixelScene.uiCamera == null) && System.currentTimeMillis() < deadline) Thread.sleep(200);
            assertNotNull(Game.scene());
            Thread.sleep(1500);
            final CustomDungeonRules saved = SPDSettings.apdCustomRules();
            final Bundle run = new Bundle();
            final WndCustomDungeonRules[] window = new WndCustomDungeonRules[1];
            final CustomDungeonRules[] active = new CustomDungeonRules[1];
            final int[] score = new int[1];
            final Point[] touch = new Point[1];
            try {
                render(() -> {
                    Dungeon.apdStoreCustomRules(run);
                    active[0] = Dungeon.customRules();
                    score[0] = com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore;
                    SPDSettings.apdCustomRules(CustomDungeonRules.defaults());
                });
                for (int phase = 0; phase < 2; phase++) {
                    final boolean maximum = phase == 0;
                    final CustomDungeonRules persisted = SPDSettings.apdCustomRules();
                    render(() -> {
                        window[0] = new WndCustomDungeonRules();
                        Game.scene().addToFront(window[0]);
                    });
                    for (final CustomDungeonRules.Slider rule : CustomDungeonRules.Slider.values()) {
                        render(() -> { try {
                            List<OptionSlider> sliders = find(window[0], OptionSlider.class);
                            assertEquals(18, sliders.size());
                            OptionSlider slider = sliders.get(rule.ordinal());
                            assertTrue(rule.name(), slider.active && slider.visible);
                            ScrollPane scroll = (ScrollPane) field(window[0], "scroll");
                            scroll.scrollTo(0, Math.max(0, slider.top() - 24));
                            touch[0] = slider.camera().cameraToScreen(maximum ? slider.right() - 3 : slider.left() + 2, slider.bottom() - 7);
                        } catch (Exception e) { throw new RuntimeException(e); } });
                        androidActivityTap(scenario,touch[0]);
                        render(() -> { try {
                            int value = maximum ? rule.max : rule.min;
                            OptionSlider slider = find(window[0], OptionSlider.class).get(rule.ordinal());
                            assertEquals(rule.name(), CustomRulesPresentation.position(rule, value), slider.getSelectedValue());
                            assertEquals(rule.name(), value, ((CustomDungeonRules) field(window[0], "draft")).get(rule));
                            assertArrayEquals("Native touch changes only the draft", persisted.values(), SPDSettings.apdCustomRules().values());
                            assertSame("Active run remains locked", active[0], Dungeon.customRules());
                            assertEquals(score[0], com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore);
                        } catch (Exception e) { throw new RuntimeException(e); } });
                    }
                    render(() -> { try {
                        touch[0] = null;
                        for (RedButton button : find(window[0], RedButton.class)) {
                            if (button.text().equals("Save for new runs")) touch[0] = button.camera().cameraToScreen(button.left() + button.width()/2, button.top() + button.height()/2);
                        }
                        assertNotNull(touch[0]);
                    } catch (Exception e) { throw new RuntimeException(e); } });
                    androidActivityTap(scenario,touch[0]);
                    render(() -> { try {
                        for (CustomDungeonRules.Slider rule : CustomDungeonRules.Slider.values()) assertEquals(rule.name(), maximum ? rule.max : rule.min, SPDSettings.apdCustomRules().get(rule));
                        assertSame(active[0], Dungeon.customRules());
                        assertEquals(score[0], com.shatteredpixel.shatteredpixeldungeon.Statistics.totalScore);
                        window[0] = new WndCustomDungeonRules();
                        Game.scene().addToFront(window[0]);
                        for (CustomDungeonRules.Slider rule : CustomDungeonRules.Slider.values()) {
                            int value = maximum ? rule.max : rule.min;
                            assertEquals(rule.name(), CustomRulesPresentation.position(rule, value), find(window[0], OptionSlider.class).get(rule.ordinal()).getSelectedValue());
                        }
                        window[0].onBackPressed();
                    } catch (Exception e) { throw new RuntimeException(e); } });
                }
            } finally {
                render(() -> {
                    if (window[0] != null) window[0].hide();
                    SPDSettings.apdCustomRules(saved);
                    Dungeon.apdRestoreCustomRules(run);
                });
            }
        }
    }

    @Test public void healingConversionSettingExplainsItsExactScopeWithoutChangingSavedValue() throws Exception {
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)) {
            long deadline=System.currentTimeMillis()+120000;
            while((Game.scene()==null || PixelScene.uiCamera==null)&&System.currentTimeMillis()<deadline)Thread.sleep(200);
            assertNotNull(Game.scene());Thread.sleep(1500);
            final com.shatteredpixel.shatteredpixeldungeon.windows.WndAPDSettings[] window=new com.shatteredpixel.shatteredpixeldungeon.windows.WndAPDSettings[1];
            final int[] saved=new int[1];
            try {
                render(()->{try {
                    saved[0]=SPDSettings.apdExoticSetting();
                    window[0]=new com.shatteredpixel.shatteredpixeldungeon.windows.WndAPDSettings();
                    Game.scene().addToFront(window[0]);
                    com.shatteredpixel.shatteredpixeldungeon.windows.APDSettingsPane pane=find(window[0],com.shatteredpixel.shatteredpixeldungeon.windows.APDSettingsPane.class).get(0);
                    RenderedTextBlock info=(RenderedTextBlock)field(pane,"info");
                    OptionSlider slider=(OptionSlider)field(pane,"optExotic");
                    RenderedTextBlock title=(RenderedTextBlock)field(slider,"title");
                    assertEquals("Healing to Shielding chance (new runs)",title.text());
                    assertTrue(info.text().contains("generated Potion of Healing"));
                    assertTrue(info.text().contains("Other potions and scrolls keep normal SPD behavior"));
                    assertTrue(info.width()<=pane.width());
                    assertTrue(info.top()>slider.bottom());
                    assertEquals(saved[0]/10,slider.getSelectedValue());
                }catch(Exception e){throw new RuntimeException(e);}});
                Thread.sleep(750);screenshot("healing-conversion-setting.png");
                render(()->{window[0].onBackPressed();assertEquals(saved[0],SPDSettings.apdExoticSetting());});
            } finally {render(()->{if(window[0]!=null)window[0].hide();});}
        }
    }
    @Test public void liveMultipliersMatchEveryPositionAndUpdateDuringNativeDragBeforeRelease()throws Exception{
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
            long deadline=System.currentTimeMillis()+120000;
            while((Game.scene()==null||PixelScene.uiCamera==null)&&System.currentTimeMillis()<deadline)Thread.sleep(200);
            assertNotNull(Game.scene());Thread.sleep(750);
            final CustomDungeonRules[] saved=new CustomDungeonRules[1];final Bundle run=new Bundle();
            final WndCustomDungeonRules[] window=new WndCustomDungeonRules[1];final Point[] touch=new Point[2];
            try{
                render(()->{try{
                    saved[0]=SPDSettings.apdCustomRules();Dungeon.apdStoreCustomRules(run);SPDSettings.apdCustomRules(CustomDungeonRules.defaults());
                    window[0]=new WndCustomDungeonRules();Game.scene().addToFront(window[0]);
                    java.lang.reflect.Method change=null;
                    for(CustomDungeonRules.Slider rule:CustomDungeonRules.Slider.values()){
                        OptionSlider slider=find(window[0],OptionSlider.class).get(rule.ordinal());
                        change=slider.getClass().getDeclaredMethod("onChange");change.setAccessible(true);
                        for(int value=rule.min;value<=rule.max;value+=rule.step()){
                            slider.setSelectedValue(CustomRulesPresentation.position(rule,value));change.invoke(slider);
                            CustomDungeonRules draft=(CustomDungeonRules)field(window[0],"draft");assertEquals(value,draft.get(rule));
                            String individual=CustomRulesPresentation.multiplierText(rule,value);boolean caption=false;
                            for(RenderedTextBlock text:find(window[0],RenderedTextBlock.class))if(text.text().startsWith(CustomRulesPresentation.label(rule)+": ")&&text.text().contains(individual))caption=true;
                            assertTrue(rule+"="+value,caption);
                            RenderedTextBlock combined=(RenderedTextBlock)field(window[0],"combined");
                            assertTrue(combined.text().endsWith(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRuleMultipliers.format(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRuleMultipliers.combined(draft))));
                            assertTrue(combined.right()<=window[0].width()+0.01f);
                        }
                    }
                    window[0].hide();SPDSettings.apdCustomRules(CustomDungeonRules.defaults());window[0]=new WndCustomDungeonRules();Game.scene().addToFront(window[0]);
                    OptionSlider first=find(window[0],OptionSlider.class).get(0);ScrollPane scroll=(ScrollPane)field(window[0],"scroll");scroll.scrollTo(0,first.top()-4);
                    touch[0]=first.camera().cameraToScreen(first.left()+first.width()*0.16f,first.bottom()-7);
                    touch[1]=first.camera().cameraToScreen(first.left()+first.width()*0.65f,first.bottom()-7);
                }catch(Exception e){throw new RuntimeException(e);}});
                Thread.sleep(500);android.app.Instrumentation in=InstrumentationRegistry.getInstrumentation();long down=android.os.SystemClock.uptimeMillis();
                android.view.MotionEvent event=android.view.MotionEvent.obtain(down,down,android.view.MotionEvent.ACTION_DOWN,touch[0].x,touch[0].y,0);event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);in.sendPointerSync(event);event.recycle();Thread.sleep(150);
                event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),android.view.MotionEvent.ACTION_MOVE,touch[1].x,touch[1].y,0);event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);in.sendPointerSync(event);event.recycle();Thread.sleep(400);
                render(()->{try{CustomDungeonRules draft=(CustomDungeonRules)field(window[0],"draft");assertNotEquals("Multiplier must update before finger release",100,draft.get(CustomDungeonRules.Slider.ENEMY_HP));assertTrue(SPDSettings.apdCustomRules().isNeutral());assertEquals(run.getInt("apd_custom_score_version"),Dungeon.customScoring()==null?0:Dungeon.customScoring().recordVersion);}catch(Exception e){throw new RuntimeException(e);}});
                screenshot("custom-rules-live-drag.png");
                event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),android.view.MotionEvent.ACTION_UP,touch[1].x,touch[1].y,0);event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);in.sendPointerSync(event);event.recycle();Thread.sleep(250);
                render(()->{try{((ScrollPane)field(window[0],"scroll")).scrollTo(0,0);}catch(Exception e){throw new RuntimeException(e);}});Thread.sleep(250);screenshot("custom-rules-live-summary.png");
            }finally{render(()->{if(window[0]!=null)window[0].hide();if(saved[0]!=null)SPDSettings.apdCustomRules(saved[0]);Dungeon.apdRestoreCustomRules(run);});}
        }
    }

}
