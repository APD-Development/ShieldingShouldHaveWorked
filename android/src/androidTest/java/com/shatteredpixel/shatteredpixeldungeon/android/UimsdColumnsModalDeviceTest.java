package com.shatteredpixel.shatteredpixeldungeon.android;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdColumns;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.utils.Point;
import com.watabou.input.PointerEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Regression: selecting Columns repeatedly must not let clicks go through the modal. */
@RunWith(AndroidJUnit4.class)
public class UimsdColumnsModalDeviceTest {
    private static void render(Runnable task)throws Exception {
        CountDownLatch finished=new CountDownLatch(1);
        AtomicReference<Throwable> exception=new AtomicReference<>();
        Game.runOnRenderThread(()->{
            try{task.run();}catch(Throwable t){exception.set(t);}finally{finished.countDown();}
        });
        assertTrue("Render thread",finished.await(45,TimeUnit.SECONDS));
        if(exception.get()!=null)throw new AssertionError(exception.get());
    }

    private static Object field(Object value,String name)throws Exception{
        for(Class<?> c=value instanceof Class ? (Class<?>)value : value.getClass();c!=null;c=c.getSuperclass()) {
            try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(value instanceof Class ? null:value);}
            catch(NoSuchFieldException ignored){}
        }
        throw new NoSuchFieldException(name);
    }

    @SuppressWarnings("unchecked")
    private static WndUimsdList picker()throws Exception{
        for(Gizmo g:(List<Gizmo>)field(Game.scene(),"members"))
            if(g instanceof WndUimsdList && g.alive)return (WndUimsdList)g;
        return null;
    }

    private static void until(Callable<Boolean> check)throws Exception{
        long limit=System.currentTimeMillis()+30000;
        while(System.currentTimeMillis()<limit){
            if(check.call())return;
            Thread.sleep(100);
        }
        fail("Native UIMSD did not reach the expected state");
    }

    @SuppressWarnings("unchecked")
    private static void tapRow(int index)throws Exception {
        AtomicReference<Point> at=new AtomicReference<>();
        render(()->{try{
            WndUimsdList w=picker();
            assertNotNull("Columns picker is still open",w);
            ScrollPane pane=(ScrollPane)field(w,"pane");
            List<float[]> spans=(List<float[]>)field(w,"spans");
            float[] row=spans.get(index);
            assertNotNull(row);
            float y=row[0]+row[1]/2;
            pane.scrollTo(0,Math.max(0,y-pane.height()/2));
            at.set(pane.content().camera().cameraToScreen(pane.width()/2,y));
        }catch(Exception e){throw new RuntimeException(e);}});
        Point p=at.get();
        PointerEvent.addPointerEvent(new PointerEvent(p.x,p.y,82,PointerEvent.Type.DOWN,PointerEvent.LEFT));
        Thread.sleep(120);
        PointerEvent.addPointerEvent(new PointerEvent(p.x,p.y,82,PointerEvent.Type.UP,PointerEvent.LEFT));
        Thread.sleep(450);
    }

    @Test public void multipleColumnTapsRemainAboveTableUntilDone()throws Exception {
        try(ActivityScenario<AndroidLauncher> scenario=ActivityScenario.launch(AndroidLauncher.class)){
            until(()->Game.scene()!=null);
            render(()->UimsdScene.openNow(TitleScene.class));
            until(()->Game.scene() instanceof UimsdScene);
            until(()->{try{return field(UimsdScene.class,"model")!=null;}catch(Exception e){return false;}});
            final AtomicReference<UimsdModel> model=new AtomicReference<>();
            final AtomicReference<Group> background=new AtomicReference<>();
            render(()->{try{
                UimsdModel m=(UimsdModel)field(UimsdScene.class,"model");
                model.set(m);
                DatasetDef d=new DatasetDef("modalfixture","Modal fixture","Column toggle regression");
                d.add(ColumnDef.text("m.first","First").shown());
                d.add(ColumnDef.text("m.second","Second").shown());
                d.add(ColumnDef.text("m.third","Third"));
                Catalog cat=new Catalog();
                cat.add(new Dataset(d,new ArrayList<>(),new ArrayList<>()));
                cat.add(new ViewDef("modalfixture","Modal fixture","Three independent columns",Query.of("modalfixture")));
                m.setCatalog(cat);
                m.showRoot("modalfixture");
                Field f=UimsdScene.class.getDeclaredField("atHome");f.setAccessible(true);f.set(null,false);
            }catch(Exception e){throw new RuntimeException(e);}});
            Thread.sleep(350);
            render(()->{try{
                background.set((Group)field(Game.scene(),"ui"));
                WndUimsdColumns.show(model.get());
                assertNotNull(picker());
            }catch(Exception e){throw new RuntimeException(e);}});
            // First column off; changing query sets dirty=true but must not
            // rebuild UIMSD's underlying pointer controls until the modal closes.
            tapRow(1);
            render(()->{try{
                assertSame("No UI replacement while popup is open",background.get(),field(Game.scene(),"ui"));
                assertTrue((Boolean)field(Game.scene(),"dirty"));
                assertNotNull("First selection did not dismiss popup",picker());
            }catch(Exception e){throw new RuntimeException(e);}});
            tapRow(3); // third column on
            render(()->{try {
                List<String> active=model.get().query().columns;
                assertFalse("first column toggled off",active.contains("m.first"));
                assertTrue("second column stays",active.contains("m.second"));
                assertTrue("third column toggled on by second tap",active.contains("m.third"));
                assertSame("Popup still has input priority",background.get(),field(Game.scene(),"ui"));
            }catch(Exception e){throw new RuntimeException(e);}});
            tapRow(4); // Done
            until(()->{try{return picker()==null;}catch(Exception e){return false;}});
            until(()->{try{return field(Game.scene(),"ui")!=background.get();}catch(Exception e){return false;}});
        }
    }
}
