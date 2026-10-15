package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.watabou.noosa.*;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.*;
import com.watabou.utils.Random;
import org.json.*;
import org.junit.*;
import org.mockito.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Fixed commands in the production Actor.process scheduler. No action policy or combat model. */
public class GetFuckedRouteTest {
    @Rule public org.junit.rules.Timeout deadline = org.junit.rules.Timeout.seconds(45);
    private final List<MockedStatic<?>> visuals = new ArrayList<>();
    private CellSelector.Listener selection;
    private AtomicBoolean watch;
    private Thread watchdog;
    private boolean oldKeepAlive;
    private boolean pendingTransition;
    private com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator oldTargetIndicator;

    private void bind(Char ch) {
        if (ch.sprite != null) return;
        CharSprite sprite = ch == Dungeon.hero ? mock(HeroSprite.class, RETURNS_DEEP_STUBS)
                : mock(CharSprite.class, RETURNS_DEEP_STUBS);
        ch.sprite = sprite;
        Group parent=mock(Group.class,i -> {
            if(i.getMethod().getName().equals("recycle") && i.getArguments().length==1 && i.getArgument(0) instanceof Class)
                return mock((Class<?>)i.getArgument(0),RETURNS_DEEP_STUBS);
            return Answers.RETURNS_DEEP_STUBS.answer(i);
        }); sprite.parent=parent;
        MissileSprite missile=mock(MissileSprite.class);
        when(parent.recycle(MissileSprite.class)).thenReturn(missile);
        doAnswer(i -> {((Callback)i.getArgument(3)).call();return null;}).when(missile).reset(any(Visual.class),any(Visual.class),any(Item.class),any(Callback.class));
        doAnswer(i -> {((Callback)i.getArgument(3)).call();return null;}).when(missile).reset(any(Visual.class),anyInt(),any(Item.class),any(Callback.class));
        when(sprite.center()).thenReturn(new PointF());
        doAnswer(i -> { ch.onAttackComplete(); return null; }).when(sprite).attack(anyInt());
        doAnswer(i -> { ch.onOperateComplete(); return null; }).when(sprite).operate(anyInt());
        doAnswer(i -> { ch.onMotionComplete(); return null; }).when(sprite).move(anyInt(), anyInt());
    }
    private void heapSprite(Heap h) {
        if(h.sprite==null){h.sprite=mock(ItemSprite.class,RETURNS_DEEP_STUBS);when(h.sprite.view(any(Heap.class))).thenReturn(h.sprite);}
    }
    private <T> MockedStatic<T> visual(Class<T> type) {
        MockedStatic<T> m = mockStatic(type); visuals.add(m); return m;
    }
    @Before public void init() throws Exception {
        System.out.println("RUNNER_STAGE initialize");
        HeadlessTestSupport.start();
        MockedStatic<GameScene> scene = mockStatic(GameScene.class, invocation -> {
            Object[] args = invocation.getArguments();
            if (invocation.getMethod().getName().equals("selectCell")) {
                selection = (CellSelector.Listener) args[0];
            } else if (invocation.getMethod().getName().equals("add") && args.length > 0) {
                if (args[0] instanceof Mob) {
                    Mob mob = (Mob)args[0];
                    float delay = args.length == 2 ? ((Number)args[1]).floatValue() : 0;
                    Dungeon.level.mobs.add(mob); bind(mob);
                    Actor.addDelayed(mob, (float)Math.ceil(Actor.now()+delay)-Actor.now());
                    mob.spendToWhole();
                } else if (args[0] instanceof Blob) Actor.add((Blob)args[0]);
                else if (args[0] instanceof Heap) heapSprite((Heap)args[0]);
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
        visuals.add(scene);
        MockedStatic<Game> game=visual(Game.class);
        game.when(() -> Game.switchScene(InterlevelScene.class)).thenAnswer(i -> { pendingTransition=true; return null; });
        oldTargetIndicator=com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance;
        com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance=mock(com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.class,CALLS_REAL_METHODS);
        MockedStatic<com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator> attackIndicator=visual(com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator.class);
        attackIndicator.when(() -> com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator.target(any()))
                .thenAnswer(i -> {com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton.target(i.getArgument(0));return null;});
        visual(Enchanting.class);
        visual(SpellSprite.class);
        MockedStatic<CellEmitter> cells = visual(CellEmitter.class);
        Emitter emitter = mock(Emitter.class);
        cells.when(() -> CellEmitter.get(anyInt())).thenReturn(emitter);
        cells.when(() -> CellEmitter.center(anyInt())).thenReturn(emitter);
        cells.when(() -> CellEmitter.bottom(anyInt())).thenReturn(emitter);
        cells.when(() -> CellEmitter.floor(anyInt())).thenReturn(emitter);
        MockedStatic<MagicMissile> missiles = mockStatic(MagicMissile.class, i -> {
            Object[] args = i.getArguments();
            if (i.getMethod().getName().equals("boltFromChar")) ((Callback)args[args.length-1]).call();
            return null;
        });
        visuals.add(missiles);
        System.out.println("RUNNER_STAGE visuals bound");
        Method context = LevelGenerationHarness.class.getDeclaredMethod("applyContext", LevelGenerationHarness.Context.class);
        context.setAccessible(true); context.invoke(null, LevelGenerationHarness.Context.REFERENCE);
        SPDSettings.challenges(Challenges.GET_FUCKED);
        Dungeon.daily = Dungeon.dailyReplay = false;
        Dungeon.customSeedText = ""; Dungeon.seed = 266L;
        GamesInProgress.selectedClass = HeroClass.CLERIC;
        GamesInProgress.randomizedClass = false; GamesInProgress.curSlot = 1;
        Dungeon.init();
        System.out.println("RUNNER_STAGE actual Cleric created");
        Random.pushGenerator(266L); //init resets the RNG stack; seed once after it, never during the route.
        Dungeon.depth = 1; Dungeon.branch = 0; Dungeon.level = Dungeon.newLevel();
        System.out.println("RUNNER_STAGE actual level generated");
        bind(Dungeon.hero);
        for (Mob m : Dungeon.level.mobs) bind(m);
        for (Heap h : Dungeon.level.heaps.valueList()) heapSprite(h);
        Dungeon.switchLevel(Dungeon.level, -1);
        System.out.println("RUNNER_STAGE actual level entered");
        oldKeepAlive = Actor.keepActorThreadAlive;
        Actor.keepActorThreadAlive = false;
        //Actor.process has a public stop flag, but waits once when an animation/input pauses it.
        //Release only that wait. The production scheduler still selects and acts every actor.
        Thread test = Thread.currentThread(); watch = new AtomicBoolean(true);
        watchdog = new Thread(() -> {
            while (watch.get()) {
                if (test.getState() == Thread.State.WAITING) synchronized(test) { test.notifyAll(); }
                try { Thread.sleep(1); } catch (InterruptedException e) { return; }
            }
        });
        watchdog.setDaemon(true); watchdog.start();
    }
    @After public void close() throws Exception {
        if (watch != null) { watch.set(false); watchdog.interrupt(); watchdog.join(1000); }
        Actor.keepActorThreadAlive = oldKeepAlive;
        com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance=oldTargetIndicator;
        Actor.clear(); Random.popGenerator();
        Collections.reverse(visuals); for (MockedStatic<?> m : visuals) m.close();
    }
    private void ready() {
        for (int steps=0; steps<10000; steps++) {
            if(pendingTransition){
                pendingTransition=false;
                try {
                    Actor.fixTime();
                    InterlevelScene scene=mock(InterlevelScene.class,CALLS_REAL_METHODS);
                    Method transition=InterlevelScene.class.getDeclaredMethod(InterlevelScene.mode==InterlevelScene.Mode.DESCEND ? "descend" : "ascend");
                    transition.setAccessible(true);transition.invoke(scene);
                    bind(Dungeon.hero);for(Mob m:Dungeon.level.mobs)bind(m);
                    for(Heap h:Dungeon.level.heaps.valueList())heapSprite(h);
                }catch(Exception e){throw new RuntimeException(e);}
            }
            if (!Dungeon.hero.isAlive()) throw new AssertionError("Terminal hero state: "+state("DEATH"));
            if (Dungeon.hero.ready && !Actor.processing()) return;
            if (Dungeon.hero.ready && Actor.processing()) return;
            Actor.process();
        }
        throw new AssertionError("No input boundary: "+state("STALL"));
    }
    private int charge(HolyTome t) throws Exception {
        Field f = Artifact.class.getDeclaredField("charge"); f.setAccessible(true); return f.getInt(t);
    }
    private void snapshot(String label) throws Exception {
        if(Boolean.getBoolean("route.noSnapshots")) return;
        Bundle b=new Bundle();
        b.put("hero",Dungeon.hero); b.put("level",Dungeon.level);
        Bundle gen=new Bundle(); Generator.storeInBundle(gen); b.put("generator",gen);
        Bundle stats=new Bundle(); Statistics.storeInBundle(stats); b.put("statistics",stats);
        java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
        Bundle.write(b,out,false);
        System.out.println("SNAPSHOT266 "+label+" "+Base64.getEncoder().encodeToString(out.toByteArray()));
        Field generators=Random.class.getDeclaredField("generators"); generators.setAccessible(true);
        out=new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(out).writeObject(generators.get(null));
        System.out.println("RNG266 "+label+" "+Base64.getEncoder().encodeToString(out.toByteArray()));
    }
    private JSONObject state(String command) {
        Hero h=Dungeon.hero;
        JSONObject j=new JSONObject();
        j.put("command",command).put("time",Actor.now()).put("depth",Dungeon.depth).put("pos",h.pos)
                .put("HP",h.HP).put("HT",h.HT).put("shield",h.shielding()).put("level",h.lvl)
                .put("exp",h.exp).put("strength",h.STR()).put("hunger",h.buff(Hunger.class)==null?JSONObject.NULL:h.buff(Hunger.class).hunger());
        JSONArray inventory=new JSONArray();
        for(Item item:h.belongings) inventory.put(item.getClass().getSimpleName()+":"+item.quantity()+":+"+item.level());
        j.put("inventory",inventory);
        try {j.put("tomeCharge",charge(h.belongings.getItem(HolyTome.class)));}catch(Exception e){throw new RuntimeException(e);}
        JSONArray mobs=new JSONArray();
        for(Mob m:Dungeon.level.mobs) mobs.put(new JSONObject().put("class",m.getClass().getSimpleName()).put("id",m.id())
                .put("pos",m.pos).put("HP",m.HP).put("state",m.state.getClass().getSimpleName()).put("visible",Dungeon.level.heroFOV[m.pos])
                .put("magicCollision",new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(h.pos,m.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT).collisionPos)
                .put("projectileCollision",new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(h.pos,m.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos));
        j.put("mobs",mobs);
        JSONArray heaps=new JSONArray();
        for(Heap heap:Dungeon.level.heaps.valueList()){
            JSONArray items=new JSONArray();for(Item item:heap.items)items.put(item.getClass().getSimpleName()+":"+item.quantity()+":+"+item.level());
            heaps.put(new JSONObject().put("pos",heap.pos).put("type",heap.type).put("items",items));
        }
        j.put("heaps",heaps);
        return j;
    }
    private void log(String s) { System.out.println("ROUTE "+state(s)); }
    private void waitOne() { Dungeon.hero.rest(false); ready(); log("WAIT"); }
    private void click(int cell) { assertTrue(Dungeon.hero.handle(cell)); Dungeon.hero.next(); Dungeon.hero.busy(); ready(); log("CLICK "+cell); }
    private void cast(ClericSpell spell, Integer target) {
        Hero h=Dungeon.hero; HolyTome t=h.belongings.getItem(HolyTome.class);
        assertTrue(t.canCast(h,spell));
        if(target!=null) assertTrue("Target must be in actual FOV",Dungeon.level.heroFOV[target]);
        selection=null; spell.onCast(t,h);
        if(target!=null){assertNotNull(selection);selection.onSelect(target);}
        ready(); log("CAST "+spell.getClass().getSimpleName()+" "+target);
    }
    private void throwItem(Class<? extends Item> type,int target) {
        Item item=Dungeon.hero.belongings.getItem(type); assertNotNull(item);
        assertTrue(Dungeon.level.heroFOV[target]);
        item.cast(Dungeon.hero,target); ready(); log("THROW "+type.getSimpleName()+" "+target);
    }
    private void throwLitBomb(int target) {
        Item bomb=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.class);
        assertNotNull(bomb);selection=null;bomb.execute(Dungeon.hero,"LIGHTTHROW");
        assertNotNull(selection);selection.onSelect(target);ready();log("LIGHTTHROW Bomb "+target);
    }
    @Test public void seed266InitialCommands() throws Exception {
        System.out.println("RUNNER_STAGE scheduler start");
        ready();
        assertEquals(1,Dungeon.hero.lvl); assertEquals(1,Dungeon.hero.HP); assertEquals(1,Dungeon.hero.HT);
        for(Map<Talent,Integer> tier:Dungeon.hero.talents) for(int value:tier.values()) assertEquals(0,value);
        JSONObject layout=new JSONObject(); Level l=Dungeon.level;
        layout.put("width",l.width()).put("height",l.height()).put("entrance",l.entrance()).put("exit",l.exit())
                .put("terrain",new JSONArray(l.map));
        JSONArray heaps=new JSONArray();
        for(Heap hp:l.heaps.valueList()){
            JSONArray its=new JSONArray();for(Item item:hp.items)its.put(item.getClass().getSimpleName()+":"+item.quantity()+":+"+item.level()+":cursed="+item.cursed);
            heaps.put(new JSONObject().put("pos",hp.pos).put("type",hp.type.toString()).put("items",its));
        }
        layout.put("heaps",heaps);
        JSONArray traps=new JSONArray();for(com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap trap:l.traps.valueList())
            traps.put(new JSONObject().put("class",trap.getClass().getSimpleName()).put("pos",trap.pos).put("visible",trap.visible));
        layout.put("traps",traps);
        System.out.println("LAYOUT266 "+layout); log("START"); snapshot("START");
        waitOne(); waitOne();
        snapshot("AFTER_TWO_WAITS");
        //Literal exploratory prefix. No path planner, policy, free item collection or resource injection.
        for(int cell:new int[]{905,906,907,908,909,910,911,912,951,952,953,954,955,956,957,958,
                959,960,999,1038,1077,1078,1079,1080,1119}) click(cell);
        snapshot("KEY_ROOM_PREFIX");
        for(int cell:new int[]{1080,1079,1078,1077,1038,999,960,959,958,957,956,955,954,953,
                952,951,912,911,910,909,908,868,830,831,832,833,834,834,795,796,797,758,719}) click(cell);
        assertNotNull("Mail must actually have been picked up",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor.class));
        snapshot("MAIL_ACQUIRED");
        click(757); //Take the actual bomb in the same room.
        for(int cell:new int[]{796,834,833,832,831,830,868,908,909,910,911,912,951,952,953,954,
                955,956,957,958,959,960,999,1038,1077,1078,1079,1080,1081,1082}) click(cell);
        snapshot("FIRST_RAT_APPROACH");
        cast(GuidingLight.INSTANCE,965);
        snapshot("FIRST_RAT_FIRST_LIGHT");
        for(int cell:new int[]{1044,1006,967,968,1007,1007,969,970,971,932,893,854,815,776,737,736,735,734,
                695,656,655,654,653}) click(cell);
        snapshot("FIRST_SNAKE_APPROACH");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,649);
        snapshot("FIRST_SNAKE_KILLED");
        for(int cell:new int[]{652,651,650,649,610,571,532,493,454,453,452}) click(cell);
        for(int n=0;n<10;n++) waitOne();
        cast(HolyWard.INSTANCE,null);
        click(451);
        snapshot("UPPER_RAT_APPROACH");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,410);
        snapshot("UPPER_RAT_FIRST_SPEAR");
        if(Boolean.getBoolean("route.badApproach")) {
            for(int cell:new int[]{450,449,410,372,333,332,331,330,329}) click(cell);
            return;
        }
        click(450);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,325);
        for(int cell:new int[]{449,410,372}) click(cell);
        cast(GuidingLight.INSTANCE,442);
        click(333);
        waitOne(); waitOne();
        snapshot("SECOND_SNAKE_APPROACH");
        cast(GuidingLight.INSTANCE,406);
        snapshot("CENTER_RAT_HIT");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,407);
        for(int cell:new int[]{371,409,408,407,367,327,326,325,364,403,442,441}) click(cell);
        for(int n=0;n<45;n++)waitOne();
        snapshot("LEFT_DOOR_APPROACH");
        click(440);
        snapshot("LEFT_RATS_VISIBLE");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,476);
        snapshot("LEFT_RAT_FIRST_HIT");
        throwLitBomb(439);
        click(441);click(442);
        snapshot("LEFT_RATS_BOMB_RESOLVED");
        click(441);
        //Explicit hand-written branches for the observed bomb variants. No route search or combat policy.
        if(Dungeon.hero.exp==8){
            if(Actor.findChar(439)!=null){
                cast(GuidingLight.INSTANCE,439);
                if(Actor.findChar(440)!=null)throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,440);
            }else{
                for(int cell:new int[]{480,519,558,597,596,595,594,555})click(cell);
                assertNotNull("Uninvestigated lower-rat timing",Actor.findChar(515));
                throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,515);
                assertEquals("Lower rat did not die",9,Dungeon.hero.exp);
                for(int cell:new int[]{594,595,596,597,558,519,480,441})click(cell);
            }
        }
        assertEquals(9,Dungeon.hero.exp);
        for(int cell:new int[]{440,440}) click(cell);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,277);
        if(Dungeon.hero.lvl==1){
            for(int cell:new int[]{439,400})click(cell);
            assertNotNull("Uninvestigated bridge-rat timing",Actor.findChar(318));
            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,318);
        }
        assertEquals(2,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));
        Dungeon.hero.upgradeTalent(Talent.SHIELD_OF_LIGHT);log("UPGRADE SHIELD_OF_LIGHT");
        for(int cell:new int[]{439,439,400,361,322,321,282,281,242,241,240,239,277,
                239,240,241,242,281,282,321,322,361,400,439,440,441,442,403,364,325,326,327,328,329,330,331,332,333,372,410,410,449,450,451,452,453,454,493,494,495})click(cell);
        assertEquals(2,Dungeon.depth);
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        log("FLOOR_TWO_ENTERED");
        for(int cell:new int[]{1333,1375,1417,1459,1458,1457,1456,1455,1454,1453,1452,1451,1450,1492,1534,1576,1575,1574,1573,1531,1489,1447})click(cell);
        log("STRENGTH_ACQUIRED");
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK STRENGTH");
        click(1405);
        cast(GuidingLight.INSTANCE,1359);
        log("SECOND_FLOOR_RAT_HIT");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,1360);
        for(int cell:new int[]{1363,1321,1320,1319,1318,1317,1316,1315,1357,1399,1441,1483})click(cell);
        log("UPGRADE_AND_KEY_ACQUIRED");
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class).execute(Dungeon.hero,"EAT");ready();log("EAT FIRST RATION");
        for(int cell:new int[]{1441,1399,1357,1358,1359,1360})click(cell);
        log("RECOVER SECOND FLOOR SPEAR");
        for(int cell:new int[]{1317,1316,1315,1273,1231,1189,1147,1105,1104,1062,1020,978})click(cell);
        log("GNOLL_APPROACH");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,848);
        log("UPPER_ROOM_FIRST_SHOT");
        assertEquals(1,Dungeon.hero.HP);
    }
}
