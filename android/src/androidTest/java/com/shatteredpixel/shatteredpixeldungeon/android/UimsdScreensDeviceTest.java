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
