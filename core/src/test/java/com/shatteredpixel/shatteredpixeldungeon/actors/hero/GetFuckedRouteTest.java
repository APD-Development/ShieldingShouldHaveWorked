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

    private void bind(Char ch) {
        if (ch.sprite != null) return;
        CharSprite sprite = ch == Dungeon.hero ? mock(HeroSprite.class, RETURNS_DEEP_STUBS)
                : mock(CharSprite.class, RETURNS_DEEP_STUBS);
        ch.sprite = sprite;
        when(sprite.center()).thenReturn(new PointF());
        doAnswer(i -> { ch.onAttackComplete(); return null; }).when(sprite).attack(anyInt());
        doAnswer(i -> { ch.onOperateComplete(); return null; }).when(sprite).operate(anyInt());
        doAnswer(i -> { ch.onMotionComplete(); return null; }).when(sprite).move(anyInt(), anyInt());
    }
    private void heapSprite(Heap h) { if (h.sprite == null) h.sprite = mock(ItemSprite.class, RETURNS_SELF); }
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
        visual(Game.class); //Render-thread scheduling, scene changes and draw access only.
        visual(com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator.class);
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
        Actor.clear(); Random.popGenerator();
        Collections.reverse(visuals); for (MockedStatic<?> m : visuals) m.close();
    }
    private void ready() {
        for (int steps=0; steps<10000; steps++) {
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
                .put("exp",h.exp).put("strength",h.STR()).put("hunger",h.buff(Hunger.class).hunger());
        JSONArray inventory=new JSONArray();
        for(Item item:h.belongings) inventory.put(item.getClass().getSimpleName()+":"+item.quantity()+":+"+item.level());
        j.put("inventory",inventory);
        try {j.put("tomeCharge",charge(h.belongings.getItem(HolyTome.class)));}catch(Exception e){throw new RuntimeException(e);}
        JSONArray mobs=new JSONArray();
        for(Mob m:Dungeon.level.mobs) mobs.put(new JSONObject().put("class",m.getClass().getSimpleName()).put("id",m.id())
                .put("pos",m.pos).put("HP",m.HP).put("state",m.state.getClass().getSimpleName()).put("visible",Dungeon.level.heroFOV[m.pos]));
        j.put("mobs",mobs);
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
        assertEquals(1,Dungeon.hero.HP);
    }
}
