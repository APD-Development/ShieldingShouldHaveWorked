package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ColumnDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Op;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Question;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Questions;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.SavedViews;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Query;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdAsk;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.TextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdChoices;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdColumns;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdFilter;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class UimsdRestoreDeviceTest {
    private static void onRender(Runnable task) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Game.runOnRenderThread(() -> {
            try { task.run(); } catch (Throwable t) { error.set(t); }
            finally { done.countDown(); }
        });
        assertTrue("render thread timed out", done.await(60, TimeUnit.SECONDS));
        if (error.get() != null) throw new AssertionError(error.get());
    }

    @SuppressWarnings("unchecked")
    private static List<Gizmo> members(Group group) throws Exception {
        Field f = Group.class.getDeclaredField("members"); f.setAccessible(true);
        return new ArrayList<>((List<Gizmo>) f.get(group));
    }

    private static List<Window> windows() {
        try {
            List<Window> result = new ArrayList<>();
            for (Gizmo member : members(Game.scene())) if (member instanceof Window) result.add((Window) member);
            return result;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static void clearWindows() {
        for (Window window : windows()) window.hide();
    }

    private static UimsdModel model() {
        try {
            Field f = UimsdScene.class.getDeclaredField("model"); f.setAccessible(true);
            return (UimsdModel) f.get(null);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static TextInput input(Window window){
        try {
            Field f = WndTextInput.class.getDeclaredField("textBox"); f.setAccessible(true);
            return (TextInput) f.get(window);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static void pickFirst(WndUimsdList list){ pickEntry(list, 0); }

    private static void pickEntry(WndUimsdList list, int entry){
        try {
            java.lang.reflect.Method method = WndUimsdList.class.getDeclaredMethod("picked", int.class);
            method.setAccessible(true); method.invoke(list, entry);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @SuppressWarnings("unchecked")
    private static List<WndUimsdList.Entry> listEntries(WndUimsdList list){
        try {
            Field f = WndUimsdList.class.getDeclaredField("entries"); f.setAccessible(true);
            return (List<WndUimsdList.Entry>) f.get(list);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static void pickItem(WndUimsdList list, int item){
        List<WndUimsdList.Entry> entries = listEntries(list);
        for (int i = 0, n = 0; i < entries.size(); i++) {
            if (entries.get(i).isItem() && n++ == item) { pickEntry(list, i); return; }
        }
        throw new AssertionError("Missing picker item " + item);
    }

    private static void assertSplitChecks(WndUimsdList list){
        try {
            Field f = WndUimsdList.class.getDeclaredField("labelTexts"); f.setAccessible(true);
            List<?> labels = (List<?>) f.get(list);
            List<WndUimsdList.Entry> entries = listEntries(list);
            int checked = 0, item = 0;
            for (int i = 0; i < entries.size(); i++) {
                WndUimsdList.Entry entry = entries.get(i);
                if (!entry.isItem()) continue;
                boolean expected = item == 1 || item == 2;
                assertEquals("only latest two split fields selected, item " + item, expected, Boolean.TRUE.equals(entry.checked));
                assertTrue("rendered check matches selected field, item " + item,
                        ((RenderedTextBlock) labels.get(i)).text().startsWith(expected ? "[x] " : "[ ] "));
                if (Boolean.TRUE.equals(entry.checked)) checked++;
                item++;
            }
            assertEquals("exactly two split fields selected", 2, checked);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static void options(String method, Class<?>[] types, Object... args){
        try {
            java.lang.reflect.Method m = WndUimsdOptions.class.getDeclaredMethod(method, types);
            m.setAccessible(true); m.invoke(null, args);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static void awaitScene(Object previous) throws Exception {
        long deadline = System.currentTimeMillis() + 30000;
        while ((Game.scene() == previous || !(Game.scene() instanceof UimsdScene))
                && System.currentTimeMillis() < deadline) Thread.sleep(100);
        assertTrue("UIMSD scene did not rebuild", Game.scene() instanceof UimsdScene && Game.scene() != previous);
        //Restoration follows scene creation in the same render-thread call.
        onRender(() -> {});
    }

    private static void rebuildScene() throws Exception {
        Object previous = Game.scene();
        onRender(ShatteredPixelDungeon::seamlessResetScene);
        awaitScene(previous);
    }

    @Test public void menusSurviveSceneRecreationAndNarrowActionsStayInsideTheirWindow() throws Exception {
        try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
            long deadline = System.currentTimeMillis() + 120000;
            while (Game.scene() == null && System.currentTimeMillis() < deadline) Thread.sleep(200);
            assertNotNull("game did not start", Game.scene());
            Object previous = Game.scene();
            //The test enters directly; it does not change production unlock flags.
            onRender(() -> UimsdScene.openNow(TitleScene.class));
            awaitScene(previous);
            onRender(() -> {
                clearWindows();
                Game.scene().addToFront(new WndSettings());
            });
            rebuildScene();
            Object settingsScene = Game.scene();
            onRender(() -> {
                assertEquals("existing zero-argument fallback survived", 1, windows().size());
                assertTrue(windows().get(0) instanceof WndSettings);
                clearWindows();
            });
            //Closing Settings schedules its own font-reset scene rebuild.
            //Wait for that real lifecycle event before opening the next menu.
            awaitScene(settingsScene);
            onRender(() -> {
                UimsdModel model = model(); assertNotNull(model);
                model.selectView("runs");
                WndUimsdOptions.show(model);
                assertEquals(1, windows().size());
                assertNotNull(windows().get(0).restoreOnSceneReset());
            });
            rebuildScene();
            onRender(() -> {
                assertEquals("Options survived exactly once", 1, windows().size());
                assertTrue(windows().get(0) instanceof WndUimsdList);
                clearWindows();
                WndUimsdColumns.show(model());
            });
            rebuildScene();
            onRender(() -> {
                assertEquals("Columns survived exactly once", 1, windows().size());
                clearWindows();
                options("pickGroup", new Class<?>[]{UimsdModel.class}, model());
            });
            rebuildScene();
            onRender(() -> {
                assertEquals("grouping menu survived", 1, windows().size());
                pickFirst((WndUimsdList) windows().get(0));
                assertNull("restored grouping callback works", model().query().group);
                clearWindows();
                SavedViews views = new SavedViews();
                views.save("Restoration fixture", model().query(), 1);
                options("viewActions", new Class<?>[]{UimsdModel.class, SavedViews.View.class},
                        model(), views.get("Restoration fixture"));
            });
            Query savedQuery = model().query();
            rebuildScene();
            onRender(() -> {
                assertEquals("saved-view actions survived", 1, windows().size());
                assertSame("restoring does not apply a saved view", savedQuery, model().query());
                pickFirst((WndUimsdList) windows().get(0));
                assertEquals("restored Open callback works", savedQuery.dataset, model().query().dataset);
                clearWindows();
            });
            //A separate marked demo fixture: no player history or real demo records.
            java.io.File cache = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getCacheDir();
            java.io.File container = java.nio.file.Files.createTempDirectory(cache.toPath(), "dialog-check-").toFile();
            java.io.File fixture = new java.io.File(container, "history");
            assertTrue(fixture.mkdir());
            java.io.File demo = DemoData.demoRootFor(fixture);
            assertTrue(demo.mkdirs());
            java.io.File marker = new java.io.File(demo, DemoData.MARKER_FILE);
            assertTrue(marker.createNewFile());
            try {
                onRender(() -> options("deleteDemoData", new Class<?>[]{java.io.File.class}, fixture));
                rebuildScene();
                onRender(() -> {
                    assertEquals("demo deletion confirmation survived", 1, windows().size());
                    assertTrue("restoration never confirms deletion", marker.isFile());
                    windows().get(0).onBackPressed();
                    assertTrue("Back never confirms deletion", marker.isFile());
                });
            } finally {
                assertTrue(marker.delete()); assertTrue(demo.delete());
                assertTrue(fixture.delete()); assertTrue(container.delete());
            }
            onRender(() -> {
                WndUimsdFilter.add(model());
                WndUimsdFilter.pickOp(model(), -1, null, model().offeredColumns().get(0));
                assertEquals(2, windows().size());
            });
            rebuildScene();
            onRender(() -> {
                assertEquals("both filter steps survived", 2, windows().size());
                windows().get(1).onBackPressed();
                assertEquals("Back returns to the preceding filter step", 1, windows().size());
                clearWindows();
                WndUimsdAsk.builder(model().catalog(), model().currentRuleset(), (q, back) -> {}, Question.about("runs"));
            });
            rebuildScene();
            final AtomicReference<String> answer = new AtomicReference<>();
            onRender(() -> {
                assertEquals("Ask builder survived", 1, windows().size());
                clearWindows();
                WndUimsdTextInput prompt = new WndUimsdTextInput("Filter", "A whole number", "123", 40,
                        false, "OK", "Cancel", (positive, value) -> { if (positive) answer.set(value); });
                Game.scene().addToFront(prompt);
                input(prompt).setText("321");
            });
            rebuildScene();
            onRender(() -> {
                assertEquals("typed input survived", 1, windows().size());
                WndUimsdTextInput prompt = (WndUimsdTextInput) windows().get(0);
                assertEquals("321", input(prompt).getText());
                prompt.onSelect(true, input(prompt).getText());
                assertEquals("restored callback works", "321", answer.get());
                clearWindows();
                ColumnDef outcome = null;
                for (ColumnDef column : model().offeredColumns()) if (column.type == ColumnDef.Type.ENUM) { outcome = column; break; }
                assertNotNull("enum fixture", outcome);
                WndUimsdFilter.pickValue(model(), -1, null, outcome, Op.IN);
                pickFirst((WndUimsdList) windows().get(0));
            });
            rebuildScene();
            onRender(() -> {
                assertEquals("pending multi-select survived", 1, windows().size());
                try {
                    Field entries = WndUimsdList.class.getDeclaredField("entries"); entries.setAccessible(true);
                    List<?> saved = (List<?>) entries.get(windows().get(0));
                    assertEquals(Boolean.TRUE, ((WndUimsdList.Entry) saved.get(0)).checked);
                    java.lang.reflect.Method done = windows().get(0).getClass().getDeclaredMethod("onDone");
                    done.setAccessible(true); done.invoke(windows().get(0));
                    assertEquals("pending choice applies after restoration", 1, model().query().filters.size());
                } catch (Exception e) { throw new RuntimeException(e); }
                clearWindows();
                try {
                    java.lang.reflect.Method split = WndUimsdAsk.class.getDeclaredMethod("pickSplit",
                            Catalog.class, String.class, WndUimsdAsk.Host.class, Question.class, DatasetDef.class);
                    split.setAccessible(true);
                    Question question = Question.about("runs");
                    DatasetDef def = model().catalog().dataset("runs").def;
                    assertTrue("three groupable split fields exist", Questions.groupable(def, false).size() >= 3);
                    split.invoke(null, model().catalog(), model().currentRuleset(),
                            (WndUimsdAsk.Host) (q, back) -> {}, question, def);
                    WndUimsdList picker = (WndUimsdList) windows().get(0);
                    pickItem(picker, 0); pickItem(picker, 1); pickItem(picker, 2);
                    assertSplitChecks(picker);
                    assertTrue("pending choices do not modify original question", question.groupBy.isEmpty());
                    picker.restoreOnSceneReset().run();
                    picker.hide();
                    WndUimsdList restored = (WndUimsdList) windows().get(0);
                    assertSplitChecks(restored);
                    java.lang.reflect.Method done = restored.getClass().getDeclaredMethod("onDone");
                    done.setAccessible(true); done.invoke(restored);
                    Field last = WndUimsdAsk.class.getDeclaredField("last"); last.setAccessible(true);
                    List<ColumnDef> cols = Questions.groupable(def, false);
                    assertEquals(java.util.Arrays.asList(cols.get(1).id, cols.get(2).id), ((Question) last.get(null)).groupBy);
                    clearWindows();
                } catch (Exception e) { throw new RuntimeException(e); }
                int originalWidth = PixelScene.uiCamera.width;
                try {
                    PixelScene.uiCamera.width = 100;
                    WndUimsdChoices choices = new WndUimsdChoices("Queue", "Nothing is sent.",
                            "Queue for submission (preview, sends nothing)", "Show what is queued for this run");
                    Game.scene().addToFront(choices);
                    Field width = Window.class.getDeclaredField("width"); width.setAccessible(true);
                    int innerWidth = width.getInt(choices);
                    assertTrue("window leaves side margins", innerWidth <= 72);
                    Field labels = WndUimsdList.class.getDeclaredField("labelTexts"); labels.setAccessible(true);
                    int checkedActions = 0;
                    for (Object item : (List<?>) labels.get(choices)) {
                        //Notes/headings have no action label in this parallel list.
                        if (item == null) continue;
                        RenderedTextBlock label = (RenderedTextBlock) item;
                        assertTrue("label left edge", label.left() >= 0);
                        assertTrue("wrapped label right edge", label.right() <= innerWidth);
                        checkedActions++;
                    }
                    assertEquals("both long actions were checked", 2, checkedActions);
                    clearWindows();
                    WndUimsdTextInput prompt = new WndUimsdTextInput("Filter", "A number", "", 40,
                            false, "OK", "Cancel", (positive, value) -> {});
                    Game.scene().addToFront(prompt);
                    innerWidth = width.getInt(prompt);
                    assertTrue("input leaves side margins", innerWidth <= 72);
                    Field buttonText = StyledButton.class.getDeclaredField("text"); buttonText.setAccessible(true);
                    for (Gizmo child : members(prompt)) if (child instanceof RedButton){
                        RedButton button = (RedButton) child;
                        RenderedTextBlock label = (RenderedTextBlock) buttonText.get(button);
                        if (!label.text().isEmpty()) {
                            assertTrue("input action left edge", label.left() >= button.left());
                            assertTrue("input action right edge", label.right() <= button.right());
                        }
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
                finally { PixelScene.uiCamera.width = originalWidth; clearWindows(); }
            });
        }
    }
}
