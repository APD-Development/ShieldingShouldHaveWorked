package com.shatteredpixel.shatteredpixeldungeon.android;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.os.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.simulation.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

// Runs only on an explicitly opted-in disposable emulator, never on a player's installation.
@RunWith(AndroidJUnit4.class)
public class WarriorSimulationDeviceTest {
    private static <T> T render(Callable<T> action) {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Game.runOnRenderThread(() -> {
            try { result.set(action.call()); } catch (Throwable t) { failure.set(t); }
            finally { done.countDown(); }
        });
        try {
            if (!done.await(30, TimeUnit.SECONDS)) throw new IllegalStateException("render timeout");
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
        if (failure.get() != null) throw new IllegalStateException("render action failed", failure.get());
        return result.get();
    }
    private static void until(Callable<Boolean> condition, long seconds) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        try {
            while (!condition.call()) {
                if (System.nanoTime() >= deadline) throw new IllegalStateException("game readiness timeout");
                Thread.sleep(25);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    private static final class Session implements WarriorHealingExperiment.GameSession {
        private int[] visits;
        private int depth, swarmDrops;
        private boolean flyHealing;
        private final String log;
        private long scannedEvents;
        Session(long seed) {
            render(() -> { Game.switchScene(TitleScene.class); return null; });
            until(() -> Game.scene() instanceof TitleScene && !Game.switchingScene(), 60);
            render(() -> {
                GamesInProgress.curSlot = 1;
                GamesInProgress.selectedClass = HeroClass.WARRIOR;
                GamesInProgress.randomizedClass = false;
                Dungeon.hero = null;
                Dungeon.daily = Dungeon.dailyReplay = false;
                SPDSettings.customSeed(DungeonSeed.convertToCode(seed));
                Dungeon.initSeed();
                ActionIndicator.clearAction();
                InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                Game.switchScene(InterlevelScene.class);
                return null;
            });
            waitForInput();
            log = ApdRecorder.status().logPath;
            if (log == null) throw new IllegalStateException("simulation recording missing");
            swarmDrops = Dungeon.LimitedDrops.SWARM_HP.count;
        }
        private void waitForInput() {
            until(() -> Dungeon.hero != null && (!Dungeon.hero.isAlive()
                    || (Game.scene() instanceof GameScene && !Game.switchingScene() && Dungeon.hero.ready)), 90);
        }
        private Mob adjacent() {
            return Dungeon.level.mobs.stream().filter(m -> m.isAlive() && m.alignment == Char.Alignment.ENEMY
                    && Dungeon.hero.fieldOfView[m.pos] && Dungeon.level.adjacent(Dungeon.hero.pos, m.pos))
                    .min(Comparator.comparingInt(m -> m.pos)).orElse(null);
        }
        // Audited normal Sewers melee bounds; never calls damageRoll or consumes RNG to predict damage.
        private int bound(Mob mob) {
            if (mob == null) return 0;
            if (mob.getClass() == Rat.class || mob.getClass() == Snake.class || mob.getClass() == Swarm.class) return 4;
            if (mob.getClass() == Gnoll.class) return 6;
            if (mob.getClass() == Crab.class) return 7;
            if (mob.getClass() == Piranha.class) return 4 + 2 * Dungeon.depth;
            throw new IllegalStateException("unaudited melee bound: " + mob.getClass().getSimpleName());
        }
        private int healing() {
            int count = 0;
            for (PotionOfHealing p : Dungeon.hero.belongings.getAllItems(PotionOfHealing.class)) count += p.quantity();
            return count;
        }
        public RecklessWarriorPolicy.Observation observe() {
            waitForInput();
            return render(() -> new RecklessWarriorPolicy.Observation(Math.max(0, Dungeon.hero.HP),
                    Dungeon.hero.HT, bound(adjacent()), healing(), adjacent() != null,
                    flyHealing || Dungeon.depth >= 5));
        }
        public boolean hasReceivedNormalFlyHealingDrop() { return flyHealing; }
        public String fingerprint() {
            return render(() -> Dungeon.depth + ":" + Dungeon.hero.pos + ":" + Dungeon.hero.HP
                    + ":" + Dungeon.hero.lvl + ":" + Actor.now() + ":" + healing());
        }
        private void explore() {
            if (depth != Dungeon.depth) { depth = Dungeon.depth; visits = new int[Dungeon.level.length()]; }
            int pos = Dungeon.hero.pos;
            visits[pos]++;
            Hunger hunger = Dungeon.hero.buff(Hunger.class);
            Food food = Dungeon.hero.belongings.getItem(Food.class);
            if (hunger != null && hunger.isStarving() && food != null) { food.execute(Dungeon.hero, Food.AC_EAT); return; }
            Heap here = Dungeon.level.heaps.get(pos);
            if (here != null && here.type == Heap.Type.HEAP) { Dungeon.hero.handle(pos); Dungeon.hero.next(); return; }
            Mob visible = Dungeon.level.mobs.stream().filter(m -> m.isAlive() && m.alignment == Char.Alignment.ENEMY
                    && Dungeon.hero.fieldOfView[m.pos]).min(Comparator.comparingInt(m -> Dungeon.level.distance(pos, m.pos))).orElse(null);
            if (visible != null) { Dungeon.hero.handle(visible.pos); Dungeon.hero.next(); return; }
            boolean[] known = new boolean[visits.length], pass = new boolean[visits.length];
            for (int i = 0; i < visits.length; i++) {
                known[i] = Dungeon.level.visited[i] || Dungeon.level.mapped[i];
                pass[i] = Dungeon.level.passable[i] && !Dungeon.level.pit[i];
                if (Dungeon.level.traps.get(i) != null && Dungeon.level.traps.get(i).visible && Dungeon.level.traps.get(i).active)
                    pass[i] = false;
            }
            int step = KnownMapExplorer.step(Dungeon.level.width(), pos, known, pass, visits, Dungeon.level.entrance());
            if (step < 0) throw new IllegalStateException("no known traversable destination");
            Dungeon.hero.handle(step);
            Dungeon.hero.next();
        }
        public void perform(RecklessWarriorPolicy.Action action) {
            final float before = Actor.now();
            final int beforeDepth = Dungeon.depth;
            render(() -> {
                if (GameScene.showingWindow()) throw new IllegalStateException("unhandled gameplay dialog");
                switch (action) {
                    case HEAL:
                        PotionOfHealing potion = Dungeon.hero.belongings.getItem(PotionOfHealing.class);
                        if (potion == null) throw new IllegalStateException("healing disappeared");
                        potion.execute(Dungeon.hero, Potion.AC_DRINK); break;
                    case ATTACK:
                        Mob enemy = adjacent();
                        if (enemy == null) throw new IllegalStateException("target disappeared");
                        Dungeon.hero.handle(enemy.pos); Dungeon.hero.next(); break;
                    case EXPLORE: explore(); break;
                    default: throw new IllegalArgumentException("unexpected action");
                }
                return null;
            });
            until(() -> !Dungeon.hero.isAlive() || Dungeon.depth != beforeDepth || Actor.now() > before, 30);
            waitForInput();
            if (Dungeon.LimitedDrops.SWARM_HP.count != swarmDrops) {
                swarmDrops = Dungeon.LimitedDrops.SWARM_HP.count;
                scanFlyDrops();
            }
        }
        private void scanFlyDrops() {
            ApdRecorder.flushForReading();
            try {
                ReadResult read = RunLogReader.read(new File(log));
                if (!read.status.isClean() || read.gapEvents != 0) throw new IllegalStateException("incomplete generation evidence");
                for (ApdEvent event : read.events) if (event.index >= scannedEvents && event.type == EventTypes.ITEM_GENERATED) {
                    GenerationEvents.ItemGenerated drop = GenerationEvents.decodeItemGenerated(event, read);
                    if (NormalFlyHealingEvidence.matches(drop)) flyHealing = true;
                }
                scannedEvents = read.eventCount;
            } catch (IOException e) { throw new IllegalStateException("cannot inspect run evidence", e); }
        }
        public void close() {
            ApdRecorder.flushForReading();
            render(() -> { Game.switchScene(TitleScene.class); return null; });
            until(() -> Game.scene() instanceof TitleScene && !Game.switchingScene(), 60);
        }
    }
    @Test public void simulateRecklessWarriorEarlyHealing() throws Exception {
        Bundle arguments = InstrumentationRegistry.getArguments();
        assertEquals("requires a disposable, empty emulator", "true", arguments.getString("apdSimulation"));
        assertTrue("physical-device simulation is refused", android.os.Build.HARDWARE.equals("ranchu")
                || android.os.Build.HARDWARE.equals("goldfish"));
        int count = Integer.parseInt(arguments.getString("simulationCount", "1"));
        int limit = Integer.parseInt(arguments.getString("simulationActions", "1000"));
        long seed = Long.parseLong(arguments.getString("simulationSeed", "1000000"));
        assertTrue(count > 0 && count <= 100 && limit > 0 && limit <= 5000);
        File output = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir(), "simulation-results.jsonl");
        try (ActivityScenario<AndroidLauncher> activity = ActivityScenario.launch(AndroidLauncher.class)) {
            until(() -> Game.scene() != null, 120);
            render(() -> {
                for (int slot = 1; slot <= GamesInProgress.MAX_SLOTS; slot++)
                    if (GamesInProgress.gameExists(slot)) throw new IllegalStateException("existing saves: simulator refused");
                if (com.watabou.utils.FileUtils.fileExists(Rankings.RANKINGS_FILE))
                    throw new IllegalStateException("existing statistics: simulator refused");
                File root = ApdRecorder.historyRoot();
                File[] history = root == null ? null : new File(root, "runs").listFiles();
                if (history != null && history.length != 0) throw new IllegalStateException("existing history: simulator refused");
                SPDSettings.apdDifficulty(3); SPDSettings.challenges(0); SPDSettings.apdRecording(true);
                return null;
            });
            int incomplete = 0, executed = 0, consecutiveAdapterFailures = 0;
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8)) {
                for (int i = 0; i < count; i++) {
                    long started = System.currentTimeMillis(), monotonicStart = System.nanoTime();
                    WarriorHealingExperiment.Result result = WarriorHealingExperiment.run(seed + i, Session::new, limit);
                    JSONObject row = new JSONObject().put("seed", result.seed).put("completed", result.completed)
                            .put("observedHealingBeforeNormalFlyHealing", result.neededHeldHealingBeforeFlyDrop)
                            .put("stopReason", result.stopReason).put("startedEpochMillis", started)
                            .put("finishedEpochMillis", System.currentTimeMillis())
                            .put("elapsedMillis", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - monotonicStart))
                            .put("trace", new JSONArray(result.trace));
                    writer.write(row.toString()); writer.write('\n'); writer.flush();
                    executed++;
                    if (!result.completed) incomplete++;
                    consecutiveAdapterFailures = result.stopReason.equals("game adapter failure") ? consecutiveAdapterFailures + 1 : 0;
                    if (consecutiveAdapterFailures >= 2) break;
                }
            }
            assertEquals("batch stopped after repeated adapter failures; no absent seeds are counted", count, executed);
            assertEquals("incomplete episodes are not balance evidence; inspect preserved traces", 0, incomplete);
        }
    }
}
