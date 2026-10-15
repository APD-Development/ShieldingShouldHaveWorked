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
    private com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector itemSelection;
    private MockedConstruction<com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade> upgradeWindows;
    private Object upgradeButton;
    private AtomicBoolean watch;
    private Thread watchdog;
    private boolean oldKeepAlive;
    private boolean pendingTransition;
    private boolean pendingAlchemy;
    private com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator oldTargetIndicator;

    private void bind(Char ch) {
        if (ch.sprite != null) return;
        CharSprite sprite = ch == Dungeon.hero ? mock(HeroSprite.class, RETURNS_DEEP_STUBS)
                : mock(CharSprite.class, RETURNS_DEEP_STUBS);
        ch.sprite = sprite;
        if(ch==Dungeon.hero)doAnswer(i -> {ch.onOperateComplete();return null;}).when((HeroSprite)sprite).read();
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
            if(invocation.getMethod().getName().equals("selectItem")){
                itemSelection=(com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector)args[0];
            }else if (invocation.getMethod().getName().equals("selectCell")) {
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
        upgradeWindows=mockConstruction(com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade.class,(window,contextArgs)->{
            Class<?> buttonClass=Class.forName("com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade$1");
            upgradeButton=mock(buttonClass,CALLS_REAL_METHODS);
            String[] fields={"this$0","val$upgrader","val$toUpgrade","val$force"};
            Object[] values={window,contextArgs.arguments().get(0),contextArgs.arguments().get(1),contextArgs.arguments().get(2)};
            for(int n=0;n<fields.length;n++){Field f=buttonClass.getDeclaredField(fields[n]);f.setAccessible(true);f.set(upgradeButton,values[n]);}
        });
        MockedStatic<Game> game=visual(Game.class);
        game.when(() -> Game.switchScene(InterlevelScene.class)).thenAnswer(i -> { pendingTransition=true; return null; });
        game.when(() -> Game.switchScene(AlchemyScene.class)).thenAnswer(i -> {pendingAlchemy=true;return null;});
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
        String resume=System.getProperty("route.resume");
        if(resume==null){
            Dungeon.init();
            Random.pushGenerator(266L);
            Dungeon.depth=1;Dungeon.branch=0;Dungeon.level=Dungeon.newLevel();
            bind(Dungeon.hero);for(Mob m:Dungeon.level.mobs)bind(m);
            for(Heap h:Dungeon.level.heaps.valueList())heapSprite(h);
            Dungeon.switchLevel(Dungeon.level,-1);
        }else{
            restoreCheckpoint(resume);
        }
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
        if(upgradeWindows!=null)upgradeWindows.close();
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
            if(Boolean.getBoolean("route.audit") && Dungeon.depth==2 && Actor.now()>=63){
                try{
                    Field f=Actor.class.getDeclaredField("all");f.setAccessible(true);
                    Field priority=Actor.class.getDeclaredField("actPriority");priority.setAccessible(true);
                    Actor selected=null;float earliest=Float.MAX_VALUE;
                    for(Actor a:(Set<Actor>)f.get(null)){
                        float t=Actor.now()+a.cooldown();
                        if(t<earliest || t==earliest && (selected==null || priority.getInt(a)>priority.getInt(selected))){selected=a;earliest=t;}
                    }
                    System.out.println("SCHEDULE "+Actor.now()+" next="+selected.getClass().getSimpleName()+":"+selected.apdPeekId()+" at="+earliest+" hero="+Dungeon.hero.pos+" heroTime="+(Actor.now()+Dungeon.hero.cooldown())+" action="+Dungeon.hero.curAction);
                }catch(Exception e){throw new RuntimeException(e);}
            }
            Actor.process();
        }
        throw new AssertionError("No input boundary: "+state("STALL"));
    }
    private int charge(HolyTome t) throws Exception {
        Field f = Artifact.class.getDeclaredField("charge"); f.setAccessible(true); return f.getInt(t);
    }
    private JSONObject continuity() throws Exception {
        JSONObject j=state("CONTINUITY");
        j.remove("command");j.remove("mobs");j.remove("heaps");j.remove("ironKeys");j.remove("goldenKeys");
        JSONArray inventory=j.getJSONArray("inventory");List<String> sorted=new ArrayList<>();
        for(int n=0;n<inventory.length();n++)sorted.add(inventory.getString(n));Collections.sort(sorted);j.put("inventory",new JSONArray(sorted));
        JSONArray mobSet=new JSONArray();List<String> mobs=new ArrayList<>();
        for(Mob m:Dungeon.level.mobs)mobs.add(m.apdPeekId()+":"+m.getClass().getSimpleName()+":"+m.pos+":"+m.HP+":"+m.cooldown());
        Collections.sort(mobs);j.put("mobs",new JSONArray(mobs));
        Field f=Artifact.class.getDeclaredField("partialCharge");f.setAccessible(true);j.put("partialCharge",f.getFloat(Dungeon.hero.belongings.getItem(HolyTome.class)));
        j.put("talents",Dungeon.hero.talents.toString());j.put("gold",Dungeon.gold).put("energy",Dungeon.energy).put("challenges",Dungeon.challenges);
        return j;
    }
    private void writeCheckpoint(String dir) throws Exception {
        //Normal native save of the earned run, including all previously visited floors. Never construct a replacement hero.
        Dungeon.saveAll();
        java.nio.file.Path output=java.nio.file.Paths.get(dir);java.nio.file.Files.createDirectories(output);
        java.nio.file.Files.copy(FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath(),output.resolve("game.dat"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        for(int d=1;d<=Dungeon.depth;d++)java.nio.file.Files.copy(FileUtils.getFileHandle(GamesInProgress.depthFile(1,d,0)).file().toPath(),output.resolve("depth"+d+".dat"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Field f=Random.class.getDeclaredField("generators");f.setAccessible(true);
        try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(java.nio.file.Files.newOutputStream(output.resolve("rng.bin")))){out.writeObject(f.get(null));}
        JSONObject manifest=new JSONObject().put("state",continuity()).put("seed",266).put("format","APD native save plus captured RNG, earned by commands from level one");
        java.nio.file.Files.write(output.resolve("manifest.json"),manifest.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        log("NATIVE_CHECKPOINT_WRITTEN");
    }
    private void restoreCheckpoint(String dir) throws Exception {
        java.nio.file.Path input=java.nio.file.Paths.get(dir);
        JSONObject manifest=new JSONObject(new String(java.nio.file.Files.readAllBytes(input.resolve("manifest.json")),java.nio.charset.StandardCharsets.UTF_8));
        int depth=manifest.getJSONObject("state").getInt("depth");
        java.nio.file.Files.createDirectories(FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath().getParent());
        java.nio.file.Files.copy(input.resolve("game.dat"),FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        for(int d=1;d<=depth;d++)java.nio.file.Files.copy(input.resolve("depth"+d+".dat"),FileUtils.getFileHandle(GamesInProgress.depthFile(1,d,0)).file().toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Badges.loadGlobal();Dungeon.loadGame(1);Dungeon.level=Dungeon.loadLevel(1);
        bind(Dungeon.hero);for(Mob m:Dungeon.level.mobs)bind(m);
        for(Heap h:Dungeon.level.heaps.valueList())heapSprite(h);
        Dungeon.switchLevel(Dungeon.level,Dungeon.hero.pos);
        Field f=Random.class.getDeclaredField("generators");f.setAccessible(true);
        try(java.io.ObjectInputStream in=new java.io.ObjectInputStream(java.nio.file.Files.newInputStream(input.resolve("rng.bin")))){f.set(null,in.readObject());}
    }
    private void verifyCheckpoint(String dir) throws Exception {
        JSONObject manifest=new JSONObject(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(dir,"manifest.json")),java.nio.charset.StandardCharsets.UTF_8));
        JSONObject expected=manifest.getJSONObject("state"),actual=continuity();
        assertTrue("Native restore changed retained resources: expected="+expected+" actual="+actual,expected.similar(new JSONObject(actual.toString())));
        log("NATIVE_CONTINUITY_VERIFIED");
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
                .put("exp",h.exp).put("totalTime",Statistics.duration+Actor.now()).put("armor",h.belongings.armor().getClass().getSimpleName()+":+"+h.belongings.armor().level()).put("strength",h.STR()).put("hunger",h.buff(Hunger.class)==null?JSONObject.NULL:h.buff(Hunger.class).hunger());
        JSONArray inventory=new JSONArray();
        for(Item item:h.belongings) inventory.put(item.getClass().getSimpleName()+":"+item.quantity()+":+"+item.level());
        j.put("inventory",inventory);
        try {j.put("tomeCharge",charge(h.belongings.getItem(HolyTome.class)));Field partial=Artifact.class.getDeclaredField("partialCharge");partial.setAccessible(true);j.put("partialCharge",partial.getFloat(h.belongings.getItem(HolyTome.class)));}catch(Exception e){throw new RuntimeException(e);}
        JSONArray mobs=new JSONArray();
        for(Mob m:Dungeon.level.mobs) mobs.put(new JSONObject().put("class",m.getClass().getSimpleName()).put("id",m.id())
                .put("pos",m.pos).put("HP",m.HP).put("state",m.state.getClass().getSimpleName()).put("visible",Dungeon.level.heroFOV[m.pos])
                .put("magicCollision",new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(h.pos,m.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT).collisionPos)
                .put("projectileCollision",new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(h.pos,m.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos));
        j.put("mobs",mobs);
        j.put("ironKeys",com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(Dungeon.depth)));
        j.put("goldenKeys",com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey(Dungeon.depth)));
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
    private void click(int cell) { assertFalse("Script would attack passive exile at "+cell, Actor.findChar(cell) instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollExile); assertTrue(Dungeon.hero.handle(cell)); Dungeon.hero.next(); Dungeon.hero.busy(); ready(); log("CLICK "+cell); }
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
    private void upgradeOwnedSpear() throws Exception {
        Item scroll=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class);
        Item spear=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class);
        assertNotNull(scroll);assertNotNull(spear);
        int oldLevel=spear.level();
        itemSelection=null;upgradeButton=null;
        scroll.execute(Dungeon.hero,"READ");assertNotNull(itemSelection);
        assertTrue(itemSelection.itemSelectable(spear));itemSelection.onSelect(spear);
        assertNotNull(upgradeButton);
        Method confirm=upgradeButton.getClass().getDeclaredMethod("onClick");confirm.setAccessible(true);confirm.invoke(upgradeButton);
        ready();log("READ OWNED UPGRADE ON OWNED SPEAR");
        assertEquals(oldLevel+1,spear.level());assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
    }
    private void craftShieldingAtActualPot() throws Exception {
        assertTrue("Must enter alchemy using the actual station action",pendingAlchemy);
        assertTrue(Dungeon.level.distance(Dungeon.hero.pos,1495)<=1);
        assertEquals(Terrain.ALCHEMY,Dungeon.level.map[1495]);
        AlchemyScene scene=mock(AlchemyScene.class,CALLS_REAL_METHODS);
        String[] ui={"energyIcon","energyLeft","energyAdd","repeat","cancel","smokeEmitter","bubbleEmitter","sparkEmitter"};
        for(String name:ui){Field f=AlchemyScene.class.getDeclaredField(name);f.setAccessible(true);f.set(scene,mock(f.getType(),RETURNS_DEEP_STUBS));}
        String[] arrays={"inputs","combines","outputs"};Object[][] old=new Object[3][];
        for(int a=0;a<arrays.length;a++){
            Field f=AlchemyScene.class.getDeclaredField(arrays[a]);f.setAccessible(true);Object[] array=(Object[])f.get(null);old[a]=array.clone();
            for(int n=0;n<array.length;n++){
                Class<?> type=array.getClass().getComponentType();
                array[n]=mock(type,a==0?CALLS_REAL_METHODS:RETURNS_DEFAULTS);
                if(a==0){Field slot=type.getDeclaredField("slot");slot.setAccessible(true);slot.set(array[n],mock(com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot.class));}
            }
        }
        try{
            Item healing=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class);assertNotNull(healing);
            ArrayList<Item> selected=new ArrayList<>();selected.add(healing.duplicate());
            scene.populate(selected,Dungeon.hero.belongings); //original inventory detachment and recipe selection
            Method filter=AlchemyScene.class.getDeclaredMethod("filterInput",Class.class);filter.setAccessible(true);
            ArrayList<Item> inputs=(ArrayList<Item>)filter.invoke(scene,Item.class);
            ArrayList<Recipe> recipes=Recipe.findRecipes(inputs);int selectedRecipe=-1;
            for(int n=0;n<recipes.size();n++)if(recipes.get(n).sampleOutput(inputs) instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding)selectedRecipe=n;
            assertTrue("No shielding recipe: inputs="+inputs+" identified="+(inputs.isEmpty()?"empty":inputs.get(0).isIdentified())+" recipeCount="+recipes.size(),selectedRecipe>=0);int cost=recipes.get(selectedRecipe).cost(inputs),energyBefore=Dungeon.energy;
            assertTrue("Actual energy must cover the recipe",cost<=energyBefore);
            Method combine=AlchemyScene.class.getDeclaredMethod("combine",int.class);combine.setAccessible(true);combine.invoke(scene,selectedRecipe);
            assertEquals(energyBefore-cost,Dungeon.energy);
            assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));
            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class));
            log("CRAFT SHIELDING USING OWNED HEALING AND EARNED ENERGY");
        }finally{
            for(int a=0;a<arrays.length;a++){Field f=AlchemyScene.class.getDeclaredField(arrays[a]);f.setAccessible(true);Object[] array=(Object[])f.get(null);System.arraycopy(old[a],0,array,0,array.length);}
            pendingAlchemy=false;
        }
    }
    private void throwLitBomb(int target) {
        Item bomb=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.class);
        assertNotNull(bomb);selection=null;bomb.execute(Dungeon.hero,"LIGHTTHROW");
        assertNotNull(selection);selection.onSelect(target);ready();log("LIGHTTHROW Bomb "+target);
    }
    @Test public void seed266InitialCommands() throws Exception {
        System.out.println("RUNNER_STAGE scheduler start");
        ready();
        if(System.getProperty("route.resume")!=null){verifyCheckpoint(System.getProperty("route.resume"));thirdFloorSection();return;}
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
        for(int n=0;n<135;n++)waitOne();
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
                cast(HolyWard.INSTANCE,null);
                cast(GuidingLight.INSTANCE,515);
                if(Actor.findChar(515)!=null)throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,515);
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
            cast(GuidingLight.INSTANCE,318);
        }
        assertEquals(2,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));
        Dungeon.hero.upgradeTalent(Talent.SHIELD_OF_LIGHT);log("UPGRADE SHIELD_OF_LIGHT");
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class).execute(Dungeon.hero,"EAT");ready();log("EAT FIRST RATION ON CLEARED FLOOR ONE");
        if(Dungeon.hero.belongings.getItem(HolyTome.class).level()==0)cast(HolyWard.INSTANCE,null);
        assertEquals(1,Dungeon.hero.belongings.getItem(HolyTome.class).level());
        for(int n=0;n<180;n++)waitOne();
        assertEquals(4,charge(Dungeon.hero.belongings.getItem(HolyTome.class)));
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
        for(int cell:new int[]{1441,1399,1357,1358,1359,1360})click(cell);
        log("RECOVER SECOND FLOOR SPEAR");
        upgradeOwnedSpear();
        Mob roomGnoll=(Mob)Actor.findChar(939);assertNotNull(roomGnoll);
        Mob roomRat=(Mob)Actor.findChar(848);assertNotNull(roomRat);
        for(int cell:new int[]{1317,1316,1315,1273,1231,1189,1147,1105,1104,1062,1020})click(cell);
        if(Boolean.getBoolean("route.encumberedMail")){
            Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor.class).execute(Dungeon.hero,"EQUIP");ready();log("EQUIP MAIL FOR GNOLL");
        }
        click(978);
        log("GNOLL_APPROACH");
        cast(GuidingLight.INSTANCE,roomRat.pos);
        if(roomRat.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,roomRat.pos);
        if(roomRat.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,roomRat.pos);
        assertFalse("Upper rat not cleared",roomRat.isAlive());
        if(!Dungeon.level.heroFOV[roomGnoll.pos])click(936);
        if(!Dungeon.level.heroFOV[roomGnoll.pos])waitOne();
        assertTrue("Gnoll not revealed by investigated approach",Dungeon.level.heroFOV[roomGnoll.pos]);
        cast(ShieldOfLight.INSTANCE,roomGnoll.pos);
        cast(GuidingLight.INSTANCE,roomGnoll.pos);
        log("GNOLL_SHIELDED_EXCHANGE");
        if(roomGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,roomGnoll.pos);
        log("GNOLL_FIRST_SPEAR");
        if(roomGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,roomGnoll.pos);
        if(roomGnoll.isAlive())click(roomGnoll.pos);
        assertFalse("First gnoll not cleared",roomGnoll.isAlive());
        log("FIRST_GNOLL_KILLED");
        for(int cell:new int[]{937,937,896,855,813,771,729,728,727})click(cell);
        log("PASTY_AND_EXIT_APPROACH");
        for(int cell:new int[]{685,643,601,600,599,598,640,682,724})click(cell);
        log("SECOND_FLOOR_EXIT");
        assertEquals(3,Dungeon.depth);
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        log("FLOOR_THREE_ENTERED");
        if(System.getProperty("route.checkpointOut")!=null)writeCheckpoint(System.getProperty("route.checkpointOut"));
        if(Boolean.getBoolean("route.stopAfterFloor3"))return;
        thirdFloorSection();
    }
    private void thirdFloorSection() throws Exception {
        assertEquals(3,Dungeon.depth);
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class).execute(Dungeon.hero,"EAT");ready();log("EAT SECOND RATION");
        Mob firstBridgeGnoll=(Mob)Actor.findChar(455);assertNotNull(firstBridgeGnoll);
        Mob secondBridgeGnoll=(Mob)Actor.findChar(412);assertNotNull(secondBridgeGnoll);
        for(int n=0;n<60;n++)waitOne();
        for(int cell:new int[]{684,683,682,681,680,679,678,677,676,675,675})click(cell);
        assertNotNull("Scroll must actually be picked up",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
        upgradeOwnedSpear();
        Mob frontBridgeGnoll=(Mob)Actor.findChar(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,firstBridgeGnoll.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT).collisionPos);
        assertNotNull("Bridge projectile has no enemy target",frontBridgeGnoll);
        cast(ShieldOfLight.INSTANCE,frontBridgeGnoll.pos);
        cast(GuidingLight.INSTANCE,frontBridgeGnoll.pos);
        if(frontBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,frontBridgeGnoll.pos);
        if(frontBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,frontBridgeGnoll.pos);
        assertFalse("First bridge gnoll not cleared",frontBridgeGnoll.isAlive());
        log("FIRST_BRIDGE_GNOLL_KILLED");
        Mob rearBridgeGnoll=frontBridgeGnoll==firstBridgeGnoll?secondBridgeGnoll:firstBridgeGnoll;
        cast(GuidingLight.INSTANCE,rearBridgeGnoll.pos);
        if(rearBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,rearBridgeGnoll.pos);
        if(rearBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,rearBridgeGnoll.pos);
        assertFalse("Second bridge gnoll not cleared",rearBridgeGnoll.isAlive());
        log("BOTH_BRIDGE_GNOLLS_KILLED");
        for(int cell:new int[]{631,631,587,543,542,541,541,584,627,626,625,624,623,622,621,620,619,618,575,532,489,446,403})click(cell);
        Dungeon.hero.search(true);ready();log("SEARCH SECRET DOOR 402");
        click(402);log("SECRET_ROOM_ENTRY");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,358);
        log("SECRET_ROOM_RAT_SHOT");
        for(int cell:new int[]{358,357,356,355,354,397,396,439,482,525,526,527,527,528,529,529})click(cell);
        assertNotNull("Levitation must actually be collected",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class));
        log("LEVITATION_ACTUALLY_ACQUIRED");
        for(int cell:new int[]{528,527,526,525,482,439,396,397,398,399,400,401,402,403,446,489,532,575,618,619,620,621,622,623,624,625,626,627,670,671,672,673,674,675})click(cell);
        Mob vaultGnoll=(Mob)Actor.findChar(804);assertNotNull("Uninvestigated vault gnoll position",vaultGnoll);
        cast(ShieldOfLight.INSTANCE,vaultGnoll.pos);cast(GuidingLight.INSTANCE,vaultGnoll.pos);
        if(vaultGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,vaultGnoll.pos);
        if(vaultGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,vaultGnoll.pos);
        assertFalse("Spawned vault gnoll survived",vaultGnoll.isAlive());
        for(int cell:new int[]{717,717,760,760,759,758,757,756,798,841,884})click(cell);
        log("CHASM_VAULT_APPROACH");
        assertEquals(841,Dungeon.hero.pos);
        Dungeon.hero.search(true);ready();log("SEARCH SECRET VAULT DOOR 884");
        click(884);assertEquals(884,Dungeon.hero.pos);
        assertTrue("Earned golden key required",com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey(3))>=1);
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK OWNED LEVITATION");
        for(int cell:new int[]{927,970,1012,1012})click(cell);
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff staff=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff.class);
        assertNotNull("Quarterstaff must actually be collected",staff);assertEquals(2,staff.level());assertTrue(staff.STRReq()<=Dungeon.hero.STR());
        staff.execute(Dungeon.hero,"EQUIP");ready();log("EQUIP EARNED QUARTERSTAFF");
        for(int cell:new int[]{970,927,884,841})click(cell);
        assertEquals(841,Dungeon.hero.pos);assertFalse("Must return to solid ground",Dungeon.level.pit[Dungeon.hero.pos]);
        log("VAULT_STAFF_ACQUIRED_AND_ESCAPED");
        for(int cell:new int[]{798,756,757,758,759,760,803,846,847})click(cell);
        for(int n=0;n<6;n++)waitOne();
        Mob bottomSnake=(Mob)Actor.findChar(975);assertNotNull(bottomSnake);
        click(890);log("SNAKE_ROOM_APPROACH");
        cast(GuidingLight.INSTANCE,bottomSnake.pos);
        if(bottomSnake.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,bottomSnake.pos);
        assertFalse(bottomSnake.isAlive());log("BOTTOM_SNAKE_KILLED");
        Mob bottomRat=(Mob)Actor.findChar(1195);assertNotNull(bottomRat);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,bottomRat.pos);
        if(bottomRat.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,bottomRat.pos);
        assertFalse(bottomRat.isAlive());log("BOTTOM_RAT_KILLED");
        for(int cell:new int[]{933,976,1019,1020,1064,1108,1151,1194,1195,1195,1194,1237,1237,1280,1323,1324,1325,1325,1326,1327,1327})click(cell);
        assertNotNull("Second strength potion must be collected",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class));
        assertEquals("Actual energy crystal collection",5,Dungeon.energy);
        log("STRENGTH_AND_ALCHEMY_ENERGY_ACQUIRED");
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK SECOND STRENGTH");
        assertEquals(12,Dungeon.hero.STR());
        for(int cell:new int[]{1369,1411,1453,1495})click(cell);
        craftShieldingAtActualPot();
        log("ALCHEMY_COMPLETE");
        click(1409);click(1366);
        if(Boolean.getBoolean("route.preemptiveShield")){
            Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK OWNED SHIELDING BEFORE EXIT");
        }
        click(1323);click(1280);
        log("ALCHEMY_ROOM_EXIT_APPROACH");
        for(int n=0;n<10;n++)waitOne();
        click(1237);log("OPEN ALCHEMY EXIT AFTER PAID WAIT");
        click(1194);click(1151);log("LEAVE ALCHEMY CORRIDOR");
        for(int cell:new int[]{1108,1064,1020,1019,976,933,890,847,846,803,760,759,716,673,672,671,670,627,626,625})click(cell);
        Mob corridorSnake=(Mob)Actor.findChar(623);assertNotNull("Uninvestigated corridor snake position",corridorSnake);
        cast(GuidingLight.INSTANCE,corridorSnake.pos);
        if(corridorSnake.isAlive())click(corridorSnake.pos);
        assertFalse(corridorSnake.isAlive());
        assertEquals(3,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));
        Dungeon.hero.upgradeTalent(Talent.SHIELD_OF_LIGHT);log("EARN SECOND SHIELD_OF_LIGHT POINT");
        for(int cell:new int[]{624,623,622,621,620,619,618,617,616,615,614,613,612,611,654})click(cell);
        log("CRAB_ROOM_OUTSIDE_CLOSED_DOOR");
        Mob pursuingSwarm=(Mob)Actor.findChar(613);assertNotNull("Uninvestigated pursuing swarm position",pursuingSwarm);
        cast(ShieldOfLight.INSTANCE,pursuingSwarm.pos);
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK OWNED SHIELDING FOR PURSUING SWARM");
        click(pursuingSwarm.pos);log("SWARM_FIRST_REAL_MELEE");
        if(pursuingSwarm.isAlive())click(pursuingSwarm.pos);
        if(pursuingSwarm.isAlive())click(pursuingSwarm.pos);
        log("SWARM_PARENT_THREE_EXCHANGES");
        if(pursuingSwarm.isAlive())click(pursuingSwarm.pos);
        assertFalse("Swarm parent survived four attacks",pursuingSwarm.isAlive());
        Mob smallSwarm=(Mob)Actor.findChar(611);assertNotNull(smallSwarm);
        click(smallSwarm.pos);if(smallSwarm.isAlive())click(smallSwarm.pos);
        assertFalse("Small swarm child survived",smallSwarm.isAlive());
        log("SWARM_PARENT_AND_SMALL_CHILD_KILLED");
        Mob largeSwarm=(Mob)Actor.findChar(655);assertNotNull(largeSwarm);
        click(largeSwarm.pos);if(largeSwarm.isAlive())click(largeSwarm.pos);
        log("LARGE_SWARM_CHILD_TWO_EXCHANGES");
        if(largeSwarm.isAlive())click(largeSwarm.pos);assertFalse(largeSwarm.isAlive());
        Mob finalSwarm=(Mob)Actor.findChar(655);assertNotNull(finalSwarm);
        click(finalSwarm.pos);if(finalSwarm.isAlive())click(finalSwarm.pos);
        assertFalse(finalSwarm.isAlive());log("PURSUING_SWARM_AND_ALL_CHILDREN_DEFEATED");
        for(int n=0;n<28;n++)waitOne();
        click(653);log("CRAB_ACTUALLY_REVEALED");
        waitOne();log("CRAB_LURE_WAIT_FROM_DOOR");
        Mob exitCrab=(Mob)Actor.findChar(564);assertNotNull(exitCrab);
        cast(ShieldOfLight.INSTANCE,exitCrab.pos);
        waitOne();log("CRAB_LURED_AROUND_OBSTACLE");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,exitCrab.pos);
        if(exitCrab.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,exitCrab.pos);
        log("CRAB_TWO_RANGED_ATTACKS");
        if(exitCrab.isAlive())click(exitCrab.pos);
        if(exitCrab.isAlive())click(exitCrab.pos);
        assertFalse(exitCrab.isAlive());log("EXIT_CRAB_DEFEATED");
        for(int cell:new int[]{652,651,650,649})click(cell);
        log("BLINK_EXIT_LINE");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink.class,520);
        assertEquals(520,Dungeon.hero.pos);log("BLINK_ONTO_REAL_EXIT");
        click(520);assertEquals(4,Dungeon.depth);log("FLOOR_FOUR_ENTERED");
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        if(System.getProperty("route.checkpoint4Out")!=null)writeCheckpoint(System.getProperty("route.checkpoint4Out"));

        assertEquals(1,Dungeon.hero.HP);
    }
}
