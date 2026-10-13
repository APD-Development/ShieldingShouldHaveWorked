package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Op;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ResultTable;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdGuide;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdTutorial;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdColumns;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdFilter;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdGuide;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdRow;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdSort;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdStatus;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdTopic;
import com.watabou.noosa.Game;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

//The UIMSD screens and dialogs, built for real on the device's game engine. The layout code is never run by the JVM tests (it needs the engine's fonts and
//cameras), so this is what catches a screen that cannot be built: the first-run Home, the tour's sample Home and tables at several steps, and every
//dialog (filter steps for every column, sort, columns, options, notes, guide topics, row sheets). A failure here is a crash on a phone.
@RunWith(AndroidJUnit4.class)
public class UimsdScreensDeviceTest {

	private static final String TAG = "APDUI";

	private static void onRender( final Runnable r ) throws Exception {
		final CountDownLatch done = new CountDownLatch(1);
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		Game.runOnRenderThread(new com.watabou.utils.Callback(){
			@Override
			public void call(){
				try {
					r.run();
				} catch (Throwable t){
					failure.set(t);
				} finally {
					done.countDown();
				}
			}
		});
		assertTrue("the render thread did not answer", done.await(60, TimeUnit.SECONDS));
		if (failure.get() != null) throw new AssertionError("failed on the render thread: " + failure.get(), failure.get());
	}

	private static Object field( String name ) throws Exception {
		Field f = UimsdScene.class.getDeclaredField(name);
		f.setAccessible(true);
		return f.get(null);
	}

