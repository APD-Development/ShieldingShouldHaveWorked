package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TitleMenuLayoutDeviceTest {
    private static Object field(Object object,String name)throws Exception {
        for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass()){
            try{Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(object);}
            catch(NoSuchFieldException inherited){}
        }
        throw new NoSuchFieldException(name);
    }
    private static void waitFor(java.util.concurrent.Callable<Boolean> condition)throws Exception {
        long end=SystemClock.uptimeMillis()+120000;
        while(SystemClock.uptimeMillis()<end){if(condition.call())return;Thread.sleep(150);}
        fail("Native title condition did not become true");
    }
    private static void nativeTap(Point p)throws Exception {
        long down=SystemClock.uptimeMillis();
        for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP}){
            MotionEvent e=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,p.x,p.y,0);
            e.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            try{assertTrue(InstrumentationRegistry.getInstrumentation().getUiAutomation().injectInputEvent(e,true));}
            finally{e.recycle();}
            if(action==MotionEvent.ACTION_DOWN)Thread.sleep(80);
        }
    }
    private static Point centre(String name)throws Exception {
        Point[] p=new Point[1];
        TopScoresDeviceTest.render(()->{try{
            Component b=(Component)field(Game.scene(),name);
            p[0]=b.camera().cameraToScreen(b.left()+b.width()/2,b.top()+b.height()/2);
        }catch(Exception e){throw new RuntimeException(e);}});return p[0];
    }
    @Test public void titleControlsStaySeparateAndNativeChangesAndHideUiRemainUsable()throws Exception {
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
            waitFor(()->Game.scene()!=null && PixelScene.uiCamera!=null);
            Thread.sleep(1000);
            TopScoresDeviceTest.render(()->ShatteredPixelDungeon.switchNoFade(TitleScene.class));
            waitFor(()->Game.scene() instanceof TitleScene);Thread.sleep(1000);
            TopScoresDeviceTest.render(()->{try{
                List<Component> buttons=new ArrayList<>();
                for(String name:new String[]{"btnPlay","btnStatistics","btnBadges","btnJournal","btnOptions","btnQuit","btnAPDSettings","btnCommunity","btnNews","btnChanges","btnAbout","btnFade"}){
                    Component b=(Component)field(Game.scene(),name);buttons.add(b);
                    assertTrue(name+" visible and enabled",b.visible && b.active);
                    assertTrue(name+" inside actual viewport",b.left()>=0 && b.top()>=0 && b.right()<=b.camera().width+0.01 && b.bottom()<=b.camera().height+0.01);
                    assertTrue(name+" usable target",b.width()>=16 && b.height()>=16);
                    if(b instanceof StyledButton){
                        com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock text=(com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)field(b,"text");
                        assertTrue(name+" label fits horizontally",text.left()>=b.left()-0.5 && text.right()<=b.right()+0.5);
                        assertTrue(name+" label fits vertically",text.top()>=b.top()-0.5 && text.bottom()<=b.bottom()+0.5);
                    }
                }
                for(int i=0;i<buttons.size();i++)for(int j=i+1;j<buttons.size();j++){
                    Component a=buttons.get(i),b=buttons.get(j);
                    assertFalse("Main-menu controls overlap: "+i+"/"+j,a.left()<b.right() && b.left()<a.right() && a.top()<b.bottom() && b.top()<a.bottom());
                }
            }catch(Exception e){throw new RuntimeException(e);}});
            TopScoresDeviceTest.screenshot("main-title-layout.png");
            nativeTap(centre("btnChanges"));waitFor(()->Game.scene() instanceof APDTextScene);
            assertEquals(APDTextScene.Page.CHANGES,APDTextScene.page);
            InstrumentationRegistry.getInstrumentation().getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
            waitFor(()->Game.scene() instanceof TitleScene);Thread.sleep(800);
            nativeTap(centre("btnFade"));Thread.sleep(900);
            TopScoresDeviceTest.render(()->{try{assertFalse(((Component)field(Game.scene(),"btnChanges")).active);}catch(Exception e){throw new RuntimeException(e);}});
            TopScoresDeviceTest.screenshot("main-title-hidden.png");
            Point[] reset=new Point[1];TopScoresDeviceTest.render(()->reset[0]=com.watabou.noosa.Camera.main.cameraToScreen(com.watabou.noosa.Camera.main.width/2f,com.watabou.noosa.Camera.main.height/2f));
            nativeTap(reset[0]);waitFor(()->{try{return ((Component)field(Game.scene(),"btnChanges")).active;}catch(Exception e){return false;}});
            TopScoresDeviceTest.render(()->{try{assertTrue(((Component)field(Game.scene(),"btnChanges")).active);}catch(Exception e){throw new RuntimeException(e);}});
            TopScoresDeviceTest.screenshot("main-title-restored.png");
            // A physical finger can land beside the tiny icon. This native press must still
            // hide the menu and allow the existing restore behavior to run afterward.
            Point[] outerEdge=new Point[1];
            TopScoresDeviceTest.render(()->{try{
                Component icon=(Component)field(Game.scene(),"btnFade");
                com.watabou.noosa.PointerArea target=(com.watabou.noosa.PointerArea)field(icon,"hotArea");
                assertTrue("Footer hit area reaches outside the visible icon",target.x+4<icon.left());
                assertTrue("Footer hit area extends upward",target.y<icon.top());
                outerEdge[0]=target.camera().cameraToScreen(icon.left()-2,target.y+target.height/2);
            }catch(Exception e){throw new RuntimeException(e);}});
            nativeTap(outerEdge[0]);waitFor(()->{try{return !((Component)field(Game.scene(),"btnChanges")).active;}catch(Exception e){return false;}});
            TopScoresDeviceTest.render(()->{try{
                assertFalse("Outside-icon touch must hide UI",((Component)field(Game.scene(),"btnChanges")).active);
            }catch(Exception e){throw new RuntimeException(e);}});
            nativeTap(reset[0]);waitFor(()->{try{return ((Component)field(Game.scene(),"btnChanges")).active;}catch(Exception e){return false;}});
            TopScoresDeviceTest.render(()->{try{
                assertTrue("UI must restore after expanded touch",((Component)field(Game.scene(),"btnChanges")).active);
            }catch(Exception e){throw new RuntimeException(e);}});
            final boolean waterWas=com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmWaterskin();
            final boolean trapWas=com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmKnownTraps();
            try{
                TopScoresDeviceTest.render(()->{com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmWaterskin(true);com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmKnownTraps(true);com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings.last_index=1;});
                nativeTap(centre("btnOptions"));Thread.sleep(500);
                Point[] toggles=new Point[2];
                TopScoresDeviceTest.render(()->{try{
                    List<com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox> boxes=TopScoresDeviceTest.find(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox.class);
                    for(com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox b:boxes){
                        int index=b.text().equals("Confirm Waterskin")?0:b.text().equals("Warn about traps")?1:-1;
                        if(index>=0){assertTrue(b.checked());assertTrue(b.visible && b.active);toggles[index]=b.camera().cameraToScreen(b.left()+b.width()/2,b.top()+b.height()/2);}
                    }
                    assertNotNull(toggles[0]);assertNotNull(toggles[1]);
                }catch(Exception e){throw new RuntimeException(e);}});
                TopScoresDeviceTest.screenshot("options-confirmations.png");
                nativeTap(toggles[0]);waitFor(()->!com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmWaterskin());assertFalse(com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmWaterskin());assertTrue(com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmKnownTraps());
                nativeTap(toggles[1]);waitFor(()->!com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmKnownTraps());assertFalse(com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmKnownTraps());
                InstrumentationRegistry.getInstrumentation().getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
                Thread.sleep(1200);waitFor(()->Game.scene() instanceof TitleScene);
                nativeTap(centre("btnOptions"));Thread.sleep(500);
                TopScoresDeviceTest.render(()->{try{
                    for(com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox b:TopScoresDeviceTest.find(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox.class))
                        if(b.text().equals("Confirm Waterskin") || b.text().equals("Warn about traps"))assertFalse("Preference retained on reopening",b.checked());
                }catch(Exception e){throw new RuntimeException(e);}});
                InstrumentationRegistry.getInstrumentation().getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
                Thread.sleep(1000);
            }finally{TopScoresDeviceTest.render(()->{com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmWaterskin(waterWas);com.shatteredpixel.shatteredpixeldungeon.SPDSettings.confirmKnownTraps(trapWas);});}

        }
    }
}
