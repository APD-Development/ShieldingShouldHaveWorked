package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.archive.ApdFiles;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.APDSettingsPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.utils.Point;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

//Touching the controls of APD Settings, for real. Building the screen (UimsdScreensDeviceTest) cannot tell whether a button answers a touch: a button inside a
//scrolling pane only gets its touches if it was created AFTER the pane's own touch area (the newest touch area is asked first, and the pane's covers all of it).
//This opens the real window, scrolls each control into view, sends real pointer events (the same ones the touch screen produces) and checks the response.
@RunWith(AndroidJUnit4.class)
public class ApdSettingsClickDeviceTest {

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
		if (!done.await(60, TimeUnit.SECONDS)){
			//say where the game thread is stuck
			for (java.util.Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()){
				String n = e.getKey().getName();
				if (n.contains("GL") || n.contains("Render") || n.contains("main")){
					StringBuilder sb = new StringBuilder("thread " + n + " " + e.getKey().getState());
					int k = 0;
					for (StackTraceElement el : e.getValue()) if (k++ < 14) sb.append("\n   at ").append(el);
					Log.w(TAG, sb.toString());
				}
			}
		}
		assertTrue("the render thread did not answer", done.getCount() == 0);
		if (failure.get() != null) throw new AssertionError("failed on the render thread: " + failure.get(), failure.get());
	}

	@SuppressWarnings("unchecked")
	private static List<Gizmo> membersOf( Group g ) throws Exception {
		Field f = Group.class.getDeclaredField("members");
		f.setAccessible(true);
		List<Gizmo> list = (List<Gizmo>) f.get(g);
		return list == null ? new ArrayList<Gizmo>() : new ArrayList<>(list);
	}

	private static <T> T find( Gizmo g, Class<T> type ) throws Exception {
		if (type.isInstance(g)) return type.cast(g);
		if (g instanceof Group){
			for (Gizmo m : membersOf((Group) g)){
				T t = find(m, type);
				if (t != null) return t;
			}
		}
		return null;
	}

	private static int count( Group g, Class<?> type ) throws Exception {
		int n = 0;
		for (Gizmo m : membersOf(g)) if (type.isInstance(m)) n++;
		return n;
	}

	private static Object field( Object o, String name ) throws Exception {
		for (Class<?> type = o.getClass(); type != null; type = type.getSuperclass()) {
			try {
				Field f = type.getDeclaredField(name);
				f.setAccessible(true);
				return f.get(o);
			} catch (NoSuchFieldException ignored) { }
		}
		throw new NoSuchFieldException(name);
	}

	//scrolls the control into view and returns the screen point of its centre
	private static Point centreOnScreen( final ScrollPane scroll, final Gizmo control ) throws Exception {
		final AtomicReference<Point> p = new AtomicReference<>();
		onRender(new Runnable(){
			public void run(){
				com.watabou.noosa.ui.Component c = (com.watabou.noosa.ui.Component) control;
				scroll.scrollTo(0, Math.max(0, c.top() - 20));
			}
		});
		Thread.sleep(400);
		onRender(new Runnable(){
			public void run(){
				com.watabou.noosa.ui.Component c = (com.watabou.noosa.ui.Component) control;
				p.set(c.camera().cameraToScreen(c.left() + c.width() / 2f, c.top() + c.height() / 2f));
			}
		});
		return p.get();
	}

	private static void tap( Point at ) throws Exception {
		PointerEvent.addPointerEvent(new PointerEvent(at.x, at.y, 9, PointerEvent.Type.DOWN, PointerEvent.LEFT));
		Thread.sleep(150);
		PointerEvent.addPointerEvent(new PointerEvent(at.x, at.y, 9, PointerEvent.Type.UP, PointerEvent.LEFT));
		Thread.sleep(400);
	}

	private static int messages() throws Exception {
		return count(Game.scene(), WndMessage.class);
	}

	//a result window is modal: close them so the next touch reaches the settings window again
	private static void dismissMessages() throws Exception {
		onRender(new Runnable(){
			public void run(){
				try {
					for (Gizmo m : membersOf(Game.scene())) if (m instanceof WndMessage) ((WndMessage) m).hide();
				} catch (Exception e){
					throw new RuntimeException(e);
				}
			}
		});
		Thread.sleep(300);
	}

	private static void waitUntil( String what, java.util.concurrent.Callable<Boolean> c, long millis ) throws Exception {
		long end = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis() < end){
			if (c.call()) return;
			Thread.sleep(150);
		}
		throw new AssertionError("timed out waiting for " + what);
	}


	//touches the control until it answers: a late result window of an earlier step can swallow a touch, so closed windows and a missed
	//position are retried. A control that never answers (the bug this test exists for) fails after every attempt.
	private static void tapUntil( String what, ScrollPane scroll, Gizmo control, java.util.concurrent.Callable<Boolean> answered, long millis ) throws Exception {
		long end = System.currentTimeMillis() + millis;
		int attempt = 0;
		while (System.currentTimeMillis() < end){
			attempt++;
			dismissMessages();
			Point at = centreOnScreen(scroll, control);
			Log.w(TAG, what + ": touch " + attempt + " at " + at.x + "," + at.y + " windows=" + messages());
			tap(at);
			long until = Math.min(end, System.currentTimeMillis() + 6000);
			while (System.currentTimeMillis() < until){
				if (answered.call()) { Log.w(TAG, what + " answered"); return; }
				Thread.sleep(150);
			}
		}
		throw new AssertionError(what + " did not answer a touch");
	}

	@Test
	public void adultHumorControlWorksWithoutAnAgeProviderAndReopensWithSavedChoice() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
			openTitleForTest();
			final boolean previous = SPDSettings.apdAdultHumor();
			final int previousSpellButtons = SPDSettings.apdClericQuickSlots();
            final int previousSpeed = SPDSettings.playbackSpeed();
			final AtomicReference<WndAPDSettings> window = new AtomicReference<>();
			try {
				onRender(() -> {
					SPDSettings.apdAdultHumor(false);
					window.set(new WndAPDSettings());
					ShatteredPixelDungeon.scene().add(window.get());
				});
				Thread.sleep(800);
				ScrollPane scroll = find(window.get(), ScrollPane.class);
				APDSettingsPane pane = find(window.get(), APDSettingsPane.class);
				tapUntil("enable Adult humor", scroll, (Gizmo) field(pane, "chkAdult"), () -> SPDSettings.apdAdultHumor(), 60000);
				assertEquals("no unavailable-verification warning", 0, messages());
				onRender(() -> {
					window.get().hide();
					window.set(new WndAPDSettings());
					ShatteredPixelDungeon.scene().add(window.get());
				});
				Thread.sleep(800);
				scroll = find(window.get(), ScrollPane.class);
				pane = find(window.get(), APDSettingsPane.class);
				assertTrue(((com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox) field(pane, "chkAdult")).checked());
				tapUntil("choose Clean humor", scroll, (Gizmo) field(pane, "chkAdult"), () -> !SPDSettings.apdAdultHumor(), 60000);
				onRender(() -> {
					window.get().hide();
					window.set(new WndAPDSettings());
					ShatteredPixelDungeon.scene().add(window.get());
				});
				Thread.sleep(800);
				pane = find(window.get(), APDSettingsPane.class);
				assertFalse(((com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox) field(pane, "chkAdult")).checked());
				assertFalse(SPDSettings.apdAdultHumor());
                for (Field setting : APDSettingsPane.class.getDeclaredFields())
                    assertFalse("removed hotbar settings must not return", setting.getName().equals("btnHotbar"));
                scroll = find(window.get(), ScrollPane.class);
                final RedButton spells = (RedButton) field(pane, "btnClericQuickCasts");
                assertNotNull("Cleric controls remain directly accessible", spells);
                tapUntil("Cleric quick-cast buttons", scroll, spells,
                        () -> find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class) != null, 30000);
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList choices =
                        find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class);
                assertEquals("only one through four spell-button choices", 4, communityEntries(choices).size());
                tapCommunityEntry(choices, 2);
                assertEquals("Cleric setting still answers real touches", 3, SPDSettings.apdClericQuickSlots());
                final RedButton speedButton = (RedButton)field(pane,"btnPlaybackSpeed");
                for(int mode=0;mode<3;mode++){
                    final int selected=mode;
                    tapUntil("open game-speed choices",scroll,speedButton,
                        () -> find(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions.class)!=null,30000);
                    com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions chooser=
                        find(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions.class);
                    java.util.List<RedButton> options=new java.util.ArrayList<>();
                    for(Gizmo child:membersOf(chooser))if(child instanceof RedButton)options.add((RedButton)child);
                    assertEquals("exactly three playback choices",3,options.size());
                    RedButton choice=options.get(mode);
                    AtomicReference<Point> touch=new AtomicReference<>();
                    onRender(()->touch.set(choice.camera().cameraToScreen(choice.left()+choice.width()/2f,choice.top()+choice.height()/2f)));
                    tap(touch.get());
                    waitUntil("selected speed saved",()->SPDSettings.playbackSpeed()==selected &&
                        find(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions.class)==null,10000);
                }
                onRender(()->{window.get().hide();window.set(new WndAPDSettings());ShatteredPixelDungeon.scene().add(window.get());});
                Thread.sleep(300);
                pane=find(window.get(),APDSettingsPane.class);
                assertEquals("Game speed: 2x",((RedButton)field(pane,"btnPlaybackSpeed")).text());

			} finally {
				onRender(() -> {
					if (window.get() != null) window.get().hide();
					SPDSettings.apdAdultHumor(previous);
                    SPDSettings.apdClericQuickSlots(previousSpellButtons);
                    SPDSettings.playbackSpeed(previousSpeed);
				});
			}
		}
	}

	// The game deliberately remembers its last scene across Activity recreation.
	// A preceding test can leave HeroSelectScene or GameScene active; launch is not a title reset.
	private static void openTitleForTest() throws Exception {
		waitUntil("the game", () -> Gdx.app != null && Gdx.files != null && Game.scene() != null, 120000);
		onRender(() -> ShatteredPixelDungeon.switchNoFade(com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene.class));
		waitUntil("the title scene", () -> Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene, 30000);
		onRender(() -> {});
	}

	@SuppressWarnings("unchecked")
	private static List<com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Entry> communityEntries(
			com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList window) throws Exception {
		return (List<com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Entry>) field(window, "entries");
	}

	@SuppressWarnings("unchecked")
	private static void tapCommunityEntry(com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList window, int index) throws Exception {
		ScrollPane pane = (ScrollPane) field(window, "pane");
		float[] span = ((List<float[]>) field(window, "spans")).get(index);
		assertNotNull("the selected row is actionable", span);
		onRender(() -> pane.scrollTo(0, Math.max(0, span[0] - 2)));
		Thread.sleep(300);
		AtomicReference<Point> point = new AtomicReference<>();
		onRender(() -> point.set(pane.content().camera().cameraToScreen(pane.width() / 2f, span[0] + span[1] / 2f)));
		tap(point.get());
		waitUntil("the Community destination", () -> find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class) != window, 10000);
	}

	private static void assertTextFits(Group group, float width) throws Exception {
		for (Gizmo member : membersOf(group)) {
			if (member instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock) {
				com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock text = (com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock) member;
				assertTrue("text exceeds its container: " + text.text(), text.left() >= 0 && text.right() <= width + 0.5f);
			}
		}
	}

	@Test
	public void communityMenuOpensEveryPreviewAndReturnsWithoutStackingWindows() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
			openTitleForTest();
			com.watabou.noosa.ui.Component button = (com.watabou.noosa.ui.Component) field(Game.scene(), "btnCommunity");
			AtomicReference<Point> point = new AtomicReference<>();
			onRender(() -> point.set(button.camera().cameraToScreen(button.left() + button.width()/2f, button.top() + button.height()/2f)));
			tap(point.get());
			waitUntil("the Community hub", () -> Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.CommunityScene, 30000);
			for (com.shatteredpixel.shatteredpixeldungeon.apd.community.CommunityPreview.Page page :
					com.shatteredpixel.shatteredpixeldungeon.apd.community.CommunityPreview.Page.values()) {
				com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList hub = find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class);
				assertNotNull(hub);
				assertEquals(page.title, communityEntries(hub).get(page.ordinal()).label);
				assertEquals(page.summary, communityEntries(hub).get(page.ordinal()).detail);
				tapCommunityEntry(hub, page.ordinal());
				Object previousPageScene = Game.scene();
				onRender(ShatteredPixelDungeon::seamlessResetScene);
				waitUntil("Community page survives scene recreation", () ->
						Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.CommunityScene
						&& Game.scene() != previousPageScene, 30000);
				onRender(() -> {});
				com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList details = find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class);
				List<com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.Entry> entries = communityEntries(details);
				assertEquals(page.explanation, entries.get(0).label);
				assertTrue(entries.stream().anyMatch(e -> e.label.equals(page.availability())));
				assertTrue(entries.stream().noneMatch(e -> e.label.toLowerCase().contains("clan")));
				onRender(() -> {
					try {
						assertEquals("one dialog, no hidden stack", 1, count(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class));
						ScrollPane pane = (ScrollPane) field(details, "pane");
						assertTextFits(details, (Integer) field(details, "width"));
						assertTextFits(pane.content(), pane.width());
					} catch (Exception e) { throw new RuntimeException(e); }
				});
				//Render the actual selected page at a narrow/short UI size, then
				//rebuild at the device's real size before continuing touch navigation.
				onRender(() -> {
					int originalWidth = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.width;
					int originalHeight = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.height;
					try {
						details.hide();
						com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.width = 100;
						com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.height = 120;
						java.lang.reflect.Method show = Game.scene().getClass().getDeclaredMethod("showPage");
						show.setAccessible(true); show.invoke(Game.scene());
						com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList narrowPage =
								find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class);
						assertEquals("same selected page on narrow screen", page.explanation, communityEntries(narrowPage).get(0).label);
						assertTrue((Integer) field(narrowPage, "width") <= 72);
						assertTrue((Integer) field(narrowPage, "height") <= 120);
						ScrollPane pane = (ScrollPane) field(narrowPage, "pane");
						assertTextFits(narrowPage, (Integer) field(narrowPage, "width"));
						assertTextFits(pane.content(), pane.width());
						narrowPage.hide();
					} catch (Exception e) { throw new RuntimeException(e); }
					finally {
						com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.width = originalWidth;
						com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.height = originalHeight;
					}
				});
				Object narrowScene = Game.scene();
				onRender(ShatteredPixelDungeon::seamlessResetScene);
				waitUntil("Community returns to device layout", () -> Game.scene() != narrowScene, 30000);
				onRender(() -> {});
				com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList restored =
						find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class);
				assertEquals("no stack after narrow layout", 1, count(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class));
				tapCommunityEntry(restored, communityEntries(restored).size() - 1);
			}
			com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList hub = find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList.class);
			tapCommunityEntry(hub, communityEntries(hub).size() - 1);
			waitUntil("return to main menu", () -> Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene, 30000);
		}
	}

	@Test
	public void everyControlOfApdSettingsAnswersATouch() throws Exception {
		final AtomicInteger exports = new AtomicInteger(), imports = new AtomicInteger();
		ApdFiles.Provider before = ApdFiles.provider;
		final ApdFiles.Provider fake;
		//the system file picker is replaced by one that records the request and says the player cancelled, so the test can see the click arrive
		fake = new ApdFiles.Provider(){
			public void chooseExport( String suggestedName, ApdFiles.ExportTarget target ){
				exports.incrementAndGet();
				target.onCancelled();
			}

			public void chooseImport( ApdFiles.ImportSource source ){
				imports.incrementAndGet();
				source.onCancelled();
			}

			public String where(){
				return "test";
			}
		};
		boolean recordingWas = SPDSettings.apdRecording();
		boolean adultWas = SPDSettings.apdAdultHumor();
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)){
			long end = System.currentTimeMillis() + 120000;
			while ((Gdx.app == null || Gdx.files == null || Game.scene() == null) && System.currentTimeMillis() < end) Thread.sleep(250);
			assertNotNull("the game did not start", Game.scene());
			//the launcher installs the real file picker when it starts: put the stand-in in only now, or the touch opens the system picker
			ApdFiles.provider = fake;
			//the first scene (welcome / splash) is replaced by the title: a window added before that is destroyed with it
			waitUntil("the title scene", new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() throws Exception { return Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene; } }, 120000);
			Thread.sleep(2500);

			final AtomicReference<WndAPDSettings> window = new AtomicReference<>();
			onRender(new Runnable(){
				public void run(){
					WndAPDSettings w = new WndAPDSettings();
					ShatteredPixelDungeon.scene().add(w);
					window.set(w);
				}
			});
			Thread.sleep(800);
			final ScrollPane scroll = find(window.get(), ScrollPane.class);
			final APDSettingsPane pane = find(window.get(), APDSettingsPane.class);
			assertNotNull("the window has a scrolling pane", scroll);
			assertNotNull("the window holds the APD settings", pane);

			//1. Run self-test: a result window follows
			dismissMessages();
			RedButton test = (RedButton) field(pane, "btnTest");
			tap(centreOnScreen(scroll, test));
			waitUntil("the self-test result (Run self-test did not answer a touch)", new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() throws Exception { return messages() >= 1; } }, 120000);
			Log.w(TAG, "Run self-test answered");

			//2. Import APD rankings
			tapUntil("Import APD rankings", scroll, (RedButton) field(pane, "btnImport"), new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() throws Exception { return messages() >= 1; } }, 90000);

			//3. Export history (ZIP): the file picker is asked for a place to save
			tapUntil("Export history (ZIP)", scroll, (RedButton) field(pane, "btnExportHistory"), new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() throws Exception { return exports.get() >= 1; } }, 60000);

			//4. Import history (ZIP): the file picker is asked for a file
			tapUntil("Import history (ZIP)", scroll, (RedButton) field(pane, "btnImportHistory"), new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() throws Exception { return imports.get() >= 1; } }, 60000);

			//5. the setting that explains itself with a message when touched
			final boolean recBefore = SPDSettings.apdRecording();
			tapUntil("Record run history", scroll, (com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox) field(pane, "chkRecord"), new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() throws Exception { return SPDSettings.apdRecording() != recBefore; } }, 60000);

			//5b. Without an age provider, real touches can select Adult and switch back to Clean.
			assertFalse(SPDSettings.apdAdultHumor());
			tapUntil("Adult humor (18+)", scroll, (com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox) field(pane, "chkAdult"), new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() { return SPDSettings.apdAdultHumor(); } }, 60000);
			assertTrue(SPDSettings.apdAdultHumor());
			assertEquals("toggling Adult must not produce an unavailable-verification warning", 0, messages());
			tapUntil("Clean humor", scroll, (com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox) field(pane, "chkAdult"), new java.util.concurrent.Callable<Boolean>(){
				public Boolean call() { return !SPDSettings.apdAdultHumor(); } }, 60000);
			assertFalse(SPDSettings.apdAdultHumor());

			//6. Open UIMSD (preview) last: it leaves the window by switching scenes
			tapUntil("Open UIMSD (preview)", scroll, (RedButton) field(pane, "btnUimsd"), new java.util.concurrent.Callable<Boolean>(){
				public Boolean call(){ return Game.scene() instanceof UimsdScene; } }, 60000);
			Log.w(TAG, "every APD Settings control answered a touch");
		} finally {
			SPDSettings.apdRecording(recordingWas);
			SPDSettings.apdAdultHumor(adultWas);
			ApdFiles.provider = before;
		}
	}

	// Regression for the owner's first alpha screenshot: the Grudge page draws normally
	// but its scroll controller was registered after the button hit areas and ate every tap.
	@Test
	public void grudgeControlsRespondToActualTouches() throws Exception {
		try (ActivityScenario<AndroidLauncher> scenario = ActivityScenario.launch(AndroidLauncher.class)) {
			openTitleForTest();
			assertTrue("Grudge page is enabled in this private test build",
					com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.available());
			final com.shatteredpixel.shatteredpixeldungeon.apd.humor.Frequency oldFrequency =
					com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.frequency();
			final com.shatteredpixel.shatteredpixeldungeon.apd.humor.Location place =
					com.shatteredpixel.shatteredpixeldungeon.apd.humor.Location.STARTUP;
			final boolean oldPlace =
					com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.prefs().enabled(place);
			final AtomicReference<WndAPDSettings> settings = new AtomicReference<>();
			try {
				onRender(() -> {
					settings.set(new WndAPDSettings());
					ShatteredPixelDungeon.scene().add(settings.get());
				});
				Thread.sleep(400);
				ScrollPane settingsScroll = find(settings.get(), ScrollPane.class);
				APDSettingsPane settingsPane = find(settings.get(), APDSettingsPane.class);
				RedButton opener = (RedButton) field(settingsPane, "btnGrudge");
				assertNotNull("Grudge option appears inside APD Settings", opener);
				tapUntil("open Grudge from APD Settings", settingsScroll, opener,
						() -> find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndClericGrudge.class) != null, 30000);

				com.shatteredpixel.shatteredpixeldungeon.windows.WndClericGrudge page =
						find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndClericGrudge.class);
				assertNotNull(page);
				ScrollPane scroll = find(page, ScrollPane.class);
				assertNotNull(scroll);
				RedButton frequency = (RedButton) field(page, "btnFrequency");
				assertNotNull(frequency);
				tapUntil("change Grudge frequency", scroll, frequency,
						() -> com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.frequency() == oldFrequency.next(), 30000);

				com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox startup =
						find(scroll.content(), com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox.class);
				assertNotNull("location switches exist in the Grudge scroll content", startup);
				assertEquals(oldPlace, startup.checked());
				tapUntil("toggle Grudge startup messages", scroll, startup,
						() -> com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.prefs().enabled(place) != oldPlace, 30000);
				assertEquals(!oldPlace, startup.checked());

				RedButton reset = null;
				for (Gizmo g : membersOf(scroll.content())) {
					if (g instanceof RedButton && "Forget what I've heard".equals(((RedButton)g).text())) {
						reset = (RedButton)g;
						break;
					}
				}
				assertNotNull("history confirmation control is present", reset);
				tapUntil("open history confirmation without clearing it", scroll, reset,
						() -> find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions.class) != null, 30000);
				assertNotNull("confirmation must appear without erasing history",
						find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions.class));
			} finally {
				onRender(() -> {
					try {
						com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions confirm =
								find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions.class);
						if (confirm != null) confirm.hide(); //never confirm destructive history reset
						com.shatteredpixel.shatteredpixeldungeon.windows.WndClericGrudge page =
								find(Game.scene(), com.shatteredpixel.shatteredpixeldungeon.windows.WndClericGrudge.class);
						if (page != null) page.hide();
					} catch (Exception e) { throw new RuntimeException(e); }
					if (settings.get() != null) settings.get().hide();
					com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.setFrequency(oldFrequency);
					com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.prefs().setEnabled(place, oldPlace);
				});
			}
		}
	}

}
