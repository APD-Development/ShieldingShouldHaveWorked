/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchControlContext.Action;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalChoiceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalVaultRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ArmoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.TreasuryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.StorageRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EmptyRoom;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ResumeIndicator;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWallSearchResume;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/** Real talent acquisition, eligibility, chooser dispatch and search costs; drawing is omitted. */
public class SearchControlContextTest {
    private static final int SIZE = 21;

    private static class Arena extends SewerLevel {
        void setRooms(Room... selected) { rooms = new ArrayList<>(Arrays.asList(selected)); }
    }

    public static class QuietSprite extends HeroSprite {
        @Override public void operate(int cell) { }
        @Override public void showStatus(int color, String text, Object... args) { }
    }

    private static class QuietHero extends Hero {
        int nextCalls;
        @Override public void next() { nextCalls++; }
    }

    private Hero oldHero;
    private Level oldLevel;
    private int oldDepth, oldBranch;
    private boolean oldTagDisappeared;
    private Field selector;
    private Object oldSelector;
    private final Map<Field, Object> oldActors = new LinkedHashMap<>();
    private Arena level;
    private QuietHero hero;
    private Method choose;

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        return type.cast(unsafe.getMethod("allocateInstance", Class.class)
                .invoke(field(unsafe, "theUnsafe").get(null), type));
    }

    private static int at(int x, int y) { return y * SIZE + x; }

    @Before public void setup() throws Exception {
        HeadlessTestSupport.start();
        Random.pushGenerator(445202611L);
        oldHero = Dungeon.hero;
        oldLevel = Dungeon.level;
        oldDepth = Dungeon.depth;
        oldBranch = Dungeon.branch;
        oldTagDisappeared = GameScene.tagDisappeared;
        for (String name : new String[]{"all", "chars", "ids", "current", "now", "nextID"}) {
            Field state = field(Actor.class, name);
            oldActors.put(state, state.get(null));
        }
        field(Actor.class, "all").set(null, new HashSet<Actor>());
        field(Actor.class, "chars").set(null, new HashSet<Char>());
        field(Actor.class, "ids").set(null, new SparseArray<Actor>());
        field(Actor.class, "current").set(null, null);
        field(Actor.class, "now").setFloat(null, 0f);
        field(Actor.class, "nextID").setInt(null, 1);
        selector = field(GameScene.class, "cellSelector");
        oldSelector = selector.get(null);
        selector.set(null, allocate(CellSelector.class));
        choose = WndWallSearchResume.class.getDeclaredMethod("choose", Hero.class, Level.class, Action.class);
        choose.setAccessible(true);

        Dungeon.depth = 3;
        Dungeon.branch = 0;
        level = new Arena();
        level.setSize(SIZE, SIZE);
        level.setRooms();
        level.mobs = new HashSet<>();
        level.traps = new SparseArray<>();
        level.blobs = new HashMap<>();
        level.heaps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.locked = true; // Search timing can be checked without adding hunger in policy cases.
        Dungeon.level = level;
        clearTerrain();
        hero = new QuietHero();
        hero.pos = at(10, 10);
        hero.HP = hero.HT = 20;
        hero.ready = true;
        hero.fieldOfView = level.heroFOV;
        hero.sprite = allocate(QuietSprite.class);
        Dungeon.hero = hero;
        talents(HeroClass.WARRIOR, false, 0);
        hero.wallSearch.searched(hero); // A real remembered completed search, with no active command.
    }

    @After public void restore() throws Exception {
        if (hero != null) for (Buff buff : new ArrayList<>(hero.buffs())) buff.detach();
        for (Map.Entry<Field, Object> entry : oldActors.entrySet()) entry.getKey().set(null, entry.getValue());
        selector.set(null, oldSelector);
        GameScene.tagDisappeared = oldTagDisappeared;
        Dungeon.hero = oldHero;
        Dungeon.level = oldLevel;
        Dungeon.depth = oldDepth;
        Dungeon.branch = oldBranch;
        if (oldLevel != null) PathFinder.setMapSize(oldLevel.width(), oldLevel.height());
        Random.popGenerator();
    }

    private void clearTerrain() {
        Arrays.fill(level.map, Terrain.EMPTY);
        Arrays.fill(level.secret, false);
        Arrays.fill(level.heroFOV, true);
        Arrays.fill(level.visited, true);
        Arrays.fill(level.mapped, false);
        Arrays.fill(level.passable, true);
        level.searchedWalls.clear();
    }

    /** Decorated side frames are valid supports but cannot be competing target candidates. */
    private int candidate(int dx, int dy) {
        int cell = hero.pos + dy * SIZE + dx;
        level.map[cell] = Terrain.WALL;
        level.map[cell - SIZE] = level.map[cell + SIZE] = Terrain.WALL_DECO;
        level.passable[cell] = level.passable[cell - SIZE] = level.passable[cell + SIZE] = false;
        assertTrue(DoorSearchEligibility.candidate(level, cell));
        return cell;
    }

    private void talents(HeroClass heroClass, boolean metamorph, int rank) {
        hero.heroClass = heroClass;
        hero.talents.clear();
        hero.metamorphedTalents.clear();
        Talent.initClassTalents(hero);
        if (metamorph) {
            Talent replaced = hero.talents.get(1).keySet().iterator().next();
            hero.metamorphedTalents.put(replaced, Talent.WIDE_SEARCH);
            hero.talents.clear();
            Talent.initClassTalents(hero);
        }
        for (int i = 0; i < rank; i++) hero.upgradeTalent(Talent.WIDE_SEARCH);
        assertEquals(rank, hero.pointsInTalent(Talent.WIDE_SEARCH));
    }

    private String memory() {
        Bundle bundle = new Bundle();
        hero.wallSearch.store(bundle);
        return bundle.toString();
    }

    private void select(Action action) throws Exception { choose.invoke(null, hero, level, action); }

    @Test public void openAreaPutsSearchFirstAndRetainsEverySavedOption() {
        assertArrayEquals(new Action[]{Action.SEARCH, Action.WALL_SEARCH, Action.SHOW_SAVED, Action.FORGET, Action.CANCEL},
                SearchControlContext.choices(hero, level));
        assertTrue(hero.wallSearch.exists());
        assertNull(hero.curAction);
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void onlyUnconfirmedObservedCandidatePrioritizesWallSearch() {
        int wall = candidate(1, 0);
        String saved = memory();
        assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
        level.searchedWalls.mark(level, wall); // A legacy attempt is not completed negative proof.
        assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
        level.searchedWalls.recordCompletedDoorCheck(level, wall, true);
        assertTrue(level.searchedWalls.confirmed(level, wall));
        assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
        assertEquals(saved, memory());
    }

    @Test public void floorTwoOrdinaryRoomCenterPrefersSearchEvenWhenWideSearchReachesWall() {
        int previousDepth = Dungeon.depth;
        boolean prior = SPDSettings.adaptiveSearchChoice();
        try {
            Dungeon.depth = 2;
            SPDSettings.adaptiveSearchChoice(true);
            hero.wallSearch.searched(hero); // The remembered search must belong to Floor 2.
            EmptyRoom ordinary = new EmptyRoom();
            ordinary.set(7, 7, 13, 13);
            level.setRooms(ordinary);
            talents(HeroClass.ROGUE, false, 0);
            int wall = candidate(2, 0);
            assertTrue(DoorSearchEligibility.candidate(level, wall));
            assertTrue("Rogue still has unchanged two-tile search footprint",
                    WallSearchSession.covers(hero, level, hero.pos, wall));
            assertEquals("middle of ordinary Floor 2 room must not prefer remembered Wall Search",
                    Action.SEARCH, SearchControlContext.preferred(hero, level));
            assertEquals(Action.SEARCH, SearchControlContext.choices(hero, level)[0]);
            hero.pos = at(11, 10);
            assertEquals("beside eligible wall should still prefer Wall Search",
                    Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
            SPDSettings.adaptiveSearchChoice(false);
            assertEquals("optional setting disables adaptive wall preference",
                    Action.SEARCH, SearchControlContext.preferred(hero, level));
            assertEquals(Action.SEARCH, SearchControlContext.choices(hero, level)[0]);
        } finally {
            SPDSettings.adaptiveSearchChoice(prior);
            Dungeon.depth = previousDepth;
            level.setRooms();
            hero.pos = at(10, 10);
        }
    }

    @Test public void actualSearchInMiddleOfFloorTwoRoomDoesNotMakeWallCheckmarks() throws Exception {
        int priorDepth = Dungeon.depth;
        boolean priorChoice = SPDSettings.adaptiveSearchChoice();
        try {
            Dungeon.depth = 2;
            SPDSettings.adaptiveSearchChoice(true);
            EmptyRoom room = new EmptyRoom();
            room.set(7, 7, 13, 13);
            level.setRooms(room);
            talents(HeroClass.ROGUE, false, 0);
            int wall = candidate(2, 0);
            select(Action.SEARCH);
            assertEquals(2f, hero.cooldown(), 0f);
            assertEquals("ordinary Search in room center must not record distant wall search",
                    false, level.searchedWalls.attempted(wall));
            assertTrue("normal Search still increments the remembered search count",
                    hero.wallSearch.searches() > 0);
            // A completed paid Search rightly keeps its negative doorway evidence.
            // Reset only this disposable test floor to inspect a fresh adjacent wall.
            level.searchedWalls.clear();
            hero.pos = at(11, 10);
            assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
            select(Action.SEARCH);
            assertTrue("adjacent eligible wall is recorded by the actual paid Search",
                    level.searchedWalls.attempted(wall));
            assertEquals("Each normal Search retains its own two-turn action cost",
                    2f, hero.cooldown(), 0f);
        } finally {
            SPDSettings.adaptiveSearchChoice(priorChoice);
            Dungeon.depth = priorDepth;
            hero.pos = at(10, 10);
            level.setRooms();
        }
    }

    @Test public void floorOneAndCrystalRoomsNeverPrioritizeAdaptiveWallChoice() {
        boolean prior = SPDSettings.adaptiveSearchChoice();
        int priorDepth = Dungeon.depth;
        try {
            SPDSettings.adaptiveSearchChoice(true);
            candidate(1, 0);
            Dungeon.depth = 1;
            assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
            Dungeon.depth = 2;
            for (Room room : new Room[]{new CrystalVaultRoom(), new CrystalChoiceRoom()}) {
                room.set(7, 7, 13, 13);
                level.setRooms(room);
                assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
            }
        } finally {
            SPDSettings.adaptiveSearchChoice(prior);
            Dungeon.depth = priorDepth;
            level.setRooms();
        }
    }

    @Test public void baseClassDoesNotPretendWideSearchWasAcquired() {
        candidate(2, 0);
        for (HeroClass heroClass : HeroClass.values()) {
            talents(heroClass, false, 0);
            assertFalse(hero.hasTalent(Talent.WIDE_SEARCH));
            assertEquals(heroClass.toString(), heroClass == HeroClass.ROGUE,
                    SearchControlContext.hasMeaningfulWall(hero, level));
        }
        clearTerrain();
        candidate(3, 0);
        talents(HeroClass.ROGUE, false, 0);
        assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
    }

    @Test public void acquiredRogueWideSearchRetainsRoundedAndSquareSevenBySeven() {
        talents(HeroClass.ROGUE, false, 1);
        candidate(3, 0);
        assertTrue(SearchControlContext.hasMeaningfulWall(hero, level));
        clearTerrain();
        candidate(3, 3);
        assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
        hero.upgradeTalent(Talent.WIDE_SEARCH);
        assertTrue(SearchControlContext.hasMeaningfulWall(hero, level));
        clearTerrain();
        candidate(4, 0);
        assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
    }

    @Test public void actualMetamorphosisTalentsExpandOtherClassesOnlyToFiveByFive() {
        for (HeroClass heroClass : HeroClass.values()) {
            if (heroClass == HeroClass.ROGUE) continue;
            clearTerrain();
            candidate(2, 0);
            talents(heroClass, true, 0);
            assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
            hero.upgradeTalent(Talent.WIDE_SEARCH);
            assertSame(heroClass, hero.heroClass);
            assertTrue(SearchControlContext.hasMeaningfulWall(hero, level));
            clearTerrain();
            candidate(2, 2);
            assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
            hero.upgradeTalent(Talent.WIDE_SEARCH);
            assertTrue(SearchControlContext.hasMeaningfulWall(hero, level));
            clearTerrain();
            candidate(3, 0);
            assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
        }
    }

    @Test public void unseenBackingAndSecretFlagsCannotLeakIntoContextPreference() {
        int wall = candidate(1, 0), backing = wall + 1;
        level.heroFOV[backing] = level.visited[backing] = level.mapped[backing] = false;
        for (int tile = 0; tile < Terrain.flags.length; tile++) {
            level.map[backing] = tile;
            for (boolean secret : new boolean[]{false, true}) {
                level.secret[wall] = level.secret[backing] = secret;
                assertEquals("concealed backing " + tile, Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
            }
        }
        level.map[wall] = Terrain.SECRET_DOOR;
        assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
    }

    @Test public void unseenUnsupportedAndDoorlessWallsDoNotTakePriority() {
        int wall = candidate(1, 0);
        level.heroFOV[wall] = false;
        assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
        level.heroFOV[wall] = true;
        level.heroFOV[wall - SIZE] = level.visited[wall - SIZE] = false;
        assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
        level.heroFOV[wall - SIZE] = true;
        level.map[wall - SIZE] = Terrain.EMPTY;
        assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
        clearTerrain();
        candidate(1, 0);
        for (int depth : new int[]{1, 10, 15, 20, 25, 26}) {
            Dungeon.depth = depth;
            assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
        }
    }

    @Test public void bothActualCrystalRoomTypesApplyOnlyWhileHeroIsInside() {
        candidate(1, 0);
        for (Room room : new Room[]{new CrystalVaultRoom(), new CrystalChoiceRoom()}) {
            room.set(7, 7, 13, 13);
            level.setRooms(room);
            assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
            Heap chest = new Heap();
            chest.type = Heap.Type.CRYSTAL_CHEST;
            level.heaps.put(hero.pos + 1, chest);
            assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
            chest.type = Heap.Type.HEAP;
            level.heaps.clear();
            assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
            room.set(2, 2, 7, 7);
            assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
        }
        level.setRooms();
        assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
    }

    @Test public void movementFirstWhenWallsAreIrrelevantStillKeepsBothSearchChoices() throws Exception {
        HeroAction.Move movement = new HeroAction.Move(hero.pos + 2);
        hero.lastAction = movement;
        assertArrayEquals(new Action[]{Action.RESUME_MOVEMENT, Action.SEARCH, Action.WALL_SEARCH, Action.SHOW_SAVED, Action.FORGET, Action.CANCEL},
                SearchControlContext.choices(hero, level));
        candidate(1, 0);
        assertArrayEquals(new Action[]{Action.WALL_SEARCH, Action.SEARCH, Action.SHOW_SAVED, Action.RESUME_MOVEMENT, Action.FORGET, Action.CANCEL},
                SearchControlContext.choices(hero, level));
        String saved = memory();
        select(Action.RESUME_MOVEMENT);
        assertSame(movement, hero.curAction);
        assertNull(hero.lastAction);
        assertEquals(1, hero.nextCalls);
        assertEquals(saved, memory());
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void contextRecomputesWithoutMutatingTimeCommandsMemoryMapOrRandomness() {
        candidate(2, 0);
        String saved = memory();
        int[] map = level.map.clone();
        boolean[] fov = level.heroFOV.clone(), visited = level.visited.clone(), mapped = level.mapped.clone();
        Random.pushGenerator(445);
        long expected = Random.Long();
        Random.popGenerator();
        Random.pushGenerator(445);
        try {
            for (int i = 0; i < 100; i++) {
                assertEquals(Action.SEARCH, SearchControlContext.choices(hero, level)[0]);
                hero.pos++;
                assertEquals(Action.WALL_SEARCH, SearchControlContext.choices(hero, level)[0]);
                hero.pos--;
            }
            assertEquals(expected, Random.Long());
        } finally { Random.popGenerator(); }
        talents(HeroClass.WARRIOR, true, 1);
        assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
        assertEquals(saved, memory());
        assertArrayEquals(map, level.map);
        assertArrayEquals(fov, level.heroFOV);
        assertArrayEquals(visited, level.visited);
        assertArrayEquals(mapped, level.mapped);
        assertNull(hero.curAction);
        assertNull(hero.lastAction);
        assertEquals(0, hero.nextCalls);
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void interruptedWallWalkAndRestoredMemoryNeverAutoResume() {
        int wall = candidate(1, 0);
        assertTrue(hero.wallSearch.select(hero, wall));
        hero.curAction = new WallSearchSession.Walk(hero.pos, level);
        hero.interrupt();
        String saved = memory();
        Bundle bundle = new Bundle();
        hero.wallSearch.store(bundle);
        hero.wallSearch.clear();
        hero.wallSearch.restore(bundle);
        hero.pos = at(3, 3);
        assertEquals(Action.SEARCH, SearchControlContext.choices(hero, level)[0]);
        assertEquals(saved, memory());
        assertNull(hero.curAction);
        assertNull(hero.lastAction);
        assertEquals(0, hero.nextCalls);
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void explicitOrdinaryChoiceUsesRealSearchAtItsUnchangedTwoTurnCost() throws Exception {
        HeroAction.Move movement = new HeroAction.Move(hero.pos + 3);
        hero.lastAction = movement;
        select(Action.SEARCH);
        assertEquals(2f, hero.cooldown(), 0f);
        assertEquals(1, hero.nextCalls);
        assertEquals(2, hero.wallSearch.searches());
        assertSame(movement, hero.lastAction);
        assertNull(hero.curAction);
    }

    @Test public void explicitWallChoiceRetainsRealPaidSearchAndCompletionProof() throws Exception {
        int wall = candidate(1, 0);
        level.locked = false;
        select(Action.WALL_SEARCH);
        assertEquals(2f, hero.cooldown(), 0f);
        assertEquals(1, hero.nextCalls);
        assertEquals(2, hero.wallSearch.searches());
        assertTrue(level.searchedWalls.confirmed(level, wall));
        assertEquals(Action.SEARCH, SearchControlContext.preferred(hero, level));
    }

    @Test public void explicitWallChoiceStillHonorsExistingDangerAndMovementGuards() throws Exception {
        candidate(1, 0);
        String saved = memory();
        select(Action.WALL_SEARCH); // locked floor
        level.locked = false;
        hero.rooted = true;
        select(Action.WALL_SEARCH);
        hero.rooted = false;
        hero.paralysed = 1;
        select(Action.WALL_SEARCH);
        hero.paralysed = 0;
        ArrayList<Mob> enemies = new ArrayList<>();
        enemies.add(new Rat());
        field(Hero.class, "visibleEnemies").set(hero, enemies);
        assertEquals(1, hero.visibleEnemies());
        select(Action.WALL_SEARCH);
        assertEquals(saved, memory());
        assertNull(hero.curAction);
        assertEquals(0, hero.nextCalls);
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void staleBusyAndDeadChooserSelectionsCannotActOrForget() throws Exception {
        String saved = memory();
        for (Action action : Action.values()) {
            hero.ready = false;
            select(action);
            hero.ready = true;
            hero.HP = 0;
            select(action);
            hero.HP = 20;
            Dungeon.hero = new Hero();
            select(action);
            Dungeon.hero = hero;
            Dungeon.level = new GrassDewPickupTest.TestLevel();
            select(action);
            Dungeon.level = level;
        }
        assertEquals(saved, memory());
        assertNull(hero.curAction);
        assertEquals(0, hero.nextCalls);
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void forgetAndCancelRequireTheirOwnExplicitSelection() throws Exception {
        String saved = memory();
        SearchControlContext.choices(hero, level);
        select(Action.CANCEL);
        assertEquals(saved, memory());
        select(Action.FORGET);
        assertFalse(hero.wallSearch.exists());
        assertEquals(0, hero.nextCalls);
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void actualSearchFromInsideKeyedRoomDoesNotCreateCheckmarks() throws Exception {
        Object[][] gated = {
                {new ArmoryRoom(), Room.Door.Type.LOCKED},
                {new TreasuryRoom(), Room.Door.Type.LOCKED},
                {new CrystalVaultRoom(), Room.Door.Type.LOCKED},
                {new StorageRoom(), Room.Door.Type.BARRICADE},
                {new StorageRoom(), Room.Door.Type.CRYSTAL}
        };
        for (Object[] pair : gated) {
            clearTerrain();
            int wall = candidate(1, 0);
            SpecialRoom room = (SpecialRoom) pair[0];
            room.set(7, 7, 13, 13);
            Room.Door entrance = new Room.Door(7, 10);
            entrance.set((Room.Door.Type) pair[1]);
            room.connected.put(new EmptyRoom(), entrance);
            level.setRooms(room);
            assertSame(room, level.room(hero.pos));
            assertTrue(DoorSearchEligibility.candidate(level, wall));
            assertTrue(DoorSearchEligibility.insideGatedRoom(level, hero.pos));
            // A previous build may already have stored this mark in a saved game.
            assertTrue(level.searchedWalls.mark(level, wall));
            assertEquals("Previously saved checkmark must also be invisible inside a gated room",
                    -1, SearchedWallMarkers.visual(level, wall));
            level.searchedWalls.clear();
            int before = level.map[wall];
            select(Action.SEARCH); // Actual ordinary paid search, not a mocked marker call.
            assertEquals("No checkmark after searching inside " + room.getClass().getSimpleName(),
                    false, level.searchedWalls.attempted(wall));
            assertEquals(before, level.map[wall]);
            assertEquals("Ordinary Search keeps its exact two-turn cost",
                    2f, hero.cooldown(), 0f);
        }
        // Locking an ordinary room is not a license to suppress its wall markers.
        clearTerrain();
        int ordinaryWall = candidate(1, 0);
        EmptyRoom ordinary = new EmptyRoom();
        ordinary.set(7, 7, 13, 13);
        level.setRooms(ordinary);
        assertFalse(DoorSearchEligibility.insideGatedRoom(level, hero.pos));
        select(Action.SEARCH);
        assertTrue(level.searchedWalls.attempted(ordinaryWall));
    }

    @Test public void existingForesightFootprintIsPreservedWithoutRevealingUnknownWalls() {
        candidate(8, 0);
        assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
        Buff.affect(hero, Foresight.class);
        assertTrue(SearchControlContext.hasMeaningfulWall(hero, level));
        Arrays.fill(level.heroFOV, false);
        assertTrue("Foresight can cover remembered terrain", SearchControlContext.hasMeaningfulWall(hero, level));
        Arrays.fill(level.visited, false);
        Arrays.fill(level.mapped, false); // Attaching actual Foresight already maps its current footprint.
        assertFalse("unknown terrain is not a presented candidate", SearchControlContext.hasMeaningfulWall(hero, level));
    }

    @Test public void actualResumeIndicatorRetainsDirectMovementWithoutSavedWallMemory() throws Exception {
        hero.wallSearch.clear();
        HeroAction.Move movement = new HeroAction.Move(hero.pos + 2);
        hero.lastAction = movement;
        ResumeIndicator indicator = allocate(ResumeIndicator.class);
        Method click = ResumeIndicator.class.getDeclaredMethod("onClick");
        click.setAccessible(true);
        hero.ready = false;
        click.invoke(indicator);
        assertSame(movement, hero.lastAction);
        hero.ready = true;
        click.invoke(indicator);
        assertSame(movement, hero.curAction);
        assertNull(hero.lastAction);
        assertEquals(1, hero.nextCalls);
        assertEquals(0f, hero.cooldown(), 0f);
    }

    @Test public void previousFloorOrBranchMemoryNeverPrioritizesAnUnavailableWallResume() throws Exception {
        String saved = memory();
        candidate(1, 0);
        assertEquals(Action.WALL_SEARCH, SearchControlContext.preferred(hero, level));
        for (boolean otherBranch : new boolean[]{false, true}) {
            Dungeon.depth = otherBranch ? 3 : 4;
            Dungeon.branch = otherBranch ? 1 : 0;
            assertTrue("a current wall still exists", SearchControlContext.hasMeaningfulWall(hero, level));
            assertFalse(hero.wallSearch.matches(level));
            assertArrayEquals(new Action[]{Action.SEARCH, Action.WALL_SEARCH, Action.SHOW_SAVED, Action.FORGET, Action.CANCEL},
                    SearchControlContext.choices(hero, level));
            HeroAction.Move movement = new HeroAction.Move(hero.pos + 2);
            hero.lastAction = movement;
            assertEquals(Action.RESUME_MOVEMENT, SearchControlContext.preferred(hero, level));
            select(Action.WALL_SEARCH);
            assertSame(movement, hero.lastAction);
            hero.lastAction = null;
            assertEquals(saved, memory());
            assertEquals(0, hero.nextCalls);
            assertEquals(0f, hero.cooldown(), 0f);
        }
    }

    @Test public void invalidContextAndGuardedWindowOpenNeverConstructAnAction() {
        assertFalse(SearchControlContext.hasMeaningfulWall(null, level));
        assertFalse(SearchControlContext.hasMeaningfulWall(hero, null));
        hero.pos = -1;
        assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
        hero.pos = level.length();
        assertFalse(SearchControlContext.hasMeaningfulWall(hero, level));
        hero.pos = at(10, 10);
        hero.ready = false;
        WndWallSearchResume.show();
        hero.ready = true;
        hero.HP = 0;
        WndWallSearchResume.show();
        assertNull(hero.curAction);
        assertEquals(0, hero.nextCalls);
        assertEquals(0f, hero.cooldown(), 0f);
    }
}