	private static void waitUntil( String what, java.util.concurrent.Callable<Boolean> c, long millis ) throws Exception {
		long end = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis() < end){
			if (c.call()) return;
			Thread.sleep(100);
		}
		throw new AssertionError("timed out waiting for " + what);
	}


    private static com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList comparisonWindow() throws Exception {
        Field children=com.watabou.noosa.Group.class.getDeclaredField("members");children.setAccessible(true);
        com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList result=null;
        for(Object child:(java.util.List<?>)children.get(Game.scene()))
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList && ((com.watabou.noosa.Gizmo)child).alive)
                result=(com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList)child;
        assertNotNull("comparison picker is visible",result);return result;
    }

    private static void tapComparisonEntry(int index) throws Exception {
        AtomicReference<com.watabou.utils.Point> point=new AtomicReference<>();
        onRender(()->{try{
            com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList list=comparisonWindow();
            Field spansField=list.getClass().getDeclaredField("spans");spansField.setAccessible(true);
            java.util.List<float[]> spans=(java.util.List<float[]>)spansField.get(list);
            Field paneField=list.getClass().getDeclaredField("pane");paneField.setAccessible(true);
            com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)paneField.get(list);
            float[] span=spans.get(index);float y=span[0]+span[1]/2;
            pane.scrollTo(0,Math.max(0,y-pane.height()/2));
            point.set(pane.content().camera().cameraToScreen(pane.width()/2,y));
        }catch(Exception e){throw new RuntimeException(e);}});
        com.watabou.utils.Point at=point.get();
        com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(at.x,at.y,14,com.watabou.input.PointerEvent.Type.DOWN,com.watabou.input.PointerEvent.LEFT));
        Thread.sleep(150);
        com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(at.x,at.y,14,com.watabou.input.PointerEvent.Type.UP,com.watabou.input.PointerEvent.LEFT));
        Thread.sleep(300);
    }

    private static void verifyComparisonPages() throws Exception {
        java.util.List<String> choices=new java.util.ArrayList<>();for(int i=0;i<81;i++)choices.add("Comparison "+i);
        AtomicReference<String> selected=new AtomicReference<>();
        onRender(()->{try{
            Method open=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdReport.class.getDeclaredMethod("chooseComparison",java.util.List.class,java.util.function.Function.class,java.util.function.Consumer.class,int.class);
            open.setAccessible(true);open.invoke(null,choices,(java.util.function.Function<String,String>)(v->v),(java.util.function.Consumer<String>)(selected::set),0);
        }catch(Exception e){throw new RuntimeException(e);}});
        Thread.sleep(300);
        tapComparisonEntry(40); // Next: choices 40 through 79, plus previous/next controls.
        onRender(()->{try{
            Field entries=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class.getDeclaredField("entries");entries.setAccessible(true);
            java.util.List<?> list=(java.util.List<?>)entries.get(comparisonWindow());
            assertEquals("only one bounded page is rendered",42,list.size());
            assertEquals("Comparison 40",((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Entry)list.get(0)).label);
        }catch(Exception e){throw new RuntimeException(e);}});
        tapComparisonEntry(40); // Previous returns to page one.
        tapComparisonEntry(40); // Next returns to page two.
        tapComparisonEntry(41); // Next reaches the final partial page.
        onRender(()->{try{
            Field entries=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class.getDeclaredField("entries");entries.setAccessible(true);
            java.util.List<?> list=(java.util.List<?>)entries.get(comparisonWindow());
            assertEquals(2,list.size());assertEquals("Comparison 80",((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Entry)list.get(0)).label);
        }catch(Exception e){throw new RuntimeException(e);}});
        tapComparisonEntry(0);assertEquals("the last comparison remains selectable", "Comparison 80",selected.get());
    }


    private static void verifyQuestionRateRendering() throws Exception {
        onRender(()->{try{
            com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetDef heals=com.shatteredpixel.shatteredpixeldungeon.apd.query.EventDatasets.def("heals");
            com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetDef floors=com.shatteredpixel.shatteredpixeldungeon.apd.query.EventDatasets.def("floors");
            for(boolean incomplete:new boolean[]{true,false}){
                com.shatteredpixel.shatteredpixeldungeon.apd.query.Row value=new com.shatteredpixel.shatteredpixeldungeon.apd.query.Row("heal",null,"fixture",heals.size());
                value.finished=true;value.ruleset="rate-fixture";
                value.setNum(heals.indexOf("heal.amount"),incomplete?12:Long.MAX_VALUE,incomplete?com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact.PROV_INCOMPLETE:com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact.PROV_RECORDED);
                com.shatteredpixel.shatteredpixeldungeon.apd.query.Row floor=new com.shatteredpixel.shatteredpixeldungeon.apd.query.Row("floor",null,"fixture",floors.size());
                floor.finished=true;floor.ruleset="rate-fixture";
                floor.setText(floors.indexOf("floor.run"),"fixture",com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact.PROV_RECORDED);
                floor.setNum(floors.indexOf("floor.turns"),incomplete?3:1,incomplete?com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact.PROV_INCOMPLETE:com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact.PROV_RECORDED);
                com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog catalog=new com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog()
                    .add(new com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset(heals,java.util.Arrays.asList(value),null))
                    .add(new com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset(floors,java.util.Arrays.asList(floor),null));
                com.shatteredpixel.shatteredpixeldungeon.apd.query.Question question=com.shatteredpixel.shatteredpixeldungeon.apd.query.Question.about("heals")
                    .withScope(com.shatteredpixel.shatteredpixeldungeon.apd.query.Scope.DEFAULT.withRules(com.shatteredpixel.shatteredpixeldungeon.apd.query.Scope.Rules.ALL,""))
                    .withMeasure(com.shatteredpixel.shatteredpixeldungeon.apd.query.Question.Measure.TOTAL,"heal.amount")
                    .withPer(com.shatteredpixel.shatteredpixeldungeon.apd.query.PerUnit.Unit.TURN);
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk.answer(catalog,"rate-fixture",null,question);
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList window=comparisonWindow();
                Field entries=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class.getDeclaredField("entries");entries.setAccessible(true);
                java.util.List<?> shown=(java.util.List<?>)entries.get(window);
                assertEquals(incomplete?"?":"9223372036854776000.00",((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Entry)shown.get(0)).label);
                Field texts=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class.getDeclaredField("labelTexts");texts.setAccessible(true);
                for(Object item:(java.util.List<?>)texts.get(window))if(item instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock){
                    com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock text=(com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)item;
                    assertTrue("question text fits its native window",text.width()<=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.windowWidth());
                }
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.closeAll();
            }
        }catch(Exception e){throw new RuntimeException(e);}});
    }

	@Test
	public void everyUimsdScreenAndDialogCanBeBuilt() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());

			//1. Exercise the screens directly; the ten-run access gate is covered by separate unlock tests. UIMSD opened for real, on whatever history this device has (none, on a fresh emulator): the first-run Home
			onRender(new Runnable(){ public void run(){ UimsdScene.openNow(TitleScene.class); } });
			waitUntil("the UIMSD scene", new java.util.concurrent.Callable<Boolean>(){ public Boolean call(){ return Game.scene() instanceof UimsdScene; } }, 30000);
			Thread.sleep(2500);       //a few frames: the screen builds itself on the render thread
			Log.w(TAG, "UIMSD scene is up (first-run Home or history Home)");

			//2. the tour on its sample history: Home with cards, then the tables at the steps that need them
			final Method begin = UimsdScene.class.getDeclaredMethod("beginTour", int.class, boolean.class);
			begin.setAccessible(true);
			int steps = UimsdTutorial.steps().size();
			int[] at = { 0, UimsdTutorial.indexOf("table"), UimsdTutorial.indexOf("deeper"), UimsdTutorial.indexOf("filter"), UimsdTutorial.indexOf("marks"),
					UimsdTutorial.indexOf("coverage"), steps - 1 };
			for (final int step : at){
				onRender(new Runnable(){
					public void run(){
						try {
							begin.invoke(Game.scene(), step, false);
						} catch (Exception e){
							throw new RuntimeException(e);
						}
					}
				});
				waitUntil("the tour at step " + step, new java.util.concurrent.Callable<Boolean>(){
					public Boolean call() throws Exception { return field("tutorial") != null && !((Boolean) field("tutorialPreparing")); } }, 60000);
				Thread.sleep(1500);
				Log.w(TAG, "tour step " + step + " built");
			}

			//3. the dialogs, on the tour's model (the sample history), one level in a table with filters, sorting and columns
			final UimsdModel m = (UimsdModel) field("model");
			assertNotNull("the tour has a model", m);
            verifyComparisonPages();
            verifyQuestionRateRendering();
            onRender(() -> {
                com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdHome home =
                    com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdHome.build(m.catalog(), m.currentRuleset(), true);
                assertFalse("sample Home contains real navigation fixtures", home.isEmpty());
                for (int width : new int[]{120,160,240}) {
                    com.shatteredpixel.shatteredpixeldungeon.ui.UimsdHomePane pane = new com.shatteredpixel.shatteredpixeldungeon.ui.UimsdHomePane();
                    Game.scene().add(pane); // ScrollPane layout requires the real scene camera.
                    final String[] selected = {null};
                    pane.setListener(id -> selected[0]=id);
                    pane.setRect(0,0,width,100);pane.setHome(home);
                    assertTrue("Home scrolls rather than dropping rows",pane.contentHeight()>100);
                    for (com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdHome.Card card : home.cards)
                        for(com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdHome.Link link : card.links){
                            float[] box=pane.linkBox(link.viewId);assertNotNull(link.viewId,box);
                            assertTrue(box[0]>=0&&box[0]+box[2]<=width+0.1f);
                            selected[0]=null;pane.onClick(box[0]+box[2]/2,box[1]+box[3]/2);
                            assertEquals("all existing Home actions remain reachable",link.viewId,selected[0]);
                        }
                    for(String target:new String[]{"!outcome:0","!outcome:1","!outcome:2"}){
                        float[] box=pane.linkBox(target);
                        if(box!=null){selected[0]=null;pane.onClick(box[0]+box[2]/2,box[1]+box[3]/2);assertEquals(target,selected[0]);}
                    }
                    Game.scene().remove(pane);pane.destroy();
                }
            });

            // Capture the real sample Home without the optional tour panel covering its statistics.
            final Object savedTutorial=field("tutorial");
            final Object savedAtHome=field("atHome"),savedAllRules=field("homeAllRules");
            onRender(()->{try{
                for(String key:new String[]{"tutorial","atHome","homeAllRules","cachedHome"}){
                    Field value=UimsdScene.class.getDeclaredField(key);value.setAccessible(true);
                    value.set(null,key.equals("atHome")||key.equals("homeAllRules")?Boolean.TRUE:null);
                }
                Field dirty=UimsdScene.class.getDeclaredField("dirty");dirty.setAccessible(true);dirty.set(Game.scene(),true);
            }catch(Exception e){throw new RuntimeException(e);}});
            Thread.sleep(500);
            PlayRunDeviceTest.visibilityScreenshot("uimsd-home.png");
            final AtomicReference<com.watabou.utils.Point> demoPoint=new AtomicReference<>();
            onRender(()->{try{
                Field uiField=UimsdScene.class.getDeclaredField("ui");uiField.setAccessible(true);
                com.watabou.noosa.Group controls=(com.watabou.noosa.Group)uiField.get(Game.scene());
                Field children=com.watabou.noosa.Group.class.getDeclaredField("members");children.setAccessible(true);
                com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton demo=null;
                for(Object child:(java.util.List<?>)children.get(controls)){
                    if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton){
                        com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton b=(com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)child;
                        assertFalse("UIMSD navigation reuses native grey chrome",b instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RedButton);
                        if(b.text().equals("DEMO DATA"))demo=b;
                    }
                }
                assertNotNull("fictional-data indicator cannot disappear",demo);assertTrue(demo.width()<=88 && demo.height()<=16);
                demoPoint.set(demo.camera().cameraToScreen(demo.left()+demo.width()/2f,demo.top()+demo.height()/2f));
            }catch(Exception e){throw new RuntimeException(e);}});
            com.watabou.utils.Point point=demoPoint.get();
            com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(point.x,point.y,12,com.watabou.input.PointerEvent.Type.DOWN,com.watabou.input.PointerEvent.LEFT));
            Thread.sleep(150);
            com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(point.x,point.y,12,com.watabou.input.PointerEvent.Type.UP,com.watabou.input.PointerEvent.LEFT));
            final AtomicReference<com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdText> demoInfo=new AtomicReference<>();
            waitUntil("demo indicator opens its information popup",()->{
                synchronized(Game.scene()){
                    Field children=com.watabou.noosa.Group.class.getDeclaredField("members");children.setAccessible(true);
                    for(Object child:(java.util.List<?>)children.get(Game.scene()))
                        if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdText)demoInfo.set((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdText)child);
                }
                return demoInfo.get()!=null;
            },10000);
            Thread.sleep(300);
            PlayRunDeviceTest.visibilityScreenshot("uimsd-demo-info.png");
            onRender(()->demoInfo.get().hide());

            onRender(()->{try{
                String[] keys={"tutorial","atHome","homeAllRules"};
                Object[] values={savedTutorial,savedAtHome,savedAllRules};
                for(int i=0;i<keys.length;i++){Field value=UimsdScene.class.getDeclaredField(keys[i]);value.setAccessible(true);value.set(null,values[i]);}
                Field dirty=UimsdScene.class.getDeclaredField("dirty");dirty.setAccessible(true);dirty.set(Game.scene(),true);
            }catch(Exception e){throw new RuntimeException(e);}});

            // Text-IN must reach values outside the compact sixty-value suggestion list.
            final AtomicReference<com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdTextInput> fullListInput=new AtomicReference<>();
            final UimsdModel uncommon=new UimsdModel("filter-fixture","fixture");
            onRender(()->{try{
                com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetDef def=m.catalog().dataset("runs").def;
                java.util.List<com.shatteredpixel.shatteredpixeldungeon.apd.query.Row> rows=new java.util.ArrayList<>();
                for(int i=0;i<70;i++){
                    com.shatteredpixel.shatteredpixeldungeon.apd.query.Row row=new com.shatteredpixel.shatteredpixeldungeon.apd.query.Row("row"+i,"row"+i,"row"+i,def.size());
                    row.finished=true;row.ruleset="filter-fixture";
                    row.setText(def.indexOf(com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys.HERO_CLASS),String.format(java.util.Locale.ROOT,"TYPE%03d",i),com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact.PROV_RECORDED);
                    rows.add(row);
                }
                uncommon.setCatalog(new com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog()
                    .add(new com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset(def,rows,null))
                    .add(new com.shatteredpixel.shatteredpixeldungeon.apd.query.ViewDef("runs","Runs","",com.shatteredpixel.shatteredpixeldungeon.apd.query.Query.of("runs"))));
                ColumnDef column=def.column(com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys.HERO_CLASS);
                assertEquals(60,uncommon.choicesFor(column,60).size());
                for(UimsdModel.Choice choice:uncommon.choicesFor(column,60))assertFalse("rare value is outside suggestions",choice.value.equals("TYPE069"));
                WndUimsdFilter.pickValue(uncommon,-1,null,column,Op.IN);
                Field children=com.watabou.noosa.Group.class.getDeclaredField("members");children.setAccessible(true);
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList list=null;
                for(Object child:(java.util.List<?>)children.get(Game.scene()))
                    if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList)list=(com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList)child;
                assertNotNull("multi-value chooser opened",list);
                Field entries=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class.getDeclaredField("entries");entries.setAccessible(true);
                java.util.List<?> values=(java.util.List<?>)entries.get(list);int typed=-1;
                for(int i=0;i<values.size();i++)if(((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Entry)values.get(i)).label.equals("Enter a full text list..."))typed=i;
                assertTrue("text-IN must expose a full-list typing action even with suggestions",typed>=0);
                Method pick=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class.getDeclaredMethod("picked",int.class);pick.setAccessible(true);pick.invoke(list,typed);
                for(Object child:(java.util.List<?>)children.get(Game.scene()))
                    if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdTextInput)fullListInput.set((com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdTextInput)child);
                assertNotNull("typing fallback opens the native input",fullListInput.get());
                Field limit=com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdTextInput.class.getDeclaredField("limit");limit.setAccessible(true);
                assertTrue("full lists are not restricted to forty characters",((Integer)limit.get(fullListInput.get()))>40);
            }catch(Exception e){throw new RuntimeException(e);}});
            Thread.sleep(300);
            PlayRunDeviceTest.visibilityScreenshot("uimsd-text-values.png");
            onRender(()->{
                fullListInput.get().onSelect(true,"TYPE069,TYPE001,TYPE002,TYPE003,TYPE004,TYPE005");
                fullListInput.get().hide();
                assertEquals(6,uncommon.result().rows.size());
                assertTrue("an uncommon value remains queryable",uncommon.result().rows.stream().anyMatch(row->row.id.equals("row69")));
                assertEquals(6,uncommon.query().filters.get(0).texts.length);
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.closeAll();
                java.util.List<com.shatteredpixel.shatteredpixeldungeon.apd.query.Sort> manySorts=new java.util.ArrayList<>();
                for(ColumnDef key:uncommon.dataset().def.columns()){
                    if(key.isInternal())continue;
                    manySorts.add(new com.shatteredpixel.shatteredpixeldungeon.apd.query.Sort(key.id,false));
                    if(manySorts.size()==9)break;
                }
                assertEquals("fixture has nine distinct public keys",9,manySorts.size());
                uncommon.setSorts(manySorts);
                WndUimsdSort.show(uncommon);
                assertEquals("opening Sort must not truncate imported sort keys",9,uncommon.query().sorts.size());
            });
            Thread.sleep(300);
            PlayRunDeviceTest.visibilityScreenshot("uimsd-sort-keys.png");
            onRender(()->com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.closeAll());

			onRender(new Runnable(){
				public void run(){
					m.showRoot("weapons");
				}
			});
			Thread.sleep(500);
			onRender(new Runnable(){
				public void run(){
					WndUimsdFilter.add(m);
					WndUimsdSort.show(m);
					WndUimsdColumns.show(m);
					WndUimsdOptions.show(m);
					WndUimsdStatus.show(m);
					WndUimsdGuide.show(new WndUimsdGuide.Starter(){ public void startTour( int stepIndex ){} });
					for (UimsdGuide.Topic t : UimsdGuide.topics()) ShatteredPixelDungeon.scene().addToFront(new WndUimsdTopic(t, true, new Runnable(){ public void run(){} }));
					ResultTable table = m.table();
					assertNotNull(table);
					for (ResultTable.Line line : table.lines) {
						WndUimsdRow.show(m, line);
						break;
					}
				}
			});
			Thread.sleep(500);

			//3b. the question builder: the sentence and its answer for every dataset that has rows, with a total, a split and a "per"
			final int[] asked = new int[1];
			onRender(new Runnable(){
				public void run(){
					final com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog cat = m.catalog();
					com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk.Host host = new com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk.Host(){
						public void openAsTable( com.shatteredpixel.shatteredpixeldungeon.apd.query.Query q, Runnable back ){}
					};
					for (com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset d : com.shatteredpixel.shatteredpixeldungeon.apd.query.Questions.askable(cat)){
						com.shatteredpixel.shatteredpixeldungeon.apd.query.Question q = com.shatteredpixel.shatteredpixeldungeon.apd.query.Question.about(d.def.id);
						com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk.builder(cat, m.currentRuleset(), host, q);
						com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk.answer(cat, m.currentRuleset(), host, q);
						java.util.List<ColumnDef> numbers = com.shatteredpixel.shatteredpixeldungeon.apd.query.Questions.measurable(d.def, false);
						java.util.List<ColumnDef> splits = com.shatteredpixel.shatteredpixeldungeon.apd.query.Questions.groupable(d.def, false);
						if (!numbers.isEmpty() && !splits.isEmpty()){
							q = q.withMeasure(com.shatteredpixel.shatteredpixeldungeon.apd.query.Question.Measure.AVERAGE, numbers.get(0).id).withGroupBy(java.util.Arrays.asList(splits.get(0).id));
							com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk.answer(cat, m.currentRuleset(), host, q);
						}
						for (com.shatteredpixel.shatteredpixeldungeon.apd.query.PerUnit.Unit u : com.shatteredpixel.shatteredpixeldungeon.apd.query.Questions.unitsFor(
								com.shatteredpixel.shatteredpixeldungeon.apd.query.Question.about(d.def.id))){
							com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk.answer(cat, m.currentRuleset(), host,
									com.shatteredpixel.shatteredpixeldungeon.apd.query.Question.about(d.def.id).withPer(u));
							asked[0]++;
						}
						asked[0]++;
					}
				}
			});
			Log.w(TAG, asked[0] + " questions asked");
			assertTrue(asked[0] >= 1);
			Thread.sleep(500);

			//4. every step of the filter workflow for every column (hidden information on, so the hidden columns too), on the runs table
			onRender(new Runnable(){
				public void run(){
					m.showRoot("runs");
					m.setShowInternal(true);
				}
			});
			Thread.sleep(300);
			final int[] built = new int[1];
			onRender(new Runnable(){
				public void run(){
					for (ColumnDef c : m.offeredColumns()){
						WndUimsdFilter.pickOp(m, -1, null, c);
						built[0]++;
						for (Op op : Op.ops(c.type)){
							//typed values open a text box; the choosing steps are the ones with layout of their own
							if (!op.takesValue() || c.type == ColumnDef.Type.NUM || op == Op.CONTAINS) continue;
							if (c.type == ColumnDef.Type.TEXT && m.choicesFor(c, 1).isEmpty()) continue;
							WndUimsdFilter.pickValue(m, -1, null, c, op);
							built[0]++;
						}
					}
					ResultTable table = m.table();
					for (ResultTable.Line line : table.lines){
						WndUimsdRow.show(m, line);
						built[0]++;
						break;
					}
				}
			});
			Log.w(TAG, built[0] + " dialogs built");
			assertTrue(built[0] > 30);
			Thread.sleep(500);

			//5. every existing catalog destination and its first recorded row/group sheet
			for (final com.shatteredpixel.shatteredpixeldungeon.apd.query.ViewDef view : m.catalog().views()){
				onRender(new Runnable(){
					public void run(){
						m.showRoot(view.id);
						ResultTable table = m.table();
						assertNotNull(view.id, table);
						if (!table.lines.isEmpty()) WndUimsdRow.show(m, table.lines.get(0));
					}
				});
				Thread.sleep(800);
			}
			assertEquals("the scene is still UIMSD", true, Game.scene() instanceof UimsdScene);
		}
	}
}
