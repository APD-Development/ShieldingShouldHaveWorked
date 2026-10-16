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
    @Rule public org.junit.rules.Timeout deadline = org.junit.rules.Timeout.seconds(Integer.getInteger("route.testTimeoutSeconds",45));
    private final List<MockedStatic<?>> visuals = new ArrayList<>();
    private CellSelector.Listener selection;
    private com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector itemSelection;
    private MockedConstruction<com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade> upgradeWindows;
    private Object upgradeButton;
    private MockedConstruction<com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem> tradeWindows;
    private com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem tradeWindow;
    private Heap tradeHeap;
    private boolean executeTradeUi;
    private boolean executeChasmUi;
    private MockedConstruction<?> chasmWindows;
    private Object chasmWindow;
    private MockedConstruction<?> hourglassWindows;
    private Object hourglassWindow;
    private Camera oldMainCamera;
    private int chainAnimationsCompleted;
    private int pushAnimationsCompleted;
    private AtomicBoolean watch;
    private Thread watchdog;
    private boolean oldKeepAlive;
    private boolean pendingTransition;
    private boolean pendingAlchemy;
    private com.watabou.noosa.Scene currentTestScene;
    private GameScene gameplaySceneStub;
    private com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator oldTargetIndicator;

    private void bind(Char ch) {
        if (ch.sprite != null) return;
        CharSprite sprite = ch == Dungeon.hero ? mock(HeroSprite.class, RETURNS_DEEP_STUBS)
                : ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo ? mock(com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite.class,RETURNS_DEEP_STUBS)
                : ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer ? mock(com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite.class,RETURNS_DEEP_STUBS) : mock(CharSprite.class, RETURNS_DEEP_STUBS);
        ch.sprite = sprite;
        if(ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo)doAnswer(i->{ch.onAttackComplete();return null;}).when((com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite)sprite).pumpAttack();
        if(ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer)
            doAnswer(i->{com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer necro=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer)ch;if(!necro.summoning)necro.onZapComplete();return null;}).when(sprite).zap(anyInt());
        if(ch==Dungeon.hero)doAnswer(i -> {ch.onOperateComplete();return null;}).when((HeroSprite)sprite).read();
        Group parent=mock(Group.class,i -> {
            if(i.getMethod().getName().equals("recycle") && i.getArguments().length==1 && i.getArgument(0) instanceof Class)
                return mock((Class<?>)i.getArgument(0),RETURNS_DEEP_STUBS);
            if(i.getMethod().getName().equals("add") && i.getArguments().length==1
                    && (i.getArgument(0) instanceof com.shatteredpixel.shatteredpixeldungeon.effects.Chains
                    || i.getArgument(0) instanceof com.shatteredpixel.shatteredpixeldungeon.effects.Pushing.Effect)){
                com.watabou.noosa.Gizmo effect=i.getArgument(0);float before=Game.elapsed;
                try{Game.elapsed=100f;effect.update();}finally{Game.elapsed=before;}
                if(effect instanceof com.shatteredpixel.shatteredpixeldungeon.effects.Chains)chainAnimationsCompleted++;
                else pushAnimationsCompleted++;
                System.out.println("ANIMATION_CALLBACK_COMPLETED "+effect.getClass().getName());
                return effect;
            }
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
    private void bindBlobVisuals(){
        for(Blob b:Dungeon.level.blobs.values())if(b instanceof com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire)
            b.use(mock(com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter.class));
    }
    private <T> MockedStatic<T> visual(Class<T> type) {
        MockedStatic<T> m = mockStatic(type); visuals.add(m); return m;
    }
    @Before public void init() throws Exception {
        System.out.println("RUNNER_STAGE initialize");
        HeadlessTestSupport.start();
        oldMainCamera=Camera.main;Camera.main=mock(Camera.class);
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
        tradeWindows=mockConstruction(com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem.class,(window,contextArgs)->{
            if(contextArgs.arguments().size()==1 && contextArgs.arguments().get(0) instanceof Heap){tradeWindow=window;tradeHeap=(Heap)contextArgs.arguments().get(0);}
        });
        Class jumpWindowClass=Class.forName("com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm$1$1");
        chasmWindows=mockConstruction(jumpWindowClass,withSettings().defaultAnswer(CALLS_REAL_METHODS),(window,contextArgs)->{chasmWindow=window;Field outer=window.getClass().getDeclaredField("this$0");outer.setAccessible(true);outer.set(window,contextArgs.arguments().get(0));});
        Class freezeWindowClass=Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass$1");
        hourglassWindows=mockConstruction(freezeWindowClass,withSettings().defaultAnswer(CALLS_REAL_METHODS),(window,contextArgs)->{hourglassWindow=window;Field outer=window.getClass().getDeclaredField("this$0");outer.setAccessible(true);outer.set(window,contextArgs.arguments().get(0));});
        MockedStatic<Game> game=visual(Game.class);
        game.when(()->Game.runOnRenderThread(any(Callback.class))).thenAnswer(i->{if(executeTradeUi || executeChasmUi)((Callback)i.getArgument(0)).call();return null;});
        gameplaySceneStub=mock(GameScene.class,RETURNS_DEEP_STUBS);
        game.when(Game::scene).thenAnswer(i->currentTestScene);
        game.when(() -> Game.switchScene(InterlevelScene.class)).thenAnswer(i -> { pendingTransition=true; currentTestScene=mock(InterlevelScene.class,RETURNS_DEEP_STUBS); return null; });
        game.when(() -> Game.switchScene(AlchemyScene.class)).thenAnswer(i -> {pendingAlchemy=true;currentTestScene=mock(AlchemyScene.class,RETURNS_DEEP_STUBS);return null;});
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
        GamesInProgress.selectedClass = HeroClass.valueOf(System.getProperty("route.selectedClass","CLERIC"));
        GamesInProgress.randomizedClass = false; GamesInProgress.curSlot = 1;
        String resume=System.getProperty("route.resume");
        if(resume==null){
            java.nio.file.Path freshSlot=FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath().getParent();
            java.nio.file.Files.createDirectories(freshSlot);
            try(java.nio.file.DirectoryStream<java.nio.file.Path> files=java.nio.file.Files.newDirectoryStream(freshSlot,"*.dat")){
                for(java.nio.file.Path file:files)java.nio.file.Files.delete(file);
            }
            Dungeon.init();
            Random.pushGenerator(266L);
            Dungeon.depth=1;Dungeon.branch=0;Dungeon.level=Dungeon.newLevel();
            bind(Dungeon.hero);for(Mob m:Dungeon.level.mobs)bind(m);
            for(Heap h:Dungeon.level.heaps.valueList())heapSprite(h);
            Dungeon.switchLevel(Dungeon.level,-1);
        }else{
            restoreCheckpoint(resume);
        }
        currentTestScene=gameplaySceneStub;
        bindBlobVisuals();
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
        Camera.main=oldMainCamera;
        com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance=oldTargetIndicator;
        if(Dungeon.hero!=null && !Dungeon.hero.isAlive()){
            try{System.out.println("ROUTE "+state("TERMINAL_DEATH_BEFORE_RENDERER_CLEANUP"));}catch(Throwable deathLogError){System.out.println("TERMINAL_DEATH_STATE_LOG_ERROR "+deathLogError);}
        }
        Actor.clear(); Random.popGenerator();
        if(upgradeWindows!=null)upgradeWindows.close();
        if(tradeWindows!=null)tradeWindows.close();
        if(chasmWindows!=null)chasmWindows.close();
        if(hourglassWindows!=null)hourglassWindows.close();
        Collections.reverse(visuals); for (MockedStatic<?> m : visuals) m.close();
    }
    private void ready() {
        for (int steps=0; steps<10000; steps++) {
            if(pendingTransition){
                pendingTransition=false;
                try {
                    Actor.fixTime();
                    InterlevelScene scene=mock(InterlevelScene.class,CALLS_REAL_METHODS);
                    Method transition=InterlevelScene.class.getDeclaredMethod(InterlevelScene.mode==InterlevelScene.Mode.DESCEND ? "descend" : InterlevelScene.mode==InterlevelScene.Mode.FALL ? "fall" : "ascend");
                    transition.setAccessible(true);transition.invoke(scene);
                    currentTestScene=gameplaySceneStub;
                    com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton.reset(); //same UI reset performed when the previous scene's quickslot buttons are destroyed
                    bindBlobVisuals();
                    bind(Dungeon.hero);for(Mob m:Dungeon.level.mobs)bind(m);
                    for(Heap h:Dungeon.level.heaps.valueList())heapSprite(h);
                    if(InterlevelScene.mode==InterlevelScene.Mode.FALL)Dungeon.hero.busy();
                }catch(Exception e){throw new RuntimeException(e);}
            }
            if (!Dungeon.hero.isAlive()) throw new AssertionError("Terminal hero state: "+state("DEATH"));
            if (Dungeon.hero.ready && Dungeon.hero.cooldown()<=0.0001f && Dungeon.hero.paralysed==0) return;
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
        Field f=Artifact.class.getDeclaredField("partialCharge");f.setAccessible(true);if(Dungeon.hero.belongings.getItem(HolyTome.class)==null)j.put("partialCharge",JSONObject.NULL);else j.put("partialCharge",f.getFloat(Dungeon.hero.belongings.getItem(HolyTome.class)));
        j.put("talents",Dungeon.hero.talents.toString());j.put("gold",Dungeon.gold).put("energy",Dungeon.energy).put("challenges",Dungeon.challenges);
        TimekeepersHourglass hourglass=Dungeon.hero.belongings.getItem(TimekeepersHourglass.class);
        if(hourglass!=null){
            Field whole=Artifact.class.getDeclaredField("charge");whole.setAccessible(true);
            j.put("hourglassCharge",whole.getInt(hourglass)).put("hourglassPartial",f.getFloat(hourglass));
            j.put("hourglassEquipped",hourglass.isEquipped(Dungeon.hero)).put("hourglassCursed",hourglass.cursed);
        }
        return j;
    }
    private void writeCheckpoint(String dir) throws Exception {
        //Normal native save of the earned run, including all previously visited floors. Never construct a replacement hero.
        Dungeon.saveAll();
        java.nio.file.Path output=java.nio.file.Paths.get(dir);java.nio.file.Files.createDirectories(output);
        java.nio.file.Path nativeDir=FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath().getParent();
        JSONArray savedFiles=new JSONArray();
        try(java.nio.file.DirectoryStream<java.nio.file.Path> files=java.nio.file.Files.newDirectoryStream(nativeDir,"*.dat")){
            for(java.nio.file.Path file:files){java.nio.file.Files.copy(file,output.resolve(file.getFileName()),java.nio.file.StandardCopyOption.REPLACE_EXISTING);savedFiles.put(file.getFileName().toString());}
        }
        Field f=Random.class.getDeclaredField("generators");f.setAccessible(true);
        try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(java.nio.file.Files.newOutputStream(output.resolve("rng.bin")))){out.writeObject(f.get(null));}
        JSONObject manifest=new JSONObject().put("state",continuity()).put("seed",266).put("format","APD native save plus captured RNG; see recorded starting state and actions for provenance").put("route_start",System.getProperty("route.resume","native level-one generator")).put("files",savedFiles);
        java.nio.file.Files.write(output.resolve("manifest.json"),manifest.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        log("NATIVE_CHECKPOINT_WRITTEN");
    }
    private void restoreCheckpoint(String dir) throws Exception {
        java.nio.file.Path input=java.nio.file.Paths.get(dir);
        JSONObject manifest=new JSONObject(new String(java.nio.file.Files.readAllBytes(input.resolve("manifest.json")),java.nio.charset.StandardCharsets.UTF_8));
        int depth=manifest.getJSONObject("state").getInt("depth");
        java.nio.file.Files.createDirectories(FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath().getParent());
        java.nio.file.Path nativeDir=FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath().getParent();
        //The checkpoint is the complete retained run. Discard only stale files from other test runs.
        try(java.nio.file.DirectoryStream<java.nio.file.Path> oldFiles=java.nio.file.Files.newDirectoryStream(nativeDir,"*.dat")){
            for(java.nio.file.Path oldFile:oldFiles)java.nio.file.Files.delete(oldFile);
        }
        try(java.nio.file.DirectoryStream<java.nio.file.Path> files=java.nio.file.Files.newDirectoryStream(input,"*.dat")){
            for(java.nio.file.Path file:files)java.nio.file.Files.copy(file,nativeDir.resolve(file.getFileName()),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        Badges.loadGlobal();Dungeon.loadGame(1);
        for(int d=1;d<=Statistics.deepestFloor;d++)assertTrue("Missing previously visited floor "+d,java.nio.file.Files.exists(FileUtils.getFileHandle(GamesInProgress.depthFile(1,d,0)).file().toPath()));
        Dungeon.level=Dungeon.loadLevel(1);
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
    private void verifyExpectedEnd() throws Exception {
        String path=System.getProperty("route.expectedEnd");if(path==null)return;
        java.nio.file.Path root=java.nio.file.Paths.get(path);
        JSONObject expected=new JSONObject(new String(java.nio.file.Files.readAllBytes(root.resolve("manifest.json")),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("state");
        JSONObject actual=continuity();actual.put("time",0);
        assertTrue("Endpoint resources differ from the connected native checkpoint: expected="+expected+" actual="+actual,expected.similar(new JSONObject(actual.toString())));
        Field f=Random.class.getDeclaredField("generators");f.setAccessible(true);
        java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
        try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(bytes)){out.writeObject(f.get(null));}
        if(Boolean.getBoolean("route.expectedActiveRngOnly")){
            Object expectedStack;
            try(java.io.ObjectInputStream in=new java.io.ObjectInputStream(java.nio.file.Files.newInputStream(root.resolve("rng.bin")))){expectedStack=in.readObject();}
            java.util.ArrayDeque<?> expectedGenerators=(java.util.ArrayDeque<?>)expectedStack,actualGenerators=(java.util.ArrayDeque<?>)f.get(null);
            assertEquals("RNG stack depth differs",expectedGenerators.size(),actualGenerators.size());
            java.io.ByteArrayOutputStream a=new java.io.ByteArrayOutputStream(),b=new java.io.ByteArrayOutputStream();
            try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(a)){out.writeObject(expectedGenerators.peekFirst());}
            try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(b)){out.writeObject(actualGenerators.peekFirst());}
            assertArrayEquals("Active gameplay RNG differs",a.toByteArray(),b.toByteArray());
            System.out.println("CONNECTED_ENDPOINT_STATE_AND_ACTIVE_RNG_VERIFIED_INACTIVE_BASE_NOT_COMPARED "+root.getFileName());
        }else{
            assertArrayEquals("Endpoint RNG differs from the connected native checkpoint",java.nio.file.Files.readAllBytes(root.resolve("rng.bin")),bytes.toByteArray());
            System.out.println("CONNECTED_ENDPOINT_STATE_AND_RNG_VERIFIED "+root.getFileName());
        }
        if(Boolean.getBoolean("route.expectedNativeState"))verifyFullNativeEnd(root);
        if(Boolean.getBoolean("route.expectedNativeFiles")){
            assertTrue("Fresh file-only comparison must explicitly compare the active gameplay RNG",Boolean.getBoolean("route.expectedActiveRngOnly"));
            int count=compareAllNativeFiles(root);
            System.out.println("CONNECTED_ALL_NATIVE_FILES_AND_ACTIVE_RNG_VERIFIED_INACTIVE_BASE_NOT_COMPARED files="+count+" "+root.getFileName());
        }

    }
    private JSONObject readNativeJson(java.nio.file.Path path) throws Exception {
        java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
        try(java.util.zip.GZIPInputStream in=new java.util.zip.GZIPInputStream(java.nio.file.Files.newInputStream(path))){
            byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1)bytes.write(buffer,0,count);
        }
        return new JSONObject(new String(bytes.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));
    }
    private void verifyFullNativeEnd(java.nio.file.Path expectedRoot) throws Exception {
        assertFalse("Full native-state verification requires the entire retained RNG stack",Boolean.getBoolean("route.expectedActiveRngOnly"));
        int fileCount=compareAllNativeFiles(expectedRoot);
        Field f=Random.class.getDeclaredField("generators");f.setAccessible(true);java.io.ByteArrayOutputStream rng=new java.io.ByteArrayOutputStream();
        try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(rng)){out.writeObject(f.get(null));}
        assertArrayEquals("Normal save must retain the expected complete RNG",java.nio.file.Files.readAllBytes(expectedRoot.resolve("rng.bin")),rng.toByteArray());
        System.out.println("CONNECTED_FULL_NATIVE_JSON_AND_RNG_VERIFIED files="+fileCount+" "+expectedRoot.getFileName());
    }

    private int compareAllNativeFiles(java.nio.file.Path expectedRoot) throws Exception {
        Dungeon.saveAll(); //Normal native serialization, never restore expected data into the character.
        java.nio.file.Path actualRoot=FileUtils.getFileHandle(GamesInProgress.gameFile(1)).file().toPath().getParent();
        String actualOut=System.getProperty("route.actualNativeOut");
        if(actualOut!=null){java.nio.file.Path copy=java.nio.file.Paths.get(actualOut);java.nio.file.Files.createDirectories(copy);try(java.nio.file.DirectoryStream<java.nio.file.Path> files=java.nio.file.Files.newDirectoryStream(actualRoot,"*.dat")){for(java.nio.file.Path file:files)java.nio.file.Files.copy(file,copy.resolve(file.getFileName()),java.nio.file.StandardCopyOption.REPLACE_EXISTING);}}
        Set<String> expectedFiles=new TreeSet<>(),actualFiles=new TreeSet<>();
        try(java.nio.file.DirectoryStream<java.nio.file.Path> files=java.nio.file.Files.newDirectoryStream(expectedRoot,"*.dat")){for(java.nio.file.Path file:files)expectedFiles.add(file.getFileName().toString());}
        try(java.nio.file.DirectoryStream<java.nio.file.Path> files=java.nio.file.Files.newDirectoryStream(actualRoot,"*.dat")){for(java.nio.file.Path file:files)actualFiles.add(file.getFileName().toString());}
        assertEquals("All visited native files must match",expectedFiles,actualFiles);
        for(String name:expectedFiles)assertTrue("Full native JSON differs: "+name,readNativeJson(expectedRoot.resolve(name)).similar(readNativeJson(actualRoot.resolve(name))));
        return expectedFiles.size();
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
        try {HolyTome ownedTome=h.belongings.getItem(HolyTome.class);if(ownedTome==null){j.put("tomeCharge",JSONObject.NULL).put("partialCharge",JSONObject.NULL);}else{j.put("tomeCharge",charge(ownedTome));Field partial=Artifact.class.getDeclaredField("partialCharge");partial.setAccessible(true);j.put("partialCharge",partial.getFloat(ownedTome));}}catch(Exception e){throw new RuntimeException(e);}
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
    private void log(String s) {
        for(Mob m:Dungeon.level.mobs)if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu){
            JSONArray hazards=new JSONArray();
            for(Buff b:m.buffs()){
                JSONObject v=new JSONObject().put("type",b.getClass().getSimpleName());
                for(String name:new String[]{"bombPos","timer","shockerPos","shockingOrdinals","direction","curPos"})try{Field f=b.getClass().getDeclaredField(name);f.setAccessible(true);v.put(name,f.get(b));}catch(NoSuchFieldException ignored){}catch(Exception e){throw new RuntimeException(e);}
                hazards.put(v);
            }
            System.out.println("TENGU_HAZARDS "+s+" "+hazards);
        }
        assertEquals("Maximum HP must stay exactly one",1,Dungeon.hero.HT);assertTrue("Current HP exceeds one",Dungeon.hero.HP<=1);
        System.out.println("ROUTE "+state(s));
        JSONArray buffs=new JSONArray();
        for(Buff b:Dungeon.hero.buffs()){
            JSONObject entry=new JSONObject().put("type",b.getClass().getSimpleName()).put("remaining",b.cooldown());
            if(b instanceof ShieldOfLight.ShieldOfLightTracker)entry.put("target",((ShieldOfLight.ShieldOfLightTracker)b).object);
            buffs.put(entry);
        }
        System.out.println("BUFF_DETAIL "+s+" "+buffs);
        HolyTome loggedTome=Dungeon.hero.belongings.getItem(HolyTome.class);if(loggedTome!=null){try{Field cap=Artifact.class.getDeclaredField("chargeCap"),xp=Artifact.class.getDeclaredField("exp");cap.setAccessible(true);xp.setAccessible(true);System.out.println("TOME_DETAIL "+s+" artifactLevel="+loggedTome.level()+" chargeCap="+cap.getInt(loggedTome)+" spellExperience="+xp.getInt(loggedTome));}catch(Exception e){throw new RuntimeException(e);}}
        TimekeepersHourglass hourglass=Dungeon.hero.belongings.getItem(TimekeepersHourglass.class);
        if(hourglass!=null){
            try{Field whole=Artifact.class.getDeclaredField("charge"),part=Artifact.class.getDeclaredField("partialCharge");whole.setAccessible(true);part.setAccessible(true);
                System.out.println("HOURGLASS_DETAIL "+s+" charge="+whole.getInt(hourglass)+" partial="+part.getFloat(hourglass)+" equipped="+hourglass.isEquipped(Dungeon.hero)+" frozen="+(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null));
            }catch(Exception e){throw new RuntimeException(e);}
        }
        if(Dungeon.depth==5){
            for(Mob m:Dungeon.level.mobs)if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo){
                try{Field pumped=m.getClass().getDeclaredField("pumpedUp");pumped.setAccessible(true);System.out.println("GOO_DETAIL "+s+" HP="+m.HP+" pumped="+pumped.getInt(m)+" water="+Dungeon.level.water[m.pos]+" heroWater="+Dungeon.level.water[Dungeon.hero.pos]+" locked="+Dungeon.level.locked+" heroShield="+Dungeon.hero.shielding());}catch(Exception e){throw new RuntimeException(e);}
            }
        }
    }
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
        Item spear=(Boolean.getBoolean("route.upgradeCloth") || (Boolean.getBoolean("route.upgradeClothOnThree") && Dungeon.depth==3) || (Boolean.getBoolean("route.originFiveClothUpgrade") && Dungeon.depth==4))?Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor.class):Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class);
        assertNotNull(scroll);assertNotNull(spear);
        int oldLevel=spear.level();
        itemSelection=null;upgradeButton=null;
        scroll.execute(Dungeon.hero,"READ");assertNotNull(itemSelection);
        assertTrue(itemSelection.itemSelectable(spear));itemSelection.onSelect(spear);
        assertNotNull(upgradeButton);
        Method confirm=upgradeButton.getClass().getDeclaredMethod("onClick");confirm.setAccessible(true);confirm.invoke(upgradeButton);
        ready();log(spear instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor?"READ OWNED UPGRADE ON ACTUALLY WORN CLOTH":"READ OWNED UPGRADE ON OWNED SPEAR");
        assertEquals(oldLevel+1,spear.level());assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
    }
    private void craftShieldingAtActualPot() throws Exception {
        craftShieldingAtActualPot(1495);
    }
    private void craftShieldingAtActualPot(int actualPot) throws Exception {
        craftExoticPotionAtActualPot(actualPot,com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class,com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class);
    }
    private void craftExoticPotionAtActualPot(int actualPot,Class<? extends Item> inputType,Class<? extends Item> outputType) throws Exception {
        assertTrue("Must enter alchemy using the actual station action",pendingAlchemy);
        assertTrue(Dungeon.level.distance(Dungeon.hero.pos,actualPot)<=1);
        assertEquals(Terrain.ALCHEMY,Dungeon.level.map[actualPot]);
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
            Item healing=Dungeon.hero.belongings.getItem(inputType);assertNotNull(healing);
            int healingCountBefore=healing.quantity();Item previousShielding=Dungeon.hero.belongings.getItem(outputType);int shieldingCountBefore=previousShielding==null?0:previousShielding.quantity();
            ArrayList<Item> selected=new ArrayList<>();Item selectOneHealing=healing.duplicate();selectOneHealing.quantity(1);selected.add(selectOneHealing);
            scene.populate(selected,Dungeon.hero.belongings); //original inventory detachment and recipe selection
            Method filter=AlchemyScene.class.getDeclaredMethod("filterInput",Class.class);filter.setAccessible(true);
            ArrayList<Item> inputs=(ArrayList<Item>)filter.invoke(scene,Item.class);
            ArrayList<Recipe> recipes=Recipe.findRecipes(inputs);int selectedRecipe=-1;
            for(int n=0;n<recipes.size();n++)if(outputType.isInstance(recipes.get(n).sampleOutput(inputs)))selectedRecipe=n;
            assertTrue("No requested exotic recipe: inputs="+inputs+" identified="+(inputs.isEmpty()?"empty":inputs.get(0).isIdentified())+" recipeCount="+recipes.size(),selectedRecipe>=0);int cost=recipes.get(selectedRecipe).cost(inputs),energyBefore=Dungeon.energy;
            assertTrue("Actual energy must cover the recipe",cost<=energyBefore);
            Method combine=AlchemyScene.class.getDeclaredMethod("combine",int.class);combine.setAccessible(true);combine.invoke(scene,selectedRecipe);
            assertEquals(energyBefore-cost,Dungeon.energy);
            Item remainingHealing=Dungeon.hero.belongings.getItem(inputType);assertEquals(healingCountBefore-1,remainingHealing==null?0:remainingHealing.quantity());
            Item newShielding=Dungeon.hero.belongings.getItem(outputType);assertNotNull(newShielding);assertEquals(shieldingCountBefore+1,newShielding.quantity());
            log("CRAFT "+outputType.getSimpleName()+" USING OWNED "+inputType.getSimpleName()+" AND EARNED ENERGY");
        }finally{
            for(int a=0;a<arrays.length;a++){Field f=AlchemyScene.class.getDeclaredField(arrays[a]);f.setAccessible(true);Object[] array=(Object[])f.get(null);System.arraycopy(old[a],0,array,0,array.length);}
            pendingAlchemy=false;currentTestScene=gameplaySceneStub;
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
        if(Boolean.getBoolean("route.liveSevenUpgradeProbe")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));assertEquals(7,Dungeon.depth);assertEquals(538,Dungeon.hero.pos);
            for(int n=0;n<180;n++){if(Dungeon.level.mobs.stream().anyMatch(m->Dungeon.level.heroFOV[m.pos]))break;waitOne();}
            log("LIVE SEVEN ACTUAL PAID PREPARATION BEFORE UNCOLLECTED UPGRADE");
            if(System.getProperty("route.liveNineOut")!=null)writeCheckpoint(System.getProperty("route.liveNineOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.liveNineFromSeven")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));liveNineFromSeven();verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.alternativeWarriorOpening")){
            assertNull(System.getProperty("route.resume"));assertEquals(HeroClass.WARRIOR,Dungeon.hero.heroClass);assertEquals(1,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.HT);assertEquals(1,Dungeon.hero.HP);for(Map<Talent,Integer> tier:Dungeon.hero.talents)for(int v:tier.values())assertEquals(0,v);log("ACTUAL FRESH LEVEL ONE WARRIOR ORIGINAL RULES");
            waitOne();waitOne();
            for(int cell:new int[]{905,906,907,908,909,910,911,912,951,952,953,954,955,956,957,958,959,960,999,1038,1077,1078,1079,1080,1119,1080,1079,1078,1077,1038,999,960,959,958,957,956,955,954,953,952,951,912,911,910,909,908,868,830,831,832,833,834,834,795,796,797,758,719,757,796,834,833,832,831,830,868,908,909,910,911,912,951,952,953,954,955,956,957,958,959,960,999,1038,1077,1078,1079,1080,1081,1082})click(cell);
            assertEquals(1082,Dungeon.hero.pos);Mob firstRat=(Mob)Actor.findChar(965);assertTrue(firstRat instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat);
            for(int n=0;n<6 && !Dungeon.level.adjacent(Dungeon.hero.pos,firstRat.pos);n++)waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,firstRat.pos));
            for(int n=0;n<8 && Dungeon.hero.shielding()==0;n++)waitOne();assertTrue(Dungeon.hero.shielding()>0);log("ACTUAL LEVEL ONE WARRIOR SEAL INTERCEPTS REAL RAT HIT");
            for(int hit=0;hit<8 && firstRat.isAlive();hit++)click(firstRat.pos);assertFalse(firstRat.isAlive());log("ACTUAL LEVEL ONE WARRIOR FIRST RAT DEFEATED WITHOUT GRANTED TALENTS OR GEAR");
            if(System.getProperty("route.alternativeOut")!=null)writeCheckpoint(System.getProperty("route.alternativeOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshSecondPrisonSkeletonNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));freshSecondPrisonSkeleton();
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshPrisonSwarmNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));freshPrisonSwarmSection();
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshPrisonNextRechargeNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));freshPrisonNextRecharge();
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshFixedTargetNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));
            Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==Integer.getInteger("route.freshTargetId")).findFirst().get();freshGooActions(target);log("FRESH FIXED TARGET ACTUAL ACTION FRONTIER");
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshPrisonSixAfterFirstNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));freshPrisonSixAfterFirst();
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshPrisonSixNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));freshPrisonSixSection();
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshAfterGooNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));freshAfterGooSection();
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(Boolean.getBoolean("route.freshGooNative")){
            assertNotNull(System.getProperty("route.resume"));verifyCheckpoint(System.getProperty("route.resume"));assertEquals(5,Dungeon.depth);
            Mob goo=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo).findFirst().get();freshGooActions(goo);log("EARNED NATIVE GOO CONTINUATION");
            if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));verifyExpectedEnd();return;
        }
        if(System.getProperty("route.resume")!=null){verifyCheckpoint(System.getProperty("route.resume"));if(Dungeon.depth==3)thirdFloorSection();else if(Dungeon.depth==4){if(Dungeon.hero.pos==826)returnedFourthFloorSection();else if(Dungeon.hero.pos==437)purityCheckpointSection();else if(Dungeon.hero.pos==1035)foodCheckpointSection();else fourthFloorSection();}else if(Dungeon.depth==5){if(Boolean.getBoolean("route.postGoo"))postGooSection();else if(Boolean.getBoolean("route.armoredGoo"))armoredGooSection();else fifthFloorSection();}else if(Dungeon.depth==6)sixthFloorSection();else if(Dungeon.depth==7)seventhFloorSection();else if(Dungeon.depth==8)eighthFloorSection();else if(Dungeon.depth==9)ninthFloorSection();else fail("Unwritten section");verifyExpectedEnd();return;}
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
            Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor.class).execute(Dungeon.hero,"EQUIP");Dungeon.hero.busy();ready();log("EQUIP MAIL FOR GNOLL");
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
        if(Boolean.getBoolean("route.certifyPrefix")){
            Mob extraSnake=Dungeon.level.mobs.stream().filter(m->m.id()==44).findFirst().get();
            cast(GuidingLight.INSTANCE,extraSnake.pos);log("CERTIFIED PREFIX HANDLE EXTRA SNAKE AT RANGE TWO");
            assertFalse(extraSnake.isAlive());
        }
        for(int cell:new int[]{685,643,601,600,599,598,640,682})click(cell);
        if(Boolean.getBoolean("route.chargeOnTwo")){
            Item ration=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);
            assertNotNull(ration);ration.execute(Dungeon.hero,"EAT");ready();log("EARNED SECOND RATION ON FLOOR TWO");
            for(int n=0;n<Integer.getInteger("route.pre3Wait",100);n++){
                waitOne();
                if(n==85 && Boolean.getBoolean("route.pre3Gnoll")){
                    Mob intercept=(Mob)Actor.findChar(640);assertNotNull(intercept);
                    cast(ShieldOfLight.INSTANCE,intercept.pos);cast(GuidingLight.INSTANCE,intercept.pos);
                    if(intercept.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,intercept.pos);
                    if(intercept.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,intercept.pos);
                    assertFalse(intercept.isAlive());log("FLOOR TWO RECHARGE GNOLL ACTUALLY DEFEATED");
                }
            }
            log("FLOOR TWO PAID RECHARGE BEFORE NEW FLOOR THREE");
        }
        click(724);
        log("SECOND_FLOOR_EXIT");
        assertEquals(3,Dungeon.depth);
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        log("FLOOR_THREE_ENTERED");
        if(System.getProperty("route.checkpointOut")!=null)writeCheckpoint(System.getProperty("route.checkpointOut"));
        if(Boolean.getBoolean("route.stopAfterFloor3")){verifyExpectedEnd();return;}
        thirdFloorSection();
        verifyExpectedEnd();
    }
    private void liveNineFromSeven() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(538,Dungeon.hero.pos);
        System.setProperty("route.stopBeforeCure","true");System.clearProperty("route.nineContinue");
        hourglassAccessSection();log("LIVE JOIN 01 PIT ACCESS");
        eighthFloorHourglassGuard();log("LIVE JOIN 02 GUARD BYPASS");
        eighthFloorHourglassCentral();log("LIVE JOIN 03 CENTRAL POOL");
        eighthFloorFallToNine();log("LIVE JOIN 04 NINE LANDING");
        if(Boolean.getBoolean("route.liveStopAtLanding")){
            if(System.getProperty("route.liveNineOut")!=null)writeCheckpoint(System.getProperty("route.liveNineOut"));return;
        }
        if(Dungeon.hero.pos==944){
            ninthFloorLiveLanding();
            if(System.getProperty("route.liveNineOut")!=null)writeCheckpoint(System.getProperty("route.liveNineOut"));
            return;
        }
        ninthFloorSection();log("LIVE JOIN 05 LANDING DM");
        ninthFloorSection();log("LIVE JOIN 06 PAID RECHARGE");
        System.setProperty("route.nineContinue","true");ninthFloorSection();log("LIVE JOIN 07 GARDEN APPROACH");
        ninthFloorSection();log("LIVE JOIN 08 GARDEN SKELETON");
        ninthFloorSection();log("LIVE JOIN 09 ACTUAL RATION");
        ninthFloorSection();log("LIVE JOIN 10 ACTUAL MEAL");
        System.setProperty("route.nineGuardAttacks","6");ninthFloorSection();log("LIVE JOIN 11 GUARD ATTACKS");
        System.setProperty("route.nineKiteRage","true");ninthFloorSection();log("LIVE JOIN 12 KITE RAGE");
        System.setProperty("route.stopKiteAfterFour","true");ninthFloorSection();log("LIVE JOIN 13 FOUR SAFE MOVES");
        ninthFloorSection();log("LIVE JOIN 14 SAFE CORNER BLESS");
        ninthFloorSection();log("LIVE JOIN 15 DOORWAY");
        ninthFloorSection();log("LIVE JOIN 16 SECRET TRANSIT");
        ninthFloorSection();log("LIVE JOIN 17 FREE MISSILE");
        if(System.getProperty("route.liveNineOut")!=null)writeCheckpoint(System.getProperty("route.liveNineOut"));
    }
    private void thirdFloorSection() throws Exception {
        assertEquals(3,Dungeon.depth);
        if(!Boolean.getBoolean("route.chargeOnTwo")){
            Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class).execute(Dungeon.hero,"EAT");ready();log("EAT SECOND RATION");
        }
        Mob firstBridgeGnoll=(Mob)Actor.findChar(455);assertNotNull(firstBridgeGnoll);
        Mob secondBridgeGnoll=(Mob)Actor.findChar(412);assertNotNull(secondBridgeGnoll);
        for(int n=0;n<Integer.getInteger("route.initial3Recharge",Boolean.getBoolean("route.chargeOnTwo")?0:60);n++)waitOne();
        for(int cell:new int[]{684,683,682,681,680,679,678,677,676,675,675})click(cell);
        assertNotNull("Scroll must actually be picked up",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
        upgradeOwnedSpear();
        Mob frontBridgeGnoll=(Mob)Actor.findChar(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,firstBridgeGnoll.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT).collisionPos);
        assertNotNull("Bridge projectile has no enemy target",frontBridgeGnoll);
        if(!Boolean.getBoolean("route.skipFirstBridgeShield"))cast(ShieldOfLight.INSTANCE,frontBridgeGnoll.pos);
        cast(GuidingLight.INSTANCE,frontBridgeGnoll.pos);
        if(frontBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,frontBridgeGnoll.pos);
        if(frontBridgeGnoll.isAlive() && Boolean.getBoolean("route.clothBridgeFocus")){
            Mob blocker=(Mob)Actor.findChar(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,frontBridgeGnoll.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos);
            assertNotNull(blocker);assertNotSame(frontBridgeGnoll,blocker);assertEquals(2,blocker.HP);cast(GuidingLight.INSTANCE,blocker.pos);
        }
        if(frontBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,frontBridgeGnoll.pos);
        assertFalse("First bridge gnoll not cleared",frontBridgeGnoll.isAlive());
        log("FIRST_BRIDGE_GNOLL_KILLED");
        Mob rearBridgeGnoll=frontBridgeGnoll==firstBridgeGnoll?secondBridgeGnoll:firstBridgeGnoll;
        if(!Boolean.getBoolean("route.clothBridgeFocus") || rearBridgeGnoll.isAlive())cast(GuidingLight.INSTANCE,rearBridgeGnoll.pos);
        if(rearBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,rearBridgeGnoll.pos);
        if(rearBridgeGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,rearBridgeGnoll.pos);
        assertFalse("Second bridge gnoll not cleared",rearBridgeGnoll.isAlive());
        log("BOTH_BRIDGE_GNOLLS_KILLED");
        for(int cell:new int[]{631,631,587,543})click(cell);
        if(Boolean.getBoolean("route.exileDetour")){click(585);assertEquals(585,Dungeon.hero.pos);click(541);assertEquals(541,Dungeon.hero.pos);}else{click(542);click(541);}
        for(int cell:new int[]{541,584,627,626,625,624,623,622,621,620,619,618,575,532,489,446,403})click(cell);
        Dungeon.hero.search(true);ready();log("SEARCH SECRET DOOR 402");
        click(402);log("SECRET_ROOM_ENTRY");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,358);
        log("SECRET_ROOM_RAT_SHOT");
        for(int cell:new int[]{358,357,356,355,354,397,396,439,482,525,526,527,527,528,529,529})click(cell);
        assertNotNull("Levitation must actually be collected",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class));
        log("LEVITATION_ACTUALLY_ACQUIRED");
        for(int cell:new int[]{528,527,526,525,482,439,396,397,398,399,400,401,402,403,446,489,532,575,618,619,620,621,622,623,624,625,626,627,670,671,672,673,674,675}){
            click(cell);
            if(cell==532 && Boolean.getBoolean("route.returnGnoll")){
                Mob intercept=(Mob)Actor.findChar(619);
                if(intercept==null && Boolean.getBoolean("route.clothBridgeFocus")){log("CLOTH ROUTE HAS NO OLD RETURN GNOLL AT 619");continue;}
                assertNotNull(intercept);
                cast(ShieldOfLight.INSTANCE,intercept.pos);waitOne();
                assertEquals(575,intercept.pos);cast(GuidingLight.INSTANCE,intercept.pos);
                if(intercept.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,intercept.pos);
                assertFalse(intercept.isAlive());log("ACTUAL SECRET RETURN GNOLL DEFEATED BEFORE APPROACH");
            }
        }
        Mob vaultGnoll=(Mob)Actor.findChar(804);
        if(vaultGnoll!=null){
            cast(ShieldOfLight.INSTANCE,vaultGnoll.pos);cast(GuidingLight.INSTANCE,vaultGnoll.pos);
            if(vaultGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,vaultGnoll.pos);
            if(vaultGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,vaultGnoll.pos);
            assertFalse("Spawned vault gnoll survived",vaultGnoll.isAlive());
        }else assertTrue("Missing old vault spawn requires new investigated branch",Boolean.getBoolean("route.returnGnoll"));
        for(int cell:new int[]{717,717,760,760,759,758,757,756,798,841,884})click(cell);
        log("CHASM_VAULT_APPROACH");
        assertEquals(841,Dungeon.hero.pos);
        Dungeon.hero.search(true);ready();log("SEARCH SECRET VAULT DOOR 884");
        click(884);assertEquals(884,Dungeon.hero.pos);
        assertTrue("Earned golden key required",com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey(3))>=1);
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK OWNED LEVITATION");
        for(int cell:new int[]{927,970,1012,1012})click(cell);
        if(Boolean.getBoolean("route.clothBridgeFocus"))click(1012);
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff staff=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff.class);
        assertNotNull("Quarterstaff must actually be collected",staff);assertEquals(2,staff.level());assertTrue(staff.STRReq()<=Dungeon.hero.STR());
        staff.execute(Dungeon.hero,"EQUIP");Dungeon.hero.busy();ready();log("EQUIP EARNED QUARTERSTAFF");
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
        if(!Boolean.getBoolean("route.fastAlchemyExit"))for(int n=0;n<10;n++)waitOne();
        click(1237);log("OPEN ALCHEMY EXIT AFTER PAID WAIT");
        if(Boolean.getBoolean("route.fastAlchemyExit")){if(Dungeon.hero.pos!=1237)click(1237);assertEquals(1237,Dungeon.hero.pos);}
        click(1194);click(1151);log("LEAVE ALCHEMY CORRIDOR");
        for(int cell:new int[]{1108,1064,1020,1019,976,933,890,847,846,803,760,759,716,673,672,671,670,627,626,625}){
            click(cell);
            if(cell==760 && Boolean.getBoolean("route.earlySwarm")){
                assertEquals(760,Dungeon.hero.pos);assertEquals(3,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));
                Dungeon.hero.upgradeTalent(Talent.SHIELD_OF_LIGHT);log("EARN SHIELD TWO AT ACTUAL EARLIER LEVEL THREE");
                Mob swarm=(Mob)Actor.findChar(759),gnoll=(Mob)Actor.findChar(803);assertNotNull(swarm);assertNotNull(gnoll);
                cast(ShieldOfLight.INSTANCE,swarm.pos);
                Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class).execute(Dungeon.hero,"DRINK");ready();log("REAL SHIELDING FOR ACTUAL CORRIDOR INTERCEPT");
                for(int n=0;n<4 && gnoll.isAlive();n++)click(gnoll.pos);
                assertFalse(gnoll.isAlive());log("ACTUAL CORRIDOR GNOLL DEFEATED WITH SWARM PRESENT");
                if(Boolean.getBoolean("route.originSwarmContinue")){
                    click(swarm.pos);cast(HolyWard.INSTANCE,null);
                    for(int n=0;n<4 && swarm.isAlive();n++)click(swarm.pos);
                    assertFalse(swarm.isAlive());log("ACTUAL ORIGIN SWARM PARENT DEFEATED");
                    if(Boolean.getBoolean("route.originClearChildren")){
                        Mob small=(Mob)Actor.findChar(759);assertNotNull(small);assertEquals(71,small.id());
                        for(int n=0;n<2 && small.isAlive();n++)click(small.pos);assertFalse(small.isAlive());
                        Mob large=Dungeon.level.mobs.stream().filter(m->m.id()==68).findFirst().get();
                        for(int n=0;n<4 && large.isAlive();n++)click(large.pos);assertFalse(large.isAlive());
                        log("ACTUAL ORIGIN TWO SWARM CHILDREN DEFEATED");
                        if(Boolean.getBoolean("route.originExit")){
                            Mob last=Dungeon.level.mobs.stream().filter(m->m.id()==73).findFirst().get();
                            for(int n=0;n<2 && last.isAlive();n++)click(last.pos);assertFalse(last.isAlive());
                            assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm && m.state==m.HUNTING));
                            for(int next:new int[]{759,716,673,672,671,670,627,626,625,624,623,622,621,620,619,618,660,616,615,614,613,612,611,654}){click(next);assertEquals(next,Dungeon.hero.pos);}
                            if(!Boolean.getBoolean("route.originLatePasty")){
                                Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty.class).execute(Dungeon.hero,"EAT");ready();log("ACTUAL OWNED PASTY BEFORE EXIT RECHARGE");
                            }
                            for(int n=0;n<Integer.getInteger("route.originExitWait",40);n++)waitOne();
                            if(Boolean.getBoolean("route.originLatePasty")){
                                log("ACTUAL PAID PREMEAL RECHARGE HUNGER STILL BELOW STARVATION");
                                Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty.class).execute(Dungeon.hero,"EAT");ready();log("ACTUAL OWNED PASTY AFTER EXIT RECHARGE");
                            }
                            log("ACTUAL LEVEL ONE ORIGIN AT CLOSED CRAB DOOR");
                            if(Boolean.getBoolean("route.originCrabProbe")){
                                click(653);log("ACTUAL ORIGIN CRAB DOOR OPEN");
                                System.out.println("ORIGIN_EXIT_GEOMETRY visible="+Dungeon.level.heroFOV[520]+" collision="+new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,520,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos);
                                for(int source:new int[]{653,652,651,650,649})System.out.println("ORIGIN_BLINK_RAY source="+source+" terrain="+Dungeon.level.map[source]+" collision="+new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(source,520,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos);
                                if(Boolean.getBoolean("route.originCrabSolve")){
                                    for(int next:new int[]{652,651}){click(next);assertEquals(next,Dungeon.hero.pos);}
                                    Mob crab=Dungeon.level.mobs.stream().filter(m->m.id()==51).findFirst().get();assertEquals(650,crab.pos);
                                    cast(ShieldOfLight.INSTANCE,crab.pos);
                                    if(!Boolean.getBoolean("route.originCrabMelee")){
                                        cast(GuidingLight.INSTANCE,crab.pos);
                                        if(crab.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,crab.pos);
                                    }
                                    for(int n=0;n<(Boolean.getBoolean("route.originCrabMelee")?4:2) && crab.isAlive();n++)click(crab.pos);
                                    assertFalse(crab.isAlive());log("ACTUAL ORIGIN CRAB DEFEATED");
                                    for(int next:new int[]{650,649}){click(next);assertEquals(next,Dungeon.hero.pos);}
                                    if(Boolean.getBoolean("route.originShieldPursuer")){
                                        Mob pursuer=Dungeon.level.mobs.stream().filter(m->m.id()==52).findFirst().get();cast(ShieldOfLight.INSTANCE,pursuer.pos);
                                    }
                                    int blinkTarget=Integer.getInteger("route.originBlinkTarget",520);
                                    System.out.println("ACTUAL_ORIGIN_BLINK_GEOMETRY target="+blinkTarget+" terrain="+Dungeon.level.map[blinkTarget]+" collision="+new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,blinkTarget,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos);
                                    throwItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink.class,blinkTarget);assertEquals(blinkTarget,Dungeon.hero.pos);
                                    if(blinkTarget==518)for(int next:(Boolean.getBoolean("route.originShieldPursuer")?new int[]{519,520}:new int[]{476,520})){click(next);assertEquals(next,Dungeon.hero.pos);}
                                    if(blinkTarget==475)for(int next:new int[]{433,477,520}){click(next);assertEquals(next,Dungeon.hero.pos);}
                                    click(520);assertEquals(4,Dungeon.depth);log("ACTUAL LEVEL ONE ORIGIN REACHES FLOOR FOUR");
                                    if(Boolean.getBoolean("route.originContinueFour"))originFourthFloorSection();
                                }
                                if(Boolean.getBoolean("route.originCrabEscape")){
                                    for(int next:new int[]{652,651,650,649}){click(next);assertEquals(next,Dungeon.hero.pos);}
                                    throwItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink.class,520);assertEquals(520,Dungeon.hero.pos);
                                    click(520);assertEquals(4,Dungeon.depth);log("ACTUAL LEVEL ONE ORIGIN REACHES FLOOR FOUR WITHOUT CRAB FIGHT");
                                }
                                if(Boolean.getBoolean("route.originCrabFight")){
                                    Mob crab=Dungeon.level.mobs.stream().filter(m->m.id()==51).findFirst().get();
                                    cast(ShieldOfLight.INSTANCE,crab.pos);cast(GuidingLight.INSTANCE,crab.pos);
                                    if(crab.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,crab.pos);
                                    log("ACTUAL ORIGIN CRAB FIRST RANGED SEQUENCE");
                                }
                            }
                        }
                    }
                }
                if(System.getProperty("route.earlySwarmOut")!=null)writeCheckpoint(System.getProperty("route.earlySwarmOut"));return;
            }
        }
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
        for(int n=0;n<Integer.getInteger("route.exitRecharge",28);n++)waitOne();
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
    private void originFourthFloorSection() throws Exception {
        assertEquals(4,Dungeon.depth);assertEquals(1007,Dungeon.hero.pos);
        for(int n=0;n<Integer.getInteger("route.originFourWait",120);n++){
            waitOne();
            if(n==151 && Boolean.getBoolean("route.originFourInterceptSlime")){
                Mob slime=Dungeon.level.mobs.stream().filter(m->m.id()==89).findFirst().get();assertEquals(928,slime.pos);
                cast(HolyWard.INSTANCE,null);cast(ShieldOfLight.INSTANCE,slime.pos);cast(GuidingLight.INSTANCE,slime.pos);
                for(int hit=0;hit<4 && slime.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,slime.pos));click(slime.pos);}
                assertFalse(slime.isAlive());log("LIVE ORIGIN FOUR RECHARGE SLIME ACTUALLY DEFEATED");
            }
        }
        log("LIVE ORIGIN FOUR PAID RECHARGE NO PASTY RESET");
        for(int c:new int[]{968,929,890,889,888}){click(c);assertEquals(c,Dungeon.hero.pos);}
        log("LIVE ORIGIN FOUR ACTUAL FIRST CRAB APPROACH");
        if(Boolean.getBoolean("route.originFourCrab")){
            Mob crab=Dungeon.level.mobs.stream().filter(m->m.id()==84).findFirst().get();
            if(!Boolean.getBoolean("route.originFourLateWard") && Dungeon.hero.buff(HolyWard.HolyArmBuff.class)==null)cast(HolyWard.INSTANCE,null);
            cast(ShieldOfLight.INSTANCE,crab.pos);if(!Boolean.getBoolean("route.originFourCrabMelee"))cast(GuidingLight.INSTANCE,crab.pos);
            for(int n=0;n<3 && crab.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,crab.pos));click(crab.pos);}
            assertFalse(crab.isAlive());log("LIVE ORIGIN FOUR CRAB DEFEATED");
            for(int cell:new int[]{887,848,809}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
            log("LIVE ORIGIN FOUR ACTUAL SLIME APPROACH");
            if(Boolean.getBoolean("route.originFourGnoll")){
                for(int cell:new int[]{808,846,806}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                Mob gnoll=Dungeon.level.mobs.stream().filter(m->m.id()==85).findFirst().get();
                cast(ShieldOfLight.INSTANCE,gnoll.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,gnoll.pos);
                for(int n=0;n<3 && gnoll.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,gnoll.pos));click(gnoll.pos);}
                assertFalse(gnoll.isAlive());log("LIVE ORIGIN FOUR NORTH GNOLL DEFEATED");
                if(Boolean.getBoolean("route.originFourNorthItems")){
                    click(806);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class));
                    click(767);assertEquals(767,Dungeon.hero.pos);click(767);log("LIVE ORIGIN FOUR MISSILE RECOVERY ATTEMPT NO ASSUMED DURABILITY");
                    for(int cell:new int[]{728,689,650,611,610,609,608,607,606,605,604,603,563,524,484,483,482,481,480,479,478,477,476,475,474}){
                        click(cell);assertEquals(cell,Dungeon.hero.pos);
                        if(cell==610 && Boolean.getBoolean("route.originFourCorridorSlime")){
                            Mob slime=Dungeon.level.mobs.stream().filter(m->m.id()==89).findFirst().get();assertEquals(609,slime.pos);
                            cast(HolyWard.INSTANCE,null);cast(ShieldOfLight.INSTANCE,slime.pos);
                            for(int n=0;n<5 && slime.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,slime.pos));click(slime.pos);}
                            assertFalse(slime.isAlive());log("LIVE ORIGIN FOUR CORRIDOR SLIME DEFEATED");
                        }
                    }
                    click(474);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame.class));
                    log("LIVE ORIGIN FOUR REAL FLAME PICKUP BEFORE SWARM");
                    if(Boolean.getBoolean("route.originFourBossRecharge")){
                        Mob swarm=Dungeon.level.mobs.stream().filter(m->m.id()==83).findFirst().get();cast(ShieldOfLight.INSTANCE,swarm.pos);
                        click(514);assertEquals(514,Dungeon.hero.pos);click(513);if(Dungeon.depth==4){assertEquals(513,Dungeon.hero.pos);click(513);}assertEquals(5,Dungeon.depth);assertEquals(708,Dungeon.hero.pos);
                        log("LIVE ORIGIN TEMPORARY GOO ENTRANCE SHELTER BEFORE SWARM DAMAGE");
                        for(int n=0;n<Integer.getInteger("route.originFiveRecharge",200);n++)waitOne();
                        cast(HolyWard.INSTANCE,null);if(!Boolean.getBoolean("route.originSkipHolyWeapon"))cast(HolyWeapon.INSTANCE,null);
                        click(708);assertEquals(4,Dungeon.depth);
                        swarm=Dungeon.level.mobs.stream().filter(m->m.id()==83).findFirst().get();
                        log("LIVE ORIGIN RETURNS WITH PAID MANA AND OWNED WEAPON BUFF");
                        if(Boolean.getBoolean("route.originFourAfterRefill")){
                            cast(ShieldOfLight.INSTANCE,swarm.pos);if(!Boolean.getBoolean("route.originSkipParentLight"))cast(GuidingLight.INSTANCE,swarm.pos);
                            boolean childProtected=false;
                            for(int n=0;n<4 && swarm.isAlive();n++){
                                assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,swarm.pos));click(swarm.pos);
                                if(!childProtected && Boolean.getBoolean("route.originProtectBigChild")){
                                    java.util.List<Mob> spawned=new java.util.ArrayList<>();for(Mob m:Dungeon.level.mobs)if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm && m!=swarm)spawned.add(m);
                                    if(!spawned.isEmpty()){
                                        assertEquals(1,spawned.size());cast(ShieldOfLight.INSTANCE,spawned.get(0).pos);childProtected=true;
                                    }
                                }
                            }
                            assertFalse(swarm.isAlive());assertEquals(4,Dungeon.hero.lvl);Dungeon.hero.upgradeTalent(Boolean.getBoolean("route.originSatiatedAtFour")?Talent.SATIATED_SPELLS:Talent.SEARING_LIGHT);
                            log("LIVE ORIGIN REFILLED SWARM PARENT CLEARED LEVEL FOUR EARNED");
                            if(Boolean.getBoolean("route.originRefillClearChildren")){
                                Mob big=Dungeon.level.mobs.stream().filter(m->m.id()==(Boolean.getBoolean("route.originSkipParentLight")?100:102)).findFirst().get(),small=Dungeon.level.mobs.stream().filter(m->m.id()==(Boolean.getBoolean("route.originSkipParentLight")?102:104)).findFirst().get();
                                for(int n=0;n<2 && small.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,small.pos));click(small.pos);}assertFalse(small.isAlive());
                                if(charge(Dungeon.hero.belongings.getItem(HolyTome.class))<1)waitOne();
                                cast(ShieldOfLight.INSTANCE,big.pos);
                                for(int n=0;n<3 && big.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,big.pos));click(big.pos);}assertFalse(big.isAlive());
                                log("LIVE ORIGIN REFILL BIG SWARM CHILD CLEARED WITH EARNED SHIELD REFRESH");
                                if(Boolean.getBoolean("route.originRefillFinish")){
                                    Mob last=Dungeon.level.mobs.stream().filter(m->m.id()==(Boolean.getBoolean("route.originSkipParentLight")?104:106)).findFirst().get();
                                    for(int n=0;n<2 && last.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,last.pos));click(last.pos);}assertFalse(last.isAlive());
                                    assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm));
                                    log("LIVE ORIGIN ENTIRE REFILLED SWARM CLEARED");
                                    for(int cell:new int[]{473,472,472,472})click(cell);
                                    assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));
                                    for(int cell:new int[]{512,552,553,554,555,594,633,672,710,709}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                    throwItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame.class,707);
                                    for(int n=0;n<7;n++){
                                        waitOne();
                                        if(n==1 && Boolean.getBoolean("route.originBarricadeSlime")){
                                            Mob slime=Dungeon.level.mobs.stream().filter(m->m.id()==94).findFirst().get();
                                            if(!Dungeon.level.adjacent(Dungeon.hero.pos,slime.pos))waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,slime.pos));
                                            if(Boolean.getBoolean("route.originBarricadeShieldSlime"))cast(ShieldOfLight.INSTANCE,slime.pos);else throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,slime.pos);
                                            for(int hit=0;hit<5 && slime.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,slime.pos));click(slime.pos);}
                                            assertFalse(slime.isAlive());log("LIVE ORIGIN REAL SLIME CLEARED DURING BARRICADE FIRE WAIT");
                                        }
                                    }
                                    assertNotEquals(Terrain.BARRICADE,Dungeon.level.map[746]);
                                    assertEquals(0,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob.volumeAt(746,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire.class));
                                    for(int cell:new int[]{708,707,746,785,784,784})click(cell);
                                    Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);food.execute(Dungeon.hero,"EAT");ready();
                                    log("LIVE ORIGIN REAL BARRICADE FOOD ACQUIRED AND EATEN AFTER WHOLE SWARM");
                                    if(Boolean.getBoolean("route.originFoodSupplies")){
                                        if(!Boolean.getBoolean("route.originDelayMealWard")){cast(HolyWard.INSTANCE,null);assertEquals(3,Dungeon.hero.shielding());log("LIVE ORIGIN EARNED SATIATED SHIELD FROM ACTUAL RATION");}
                                        for(int cell:new int[]{823,822,821,821})click(cell);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
                                        for(int cell:new int[]{860,899,900,901,902,902,903,864,825,826,826})click(cell);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas.class));
                                        log("LIVE ORIGIN ACTUAL QUEST GAS UPGRADE AND GOLD OWNED");
                                        if(Boolean.getBoolean("route.originQuestPurity")){
                                            for(int cell:new int[]{825,824,823,784,785,746,707,708,709,710,672,633,594,555,516,477,437,437,437})click(cell);
                                            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity.class));
                                            log("LIVE ORIGIN QUEST PURITY ACTUALLY OWNED");
                                            if(Boolean.getBoolean("route.originAcceptGhost")){
                                                Mob ghost=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost).findFirst().get();
                                                for(int n=0;n<8 && !com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost.Quest.active();n++)click(ghost.pos);
                                                assertTrue(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost.Quest.active());log("LIVE ORIGIN REAL GHOST QUEST ACCEPTED AFTER PURITY");
                                                if(Boolean.getBoolean("route.originQuestApproach")){
                                                    for(int cell:new int[]{519,518,517,516,555,594,633}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                    log("LIVE ORIGIN QUEST CENTRAL APPROACH ACTUAL ENEMIES");
                                                    if(Boolean.getBoolean("route.originQuestCrabIntercept")){
                                                        Mob questPursuer=Dungeon.level.mobs.stream().filter(m->m.id()==107).findFirst().get();
                                                        if(Boolean.getBoolean("route.originDelayMealWard")){cast(HolyWard.INSTANCE,null);assertEquals(3,Dungeon.hero.shielding());}
                                                        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,questPursuer.pos);
                                                        if(questPursuer.isAlive())cast(ShieldOfLight.INSTANCE,questPursuer.pos);
                                                        for(int n=0;n<4 && questPursuer.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,questPursuer.pos));click(questPursuer.pos);}
                                                        assertFalse(questPursuer.isAlive());log("LIVE ORIGIN QUEST PURSUING CRAB ACTUALLY CLEARED");
                                                        if(Boolean.getBoolean("route.originQuestEast")){
                                                            for(int cell:new int[]{594,594,633,672,673,673,674,675,676}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                            log("LIVE ORIGIN EAST QUEST APPROACH ACTUAL LOOT");
                                                            if(Boolean.getBoolean("route.originQuestRageEscape")){
                                                                Item rage=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class);assertNotNull(rage);rage.execute(Dungeon.hero,"READ");ready();
                                                                for(int cell:new int[]{716,756,795,834,873,912}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                log("LIVE ORIGIN QUEST EAST ESCAPE AFTER ACTUAL RAGE");
                                                                if(Boolean.getBoolean("route.originQuestSouth")){
                                                                    Dungeon.hero.search(true);ready();assertEquals(Terrain.DOOR,Dungeon.level.map[913]);
                                                                    for(int cell:new int[]{913,953,993}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                    if(Boolean.getBoolean("route.originQuestGnoll")){
                                                                        Mob southernGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==87).findFirst().get();assertTrue(Dungeon.level.heroFOV[southernGnoll.pos]);
                                                                        if(!Boolean.getBoolean("route.originGnollSpear"))cast(ShieldOfLight.INSTANCE,southernGnoll.pos);
                                                                        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,southernGnoll.pos);
                                                                        if(Boolean.getBoolean("route.originGnollSpear") && southernGnoll.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,southernGnoll.pos);
                                                                        for(int n=0;n<3 && southernGnoll.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,southernGnoll.pos));click(southernGnoll.pos);}
                                                                        assertFalse(southernGnoll.isAlive());log("LIVE ORIGIN SOUTH GNOLL INTERCEPTED BEFORE COLD CORNER");
                                                                        if(Boolean.getBoolean("route.originRecoverGnollLoot")){
                                                                            Mob chasingChild=Dungeon.level.mobs.stream().filter(m->m.id()==120).findFirst().get();cast(ShieldOfLight.INSTANCE,chasingChild.pos);
                                                                            Mob chasingParent=Dungeon.level.mobs.stream().filter(m->m.id()==110).findFirst().get();
                                                                            for(int n=0;n<2 && chasingParent.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,chasingParent.pos));click(chasingParent.pos);}assertFalse(chasingParent.isAlive());
                                                                            for(int n=0;n<3 && chasingChild.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,chasingChild.pos));click(chasingChild.pos);}assertFalse(chasingChild.isAlive());
                                                                            for(int n=0;n<5;n++)click(953);
                                                                            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));log("LIVE ORIGIN GNOLL LOOT AND CHASING SWARMS ACTUALLY CLEARED");
                                                                        }

                                                                    }
                                                                    if(Boolean.getBoolean("route.originRecoverGnollLoot"))click(993);
                                                                    for(int cell:new int[]{1032,1072,1073,1074,1035}){click(cell);assertEquals(cell,Dungeon.hero.pos);if(cell==1032 && Boolean.getBoolean("route.originGnollSpear"))click(1032);}
                                                                    log("LIVE ORIGIN SOUTH QUEST ENEMIES ACTUAL APPROACH");
                                                                    if(Boolean.getBoolean("route.originColdLoop")){
                                                                        Item rawLoop=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat.class);assertNotNull(rawLoop);rawLoop.execute(Dungeon.hero,"DROP");ready();
                                                                        Item purityLoop=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity.class);assertNotNull(purityLoop);purityLoop.execute(Dungeon.hero,"DRINK");ready();
                                                                        for(int cell:new int[]{996,958,918,917}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                        log("LIVE ORIGIN REAL PURITY COLD LOOP AND OWNED MEAT");
                                                                        if(Boolean.getBoolean("route.originColdPursuers")){
                                                                            Mob coldCrab=Dungeon.level.mobs.stream().filter(m->m.id()==82).findFirst().get();Mob coldSlime=Dungeon.level.mobs.stream().filter(m->m.id()==94).findFirst().get();
                                                                            if(Boolean.getBoolean("route.originColdShieldFirst"))cast(ShieldOfLight.INSTANCE,coldSlime.pos);
                                                                            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,coldCrab.pos);
                                                                            if(coldCrab.isAlive() && !Boolean.getBoolean("route.originColdNoSpear"))throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,coldCrab.pos);
                                                                            for(int n=0;n<2 && coldCrab.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,coldCrab.pos));click(coldCrab.pos);}assertFalse(coldCrab.isAlive());
                                                                            if(!Boolean.getBoolean("route.originColdShieldFirst"))cast(ShieldOfLight.INSTANCE,coldSlime.pos);
                                                                            for(int n=0;n<5 && coldSlime.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,coldSlime.pos));click(coldSlime.pos);}assertFalse(coldSlime.isAlive());
                                                                            log("LIVE ORIGIN BOTH REAL COLD CORRIDOR PURSUERS DEFEATED");
                                                                            if(Boolean.getBoolean("route.originColdFoodPickup")){
                                                                                for(int cell:new int[]{957,957,957,957}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                Item coldFood=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class);assertNotNull(coldFood);coldFood.execute(Dungeon.hero,"EAT");ready();
                                                                                if(Boolean.getBoolean("route.originColdNorthExit")){
                                                                                    for(int cell:new int[]{918,917,916}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                    log("LIVE ORIGIN FED NORTH EXIT SECOND MEAT LEFT ON ACTUAL GROUND");
                                                                                    if(Boolean.getBoolean("route.originColdRing")){
                                                                                        int[] ring={917,957,996,1035,1073,1072,1032,993,954,916};
                                                                                        for(int n=0;n<Integer.getInteger("route.originColdRingSteps",25);n++){
                                                                                            int cell=ring[n%ring.length];click(cell);
                                                                                            if(n==6 && Boolean.getBoolean("route.originRingSlime")){
                                                                                                Mob ringSlime=Dungeon.level.mobs.stream().filter(m->m.id()==81).findFirst().get();assertTrue(Dungeon.level.heroFOV[ringSlime.pos]);
                                                                                                throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,ringSlime.pos);
                                                                                                if(ringSlime.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,ringSlime.pos);
                                                                                                for(int hit=0;hit<3 && ringSlime.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,ringSlime.pos));click(ringSlime.pos);}assertFalse(ringSlime.isAlive());
                                                                                                click(cell);
                                                                                            }
                                                                                            assertEquals(cell,Dungeon.hero.pos);
                                                                                        }
                                                                                        log("LIVE ORIGIN COLD ROOM FIXED LEGAL KITE RESOURCE BOUNDARY");
                                                                                        if(Boolean.getBoolean("route.originRingCrab")){
                                                                                            Mob ringCrab=Dungeon.level.mobs.stream().filter(m->m.id()==132).findFirst().get();cast(ShieldOfLight.INSTANCE,ringCrab.pos);assertEquals(3,Dungeon.hero.shielding());
                                                                                            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,ringCrab.pos);
                                                                                            if(ringCrab.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,ringCrab.pos);
                                                                                            for(int hit=0;hit<3 && ringCrab.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,ringCrab.pos));click(ringCrab.pos);}assertFalse(ringCrab.isAlive());
                                                                                            assertEquals(5,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));Dungeon.hero.upgradeTalent(Talent.SEARING_LIGHT);
                                                                                            log("LIVE ORIGIN LEVEL FIVE ACTUALLY EARNED AFTER RING CRAB");
                                                                                            if(Boolean.getBoolean("route.originFiveBacktrack")){
                                                                                                if(Boolean.getBoolean("route.originFiveClothUpgrade"))upgradeOwnedSpear();
                                                                                                for(int cell:new int[]{1113,1113,1073,1035,1075,1115,1116,1117,1118,1158,1159,1160,1122,1084,1045,1007}){
                                                                                                    if(cell==1084 && Boolean.getBoolean("route.originFiveExitCrab")){
                                                                                                        Mob exitCrab=Dungeon.level.mobs.stream().filter(m->m.id()==141).findFirst().get();assertEquals(1084,exitCrab.pos);
                                                                                                        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,exitCrab.pos);
                                                                                                        if(exitCrab.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,exitCrab.pos);
                                                                                                        for(int hit=0;hit<3 && exitCrab.isAlive();hit++)click(exitCrab.pos);assertFalse(exitCrab.isAlive());
                                                                                                    }
                                                                                                    if(cell==1007 && Boolean.getBoolean("route.originFiveBacktrackRecharge")){
                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackMealWard")){Item meal=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class);assertNotNull(meal);meal.execute(Dungeon.hero,"EAT");ready();}
                                                                                                        for(int n=0;n<Integer.getInteger("route.originFiveBacktrackWait",14);n++)waitOne();
                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackMealWard"))cast(HolyWard.INSTANCE,null);
                                                                                                    }
                                                                                                    click(cell);assertEquals(cell==1007?520:cell,Dungeon.hero.pos);
                                                                                                }
                                                                                                log("LIVE ORIGIN LEVEL FIVE ACTUAL RETURN TO FLOOR FOUR ENTRANCE");
                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackFood")){
                                                                                                    assertEquals(3,Dungeon.depth);Mob returnSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==52).findFirst().get();cast(ShieldOfLight.INSTANCE,returnSwarm.pos);
                                                                                                    for(int cell:new int[]{563,606,605,604,604}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                    assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class));
                                                                                                    log("LIVE ORIGIN LEVEL FIVE ACTUAL BACKTRACK FOOD ACQUISITION");
                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackCrab")){
                                                                                                        Mob backtrackCrab=Dungeon.level.mobs.stream().filter(m->m.id()==76).findFirst().get();assertEquals(649,backtrackCrab.pos);
                                                                                                        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,backtrackCrab.pos);
                                                                                                        if(backtrackCrab.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,backtrackCrab.pos);
                                                                                                        for(int hit=0;hit<4 && backtrackCrab.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,backtrackCrab.pos));click(backtrackCrab.pos);}assertFalse(backtrackCrab.isAlive());
                                                                                                        log("LIVE ORIGIN LEVEL FIVE BACKTRACK CRAB ACTUALLY DEFEATED");
                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackPurity")){
                                                                                                            for(int cell:new int[]{561,519,477,435,393,350,307,264,221,178,135,93,137,181,225,226,227,271}){
                                                                                                                if(cell==435 && Boolean.getBoolean("route.originFiveBacktrackIntuitionStone")){click(434);assertEquals(434,Dungeon.hero.pos);click(434);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition.class));log("LIVE ORIGIN ACTUAL INTUITION STONE434 ACQUIRED");}
                                                                                                                assertNull(Dungeon.level.traps.get(cell));click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);
                                                                                                            }
                                                                                                            click(271);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity.class));
                                                                                                            log("LIVE ORIGIN LEVEL FIVE ACTUAL BACKTRACK PURITY ACQUIRED");
                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackHealing")){
                                                                                                                for(int cell:new int[]{314,358,402,446,489,532,575,619,620,621,622,623,624,625,626,584,628,672,716,758,802}){assertNull(Dungeon.level.traps.get(cell));click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackCleansingGnoll")){
                                                                                                                    Mob stoneGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==75).findFirst().get();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,stoneGnoll.pos));cast(ShieldOfLight.INSTANCE,stoneGnoll.pos);
                                                                                                                    for(int hit=0;hit<4 && stoneGnoll.isAlive();hit++)click(stoneGnoll.pos);assertFalse(stoneGnoll.isAlive());log("LIVE ORIGIN ACTUAL STONE DETOUR PURSUING GNOLL DEFEATED");
                                                                                                                }
                                                                                                                click(802);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));log("LIVE ORIGIN LEVEL FIVE ACTUAL BACKTRACK HEALING ACQUIRED");
                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackAlchemy")){
                                                                                                                    Item gas=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas.class);assertNotNull(gas);assertTrue(com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem.selector.itemSelectable(gas));int oldEnergy=Dungeon.energy,earnedEnergy=gas.energyVal();
                                                                                                                    com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem.energizeAll(gas);ready();assertEquals(oldEnergy+earnedEnergy,Dungeon.energy);assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas.class));log("ENERGIZE ACTUALLY OWNED GAS USING ORIGINAL INVENTORY ACTION");
                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackIntuitionStone")){
                                                                                                                        Item intuition=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition.class);assertNotNull(intuition);assertTrue(com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem.selector.itemSelectable(intuition));int beforeIntuition=Dungeon.energy,intuitionEnergy=intuition.energyVal();com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem.energizeAll(intuition);ready();assertEquals(beforeIntuition+intuitionEnergy,Dungeon.energy);assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition.class));log("ENERGIZE ACTUALLY ACQUIRED INTUITION STONE energy="+intuitionEnergy);
                                                                                                                    }
                                                                                                                    if(!Boolean.getBoolean("route.originFiveBacktrackCleansingGnoll"))cast(HolyWard.INSTANCE,null);
                                                                                                                    for(int cell:new int[]{803,847,890,932,976,1020,1064,1108,1152,1195,1237,1280,1323,1366,1409,1453}){assertNull(Dungeon.level.traps.get(cell));click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                    click(1495);craftShieldingAtActualPot();log("LIVE ORIGIN LEVEL FIVE SECOND ACTUAL SHIELDING POTION CRAFTED");
                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackCleansing")){
                                                                                                                        click(1495);craftExoticPotionAtActualPot(1495,com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity.class,com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing.class);log("LIVE ORIGIN ACTUAL CLEANSING FOR HUNGER CRAFTED FROM ACQUIRED PURITY");
                                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackCleansingSwarm")){
                                                                                                                            Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class).execute(Dungeon.hero,"DRINK");ready();
                                                                                                                            for(int cell:new int[]{1497,1498,1455,1412,1411,1410,1452}){click(cell);assertEquals(cell,Dungeon.hero.pos);}cast(HolyWard.INSTANCE,null);
                                                                                                                            Mob cleanseSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==52).findFirst().get();for(int hit=0;hit<8 && cleanseSwarm.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,cleanseSwarm.pos));click(cleanseSwarm.pos);}assertFalse(cleanseSwarm.isAlive());log("LIVE ORIGIN CLEANSING OWNED REAL SWARM PARENT DEFEATED AFTER PAID RECHARGE LOOP");
                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackCleansingChildren")){
                                                                                                                                for(int id:new int[]{156,154}){Mob child=Dungeon.level.mobs.stream().filter(m->m.id()==id).findFirst().get();for(int hit=0;hit<8 && child.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,child.pos));click(child.pos);}assertFalse(child.isAlive());}
                                                                                                                                log("LIVE ORIGIN CLEANSING OWNED FIRST TWO SWARM CHILDREN DEFEATED");
                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackCleansingFinish")){
                                                                                                                                    Mob cleansingLastChild=Dungeon.level.mobs.stream().filter(m->m.id()==158).findFirst().get();for(int hit=0;hit<8 && cleansingLastChild.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,cleansingLastChild.pos));click(cleansingLastChild.pos);}assertFalse(cleansingLastChild.isAlive());log("LIVE ORIGIN CLEANSING OWNED WHOLE SWARM FAMILY DEFEATED");
                                                                                                                                    for(int n=0;n<120;n++){if(Dungeon.level.mobs.stream().anyMatch(m->Dungeon.level.heroFOV[m.pos] && !(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost) && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}log("LIVE ORIGIN CLEANSING PAID CAMP ENDS AT HOSTILE OR 120 TURN BOUND");
                                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackCleansingNorth")){
                                                                                                                                        for(int n=0;n<Integer.getInteger("route.originFiveBacktrackCleansingRestLimit",100) && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<5;n++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}log("LIVE ORIGIN CLEANSING ADDITIONAL PAID REST");
                                                                                                                                        Item cleansingRation=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(cleansingRation);cleansingRation.execute(Dungeon.hero,"EAT");ready();cast(HolyWard.INSTANCE,null);
                                                                                                                                        for(int cell:new int[]{1409,1366,1323,1280,1237,1195,1152,1108,1064,1020}){assertNull(Dungeon.level.traps.get(cell));click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}log("LIVE ORIGIN CLEANSING ACTUAL NORTH CORRIDOR APPROACH");
                                                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackCleansingSpears")){
                                                                                                                                            for(int cell:new int[]{976,932,890,847,803,802,758,716,672,628,584,585,586,586,587,587,587}){assertNull(Dungeon.level.traps.get(cell));
                                                                                                                                                if(cell==586){Mob spearGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==161).findFirst().orElse(null);if(spearGnoll!=null && spearGnoll.isAlive() && Dungeon.level.adjacent(Dungeon.hero.pos,spearGnoll.pos)){cast(ShieldOfLight.INSTANCE,spearGnoll.pos);cast(GuidingLight.INSTANCE,spearGnoll.pos);for(int hit=0;hit<8 && spearGnoll.isAlive();hit++)click(spearGnoll.pos);assertFalse(spearGnoll.isAlive());log("LIVE ORIGIN CLEANSING ACTUAL SPEAR GUARDING GNOLL DEFEATED");}}
                                                                                                                                                if(cell==847){Mob northGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==160).findFirst().orElse(null);if(northGnoll!=null && northGnoll.isAlive() && Dungeon.level.adjacent(Dungeon.hero.pos,northGnoll.pos)){cast(GuidingLight.INSTANCE,northGnoll.pos);if(northGnoll.isAlive()){cast(ShieldOfLight.INSTANCE,northGnoll.pos);for(int hit=0;hit<8 && northGnoll.isAlive();hit++)click(northGnoll.pos);}assertFalse(northGnoll.isAlive());log("LIVE ORIGIN CLEANSING ACTUAL NORTH GNOLL DEFEATED");}}
                                                                                                                                                if(cell==672){Mob northSnake=Dungeon.level.mobs.stream().filter(m->m.id()==150).findFirst().orElse(null);if(northSnake!=null && northSnake.isAlive() && Dungeon.level.adjacent(Dungeon.hero.pos,northSnake.pos)){cast(ShieldOfLight.INSTANCE,northSnake.pos);for(int hit=0;hit<18 && northSnake.isAlive();hit++)click(northSnake.pos);assertFalse(northSnake.isAlive());log("LIVE ORIGIN CLEANSING ACTUAL NORTH SNAKE DEFEATED");}}
                                                                                                                                                click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}Item cleansingSpears=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class);assertNotNull(cleansingSpears);assertEquals(2,cleansingSpears.quantity());assertEquals(3,cleansingSpears.level());log("LIVE ORIGIN CLEANSING TWO REAL UPGRADED SPEARS RECOVERED");
                                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackCleansingSpearRest")){
                                                                                                                                                for(int spearRest=0;spearRest<80 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<2;spearRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}log("LIVE ORIGIN CLEANSING PAID SPEAR ALCOVE REST BOUNDARY");
                                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackCleansingSpearGnoll")){
                                                                                                                                                    Mob spearCampGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==160).findFirst().get();for(int shot=0;shot<2 && spearCampGnoll.isAlive();shot++){assertEquals(spearCampGnoll.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,spearCampGnoll.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,spearCampGnoll.pos);}assertFalse(spearCampGnoll.isAlive());log("LIVE ORIGIN CLEANSING SPEARS ACTUALLY DEFEAT REST GNOLL");
                                                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackCleansingNorthRest")){
                                                                                                                                                        for(int spearPickupCell:new int[]{543,499,455,412,412}){assertNull(Dungeon.level.traps.get(spearPickupCell));click(spearPickupCell);if(Dungeon.hero.pos!=spearPickupCell)click(spearPickupCell);assertEquals(spearPickupCell,Dungeon.hero.pos);}assertEquals(2,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class).quantity());log("LIVE ORIGIN CLEANSING ACTUAL SPEARS RECOVERED FROM DEFEATED NORTH GNOLL");
                                                                                                                                                        for(int northRest=0;northRest<80 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<2;northRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}log("LIVE ORIGIN CLEANSING PAID NORTH ROOM REST BOUNDARY");
                                                                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackCleansingNorthLoop")){
                                                                                                                                                            for(int northLoopCell:new int[]{369,325,324,323,322,321,320,319,275,274,273,229,271}){assertNull(Dungeon.level.traps.get(northLoopCell));click(northLoopCell);if(Dungeon.hero.pos!=northLoopCell)click(northLoopCell);assertEquals(northLoopCell,Dungeon.hero.pos);}waitOne();waitOne();log("LIVE ORIGIN CLEANSING ACTUAL NORTH ROOM RECHARGE DETOUR");
                                                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackCleansingPurityRest")){
                                                                                                                                                                for(int purityRoomRest=0;purityRoomRest<180 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<5;purityRoomRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}log("LIVE ORIGIN CLEANSING ACTUAL PURITY ROOM PAID REST BOUNDARY");
                                                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackCleansingPurityRat")){
                                                                                                                                                                    Mob purityRat=Dungeon.level.mobs.stream().filter(m->m.id()==162).findFirst().get();for(int purityRatShot=0;purityRatShot<2 && purityRat.isAlive();purityRatShot++){assertEquals(purityRat.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,purityRat.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,purityRat.pos);}assertFalse(purityRat.isAlive());log("LIVE ORIGIN CLEANSING ACTUAL PURITY ROOM RAT DEFEATED AT RANGE");
                                                                                                                                                                    for(int secondPurityRest=0;secondPurityRest<160 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<5;secondPurityRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}log("LIVE ORIGIN CLEANSING ACTUAL SECOND PURITY ROOM PAID REST BOUNDARY");
                                                                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackCleansingPurityCrab")){
                                                                                                                                                                        Mob purityCrab=Dungeon.level.mobs.stream().filter(m->m.id()==172).findFirst().get();cast(GuidingLight.INSTANCE,purityCrab.pos);if(purityCrab.isAlive()){assertEquals(purityCrab.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,purityCrab.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,purityCrab.pos);}assertFalse(purityCrab.isAlive());log("LIVE ORIGIN CLEANSING ACTUAL NORTH ROOM RESPAWN CRAB DEFEATED");
                                                                                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackCleansingRingCamp")){
                                                                                                                                                                            for(int ringCampCell:new int[]{228,185,142,141,140,139,138,137}){assertNull(Dungeon.level.traps.get(ringCampCell));click(ringCampCell);if(Dungeon.hero.pos!=ringCampCell)click(ringCampCell);assertEquals(ringCampCell,Dungeon.hero.pos);}log("LIVE ORIGIN CLEANSING ACTUAL RETREAT BEHIND RING ROOM CENTRAL WALL");
                                                                                                                                                                            for(int ringCampWait=0;ringCampWait<180 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<5;ringCampWait++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}log("LIVE ORIGIN CLEANSING ACTUAL RING ROOM PAID CAMP BOUNDARY");
                                                                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackCleansingRingEscape")){
                                                                                                                                                                                Mob ringPursuingSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==166).findFirst().get();for(int ringEscapeCell:new int[]{180,223,266,310,311,312,313,314,315}){if(ringEscapeCell==310){Mob ringSnake=Dungeon.level.mobs.stream().filter(m->m.id()==175).findFirst().get();cast(GuidingLight.INSTANCE,ringSnake.pos);assertFalse(ringSnake.isAlive());}if(ringEscapeCell==315 && !Boolean.getBoolean("route.originFourCleansingSkipRingShield"))cast(ShieldOfLight.INSTANCE,ringPursuingSwarm.pos);assertNull(Dungeon.level.traps.get(ringEscapeCell));click(ringEscapeCell);if(Dungeon.hero.pos!=ringEscapeCell)click(ringEscapeCell);assertEquals(ringEscapeCell,Dungeon.hero.pos);}click(315);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));log("LIVE ORIGIN CLEANSING ACTUAL RING CIRCLE AND FIRST SPEAR RECOVERY");
                                                                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackCleansingReturnFour")){
                                                                                                                                                                                    for(int cleansingExitCell:new int[]{272,273,274,275,319,320,321,322,323,324,325,369,412,455,498,541,584,626,625,624,623,622,621,620,619,618,660,616,615,614,613,612,611,653,652,651,650,606,562}){assertNull(Dungeon.level.traps.get(cleansingExitCell));click(cleansingExitCell);if(Dungeon.hero.pos!=cleansingExitCell)click(cleansingExitCell);assertEquals(cleansingExitCell,Dungeon.hero.pos);}if(!Boolean.getBoolean("route.originFourCleansingSkipStairShield"))cast(ShieldOfLight.INSTANCE,ringPursuingSwarm.pos);click(520);if(Dungeon.depth==3)click(520);assertEquals(4,Dungeon.depth);assertEquals(1007,Dungeon.hero.pos);log("LIVE ORIGIN CLEANSING ACTUAL FRESH START THROUGH RING DETOUR RETURN TO FOUR");
                                                                                                                                                                                    if(Boolean.getBoolean("route.originFourCleansingEntranceRest")){
                                                                                                                                                                                        for(int fourEntranceRest=0;fourEntranceRest<100 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;fourEntranceRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}log("LIVE ORIGIN CLEANSING ACTUAL FLOOR FOUR ENTRANCE PAID REST BOUNDARY");
                                                                                                                                                                                        if(Boolean.getBoolean("route.originFourCleansingEntranceSlime")){
                                                                                                                                                                                            Mob entranceSlime=Dungeon.level.mobs.stream().filter(m->m.id()==144).findFirst().get();if(!Boolean.getBoolean("route.originFourCleansingEntranceSlimeShield"))cast(GuidingLight.INSTANCE,entranceSlime.pos);assertEquals(entranceSlime.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,entranceSlime.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,entranceSlime.pos);
                                                                                                                                                                                            if(Boolean.getBoolean("route.originFourCleansingEntranceSlimeShield") && entranceSlime.isAlive()){cast(ShieldOfLight.INSTANCE,entranceSlime.pos);for(int slimeWait=0;slimeWait<3 && !Dungeon.level.adjacent(Dungeon.hero.pos,entranceSlime.pos);slimeWait++)waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,entranceSlime.pos));for(int slimeHit=0;slimeHit<8 && entranceSlime.isAlive();slimeHit++)click(entranceSlime.pos);}assertFalse(entranceSlime.isAlive());assertEquals(6,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));Dungeon.hero.upgradeTalent(Talent.SEARING_LIGHT);log("LIVE ORIGIN CLEANSING ACTUAL LEVEL SIX EARNED BY ENTRANCE SLIME");
                                                                                                                                                                                            if(Boolean.getBoolean("route.originFourCleansingSixSpearRest")){
                                                                                                                                                                                                click(968);assertEquals(968,Dungeon.hero.pos);click(968);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));log("LIVE ORIGIN CLEANSING ACTUAL LEVEL SIX SPEAR RECOVERED");
                                                                                                                                                                                                for(int levelSixRest=0;levelSixRest<80 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<2;levelSixRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}log("LIVE ORIGIN CLEANSING ACTUAL LEVEL SIX PAID ENTRANCE REST BOUNDARY");
                                                                                                                                                                                                if(Boolean.getBoolean("route.originFourCleansingSecondSlime")){
                                                                                                                                                                                                    for(int secondSlimeRetreat:new int[]{969,970,971}){assertNull(Dungeon.level.traps.get(secondSlimeRetreat));click(secondSlimeRetreat);if(Dungeon.hero.pos!=secondSlimeRetreat)click(secondSlimeRetreat);assertEquals(secondSlimeRetreat,Dungeon.hero.pos);}Mob secondEntranceSlime=Dungeon.level.mobs.stream().filter(m->m.id()==86).findFirst().get();throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,secondEntranceSlime.pos);cast(ShieldOfLight.INSTANCE,secondEntranceSlime.pos);for(int secondSlimeWait=0;secondSlimeWait<3 && !Dungeon.level.adjacent(Dungeon.hero.pos,secondEntranceSlime.pos);secondSlimeWait++)waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,secondEntranceSlime.pos));for(int secondSlimeHit=0;secondSlimeHit<8 && secondEntranceSlime.isAlive();secondSlimeHit++)click(secondEntranceSlime.pos);assertFalse(secondEntranceSlime.isAlive());log("LIVE ORIGIN CLEANSING ACTUAL SECOND FLOOR FOUR SLIME DEFEATED AFTER THREE PAID RETREATS AND SPEAR RECHARGE");
                                                                                                                                                                                                    if(Boolean.getBoolean("route.originFourCleansingNorthDoor")){
                                                                                                                                                                                                        for(int freshNorthCell:new int[]{970,970,969,968,928,888}){assertNull(Dungeon.level.traps.get(freshNorthCell));click(freshNorthCell);if(Dungeon.hero.pos!=freshNorthCell)click(freshNorthCell);assertEquals(freshNorthCell,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));log("LIVE ORIGIN CLEANSING ACTUAL LEVEL SIX NORTH DOOR RESOURCE FRONTIER");
                                                                                                                                                                                                        if(Boolean.getBoolean("route.originFourCleansingNorthCross")){
                                                                                                                                                                                                            Mob northGreatCrab=Dungeon.level.mobs.stream().filter(m->m.id()==109).findFirst().get();assertTrue(Dungeon.level.heroFOV[northGreatCrab.pos]);if(!Boolean.getBoolean("route.originFourCleansingSkipNorthShield"))cast(ShieldOfLight.INSTANCE,northGreatCrab.pos);for(int northCrossCell:new int[]{848,808,769,730,691,651,611}){assertNull(Dungeon.level.traps.get(northCrossCell));click(northCrossCell);if(Dungeon.hero.pos!=northCrossCell)click(northCrossCell);assertEquals(northCrossCell,Dungeon.hero.pos);}log("LIVE ORIGIN CLEANSING ACTUAL GREAT CRAB NORTHWARD PASSAGE RESOURCE FRONTIER");
                                                                                                                                                                                                            if(Boolean.getBoolean("route.originFourCleansingCorridorSwarm")){
                                                                                                                                                                                                                for(int corridorSwarmCell:new int[]{610,609}){click(corridorSwarmCell);if(Dungeon.hero.pos!=corridorSwarmCell)click(corridorSwarmCell);assertEquals(corridorSwarmCell,Dungeon.hero.pos);}Mob corridorSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==118).findFirst().get();assertTrue(Dungeon.level.heroFOV[corridorSwarm.pos]);cast(GuidingLight.INSTANCE,corridorSwarm.pos);if(corridorSwarm.isAlive()){assertEquals(corridorSwarm.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,corridorSwarm.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,corridorSwarm.pos);}log("LIVE ORIGIN CLEANSING ACTUAL CORRIDOR SWARM LIGHT AND SPEAR EXCHANGES");if(Boolean.getBoolean("route.freshGooContinue"))freshGooContinuousSection();
                                                                                                                                                                                                            }
                                                                                                                                                                                                        }
                                                                                                                                                                                                    }
                                                                                                                                                                                                }
                                                                                                                                                                                            }
                                                                                                                                                                                        }
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackFightSwarm")){
                                                                                                                        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class).execute(Dungeon.hero,"DRINK");ready();
                                                                                                                        Mob potSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==52).findFirst().get();
                                                                                                                        for(int hit=0;hit<8 && potSwarm.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,potSwarm.pos));click(potSwarm.pos);}
                                                                                                                        log("LIVE ORIGIN LEVEL FIVE BACKTRACK SWARM ACTUAL FIRST EXCHANGES");
                                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackSwarmChildren")){
                                                                                                                            for(int id:new int[]{154,152}){Mob child=Dungeon.level.mobs.stream().filter(m->m.id()==id).findFirst().get();for(int hit=0;hit<8 && child.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,child.pos));click(child.pos);}assertFalse(child.isAlive());}
                                                                                                                            log("LIVE ORIGIN LEVEL FIVE BACKTRACK SWARM FIRST TWO CHILDREN DEFEATED");
                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackSwarmFinish")){
                                                                                                                                Mob finalChild=Dungeon.level.mobs.stream().filter(m->m.id()==156).findFirst().get();for(int hit=0;hit<5 && finalChild.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,finalChild.pos));click(finalChild.pos);}assertFalse(finalChild.isAlive());
                                                                                                                                log("LIVE ORIGIN LEVEL FIVE BACKTRACK FINAL KNOWN SWARM CHILD DEFEATED");
                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackCamp")){
                                                                                                                                    assertTrue(Dungeon.level.mobs.stream().noneMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm));
                                                                                                                                    for(int n=0;n<80;n++){if(Dungeon.level.mobs.stream().anyMatch(m->Dungeon.level.heroFOV[m.pos] && !(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost) && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}
                                                                                                                                    log("LIVE ORIGIN LEVEL FIVE BACKTRACK PAID CAMP ENDS AT FIRST HOSTILE");
                                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackSnake")){
                                                                                                                                        waitOne();Mob campSnake=Dungeon.level.mobs.stream().filter(m->m.id()==150).findFirst().get();if(!Boolean.getBoolean("route.originFiveBacktrackNoSnakeGuide"))cast(GuidingLight.INSTANCE,campSnake.pos);
                                                                                                                                        for(int n=0;n<8 && campSnake.isAlive() && !Dungeon.level.adjacent(Dungeon.hero.pos,campSnake.pos);n++)waitOne();
                                                                                                                                        for(int hit=0;hit<(Boolean.getBoolean("route.originFiveBacktrackNoSnakeGuide")?18:6) && campSnake.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,campSnake.pos));click(campSnake.pos);}assertFalse(campSnake.isAlive());
                                                                                                                                        log("LIVE ORIGIN LEVEL FIVE BACKTRACK ACTUAL FIRST CAMP SNAKE DEFEATED");
                                                                                                                                        for(int n=0;n<60;n++){if(Dungeon.level.mobs.stream().anyMatch(m->Dungeon.level.heroFOV[m.pos] && !(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost) && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}
                                                                                                                                        log("LIVE ORIGIN LEVEL FIVE BACKTRACK SECOND PAID CAMP BOUNDARY");
                                                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackGnoll")){
                                                                                                                                            Mob campGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==75).findFirst().get();for(int n=0;n<8 && !Dungeon.level.adjacent(Dungeon.hero.pos,campGnoll.pos);n++)waitOne();
                                                                                                                                            for(int hit=0;hit<6 && campGnoll.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,campGnoll.pos));click(campGnoll.pos);}assertFalse(campGnoll.isAlive());
                                                                                                                                            log("LIVE ORIGIN LEVEL FIVE BACKTRACK ACTUAL CAMP GNOLL DEFEATED");
                                                                                                                                            for(int n=0;n<80;n++){if(Dungeon.level.mobs.stream().anyMatch(m->Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}
                                                                                                                                            log("LIVE ORIGIN LEVEL FIVE BACKTRACK THIRD PAID CAMP BOUNDARY");
                                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackFullRest")){
                                                                                                                                                for(int n=0;n<80 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<5;n++){if(Dungeon.level.mobs.stream().anyMatch(m->Dungeon.level.heroFOV[m.pos] && !m.getClass().getSimpleName().equals("GnollExile")))break;waitOne();}
                                                                                                                                                log("LIVE ORIGIN LEVEL FIVE BOUNDED REST FOR FULL TOME");
                                                                                                                                                if(!Boolean.getBoolean("route.originFiveBacktrackRestGnoll"))assertEquals(5,charge(Dungeon.hero.belongings.getItem(HolyTome.class)));
                                                                                                                                                Item fullRestRation=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(fullRestRation);fullRestRation.execute(Dungeon.hero,"EAT");ready();cast(HolyWard.INSTANCE,null);assertEquals(3,Dungeon.hero.shielding());
                                                                                                                                                log("LIVE ORIGIN LEVEL FIVE OWNED MEAL AND WARD AFTER BOUNDED RECHARGE");
                                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackRestGnoll")){
                                                                                                                                                    Mob restGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==159).findFirst().get();cast(ShieldOfLight.INSTANCE,restGnoll.pos);
                                                                                                                                                    for(int n=0;n<3 && !Dungeon.level.adjacent(Dungeon.hero.pos,restGnoll.pos);n++)waitOne();
                                                                                                                                                    for(int hit=0;hit<6 && restGnoll.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,restGnoll.pos));click(restGnoll.pos);}assertFalse(restGnoll.isAlive());log("LIVE ORIGIN LEVEL FIVE ACTUAL REST INTERRUPTING GNOLL DEFEATED");
                                                                                                                                                }
                                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackSpears")){
                                                                                                                                                    for(int cell:new int[]{1409,1366,1323,1280,1237,1195,1152,1108,1064,1020,976,932,890,847,803,802,758,716,672,628,584,585,586,586,587,587,587}){assertNull(Dungeon.level.traps.get(cell));click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                                                    Item recoveredSpears=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class);assertNotNull(recoveredSpears);assertEquals(2,recoveredSpears.quantity());assertEquals(3,recoveredSpears.level());log("LIVE ORIGIN ACTUALLY RECOVERED TWO EARNED UPGRADED SPEARS");
                                                                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackExitProbe")){
                                                                                                                                                        for(int cell:new int[]{543,542,584,626,625,624,623,622,621,620,619,618,660,616,615,614,613,612,611,653}){assertNull(Dungeon.level.traps.get(cell));click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);if(cell==615 && !Boolean.getBoolean("route.originFiveBacktrackKeepWardMana"))cast(HolyWard.INSTANCE,null);}
                                                                                                                                                        log("LIVE ORIGIN ACTUAL BACKTRACK EXIT ENCOUNTER GEOMETRY");
                                                                                                                                                        if(Boolean.getBoolean("route.originFiveBacktrackExitFight")){
                                                                                                                                                            Mob exitSnake=Dungeon.level.mobs.stream().filter(m->m.id()==165).findFirst().get();cast(GuidingLight.INSTANCE,exitSnake.pos);if(exitSnake.isAlive())cast(GuidingLight.INSTANCE,exitSnake.pos);assertFalse(exitSnake.isAlive());
                                                                                                                                                            Mob exitRat=Dungeon.level.mobs.stream().filter(m->m.id()==160).findFirst().get();
                                                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackClearRatLine")){
                                                                                                                                                                for(int clearWait=0;clearWait<3;clearWait++){waitOne();if(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,exitRat.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos==exitRat.pos)break;}for(int shot=0;shot<2 && exitRat.isAlive();shot++){assertEquals(exitRat.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,exitRat.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,exitRat.pos);}
                                                                                                                                                                if(exitRat.isAlive()){cast(ShieldOfLight.INSTANCE,exitRat.pos);for(int n=0;n<2 && !Dungeon.level.adjacent(Dungeon.hero.pos,exitRat.pos);n++)waitOne();for(int hit=0;hit<4 && exitRat.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,exitRat.pos));click(exitRat.pos);}}
                                                                                                                                                            }else for(int shot=0;shot<3 && exitRat.isAlive();shot++)throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,exitRat.pos);
                                                                                                                                                            assertFalse(exitRat.isAlive());
                                                                                                                                                            log("LIVE ORIGIN ACTUAL EXIT SNAKE AND RAT DEFEATED WITH OWNED SPELLS AND SPEARS");
                                                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackReturnFour")){
                                                                                                                                                                if(Boolean.getBoolean("route.originFiveBacktrackKeepWardMana")){
                                                                                                                                                                    for(int cell:new int[]{652,652,651,651}){click(cell);assertEquals(cell,Dungeon.hero.pos);}Mob exitSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==158).findFirst().get();cast(ShieldOfLight.INSTANCE,exitSwarm.pos);
                                                                                                                                                                    for(int cell:new int[]{650,650,649,605}){click(cell);assertEquals(cell,Dungeon.hero.pos);}cast(ShieldOfLight.INSTANCE,exitSwarm.pos);
                                                                                                                                                                    for(int cell:new int[]{561,518,519}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                                                                }else for(int cell:new int[]{652,652,651,651,650,650,606,563}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                                                                click(520);if(Dungeon.depth==3)click(520);assertEquals(4,Dungeon.depth);assertEquals(1007,Dungeon.hero.pos);log("LIVE ORIGIN ACTUAL LEVEL ONE THROUGH BACKTRACKING AND RETURN TO FLOOR FOUR");
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                            if(Boolean.getBoolean("route.originFiveBacktrackSecondGnoll")){
                                                                                                                                                Item ration=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(ration);ration.execute(Dungeon.hero,"EAT");ready();
                                                                                                                                                Mob secondGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==158).findFirst().get();for(int n=0;n<6 && !Dungeon.level.adjacent(Dungeon.hero.pos,secondGnoll.pos);n++)waitOne();
                                                                                                                                                cast(ShieldOfLight.INSTANCE,secondGnoll.pos);assertEquals(3,Dungeon.hero.shielding());
                                                                                                                                                for(int hit=0;hit<6 && secondGnoll.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,secondGnoll.pos));click(secondGnoll.pos);}assertFalse(secondGnoll.isAlive());
                                                                                                                                                log("LIVE ORIGIN LEVEL FIVE BACKTRACK SECOND GNOLL ACTUALLY DEFEATED WITH REAL MEAL SHIELD");
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                    if(Boolean.getBoolean("route.originFiveBacktrackFrozen")){
                                                                                                        Item coldMeal=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class);assertNotNull(coldMeal);coldMeal.execute(Dungeon.hero,"EAT");ready();
                                                                                                        log("LIVE ORIGIN LEVEL FIVE BACKTRACK ACTUAL FROZEN MEAL EFFECT");
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                            if(Boolean.getBoolean("route.originLevelFiveLoop")){
                                                                                                Item lastColdMeal=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class);assertNotNull(lastColdMeal);lastColdMeal.execute(Dungeon.hero,"EAT");ready();
                                                                                                int[] secondRing={1072,1032,993,954,916,917,957,996,1035,1073};
                                                                                                for(int n=0;n<34;n++){int cell=secondRing[n%secondRing.length];click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                Mob lastSlime=Dungeon.level.mobs.stream().filter(m->m.id()==86).findFirst().get();assertTrue(Dungeon.level.heroFOV[lastSlime.pos]);cast(ShieldOfLight.INSTANCE,lastSlime.pos);assertEquals(3,Dungeon.hero.shielding());
                                                                                                for(int hit=0;hit<5 && lastSlime.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,lastSlime.pos));click(lastSlime.pos);}assertFalse(lastSlime.isAlive());
                                                                                                log("LIVE ORIGIN LEVEL FIVE FED LOOP AND FINAL SLIME ACTUALLY CLEARED");
                                                                                            }

                                                                                        }

                                                                                    }
                                                                                    return;
                                                                                }
                                                                                for(int cell:new int[]{996,1035,1035}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class));log("LIVE ORIGIN COLD FOOD ACTUALLY EATEN AND SECOND MEAT OWNED");
                                                                                if(Boolean.getBoolean("route.originColdFoodWait")){
                                                                                    for(int n=0;n<120;n++){
                                                                                        waitOne();
                                                                                        if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos])){log("FED COLD CORNER FIRST ACTUAL VISIBLE ENEMY");break;}
                                                                                    }
                                                                                    log("LIVE ORIGIN FED COLD CORNER ACTUAL RESOURCE BOUNDARY");
                                                                                }

                                                                            }

                                                                        }

                                                                    }

                                                                    if(Boolean.getBoolean("route.originSouthSwarm") && !Boolean.getBoolean("route.originRecoverGnollLoot")){
                                                                        Mob smallSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==120).findFirst().get();
                                                                        if(smallSwarm.isAlive())throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,smallSwarm.pos);
                                                                        for(int n=0;n<3 && smallSwarm.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,smallSwarm.pos));click(smallSwarm.pos);}
                                                                        assertFalse(smallSwarm.isAlive());log("LIVE ORIGIN SOUTH SMALL SWARM ACTUALLY CLEARED");
                                                                        if(Boolean.getBoolean("route.originColdMeal")){
                                                                            Item raw=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat.class);assertNotNull(raw);raw.execute(Dungeon.hero,"DROP");ready();
                                                                            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,996);
                                                                            for(int cell:new int[]{1074,1073,1073,1074}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));
                                                                            for(int n=0;n<11;n++){
                                                                                waitOne();
                                                                                if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos])){log("COLD FOOD WAIT FIRST ACTUAL VISIBLE ENEMY");break;}
                                                                            }
                                                                            log("LIVE ORIGIN OWNED MEAT COLD PROCESSING ACTUAL BOUNDARY");
                                                                        }

                                                                    }

                                                                }

                                                            }

                                                        }

                                                    }

                                                }

                                            }

                                        }

                                    }
                                }
                            }
                        }
                        return;
                    }
                    if(Boolean.getBoolean("route.originFourSwarm")){
                        Mob swarm=Dungeon.level.mobs.stream().filter(m->m.id()==83).findFirst().get();
                        cast(ShieldOfLight.INSTANCE,swarm.pos);
                        click(473);assertEquals(473,Dungeon.hero.pos);click(472);click(472);click(472);
                        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));log("LIVE ORIGIN FOUR CURSED SHURIKEN ACTUALLY ACQUIRED");
                        if(Boolean.getBoolean("route.originFourMeleeFirst")){
                            click(swarm.pos);
                            java.util.List<Mob> children=new java.util.ArrayList<>();for(Mob m:Dungeon.level.mobs)if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm && m!=swarm)children.add(m);assertEquals(1,children.size());
                            Mob child=children.get(0);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,swarm.pos);
                            assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,child.pos));click(child.pos);
                            log("LIVE ORIGIN FOUR MELEE FIRST SWARM EXCHANGES");
                            if(Boolean.getBoolean("route.originFourClearSwarm")){
                                for(int n=0;n<2 && swarm.isAlive();n++)click(swarm.pos);assertFalse(swarm.isAlive());
                                assertEquals(4,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));Dungeon.hero.upgradeTalent(Talent.SEARING_LIGHT);
                                for(int id:new int[]{96,98,102}){
                                    Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==id).findFirst().get();
                                    for(int n=0;n<2 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}
                                    assertFalse(target.isAlive());
                                }
                                assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm));
                                log("LIVE ORIGIN FOUR ENTIRE SWARM CLEARED EARNED LEVEL FOUR");
                            }
                            return;
                        }
                        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,swarm.pos);
                        for(int n=0;n<4 && swarm.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,swarm.pos));click(swarm.pos);}
                        assertFalse(swarm.isAlive());assertEquals(4,Dungeon.hero.lvl);Dungeon.hero.upgradeTalent(Talent.SEARING_LIGHT);log("LIVE ORIGIN FOUR SWARM PARENT DEFEATED LEVEL FOUR EARNED");
                    }
                }
            }
        }
    }
    private void fourthFloorSection() throws Exception {
        assertEquals(4,Dungeon.depth);assertEquals(1007,Dungeon.hero.pos);
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",4).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        for(com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap t:Dungeon.level.traps.valueList())System.out.println("FLOOR_FOUR_TRAP "+t.pos+" "+t.getClass().getSimpleName());
        Item pasty=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty.class);assertNotNull(pasty);
        pasty.execute(Dungeon.hero,"EAT");ready();log("EAT EARNED PASTY ON FLOOR FOUR");
        for(int n=0;n<120;n++)waitOne();
        log("FLOOR_FOUR_BOUNDED_RECHARGE");
        for(int c:new int[]{968,929,890,889,888})click(c);
        Mob entranceCrab=Dungeon.level.mobs.stream().filter(m->m.id()==81).findFirst().get();
        cast(ShieldOfLight.INSTANCE,entranceCrab.pos);if(!Boolean.getBoolean("route.crabNoLight"))cast(GuidingLight.INSTANCE,entranceCrab.pos);
        for(int n=0;n<3 && entranceCrab.isAlive();n++)click(entranceCrab.pos);
        assertFalse(entranceCrab.isAlive());log("FLOOR_FOUR_CRAB_DEFEATED");
        for(int c:new int[]{887,848,809})click(c);
        Mob approachSlime=Dungeon.level.mobs.stream().filter(m->m.id()==87).findFirst().get();
        cast(ShieldOfLight.INSTANCE,approachSlime.pos);
        for(int n=0;n<4 && approachSlime.isAlive();n++)click(approachSlime.pos);
        if(approachSlime.isAlive() && Boolean.getBoolean("route.crabNoLight")){
            cast(HolyWard.INSTANCE,null);
            for(int n=0;n<2 && approachSlime.isAlive();n++)click(approachSlime.pos);
        }
        assertFalse(approachSlime.isAlive());log("FLOOR_FOUR_SPAWNED_SLIME_DEFEATED");
        for(int n=0;n<Integer.getInteger("route.mid4Recharge",20);n++)waitOne();
        for(int c:new int[]{808,807,806})click(c);
        Mob northGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==82).findFirst().get();
        if(!Dungeon.level.adjacent(Dungeon.hero.pos,northGnoll.pos))click(806);
        click(northGnoll.pos);
        assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m.id()==82));log("FLOOR_FOUR_GNOLL_DEFEATED");
        for(int c:new int[]{767,728,689,650,611,610,609,608,607,606,605,604,603})click(c);
        if(Boolean.getBoolean("route.earlyWard"))cast(HolyWard.INSTANCE,null);
        for(int c:(Boolean.getBoolean("route.diagonalCorner")?new int[]{563,524,484}:new int[]{563,524,485,484}))click(c);
        for(int c:new int[]{483,482,481,480,479,478,477,476,475,474,474})click(c);
        Mob northSwarm=Dungeon.level.mobs.stream().filter(m->m.id()==80).findFirst().get();
        boolean retainFlame=Boolean.getBoolean("route.retainFlame");
        if(retainFlame){if(Dungeon.hero.buff(HolyWard.HolyArmBuff.class)==null)cast(HolyWard.INSTANCE,null);}else cast(ShieldOfLight.INSTANCE,northSwarm.pos);
        click(473);
        if(retainFlame)waitOne();else throwItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame.class,436);
        click(472);click(472);
        if(retainFlame)cast(GuidingLight.INSTANCE,northSwarm.pos);else cast(HolyWard.INSTANCE,null);
        click(472);
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));
        click(northSwarm.pos);
        Mob child=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm && m!=northSwarm).findFirst().get();
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,northSwarm.pos);
        click(child.pos);
        log("FLOOR_FOUR_SWARM_EXCHANGES");
        if(retainFlame){
            for(int n=0;n<2 && northSwarm.isAlive();n++)click(northSwarm.pos);
            for(int n=0;n<2 && child.isAlive();n++)click(child.pos);
            log("FLOOR_FOUR_RETAIN_FLAME_EXTRA_EXCHANGES");
        }
        assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm));
        assertEquals(4,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));
        Dungeon.hero.upgradeTalent(Talent.SEARING_LIGHT);log("EARN FIRST SEARING_LIGHT POINT");
        if(System.getProperty("route.checkpointNorthOut")!=null)writeCheckpoint(System.getProperty("route.checkpointNorthOut"));
        if(retainFlame && Boolean.getBoolean("route.toGoo")){
            for(int c:new int[]{473,473,473,513,513})click(c);
            assertEquals(5,Dungeon.depth);log("FLOOR_FIVE_EARNED_ENTRY");
            System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",5).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
            if(System.getProperty("route.checkpoint5Out")!=null)writeCheckpoint(System.getProperty("route.checkpoint5Out"));
            return;
        }
        if(retainFlame){
            for(int c:new int[]{473,473,473,514,554,555,594,633,672})click(c);
            
            log("FLOOR_FOUR_BARRICADE_APPROACH_WITH_FLAME");
            Mob secondSlime=Dungeon.level.mobs.stream().filter(m->m.id()==92).findFirst().get();
            if(charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1)cast(ShieldOfLight.INSTANCE,secondSlime.pos);
            for(int n=0;n<4 && secondSlime.isAlive();n++)click(secondSlime.pos);
            assertFalse(secondSlime.isAlive());log("FLOOR_FOUR_SECOND_SPAWNED_SLIME_DEFEATED");
        }
    }

    private void fifthFloorSection() throws Exception {
        assertEquals(5,Dungeon.depth);assertEquals(708,Dungeon.hero.pos);
        for(int n=0;n<Integer.getInteger("route.gooPrep",100);n++)waitOne();
        if(Boolean.getBoolean("route.returnToFour")){
            click(708);assertEquals(4,Dungeon.depth);log("RETURN FLOOR FOUR AFTER PAID BOSS_FLOOR_RECHARGE");
            Mob returnSlime=Dungeon.level.mobs.stream().filter(m->m.id()==92).findFirst().get();
            cast(HolyWard.INSTANCE,null);cast(GuidingLight.INSTANCE,returnSlime.pos);
            waitOne();cast(ShieldOfLight.INSTANCE,returnSlime.pos);waitOne();
            for(int n=0;n<3 && returnSlime.isAlive();n++)click(returnSlime.pos);
            assertFalse(returnSlime.isAlive());log("RETURNING CLERIC DEFEATS SECOND SLIME WITH EARNED CHARGES");
            click(514);
            Mob ghost=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost).findFirst().get();
            assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,ghost.pos));click(ghost.pos);
            assertTrue(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost.Quest.active());log("ACCEPT REAL GHOST QUEST");
            for(int c:new int[]{515,555,594,633,672,710,709})click(c);
            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame.class,707);
            for(int n=0;n<7;n++)waitOne();
            assertFalse(Dungeon.level.map[746]==Terrain.BARRICADE);
            assertEquals(0,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob.volumeAt(746,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire.class));
            for(int c:new int[]{708,707,746,785,784,784,823,822,821,821})click(c);
            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class));
            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
            log("FLOOR FOUR BARRICADE FOOD AND UPGRADE ACTUALLY ACQUIRED");
            Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class).execute(Dungeon.hero,"EAT");ready();log("EAT LEGALLY ACQUIRED BARRICADE RATION");
            for(int c:new int[]{860,899,900,901,902,902,903,864,825,826,826})click(c);
            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas.class));
            log("BARRICADE ROOM GAS POTION AND GOLD ACQUIRED");
            if(System.getProperty("route.checkpointReturnOut")!=null)writeCheckpoint(System.getProperty("route.checkpointReturnOut"));
            return;
        }
        for(int c:new int[]{673,672,637})click(c);
        Dungeon.hero.search(true);ready();log("SEARCH FLOOR FIVE SECRET ENTRANCE");
        assertEquals(Terrain.DOOR,Dungeon.level.map[603]);
        for(int c:new int[]{603,569,535,501,500,499,465,431,430})click(c);
        log("GOO_EARNED_APPROACH");
        Mob goo=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo).findFirst().get();
        cast(GuidingLight.INSTANCE,goo.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,goo.pos);
        for(int c:new int[]{431,465,499,500,501,535,569,603,637})click(c);
        log("GOO_FIRST_OUTER_RETREAT");
    }

    private void returnedFourthFloorSection() throws Exception {
        assertEquals(4,Dungeon.depth);assertEquals(826,Dungeon.hero.pos);
        Mob pursuingCrab=Dungeon.level.mobs.stream().filter(m->m.id()==99).findFirst().get();
        cast(ShieldOfLight.INSTANCE,pursuingCrab.pos);
        for(int n=0;n<3 && pursuingCrab.isAlive();n++)click(pursuingCrab.pos);
        assertFalse(pursuingCrab.isAlive());log("RETURN ROUTE PURSUING CRAB DEFEATED");
        for(int c:new int[]{825,824,823,784,785,746,707,708,709,710,672,633,594,555,516,477,438,437,437,437})click(c);
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity.class));
        log("RETURN ROUTE PURITY POTION ACQUIRED");
        if(System.getProperty("route.checkpointPurityOut")!=null)writeCheckpoint(System.getProperty("route.checkpointPurityOut"));
        if(Boolean.getBoolean("route.stopAtPurity"))return;
        Mob questCrab=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatCrab).findFirst().get();
        cast(ShieldOfLight.INSTANCE,questCrab.pos);
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK OWNED PURITY BEFORE QUEST GAS");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas.class,questCrab.pos);
        for(int c:new int[]{436,435,434,472,512,552})click(c);
        log("GREAT CRAB FIRST GAS KITE");
        for(int c:new int[]{553,554,555,556,517,478})click(c);
        log("GREAT CRAB GAS ESCAPE");
    }

    private void purityCheckpointSection() throws Exception {
        assertEquals(437,Dungeon.hero.pos);assertEquals(4,Dungeon.depth);
        log("EARNED PURITY CHECKPOINT READY FOR NEXT EXPERIMENT");
        for(int c:new int[]{436,435,434,472,512,552,553,554,555,594,633,672,673,674,675,676})click(c);
        log("GREAT CRAB LURED THROUGH CENTRAL ROOM WITHOUT SPENDING GAS");
        Mob questCrab=Dungeon.level.mobs.stream().filter(m->m.id()==107).findFirst().get();
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,questCrab.pos);
        if(questCrab.isAlive())cast(GuidingLight.INSTANCE,questCrab.pos);
        log("GREAT CRAB FREE THROW BEFORE LIGHT");
        Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity.class).execute(Dungeon.hero,"DRINK");ready();log("DRINK PURITY AFTER OPENING QUEST BURST");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas.class,676);
        for(int c:new int[]{716,756,795,834,873,912})click(c);
        Dungeon.hero.search(true);ready();log("SEARCH ACTUAL SECRET DOOR 913");assertEquals(Terrain.DOOR,Dungeon.level.map[913]);
        for(int c:new int[]{913,953,993,1032,1072,1073,1074,1035})click(c);
        log("QUEST GAS FOLLOWED TOWARD CHILLING TRAP");
        Mob followingGnoll=Dungeon.level.mobs.stream().filter(m->m.id()==84).findFirst().get();
        cast(ShieldOfLight.INSTANCE,followingGnoll.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,questCrab.pos);
        assertFalse(questCrab.isAlive());log("GREAT CRAB FINISHED AT CORRIDOR CORNER");
        for(int n=0;n<2 && followingGnoll.isAlive();n++)click(followingGnoll.pos);
        assertFalse(followingGnoll.isAlive());log("QUEST ESCAPE FOLLOWING GNOLL DEFEATED");
        if(!Boolean.getBoolean("route.killSlimeBeforeTrap")){click(996);assertEquals(996,Dungeon.hero.pos);}
        Mob followingSlime=Dungeon.level.mobs.stream().filter(m->m.id()==83).findFirst().get();
        if(!Boolean.getBoolean("route.skipSlimeShield"))cast(ShieldOfLight.INSTANCE,followingSlime.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,followingSlime.pos);
        for(int n=0;n<4 && followingSlime.isAlive();n++)click(followingSlime.pos);
        assertFalse(followingSlime.isAlive());log("QUEST ESCAPE SLIME DEFEATED AT COLD TRAP");
        if(Boolean.getBoolean("route.killSlimeBeforeTrap"))assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));else assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));
        if(System.getProperty("route.checkpointQuestOut")!=null)writeCheckpoint(System.getProperty("route.checkpointQuestOut"));
        log("QUEST READY ACTUAL SURVIVOR");
        if(Boolean.getBoolean("route.stopAfterQuest"))return;
        if(Boolean.getBoolean("route.killSlimeBeforeTrap")){
            for(int c:new int[]{1073,1073,1072,1072,1072})click(c);
            click(1073);
            Mob lateCrab=Dungeon.level.mobs.stream().filter(m->m.id()==119).findFirst().get();
            cast(ShieldOfLight.INSTANCE,lateCrab.pos);
            for(int n=0;n<3 && lateCrab.isAlive();n++)click(lateCrab.pos);
            assertFalse(lateCrab.isAlive());log("LATE SPAWNED CRAB DEFEATED WITH SAVED CHARGE");
            assertEquals(5,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(1));
            Dungeon.hero.upgradeTalent(Talent.SATIATED_SPELLS);log("EARN SATIATED SPELLS AT LEVEL FIVE");
            for(int c:new int[]{1074,1035})click(c);
            Item meat=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat.class);assertNotNull(meat);
            int meatQuantity=meat.quantity();
            meat.execute(Dungeon.hero,"DROP");ready();log("DROP OWNED RAW MEAT BEFORE TRIGGERING COLD");
            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,996);
            click(1074);log("LEAVE COLD WITH HEALING POTION RETAINED");
            for(int n=0;n<11;n++)waitOne();
            assertEquals(0,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob.volumeAt(1035,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing.class));
            click(1035);click(1035);
            Item frozen=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class);assertNotNull(frozen);assertEquals(meatQuantity,frozen.quantity());
            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));
            log("OWNED RAW MEAT SAFELY FROZEN AND RECOVERED");
            if(System.getProperty("route.checkpointFoodOut")!=null)writeCheckpoint(System.getProperty("route.checkpointFoodOut"));
        }
    }

    private void foodCheckpointSection() throws Exception {
        assertEquals(4,Dungeon.depth);assertEquals(1035,Dungeon.hero.pos);
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class);assertNotNull(food);assertEquals(2,food.quantity());
        food.execute(Dungeon.hero,"EAT");ready();log("EAT ONE ACTUALLY FROZEN QUEST MEAT");
        cast(HolyWard.INSTANCE,null);assertEquals(3,Dungeon.hero.shielding());log("MEAL EARNED THREE SHIELD FROM SATIATED SPELLS");
        for(int c:new int[]{1036,1036,996,996,1035,1074,1073,1072,1032,993,953,913,912,873,834,795,756,717,676})click(c);
        log("RETURN TO GHOST AFTER COOKING AND MEAL");
        for(int c:new int[]{676,675,674,673,633,594,555,516,477})click(c);
        log("READY GHOST NORTH APPROACH WITH FOLLOWING SWARM");
        Mob swarm=Dungeon.level.mobs.stream().filter(m->m.id()==109).findFirst().get();cast(ShieldOfLight.INSTANCE,swarm.pos);
        click(478);log("GHOST FINAL PROTECTED APPROACH");click(479);
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor reward=claimActualGhostArmor();assertEquals(1,reward.level());
        reward.execute(Dungeon.hero,"EQUIP");Dungeon.hero.busy();ready();log("EQUIP EARNED LEATHER ARMOR");
        upgradeOwnedGear(reward);assertEquals(2,reward.level());log("EARNED LEATHER ARMOR UPGRADED TO TWO");
        for(int c:new int[]{478,477,476,475,513,513})click(c);
        assertEquals(5,Dungeon.depth);assertEquals(708,Dungeon.hero.pos);log("GOO FLOOR RETURN WITH EARNED UPGRADED ARMOR");
        if(System.getProperty("route.checkpointArmored5Out")!=null)writeCheckpoint(System.getProperty("route.checkpointArmored5Out"));
    }
    private void upgradeOwnedGear(Item spear) throws Exception {
        Item scroll=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class);
        assertNotNull(scroll);assertNotNull(spear);
        int oldLevel=spear.level();
        itemSelection=null;upgradeButton=null;
        scroll.execute(Dungeon.hero,"READ");assertNotNull(itemSelection);
        assertTrue(itemSelection.itemSelectable(spear));itemSelection.onSelect(spear);
        assertNotNull(upgradeButton);
        Method confirm=upgradeButton.getClass().getDeclaredMethod("onClick");confirm.setAccessible(true);confirm.invoke(upgradeButton);
        ready();log("READ OWNED UPGRADE ON OWNED GEAR");
        assertEquals(oldLevel+1,spear.level());assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
    }

    private com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor claimActualGhostArmor() throws Exception {
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost ghost=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost)Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost).findFirst().get();
        assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,ghost.pos));
        assertTrue(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost.Quest.processed());
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor offered=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost.Quest.armor;
        assertNotNull(offered);click(ghost.pos); //actual adjacent NPC interaction, with its normal quest gate
        //Rendering-only window substitute. The original reward handler performs the actual pickup, glyph assignment, NPC removal and quest completion.
        com.shatteredpixel.shatteredpixeldungeon.windows.WndSadGhost window=mock(com.shatteredpixel.shatteredpixeldungeon.windows.WndSadGhost.class);
        Field ghostField=window.getClass().getDeclaredField("ghost");ghostField.setAccessible(true);ghostField.set(window,ghost);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost.Quest.revealOfferedLevels();
        Method choose=com.shatteredpixel.shatteredpixeldungeon.windows.WndSadGhost.class.getDeclaredMethod("selectReward",Item.class);choose.setAccessible(true);choose.invoke(window,offered);
        ready();log("CLAIM ACTUAL GHOST ARMOR THROUGH ORIGINAL REWARD HANDLER");
        assertTrue(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost.Quest.completed());
        assertSame(offered,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor.class));
        return offered;
    }

    private void buyActualShopHeap(int cell) throws Exception {
        Heap heap=Dungeon.level.heaps.get(cell);assertNotNull(heap);assertEquals(Heap.Type.FOR_SALE,heap.type);
        int price=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper.sellPrice(heap.peek());
        assertTrue("Cannot afford shop price "+price,Dungeon.gold>=price);
        tradeWindow=null;tradeHeap=null;executeTradeUi=true;
        try{click(cell);click(cell);}finally{executeTradeUi=false;}
        assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,cell) || Dungeon.hero.pos==cell);
        assertSame(heap,tradeHeap);assertNotNull(tradeWindow);
        Method buy=com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem.class.getDeclaredMethod("buy",Heap.class);buy.setAccessible(true);
        Dungeon.hero.busy();buy.invoke(tradeWindow,heap);ready();log("BUY ACTUAL SHOP HEAP "+cell+" PRICE "+price);
    }

    private void eighthFloorAfterBless() throws Exception {
        for(int n=0;n<Integer.getInteger("route.guardPrepWait",0);n++)waitOne();
        for(int c:new int[]{808,776,744,712,680,648,647,646})click(c);
        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);assertEquals(10,Dungeon.hero.shielding());
        click(614);log("FLOOR EIGHT GUARDED NORTHWEST DOOR WITH EARNED BLESS");
        Mob guard=Dungeon.level.mobs.stream().filter(m->m.id()==250).findFirst().get();
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,guard.pos);
        click(581);click(580);log("FLOOR EIGHT GUARD WITH DM LINE OF SIGHT BROKEN");
        click(guard.pos);click(guard.pos);click(guard.pos);
        log("FLOOR EIGHT GUARD MELEE WITHOUT A RESET");
        click(guard.pos);click(581);
        log("FLOOR EIGHT CLOSE DM TO MELEE AFTER GUARD STRIKE");
        click(guard.pos);log("FLOOR EIGHT GUARD FINISHER WITH DM ADJACENT");
        assertFalse(guard.isAlive());
        Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==265).findFirst().get();
        click(dm.pos);click(dm.pos);
        log("FLOOR EIGHT DM MELEE EXCHANGE");
        click(dm.pos);assertFalse(dm.isAlive());log("FLOOR EIGHT GUARD AND DM DEFEATED WITH RETAINED BLESS");
        for(int c:new int[]{549,549,549,517,518,519,520,521,522,490,458,426})click(c);
        assertEquals(426,Dungeon.hero.pos);
        Dungeon.hero.search(true);ready();log("REVEAL FLOOR EIGHT FOOD ROOM SECRET DOOR");
        assertEquals(Terrain.DOOR,Dungeon.level.map[427]);
        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
        click(427);assertEquals(427,Dungeon.hero.pos);log("FLOOR EIGHT FOOD ROOM DOOR AFTER SECOND EARNED BLESS");
        Mob foodDM=Dungeon.level.mobs.stream().filter(m->m.id()==251).findFirst().get();
        click(foodDM.pos);click(foodDM.pos);log("FLOOR EIGHT FOOD GUARD DM MELEE");
        assertFalse(foodDM.isAlive());
        click(396);assertEquals(396,Dungeon.hero.pos);
        assertNull(Dungeon.hero.buff(Vertigo.class));assertTrue(Dungeon.level.traps.get(428).active);
        for(int c:new int[]{364,365,365,366,366})click(c);
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty.class));
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class));
        log("FLOOR EIGHT PASTY AND LEVITATION ACTUALLY ACQUIRED WITHOUT TRIGGERING CONFUSION TRAP");
        if(System.getProperty("route.checkpoint8PastyOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint8PastyOut"));
    }

    private void eighthFloorAfterRest() throws Exception {
        for(int c:new int[]{998,1031,1063})click(c);
        assertEquals(1063,Dungeon.hero.pos);
        for(int c:new int[]{1095,1127,1126,1125,1124,1123,1122,1090,1058,1026,994,962,930,898,899,900,901,902})click(c);
        assertEquals(902,Dungeon.hero.pos);
        log("FLOOR EIGHT WESTERN ROUTE PAST NECROMANCER RANGE");
        for(int c:new int[]{870,838})click(c);
        assertEquals(838,Dungeon.hero.pos);
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==254).findFirst().get();
        cast(GuidingLight.INSTANCE,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        log("FLOOR EIGHT SPAWNED SKELETON RANGED OPENING");
        waitOne();waitOne();waitOne();
        cast(ShieldOfLight.INSTANCE,skeleton.pos);
        click(skeleton.pos);click(skeleton.pos);
        log("FLOOR EIGHT SPAWNED SKELETON SHIELDED MELEE");
        assertFalse(skeleton.isAlive());
        for(int c:new int[]{774,742,710,678,646})click(c);
        for(int n=0;n<9;n++)waitOne();
        click(647);click(648);
        Mob thief=Dungeon.level.mobs.stream().filter(m->m.id()==246).findFirst().get();
        cast(GuidingLight.INSTANCE,thief.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,thief.pos);
        log("FLOOR EIGHT THIEF AFTER EARNED SKELETON KILL");
        cast(GuidingLight.INSTANCE,thief.pos);assertFalse(thief.isAlive());
        assertEquals(7,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));
        Dungeon.hero.upgradeTalent(Talent.BLESS);log("EARN LEVEL SEVEN AND FIRST BLESS POINT");
        for(int c:new int[]{680,712,712,712,744,776,808,809,809})click(c);
        Item strength=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class);assertNotNull(strength);
        strength.execute(Dungeon.hero,"DRINK");ready();assertEquals(14,Dungeon.hero.STR);log("DRINK FOURTH EARNED STRENGTH POTION");
        if(System.getProperty("route.checkpoint8BlessOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint8BlessOut"));
    }

    private void activateOwnedHourglassFreeze() throws Exception {
        TimekeepersHourglass hourglass=Dungeon.hero.belongings.getItem(TimekeepersHourglass.class);assertNotNull(hourglass);assertTrue(hourglass.isEquipped(Dungeon.hero));
        hourglassWindow=null;hourglass.execute(Dungeon.hero,"ACTIVATE");assertNotNull("Original Hourglass choice window must open",hourglassWindow);
        Method select=hourglassWindow.getClass().getDeclaredMethod("onSelect",int.class);select.setAccessible(true);select.invoke(hourglassWindow,1);
        assertNotNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));log("ACTUAL HOURGLASS ORIGINAL FREEZE CHOICE");
    }
    private void activateOwnedHourglassStasis() throws Exception {
        TimekeepersHourglass hourglass=Dungeon.hero.belongings.getItem(TimekeepersHourglass.class);assertNotNull(hourglass);assertTrue(hourglass.isEquipped(Dungeon.hero));
        hourglassWindow=null;hourglass.execute(Dungeon.hero,"ACTIVATE");assertNotNull(hourglassWindow);
        Method select=hourglassWindow.getClass().getDeclaredMethod("onSelect",int.class);select.setAccessible(true);select.invoke(hourglassWindow,0);
        assertNotNull("Original stasis must attach",Dungeon.hero.buff(TimekeepersHourglass.timeStasis.class));
        Dungeon.hero.busy();ready();assertNull("Stasis must actually complete",Dungeon.hero.buff(TimekeepersHourglass.timeStasis.class));assertEquals(0,Dungeon.hero.paralysed);
        log("ACTUAL PAID STASIS COMPLETE BEFORE LEAVING GUARD ROOM");
    }
    private void eighthFloorHourglassGuard() throws Exception {
        assertEquals(214,Dungeon.hero.pos);
        for(int cell:new int[]{246,279,280,281,314})click(cell);
        log("PIT EXIT NORTHERN GUARD APPROACH");
        Mob guard=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard && Dungeon.level.heroFOV[m.pos]).findFirst().get();
        activateOwnedHourglassFreeze();
        if(Boolean.getBoolean("route.hourglassAttack")){
            click(guard.pos);
            assertNull("First attack must dispel actual time freeze",Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
            try{click(guard.pos);}catch(AssertionError expected){
                if(Dungeon.hero.isAlive())throw expected;
                assertTrue("Guard must still be alive in failed combat strategy",guard.isAlive());assertEquals(1,Dungeon.hero.HT);
                System.out.println("NEGATIVE_CONTROL_ATTACK_DISPELS_FREEZE_AND_GUARD_KILLS HP="+Dungeon.hero.HP+" guardHP="+guard.HP);
                return;
            }
            fail("Undefended Hourglass attack route unexpectedly survived; inspect negative control");
        }else{
            for(int cell:new int[]{347,379,411,442,474,507})click(cell);
            boolean early= Integer.getInteger("route.guardFreezeMoves",7)==6;
            if(early){Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");assertNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));log("END FREEZE AT DISTANCE FIVE AFTER SIX REAL MOVES");}
            click(540);
            if(!early && !Boolean.getBoolean("route.freezeAllTen")){
                Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                assertNull("Original deactivation must end freeze",Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));log("END ACTUAL FREEZE AFTER SEVEN MOVES OUTSIDE CHAIN RANGE");
            }
            if(Boolean.getBoolean("route.breakGuardPursuitWithStasis"))activateOwnedHourglassStasis();
            for(int cell:new int[]{572,604,636})click(cell);
            assertEquals(636,Dungeon.hero.pos);assertTrue("Guard is bypassed, not secretly killed",guard.isAlive());
            log("NORTHERN GUARD BYPASSED USING ACTUAL FROZEN MOVEMENT");
        }
        if(System.getProperty("route.hourglassGuardOut")!=null)writeCheckpoint(System.getProperty("route.hourglassGuardOut"));
    }
    private void eighthFloorHourglassCentral() throws Exception {
        assertEquals(636,Dungeon.hero.pos);
        for(int cell:new int[]{668,700,732,763,762,793,825,857,889,921,953})click(cell);
        assertEquals(953,Dungeon.hero.pos);log("HOURGLASS BRANCH CENTRAL DOOR APPROACH WITH CONTINUOUS RESOURCES");
        if(System.getProperty("route.hourglassCentralOut")!=null)writeCheckpoint(System.getProperty("route.hourglassCentralOut"));
    }
    private void eighthFloorHourglassSkeleton() throws Exception {
        assertEquals(953,Dungeon.hero.pos);
        if(Boolean.getBoolean("route.hourglassRangedSkeleton")){
            click(984);cast(HolyWard.INSTANCE,null);
            Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==244).findFirst().get();
            cast(ShieldOfLight.INSTANCE,skeleton.pos);
            for(int n=0;n<3;n++)cast(GuidingLight.INSTANCE,skeleton.pos);
            assertTrue("Three 2..8 lights cannot alone kill a full 25-HP skeleton",skeleton.isAlive());
            assertNull("Owned missile must have earned its free cast cooldown",Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.ShurikenInstantTracker.class));
            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
            assertFalse("Prepared skeleton must actually die",skeleton.isAlive());
            assertEquals("One skeleton cannot unlock Bless from 26 XP",6,Dungeon.hero.lvl);assertEquals(31,Dungeon.hero.exp);
            log("RECHARGED BRANCH REAL SKELETON CLEAR NO BLESS GRANTED");
            if(System.getProperty("route.hourglassSkeletonOut")!=null)writeCheckpoint(System.getProperty("route.hourglassSkeletonOut"));
            return;
        }
        Item rage=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class);assertNotNull(rage);
        if(Boolean.getBoolean("route.hourglassRageAtDoor")){
            click(984);log("CENTRAL ROOM BEFORE RETAINED RAGE AT ACTUAL DOOR");
            rage.execute(Dungeon.hero,"READ");ready();log("ACTUAL CENTRAL ROOM RAGE WITH VISIBLE SKELETON AND DM");
            if(System.getProperty("route.hourglassDoorOut")!=null)writeCheckpoint(System.getProperty("route.hourglassDoorOut"));
            return;
        }
        rage.execute(Dungeon.hero,"READ");ready();log("READ PRESERVED RAGE TO INTERRUPT PURSUING NORTHERN GUARD");
        click(984);click(984);log("CENTRAL ROOM ACTUAL DOOR ENTRY");
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==244).findFirst().get();
        cast(ShieldOfLight.INSTANCE,skeleton.pos);cast(GuidingLight.INSTANCE,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        for(int n=0;n<3 && skeleton.isAlive();n++)click(skeleton.pos);
        assertFalse(skeleton.isAlive());log("HOURGLASS BRANCH CENTRAL SKELETON DEFEATED WITH ACTUAL XP ONLY");
        if(System.getProperty("route.hourglassBlessOut")!=null)writeCheckpoint(System.getProperty("route.hourglassBlessOut"));
    }
    private void eighthFloorHourglassDuel() throws Exception {
        assertEquals(984,Dungeon.hero.pos);
        click(1015);click(1044);assertEquals("Engine movement is interrupted at the actual cell, not the clicked target",1077,Dungeon.hero.pos);log("CENTRAL DUEL AFTER TWO INPUTS AND THREE ELAPSED TURNS");
        if(System.getProperty("route.hourglassDuelOut")!=null)writeCheckpoint(System.getProperty("route.hourglassDuelOut"));
    }
    private void eighthFloorHourglassDM() throws Exception {
        assertEquals(984,Dungeon.hero.pos);assertEquals(31,Dungeon.hero.exp);assertEquals(6,Dungeon.hero.lvl);
        Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==245).findFirst().get();assertEquals(1138,dm.pos);
        activateOwnedHourglassFreeze();
        for(int cell:new int[]{1015,1046,1077,1108})click(cell);
        assertEquals(1108,Dungeon.hero.pos);assertEquals("DM must remain at actual frozen cell",1138,dm.pos);
        assertNull("Exactly four moves consume the two owned Hourglass charges",Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
        click(1139);assertEquals(1139,Dungeon.hero.pos);assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,dm.pos));log("ACTUAL ADJACENT DM APPROACH AFTER FOUR FROZEN MOVES");
        for(int n=0;n<4 && dm.isAlive();n++){
            assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,dm.pos));click(dm.pos);
            if(n==0 && Boolean.getBoolean("route.hourglassRageAfterFirstHit")){
                Item rage=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class);assertNotNull(rage);
                rage.execute(Dungeon.hero,"READ");ready();log("RETAINED RAGE AFTER FIRST DM HIT WITH GUARD AND NECROMANCER VISIBLE");
                if(System.getProperty("route.hourglassDMRageOut")!=null)writeCheckpoint(System.getProperty("route.hourglassDMRageOut"));
                return;
            }
        }
        assertFalse("Real adjacent DM encounter must be completed",dm.isAlive());assertEquals(7,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));
        Dungeon.hero.upgradeTalent(Talent.BLESS);log("RECHARGED HOURGLASS BRANCH EARNS BLESS AFTER BOTH REAL ENCOUNTERS");
        if(System.getProperty("route.hourglassDMOut")!=null)writeCheckpoint(System.getProperty("route.hourglassDMOut"));
    }
    private void eighthFloorHourglassFinishDM() throws Exception {
        assertEquals(1139,Dungeon.hero.pos);
        Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==245).findFirst().get();
        for(int n=0;n<4 && dm.isAlive();n++)click(dm.pos);
        assertFalse("DM must really be dead, including any paid Rage interaction",dm.isAlive());
        assertEquals(7,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));Dungeon.hero.upgradeTalent(Talent.BLESS);
        log("CONTINUOUS PIT RECHARGE SKELETON DM AND RAGE ROUTE ACTUALLY EARNS BLESS");
        if(System.getProperty("route.hourglassBlessEarnedOut")!=null)writeCheckpoint(System.getProperty("route.hourglassBlessEarnedOut"));
    }
    private void eighthFloorHourglassBlessEscape() throws Exception {
        assertEquals(1077,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.pointsInTalent(Talent.BLESS));
        for(int cell:new int[]{1076,1043,1042,1073,1072,1071,1102,1133,1164}){click(cell);assertEquals("Actual escape movement must reach the requested adjacent cell",cell,Dungeon.hero.pos);}
        log("ACTUAL POST DUEL ESCAPE WITH ALL REMAINING ENEMIES");
        if(System.getProperty("route.hourglassEscapeOut")!=null)writeCheckpoint(System.getProperty("route.hourglassEscapeOut"));
    }
    private void eighthFloorHourglassFirstBless() throws Exception {
        assertEquals(1164,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.pointsInTalent(Talent.BLESS));
        assertEquals("No whole charge may be granted at this checkpoint",0,charge(Dungeon.hero.belongings.getItem(HolyTome.class)));
        click(1131);assertEquals(1131,Dungeon.hero.pos);
        assertTrue("The move must actually earn the first whole charge",charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1);
        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FIRST REAL BLESS CAST AFTER PAID RECHARGE AND CHASING ENEMIES");
        if(System.getProperty("route.hourglassFirstBlessOut")!=null)writeCheckpoint(System.getProperty("route.hourglassFirstBlessOut"));
    }
    private void eighthFloorHourglassThiefApproach() throws Exception {
        assertEquals(1131,Dungeon.hero.pos);
        click(1098);assertEquals(1098,Dungeon.hero.pos);
        Mob thief=Dungeon.level.mobs.stream().filter(m->m.id()==242).findFirst().get();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,thief.pos));
        click(thief.pos);log("ACTUAL THIEF EXCHANGE WHILE NECRO SKELETON CONTINUES CHASING");
        if(System.getProperty("route.hourglassThiefOut")!=null)writeCheckpoint(System.getProperty("route.hourglassThiefOut"));
    }
    private void eighthFloorHourglassWestFight() throws Exception {
        assertEquals(1098,Dungeon.hero.pos);
        Mob thief=Dungeon.level.mobs.stream().filter(m->m.id()==242).findFirst().orElse(null);
        for(int n=0;n<Integer.getInteger("route.westThiefAttempts",2) && thief!=null && thief.isAlive() && Dungeon.level.adjacent(Dungeon.hero.pos,thief.pos);n++)click(thief.pos);
        log("ACTUAL THIEF FOLLOW UP ATTEMPTS NO STOLEN ITEMS RESTORED");
        if(Boolean.getBoolean("route.westFocusFront")){
            Mob front=Dungeon.level.mobs.stream().filter(m->m.id()==279).findFirst().get();
            Mob rear=Dungeon.level.mobs.stream().filter(m->m.id()==281).findFirst().get();
            for(int n=0;n<4 && !Dungeon.level.adjacent(Dungeon.hero.pos,front.pos);n++){
                assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,rear.pos));click(rear.pos);
            }
            assertTrue("Northern corridor blocker must actually arrive",Dungeon.level.adjacent(Dungeon.hero.pos,front.pos));
            for(int n=0;n<8 && front.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,front.pos));click(front.pos);}
            assertFalse("Ordinary northern skeleton must really die",front.isAlive());log("ACTUAL NORTHERN SKELETON CLEAR HEALED MINION NOT REMOVED");
            if(System.getProperty("route.hourglassFrontClearOut")!=null)writeCheckpoint(System.getProperty("route.hourglassFrontClearOut"));
            return;
        }
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==281).findFirst().get();
        for(int n=0;n<5 && skeleton.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,skeleton.pos));click(skeleton.pos);}
        assertFalse("Real chasing necro skeleton must be dead",skeleton.isAlive());log("ACTUAL NECRO SKELETON CLEAR WITH ITS REAL EXPLOSION");
        if(System.getProperty("route.hourglassWestClearOut")!=null)writeCheckpoint(System.getProperty("route.hourglassWestClearOut"));
    }
    private void eighthFloorHourglassCasterFocus() throws Exception {
        assertEquals(1098,Dungeon.hero.pos);
        for(int cell:new int[]{1130,1163}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
        Mob necro=Dungeon.level.mobs.stream().filter(m->m.id()==235).findFirst().get();assertEquals(1164,necro.pos);
        for(int n=0;n<6 && necro.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,necro.pos));click(necro.pos);}
        assertFalse("Original necromancer must actually die",necro.isAlive());
        assertFalse("Its minion must die through original owner-death code",Dungeon.level.mobs.stream().anyMatch(m->m.id()==281 && m.isAlive()));
        log("ACTUAL NECROMANCER AND OWNED MINION DEFEATED WITHOUT RESET");
        if(System.getProperty("route.hourglassCasterClearOut")!=null)writeCheckpoint(System.getProperty("route.hourglassCasterClearOut"));
    }
    private void ninthFloorGardenFight() throws Exception {
        assertEquals(905,Dungeon.hero.pos);
        activateOwnedHourglassFreeze();
        for(int cell:new int[]{950,995}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
        Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");assertNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
        log("TWO FROZEN GARDEN APPROACH MOVES RETAIN THE FINAL HOURGLASS CHARGE");
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==267).findFirst().get();
        cast(ShieldOfLight.INSTANCE,skeleton.pos);cast(GuidingLight.INSTANCE,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        for(int n=0;n<2 && skeleton.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,skeleton.pos));click(skeleton.pos);}
        assertFalse("Actual garden skeleton must be cleared",skeleton.isAlive());
        assertEquals(7,Dungeon.hero.lvl);assertEquals(2,Dungeon.hero.exp);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));Dungeon.hero.upgradeTalent(Talent.BLESS);
        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FLOOR NINE REAL SKELETON CLEAR EARNED BLESS AND ACTUAL BARRIER");
        if(System.getProperty("route.floorNineBlessOut")!=null)writeCheckpoint(System.getProperty("route.floorNineBlessOut"));
    }
    private void ninthFloorActualFood() throws Exception {
        assertEquals(995,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.pointsInTalent(Talent.BLESS));
        activateOwnedHourglassFreeze();click(1040);assertEquals(1040,Dungeon.hero.pos);click(1040);
        assertNotNull("Actual shuriken must be recovered first from the heap",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));
        assertNull("Two owned frozen turns must be fully spent",Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
        click(1040);assertNotNull("Ration must actually enter owned inventory",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class));
        assertEquals(1040,Dungeon.hero.pos);log("ACTUAL FLOOR NINE RATION ACQUIRED WITH RETAINED GUARD AND REAL RESOURCES");
        if(System.getProperty("route.floorNineFoodOut")!=null)writeCheckpoint(System.getProperty("route.floorNineFoodOut"));
    }
    private void ninthFloorEatAcquiredRation() throws Exception {
        assertEquals(1040,Dungeon.hero.pos);
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);
        food.execute(Dungeon.hero,"EAT");ready();assertEquals(1,Dungeon.hero.HP);assertEquals(1,Dungeon.hero.HT);
        assertEquals(3f,Dungeon.hero.buff(Hunger.class).hunger(),0.001f);
        log("ACTUAL ACQUIRED FLOOR NINE RATION EATEN UNDER CONTINUOUS PURSUIT");
        if(System.getProperty("route.floorNineMealOut")!=null)writeCheckpoint(System.getProperty("route.floorNineMealOut"));
    }
    private void ninthFloorPulledGuardFight() throws Exception {
        assertEquals(903,Dungeon.hero.pos);Mob guard=Dungeon.level.mobs.stream().filter(m->m.id()==286).findFirst().get();
        if(Boolean.getBoolean("route.nineKiteRage")){
            for(int cell:new int[]{948,993,1039,1084}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
            log("REAL GUARD KITE THROUGH FOUR SAFE MOVEMENTS TO SOUTH DOOR");
            Item rage=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class);assertNotNull(rage);rage.execute(Dungeon.hero,"READ");ready();log("RETAINED RAGE AT REAL THREE ENEMY DOOR ENCOUNTER");
            click(1083);assertEquals(1083,Dungeon.hero.pos);log("STEP OUT OF THE DOOR TO ALLOW ORIGINAL ENEMY INTERACTIONS");
            if(System.getProperty("route.floorNineKiteOut")!=null)writeCheckpoint(System.getProperty("route.floorNineKiteOut"));return;
        }
        for(int n=0;n<Integer.getInteger("route.nineGuardAttacks",6) && guard.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,guard.pos));click(guard.pos);}
        log("ACTUAL FLOOR NINE POST MEAL GUARD FIGHT WITH PAID ATTACKS");
        if(System.getProperty("route.floorNineGuardFightOut")!=null)writeCheckpoint(System.getProperty("route.floorNineGuardFightOut"));
    }
    private void ninthFloorKiteRecharge() throws Exception {
        assertEquals(1083,Dungeon.hero.pos);
        for(int cell:new int[]{1082,1036,989,942}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
        if(Boolean.getBoolean("route.stopKiteAfterFour")){
            Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==272).findFirst().get();boolean[] fov=new boolean[Dungeon.level.length()];Dungeon.level.updateFieldOfView(dm,fov);
            for(int cell:new int[]{897,941,942,943,987,988,989}){
                com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica ray=new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(dm.pos,cell,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT);
                System.out.println("READ_ONLY_DM_GEOMETRY cell="+cell+" terrain="+Dungeon.level.map[cell]+" seenByDM="+fov[cell]+" collision="+ray.collisionPos+" DMpos="+dm.pos);
            }
            log("ACTUAL FOUR KITE MOVES BEFORE FATAL FIFTH CELL");
            if(System.getProperty("route.floorNineFourKiteOut")!=null)writeCheckpoint(System.getProperty("route.floorNineFourKiteOut"));return;
        }
        click(Integer.getInteger("route.kiteFifthCell",943));
        assertTrue("Walking must actually earn the whole charge",charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1);
        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("ACTUAL KITE AND ENEMY DUEL RECHARGE EARNS SECOND BLESS");
        if(System.getProperty("route.floorNineSecondBlessOut")!=null)writeCheckpoint(System.getProperty("route.floorNineSecondBlessOut"));
    }
    private void ninthFloorSafeCornerBless() throws Exception {
        assertEquals(942,Dungeon.hero.pos);click(941);assertEquals(941,Dungeon.hero.pos);
        assertTrue("The safe step must earn the charge without restoring it",charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1);
        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("SAFE CORNER 941 EARNS REAL BLESS WHILE DM RAY REMAINS BLOCKED");
        if(System.getProperty("route.floorNineSafeBlessOut")!=null)writeCheckpoint(System.getProperty("route.floorNineSafeBlessOut"));
    }
    private void ninthFloorDoorwayApproach() throws Exception {
        assertEquals(941,Dungeon.hero.pos);
        click(942);assertEquals(942,Dungeon.hero.pos);click(897);assertEquals(897,Dungeon.hero.pos);
        Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==272).findFirst().get();
        com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica ray=new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,dm.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE);
        System.out.println("READ_ONLY_DOORWAY_SHOT target="+dm.pos+" targetHP="+dm.HP+" collision="+ray.collisionPos+" heroSeesDM="+Dungeon.level.heroFOV[dm.pos]);
        log("ACTUAL DOORWAY APPROACH AFTER SAFE CORNER BLESS");
        if(System.getProperty("route.floorNineDoorwayOut")!=null)writeCheckpoint(System.getProperty("route.floorNineDoorwayOut"));
    }
    private void ninthFloorSecretTransit() throws Exception {
        assertEquals(897,Dungeon.hero.pos);
        for(int cell:new int[]{852,853,854,855}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
        Dungeon.hero.search(true);ready();log("ACTUAL SEARCH OF SECRET TRANSIT DOOR 856");
        for(int cell:new int[]{856,811}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
        log("ACTUAL SECRET TRANSIT WITH PURSUING GUARD SKELETON AND DM");
        if(System.getProperty("route.floorNineTransitOut")!=null)writeCheckpoint(System.getProperty("route.floorNineTransitOut"));
    }
    private void ninthFloorLowHpDmShot() throws Exception {
        assertEquals(811,Dungeon.hero.pos);
        Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==272).findFirst().get();assertEquals(5,dm.HP);
        assertNull("The original twenty-turn free missile cooldown must have elapsed",Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.ShurikenInstantTracker.class));
        assertEquals("Projectile line must really reach this DM",dm.pos,(int)new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,dm.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,dm.pos);log("ACTUAL FREE MISSILE ATTEMPT ON FIVE HP DM NO HIT GUARANTEE");
        if(System.getProperty("route.floorNineShotOut")!=null)writeCheckpoint(System.getProperty("route.floorNineShotOut"));
    }
    private void ninthFloorLiveLanding() throws Exception {
        assertEquals(9,Dungeon.depth);assertEquals(944,Dungeon.hero.pos);
        activateOwnedHourglassFreeze();
        Item healing=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class);
        assertNotNull(healing);healing.execute(Dungeon.hero,"DRINK");ready();assertNull(Dungeon.hero.buff(Cripple.class));
        log("LIVE LANDING FROZEN HEALING CURE");
        click(943);assertEquals(943,Dungeon.hero.pos);
        if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
        assertNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
        click(942);assertEquals(942,Dungeon.hero.pos);
        for(int n=0;n<Integer.getInteger("route.liveLandingWait",2);n++)waitOne();
        log("LIVE LANDING SHADOW CORNER PAID RECHARGE");
        if(Boolean.getBoolean("route.liveFoodApproach")){
            cast(HolyWard.INSTANCE,null);
            for(int cell:new int[]{941,942}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
            click(897);if(Dungeon.hero.pos!=897)click(897);assertEquals(897,Dungeon.hero.pos);
            for(int cell:new int[]{852,853,854,855}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
            Dungeon.hero.search(true);ready();log("LIVE ACTUAL SEARCH OF TRANSIT 856");
            for(int cell:new int[]{856,857,858,905}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
            log("LIVE FLOOR NINE ACTUAL GARDEN APPROACH");
            if(Boolean.getBoolean("route.liveGardenClear")){
                activateOwnedHourglassFreeze();
                for(int cell:new int[]{950,995}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");assertNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
                Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==261).findFirst().get();
                cast(ShieldOfLight.INSTANCE,skeleton.pos);cast(GuidingLight.INSTANCE,skeleton.pos);
                throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
                for(int n=0;n<2 && skeleton.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,skeleton.pos));click(skeleton.pos);}
                assertFalse(skeleton.isAlive());assertEquals(6,Dungeon.hero.lvl);assertEquals(31,Dungeon.hero.exp);
                log("LIVE GARDEN SKELETON DEFEATED WITHOUT CREDITING UNEARNED BLESS");
                if(Boolean.getBoolean("route.liveGardenFood")){
                    click(1040);assertEquals(1040,Dungeon.hero.pos);click(1040);
                    Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);
                    food.execute(Dungeon.hero,"EAT");ready();log("LIVE ACTUAL FLOOR NINE RATION ACQUIRED AND EATEN");
                    if(Boolean.getBoolean("route.liveReturnRecharge")){
                        click(994);assertEquals(994,Dungeon.hero.pos);click(994);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));
                        for(int cell:new int[]{948,903,856,855,854,853,852,897,942}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                        if(Boolean.getBoolean("route.liveInterceptSkeleton")){
                            Mob second=Dungeon.level.mobs.stream().filter(m->m.id()==265).findFirst().get();assertEquals(1129,second.pos);
                            cast(ShieldOfLight.INSTANCE,second.pos);cast(GuidingLight.INSTANCE,second.pos);cast(GuidingLight.INSTANCE,second.pos);
                            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,second.pos);
                            for(int n=0;n<2 && second.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,second.pos));click(second.pos);}
                            assertFalse(second.isAlive());assertEquals(7,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.exp);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));
                            Dungeon.hero.upgradeTalent(Talent.BLESS);log("LIVE SECOND REAL SKELETON EARNS BLESS TALENT");
                            for(int n=0;n<Integer.getInteger("route.liveEarnBlessWait",25);n++)waitOne();
                            cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("LIVE ACTUAL FIRST BLESS AFTER EARNED RECHARGE");
                            if(Boolean.getBoolean("route.liveSouthDM")){
                                click(989);assertEquals(989,Dungeon.hero.pos);click(989);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));
                                activateOwnedHourglassFreeze();
                                for(int cell:new int[]{1036,1083,1128,1174}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");assertNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
                                Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==266).findFirst().get();assertEquals(1221,dm.pos);assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,dm.pos));
                                cast(ShieldOfLight.INSTANCE,dm.pos);
                                for(int n=0;n<5 && dm.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,dm.pos));click(dm.pos);}
                                assertFalse(dm.isAlive());log("LIVE SOUTH DM DEFEATED WITH OWNED BARRIER AND PAID FREEZE");
                                if(Boolean.getBoolean("route.liveNineExit")){
                                    for(int cell:new int[]{1129,1084,1039,994,949,904,905,952,999}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                    if(Boolean.getBoolean("route.liveEastRefreshBless"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                    Dungeon.hero.search(true);ready();log("LIVE SEARCH OF ACTUAL SECRET WATER EXIT 1046");
                                    click(1046);assertEquals(1046,Dungeon.hero.pos);log("LIVE SECRET WATER ROOM ENTRY");
                                    if(Boolean.getBoolean("route.liveNineEastDM")){
                                        click(1001);assertEquals(1001,Dungeon.hero.pos);log("LIVE EAST WATER FIRST NORMAL APPROACH BEFORE FREEZE");
                                        activateOwnedHourglassFreeze();
                                        for(int cell:new int[]{1048,1095}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                        if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                        assertNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
                                        click(1141);assertEquals(1141,Dungeon.hero.pos);
                                        Mob east=Dungeon.level.mobs.stream().filter(m->m.id()==267).findFirst().get();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,east.pos));
                                        if(!Boolean.getBoolean("route.liveEastRefreshBless"))cast(ShieldOfLight.INSTANCE,east.pos);
                                        for(int n=0;n<5 && east.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,east.pos));click(east.pos);}
                                        assertFalse(east.isAlive());log("LIVE EAST DM DEFEATED AFTER TWO FROZEN STEPS AND NORMAL ADJACENT STEP");
                                        if(Boolean.getBoolean("route.liveNineTeleport")){
                                            for(int cell:new int[]{1095,1048,1002}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                            click(1002);Item scroll=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation.class);assertNotNull(scroll);
                                            scroll.execute(Dungeon.hero,"READ");ready();log("LIVE OWNED FLOOR NINE TELEPORTATION ACTUALLY READ");
                                            if(Boolean.getBoolean("route.liveTeleportDM")){
                                                assertEquals(704,Dungeon.hero.pos);Mob third=Dungeon.level.mobs.stream().filter(m->m.id()==259).findFirst().get();assertEquals(750,third.pos);
                                                for(int n=0;n<5 && third.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,third.pos));click(third.pos);}
                                                assertFalse(third.isAlive());log("LIVE TELEPORT ROOM THIRD DM ACTUALLY DEFEATED");
                                                if(Boolean.getBoolean("route.liveTeleportFoodApproach")){
                                                    for(int cell:new int[]{749,748,747,746}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                    click(746);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class));log("LIVE SECOND FLOOR NINE RATION ACTUALLY OWNED NOT YET EATEN");
                                                    click(699);assertEquals(699,Dungeon.hero.pos);click(699);
                                                    assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby.class));log("LIVE FLOOR NINE LULLABY ACTUALLY ACQUIRED");
                                                    if(Boolean.getBoolean("route.liveWestRest")){
                                                        for(int n=0;n<Integer.getInteger("route.liveWestRestLimit",180);n++){
                                                            waitOne();
                                                            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos])){log("BOUNDED WESTERN REST STOPS AT FIRST VISIBLE ENEMY");break;}
                                                        }
                                                        log("LIVE WESTERN REST ACTUAL RESOURCE BOUNDARY");
                                                        if(Boolean.getBoolean("route.liveWestMealRest")){
                                                            Item ration=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(ration);if(Boolean.getBoolean("route.liveWestDeferMeal")){for(int n=0;n<3;n++)waitOne();}else{ration.execute(Dungeon.hero,"EAT");ready();}
                                                            for(int n=0;n<Integer.getInteger("route.liveWestSecondRest",170);n++){
                                                                waitOne();
                                                                if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos])){log("SECOND WESTERN REST STOPS AT FIRST VISIBLE ENEMY");break;}
                                                            }
                                                            cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("LIVE WEST RATION FINANCES RECHARGE AND EARNED BLESS");
                                                            if(Boolean.getBoolean("route.liveWestNecroApproach")){
                                                                cast(HolyWard.INSTANCE,null);
                                                                cast(HolyWeapon.INSTANCE,null);
                                                                Mob necro=Dungeon.level.mobs.stream().filter(m->m.id()==291).findFirst().get();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,necro.pos));
                                                                click(necro.pos);
                                                                log("LIVE WEST NECROMANCER APPROACH WITH OWNED DEFENSE");
                                                                if(Boolean.getBoolean("route.liveWestNecroFinish")){
                                                                    click(necro.pos);
                                                                    Mob minion=Dungeon.level.mobs.stream().filter(m->m.id()==298).findFirst().orElse(null);
                                                                    if(minion!=null && Dungeon.level.heroFOV[minion.pos])cast(ShieldOfLight.INSTANCE,minion.pos);
                                                                    for(int n=0;n<5 && necro.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,necro.pos));click(necro.pos);}
                                                                    assertFalse(necro.isAlive());log("LIVE WEST NECROMANCER ACTUALLY DEFEATED WITH CONTINUOUS RESOURCES");
                                                                    if(Boolean.getBoolean("route.liveWestDoorApproach")){
                                                                        click(744);assertEquals(744,Dungeon.hero.pos);log("LIVE WEST EXIT DOOR AFTER REAL NECROMANCER KILL");
                                                                        if(Boolean.getBoolean("route.liveWestThiefFight")){
                                                                            Mob thief=Dungeon.level.mobs.stream().filter(m->m.id()==292).findFirst().get();
                                                                            if(Boolean.getBoolean("route.liveWestDeferMeal") && !Dungeon.level.adjacent(Dungeon.hero.pos,thief.pos))click(743);
                                                                            for(int n=0;n<4 && thief.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,thief.pos));click(thief.pos);}
                                                                            assertFalse(thief.isAlive());log("LIVE WEST THIEF FIGHT ACTUAL EXIT RESOURCES");
                                                                            if(Boolean.getBoolean("route.liveWestRage")){
                                                                                Item rage=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class);assertNotNull(rage);rage.execute(Dungeon.hero,"READ");ready();
                                                                                log("LIVE WEST TWO GUARDS AFTER ACTUAL OWNED RAGE");
                                                                                if(Boolean.getBoolean("route.liveWestRageHits")){
                                                                                    Mob guard=Dungeon.level.mobs.stream().filter(m->m.id()==276).findFirst().get();
                                                                                    for(int n=0;n<(Boolean.getBoolean("route.liveWestLeanGuard")?10:3) && guard.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,guard.pos));click(guard.pos);}
                                                                                    log("LIVE WEST FIRST GUARD THREE PAID RAGE EXCHANGES");
                                                                                    if(Boolean.getBoolean("route.liveWestSleepRetreat")){
                                                                                        Item lullaby=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby.class);assertNotNull(lullaby);lullaby.execute(Dungeon.hero,"READ");ready();
                                                                                        for(int cell:new int[]{744,745,746,747,748,749}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                        for(int n=0;n<4;n++)waitOne();
                                                                                        log("LIVE WEST ACTUAL LULLABY NORMAL RETREAT AND FOUR NORMAL TICKS");
                                                                                        if(Boolean.getBoolean("route.liveWestSleepCamp")){
                                                                                            for(int n=0;n<Integer.getInteger("route.liveWestSleepCampLimit",140);n++){
                                                                                                waitOne();
                                                                                                if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && m.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep.class)==null)){log("SLEEP CAMP STOPS AT FIRST AWAKE VISIBLE ENEMY");break;}
                                                                                            }
                                                                                            if(!Boolean.getBoolean("route.liveWestLeanGuard") || Boolean.getBoolean("route.liveWestLeanIntercept"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("LIVE WEST SLEEP CAMP ACTUAL PAID RECHARGE");
                                                                                            if(Boolean.getBoolean("route.liveWestSleepReturn")){
                                                                                                if(Boolean.getBoolean("route.liveWestLeanIntercept")){click(748);assertEquals(746,Dungeon.hero.pos);}else{for(int cell:new int[]{748,747,746,745,744,743,742}){click(cell);assertEquals(cell,Dungeon.hero.pos);}}
                                                                                                log("LIVE WEST RETURN TO ACTUAL SLEEPING GUARD WITH EARNED RESERVE");
                                                                                                if(Boolean.getBoolean("route.liveWestLastGuard")){
                                                                                                    Mob lastGuard=Dungeon.level.mobs.stream().filter(m->m.id()==304).findFirst().get();
                                                                                                    if(Boolean.getBoolean("route.liveWestLeanGuard")){if(!Boolean.getBoolean("route.liveWestLeanIntercept")){cast(BlessSpell.INSTANCE,Dungeon.hero.pos);cast(ShieldOfLight.INSTANCE,lastGuard.pos);}}else{
                                                                                                        cast(HolyWard.INSTANCE,null);if(!Boolean.getBoolean("route.liveWestNoHoly"))cast(HolyWeapon.INSTANCE,null);cast(ShieldOfLight.INSTANCE,lastGuard.pos);cast(GuidingLight.INSTANCE,lastGuard.pos);
                                                                                                    }
                                                                                                    throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,lastGuard.pos);
                                                                                                    for(int n=0;n<(Boolean.getBoolean("route.liveWestLeanGuard")?12:5) && lastGuard.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,lastGuard.pos));click(lastGuard.pos);}
                                                                                                    assertFalse(lastGuard.isAlive());log("LIVE WEST FINAL BLOCKING GUARD ACTUALLY DEFEATED");
                                                                                                    if(Boolean.getBoolean("route.liveWestDeferMeal")){
                                                                                                        Item lateRation=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(lateRation);lateRation.execute(Dungeon.hero,"EAT");ready();log("LIVE OWNED RATION ACTUALLY EATEN AFTER FINAL GUARD");
                                                                                                    }

                                                                                                    if(Boolean.getBoolean("route.liveWestCorridor")){
                                                                                                        assertEquals(8,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));Dungeon.hero.upgradeTalent(Talent.BLESS);
                                                                                                        if(Boolean.getBoolean("route.liveWestAlchemyProbe")){
                                                                                                            if(Boolean.getBoolean("route.liveWestAlchemyRecoverHealing")){
                                                                                                                for(int cell:new int[]{745,745,744,743,742,742,743,744,745,746}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));log("LIVE WEST ACTUALLY RECOVER STOLEN HEALING FROM DEAD THIEF DROP");
                                                                                                            }
                                                                                                            for(int cell:new int[]{701,656,611,566}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                            Dungeon.hero.search(true);ready();log("LIVE WEST ACTUAL SEARCH FOR NORTH SECRET SHORTCUT 613");assertEquals(Terrain.DOOR,Dungeon.level.map[613]);
                                                                                                            for(int cell:new int[]{613,568,523,524,525,526,527,528}){click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                            log("LIVE WEST ACTUAL NORTHERN ALCHEMY ACCESS PROBE");
                                                                                                            if(Boolean.getBoolean("route.liveWestAlchemyKey")){
                                                                                                                if(!Boolean.getBoolean("route.liveWestAlchemyLateBless"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                for(int cell:new int[]{575,621,667,713,759,805,852,853,854,901,947,993,1040,1087,1087}){
                                                                                                                    assertNull(Dungeon.level.traps.get(cell));click(cell);
                                                                                                                    if(Boolean.getBoolean("route.liveWestAlchemyLateBless") && Dungeon.hero.pos!=cell && Dungeon.hero.buff(Cripple.class)!=null){
                                                                                                                        Mob alchemyGuard=Dungeon.level.mobs.stream().filter(m->m.id()==263).findFirst().get();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,alchemyGuard.pos));cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                        for(int hit=0;hit<8 && alchemyGuard.isAlive();hit++)click(alchemyGuard.pos);assertFalse(alchemyGuard.isAlive());log("LIVE WEST ALCHEMY KEY APPROACH REAL CHAIN GUARD DEFEATED");
                                                                                                                        if(Boolean.getBoolean("route.liveWestAlchemyGuardKey")){
                                                                                                                            for(int waypoint:new int[]{805,852}){click(waypoint);assertEquals(waypoint,Dungeon.hero.pos);}activateOwnedHourglassFreeze();
                                                                                                                            for(int waypoint:new int[]{853,854,901,947,993,1040,1087,1087}){assertNull(Dungeon.level.traps.get(waypoint));click(waypoint);assertEquals(waypoint,Dungeon.hero.pos);}
                                                                                                                            if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                                                                                                            assertTrue(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(9))>=1);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));log("LIVE WEST ACTUAL KEY AND RECOVERED HEALING AFTER REAL CHAIN GUARD");
                                                                                                                            if(Boolean.getBoolean("route.liveWestAlchemyGuardCraft")){
                                                                                                                                boolean alchemyDoorBlessCast=false;
                                                                                                                                for(int waypoint:new int[]{1040,993,947,901,856,811,765,719,672,627,581}){
                                                                                                                                    assertNull(Dungeon.level.traps.get(waypoint));click(waypoint);
                                                                                                                                    if(waypoint==856 && Boolean.getBoolean("route.liveWestAlchemyInterceptThief")){
                                                                                                                                        Mob alchemyThief=Dungeon.level.mobs.stream().filter(m->m.id()==258).findFirst().get();assertEquals(811,alchemyThief.pos);
                                                                                                                                        if(Boolean.getBoolean("route.liveWestAlchemyDropHealing")){Item safeHealing=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class);assertNotNull(safeHealing);safeHealing.execute(Dungeon.hero,"DROP");ready();log("LIVE WEST ACTUAL HEALING DROP BEFORE SECOND THIEF COMBAT");}
                                                                                                                                        else throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,alchemyThief.pos);
                                                                                                                                        if(!Boolean.getBoolean("route.liveWestAlchemyDropHealing")){for(int hit=0;hit<5 && alchemyThief.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,alchemyThief.pos));click(alchemyThief.pos);}assertFalse(alchemyThief.isAlive());log("LIVE WEST ALCHEMY APPROACH SECOND THIEF ACTUAL INTERCEPTION");}
                                                                                                                                        else{click(856);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class));log("LIVE WEST GROUND HEALING RECOVERED WHILE SECOND THIEF ESCAPES WITH ITS REAL STOLEN ITEM");}
                                                                                                                                    }
                                                                                                                                    if(waypoint==719 && Boolean.getBoolean("route.liveWestAlchemyFightNecro") && Dungeon.hero.pos!=719){
                                                                                                                                        Mob alchemyNecro=Dungeon.level.mobs.stream().filter(m->m.id()==256).findFirst().get();assertEquals(719,alchemyNecro.pos);assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,alchemyNecro.pos));cast(BlessSpell.INSTANCE,Dungeon.hero.pos);alchemyDoorBlessCast=true;
                                                                                                                                        for(int hit=0;hit<8 && alchemyNecro.isAlive();hit++)click(alchemyNecro.pos);assertFalse(alchemyNecro.isAlive());log("LIVE WEST ACTUAL ALCHEMY BLOCKING NECROMANCER DEFEATED");
                                                                                                                                    }
                                                                                                                                    if(Dungeon.hero.pos!=waypoint)click(waypoint);assertEquals(waypoint,Dungeon.hero.pos);
                                                                                                                                    if(waypoint==719 && Boolean.getBoolean("route.liveWestAlchemyDropHealing") && !alchemyDoorBlessCast){cast(BlessSpell.INSTANCE,Dungeon.hero.pos);alchemyDoorBlessCast=true;}
                                                                                                                                }
                                                                                                                                if(!Boolean.getBoolean("route.liveWestAlchemyDropHealing"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                                for(int waypoint:new int[]{534,487,440,440,393,347}){click(waypoint);if(Dungeon.hero.pos!=waypoint)click(waypoint);assertEquals(waypoint,Dungeon.hero.pos);}
                                                                                                                                click(301);craftShieldingAtActualPot(301);log("LIVE WEST RECOVERED HEALING AND EARNED KEY ENERGY BECOME ACTUAL SHIELDING");
                                                                                                                                if(Boolean.getBoolean("route.liveWestAlchemySecondShield")){
                                                                                                                                    Item aggression=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression.class);assertNotNull(aggression);assertTrue(com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem.selector.itemSelectable(aggression));int energyBeforeStone=Dungeon.energy,stoneEnergy=aggression.energyVal();
                                                                                                                                    com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem.energizeAll(aggression);ready();assertEquals(energyBeforeStone+stoneEnergy,Dungeon.energy);assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression.class));log("ENERGIZE ACTUALLY ACQUIRED AGGRESSION STONE energy="+stoneEnergy);
                                                                                                                                    click(301);craftShieldingAtActualPot(301);assertEquals(2,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class).quantity());log("LIVE WEST TWO ACTUALLY CRAFTED PRISON SHIELDING POTIONS");
                                                                                                                                    if(Boolean.getBoolean("route.liveWestAlchemyReturnEight")){
                                                                                                                                        for(int rest=0;rest<120;rest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}
                                                                                                                                        log("LIVE WEST REAL PAID ALCHEMY ROOM REST BEFORE FLOOR EIGHT RETURN");
                                                                                                                                        if(Boolean.getBoolean("route.liveWestAlchemyRestGuard")){
                                                                                                                                            Mob roomGuard=Dungeon.level.mobs.stream().filter(m->m.id()==262).findFirst().get();for(int wait=0;wait<6 && !Dungeon.level.adjacent(Dungeon.hero.pos,roomGuard.pos);wait++)waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,roomGuard.pos));cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                                            for(int hit=0;hit<20 && roomGuard.isAlive();hit++){
                                                                                                                                                if(Dungeon.hero.shielding()<=3){Item reserve=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class);assertNotNull(reserve);reserve.execute(Dungeon.hero,"DRINK");ready();}
                                                                                                                                                click(roomGuard.pos);
                                                                                                                                            }
                                                                                                                                            assertFalse(roomGuard.isAlive());log("LIVE WEST ACTUAL ALCHEMY REST INTERRUPTING GUARD DEFEATED");
                                                                                                                                            if(Boolean.getBoolean("route.liveWestAlchemyTerror")){
                                                                                                                                                assertTrue(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey(9))>=1);
                                                                                                                                                for(int terrorWaypoint:new int[]{534,489,443,398,353,306,305,305}){assertNull(Dungeon.level.traps.get(terrorWaypoint));click(terrorWaypoint);if(Dungeon.hero.pos!=terrorWaypoint)click(terrorWaypoint);assertEquals(terrorWaypoint,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror.class));log("LIVE WEST ACTUAL CRYSTAL KEY SPENT TO ACQUIRE TERROR");
                                                                                                                                                for(int rest=0;rest<100 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<2;rest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}log("LIVE WEST TERROR NOOK PAID REST BOUNDARY");
                                                                                                                                                if(Boolean.getBoolean("route.liveWestTerrorReturnEight")){
                                                                                                                                                    cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                                                    for(int terrorExitCell:new int[]{306,353,398,443,489,534,581,627,672,673}){assertNull(Dungeon.level.traps.get(terrorExitCell));click(terrorExitCell);if(Dungeon.hero.pos!=terrorExitCell)click(terrorExitCell);assertEquals(terrorExitCell,Dungeon.hero.pos);}click(674);if(Dungeon.depth==9)click(674);assertEquals(8,Dungeon.depth);log("LIVE WEST TERROR NOOK ACTUAL RETURN TO EIGHT");
                                                                                                                                                    if(Boolean.getBoolean("route.liveWestEightTerrorTransit")){
                                                                                                                                                        Item actualTerror=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror.class);assertNotNull(actualTerror);
                                                                                                                                                        if(Boolean.getBoolean("route.liveWestEightSaveTerror")){activateOwnedHourglassFreeze();}else{actualTerror.execute(Dungeon.hero,"READ");ready();assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror.class));log("LIVE WEST ACTUAL OWNED TERROR USED ON STAIR GUARD");}
                                                                                                                                                        for(int eightTransitCell:new int[]{507,540,572,604,636,668,700,732,763,762}){assertNull(Dungeon.level.traps.get(eightTransitCell));click(eightTransitCell);if(Dungeon.hero.pos!=eightTransitCell)click(eightTransitCell);assertEquals(eightTransitCell,Dungeon.hero.pos);if(eightTransitCell==540 && Boolean.getBoolean("route.liveWestEightSaveTerror") && Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");}log("LIVE WEST ACTUAL FLOOR EIGHT EASTERN TRANSIT");
                                                                                                                                                        if(Boolean.getBoolean("route.liveWestEightSouthernTransit")){
                                                                                                                                                            for(int southernTransitCell:new int[]{793,825,857,889,921,953,984,1015,1046,1077,1076,1043,1042,1073,1072}){assertNull(Dungeon.level.traps.get(southernTransitCell));
                                                                                                                                                                if(southernTransitCell==1077 && Boolean.getBoolean("route.liveWestEightSaveTerror")){Item chamberTerror=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror.class);assertNotNull(chamberTerror);chamberTerror.execute(Dungeon.hero,"READ");ready();assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror.class));log("LIVE WEST ACTUAL TERROR RESERVED FOR DM AND SKELETON ROOM");}
                                                                                                                                                                if(southernTransitCell==1077 && Boolean.getBoolean("route.liveWestEightSouthernDefense")){cast(BlessSpell.INSTANCE,Dungeon.hero.pos);if(!Boolean.getBoolean("route.liveWestEightLaterFreeze"))activateOwnedHourglassFreeze();}if(southernTransitCell==1043 && Boolean.getBoolean("route.liveWestEightLaterFreeze"))activateOwnedHourglassFreeze();
                                                                                                                                                                click(southernTransitCell);if(Dungeon.hero.pos!=southernTransitCell)click(southernTransitCell);assertEquals(southernTransitCell,Dungeon.hero.pos);
                                                                                                                                                                if(southernTransitCell==1042 && (Boolean.getBoolean("route.liveWestEightChamberPassage") || Boolean.getBoolean("route.liveWestEightChainStasis"))){
                                                                                                                                                                    if(Boolean.getBoolean("route.liveWestEightChainStasis")){
                                                                                                                                                                        click(1073);assertEquals(1076,Dungeon.hero.pos);assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class));cast(BlessSpell.INSTANCE,Dungeon.hero.pos);activateOwnedHourglassStasis();assertNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class));
                                                                                                                                                                        for(int chainStasisExitCell:new int[]{1075,1074,1073,1072,1071,1102}){assertNull(Dungeon.level.traps.get(chainStasisExitCell));click(chainStasisExitCell);if(Dungeon.hero.pos!=chainStasisExitCell)click(chainStasisExitCell);assertEquals(chainStasisExitCell,Dungeon.hero.pos);}log("LIVE WEST ACTUAL GUARD CHAIN SPENT AND CRIPPLE CLEARED BY PAID STASIS");
                                                                                                                                                                    }else{
                                                                                                                                                                        activateOwnedHourglassFreeze();for(int frozenChamberCell:new int[]{1073,1104}){click(frozenChamberCell);assertEquals(frozenChamberCell,Dungeon.hero.pos);}if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                                                                    }
                                                                                                                                                                    for(int chamberExitCell:new int[]{1135,1166,1165,1164,1131}){assertNull(Dungeon.level.traps.get(chamberExitCell));click(chamberExitCell);if(Dungeon.hero.pos!=chamberExitCell)click(chamberExitCell);assertEquals(chamberExitCell,Dungeon.hero.pos);}log("LIVE WEST ACTUAL SOUTHERN ROOM PASSAGE WITH RESERVED TERROR");
                                                                                                                                                                    if(Boolean.getBoolean("route.liveWestEightWesternFoodApproach")){
                                                                                                                                                                        for(int westernFoodCell:new int[]{1098,1066,1034,1002,970,937,904,903,870,838,806,774,742,710,678,646,614,582,551,520,489,457,426}){assertNull(Dungeon.level.traps.get(westernFoodCell));
                                                                                                                                                                            if(westernFoodCell==1002 && Boolean.getBoolean("route.liveWestEightChainWesternDefense"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                                                                            if(westernFoodCell==970 && Boolean.getBoolean("route.liveWestEightChainWesternDefense")){
                                                                                                                                                                                Mob westernDM=Dungeon.level.mobs.stream().filter(m->m.id()==245).findFirst().get();assertEquals(970,westernDM.pos);assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,westernDM.pos));throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,westernDM.pos);for(int westDMHit=0;westDMHit<8 && westernDM.isAlive();westDMHit++)click(westernDM.pos);assertFalse(westernDM.isAlive());log("LIVE WEST ACTUAL MOVED WESTERN DM DEFEATED IN CHAIN STASIS VARIANT");
                                                                                                                                                                            }
                                                                                                                                                                            click(westernFoodCell);if(Dungeon.hero.pos!=westernFoodCell)click(westernFoodCell);assertEquals(westernFoodCell,Dungeon.hero.pos);
                                                                                                                                                                            if(westernFoodCell==970 && Boolean.getBoolean("route.liveWestEightChainWesternDefense")){for(int westLoot=0;westLoot<3 && Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class)==null;westLoot++)click(970);assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));log("LIVE WEST ACTUAL WESTERN DM THROWN SHURIKEN RECOVERED");}
                                                                                                                                                                        }log("LIVE WEST ACTUAL WESTERN FOOD SECRET DOOR APPROACH");
                                                                                                                                                                        if(Boolean.getBoolean("route.liveWestEightFoodDoor")){
                                                                                                                                                                            cast(BlessSpell.INSTANCE,Dungeon.hero.pos);Dungeon.hero.search(true);ready();assertEquals(com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR,Dungeon.level.map[427]);log("LIVE WEST ACTUAL FOOD SECRET DOOR REVEALED WITH PAID SEARCH");click(427);if(Dungeon.hero.pos!=427)click(427);assertEquals(427,Dungeon.hero.pos);log("LIVE WEST ACTUAL FOOD ROOM DOOR ENTERED");
                                                                                                                                                                            if(Boolean.getBoolean("route.liveWestEightFoodDM")){
                                                                                                                                                                                Mob foodDM=Dungeon.level.mobs.stream().filter(m->m.id()==247).findFirst().get();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,foodDM.pos));throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,foodDM.pos);
                                                                                                                                                                                for(int foodDMHit=0;foodDMHit<8 && foodDM.isAlive();foodDMHit++){
                                                                                                                                                                                    if(Dungeon.hero.shielding()<=7 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1)cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                                                                                    else if(Dungeon.hero.shielding()<=3){Item foodShield=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class);assertNotNull(foodShield);foodShield.execute(Dungeon.hero,"DRINK");ready();}
                                                                                                                                                                                    click(foodDM.pos);
                                                                                                                                                                                }assertFalse(foodDM.isAlive());log("LIVE WEST ACTUAL FOOD ROOM DM DEFEATED WHILE PURSUERS REMAIN ACTIVE");
                                                                                                                                                                                if(Boolean.getBoolean("route.liveWestEightPasty")){
                                                                                                                                                                                    for(int actualPastyCell:new int[]{396,365,365,366,366}){assertNull(Dungeon.level.traps.get(actualPastyCell));click(actualPastyCell);if(Dungeon.hero.pos!=actualPastyCell)click(actualPastyCell);assertEquals(actualPastyCell,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty.class));assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class));log("LIVE WEST ACTUAL PASTY AND LEVITATION ACQUIRED FROM GUARDED ROOM");
                                                                                                                                                                                    if(Boolean.getBoolean("route.liveWestEightLevitationExit")){Item acquiredLevitation=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class);assertNotNull(acquiredLevitation);acquiredLevitation.execute(Dungeon.hero,"DRINK");ready();log("LIVE WEST ACTUAL LEVITATION DRUNK BEFORE MEAL TO PREVENT ITS THEFT");}
                                                                                                                                                                                    if(!Boolean.getBoolean("route.liveWestEightNoMealBless"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty.class).execute(Dungeon.hero,"EAT");ready();log("LIVE WEST ACTUAL ACQUIRED PASTY EATEN WITH PURSUERS ACTIVE");
                                                                                                                                                                                    if(Boolean.getBoolean("route.liveWestEightLevitationExit")){
                                                                                                                                                                                        assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation.class));log("LIVE WEST ACTUAL ACQUIRED LEVITATION USED FOR TRAP AND CHASM RETURN");
                                                                                                                                                                                        for(int levitationExitCell:new int[]{335,336,337,338,339,340,341,342,343,344,377,410,443}){assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation.class));
                                                                                                                                                                                            if(levitationExitCell==340 && Boolean.getBoolean("route.liveWestEightGuardExitReserve")){
                                                                                                                                                                                                Item gateReserve=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class);assertNotNull(gateReserve);gateReserve.execute(Dungeon.hero,"DRINK");ready();log("LIVE WEST ACTUAL LAST SHIELDING USED BEFORE NORTHERN GUARDS");activateOwnedHourglassStasis();log("LIVE WEST ACTUAL POST MEAL GUARD GATE PAID STASIS");
                                                                                                                                                                                                if(Boolean.getBoolean("route.liveWestEightGuardGateContinue")){
                                                                                                                                                                                                    for(int gateApproachCell:new int[]{340,341,342}){if(gateApproachCell==341)assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation.class));click(gateApproachCell);if(Dungeon.hero.pos!=gateApproachCell)click(gateApproachCell);assertEquals(gateApproachCell,Dungeon.hero.pos);}Mob northernGuard=Dungeon.level.mobs.stream().filter(m->m.id()==243).findFirst().get();if(northernGuard.pos==343){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,northernGuard.pos));
                                                                                                                                                                                                    for(int gateGuardHit=0;gateGuardHit<16 && northernGuard.isAlive();gateGuardHit++){if(Dungeon.hero.shielding()<=12 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1)cast(BlessSpell.INSTANCE,Dungeon.hero.pos);click(northernGuard.pos);}assertFalse(northernGuard.isAlive());log("LIVE WEST ACTUAL NORTHERN GUARD GATE DEFEATED AFTER FED RETURN");
                                                                                                                                                                                                    assertEquals(9,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));Dungeon.hero.upgradeTalent(Talent.SUNRAY);log("LIVE WEST ACTUAL LEVEL NINE SUNRAY EARNED");}else{assertNull(Actor.findChar(343));log("LIVE WEST ACTUAL STASIS LET NORTHERN GUARD WANDER CLEAR OF EXIT");}
                                                                                                                                                                                                    for(int gateClearedCell:new int[]{343,344}){click(gateClearedCell);if(Dungeon.hero.pos!=gateClearedCell)click(gateClearedCell);assertEquals(gateClearedCell,Dungeon.hero.pos);}assertNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation.class));jumpIntoKnownChasm(377);assertEquals(9,Dungeon.depth);log("LIVE WEST ACTUAL FED NORTHERN CHASM FALL RETURN TO NINE");
                                                                                                                                                                                                    if(Boolean.getBoolean("route.liveFedNineGuard")){
                                                                                                                                                                                                        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);Mob fedLandingGuard=Dungeon.level.mobs.stream().filter(m->m.id()==335).findFirst().get();for(int landingWait=0;landingWait<6 && !Dungeon.level.adjacent(Dungeon.hero.pos,fedLandingGuard.pos);landingWait++)waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,fedLandingGuard.pos));
                                                                                                                                                                                                        for(int landingGuardHit=0;landingGuardHit<18 && fedLandingGuard.isAlive();landingGuardHit++){if(Dungeon.hero.shielding()<=4 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1)cast(BlessSpell.INSTANCE,Dungeon.hero.pos);click(fedLandingGuard.pos);}assertFalse(fedLandingGuard.isAlive());assertEquals(9,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));Dungeon.hero.upgradeTalent(Talent.SUNRAY);log("LIVE WEST ACTUAL FED FLOOR NINE LANDING GUARD DEFEATED LEVEL NINE SUNRAY EARNED");
                                                                                                                                                                                                        if(Boolean.getBoolean("route.liveFedNineNook")){
                                                                                                                                                                                                            for(int fedNookCell:new int[]{612,613,568,523,524,525,526,527,528,575,621,667,713,760,761,762,717,672,627,581,534,489,443,398,353,306,305}){assertNull(Dungeon.level.traps.get(fedNookCell));click(fedNookCell);if(Dungeon.hero.pos!=fedNookCell)click(fedNookCell);assertEquals(fedNookCell,Dungeon.hero.pos);}log("LIVE WEST ACTUAL FED LEVEL NINE RETURNS TO CRYSTAL NOOK");
                                                                                                                                                                                                            for(int fedNookRest=0;fedNookRest<100 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;fedNookRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}log("LIVE WEST ACTUAL FED LEVEL NINE PAID NOOK REST BOUNDARY");
                                                                                                                                                                                                            if(Boolean.getBoolean("route.liveFedNineNookContinue")){
                                                                                                                                                                                                                Mob nookThief=Dungeon.level.mobs.stream().filter(m->m.id()==258).findFirst().get();
                                                                                                                                                                                                                if(Boolean.getBoolean("route.liveFedNineNookFightThief")){
                                                                                                                                                                                                                    assertEquals("Hunting",nookThief.state.getClass().getSimpleName());assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,nookThief.pos));cast(BlessSpell.INSTANCE,Dungeon.hero.pos);for(int nookThiefHit=0;nookThiefHit<10 && nookThief.isAlive();nookThiefHit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,nookThief.pos));click(nookThief.pos);}assertFalse(nookThief.isAlive());log("LIVE WEST ACTUAL CRYSTAL NOOK HUNTING THIEF DEFEATED WITH OWNED BLESS");
                                                                                                                                                                                                                }else{assertEquals("Fleeing",nookThief.state.getClass().getSimpleName());waitOne();waitOne();log("LIVE WEST ACTUAL TWO PAID TURNS WHILE THIEF FLEES PAST CRYSTAL NOOK");}
                                                                                                                                                                                                                for(int secondFedRest=0;secondFedRest<100 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;secondFedRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos] && !(m.id()==258 && m.state.getClass().getSimpleName().equals("Fleeing"))))break;waitOne();}log("LIVE WEST ACTUAL FED NOOK PAID RECHARGE CONTINUATION BOUNDARY");
                                                                                                                                                                                                                if(Boolean.getBoolean("route.liveFedNineNookGuard")){
                                                                                                                                                                                                                    Mob nookGuard=Dungeon.level.mobs.stream().filter(m->m.id()==336).findFirst().get();for(int nookGuardWait=0;nookGuardWait<6 && !Dungeon.level.adjacent(Dungeon.hero.pos,nookGuard.pos);nookGuardWait++)waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,nookGuard.pos));
                                                                                                                                                                                                                    for(int nookGuardHit=0;nookGuardHit<18 && nookGuard.isAlive();nookGuardHit++){if(Dungeon.hero.shielding()<=4 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1)cast(BlessSpell.INSTANCE,Dungeon.hero.pos);click(nookGuard.pos);}assertFalse(nookGuard.isAlive());log("LIVE WEST ACTUAL FOLLOWING CRYSTAL NOOK GUARD DEFEATED WITHOUT RESOURCE RESTORE");
                                                                                                                                                                                                                    for(int thirdNookRest=0;thirdNookRest<140 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;thirdNookRest++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}log("LIVE WEST ACTUAL THIRD FED NOOK PAID REST BOUNDARY");
                                                                                                                                                                                                                    if(Boolean.getBoolean("route.liveFedNineLeaveNook")){
                                                                                                                                                                                                                        activateOwnedHourglassStasis();cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("LIVE WEST ACTUAL PAID STASIS AND BLESS FOR SOUTHERN EXIT TRANSIT");
                                                                                                                                                                                                                        for(int fedSouthCell:new int[]{353,398,443,489,534,581,627,673}){assertNull(Dungeon.level.traps.get(fedSouthCell));click(fedSouthCell);if(Dungeon.hero.pos!=fedSouthCell)click(fedSouthCell);assertEquals(fedSouthCell,Dungeon.hero.pos);}log("LIVE WEST ACTUAL FED LEVEL NINE NOOK DEPARTURE INTO SOUTHERN PASSAGE");
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                }
                                                                                                                                                                                                            }
                                                                                                                                                                                                        }
                                                                                                                                                                                                    }
                                                                                                                                                                                                }return;
                                                                                                                                                                                            }
                                                                                                                                                                                            if(levitationExitCell==338 && Boolean.getBoolean("route.liveWestEightExitNecro")){
                                                                                                                                                                                                Mob exitNecro=Dungeon.level.mobs.stream().filter(m->m.id()==235).findFirst().get();assertEquals(338,exitNecro.pos);assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,exitNecro.pos));Item exitShield=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class);assertNotNull(exitShield);exitShield.execute(Dungeon.hero,"DRINK");ready();
                                                                                                                                                                                                for(int exitNecroHit=0;exitNecroHit<12 && exitNecro.isAlive();exitNecroHit++)click(exitNecro.pos);assertFalse(exitNecro.isAlive());log("LIVE WEST ACTUAL LEVITATION EXIT BLOCKING NECROMANCER DEFEATED");if(Boolean.getBoolean("route.liveWestEightExitStasis")){assertEquals(338,Dungeon.hero.pos);activateOwnedHourglassStasis();log("LIVE WEST ACTUAL STASIS AFTER EXIT NECROMANCER TO TEST GUARD BLOCKAGE");return;}if(!Boolean.getBoolean("route.liveWestEightExitNecroContinue"))return;
                                                                                                                                                                                            }click(levitationExitCell);if(Dungeon.hero.pos!=levitationExitCell)click(levitationExitCell);assertEquals(levitationExitCell,Dungeon.hero.pos);if(levitationExitCell==335 || levitationExitCell==341)assertTrue(Dungeon.level.traps.get(levitationExitCell).active);}click(476);if(Dungeon.depth==8)click(476);assertEquals(9,Dungeon.depth);log("LIVE WEST ACTUAL PASTY FED LEVITATION SHORTCUT RETURNS TO NINE");
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    }break;
                                                                                                                                                                }
                                                                                                                                                                if(southernTransitCell==(Boolean.getBoolean("route.liveWestEightLaterFreeze")?1072:1042) && Boolean.getBoolean("route.liveWestEightSouthernDefense")){if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");break;}
                                                                                                                                                            }log("LIVE WEST ACTUAL FLOOR EIGHT SOUTHERN TRANSIT BEFORE NECROMANCER");
                                                                                                                                                            if(Boolean.getBoolean("route.liveWestEightNecroPassage")){
                                                                                                                                                                Item southernShield=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding.class);assertNotNull(southernShield);southernShield.execute(Dungeon.hero,"DRINK");ready();log("LIVE WEST ACTUAL LAST PRISON SHIELDING USED BEFORE NECROMANCER PASSAGE");
                                                                                                                                                                for(int necroPassageCell:new int[]{1071,1102,1133,1164}){if(Dungeon.hero.shielding()<=10 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1)cast(BlessSpell.INSTANCE,Dungeon.hero.pos);assertNull(Dungeon.level.traps.get(necroPassageCell));click(necroPassageCell);if(Dungeon.hero.pos!=necroPassageCell)click(necroPassageCell);assertEquals(necroPassageCell,Dungeon.hero.pos);}log("LIVE WEST ACTUAL NECROMANCER ROOM SOUTH EXIT");
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }return;
                                                                                                                                        }
                                                                                                                                        assertTrue(charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=2);
                                                                                                                                        for(int waypoint:new int[]{393,440,487,534,581,627,672,673}){assertNull(Dungeon.level.traps.get(waypoint));click(waypoint);if(Dungeon.hero.pos!=waypoint)click(waypoint);assertEquals(waypoint,Dungeon.hero.pos);}
                                                                                                                                        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);click(674);if(Dungeon.depth==9)click(674);assertEquals(8,Dungeon.depth);log("LIVE WEST ACTUAL RETURN TO FLOOR EIGHT WITH EARNED POTIONS AND PRECAST BLESS");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);
                                                                                                                    if(cell==993 && Boolean.getBoolean("route.liveWestAlchemyLateBless"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                }
                                                                                                                assertTrue(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(9))>=1);log("LIVE WEST ACTUAL IRON KEY FOR LOCKED ALCHEMY ROOM ACQUIRED");
                                                                                                                if(Boolean.getBoolean("route.liveWestAlchemyEscape")){
                                                                                                                    activateOwnedHourglassFreeze();for(int cell:new int[]{1040,993,947,901,854,853,852,805}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                    if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                                                                                                    log("LIVE WEST OWNED HOURGLASS ESCAPE WITH ACTUAL IRON KEY");
                                                                                                                    if(Boolean.getBoolean("route.liveWestAlchemyEntry")){
                                                                                                                        for(int cell:new int[]{760,761,762,717,672,627,581,534,487,440,440}){assertNull(Dungeon.level.traps.get(cell));click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                        log("LIVE WEST ACTUAL KEY UNLOCK AND ENERGY CRYSTAL ACQUISITION");
                                                                                                                        for(int cell:new int[]{393,347}){click(cell);assertEquals(cell,Dungeon.hero.pos);}click(301);craftShieldingAtActualPot(301);log("LIVE WEST ACTUAL PRISON SHIELDING POTION CRAFTED");
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                            return;
                                                                                                        }
                                                                                                        if(Boolean.getBoolean("route.liveWestLeanIntercept"))for(int cell:new int[]{745,745,744,743,742}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                        for(int cell:new int[]{741,741,786,832,878,923,970,1017,1064,1111}){
                                                                                                            click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);
                                                                                                            if(cell==970 && Boolean.getBoolean("route.liveWestLeanDMBreach"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                            if(cell==970 && Boolean.getBoolean("route.liveWestClearPursuerChain")){
                                                                                                                cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                                                                click(923);assertEquals(923,Dungeon.hero.pos);click(878);assertEquals(878,Dungeon.hero.pos);click(832);assertEquals(878,Dungeon.hero.pos);
                                                                                                                log("LIVE WEST PURSUER ORIGINAL CHAIN LURED OUTSIDE DM ROOM");
                                                                                                                click(923);assertEquals(923,Dungeon.hero.pos);click(970);assertEquals(970,Dungeon.hero.pos);
                                                                                                            }
                                                                                                            if(cell==1064 && Boolean.getBoolean("route.liveWestLeanDMBreach")){
                                                                                                                activateOwnedHourglassFreeze();
                                                                                                                for(int waypoint:new int[]{1111,1112,1113,1160,1206}){click(waypoint);assertEquals(waypoint,Dungeon.hero.pos);}
                                                                                                                Mob breachDM=Dungeon.level.mobs.stream().filter(m->m.id()==260).findFirst().get();
                                                                                                                if(!Dungeon.level.adjacent(Dungeon.hero.pos,breachDM.pos)){click(1252);assertEquals(1252,Dungeon.hero.pos);}
                                                                                                                if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                                                                                                assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,breachDM.pos));throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,breachDM.pos);
                                                                                                                for(int hit=0;hit<5 && breachDM.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,breachDM.pos));click(breachDM.pos);}assertFalse(breachDM.isAlive());
                                                                                                                log("LIVE LEAN WEST DM FORCED INTO MELEE AND ACTUALLY DEFEATED");
                                                                                                                if(Boolean.getBoolean("route.liveWestLeanStairApproach")){
                                                                                                                    for(int waypoint:new int[]{1252,1252,1299,1300,1301,1302,1303,1350,1396,1443,1398,1399,1446}){
                                                                                                                        click(waypoint);if(Dungeon.hero.pos!=waypoint)click(waypoint);
                                                                                                                        if(waypoint==1303 && Boolean.getBoolean("route.liveWestLeanNecro")){
                                                                                                                            Mob blockingNecro=Dungeon.level.mobs.stream().filter(m->m.id()==264).findFirst().get();
                                                                                                                            for(int hit=0;hit<8 && blockingNecro.isAlive();hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,blockingNecro.pos));click(blockingNecro.pos);}assertFalse(blockingNecro.isAlive());
                                                                                                                            log("LIVE LEAN CORRIDOR BLOCKING NECROMANCER ACTUALLY DEFEATED");click(waypoint);
                                                                                                                        }
                                                                                                                        assertEquals(waypoint,Dungeon.hero.pos);
                                                                                                                    }
                                                                                                                    log("LIVE LEAN SOUTH ROOM ACTUAL STAIR ENEMIES");
                                                                                                                    if(Boolean.getBoolean("route.liveWestLeanFinishStairs")){
                                                                                                                        for(int waypoint:new int[]{1493,1448,1449}){click(waypoint);assertEquals(waypoint,Dungeon.hero.pos);}
                                                                                                                        click(1449);assertEquals(10,Dungeon.depth);log("LIVE LEAN PRISON ACTUAL TENGU FLOOR ARRIVAL");
                                                                                                                        if(Boolean.getBoolean("route.liveTenguKey"))liveTenguPreparation();
                                                                                                                    }

                                                                                                                }
                                                                                                                return;
                                                                                                            }
                                                                                                        }
                                                                                                        log("LIVE WEST CORRIDOR ACTUAL STAIR APPROACH AFTER EARNED BLESS TWO");
                                                                                                        if(Boolean.getBoolean("route.liveWestSouthPassage")){
                                                                                                            cast(BlessSpell.INSTANCE,Dungeon.hero.pos);assertEquals(15,Dungeon.hero.shielding());
                                                                                                            for(int cell:new int[]{1112,1113,1160,1206,1252,1299,1300,1301,1302,1303}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                            log("LIVE WEST SOUTH PASSAGE ACTUAL NEXT ENEMIES");
                                                                                                            if(Boolean.getBoolean("route.liveWestStairs")){
                                                                                                                for(int cell:new int[]{1350,1396,1443,1398,1399,1446,1493,1448,1449}){click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                                                                click(1449);assertEquals(10,Dungeon.depth);log("LIVE SEVEN THROUGH FLOOR NINE ACTUAL TENGU FLOOR ARRIVAL");
                                                                                                                if(Boolean.getBoolean("route.liveTenguKey"))liveTenguPreparation();

                                                                                                            }

                                                                                                        }

                                                                                                    }

                                                                                                }

                                                                                            }

                                                                                        }

                                                                                    }

                                                                                }

                                                                            }

                                                                        }

                                                                    }

                                                                }

                                                            }

                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            return;
                        }
                        for(int n=0;n<Integer.getInteger("route.liveSecondWait",140);n++)waitOne();
                        log("LIVE RATION FINANCES SECOND ACTUAL SAFE CORNER RECHARGE");
                    }
                }
            }
        }
    }
    private void liveTenguIntermission(Mob bossTengu) throws Exception {
                                    if(Boolean.getBoolean("route.liveLeanTenguIntermission")){
                                        if(Boolean.getBoolean("route.liveLeanTenguRecoverShard")){click(777);assertEquals(777,Dungeon.hero.pos);}
                                        for(int cell:new int[]{746,714,682,650,618,586,554,522,490,458,426,394,362,330,298}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                        for(int n=0;n<Integer.getInteger("route.liveLeanTenguIntermissionWait",32);n++)waitOne();
                                        log("LIVE LEAN TENGU PAID INTERMISSION RECHARGE AT ARENA THRESHOLD");
                                        if(Boolean.getBoolean("route.liveLeanTenguArena")){
                                            cast(BlessSpell.INSTANCE,Dungeon.hero.pos);click(266);
                                            assertEquals(com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.FIGHT_ARENA,((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).state());
                                            log("LIVE LEAN TENGU ACTUAL SECOND PHASE START");
                                            if(Boolean.getBoolean("route.liveLeanTenguArenaShard")){
                                                throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,bossTengu.pos);
                                                log("LIVE LEAN TENGU SECOND PHASE OWNED RECOVERED SHURIKEN");
                                                if(Boolean.getBoolean("route.liveLeanTenguEconomyArena")){
                                                    assertEquals(426,bossTengu.pos);
                                                    if(Boolean.getBoolean("route.liveLeanTenguEconomyLateSpells")){
                                                        for(int cell:new int[]{298,330}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                        activateOwnedHourglassFreeze();for(int cell:new int[]{329,360}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                        if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                                        click(391);assertEquals(391,Dungeon.hero.pos);log("LIVE ECONOMY TENGU LATE SPELL APPROACH WITH TWO OWNED CHARGES");
                                                        if(Boolean.getBoolean("route.liveLeanTenguLateShield")){
                                                            if(!Boolean.getBoolean("route.liveLeanTenguLateMeleeShield"))cast(ShieldOfLight.INSTANCE,bossTengu.pos);click(423);assertEquals(423,Dungeon.hero.pos);log("LIVE ECONOMY TENGU BOMB EVASION AND NEXT ABILITY");
                                                            if(Boolean.getBoolean("route.liveLeanTenguLateSouthApproach")){
                                                                for(int cell:new int[]{456,457,458,427}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                                cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                                if(Boolean.getBoolean("route.liveLeanTenguLateMeleeShield"))cast(Boolean.getBoolean("route.liveLeanTenguLateMeleeGuide")?GuidingLight.INSTANCE:ShieldOfLight.INSTANCE,bossTengu.pos);
                                                                for(int hit=0;hit<6 && bossTengu.pos==426;hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                                                                log("LIVE ECONOMY TENGU SOUTH APPROACH AND REAL MELEE");
                                                            }
                                                        }
                                                        return;
                                                    }
                                                    cast(GuidingLight.INSTANCE,bossTengu.pos);activateOwnedHourglassFreeze();
                                                    for(int cell:new int[]{298,330}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                    if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                                    cast(ShieldOfLight.INSTANCE,bossTengu.pos);
                                                    for(int cell:new int[]{362,394}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                    log("LIVE ECONOMY TENGU SECOND PHASE REAL APPROACH AND HAZARDS");return;
                                                }
                                                if(Boolean.getBoolean("route.liveLeanTenguArenaApproach")){
                                                    assertEquals(335,bossTengu.pos);
                                                    click(299);assertEquals(299,Dungeon.hero.pos);
                                                    click(332);assertEquals(332,Dungeon.hero.pos);
                                                    cast(ShieldOfLight.INSTANCE,bossTengu.pos);
                                                    int[] approach=Boolean.getBoolean("route.liveLeanTenguDodgeBomb")?new int[]{365,398,430,397,364,333,334}:new int[]{333,334};
                                                    for(int cell:approach){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                    if(Boolean.getBoolean("route.liveLeanTenguArenaMeleeBless"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                                    for(int hit=0;hit<5 && bossTengu.pos==335;hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                                                    log("LIVE LEAN TENGU SECOND PHASE FIRST REAL MELEE BRACKET");
                                                    if(Boolean.getBoolean("route.liveLeanTenguArenaSecond")){
                                                        assertEquals(106,bossTengu.pos);activateOwnedHourglassFreeze();
                                                        for(int cell:new int[]{302,270}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                        if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                                        for(int cell:new int[]{238,205,172,139}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                                        for(int hit=0;hit<5 && bossTengu.pos==106;hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                                                        log("LIVE LEAN TENGU SECOND PHASE SECOND MELEE BRACKET");
                                                    }
                                                }
                                            }
                                        }
                                    }
    }
    private void liveTenguPreparation() throws Exception {
        for(int n=0;n<Integer.getInteger("route.liveTenguPrep",80);n++)waitOne();
        for(int cell:new int[]{169,201,234,266,298,330,362,393,360,328,328}){click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
        log("LIVE TENGU ACTUAL IRON KEY ACQUIRED AFTER PAID PREPARATION");
        if(Boolean.getBoolean("route.liveTenguStart")){
            for(int cell:new int[]{360,393,426,458,490,522,554,585,618,650,682,714}){click(cell);if(Dungeon.hero.pos!=cell)click(cell);assertEquals(cell,Dungeon.hero.pos);}
            for(int n=0;n<3 && Dungeon.hero.pos!=746;n++)click(746);assertEquals(746,Dungeon.hero.pos);
            cast(HolyWard.INSTANCE,null);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);assertEquals(15,Dungeon.hero.shielding());
            click(778);assertEquals(778,Dungeon.hero.pos);assertTrue(Dungeon.level.locked);
            Mob bossTengu=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu).findFirst().get();
            if(!Boolean.getBoolean("route.liveLeanTenguSkipInitialShield"))cast(ShieldOfLight.INSTANCE,bossTengu.pos);
            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,bossTengu.pos);
            System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
            log("LIVE TENGU ACTUAL FIRST SHURIKEN FIRST JUMP AND FULL RESOURCES");
                if(Boolean.getBoolean("route.liveLeanTenguFirst")){
                    assertEquals(940,bossTengu.pos);cast(GuidingLight.INSTANCE,bossTengu.pos);activateOwnedHourglassFreeze();
                    for(int cell:new int[]{811,842,875,907}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                    if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                    for(int hit=0;hit<6 && bossTengu.pos==940;hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                    log("LIVE LEAN TENGU GUIDED FIRST BRACKET ACTUAL RESERVES");
                    if(Boolean.getBoolean("route.liveLeanTenguEconomySecond")){
                        assertEquals(813,bossTengu.pos);
                        JSONArray trapped=new JSONArray();for(int c=0;c<Dungeon.level.length();c++)if(Dungeon.level.traps.get(c)!=null)trapped.put(c);System.out.println("TENGU_ECONOMY_TRAPS "+trapped);
                        for(int cell:new int[]{875,843,811,780}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                        for(int hit=0;hit<6 && bossTengu.pos==813;hit++)click(bossTengu.pos);
                        log("LIVE ECONOMY TENGU SECOND ACTUAL BRACKET");
                        if(Boolean.getBoolean("route.liveLeanTenguEconomyThird")){
                            assertEquals(967,bossTengu.pos);activateOwnedHourglassFreeze();
                            for(int cell:new int[]{779,810,841,872}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                            if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                            for(int cell:new int[]{903,936}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                            cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                            for(int hit=0;hit<6 && bossTengu.pos==967;hit++)click(bossTengu.pos);
                            log("LIVE ECONOMY TENGU THIRD ACTUAL BRACKET");
                            if(Boolean.getBoolean("route.liveLeanTenguEconomyThirdFinish")){
                                for(int hit=0;hit<6 && bossTengu.pos==967;hit++)click(bossTengu.pos);
                                log("LIVE ECONOMY TENGU THIRD ACTUAL JUMP");
                                if(Boolean.getBoolean("route.liveLeanTenguEconomyFirstPhase")){
                                    assertEquals(777,bossTengu.pos);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                                    for(int cell:new int[]{905,874,843,812,779,778}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                    for(int hit=0;hit<10 && ((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).state()==com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.FIGHT_START;hit++)click(bossTengu.pos);
                                    assertEquals(com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.FIGHT_PAUSE,((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).state());
                                    log("LIVE ECONOMY TENGU ENTIRE FIRST PHASE COMPLETED");liveTenguIntermission(bossTengu);
                                }
                            }
                        }
                        return;
                    }
                    if(Boolean.getBoolean("route.liveLeanTenguSecond")){
                        assertEquals(780,bossTengu.pos);
                        for(int cell:new int[]{875,842,811}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                        for(int hit=0;hit<5 && bossTengu.pos==780;hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                        log("LIVE LEAN TENGU SECOND BRACKET AFTER THREE NORMAL SAFE MOVES");
                        if(Boolean.getBoolean("route.liveLeanTenguSecondFinish")){
                            cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                            for(int hit=0;hit<3 && bossTengu.pos==780;hit++)click(bossTengu.pos);
                            log("LIVE LEAN TENGU SECOND BRACKET FRESH BLESS AND ACTUAL NEXT JUMP");
                            if(Boolean.getBoolean("route.liveLeanTenguThird")){
                                assertEquals(940,bossTengu.pos);activateOwnedHourglassFreeze();
                                for(int cell:new int[]{812,845,876,908}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                                for(int hit=0;hit<6 && bossTengu.pos==940;hit++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                                log("LIVE LEAN TENGU THIRD BRACKET WITH EARNED SHIELD RESERVE");
                                if(Boolean.getBoolean("route.liveLeanTenguFirstPhase")){
                                    cast(BlessSpell.INSTANCE,Dungeon.hero.pos);assertEquals(777,bossTengu.pos);
                                    for(int cell:new int[]{876,844,812,779,778}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                    for(int hit=0;hit<10 && ((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).state()==com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.FIGHT_START;hit++){
                                        assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);
                                    }
                                    assertEquals(com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.FIGHT_PAUSE,((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).state());
                                    log("LIVE LEAN TENGU ENTIRE FIRST PHASE ACTUALLY COMPLETED");
                                    liveTenguIntermission(bossTengu);
                                }

                            }

                        }

                    }

                }

            if(Boolean.getBoolean("route.liveTenguFirstMelee")){
                activateOwnedHourglassFreeze();
                for(int cell:new int[]{810,843,874,905}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                assertNull(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
                for(int n=0;n<4 && bossTengu.pos==872;n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
                log("LIVE TENGU FIRST REAL MELEE EXCHANGES AFTER FOUR SAFE FROZEN MOVES");
                if(Boolean.getBoolean("route.liveTenguSecondMelee")){
                    assertEquals(780,bossTengu.pos);activateOwnedHourglassFreeze();
                    for(int cell:new int[]{873,841,810,779}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                    if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                    for(int n=0;n<5 && bossTengu.pos==780;n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                    log("LIVE TENGU SECOND REAL MELEE EXCHANGES AND ACTUAL NEXT JUMP");
                    if(Boolean.getBoolean("route.liveTenguThirdMelee")){
                        assertEquals(968,bossTengu.pos);activateOwnedHourglassFreeze();
                        for(int cell:new int[]{811,842}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                        if(Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class)!=null)Dungeon.hero.belongings.getItem(TimekeepersHourglass.class).execute(Dungeon.hero,"ACTIVATE");
                        for(int cell:new int[]{873,906,937}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                        for(int n=0;n<5 && bossTengu.pos==968;n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,bossTengu.pos));click(bossTengu.pos);}
                        log("LIVE TENGU THIRD MELEE AFTER LAST TWO FROZEN STEPS AND THREE NORMAL STEPS");
                        if(Boolean.getBoolean("route.liveTenguThirdExtra")){
                            for(int n=0;n<6 && bossTengu.pos==968 && ((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).state()==com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.FIGHT_START;n++)click(bossTengu.pos);
                            log("LIVE TENGU THIRD BRACKET ADDITIONAL REAL ATTACKS");
                            if(Boolean.getBoolean("route.liveTenguFourthApproach")){
                                for(int cell:new int[]{906,907,876,844,813}){assertNull(Dungeon.level.traps.get(cell));click(cell);assertEquals(cell,Dungeon.hero.pos);}
                                log("LIVE TENGU FOURTH NORMAL APPROACH ACTUAL SURVIVAL");
                            }

                        }

                    }

                }

            }

        }
    }

    private void ninthFloorSection() throws Exception {
        assertEquals(9,Dungeon.depth);
        if(Dungeon.hero.pos==944){ninthFloorLiveLanding();if(System.getProperty("route.liveNineOut")!=null)writeCheckpoint(System.getProperty("route.liveNineOut"));return;}
        if(Dungeon.hero.pos==811){ninthFloorLowHpDmShot();return;}
        if(Dungeon.hero.pos==897){ninthFloorSecretTransit();return;}
        if(Dungeon.hero.pos==941){ninthFloorDoorwayApproach();return;}
        if(Dungeon.hero.pos==942){ninthFloorSafeCornerBless();return;}
        if(Dungeon.hero.pos==1083){ninthFloorKiteRecharge();return;}
        if(Dungeon.hero.pos==903){ninthFloorPulledGuardFight();return;}
        if(Dungeon.hero.pos==1040){ninthFloorEatAcquiredRation();return;}
        if(Dungeon.hero.pos==995){ninthFloorActualFood();return;}
        if(Dungeon.hero.pos==905){ninthFloorGardenFight();return;}
        if(Dungeon.hero.pos==1141 && Boolean.getBoolean("route.nineContinue")){
            for(int cell:new int[]{1095,1048,1001}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
            Dungeon.hero.search(true);ready();log("REAL SEARCH OF FLOOR NINE SECRET EXIT 1046");
            cast(HolyWard.INSTANCE,null);
            for(int cell:new int[]{1046,999,952,905}){click(cell);assertEquals(cell,Dungeon.hero.pos);}
            log("ACTUAL FLOOR NINE GARDEN APPROACH BEFORE FOOD GUARD");
            if(System.getProperty("route.floorNineGardenOut")!=null)writeCheckpoint(System.getProperty("route.floorNineGardenOut"));return;
        }
        if(Dungeon.hero.pos==1141){
            for(int n=0;n<Integer.getInteger("route.nineRecharge",130);n++)waitOne();
            log("ACTUAL FLOOR NINE PAID RECHARGE AFTER ISOLATED DM");
            if(System.getProperty("route.floorNineRestOut")!=null)writeCheckpoint(System.getProperty("route.floorNineRestOut"));return;
        }
        assertEquals(1095,Dungeon.hero.pos);
        Mob dm=Dungeon.level.mobs.stream().filter(m->m.id()==273).findFirst().get();assertEquals(1188,dm.pos);
        activateOwnedHourglassFreeze();click(1141);assertEquals(1141,Dungeon.hero.pos);
        assertNull("One slowed movement must spend both frozen turns",Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class));
        assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,dm.pos));
        Item healing=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class);assertNotNull(healing);healing.execute(Dungeon.hero,"DRINK");ready();assertNull(Dungeon.hero.buff(Cripple.class));log("REAL FLOOR NINE CURE AFTER FROZEN ADJACENT APPROACH");
        for(int n=0;n<4 && dm.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,dm.pos));click(dm.pos);}
        assertFalse("Actual floor-nine landing DM must die",dm.isAlive());assertEquals(6,Dungeon.hero.lvl);assertEquals(32,Dungeon.hero.exp);
        log("ACTUAL FLOOR NINE ISOLATED DM DEFEATED WITH CONTINUOUS RESOURCES");
        if(System.getProperty("route.floorNineDMOut")!=null)writeCheckpoint(System.getProperty("route.floorNineDMOut"));
    }
    private void eighthFloorFallToNine() throws Exception {
        assertEquals(953,Dungeon.hero.pos);cast(HolyWard.INSTANCE,null);
        jumpIntoKnownChasm(922);assertEquals(9,Dungeon.depth);
        assertNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.Falling.class));
        waitOne();assertNull(Dungeon.hero.buff(Bleeding.class));assertEquals(1,Dungeon.hero.HP);assertEquals(1,Dungeon.hero.HT);
        log("ACTUAL FLOOR NINE FALL LANDING BEFORE CURE");
        if(Boolean.getBoolean("route.stopBeforeCure")){
            System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",9).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
            if(System.getProperty("route.floorNineFallOut")!=null)writeCheckpoint(System.getProperty("route.floorNineFallOut"));return;
        }
        Item healing=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class);assertNotNull(healing);
        healing.execute(Dungeon.hero,"DRINK");ready();assertNull(Dungeon.hero.buff(Cripple.class));log("CURE FALL CRIPPLE WITH OWNED HEALING POTION HP REMAINS ONE");
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",9).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        if(System.getProperty("route.floorNineFallOut")!=null)writeCheckpoint(System.getProperty("route.floorNineFallOut"));
    }
    private void eighthFloorSection() throws Exception {
        if(Dungeon.hero.pos==1098){if(Boolean.getBoolean("route.hourglassCasterFocus"))eighthFloorHourglassCasterFocus();else eighthFloorHourglassWestFight();return;}
        if(Dungeon.hero.pos==1131){eighthFloorHourglassThiefApproach();return;}
        if(Dungeon.hero.pos==1164){eighthFloorHourglassFirstBless();return;}
        if(Dungeon.hero.pos==1077){eighthFloorHourglassBlessEscape();return;}
        if(Dungeon.hero.pos==1139){eighthFloorHourglassFinishDM();return;}
        if(Dungeon.hero.pos==984){if(Boolean.getBoolean("route.hourglassMeleeDM"))eighthFloorHourglassDM();else eighthFloorHourglassDuel();return;}
        if(Dungeon.hero.pos==953){if(Boolean.getBoolean("route.fallToNine"))eighthFloorFallToNine();else eighthFloorHourglassSkeleton();return;}
        if(Dungeon.hero.pos==636){eighthFloorHourglassCentral();return;}
        if(Dungeon.hero.pos==214){eighthFloorHourglassGuard();return;}
        if(Dungeon.hero.pos==809){eighthFloorAfterBless();return;}
        assertEquals(997,Dungeon.hero.pos);
        if(Boolean.getBoolean("route.eightContinue")){eighthFloorAfterRest();return;}
        for(int n=0;n<Integer.getInteger("route.eightWait",225);n++)waitOne();
        log("FLOOR EIGHT PAID ENTRANCE RECHARGE");
        if(System.getProperty("route.checkpoint8RestOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint8RestOut"));
    }

    private void seventhFloorAfterIntercept() throws Exception {
        for(int c:new int[]{506,473,440,407,408,409,410,411,412,413,414,447,448,449,450,482,515,548,581,614,647,680,713,746,779,812})click(c);
        log("FLOOR SEVEN SOUTHBOUND APPROACH WITH RETAINED RAGE");
        for(int c:new int[]{845,878})click(c);
        log("FLOOR SEVEN EXIT GROUP BEFORE RAGE");
        if(Boolean.getBoolean("route.guardPullAudit")){
            try{click(911);}catch(AssertionError expected){
                if(Dungeon.hero.isAlive())throw expected;
                assertTrue(chainAnimationsCompleted>0);assertTrue(pushAnimationsCompleted>0);
                assertNotEquals(911,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HT);
                System.out.println("NEGATIVE_CONTROL_REAL_CHAIN_PULL_AND_DEATH pos="+Dungeon.hero.pos+" HP="+Dungeon.hero.HP);
                return;
            }
            fail("Unprotected chain route unexpectedly survived; inspect before updating this negative control");
        }
        Mob trailingSkeleton=Dungeon.level.mobs.stream().filter(m->m.id()==230).findFirst().get();
        cast(ShieldOfLight.INSTANCE,trailingSkeleton.pos);
        Item rage=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class);assertNotNull(rage);
        rage.execute(Dungeon.hero,"READ");ready();log("READ RETAINED RAGE BEFORE GUARD CHAIN");
        for(int c:new int[]{911,944,977,977})click(c);
        assertEquals(8,Dungeon.depth);log("EARNED FLOOR EIGHT ENTRY");
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        if(System.getProperty("route.checkpoint8Out")!=null)writeCheckpoint(System.getProperty("route.checkpoint8Out"));
    }

    private void seventhFloorFood() throws Exception {
        for(int n=0;n<40;n++)waitOne();
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);
        food.execute(Dungeon.hero,"EAT");ready();log("EAT ACQUIRED FLOOR SEVEN RATION AFTER PAID FORTY TURNS");
        for(int n=0;n<(Boolean.getBoolean("route.interceptThief")?31:65);n++)waitOne();
        log("FLOOR SEVEN SECOND FOOD FUNDED RECHARGE INTERRUPTED BY THIEF");
        Mob thief=Dungeon.level.mobs.stream().filter(m->m.id()==222).findFirst().get();
        if(Boolean.getBoolean("route.interceptThief")){
            cast(GuidingLight.INSTANCE,thief.pos);
            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,thief.pos);
            log("INTERCEPT THIRD THIEF APPROACH BEFORE RAGE THEFT");
            cast(GuidingLight.INSTANCE,thief.pos);
            log("INTERCEPT THIRD THIEF SECOND LIGHT");
            waitOne();waitOne();click(thief.pos);assertFalse(thief.isAlive());
            for(int c:new int[]{538,538,538})click(c);
            log("INTERCEPT THIEF DEFEATED RAGE RETAINED AND MISSILE RECOVERED");
            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage.class));
            assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class));
            if(System.getProperty("route.checkpoint7InterceptOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint7InterceptOut"));
            return;
        }
        cast(ShieldOfLight.INSTANCE,thief.pos);
        cast(GuidingLight.INSTANCE,thief.pos);
        click(thief.pos);log("FLOOR SEVEN RESPAWNED THIEF EXCHANGE");
        if(System.getProperty("route.checkpoint7FoodRestOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint7FoodRestOut"));
    }

    private void seventhFloorAfterRest() throws Exception {
        for(int c:new int[]{819,786,753,720,687,686,685,652,619,586,553,520,487,455,422,421,420,419})click(c);
        Dungeon.hero.search(true);ready();log("SEARCH FLOOR SEVEN EASTERN ARENA SECRET DOOR");
        for(int c:new int[]{418,417,416,415,414})click(c);
        cast(HolyWard.INSTANCE,null);
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==202).findFirst().get();
        cast(GuidingLight.INSTANCE,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        log("FLOOR SEVEN CENTRAL APPROACH");
        cast(GuidingLight.INSTANCE,skeleton.pos);
        cast(GuidingLight.INSTANCE,skeleton.pos);assertFalse(skeleton.isAlive());
        log("FLOOR SEVEN FIRST SKELETON DEFEATED");
        for(int c:new int[]{447,448,448,414})click(c);
        assertEquals(1,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class).quantity());
        Mob northSkeleton=Dungeon.level.mobs.stream().filter(m->m.id()==201).findFirst().get();
        cast(ShieldOfLight.INSTANCE,northSkeleton.pos);
        waitOne();waitOne();
        cast(GuidingLight.INSTANCE,northSkeleton.pos);
        click(northSkeleton.pos);
        log("FLOOR SEVEN NORTH SKELETON EXCHANGE");
        click(northSkeleton.pos);assertFalse(northSkeleton.isAlive());
        log("FLOOR SEVEN SECOND SKELETON DEFEATED");
        for(int c:new int[]{413,412,411,410,409,408,407,440})click(c);
        log("FLOOR SEVEN FOOD GUARD APPROACH");
        Mob foodSkeleton=Dungeon.level.mobs.stream().filter(m->m.id()==199).findFirst().get();
        click(foodSkeleton.pos);log("FLOOR SEVEN FOOD GUARD MELEE");
        click(foodSkeleton.pos);click(foodSkeleton.pos);
        log("FLOOR SEVEN FOOD GUARD CONTINUED MELEE");
        assertFalse(foodSkeleton.isAlive());
        for(int c:new int[]{407,440,473,506,506})click(c);
        Item acquiredFood=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(acquiredFood);
        log("FLOOR SEVEN FOOD ACTUALLY ACQUIRED");
        if(System.getProperty("route.checkpoint7FoodOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint7FoodOut"));
    }

    private void jumpIntoKnownChasm(int cell) throws Exception {
        assertTrue("Jump destination must actually be a pit",Dungeon.level.pit[cell]);
        assertTrue("Must stand adjacent before asking to jump",Dungeon.level.adjacent(Dungeon.hero.pos,cell));
        executeChasmUi=true;chasmWindow=null;
        try{click(cell);}finally{executeChasmUi=false;}
        assertNotNull("Original chasm confirmation UI must open",chasmWindow);
        //A real-time UI wait, not game time or a gameplay-state change. Invoke the original yes-button callback.
        Field elapsed=chasmWindow.getClass().getDeclaredField("elapsed");elapsed.setAccessible(true);elapsed.setFloat(chasmWindow,0.3f);
        Method select=chasmWindow.getClass().getDeclaredMethod("onSelect",int.class);select.setAccessible(true);select.invoke(chasmWindow,0);
        Dungeon.hero.busy();ready();log("ORIGINAL CONFIRMED CHASM TRANSITION COMPLETE");
    }
    private void hourglassAccessSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(538,Dungeon.hero.pos);
        for(int cell:new int[]{504,472,440,408,376,377,378,379,380,348,314,280,279,278,277,276})click(cell);
        assertEquals(276,Dungeon.hero.pos);log("WEAK FLOOR ROOM REACHED FROM RETAINED THIEF CHECKPOINT");
        int preFallShield=Dungeon.hero.shielding();
        jumpIntoKnownChasm(275);assertEquals("Fall must not consume shielding",preFallShield,Dungeon.hero.shielding());assertEquals(8,Dungeon.depth);assertEquals(1,Dungeon.hero.HP);assertEquals(1,Dungeon.hero.HT);
        assertNull("Actual Falling actor must finish before claiming survival",Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.Falling.class));
        waitOne();
        assertNull("Zero bleeding must detach before claiming survival",Dungeon.hero.buff(Bleeding.class));
        log("ACTUAL PIT ROOM LANDING BEFORE ITEM ACCESS");
        for(int cell:new int[]{207,176,177,177,177,177,177})click(cell);
        assertNotNull("Hourglass must be acquired from actual remains",Dungeon.hero.belongings.getItem(TimekeepersHourglass.class));
        assertEquals("Actual gold from this heap",164,Dungeon.gold);
        log("HOURGLASS AND PIT CRYSTAL KEY ACTUALLY ACQUIRED");
        TimekeepersHourglass hourglass=Dungeon.hero.belongings.getItem(TimekeepersHourglass.class);
        hourglass.execute(Dungeon.hero,"EQUIP");Dungeon.hero.busy();ready();assertTrue(hourglass.isEquipped(Dungeon.hero));log("ACTUAL HOURGLASS EQUIPPED IN FREE MISC SLOT");
        for(int n=0;n<Integer.getInteger("route.pitRecharge",0);n++)waitOne();
        if(Integer.getInteger("route.pitRecharge",0)>0)log("PAID NATIVE PIT ROOM RECHARGE BEFORE CRYSTAL EXIT");
        for(int cell:new int[]{178,179,180,212,213,213,214})click(cell);
        assertEquals("Must actually escape the pit room through the owned crystal key",214,Dungeon.hero.pos);
        assertEquals(0,com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(new com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey(8)));
        Field hourglassCharge=Artifact.class.getDeclaredField("charge");hourglassCharge.setAccessible(true);
        Field hourglassPartial=Artifact.class.getDeclaredField("partialCharge");hourglassPartial.setAccessible(true);
        assertEquals("No Hourglass charge granted or spent",5,hourglassCharge.getInt(hourglass));
        System.out.println("HOURGLASS_DETAIL charge="+hourglassCharge.getInt(hourglass)+" partial="+hourglassPartial.getFloat(hourglass)+" cursed="+hourglass.cursed+" equipped="+hourglass.isEquipped(Dungeon.hero));
        log("ACTUAL PIT ROOM EXIT UNLOCKED AND ESCAPED WITH HOURGLASS");
        if(System.getProperty("route.hourglassOut")!=null)writeCheckpoint(System.getProperty("route.hourglassOut"));
    }
    private void seventhFloorSection() throws Exception {
        if(Boolean.getBoolean("route.hourglassAccess")){hourglassAccessSection();return;}
        if(Dungeon.hero.pos==538){seventhFloorAfterIntercept();return;}
        if(Dungeon.hero.pos==506){seventhFloorFood();return;}
        assertEquals(818,Dungeon.hero.pos);
        if(Boolean.getBoolean("route.sevenContinue")){seventhFloorAfterRest();return;}
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);
        assertEquals(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class,food.getClass());
        food.execute(Dungeon.hero,"EAT");ready();log("EAT RECOVERED FULL RATION AT FLOOR SEVEN ENTRANCE");
        for(int n=0;n<Integer.getInteger("route.sevenWait",200);n++)waitOne();
        log("FLOOR SEVEN PAID ENTRANCE RECHARGE");
        if(System.getProperty("route.checkpoint7RestOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint7RestOut"));
    }

    private void sixthFloorSection() throws Exception {
        if(Dungeon.hero.pos==334){sixthFloorNorth();return;}
        if(Dungeon.hero.pos==850){sixthFloorAfterUpgrade();return;}
        if(Dungeon.hero.pos==904){sixthFloorAfterStrength();return;}
        if(Dungeon.hero.pos==620){sixthFloorAfterFood();return;}
        for(int c:new int[]{644,613,582,583,584,585,586,587,588,589})click(c);
        buyActualShopHeap(620);
        Item ration=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation.class);assertNotNull(ration);
        ration.execute(Dungeon.hero,"EAT");ready();log("EAT PURCHASED FLOOR SIX RATION");
        if(System.getProperty("route.checkpoint6FoodOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint6FoodOut"));
    }

    private void sixthFloorNorth() throws Exception {
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==164).findFirst().get();
        click(333);log("FLOOR SIX NORTHWEST SIGHTLINE");
        cast(GuidingLight.INSTANCE,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        log("FLOOR SIX SPAWNED SKELETON EXCHANGE");
        assertFalse(skeleton.isAlive());
        for(int c:new int[]{365,366,367,367,366,334,333,301,269,237,205,173})click(c);
        log("FLOOR SIX UPPER ROOM APPROACH");
        cast(HolyWard.INSTANCE,null);
        for(int c:new int[]{141,109,77,78,79})click(c);
        Mob thief=Dungeon.level.mobs.stream().filter(m->m.id()==151).findFirst().get();
        cast(GuidingLight.INSTANCE,thief.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,thief.pos);
        log("FLOOR SIX THIEF OPENING");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,thief.pos);
        assertFalse(thief.isAlive());assertEquals(6,Dungeon.hero.lvl);
        Dungeon.hero.upgradeTalent(Talent.SEARING_LIGHT);log("EARN LEVEL SIX AND SECOND SEARING LIGHT POINT");
        for(int c:new int[]{80,81,82,82,82,82,114,114})click(c);
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class));
        assertEquals(2,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class).quantity());
        log("RECOVER STOLEN FOOD MISSILES AND UPPER HEALING POTION");
        for(int c:new int[]{81,81,81})click(c);
        log("FLOOR SIX RESPAWNED SWARM FIRST EXCHANGE");
        for(int c:new int[]{82,81,81})click(c);
        log("FLOOR SIX RESPAWNED SWARM SECOND EXCHANGE");
        for(int c:new int[]{82,81,80,80})click(c);
        log("FLOOR SIX RESPAWNED SWARM CLEANUP");
        click(111);log("FLOOR SIX LAST SWARM CHILD");
        assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm));
        for(int c:new int[]{81,82,83,84,85,117,149,181,213,245,244,243,242,274,275,276,277,278,279,280,281,313})click(c);
        log("FLOOR SIX FINAL EXIT APPROACH");
        Mob exitSkeleton=Dungeon.level.mobs.stream().filter(m->m.id()==186).findFirst().get();
        cast(ShieldOfLight.INSTANCE,exitSkeleton.pos);
        click(312);click(312);assertEquals(7,Dungeon.depth);log("EARNED FLOOR SEVEN ENTRY");
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        if(System.getProperty("route.checkpoint7Out")!=null)writeCheckpoint(System.getProperty("route.checkpoint7Out"));
    }

    private void sixthFloorAfterUpgrade() throws Exception {
        for(int c:new int[]{818,819,820,788,756,724,692,660,628,596,564,565,566,567,568,569})click(c);
        Dungeon.hero.search(true);ready();log("SEARCH FLOOR SIX EASTERN SECRET DOOR");
        for(int c:new int[]{537,505,473,474,475})click(c);
        Dungeon.hero.search(true);ready();log("SEARCH FLOOR SIX NORTHERN SECRET DOOR");
        for(int c:new int[]{443,411,379,347,315,314,313})click(c);
        assertEquals(313,Dungeon.hero.pos);
        log("FLOOR SIX NORTHERN EXIT ROOM APPROACH");
        cast(HolyWard.INSTANCE,null);
        for(int c:new int[]{345,344,343,342,341})click(c);
        log("FLOOR SIX SWARM MELEE APPROACH");
        for(int c:new int[]{372,371,371})click(c);
        log("FLOOR SIX SWARM FIRST MELEE EXCHANGE");
        for(int c:new int[]{371,371,371})click(c);
        log("FLOOR SIX SWARM SECOND MELEE EXCHANGE");
        for(int c:new int[]{339,339})click(c);
        log("FLOOR SIX SWARM CHILD EXCHANGE");
        for(int c:new int[]{339,340,340})click(c);
        log("FLOOR SIX SWARM CLEANUP");
        assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m.id()==150||m.id()==173||m.id()==177));
        for(int c:new int[]{371,370,369,368,367,366,334,334})click(c);
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);
        assertTrue(food instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation);
        food.execute(Dungeon.hero,"EAT");ready();log("EAT CARRIED SMALL RATION AFTER ACQUIRING NORTHERN FOOD");
        if(System.getProperty("route.checkpoint6NorthOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint6NorthOut"));
    }

    private void sixthFloorAfterStrength() throws Exception {
        for(int c:new int[]{873,842,810,779,780,812,813,814})click(c);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,817);
        log("OPEN SKELETON CORRIDOR DOOR FROM RANGE THREE");
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==149).findFirst().get();
        cast(GuidingLight.INSTANCE,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        log("SECOND SKELETON RANGED EXCHANGE");
        cast(GuidingLight.INSTANCE,skeleton.pos);assertFalse(skeleton.isAlive());
        for(int c:new int[]{815,816,816,817,817,818,850,850})click(c);
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
        upgradeOwnedGear(Dungeon.hero.belongings.armor());
        assertEquals(3,Dungeon.hero.belongings.armor().level());
        if(System.getProperty("route.checkpoint6UpgradeOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint6UpgradeOut"));
    }

    private void sixthFloorAfterFood() throws Exception {
        for(int c:new int[]{587,555,523,491})click(c);
        buyActualShopHeap(459);
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation.class));
        for(int c:new int[]{491,523,555,587,586,585,584,615,646,678,710,711,712,713,745,777,778})click(c);
        Dungeon.hero.search(true);ready();log("REVEAL FLOOR SIX CORRIDOR OOZE TRAP");
        click(777);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,779);
        for(int c:new int[]{778,779,779,780})click(c);
        log("FLOOR SIX SOUTHERN APPROACH WITH SECOND PAID RATION");
        cast(HolyWard.INSTANCE,null);
        for(int c:new int[]{779,810,842})click(c);
        log("FLOOR SIX STRENGTH ROOM APPROACH");
        Mob skeleton=Dungeon.level.mobs.stream().filter(m->m.id()==148).findFirst().get();
        cast(GuidingLight.INSTANCE,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        log("FLOOR SIX FIRST SKELETON RANGED EXCHANGE");
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,skeleton.pos);
        assertFalse("Skeleton must die before adjacent explosion",skeleton.isAlive());
        log("FLOOR SIX FIRST SKELETON DEFEATED");
        for(int c:new int[]{841,840,840})click(c);
        Item strength=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class);assertNotNull(strength);
        strength.execute(Dungeon.hero,"DRINK");ready();log("DRINK THIRD EARNED STRENGTH POTION");assertEquals(13,Dungeon.hero.STR);
        for(int c:new int[]{871,903,903,904,904})click(c);
        if(System.getProperty("route.checkpoint6SkeletonOut")!=null)writeCheckpoint(System.getProperty("route.checkpoint6SkeletonOut"));
    }

    private void postGooSection() throws Exception {
        assertEquals(5,Dungeon.depth);assertFalse(Dungeon.level.locked);
        for(int c:new int[]{743,743,743,709,709,674,673,672,637,637})click(c);
        assertEquals(3,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class).quantity());
        for(int n=0;n<Integer.getInteger("route.postGooWait",100);n++)waitOne();
        for(int c:new int[]{603,569,535,501,500,499,465,431,430,429,428,427,426,425,424,423,422,421,420,386,352,351,350,349,383,384,418,417,416,415,381,381})click(c);
        assertEquals(6,Dungeon.depth);
        log("POST GOO PROGRESSION");
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        if(System.getProperty("route.checkpoint6Out")!=null)writeCheckpoint(System.getProperty("route.checkpoint6Out"));
    }

    private void armoredGooSection() throws Exception {
        assertEquals(5,Dungeon.depth);assertEquals(708,Dungeon.hero.pos);assertEquals(2,Dungeon.hero.belongings.armor().level());
        for(int n=0;n<200;n++)waitOne();
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio.class);assertNotNull(food);
        food.execute(Dungeon.hero,"EAT");ready();log("EAT REMAINING EARNED FROZEN MEAT BEFORE GOO");
        for(int c:new int[]{673,672,637})click(c);
        Dungeon.hero.search(true);ready();log("SEARCH GOO APPROACH SECRET DOOR");assertEquals(Terrain.DOOR,Dungeon.level.map[603]);
        for(int c:new int[]{603,569,535,501,500,499,465,431,430})click(c);
        Mob goo=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo).findFirst().get();
        assertEquals(100,goo.HP);log("GOO ARMORED LEGITIMATE FIRST ENCOUNTER");
        cast(HolyWard.INSTANCE,null);assertEquals(3,Dungeon.hero.shielding());
        if(Boolean.getBoolean("route.gooHolyWeapon"))cast(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE,null);
        cast(GuidingLight.INSTANCE,goo.pos);
        throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,goo.pos);
        log("GOO ARMORED OPENING BURST");
        // Manually authored continuation; never chooses actions from game state.
        String verifiedActions="C431,C465,C499,C500,C501,C535,A,C569,C603,C637,A,C672,T,T,L,A,C707,C742,W,A,C777,C778,W,A,A,A,A,A";
        for(String action:System.getProperty("route.gooActions", Boolean.getBoolean("route.assertGooDefeated")?verifiedActions:"").split(",")){
            if(action.isEmpty())continue;
            if(action.equals("W"))waitOne();
            else if(action.equals("S"))cast(ShieldOfLight.INSTANCE,goo.pos);
            else if(action.equals("L"))cast(GuidingLight.INSTANCE,goo.pos);
            else if(action.equals("T"))throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken.class,goo.pos);
            else if(action.equals("A"))click(goo.pos);
            else if(action.startsWith("C"))click(Integer.parseInt(action.substring(1)));
            else fail("Unknown manually authored action "+action);
        }
        if(Boolean.getBoolean("route.assertGooDefeated")){
            assertFalse(goo.isAlive());assertFalse(Dungeon.level.locked);assertEquals(1,Dungeon.hero.HP);
            log("GOO DEFEATED WITH CONTINUOUS EARNED RESOURCES");
            if(System.getProperty("route.checkpointGooOut")!=null)writeCheckpoint(System.getProperty("route.checkpointGooOut"));
        }
    }


    private void freshGooContinuousSection() throws Exception {
        assertEquals(4,Dungeon.depth);assertEquals(609,Dungeon.hero.pos);
        for(int c:new int[]{608,607,606,606,605,604,603,563,523,483,482,481,480,479,478,438,437,475,513}){click(c);if(Dungeon.hero.pos!=c && Dungeon.depth==4)click(c);}
        if(Dungeon.depth==4)click(513);assertEquals(5,Dungeon.depth);assertEquals(708,Dungeon.hero.pos);
        boolean drank=false;
        for(int n=0;n<300 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<6;n++){
            assertFalse(Dungeon.level.mobs.stream().anyMatch(m->Dungeon.level.heroFOV[m.pos] && m.alignment==Char.Alignment.ENEMY));
            if(!drank && Dungeon.hero.buff(Hunger.class).hunger()>=400){
                Item cleansing=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing.class);assertNotNull(cleansing);cleansing.execute(Dungeon.hero,"DRINK");ready();log("DRINK ACTUALLY ACQUIRED CLEANSING BEFORE STARVATION");drank=true;
            }else waitOne();
        }
        assertTrue(drank);assertEquals(6,charge(Dungeon.hero.belongings.getItem(HolyTome.class)));log("FRESH CONTINUOUS GOO PREPARATION");
        for(int c:new int[]{673,672,637})click(c);
        Dungeon.hero.search(true);ready();assertEquals(Terrain.DOOR,Dungeon.level.map[603]);
        for(int c:new int[]{603,569,535,501,500,499,465,431,430})click(c);
        Mob goo=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo).findFirst().get();
        cast(HolyWard.INSTANCE,null);cast(GuidingLight.INSTANCE,goo.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,goo.pos);
        for(int c:new int[]{431,465,499,500,501})click(c);
        freshGooActions(goo);
        log("FRESH CONTINUOUS GOO FIXED ACTION FRONTIER");
        if(Boolean.getBoolean("route.freshGooAssertVictory"))assertFalse(goo.isAlive());
        if(Boolean.getBoolean("route.freshAfterGoo"))freshAfterGooSection();
    }
    private void freshGooActions(Mob goo) throws Exception { freshFixedActions(goo,System.getProperty("route.freshGooActions","")); }
    private void freshFixedActions(Mob goo,String commands) throws Exception {
        for(String a:commands.split(",")){
            if(a.isEmpty())continue;
            if(a.equals("S"))cast(ShieldOfLight.INSTANCE,goo.pos);
            else if(a.equals("L"))cast(GuidingLight.INSTANCE,goo.pos);
            else if(a.equals("W"))cast(HolyWard.INSTANCE,null);
            else if(a.equals("H"))waitOne();
            else if(a.equals("T"))throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,goo.pos);
            else if(a.equals("A")){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,goo.pos));click(goo.pos);}
            else if(a.startsWith("K")){int c=Integer.parseInt(a.substring(1));Char enemy=Actor.findChar(c);assertTrue(enemy instanceof Mob);assertEquals(Char.Alignment.ENEMY,enemy.alignment);assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,c));click(c);}
            else if(a.startsWith("C")){int c=Integer.parseInt(a.substring(1));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);}
            else fail("Unknown fixed Goo command "+a);
            assertEquals(1,Dungeon.hero.HP);
        }
    }

    private void freshAfterGooSection() throws Exception {
        assertEquals(5,Dungeon.depth);assertFalse(Dungeon.level.locked);assertFalse(Dungeon.level.mobs.stream().anyMatch(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo));assertEquals(707,Dungeon.hero.pos);
        for(int c:new int[]{706,706,673,673,672,672,672}){click(c);assertEquals(c,Dungeon.hero.pos);}log("FRESH GOO ACTUAL BLOB KEY AND SPEAR RECOVERY");
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));
        for(int n=0;n<300 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<6;n++){assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<440);waitOne();}assertEquals(6,charge(Dungeon.hero.belongings.getItem(HolyTome.class)));log("FRESH POST GOO SIX CHARGES PAID IN HUNGER");
        for(int c:new int[]{637,603,569,535,501,500,499,465,431,430,429,428,427,426,425,424,423,422,421,420,386,352,351,350,349,383,384,418,417,416,415,381,381})click(c);
        assertEquals(6,Dungeon.depth);log("FRESH UNJOINED PRISON ENTRY");System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        if(Boolean.getBoolean("route.freshAfterGooShopFood")){
            for(int c:new int[]{644,613,582,583,584,585,586,587,588,589})click(c);
            buyActualShopHeap(620);Item ration=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation.class);assertNotNull(ration);ration.execute(Dungeon.hero,"EAT");ready();log("FRESH EAT ACTUALLY PURCHASED PRISON RATION");if(Boolean.getBoolean("route.freshPrisonSix"))freshPrisonSixSection();
        }
    }

    private void freshPrisonSixSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(620,Dungeon.hero.pos);
        Mob firstSkeleton=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton && m.pos==902).findFirst().get();
        for(int c:new int[]{587,555,523,491})click(c);buyActualShopHeap(459);
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation.class));
        for(int c:new int[]{491,523,555,587,586,585,584,615,646,678,710,711,712,713,745,777,778})click(c);
        Dungeon.hero.search(true);ready();log("FRESH PRISON REVEAL REAL OOZE TRAP");click(777);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,779);
        for(int c:new int[]{778,779,779,780})click(c);cast(HolyWard.INSTANCE,null);for(int c:new int[]{779,810,842})click(c);
        assertTrue(Dungeon.level.heroFOV[firstSkeleton.pos]);log("FRESH FIRST PRISON SKELETON ACTUAL APPROACH");
        cast(GuidingLight.INSTANCE,firstSkeleton.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,firstSkeleton.pos);log("FRESH FIRST PRISON SKELETON GUIDE AND ONE OWNED SPEAR");
        if(Boolean.getBoolean("route.freshPrisonSixFinishSkeleton")){
            assertTrue(firstSkeleton.isAlive());cast(GuidingLight.INSTANCE,firstSkeleton.pos);assertTrue(firstSkeleton.isAlive());cast(GuidingLight.INSTANCE,firstSkeleton.pos);assertFalse(firstSkeleton.isAlive());log("FRESH FIRST PRISON SKELETON DEFEATED BY GUIDE; ACTUAL EXPLOSION PROCESSED");if(Boolean.getBoolean("route.freshPrisonSixAfterFirst"))freshPrisonSixAfterFirst();
        }
    }

    private void freshPrisonSixAfterFirst() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(842,Dungeon.hero.pos);
        for(int c:new int[]{841,840,840}){click(c);assertEquals(c,Dungeon.hero.pos);}
        Item strength=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class);assertNotNull(strength);strength.execute(Dungeon.hero,"DRINK");ready();assertEquals(13,Dungeon.hero.STR());log("FRESH THIRD STRENGTH POTION ACTUALLY ACQUIRED AND DRUNK");
        for(int c:new int[]{841,873,873}){click(c);assertEquals(c,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));log("FRESH SPEAR RECOVERED FROM FIRST SKELETON");
        for(int n=0;n<250 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<6;n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;
            assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<440);
            if(Dungeon.hero.buff(Hunger.class).hunger()>=390){Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation.class);assertNotNull(food);food.execute(Dungeon.hero,"EAT");ready();log("FRESH OWNED SECOND SHOP RATION EATEN DURING PAID RECHARGE");}else waitOne();
        }
        log("FRESH FIRST SKELETON ROOM ACTUAL PAID RECHARGE FRONTIER");
        if(Boolean.getBoolean("route.freshPrisonRespawn")){
            Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==207).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);assertTrue(Dungeon.level.heroFOV[target.pos]);freshFixedActions(target,"S,W,L,T,A");assertFalse(target.isAlive());log("FRESH REAL RECHARGE INTERRUPTER SKELETON DEFEATED WITHOUT REFILL");if(Boolean.getBoolean("route.freshPrisonNextRecharge"))freshPrisonNextRecharge();
        }

    }

    private void freshPrisonNextRecharge() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(873,Dungeon.hero.pos);
        for(int c:new int[]{842,842}){click(c);assertEquals(c,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation.class);assertNotNull(food);food.execute(Dungeon.hero,"EAT");ready();log("FRESH SECOND OWNED SHOP RATION EATEN BEFORE NEXT ENCOUNTER");
        for(int n=0;n<150 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;n++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}
        log("FRESH NEXT REAL PAID REST BOUNDARY");if(Boolean.getBoolean("route.freshDirectUpgrade"))freshDirectUpgradeSection();if(Boolean.getBoolean("route.freshPrisonSwarm"))freshPrisonSwarmSection();
    }

    private void freshPrisonSwarmSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(842,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm && m.pos==781).findFirst().get();cast(HolyWard.INSTANCE,null);assertEquals(3,Dungeon.hero.shielding());click(810);assertEquals(810,Dungeon.hero.pos);log("FRESH PRISON NEXT DOOR WITH OWNED MEAL SPELL BARRIER");
        assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm);click(779);assertEquals(779,Dungeon.hero.pos);assertTrue(Dungeon.level.heroFOV[target.pos]);cast(ShieldOfLight.INSTANCE,target.pos);log("FRESH REAL RESPAWN SWARM INTERCEPTION");
        freshFixedActions(target,System.getProperty("route.freshPrisonSwarmActions",""));log("FRESH REAL RESPAWN SWARM FIXED ACTION FRONTIER");if(Boolean.getBoolean("route.freshSecondPrisonSkeleton"))freshSecondPrisonSkeleton();
    }

    private void freshSecondPrisonSkeleton() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(812,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton && m.pos==818).findFirst().get();
        freshFixedActions(target,"K781,C813,C814");throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,817);log("FRESH SECOND INITIAL SKELETON REAL DOOR OPENING");
        assertTrue(Dungeon.level.heroFOV[target.pos]);cast(ShieldOfLight.INSTANCE,target.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);log("FRESH SECOND INITIAL SKELETON REAL SPEAR");
        if(Boolean.getBoolean("route.freshSecondSkeletonMelee")){
            waitOne();waitOne();assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);log("FRESH SECOND INITIAL SKELETON FIRST MELEE");if(Boolean.getBoolean("route.freshSecondSkeletonFinish")){click(target.pos);assertFalse(target.isAlive());assertEquals(7,Dungeon.hero.lvl);log("FRESH LEVEL SEVEN ACTUALLY EARNED");}
        }
    }

    private void freshDirectUpgradeSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(842,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton && m.pos==818).findFirst().get();
        cast(HolyWard.INSTANCE,null);assertEquals(3,Dungeon.hero.shielding());for(int c:new int[]{810,779,780,812,813,814}){click(c);assertEquals(c,Dungeon.hero.pos);}throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,817);
        assertTrue(Dungeon.level.heroFOV[target.pos]);cast(ShieldOfLight.INSTANCE,target.pos);cast(GuidingLight.INSTANCE,target.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);log("FRESH UNINTERRUPTED REAL UPGRADE GUARD OPENING");
        if(Boolean.getBoolean("route.freshDirectUpgradeMelee")){
            waitOne();for(int n=0;n<2 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());log("FRESH UNINTERRUPTED REAL UPGRADE GUARD DEFEATED");if(Boolean.getBoolean("route.freshDirectMail"))freshDirectMailSection();
        }
    }

    private void freshDirectMailSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(814,Dungeon.hero.pos);for(int c:new int[]{815,815,816,817,817,818,850,850}){click(c);assertEquals(c,Dungeon.hero.pos);}
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class));
        com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor mail=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor.class);assertNotNull(mail);assertEquals(0,mail.level());assertEquals(14,mail.STRReq());upgradeOwnedGear(mail);assertEquals(1,mail.level());assertEquals(13,mail.STRReq());assertEquals(1,mail.DRMin());assertEquals(9,mail.DRMax());assertEquals(13,Dungeon.hero.STR());log("FRESH ACTUAL GUARDED SCROLL UPGRADES CARRIED MAIL TO USABLE STRENGTH");
        mail.execute(Dungeon.hero,"EQUIP");Dungeon.hero.busy();ready();assertSame(mail,Dungeon.hero.belongings.armor());log("FRESH MAIL ONE ACTUALLY EQUIPPED AT EARNED THIRTEEN STRENGTH");
        for(int n=0;n<140 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;n++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();}
        log("FRESH MAIL ROOM ACTUAL PAID RECHARGE FRONTIER");if(Boolean.getBoolean("route.freshMailNorth"))freshMailNorthSection();
    }

    private void freshMailNorthSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(850,Dungeon.hero.pos);cast(HolyWard.INSTANCE,null);
        for(int c:new int[]{819,788,757,725,693,662,631,599,568}){assertNull("Actual northward path occupied at "+c,Actor.findChar(c));click(c);assertEquals(c,Dungeon.hero.pos);}
        if(Dungeon.level.map[537]==Terrain.SECRET_DOOR){Dungeon.hero.search(true);ready();}assertTrue(Dungeon.level.map[537]==Terrain.DOOR || Dungeon.level.map[537]==Terrain.OPEN_DOOR);log("FRESH MAIL NORTH ACTUAL REVEALED DOOR");
        for(int c:new int[]{537,505,474}){assertNull("Actual northward path occupied at "+c,Actor.findChar(c));click(c);assertEquals(c,Dungeon.hero.pos);}
        assertTrue(Dungeon.level.map[443]!=Terrain.SECRET_DOOR);
        for(int c:new int[]{443,411,379,347,314,313}){assertNull("Actual northward path occupied at "+c,Actor.findChar(c));click(c);assertEquals(c,Dungeon.hero.pos);}
        log("FRESH MAIL NORTH ACTUAL FOOD AND EXIT ROOM FRONTIER");if(Boolean.getBoolean("route.freshMailFoodFight"))freshMailFoodFightSection();
    }

    private void freshMailFoodFightSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(313,Dungeon.hero.pos);assertEquals(6,Dungeon.hero.lvl);assertEquals(32,Dungeon.hero.exp);assertEquals(0,Dungeon.hero.pointsInTalent(Talent.BLESS));
        Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==196).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm);assertEquals(50,target.HP);
        for(int c:new int[]{345,344}){assertNull(Actor.findChar(c));click(c);assertEquals(c,Dungeon.hero.pos);}assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));cast(ShieldOfLight.INSTANCE,target.pos);
        for(int n=0;n<4 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());assertEquals(7,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));Dungeon.hero.upgradeTalent(Talent.BLESS);log("FRESH ACTUAL PARENT SWARM KILL EARNS FIRST BLESS POINT");
        cast(BlessSpell.INSTANCE,Dungeon.hero.pos);assertTrue(Dungeon.hero.shielding()>0);log("FRESH ACTUAL FIRST EARNED BLESS CAST WITH CONTINUOUS RESOURCES");if(System.getProperty("route.freshFoodClearActions")!=null)freshFixedActions(target,System.getProperty("route.freshFoodClearActions"));if(Boolean.getBoolean("route.freshMailFoodPickup"))freshMailFoodPickupSection();
    }

    private void freshMailFoodPickupSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(344,Dungeon.hero.pos);Char child=Actor.findChar(375);assertTrue(child instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm);assertEquals(2,child.HP);click(375);assertFalse(child.isAlive());log("FRESH ALL FOOD GUARD SWARM CHILDREN ACTUALLY DEFEATED");
        for(int c:new int[]{343,342,341,340,339,370,369,368,367,334}){assertNull(Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);}click(334);
        Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<450);log("FRESH NORTHERN FOOD ACTUALLY ACQUIRED BEFORE STARVATION");food.execute(Dungeon.hero,"EAT");ready();log("FRESH NORTHERN FOOD ACTUALLY EATEN WITH ORIGINAL STARVATION RULE");
        cast(HolyWard.INSTANCE,null);log("FRESH POST FOOD EARNED WARD AND REAL SHIELD RESOURCE FRONTIER");if(Boolean.getBoolean("route.freshPostFoodToSeven"))freshPostFoodToSevenSection();
    }

    private void freshPostFoodToSevenSection() throws Exception {
        assertEquals(6,Dungeon.depth);assertEquals(334,Dungeon.hero.pos);
        for(int n=0;n<140 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<Integer.getInteger("route.freshPostFoodCampTarget",2);n++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<440);waitOne();}
        log("FRESH POST FOOD ACTUAL DEPARTURE CAMP BOUNDARY");assertTrue("Actual enemy interruption prevents planned departure charge",charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=Integer.getInteger("route.freshPostFoodCampTarget",2));
        if(Integer.getInteger("route.freshPostFoodCampTarget",2)>1)cast(HolyWard.INSTANCE,null);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FRESH PAID DEPARTURE WARD AND BLESS WITH OWNED CHARGES");
        for(int c:new int[]{367,368,369,370,339,340,341,342,343,312}){assertNull("Actual departure path occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);if(Dungeon.depth==7)break;assertEquals(c,Dungeon.hero.pos);}if(Dungeon.depth==6)click(312);assertEquals(7,Dungeon.depth);log("FRESH ACTUAL LEVEL ONE THROUGH MAIL BLESS FOOD AND FLOOR SEVEN ENTRY");
        System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
        if(Boolean.getBoolean("route.freshSevenCamp"))freshSevenCampSection();
    }

    private void freshSevenCampSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(818,Dungeon.hero.pos);
        for(int n=0;n<220 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<Integer.getInteger("route.freshSevenCampTarget",4);n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;
            assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<440);waitOne();
        }
        log("FRESH DARK FLOOR SEVEN ACTUAL PAID RECHARGE OR ENEMY INTERRUPTION");if(Boolean.getBoolean("route.freshSevenFoodApproach"))freshSevenFoodApproachSection();
    }

    private void freshSevenFoodApproachSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(818,Dungeon.hero.pos);assertEquals(Integer.getInteger("route.freshSevenCampTarget",4).intValue(),charge(Dungeon.hero.belongings.getItem(HolyTome.class)));cast(HolyWard.INSTANCE,null);if(!Boolean.getBoolean("route.freshSevenLateBless"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
        for(int c:new int[]{785,753,720,686,652,619,586,553,520,487,455,421,420,419}){assertNull("Actual dark-food path occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);}log("FRESH DARK SEVEN REAL SECRET FOOD CORRIDOR APPROACH");
        if(Dungeon.level.map[418]==Terrain.SECRET_DOOR){Dungeon.hero.search(true);ready();}assertTrue(Dungeon.level.map[418]==Terrain.DOOR || Dungeon.level.map[418]==Terrain.OPEN_DOOR);
        for(int c:new int[]{418,417,416}){assertNull("Actual dark-food corridor occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);}log("FRESH DARK SEVEN ACTUAL FIRST FOOD CORRIDOR CONTACT");if(Boolean.getBoolean("route.freshSevenFirstSkeleton"))freshSevenFirstSkeletonSection();
    }

    private void freshSevenFirstSkeletonSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(416,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==239).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);assertEquals(25,target.HP);assertTrue(Dungeon.level.heroFOV[target.pos]);
        if(Boolean.getBoolean("route.freshSevenLateBless")){cast(BlessSpell.INSTANCE,Dungeon.hero.pos);cast(ShieldOfLight.INSTANCE,target.pos);}
        cast(GuidingLight.INSTANCE,target.pos);assertEquals(target.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,target.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);log("FRESH DARK SEVEN FIRST SKELETON OWNED GUIDE AND SPEAR RESULT");if(Boolean.getBoolean("route.freshSevenFirstFinish")){assertTrue(target.isAlive());if(!Boolean.getBoolean("route.freshSevenLateBless")){cast(ShieldOfLight.INSTANCE,target.pos);waitOne();}assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);log("FRESH DARK SEVEN FIRST SKELETON SHIELDED MELEE RESULT");if(Boolean.getBoolean("route.freshSevenFirstSecondMelee")){if(target.isAlive())click(target.pos);assertFalse(target.isAlive());log("FRESH DARK SEVEN FIRST SKELETON ACTUALLY DEFEATED");if(Boolean.getBoolean("route.freshSevenSecondSkeleton"))freshSevenSecondSkeletonSection();}}
    }

    private void freshSevenSecondSkeletonSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(416,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==238).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);assertEquals(25,target.HP);
        for(int c:new int[]{449,449}){click(c);assertEquals(c,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));log("FRESH DARK SEVEN SPEAR ACTUALLY RECOVERED WITH APPROACHING SECOND SKELETON");
        assertTrue(Dungeon.level.heroFOV[target.pos]);cast(ShieldOfLight.INSTANCE,target.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);for(int n=0;n<2 && !Dungeon.level.adjacent(Dungeon.hero.pos,target.pos);n++)waitOne();log("FRESH DARK SEVEN SECOND SKELETON REAL SHIELDED SPEAR RESULT");
        for(int n=0;n<3 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());log("FRESH DARK SEVEN SECOND SKELETON ACTUALLY DEFEATED");if(Boolean.getBoolean("route.freshSevenFoodGuard"))freshSevenFoodGuardSection();
    }

    private void freshSevenFoodGuardSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(449,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==236).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);assertEquals(25,target.HP);
        for(int n=0;n<30 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<1;n++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}log("FRESH DARK SEVEN REAL THIRD GUARD PREPARATION BOUNDARY");assertTrue(charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1);
        for(int c:new int[]{415,415,414,413,412,411,410,409,408,440}){if(c!=Dungeon.hero.pos)assertNull("Actual third-guard path occupied at "+c,Actor.findChar(c));click(c);assertEquals(c,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));assertTrue(Dungeon.level.heroFOV[target.pos]);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);for(int n=0;n<2 && !Dungeon.level.adjacent(Dungeon.hero.pos,target.pos);n++)waitOne();log("FRESH DARK SEVEN THIRD FOOD GUARD REAL BLESS AND SPEAR");
        if(Boolean.getBoolean("route.freshSevenFoodGuardMelee")){
            for(int n=0;n<3 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());log("FRESH DARK SEVEN THIRD FOOD GUARD ACTUALLY DEFEATED");
            if(Boolean.getBoolean("route.freshSevenFoodPickup")){
                for(int c:new int[]{473,506}){assertNull("Actual food pickup path occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);}click(506);Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class);assertNotNull(food);assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<450);food.execute(Dungeon.hero,"EAT");ready();log("FRESH DARK SEVEN GUARDED FOOD ACTUALLY ACQUIRED AND EATEN BEFORE STARVATION");if(Boolean.getBoolean("route.freshSevenPostFoodCamp"))freshSevenPostFoodCampSection();
            }
        }
    }

    private void freshSevenPostFoodCampSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(506,Dungeon.hero.pos);
        for(int n=0;n<130 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;
            assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<440);waitOne();
        }
        log("FRESH DARK SEVEN POST MEAL ACTUAL RECHARGE OR NEW ENEMY BOUNDARY");
        if(Boolean.getBoolean("route.freshSevenThief")){
            Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==254).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief);assertTrue(Dungeon.level.heroFOV[target.pos]);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);log("FRESH DARK SEVEN POST MEAL THIEF OWNED BLESS AND SPEAR");
            if(Boolean.getBoolean("route.freshSevenThiefMelee")){
                for(int n=0;n<3 && !Dungeon.level.adjacent(Dungeon.hero.pos,target.pos);n++)waitOne();
                for(int n=0;n<4 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());log("FRESH DARK SEVEN POST MEAL THIEF ACTUALLY DEFEATED");if(Boolean.getBoolean("route.freshSevenScrollApproach"))freshSevenScrollApproachSection();if(Boolean.getBoolean("route.freshSevenSouthApproach"))freshSevenSouthApproachSection();
            }
        }
    }
    private void freshSevenScrollApproachSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(506,Dungeon.hero.pos);
        for(int c:new int[]{538,538,506,473,440,408,409,410,411,412,413,414,415,416,417,418,419,420,421,455,487,520,553,586,619,652,686,720,753,786,819,820,854,888,921,954,986,985,984,983}){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;
            if(c!=Dungeon.hero.pos)assertNull("Actual scroll approach occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);
        }
        assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));log("FRESH DARK SEVEN REAL SCROLL APPROACH OR ENEMY INTERRUPTION");
        if(Boolean.getBoolean("route.freshSevenOpenScrollDoor")){
            assertEquals(983,Dungeon.hero.pos);assertTrue(Dungeon.level.map[981]==Terrain.DOOR || Dungeon.level.map[981]==Terrain.OPEN_DOOR);
            if(Boolean.getBoolean("route.freshSevenBeforeDoorCamp")){
                for(int n=0;n<150 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<4;n++){
                    if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
                }
                log("FRESH DARK SEVEN ACTUAL RECHARGE BEFORE OPENING SCROLL DOOR");if(Boolean.getBoolean("route.freshSevenClosedSkeleton"))freshSevenClosedSkeletonSection();if(Boolean.getBoolean("route.freshSevenBeforeDoorCampOnly"))return;
            }
            throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class,981);log("FRESH DARK SEVEN ACTUAL SCROLL DOOR OPENED AT RANGE");
            if(Boolean.getBoolean("route.freshSevenDoorCamp")){
                for(int n=0;n<110 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;n++){
                    if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<440);waitOne();
                }
                log("FRESH DARK SEVEN OPEN SCROLL DOOR ACTUAL RECHARGE BOUNDARY");
                if(Boolean.getBoolean("route.freshSevenSecondThief")){
                    Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==241).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief);assertTrue(Dungeon.level.heroFOV[target.pos]);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);log("FRESH DARK SEVEN SECOND THIEF REAL BLESS AND SPEAR");
                    for(int n=0;n<4 && !Dungeon.level.adjacent(Dungeon.hero.pos,target.pos);n++)waitOne();
                    for(int n=0;n<4 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());log("FRESH DARK SEVEN SECOND THIEF ACTUALLY DEFEATED");if(Boolean.getBoolean("route.freshSevenGuardCamp"))freshSevenGuardCampSection();
                }

            }

        }

    }

    private void freshSevenGuardCampSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(983,Dungeon.hero.pos);
        if(Boolean.getBoolean("route.freshSevenCampBeforeRecovery")){
            for(int n=0;n<160 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<4;n++){
                if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
            }
            log("FRESH DARK SEVEN ACTUAL CAMP BEFORE SPEAR RECOVERY EXPOSURE");
        }
        for(int c:new int[]{982,982}){click(c);assertEquals(c,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));
        for(int n=0;n<160 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<4;n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
        }
        log("FRESH DARK SEVEN OWNED SPEAR RECOVERY AND ACTUAL GUARD PREPARATION BOUNDARY");
        if(Boolean.getBoolean("route.freshSevenGuardLure")){
            Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==240).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard);assertTrue(Dungeon.level.heroFOV[target.pos]);assertEquals(target.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,target.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);log("FRESH DARK SEVEN ACTUAL GUARDED SCROLL LURE SPEAR");freshFixedActions(target,System.getProperty("route.freshSevenGuardLureActions","C983,C984,C985"));log("FRESH DARK SEVEN ACTUAL GUARDED SCROLL LURE BOUNDARY");
        }

    }

    private void freshSevenClosedSkeletonSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(983,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==261).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);assertTrue(Dungeon.level.heroFOV[target.pos]);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);assertTrue(charge(Dungeon.hero.belongings.getItem(HolyTome.class))>=1);cast(ShieldOfLight.INSTANCE,target.pos);log("FRESH DARK SEVEN CLOSED DOOR CAMP INTERRUPTER REAL BLESS SPEAR AND SHIELD");
        if(Boolean.getBoolean("route.freshSevenClosedSkeletonFinish")){
            for(int n=0;n<3 && !Dungeon.level.adjacent(Dungeon.hero.pos,target.pos);n++)waitOne();
            for(int n=0;n<3 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());log("FRESH DARK SEVEN CLOSED DOOR CAMP INTERRUPTER ACTUALLY DEFEATED");
            if(Boolean.getBoolean("route.freshSevenClosedPostFightCamp")){
                for(int c:new int[]{984,984,983}){click(c);assertEquals(c,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));
                for(int n=0;n<125 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;n++){
                    if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
                }
                log("FRESH DARK SEVEN CLOSED DOOR POST FIGHT ACTUAL PREPARATION BOUNDARY");
                if(Boolean.getBoolean("route.freshSevenSecondSkeletonKite")){
                    for(int c:new int[]{984,985,986,954,921,888,854,820,819,786,753,720,686,652,619,586,553,520,487,455,421,420,419,418,417,416}){
                        if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.adjacent(Dungeon.hero.pos,m.pos)))break;
                        assertNull("Actual second skeleton retreat occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);
                    }
                    log("FRESH DARK SEVEN SECOND CAMP INTERRUPTER ACTUAL RETREAT RECHARGE BOUNDARY");
                }

            }

        }
    }

    private void freshSevenSouthApproachSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(506,Dungeon.hero.pos);
        for(int c:Boolean.getBoolean("route.freshSevenDmFlank") ? new int[]{538,538,506,473,440,408,409,410,411,412,413,414,415,416,449,482,515,548,581,614,647,680,713,746,779,812,811,810,809,808,807,806,805,804,803,802,801,768,735,702,701,700} : Boolean.getBoolean("route.freshSevenEastBank") ? new int[]{538,538,506,473,440,408,409,410,411,412,413,414,415,416,449,482,515,548,581,614,647,680,713,746,779,812,845,879,912,945,978} : new int[]{538,538,570,603,636,669,703,737,771,772,806,840,874,908,876,910,944,978}){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;
            if(c!=Dungeon.hero.pos)assertNull("Actual southern scroll approach occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);
        }
        log("FRESH DARK SEVEN ACTUAL SOUTHERN APPROACH OR ENEMY BOUNDARY");if(Boolean.getBoolean("route.freshSevenFlankSkeleton"))freshSevenFlankSkeletonSection();
    }

    private void freshSevenFlankSkeletonSection() throws Exception {
        assertEquals(7,Dungeon.depth);assertEquals(811,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==242).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);assertTrue(Dungeon.level.heroFOV[target.pos]);waitOne();waitOne();cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FRESH DARK SEVEN SOUTH FLANK SKELETON ACTUAL PAID BLESS");
        assertTrue(Dungeon.level.heroFOV[target.pos]);assertEquals(target.pos,new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,target.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos.intValue());throwItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class,target.pos);log("FRESH DARK SEVEN SOUTH FLANK SKELETON ACTUAL SPEAR RESULT");
        if(Boolean.getBoolean("route.freshSevenFlankSkeletonFinish")){
            if(Boolean.getBoolean("route.freshSevenChainEscape")){
                assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);log("FRESH DARK SEVEN ACTUAL GUARD CHAIN INTERRUPTS FLANK FIGHT");assertEquals(911,Dungeon.hero.pos);click(945);log("FRESH DARK SEVEN ACTUAL CRIPPLED EXIT APPROACH");assertEquals(945,Dungeon.hero.pos);click(977);if(Dungeon.depth==7 && Dungeon.hero.pos==977)click(977);assertEquals(8,Dungeon.depth);log("FRESH ACTUAL LEVEL ONE THROUGH SEVEN CHAIN ESCAPE AND EIGHT ENTRY");System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));if(Boolean.getBoolean("route.freshEightEntryCamp"))freshEightEntryCampSection();return;
            }
            for(int n=0;n<3 && !Dungeon.level.adjacent(Dungeon.hero.pos,target.pos);n++)waitOne();
            for(int n=0;n<3 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);}assertFalse(target.isAlive());log("FRESH DARK SEVEN SOUTH FLANK SKELETON ACTUALLY DEFEATED");
        }
    }

    private void freshEightEntryCampSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(997,Dungeon.hero.pos);assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear.class));assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel.class));
        for(int n=0;n<180 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<Integer.getInteger("route.freshEightCampTarget",3);n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
        }
        log("FRESH EIGHT ACTUAL ENTRANCE PREPARATION OR ENEMY BOUNDARY WITHOUT LOST WEAPONS");if(Boolean.getBoolean("route.freshEightFoodApproach"))freshEightFoodApproachSection();
    }

    private void freshEightFoodApproachSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(997,Dungeon.hero.pos);assertEquals(Integer.getInteger("route.freshEightCampTarget",3).intValue(),charge(Dungeon.hero.belongings.getItem(HolyTome.class)));cast(HolyWard.INSTANCE,null);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
        for(int c:new int[]{1029,1062,1095,1126,1125,1124,1091,1058,1026,994,963,932,901,870,838,806,774,742,710,678,646,614,582,551,520,489,458}){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertNull("Actual eight food path occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);
        }
        log("FRESH EIGHT ACTUAL NORTHERN FOOD APPROACH OR ENEMY BOUNDARY");if(Boolean.getBoolean("route.freshEightFirstGuard"))freshEightFirstGuardSection();
    }

    private void freshEightFirstGuardSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(614,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==278).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard);assertTrue(Dungeon.level.heroFOV[target.pos]);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FRESH EIGHT FOOD GUARD REAL BLESS AND CHAIN RESULT");cast(ShieldOfLight.INSTANCE,target.pos);log("FRESH EIGHT FOOD GUARD ACTUAL SHIELD");
        if(Boolean.getBoolean("route.freshEightFirstGuardMelee")){
            for(int n=0;n<8 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);assertEquals(1,Dungeon.hero.HP);}assertFalse(target.isAlive());log("FRESH EIGHT FIRST FOOD GUARD ACTUALLY DEFEATED");if(Boolean.getBoolean("route.freshEightDmFood"))freshEightDmFoodSection();
        }
    }

    private void freshEightDmFoodSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(582,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==279).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100);
        for(int c:new int[]{551,520,489,458}){assertNull(Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);}if(Dungeon.level.map[427]==Terrain.SECRET_DOOR){Dungeon.hero.search(true);ready();}assertTrue(Dungeon.level.map[427]==Terrain.DOOR || Dungeon.level.map[427]==Terrain.OPEN_DOOR);
        if(Boolean.getBoolean("route.freshEightInterveningThief")){
            Mob thief=Dungeon.level.mobs.stream().filter(m->m.id()==274).findFirst().get();assertTrue(thief instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief);freshFixedActions(thief,"C426,C425,C424,C425");cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
            for(int n=0;n<8 && thief.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,thief.pos));click(thief.pos);assertEquals(1,Dungeon.hero.HP);}assertFalse(thief.isAlive());log("FRESH EIGHT ACTUAL INTERVENING THIEF DEFEATED BEFORE FOOD DOOR");
            for(int n=0;n<(Boolean.getBoolean("route.freshEightThiefRushFood") ? 0 : 50) && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<1;n++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<440);waitOne();}log("FRESH EIGHT ACTUAL POST THIEF LAST FOOD DOOR RECHARGE");click(426);assertEquals(426,Dungeon.hero.pos);click(458);assertEquals(458,Dungeon.hero.pos);
        }
        for(int n=0;n<(Boolean.getBoolean("route.freshEightThiefRushFood") ? 0 : 10) && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<1;n++){if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;waitOne();}log("FRESH EIGHT FOOD SECRET DOOR ACTUAL PAID LAST CHARGE");if(!Boolean.getBoolean("route.freshEightThiefRushFood"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);click(427);assertEquals(427,Dungeon.hero.pos);assertTrue(Dungeon.level.heroFOV[target.pos]);assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));log("FRESH EIGHT ACTUAL ADJACENT FOOD DM CONTACT");
        if(Boolean.getBoolean("route.freshEightDmFoodFinish")){
            for(int n=0;n<6 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);assertEquals(1,Dungeon.hero.HP);}assertFalse(target.isAlive());log("FRESH EIGHT ACTUAL FOOD DM DEFEATED WITHOUT A SPEAR");
            for(int c:new int[]{396,365,365}){if(c!=Dungeon.hero.pos)assertNull(Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);}Item food=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty.class);assertNotNull(food);assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<450);food.execute(Dungeon.hero,"EAT");ready();log("FRESH EIGHT GUARDED PASTY ACTUALLY ACQUIRED AND EATEN ON CONTINUOUS LEVEL ONE RUN");if(Boolean.getBoolean("route.freshEightAfterPasty"))freshEightAfterPastySection();
        }
    }

    private void freshEightAfterPastySection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(365,Dungeon.hero.pos);assertEquals(38,Dungeon.hero.exp);
        for(int c:new int[]{366,366}){click(c);assertEquals(c,Dungeon.hero.pos);}assertNotNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation.class));log("FRESH EIGHT ACTUAL OWNED LEVITATION AFTER PASTY");
        for(int n=0;n<160 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<4;n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
        }
        log("FRESH EIGHT POST PASTY ACTUAL PAID RESERVE OR ENEMY BOUNDARY");if(Boolean.getBoolean("route.freshEightPastySkeleton"))freshEightPastySkeletonSection();
    }

    private void freshEightPastySkeletonSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(366,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==281).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);assertTrue(Dungeon.level.heroFOV[target.pos]);assertEquals(25,target.HP);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FRESH EIGHT POST PASTY SKELETON REAL BLESS ONLY RESERVE");
        for(int n=0;n<3 && !Dungeon.level.adjacent(Dungeon.hero.pos,target.pos);n++)waitOne();
        for(int n=0;n<6 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);assertEquals(1,Dungeon.hero.HP);}assertFalse(target.isAlive());assertEquals(8,Dungeon.hero.lvl);assertEquals(1,Dungeon.hero.talentPointsAvailable(2));Dungeon.hero.upgradeTalent(Talent.BLESS);assertEquals(2,Dungeon.hero.pointsInTalent(Talent.BLESS));log("FRESH EIGHT SKELETON ACTUAL DEATH AND EARNED LEVEL EIGHT BLESS TWO");if(Boolean.getBoolean("route.freshEightStrengthPrep"))freshEightStrengthPrepSection();
    }

    private void freshEightStrengthPrepSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(366,Dungeon.hero.pos);assertEquals(8,Dungeon.hero.lvl);assertEquals(2,Dungeon.hero.pointsInTalent(Talent.BLESS));
        for(int n=0;n<165 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<4;n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
        }
        log("FRESH EIGHT LEVEL EIGHT ACTUAL STRENGTH PREPARATION BOUNDARY");if(Boolean.getBoolean("route.freshEightPrepDm"))freshEightPrepDmSection();
    }

    private void freshEightPrepDmSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(366,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==293).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100);assertTrue(Dungeon.level.heroFOV[target.pos]);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FRESH EIGHT ACTUAL FIRST EARNED BLESS TWO AGAINST RECHARGE DM");
        for(int c:new int[]{397,396}){assertNull(Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);}log("FRESH EIGHT ACTUAL RECHARGE DM APPROACH");
        for(int n=0;n<6 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);assertEquals(1,Dungeon.hero.HP);}assertFalse(target.isAlive());log("FRESH EIGHT RECHARGE DM ACTUALLY DEFEATED WITH EARNED BLESS TWO");if(Boolean.getBoolean("route.freshEightStrengthWalk"))freshEightStrengthWalkSection();
    }

    private void freshEightStrengthWalkSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(396,Dungeon.hero.pos);assertNull(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class));
        for(int c:new int[]{427,458,489,520,551,582,614,646,647,680,712,744,776,809}){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertNull("Actual earned strength path occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);
        }
        log("FRESH EIGHT ACTUAL STRENGTH ROOM APPROACH OR ENEMY BOUNDARY");
        if(Boolean.getBoolean("route.freshEightFinishStrength")){
            assertEquals(680,Dungeon.hero.pos);for(int c:new int[]{712,744,776,809}){assertNull(Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);}log("FRESH EIGHT ACTUAL STRENGTH ROOM WITH DISTANT SKELETON STILL ALIVE");
        }
        if(Dungeon.hero.pos==809){click(809);Item potion=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class);assertNotNull(potion);potion.execute(Dungeon.hero,"DRINK");ready();assertEquals(14,Dungeon.hero.STR());assertEquals(1,Dungeon.hero.HP);log("FRESH EIGHT FOURTH STRENGTH ACTUALLY ACQUIRED AND DRUNK");if(Boolean.getBoolean("route.freshEightStrengthReserve"))freshEightStrengthReserveSection();}
    }

    private void freshEightStrengthReserveSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(809,Dungeon.hero.pos);assertEquals(14,Dungeon.hero.STR());
        for(int n=0;n<150 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<4;n++){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
        }
        log("FRESH EIGHT ACTUAL STRENGTH ROOM RESERVE OR ENEMY BOUNDARY");if(Boolean.getBoolean("route.freshEightStrengthIntercept"))freshEightStrengthInterceptSection();
    }

    private void freshEightStrengthInterceptSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(809,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==290).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton);if(!Boolean.getBoolean("route.freshEightSkipStrengthWard"))cast(HolyWard.INSTANCE,null);cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
        for(int c:new int[]{776,744}){assertNull(Actor.findChar(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);}log("FRESH EIGHT ACTUAL STRENGTH ROOM SKELETON INTERCEPTION");
        if(Boolean.getBoolean("route.freshEightStrengthInterceptMelee")){
            for(int n=0;n<6 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);assertEquals(1,Dungeon.hero.HP);}assertFalse(target.isAlive());log("FRESH EIGHT STRENGTH EXIT SKELETON ACTUALLY DEFEATED");if(Boolean.getBoolean("route.freshEightReturnStair"))freshEightReturnStairSection();
        }
    }

    private void freshEightReturnStairSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(744,Dungeon.hero.pos);
        for(int c:new int[]{712,680,647,678,710,742,774,806,838,870,901,932,963,994,1026,1058,1091,1124,1125,1126,1095,1062,1029}){
            if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && m.id()!=295 && Dungeon.level.heroFOV[m.pos]))break;assertNull("Actual stair return path occupied at "+c,Actor.findChar(c));assertNull(Dungeon.level.traps.get(c));if(c==1124 && Boolean.getBoolean("route.freshEightBlindBless")){cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FRESH EIGHT ACTUAL BLESS TWO BEFORE BLIND LIGHTNING CORNER");}click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);if(c==1124 && Boolean.getBoolean("route.freshEightStopBlind"))break;
        }
        log("FRESH EIGHT ACTUAL RETURN TOWARD STAIR OR NEW ENEMY BOUNDARY");
        if(Dungeon.hero.pos==1124 && Boolean.getBoolean("route.freshEightBlindLure"))freshEightBlindLureSection();
        if(Dungeon.hero.pos==1029){
            for(int n=0;n<120 && charge(Dungeon.hero.belongings.getItem(HolyTome.class))<3;n++){
                if(Dungeon.level.mobs.stream().anyMatch(m->m.alignment==Char.Alignment.ENEMY && Dungeon.level.heroFOV[m.pos]))break;assertTrue(Dungeon.hero.buff(Hunger.class).hunger()<430);waitOne();
            }
            log("FRESH EIGHT ACTUAL PAID STAIR RETURN PREPARATION BOUNDARY");if(Boolean.getBoolean("route.manualTail"))manualTailSection();
        }
    }

    private void freshEightBlindLureSection() throws Exception {
        assertEquals(8,Dungeon.depth);assertEquals(1124,Dungeon.hero.pos);Mob target=Dungeon.level.mobs.stream().filter(m->m.id()==296).findFirst().get();assertTrue(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100);for(String action:System.getProperty("route.freshEightBlindLureActions","C1091,H,C1124").split(",")){freshFixedActions(target,action);if(Boolean.getBoolean("route.freshEightGeometry")){com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica ray=new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(target.pos,Dungeon.hero.pos,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT);System.out.println("DM_COVER_GEOMETRY "+new JSONObject().put("action",action).put("source",target.pos).put("hero",Dungeon.hero.pos).put("collision",ray.collisionPos).put("path",new JSONArray(ray.path)));}}log("FRESH EIGHT ACTUAL BLIND CORNER DM COVER LURE");
        if(Boolean.getBoolean("route.freshEightBlindLureMelee")){
            if(Boolean.getBoolean("route.freshEightPostLureBless")){cast(BlessSpell.INSTANCE,Dungeon.hero.pos);log("FRESH EIGHT ACTUAL SAVED WARD CHARGE SPENT ON POST LURE BLESS");}
            for(int n=0;n<6 && target.isAlive();n++){assertTrue(Dungeon.level.adjacent(Dungeon.hero.pos,target.pos));click(target.pos);assertEquals(1,Dungeon.hero.HP);}assertFalse(target.isAlive());log("FRESH EIGHT BLIND CORNER DM ACTUALLY DEFEATED");
            if(Dungeon.hero.pos==1091){click(1124);assertEquals(1124,Dungeon.hero.pos);}
            for(int c:new int[]{1125,1126,1095,1062,1029}){assertNull(Actor.findChar(c));click(c);assertEquals(c,Dungeon.hero.pos);assertEquals(1,Dungeon.hero.HP);}log("FRESH EIGHT BLIND CORNER CLEARED AND ACTUAL RETURN BELOW STAIR");
        }
    }

    //Manual commands and fixed replay only: no route search, action selection or game-state grants.
    private void manualTailSection() throws Exception {
        java.util.Iterator<String> recorded=System.getProperty("route.manualReplay")==null ? java.util.Collections.<String>emptyList().iterator() : java.nio.file.Files.readAllLines(java.nio.file.Path.of(System.getProperty("route.manualReplay"))).iterator();
        boolean interactive=System.getProperty("route.manualReplay")==null || Boolean.getBoolean("route.manualContinue");
        java.io.BufferedReader input=new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        for(int n=0;n<10000;n++){
            String line;
            if(recorded.hasNext())line=recorded.next();else if(interactive){System.out.println("MANUAL_READY");System.out.flush();line=input.readLine();}else break;
            if(line==null || line.trim().equals("END"))break;line=line.trim();if(line.isEmpty() || line.startsWith("#"))continue;
            if(System.getProperty("route.manualJournal")!=null)java.nio.file.Files.writeString(java.nio.file.Path.of(System.getProperty("route.manualJournal")),line+"\n",java.nio.charset.StandardCharsets.UTF_8,java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
            System.out.println("MANUAL_COMMAND "+line);int divider=line.indexOf('|');assertTrue("Manual line is explicit target-id|commands",divider>0);int targetId=Integer.parseInt(line.substring(0,divider));Mob target=null;
            if(targetId>=0){Actor actor=Actor.findById(targetId);assertTrue(actor instanceof Mob);target=(Mob)actor;assertEquals(Char.Alignment.ENEMY,target.alignment);}
            for(String action:line.substring(divider+1).split(",")){
                if(action.equals("B"))cast(BlessSpell.INSTANCE,Dungeon.hero.pos);
                else if(action.equals("F"))activateOwnedHourglassFreeze();
                else if(action.equals("N"))activateOwnedHourglassStasis();
                else if(action.equals("P")){Dungeon.hero.search(true);ready();log("MANUAL ACTUAL PAID SEARCH");}
                else if(action.startsWith("Y")){int at=action.indexOf('@'),arrow=action.indexOf('>');assertTrue(at>1 && arrow>at);int pot=Integer.parseInt(action.substring(1,at));Class<? extends Item> inputType=Class.forName("com.shatteredpixel.shatteredpixeldungeon.items."+action.substring(at+1,arrow)).asSubclass(Item.class);Class<? extends Item> outputType=Class.forName("com.shatteredpixel.shatteredpixeldungeon.items."+action.substring(arrow+1)).asSubclass(Item.class);craftExoticPotionAtActualPot(pot,inputType,outputType);}
                else if(action.startsWith("Z")){Talent talent=Talent.valueOf(action.substring(1));int tier=-1;for(int k=0;k<Dungeon.hero.talents.size();k++)if(Dungeon.hero.talents.get(k).containsKey(talent))tier=k;assertTrue(tier>=0);assertTrue(Dungeon.hero.talentPointsAvailable(tier+1)>0);assertTrue(Dungeon.hero.pointsInTalent(talent)<talent.maxPoints());Dungeon.hero.upgradeTalent(talent);log("MANUAL EARNED TALENT "+talent.name());}
                else if(action.startsWith("X")){int at=action.lastIndexOf('@');assertTrue(at>1);Class<? extends Item> type=Class.forName("com.shatteredpixel.shatteredpixeldungeon.items."+action.substring(1,at)).asSubclass(Item.class);throwItem(type,Integer.parseInt(action.substring(at+1)));}
                else if(action.startsWith("H") && action.length()>1){int count=Integer.parseInt(action.substring(1));assertTrue(count>=0 && count<=500);for(int k=0;k<count;k++)waitOne();}
                else if(action.startsWith("a")){assertNotNull(target);int count=Integer.parseInt(action.substring(1));assertTrue(count>0 && count<=30);for(int k=0;k<count && target.isAlive();k++)freshFixedActions(target,"A");}
                else if(action.startsWith("G"))click(Integer.parseInt(action.substring(1)));
                else if(action.startsWith("J"))jumpIntoKnownChasm(Integer.parseInt(action.substring(1)));
                else if(action.equals("V"))System.out.println("FLOOR_LAYOUT "+new JSONObject().put("depth",Dungeon.depth).put("width",Dungeon.level.width()).put("height",Dungeon.level.height()).put("terrain",new JSONArray(Dungeon.level.map)).put("entrance",Dungeon.level.entrance()).put("exit",Dungeon.level.exit()));
                else if(action.startsWith("D") || action.startsWith("E") || action.startsWith("Q") || action.startsWith("U") || action.startsWith("R")){
                    Class<? extends Item> itemType=Class.forName("com.shatteredpixel.shatteredpixeldungeon.items."+action.substring(1)).asSubclass(Item.class);Item item=Dungeon.hero.belongings.getItem(itemType);assertNotNull("Manual action requires an actually owned item",item);
                    if(action.startsWith("U"))upgradeOwnedGear(item);else{String itemAction=action.startsWith("D")?"DRINK":action.startsWith("E")?"EAT":action.startsWith("R")?"READ":"EQUIP";assertTrue(item.actions(Dungeon.hero).contains(itemAction));item.execute(Dungeon.hero,itemAction);if(itemAction.equals("EQUIP"))Dungeon.hero.busy();ready();log("MANUAL OWNED "+itemAction+" "+itemType.getSimpleName());}
                }else {if(action.equals("S") || action.equals("L") || action.equals("T") || action.equals("A"))assertNotNull(target);freshFixedActions(target,action);}
                assertEquals(1,Dungeon.hero.HT);assertEquals(1,Dungeon.hero.HP);
            }
            log("MANUAL_BOUNDARY");
        }
    }

}
