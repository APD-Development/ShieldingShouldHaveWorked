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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.levels.LevelTransition;
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
import com.watabou.noosa.Group;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import java.lang.reflect.Field;
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
        if (failure.get() instanceof RuntimeException) throw (RuntimeException) failure.get();
        if (failure.get() != null) throw new IllegalStateException("render action failed: " + failure.get(), failure.get());
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
        private String windowTypes() throws Exception {
            Field field = Group.class.getDeclaredField("members");
            field.setAccessible(true);
            List<?> members = (List<?>) field.get(Game.scene());
            List<String> names = new ArrayList<>();
            if (members != null) for (Object member : members)
                if (member instanceof Window) names.add(member.getClass().getName());
            return names.toString();
        }
        private int[] visits;
        private int depth, swarmDrops;
        private boolean flyHealing;
        private final String log;
        private long scannedEvents;
        private final List<String> commands = new ArrayList<>();
        private Item rejectedPickup;
        private int rejectedHp, rejectedInventory;
        private boolean attemptedPickup;
        private void command(String kind, int target) {
            commands.add(kind + ":depth=" + Dungeon.depth + ":from=" + Dungeon.hero.pos
                    + ":to=" + target + ":time=" + Actor.now());
        }
        private void move(int target) {
            command("MOVE", target);
            // Contextual taps can open NPC/treasure dialogs. Execute the ordinary movement action instead.
            Dungeon.hero.curAction = new HeroAction.Move(target);
            Dungeon.hero.lastAction = null;
            Dungeon.hero.next();
        }
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
                    && Dungeon.hero.fieldOfView[m.pos] && !Dungeon.hero.isCharmedBy(m)
                    && Dungeon.level.adjacent(Dungeon.hero.pos, m.pos))
                    .min(Comparator.comparingInt(m -> m.pos)).orElse(null);
        }
        // Audited normal Sewers melee bounds; never calls damageRoll or consumes RNG to predict damage.
        private int bound(Mob mob) {
            return AuditedSewersMeleeBounds.maximum(mob, Dungeon.depth);
        }
        private int healing() {
            int count = 0;
            for (PotionOfHealing p : Dungeon.hero.belongings.getAllItems(PotionOfHealing.class)) count += p.quantity();
            return count;
        }
        public RecklessWarriorPolicy.Observation observe() {
            waitForInput();
            return render(() -> {
                boolean finished = flyHealing || Dungeon.depth >= 5 || !Dungeon.hero.isAlive();
                Mob enemy = finished ? null : adjacent();
                return new RecklessWarriorPolicy.Observation(Math.max(0, Dungeon.hero.HP),
                        Dungeon.hero.HT, bound(enemy), healing(), enemy != null, finished);
            });
        }
        public boolean hasReceivedNormalFlyHealingDrop() { return flyHealing; }
        public String fingerprint() {
            return render(() -> {
                Heap heap = Dungeon.level.heaps.get(Dungeon.hero.pos);
                return Dungeon.depth + ":" + Dungeon.hero.pos + ":" + Dungeon.hero.HP
                        + ":" + Dungeon.hero.lvl + ":" + Actor.now() + ":" + healing()
                        + ":heap=" + (heap == null ? 0 : heap.size());
            });
        }
        private void explore() {
            if (depth != Dungeon.depth) { depth = Dungeon.depth; visits = new int[Dungeon.level.length()]; }
            int pos = Dungeon.hero.pos;
            visits[pos]++;
            if (Dungeon.hero.rooted) {
                command("WAIT_ROOTED", pos); Dungeon.hero.rest(false); return;
            }
            Hunger hunger = Dungeon.hero.buff(Hunger.class);
            Food food = Dungeon.hero.belongings.getItem(Food.class);
            if (hunger != null && hunger.isStarving() && food != null) {
                command("EAT:" + food.getClass().getSimpleName(), pos);
                food.execute(Dungeon.hero, Food.AC_EAT); return;
            }
            Heap here = Dungeon.level.heaps.get(pos);
            if (here != null && here.type == Heap.Type.HEAP && (here.peek() != rejectedPickup
                    || Dungeon.hero.HP != rejectedHp || Dungeon.hero.belongings.backpack.items.size() != rejectedInventory)) {
                command("PICKUP:" + here.peek().getClass().getSimpleName(), pos);
                attemptedPickup = true;
                Dungeon.hero.handle(pos); Dungeon.hero.next(); return;
            }
            if (Dungeon.level.getTransition(pos) != null
                    && Dungeon.level.getTransition(pos).type == LevelTransition.Type.REGULAR_EXIT
                    && !Dungeon.level.locked && !Dungeon.level.plants.containsKey(pos)
                    && Dungeon.hero.visibleEnemies() == 0) {
                command("DESCEND", pos);
                Dungeon.hero.curAction = new HeroAction.LvlTransition(pos);
                Dungeon.hero.next(); return;
            }
            boolean[] pass = SimulationNavigation.walkable(Dungeon.level, Dungeon.hero);
            Mob visible = Dungeon.level.mobs.stream().filter(m -> m.isAlive() && m.alignment == Char.Alignment.ENEMY
                    && Dungeon.hero.fieldOfView[m.pos] && !Dungeon.hero.isCharmedBy(m))
                    .sorted(Comparator.comparingInt(m -> Dungeon.level.distance(pos, m.pos)))
                    .filter(m -> {
                        int step = Dungeon.findStep(Dungeon.hero, m.pos, pass, Dungeon.hero.fieldOfView, true);
                        return step >= 0 && pass[step];
                    }).findFirst().orElse(null);
            if (visible != null) {
                int step = Dungeon.findStep(Dungeon.hero, visible.pos, pass, Dungeon.hero.fieldOfView, true);
                move(step); return;
            }
            boolean[] known = new boolean[visits.length];
            for (int i = 0; i < visits.length; i++) {
                known[i] = Dungeon.level.visited[i] || Dungeon.level.mapped[i];
            }
            int step = KnownMapExplorer.step(Dungeon.level.width(), pos, known, pass, visits, Dungeon.level.entrance());
            if (step < 0) throw new IllegalStateException("no known traversable destination");
            move(step);
        }
        public void perform(RecklessWarriorPolicy.Action action) {
            final float before = Actor.now();
            final int beforeDepth = Dungeon.depth;
            final int beforePosition = Dungeon.hero.pos;
            final Item beforePickup = render(() -> {
                Heap heap = Dungeon.level.heaps.get(Dungeon.hero.pos);
                return heap == null ? null : heap.peek();
            });
            render(() -> {
                attemptedPickup = false;
                if (GameScene.showingWindow()) throw new IllegalStateException("unhandled gameplay dialog: " + windowTypes()
                        + ":position=" + Dungeon.hero.pos + ":entrance=" + Dungeon.level.entrance()
                        + ":exit=" + Dungeon.level.exit() + ":terrain=" + Dungeon.level.map[Dungeon.hero.pos]);
                switch (action) {
                    case HEAL:
                        PotionOfHealing potion = Dungeon.hero.belongings.getItem(PotionOfHealing.class);
                        if (potion == null) throw new IllegalStateException("healing disappeared");
                        command("HEAL", Dungeon.hero.pos);
                        potion.execute(Dungeon.hero, Potion.AC_DRINK); break;
                    case ATTACK:
                        Mob enemy = adjacent();
                        if (enemy == null) throw new IllegalStateException("target disappeared");
                        command("ATTACK:" + enemy.getClass().getSimpleName(), enemy.pos);
                        Dungeon.hero.handle(enemy.pos); Dungeon.hero.next(); break;
                    case EXPLORE: explore(); break;
                    default: throw new IllegalArgumentException("unexpected action");
                }
                return null;
            });
            try {
                until(() -> render(() -> SimulationReadiness.actionReleased(before, Actor.now(), beforeDepth,
                        Dungeon.depth, Dungeon.hero.isAlive(), Dungeon.hero.ready, Dungeon.hero.curAction != null)), 30);
            } catch (RuntimeException failure) {
                String state = render(() -> "scene=" + Game.scene().getClass().getSimpleName()
                        + ":ready=" + Dungeon.hero.ready + ":windows=" + windowTypes()
                        + ":queued=" + (Dungeon.hero.curAction == null ? "none" : Dungeon.hero.curAction.getClass().getSimpleName()
                        + "@" + Dungeon.hero.curAction.dst) + ":depth=" + Dungeon.depth
                        + ":position=" + Dungeon.hero.pos + ":actorTime=" + Actor.now());
                throw new IllegalStateException("action did not release input: " + state, failure);
            }
            waitForInput();
            render(() -> {
                Heap heap = Dungeon.level.heaps.get(Dungeon.hero.pos);
                if (attemptedPickup && beforePickup != null && beforeDepth == Dungeon.depth && beforePosition == Dungeon.hero.pos
                        && before == Actor.now() && heap != null && heap.peek() == beforePickup) {
                    rejectedPickup = beforePickup;
                    rejectedHp = Dungeon.hero.HP;
                    rejectedInventory = Dungeon.hero.belongings.backpack.items.size();
                    commands.add("REJECTED_PICKUP:" + beforePickup.getClass().getSimpleName());
                }
                return null;
            });
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
        long[] seeds = SimulationSeedPlan.parse(arguments.getString("simulationSeeds", ""), seed, count);
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
                    Session[] current = new Session[1];
                    WarriorHealingExperiment.Result result = WarriorHealingExperiment.run(seeds[i],
                            value -> { current[0] = new Session(value); return current[0]; }, limit);
                    JSONObject row = new JSONObject().put("seed", result.seed).put("completed", result.completed)
                            .put("observedHealingBeforeNormalFlyHealing", result.neededHeldHealingBeforeFlyDrop)
                            .put("normalFlyHealingObserved", current[0] != null && current[0].flyHealing)
                            .put("terminalDepth", Dungeon.depth)
                            .put("terminalHp", Dungeon.hero == null ? JSONObject.NULL : Dungeon.hero.HP)
                            .put("stopReason", result.stopReason).put("startedEpochMillis", started)
                            .put("finishedEpochMillis", System.currentTimeMillis())
                            .put("elapsedMillis", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - monotonicStart))
                            .put("commands", new JSONArray(current[0] == null ? Collections.emptyList() : current[0].commands))
                            .put("trace", new JSONArray(result.trace));
                    String serialized = row.toString();
                    writer.write(serialized); writer.write('\n'); writer.flush();
                    String encoded = android.util.Base64.encodeToString(
                            serialized.getBytes(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP);
                    int chunks = (encoded.length() + 1999) / 2000;
                    for (int chunk = 0; chunk < chunks; chunk++) {
                        int from = chunk * 2000, to = Math.min(encoded.length(), from + 2000);
                        android.util.Log.i("APDSIM", "APD_SIM_CHUNK:" + i + ":" + chunk + ":" + chunks + ":"
                                + encoded.substring(from, to));
                    }
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
