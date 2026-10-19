package com.shatteredpixel.shatteredpixeldungeon.android;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdSearch;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.utils.Point;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** UI gesture acceptance for APD's entirely local searchable UIMSD. */
@RunWith(AndroidJUnit4.class)
public class UimsdSearchDeviceTest {
    private static void onRender(Runnable work) throws Exception {
        CountDownLatch latch=new CountDownLatch(1);
        AtomicReference<Throwable> thrown=new AtomicReference<>();
        Game.runOnRenderThread(()->{
            try{work.run();}catch(Throwable t){thrown.set(t);}finally{latch.countDown();}
        });
        assertTrue("render thread timed out",latch.await(45,TimeUnit.SECONDS));
        if(thrown.get()!=null)throw new AssertionError(thrown.get());
    }
    private static Object value(Object target,String name)throws Exception {
        for(Class<?> c=target.getClass();c!=null;c=c.getSuperclass()){
            try {Field field=c.getDeclaredField(name);field.setAccessible(true);return field.get(target);}
            catch(NoSuchFieldException ignored){}
        }
        throw new NoSuchFieldException(name);
    }
    @SuppressWarnings("unchecked")
    private static <T> T find(Group group,Class<T> type)throws Exception {
        for(Gizmo g:(List<Gizmo>)value(group,"members"))
            if(type.isInstance(g)&&g.alive)return type.cast(g);
        return null;
    }
    @Test public void searchDialogOpensNativePickerAndRowTouchOpensOnlyTheChosenTable()throws Exception{
        try(ActivityScenario<AndroidLauncher> app=ActivityScenario.launch(AndroidLauncher.class)){
            onRender(()->ShatteredPixelDungeon.switchNoFade(TitleScene.class));
            Thread.sleep(500);
            AtomicReference<String> selected=new AtomicReference<>();
            AtomicReference<Point> touch=new AtomicReference<>();
            onRender(()->{try {
                Catalog c=new Catalog();
                DatasetDef deaths=new DatasetDef("deaths","Deaths","How characters died");
                deaths.add(ColumnDef.num("death.floor","Floor").shown());
                c.add(new Dataset(deaths,new ArrayList<>(),new ArrayList<>()));
                c.add(new ViewDef("deaths","Deaths","All recorded deaths",Query.of("deaths")));
                WndUimsdSearch.open(c,selected::set);
                WndTextInput entry=find(Game.scene(),WndTextInput.class);
                assertNotNull("native search text input must open",entry);
                entry.onSelect(true,"muerte");
                entry.hide();
                WndUimsdList results=find(Game.scene(),WndUimsdList.class);
                assertNotNull("localized query must have a result",results);
                List<WndUimsdList.Entry> rows=(List<WndUimsdList.Entry>)value(results,"entries");
                assertEquals(1,rows.size());
                assertEquals("Deaths",rows.get(0).label);
                ScrollPane pane=(ScrollPane)value(results,"pane");
                List<float[]> spans=(List<float[]>)value(results,"spans");
                float[] span=spans.get(0);
                float yy=span[0]+span[1]/2;
                pane.scrollTo(0,0);
                touch.set(pane.content().camera().cameraToScreen(pane.width()/2,yy));
            }catch(Exception e){throw new RuntimeException(e);}});
            assertNotNull(touch.get());
            Point p=touch.get();
            PointerEvent.addPointerEvent(new PointerEvent(p.x,p.y,95,PointerEvent.Type.DOWN,PointerEvent.LEFT));
            Thread.sleep(150);
            PointerEvent.addPointerEvent(new PointerEvent(p.x,p.y,95,PointerEvent.Type.UP,PointerEvent.LEFT));
            for(int i=0;i<35 && selected.get()==null;i++)Thread.sleep(100);
            assertEquals("Native result click must use original UIMSD view", "deaths",selected.get());
        }
    }
}
