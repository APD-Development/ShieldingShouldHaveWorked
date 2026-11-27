package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdRecorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.RecorderStatus;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AmuletScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.utils.FileUtils;
import com.watabou.utils.GameSettings;
import com.watabou.utils.Random;
import com.watabou.utils.Signal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//The simulator's driver: runs once per frame on the render thread, plays the bot's turns, watches for bugs and files reports.
public final class SimRuntime {

	public static final HeroClass[] CLASSES = HeroClass.values();
	public static final String[] SPEED_LABELS = {"1x", "2x", "4x", "max"};
	//game frames per drawn frame for 1x, 2x, 4x and max (the game's own playback speed stays at normal)
	private static final int[] SPEED_STEPS = {1, 2, 4, 400};
	//the bot's thinking pause in game time, the same at every speed so runs replay exactly
	private static final float BOT_THINK = 0.20f;

	private static SimRuntime instance;

	//the game's log window replaces every listener when it is created, so this is re-added every frame
	private static final Signal.Listener<String> LOG_LISTENER = new Signal.Listener<String>(){
		@Override public boolean onSignal(String text){
			if (instance != null) instance.gameMessage(text);
			return false;
		}
	};

	//desktop test runs only (set by the desktop launcher)
	public static boolean autostart;
	public static int exitAfterRuns;
	public static int forcedSpeed = -1;
	public static String forcedAggression, forcedIntelligence;
	public static int forcedCheckpoint;
	public static int forcedVerify = -1;
	public static long forcedSeedBase;
	private int finishedRuns;

	public static SimRuntime get(){ return instance; }

	//the REAL game's history, read only (seeds of real runs, Try my death, play like the owner); set by the launcher
	private static HistorySource history;
	public static void history(HistorySource h){ history = h; }
	public static HistorySource history(){ return history; }
	public static void realHistoryRoot(final java.io.File root){
		history = new HistorySource(){
			@Override public String status(){
				java.io.File[] f = new java.io.File(root, "runs").listFiles();
				return f == null ? "APD's save folder not found (" + root + ")" : "APD's save folder (read only): " + root;
			}
			@Override public java.util.List<java.io.File> runLogs(int max){
				java.util.List<java.io.File> out = new java.util.ArrayList<>();
				java.io.File[] f = new java.io.File(root, "runs").listFiles();
				if (f == null) return out;
				java.util.Arrays.sort(f, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
				for (java.io.File x : f) if (x.isFile() && x.getName().endsWith(".apdlog") && out.size() < max) out.add(x);
				return out;
			}
		};
	}

	//"Send this run to the simulator" from the Developer build (phone)
	public static volatile String[] requested;
	public static void requestedRun(String seed, String heroClass, int difficulty, int challenges, String customRules, boolean tryDeath){
		requested = new String[]{seed, heroClass, String.valueOf(difficulty), String.valueOf(challenges), customRules == null ? "" : customRules, String.valueOf(tryDeath)};
	}

	final SimPlatform platform;
	final ReportStore reports = new ReportStore();
	final TurnLog log = new TurnLog();
	private final List<Object[]> pendingExceptions = new ArrayList<>();

	//settings that outlive the app (the simulator's own settings file, never the game's)
	public boolean playing;
	public boolean paused;
	//the player has the controls (take over / Chauffeur); the bot watches and does nothing
	public boolean driving;
	private boolean chauffeurDone;
	boolean firstWinHit;
	public int speed;

	private SimProfile profile;
	public SimProfile profile(){
		if (profile == null) profile = SimProfile.load(GameSettings.getInt("apdsim_profile_current", 0));
		return profile;
	}
	public void reloadProfile(){ profile = null; }
	private SimProfile runProfile;  //the profile the current run was started with (a replay may use a report's copy)
	public String runSettings = "";
	private long stopClockStart;
	private int runsSinceStart;
	public String pauseReason = "";
	private float replayTargetTurn = -1;

	public void resetStopClock(){ stopClockStart = System.currentTimeMillis(); runsSinceStart = 0; firstBugHit = firstWinHit = floorStopHit = false; }

	//the current run
	public int run;
	public HeroClass heroClass;
	public long seed, botSeed;
	public boolean replaying;
	SimBot bot;
	private boolean runActive;
	private long runStartMillis;
	private String recorderLogId;
	private int recorderErrors;
	private long recorderDropped;
	public int bugsThisRun, newBugsTotal;
	public String lastRunSummary = "";
	public String status = "";

	//watchers
	private float thinkTimer;
	private float deadTimer;
	private Window lastWindow;
	private int sameWindowActions;
	private long lastFrameNanos;
	private int framesSinceSceneChange;
	private Scene lastScene;
	private long waitingSince;
	private boolean slowTurnReported;
	private float lastProgressTurn;
	private long progressKey = Long.MIN_VALUE;
	private float lastActionTurn = -1;
	private int zeroTimeActions;
	private int depthEntries;
	private int maxDepth;
	private SimHud hud;
	private GameScene hudScene;
	private long interlevelSince;
	private final java.util.Set<String> textSeen = new java.util.HashSet<>();
	private int itemCheckCounter;
	private int pendingFrames;
	private int busySamples, idleSamples;
	private boolean runTagsRecorded;
	private int checkpointedDepth;
	private final SaveLoadCheck saveLoad = new SaveLoadCheck();
	private long nextSaveLoadTurn = 300;
	public static int saveLoadEvery = 300; //turns between save/load stress checks (0 = off)
	public String startedFrom = "";

	private SimRuntime(SimPlatform platform){
		this.platform = platform;
	}

	//Called by the simulator's launcher before the game starts. Never called in the real game.
	public static synchronized void install(SimPlatform platform){
		if (instance != null) return;
		instance = new SimRuntime(platform);
		NetworkGuard.install(instance);
		StableTurnOrder.install();
		GLog.update.add(LOG_LISTENER);
		final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
			try { instance.crash(thread, error); } catch (Throwable ignored){}
			if (previous != null) previous.uncaughtException(thread, error);
		});
	}

	void loadSettings(){
		playing = GameSettings.getBoolean("apdsim_playing", false);
		speed = GameSettings.getInt("apdsim_speed", 0, 0, SPEED_LABELS.length - 1);
		run = GameSettings.getInt("apdsim_run", 0);
		newBugsTotal = GameSettings.getInt("apdsim_bugs", 0);
		lastRunSummary = GameSettings.getString("apdsim_last", "");
		if (autostart){
			playing = true;
			if (forcedSpeed >= 0) speed = forcedSpeed;
			if (forcedAggression != null) profile().aggression = SimProfile.Aggression.valueOf(forcedAggression);
			if (forcedIntelligence != null) profile().intelligence = SimProfile.Intelligence.valueOf(forcedIntelligence);
			if (forcedCheckpoint > 0) profile().checkpointFloor = forcedCheckpoint;
			if (forcedVerify >= 0) profile().verifyEvery = forcedVerify;
			resetStopClock();
		}
	}

	private boolean settingsLoaded;

	public void setPlaying(boolean p){ playing = p; GameSettings.put("apdsim_playing", p); }
	public void setSpeed(int s){ speed = s; GameSettings.put("apdsim_speed", s); }
	public void cycleSpeed(){ setSpeed((speed + 1) % SPEED_LABELS.length); }
	public String speedLabel(){ return SPEED_LABELS[speed]; }
	public int reportCount(){ return reports.count(); }
	public ReportStore store(){ return reports; }

	//---- the frame -------------------------------------------------------------------------------------------

	public void frame(){
		try {
			frameInner();
		} catch (Throwable t){
			botProblem("simulator frame", t);
		}
	}

	private void frameInner(){
		if (!settingsLoaded){
			settingsLoaded = true;
			loadSettings();
			//a fresh simulator: no tutorial, quiet by default
			SPDSettings.intro(false);
			if (!GameSettings.contains("apdsim_sound")){
				GameSettings.put("apdsim_sound", false);
				SPDSettings.music(false);
				SPDSettings.soundFx(false);
			}
		}
		long now = System.nanoTime();
		float realDelta = lastFrameNanos == 0 ? 0 : (now - lastFrameNanos) / 1e9f;
		lastFrameNanos = now;

		flushExceptions();
		GLog.update.add(LOG_LISTENER);

		Scene scene = Game.scene();
		if (scene != lastScene){ lastScene = scene; framesSinceSceneChange = 0; }
		else framesSinceSceneChange++;
		if (scene == null || Game.switchingScene()) return;

		if (scene instanceof TitleScene || (!(scene instanceof GameScene) && !(scene instanceof InterlevelScene)
				&& !(scene instanceof AmuletScene) && !(scene instanceof SimHomeScene) && isMenuScene(scene))){
			if (runActive && scene.getClass().getSimpleName().equals("RankingsScene")) endRun(lastOutcome == null ? "won" : lastOutcome);
			runActive = false;
			ShatteredPixelDungeon.switchNoFade(SimHomeScene.class);
			return;
		}
		autoPause();
		if (scene instanceof SimHomeScene){
			if (playing && !paused && pauseReason.isEmpty() && !stopReached()) startRun();
			return;
		}
		if (scene instanceof InterlevelScene){
			//a region's story is shown with a Continue button: read it (text checks), then continue like a player
			if (!paused && framesSinceSceneChange > 10){
				for (SimUi.Choice c : SimUi.buttons(scene)){
					if (c.label != null && !c.label.trim().isEmpty() && (!(c.button instanceof StyledButton) || ((StyledButton) c.button).alpha() > 0.99f)){
						for (String t : SimUi.texts(scene)) checkText(t, "story screen");
						log.turn("story screen: press \"" + c.label + "\"");
						click(c.button);
						break;
					}
				}
			}
			if (interlevelSince == 0) interlevelSince = System.currentTimeMillis();
			else if (System.currentTimeMillis() - interlevelSince > 90_000 && runActive){
				report(BugReport.Kind.STUCK, "interlevel", "Loading the next floor never finished (over 90 seconds on the loading screen).", threadDump());
				interlevelSince = System.currentTimeMillis();
			}
			return;
		}
		interlevelSince = 0;
		if (scene instanceof AmuletScene){
			if (paused) return;
			for (SimUi.Choice c : SimUi.buttons(scene)){
				if (c.button instanceof StyledButton && !tried("amulet")){
					log.turn("found the Amulet: choose to leave the dungeon (win)");
					lastOutcome = "won";
					click(c.button);
					return;
				}
			}
			return;
		}
		if (scene instanceof GameScene) inGame((GameScene) scene, realDelta);
	}

	private String lastOutcome;
	private int amuletClicks;
	private boolean tried(String what){ return amuletClicks++ > 5; }

	private static boolean isMenuScene(Scene s){
		String n = s.getClass().getSimpleName();
		return n.equals("RankingsScene") || n.equals("HeroSelectScene") || n.equals("StartScene") || n.equals("WelcomeScene")
				|| n.equals("SurfaceScene");
	}

	private void inGame(GameScene scene, float realDelta){
		if (hudScene != scene){
			hudScene = scene;
			hud = new SimHud(this);
			scene.addToFront(hud);
			hud.layoutFor(scene);
		} else if (hud != null){
			scene.bringToFront(hud);
		}
		if (hud != null) hud.refresh();

		if (runActive && !runTagsRecorded && com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdFacts.isActive()){
			runTagsRecorded = true;
			SimFacts.recordRun(runProfile, replaying, runSettings + (startedFrom.isEmpty() ? "" : ",checkpoint=" + startedFrom), botSeed);
			recorderLogId = ApdRecorder.status().logId;
		}
		Hero h = Dungeon.hero;
		if (h == null || !runActive){
			if (h != null && !runActive && playing){
				//a run left over from before (e.g. after a crash): leave it and start fresh
				startRun();
			}
			return;
		}

		//frame timing (the bot's own work is included; level loading is excluded)
		if (framesSinceSceneChange > 3 && realDelta > 0.5f && !paused){
			report(BugReport.Kind.SLOW, "slowframe-d" + (Dungeon.depth / 5),
					String.format(Locale.ROOT, "A frame took %.0f ms (more than 500 ms) on floor %d.", realDelta * 1000, Dungeon.depth), "");
		}

		if (!h.isAlive()){
			Window w = SimUi.topWindow();
			if (w != null && !paused){ answerWindow(w); return; }
			if (deathTurn < 0) deathTurn = Statistics.apdTurn();
			deadTimer += SimGame.FIXED_DT;
			if (deadTimer > 1.5f){
				deadTimer = 0;
				endRun("died: " + deathCause());
				nextRun();
			}
			return;
		}
		deadTimer = 0;
		//Chauffeur: the bot plays to the chosen floor, then the player takes over
		if (runProfile != null && runProfile.chauffeurFloor > 0 && !chauffeurDone && Dungeon.depth >= runProfile.chauffeurFloor){
			chauffeurDone = true;
			driving = true;
			log.turn(where() + " Chauffeur: reached floor " + Dungeon.depth + ", the controls are yours");
			platform.notifyBug(7, "APD Simulator: Chauffeur", "Reached floor " + Dungeon.depth + ". The controls are yours.");
		}
		if (runProfile != null && runProfile.stop == SimProfile.Stop.FLOOR && Dungeon.depth >= runProfile.stopAmount && !floorStopHit){
			floorStopHit = true;
			endRun("reached floor " + Dungeon.depth + " (stop condition)");
			nextRun();
			return;
		}
		if (paused || driving) { waitingSince = 0; return; }

		if (!pauseReason.isEmpty()) return;
		PlaybackSpeed.apply(1);

		thinkTimer -= SimGame.FIXED_DT;
		Window w = SimUi.topWindow();
		if (w != null){
			scanWindowText(w);
			if (thinkTimer > 0) return;
			thinkTimer = BOT_THINK;
			answerWindow(w);
			return;
		}
		lastWindow = null;

		WndBag.ItemSelector pane = SimUi.paneSelector();
		if (pane != null){
			if (thinkTimer > 0) return;
			thinkTimer = BOT_THINK;
			Item pick = bot.pickItem(pane);
			SimUi.clearPaneSelector();
			log.turn(where() + " choose item: " + SimBot.nameOf(pick));
			pane.onSelect(pick);
			return;
		}

		//an action was given and the game has not taken it yet
		if (h.ready && h.curAction != null){
			pendingFrames++;
			if (pendingFrames < 120) return;
			report(BugReport.Kind.STUCK, "untaken:" + h.curAction.getClass().getSimpleName(),
					"The hero was given an action (" + h.curAction.getClass().getSimpleName() + ") that the game never carried out.", "");
			h.curAction = null;
		}
		pendingFrames = 0;

		if (!h.ready){
			if (waitingSince == 0){ waitingSince = System.currentTimeMillis(); busySamples = idleSamples = 0; }
			long waited = System.currentTimeMillis() - waitingSince;
			if (actorBusy()) busySamples++; else idleSamples++;
			//slow only if the game was computing (not animating or waiting) in at least 80% of the frames sampled
			if (waited > 4000 && !slowTurnReported && busySamples >= 20 && busySamples * 5 >= (busySamples + idleSamples) * 4){
				slowTurnReported = true;
				report(BugReport.Kind.SLOW, "slowturn:" + topFrames(actorThread(), 4),
						"One turn took more than 4 seconds to finish (the game was busy working, not animating).", threadDump());
			}
			if (waited > 45_000){
				waitingSince = System.currentTimeMillis();
				report(BugReport.Kind.STUCK, "frozen:" + topFrames(actorThread(), 4),
						"The game stopped responding: the hero's turn never came back for 45 seconds and no window was open.", threadDump());
				endRun("stuck (game stopped responding)");
				nextRun();
			}
			return;
		}
		waitingSince = 0;
		slowTurnReported = false;

		if (SimUi.choosingCell()){
			if (thinkTimer > 0) return;
			thinkTimer = BOT_THINK;
			int target = bot.pickCell();
			if (target < 0){
				log.turn(where() + " no target for this, cancel");
				GameScene.cancelCellSelector();
			} else {
				log.turn(where() + " target " + SimBot.cell(target));
				GameScene.handleCell(target);
			}
			return;
		}

		if (thinkTimer > 0) return;
		thinkTimer = BOT_THINK;

		if (Dungeon.depth > checkpointedDepth && Dungeon.depth > 1 && !replaying){
			checkpointedDepth = Dungeon.depth;
			Checkpoints.save(heroClass.name(), Dungeon.depth);
		}
		if (saveLoad.pending){ saveLoad.compare(this); }
		else if (saveLoadEvery > 0 && !replaying && Statistics.apdTurn() >= nextSaveLoadTurn){
			nextSaveLoadTurn = Statistics.apdTurn() + saveLoadEvery;
			log.turn(where() + " save and reload (save/load stress check)");
			saveLoadsThisRun++;
			saveLoad.start(this);
			return;
		}
		Invariants.check(this);
		checkItemsText();
		if (!checkProgress()) return;

		String did;
		try {
			did = bot.act();
			lastTurnSeen = Statistics.apdTurn();
		} catch (Throwable t){
			botProblem("bot decision", t);
			try { h.rest(false); } catch (Throwable ignored){}
			did = "wait (after a simulator problem)";
		}
		if (did != null){
			//determinism check: a running fingerprint of every action (with the hero's state), kept every 50 actions
			runDigest = runDigest * 1099511628211L + (where() + did).hashCode();
			if (++digestCount % 50 == 0) digests.add(runDigest);
		}
		if (did != null && TurnLog.echo){
			//full-trace mode: a fingerprint of every monster (place, health) so the first hidden difference between two replays shows
			long fp = 17;
			for (Mob m : Dungeon.level.mobs) fp = fp * 31 + m.pos * 1009L + m.HP * 7L + m.getClass().getSimpleName().hashCode();
			did = did + " #" + Long.toHexString(fp & 0xFFFFFF) + "/" + Dungeon.level.mobs.size() + " f" + lockFrames + "/" + freeFrames + " r" + (runRandom == null ? -1 : runRandom.draws)
					+ (runRandom == null ? "" : " [" + String.join(" | ", runRandom.lastCallers) + "]");
			if (runRandom != null) runRandom.lastCallers.clear();
		}
		if (did != null) log.turn(where() + " " + did);
		if (bot.refusedAttackNote != null){
			report(BugReport.Kind.STUCK, "refusedattack:" + bot.refusedAttackNote.replaceAll("[0-9(),]", "").replaceAll("\\s+", " ").substring(0, Math.min(120, bot.refusedAttackNote.length())),
					"An attack the game said was possible (canAttack) did not happen and took no time.", bot.refusedAttackNote);
			bot.refusedAttackNote = null;
		}
				if (bot.oscillation != null){
			report(BugReport.Kind.BOT_STUCK, "oscillate", "The bot was " + bot.oscillation + " and changed its plan.", "");
			bot.oscillation = null;
		}

		float t = Actor.now();
		if (t == lastActionTurn) zeroTimeActions++; else zeroTimeActions = 0;
		lastActionTurn = t;
		if (zeroTimeActions > 300){
			zeroTimeActions = 0;
			//the same action refused again and again is the game's problem; different actions going round in a loop is the bot's
			java.util.Set<String> kinds = new java.util.HashSet<>();
			for (String line : log.turns()) kinds.add(line.replaceAll("^\\d+\\. T\\d+ D\\d+ \\(\\d+,\\d+\\) HP \\d+/\\d+ L\\d+: ", "").replaceAll("[0-9(),]", ""));
			if (kinds.size() > 1){
				report(BugReport.Kind.BOT_STUCK, "botloop:" + (did == null ? "" : did.replaceAll("[0-9(),]", "")),
						"The bot went round in a loop of actions that take no game time (" + kinds.size() + " different actions).", String.join("\n", kinds));
			} else {
				report(BugReport.Kind.STUCK, "notime:" + (did == null ? "" : did.replaceAll("[0-9(),]", "")),
						"300 actions in a row took no game time (the game keeps refusing or undoing the same action). Last action: " + did, "");
			}
			endRun("stuck (actions take no time)");
			nextRun();
		}
	}

	private boolean checkProgress(){
		Hero h = Dungeon.hero;
		int seen = 0;
		for (boolean v : Dungeon.level.visited) if (v) seen++;
		long key = ((long) Dungeon.depth << 40) ^ ((long) seen << 20) ^ ((long) h.lvl << 12) ^ h.exp ^ ((long) Statistics.enemiesSlain << 30)
				^ ((long) Dungeon.gold << 8) ^ h.belongings.backpack.items.size();
		if (Dungeon.depth > maxDepth){ maxDepth = Dungeon.depth; depthEntries++; }
		if (key != progressKey){
			progressKey = key;
			lastProgressTurn = Actor.now();
			return true;
		}
		if (Actor.now() - lastProgressTurn > 1500){
			SimNav nav = new SimNav(Dungeon.level, h.pos, false);
			String why = bot.noWayForward(nav);
			FloorTruth.Result truth = FloorTruth.check(Dungeon.level, Dungeon.depth, h.pos);
			if (truth.problems.isEmpty()){
				//the real map has a way on: the bot was dumb, not the game
				report(BugReport.Kind.BOT_STUCK, "botstuck-d" + Dungeon.depth + ":" + why.replaceAll("[0-9]", "#"),
						"The bot made no progress for 1500 turns on floor " + Dungeon.depth + ", but the real map has a way on.", "Bot's view of the floor: " + why);
			} else {
				report(BugReport.Kind.STUCK, "noprogress-d" + Dungeon.depth + ":" + String.join("|", truth.problems).replaceAll("[0-9]+", "#"),
						"Stuck on floor " + Dungeon.depth + ": " + truth.problems.get(0) + ".",
						"Checked against the real map (hidden doors and keys included):\n- " + String.join("\n- ", truth.problems) + "\n\nBot's view of the floor: " + why);
			}
			endRun("stuck (no progress)");
			nextRun();
			return false;
		}
		return true;
	}

	//---- windows ---------------------------------------------------------------------------------------------

	private void scanWindowText(Window w){
		if (w == lastWindow) return;
		for (String t : SimUi.texts(w)) checkText(t, "window " + w.getClass().getName().replaceAll(".*\\.", ""));
	}

	private void answerWindow(Window w){
		String name = w.getClass().getName().replaceAll(".*\\.", "");
		if (w == lastWindow) sameWindowActions++; else sameWindowActions = 0;
		lastWindow = w;
		if (sameWindowActions > 40){
			report(BugReport.Kind.STUCK, "window:" + name, "A window (" + name + ") would not close after 40 tries.", String.join(" | ", SimUi.texts(w)));
			w.hide();
			return;
		}
		if (w instanceof WndBag && ((WndBag) w).getSelector() != null){
			WndBag.ItemSelector sel = ((WndBag) w).getSelector();
			Item pick = bot.pickItem(sel);
			log.turn(where() + " [" + name + "] choose item: " + SimBot.nameOf(pick));
			w.hide();
			sel.onSelect(pick);
			return;
		}
		List<SimUi.Choice> buttons = SimUi.buttons(w);
		List<SimUi.Choice> labelled = new ArrayList<>();
		for (SimUi.Choice c : buttons) if (!c.label.trim().isEmpty()) labelled.add(c);
		List<SimUi.Choice> from = labelled.isEmpty() ? buttons : labelled;
		if (from.isEmpty() || sameWindowActions > 25){
			log.turn(where() + " [" + name + "] close");
			w.onBackPressed();
			return;
		}
		List<String> labels = new ArrayList<>();
		for (SimUi.Choice c : from) labels.add(c.label);
		int pick = bot.pickButton(labels);
		if (pick < 0){
			w.onBackPressed();
			return;
		}
		log.turn(where() + " [" + name + "] press \"" + from.get(pick).label.replace('\n', ' ') + "\"");
		click(from.get(pick).button);
	}

	private void click(Button b){
		try {
			SimUi.click(b);
		} catch (java.lang.reflect.InvocationTargetException e){
			caught(e.getCause() == null ? e : e.getCause());
		} catch (Throwable t){
			botProblem("pressing a button", t);
		}
	}

	//---- runs ------------------------------------------------------------------------------------------------

	public void startRun(){
		//desktop testing: replay one exact run (class:seed:botSeed) given on the command line
		if (forcedReplay != null){
			String[] r = forcedReplay.split(":");
			forcedReplay = null;
			HeroClass cls = HeroClass.valueOf(r[0]);
			long rs = r[1].matches("-?\\d+") ? Long.parseLong(r[1]) : DungeonSeed.convertFromText(r[1]), rb = Long.parseLong(r[2]);
			startRun(cls.ordinal(), rs, rb);
			return;
		}
		startRun(-1, 0, 0);
	}
	public static String forcedReplay;

	//Same seed, class, bot seed and simulator settings as a report; plays fast until about 50 turns before the bug, then at 1x.
	public void startReplay(HeroClass cls, long seed, long botSeed, String profileText, int bugTurn){
		replaying = true;
		SimProfile copy = profileText == null || profileText.isEmpty() ? profile() : SimProfile.decode(profile().index, profileText);
		copy.randomizeSettings = false;
		replayProfile = copy;
		replayTargetTurn = Math.max(0, bugTurn - 50);
		setPlaying(false);
		paused = false;
		startRun(cls.ordinal(), seed, botSeed);
	}
	private SimProfile replayProfile;

	//Simulator history only: its UIMSD folder, bug reports and run list. The real game's folders are elsewhere and are never touched.
	public void clearHistory(){
		try {
			ApdRecorder.reset();
			FileUtils.getFileHandle("apd").deleteDirectory();
			FileUtils.getFileHandle("simruns.txt").delete();
			reports.deleteAll();
			SeedPicker.forget();
			newBugsTotal = 0;
			GameSettings.put("apdsim_bugs", 0);
			GameSettings.put("apdsim_last", "");
			lastRunSummary = "";
			ApdRecorder.configureDefault(SPDSettings.apdRecording());
		} catch (Throwable t){
			botProblem("clearing the simulator history", t);
		}
	}

	private void startRun(int classIndex, long fixedSeed, long fixedBotSeed){
		if (runActive) endRun("skipped");
		run++;
		GameSettings.put("apdsim_run", run);
		if (classIndex < 0) replaying = false;
		if (classIndex < 0){ replayProfile = null; replayTargetTurn = -1; }
		runProfile = replayProfile != null ? replayProfile : profile();
		java.util.Random master = new java.util.Random(System.nanoTime() ^ ((long) run << 32));
		heroClass = classIndex >= 0 ? CLASSES[classIndex]
				: runProfile.heroClass >= 0 ? CLASSES[runProfile.heroClass] : CLASSES[(rotation++) % CLASSES.length];
		if (fixedSeed != 0) seed = fixedSeed;
		else if (runProfile.seedMode != SimProfile.SeedMode.RANDOM && !runProfile.seedText.isEmpty()) seed = DungeonSeed.convertFromText(runProfile.seedText);
		else seed = SeedPicker.freshRandomSeed(master);
		botSeed = fixedBotSeed != 0 ? fixedBotSeed : master.nextLong();
		//benchmark mode (desktop): a fixed list of seeds, so bot changes are compared on the same dungeons
		if (forcedSeedBase > 0 && fixedSeed == 0){
			seed = Math.floorMod(forcedSeedBase * 1_000_003L + run * 7919L, DungeonSeed.TOTAL_SEEDS);
			botSeed = forcedSeedBase * 31L + run;
		}
		runSettings = runProfile.applyForRun(new java.util.Random(botSeed));
		runTagsRecorded = false;
		bot = new SimBot(botSeed, runProfile.aggression, runProfile.intelligence);
		runsSinceStart++;
		log.clear();
		textSeen.clear();
		bugsThisRun = 0;
		botProblemsThisRun = 0;
		crashesThisRun = 0;
		saveLoadsThisRun = 0;
		depthEntries = 0;
		maxDepth = 0;
		progressKey = Long.MIN_VALUE;
		zeroTimeActions = 0;
		amuletClicks = 0;
		lastOutcome = null;
		chauffeurDone = false;
		driving = false;
		runStartMillis = System.currentTimeMillis();

		SPDSettings.intro(false);
		RunContext.apply();
		SPDSettings.customSeed("");
		GamesInProgress.curSlot = 1;
		if (GamesInProgress.gameExists(1)) Dungeon.deleteGame(1, true);
		GamesInProgress.selectedClass = heroClass;
		GamesInProgress.randomizedClass = false;
		Dungeon.hero = null;
		Dungeon.daily = Dungeon.dailyReplay = false;
		Dungeon.initSeed();
		Dungeon.seed = seed;
		Dungeon.customSeedText = "";
		ActionIndicator.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		checkpointedDepth = 0;
		startedFrom = "";
		nextSaveLoadTurn = saveLoadEvery;
		saveLoad.pending = false;
		//start deep: continue a saved checkpoint instead of a new game (tagged, kept apart in the stats)
		if (fixedSeed == 0 && runProfile.checkpointFloor > 1){
			com.badlogic.gdx.files.FileHandle cp = Checkpoints.pick(master, runProfile.heroClass >= 0 ? heroClass.name() : null, runProfile.checkpointFloor);
			if (cp != null && Checkpoints.restore(cp)){
				startedFrom = cp.name();
				GamesInProgress.curSlot = 1;
				GamesInProgress.Info info = GamesInProgress.check(1);
				heroClass = info.heroClass;
				seed = info.seed;
				checkpointedDepth = info.depth;
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			}
		}
		runActive = true;
		SeedPicker.remember(seed);
		RecorderStatus rs = ApdRecorder.status();
		recorderErrors = rs.errorCount;
		recorderDropped = rs.eventsDropped;
		recorderLogId = null;
		status = "Run " + run + ": " + heroClass.title();
		//the render thread's random numbers (thrown items landing, item effects, visuals) come from this run's own seed too
		//everything this run does on this thread (turns, item effects, visuals) draws from its own seeded stream; level
		//generation on the loading thread keeps using the shared stack with its own per-floor seeds
		Random.isolateThread();
		runRandom = CountingRandom.pushOnIsolated(seed * 0x9E3779B97F4A7C15L ^ botSeed);
		CountingRandom.recordCallers = TurnLog.echo;
		sharedSeedCounter = 0;
		//nothing may carry over from the previous run into this one's timing
		thinkTimer = 0;
		deadTimer = 0;
		pendingFrames = 0;
		sameWindowActions = 0;
		lastWindow = null;
		waitingSince = 0;
		itemCheckCounter = 0;
		lastActionTurn = -1;
		lastTurnSeen = 0;
		deathTurn = -1;
		runDigest = 0;
		digestCount = 0;
		digests = new ArrayList<>();
		lockFrames = freeFrames = 0;
		seedSharedRandom();
		seededShared = true;
		lockstepTimeouts = 0;
		Game.switchScene(InterlevelScene.class);
	}

	//Try my death: one run on the seed and class of one of the owner's real runs, with this simulator's settings
	public void tryRun(String heroClassName, String seedCode){
		HeroClass cls;
		try { cls = HeroClass.valueOf(heroClassName.toUpperCase(Locale.ROOT)); } catch (RuntimeException e){ cls = CLASSES[0]; }
		replaying = true;
		replayProfile = null;
		replayTargetTurn = -1;
		setPlaying(false);
		paused = false;
		startRun(cls.ordinal(), DungeonSeed.convertFromText(seedCode), 0);
	}

	public void skipRun(){
		if (!runActive) return;
		endRun("skipped");
		nextRun();
	}

	private void nextRun(){
		//determinism check: play the run that just ended again, with the same seed and decisions, and compare
		if (pendingVerify != null && !verifying && playing && pauseReason.isEmpty()){
			verifying = true;
			startRun((Integer) pendingVerify[0], (Long) pendingVerify[1], (Long) pendingVerify[2]);
			return;
		}
		if (playing && !replaying && !stopReached() && pauseReason.isEmpty()) startRun();
		else {
			if (stopReached()) setPlaying(false);
			replaying = false;
			ShatteredPixelDungeon.switchNoFade(SimHomeScene.class);
		}
	}

	boolean firstBugHit, floorStopHit;

	public void takeOver(boolean player){
		driving = player;
		if (!player) thinkTimer = 0;
	}

	private boolean stopReached(){
		SimProfile p = profile();
		switch (p.stop){
			case RUNS: return runsSinceStart >= p.stopAmount;
			case MINUTES: return stopClockStart > 0 && System.currentTimeMillis() - stopClockStart >= p.stopAmount * 60_000L;
			case FIRST_BUG: return firstBugHit;
			case FIRST_WIN: return firstWinHit;
			case FLOOR: return floorStopHit;
			default: return false;
		}
	}

	private long lastPowerCheck;

	//pauses (and resumes) by itself on low battery, heat, or when "only while charging" is on and the phone is unplugged
	private void autoPause(){
		long now = System.currentTimeMillis();
		if (now - lastPowerCheck < 5000) return;
		lastPowerCheck = now;
		String reason = "";
		int battery = platform.batteryPercent();
		boolean charging = platform.charging();
		if (battery >= 0 && battery < 20 && !charging) reason = "battery under 20%";
		else if (platform.hot()) reason = "the phone is hot";
		else if (GameSettings.getBoolean("apdsim_only_charging", false) && !charging) reason = "only runs while charging";
		pauseReason = reason;
	}

	public void stopToHome(){
		setPlaying(false);
		if (runActive) endRun("stopped");
		ShatteredPixelDungeon.switchNoFade(SimHomeScene.class);
	}

	private String deathCause(){
		try {
			com.shatteredpixel.shatteredpixeldungeon.Rankings r = com.shatteredpixel.shatteredpixeldungeon.Rankings.INSTANCE;
			if (r.records != null && r.lastRecord >= 0 && r.lastRecord < r.records.size()){
				String d = r.records.get(r.lastRecord).desc();
				checkText(d, "death screen text");
				return d;
			}
		} catch (Throwable ignored){}
		return "killed on floor " + Dungeon.depth;
	}

	private float lastTurnSeen;
	private long deathTurn = -1;

	void endRun(String outcome){
		if (!runActive) return;
		if (outcome.startsWith("won")) firstWinHit = true;
		runActive = false;
		int depth = Dungeon.depth;
		Hero h = Dungeon.hero;
		long minutes = (System.currentTimeMillis() - runStartMillis) / 60000;
		lastRunSummary = String.format(Locale.ROOT, "Run %d %s: %s on floor %d, level %d, turn %.0f, %d bug report(s), %d min",
				run, heroClass == null ? "?" : heroClass.title(), outcome, depth, h == null ? 0 : h.lvl, (float) (deathTurn >= 0 ? deathTurn : Math.max(Statistics.apdTurn(), lastTurnSeen)), bugsThisRun, minutes);
		GameSettings.put("apdsim_last", lastRunSummary);
		System.out.println("APDSIM_RUN " + lastRunSummary + (verifying ? " [determinism replay]" : "") + " digest " + Long.toHexString(runDigest));
		if (autostart){
			for (String t : log.turns()) System.out.println("APDSIM_TURN " + t);
			for (String m : log.messages()) System.out.println("APDSIM_MSG " + m);
		}
		finishedRuns++;
		if (autostart && exitAfterRuns > 0 && finishedRuns >= exitAfterRuns){
			Game.runOnRenderThread(() -> platform.close());
		}
		try {
			FileUtils.getFileHandle("simruns.txt").writeString(lastRunSummary + " | seed " + DungeonSeed.convertToCode(seed)
					+ " | bot seed " + botSeed + " | build " + Game.version + " | crashes " + crashesThisRun + " | deepest " + maxDepth + " | save/load checks " + saveLoadsThisRun
					+ (startedFrom.isEmpty() ? "" : " | checkpoint") + "\n", true, "UTF-8");
			Digest.runEnded(this, maxDepth);
		} catch (Throwable ignored){}
		checkRecorder(depth, outcome.startsWith("died") || outcome.startsWith("won"));
		boolean natural = outcome.startsWith("died") || outcome.startsWith("won");
		if (verifying){
			verifying = false;
			Object[] v = pendingVerify;
			pendingVerify = null;
			if (v != null && natural){
				@SuppressWarnings("unchecked")
				java.util.List<Long> first = (java.util.List<Long>) v[3];
				int same = 0;
				while (same < first.size() && same < digests.size() && first.get(same).equals(digests.get(same))) same++;
				verifiedRuns++;
				if (same < first.size() || same < digests.size() || !String.valueOf(v[4]).equals(outcome)){
					verifyMismatches++;
					report(BugReport.Kind.NONDETERMINISM, "replaymismatch",
							"Replaying a run with the same seed and the same decisions gave a different result.",
							"First play: " + v[4] + " (" + first.size() * 50 + "+ actions)\nReplay: " + outcome + " (" + digests.size() * 50 + "+ actions)\n"
									+ "The two were identical for the first " + same * 50 + " actions, then differed within the next 50.");
				}
			}
		} else if (natural && !replaying && startedFrom.isEmpty() && runProfile != null && runProfile.verifyEvery > 0
				&& runsSinceStart % runProfile.verifyEvery == 0){
			pendingVerify = new Object[]{heroClass.ordinal(), seed, botSeed, digests, outcome};
		}
		//a run the bot stopped is left as an unfinished save; remove it so the next run starts clean
		try { if (!outcome.startsWith("died") && !outcome.startsWith("won") && GamesInProgress.gameExists(1)) Dungeon.deleteGame(1, true); } catch (Throwable ignored){}
	}

	//the recorder must agree with what the game did
	private void checkRecorder(int depth, boolean finished){
		try {
			RecorderStatus rs = ApdRecorder.status();
			if (!rs.enabled) return;
			if (rs.errorCount > recorderErrors)
				report(BugReport.Kind.RECORDER, "recerr:" + rs.lastError.replaceAll("[0-9]", "#"), "The run recorder reported an error during this run.", rs.lastError);
			if (rs.eventsDropped > recorderDropped)
				report(BugReport.Kind.RECORDER, "recdrop", "The run recorder dropped " + (rs.eventsDropped - recorderDropped) + " event(s) during this run.", "");
			if (rs.writerProblem != null && !rs.writerProblem.isEmpty())
				report(BugReport.Kind.RECORDER, "recwriter:" + rs.writerProblem.replaceAll("[0-9]", "#"), "The run recorder's log writer failed.", rs.writerProblem);
			if (finished) RecorderCheck.later(this, recorderLogId != null ? recorderLogId : rs.logId, heroClass, maxDepth, depth, Dungeon.hero == null ? 0 : Dungeon.hero.lvl);
		} catch (Throwable t){
			botProblem("checking the recorder", t);
		}
	}

	//---- watching --------------------------------------------------------------------------------------------

	String where(){
		Hero h = Dungeon.hero;
		if (h == null || Dungeon.level == null) return "";
		return String.format(Locale.ROOT, "T%d D%d %s HP %d/%d L%d:", Statistics.apdTurn(), Dungeon.depth, SimBot.cell(h.pos), h.HP, h.HT, h.lvl);
	}

	private void gameMessage(String text){
		if (text == null) return;
		log.message(text);
		checkText(text, "game log");
	}

	void checkText(String text, String where){
		String p = TextCheck.problem(text);
		if (p == null && !SPDSettings.apdAdultHumor()) p = TextCheck.swear(text);
		if (p == null) return;
		String sig = "text:" + p.replaceAll("\\(.*", "") + ":" + text.replaceAll("[0-9]+", "#");
		if (!textSeen.add(sig)) return;
		report(BugReport.Kind.TEXT, sig, p + " in the " + where + ".", "Full text: \"" + text + "\"");
	}

	private void checkItemsText(){
		if (++itemCheckCounter % 25 != 0) return;
		Hero h = Dungeon.hero;
		for (Item i : h.belongings){
			try { checkText(i.name(), "name of an item in the backpack (" + i.getClass().getSimpleName() + ")"); } catch (Throwable t){ caught(t); }
			if (itemCheckCounter % 100 == 0){
				try { checkText(i.info(), "description of " + i.getClass().getSimpleName()); } catch (Throwable t){ caught(t); }
			}
		}
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
			if (m.pos >= 0 && m.pos < Dungeon.level.length() && Dungeon.level.heroFOV[m.pos]){
				try { checkText(m.name(), "name of " + m.getClass().getSimpleName()); } catch (Throwable t){ caught(t); }
				if (itemCheckCounter % 100 == 0){
					try { checkText(m.info(), "description of " + m.getClass().getSimpleName()); } catch (Throwable t){ caught(t); }
				}
			}
		}
	}

	//any thread: Game.reportException was called (SimGame forwards it)
	void caught(Throwable t){
		synchronized (pendingExceptions){
			if (pendingExceptions.size() < 20) pendingExceptions.add(new Object[]{t, Thread.currentThread().getName(), log.turns(), log.messages()});
		}
	}

	@SuppressWarnings("unchecked")
	private void flushExceptions(){
		List<Object[]> list;
		synchronized (pendingExceptions){
			if (pendingExceptions.isEmpty()) return;
			list = new ArrayList<>(pendingExceptions);
			pendingExceptions.clear();
		}
		for (Object[] e : list){
			Throwable t = (Throwable) e[0];
			BugReport r = base(BugReport.Kind.EXCEPTION, "exc:" + signature(t), t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage()),
					"Thread: " + e[1] + "\n" + stack(t));
			r.lastTurns = (List<String>) e[2];
			r.lastMessages = (List<String>) e[3];
			file(r);
		}
	}

	//the app is about to crash: write the report now, synchronously
	private void crash(Thread thread, Throwable t){
		BugReport r = base(BugReport.Kind.CRASH, "crash:" + signature(t), "The game crashed: " + t.getClass().getSimpleName()
				+ (t.getMessage() == null ? "" : ": " + t.getMessage()), "Thread: " + thread.getName() + "\n" + stack(t));
		file(r);
		GameSettings.put("apdsim_last", "Run " + run + " crashed: " + r.summary);
	}

	private int botProblemsThisRun;
	int crashesThisRun, saveLoadsThisRun;

	void botProblem(String during, Throwable t){
		//the simulator's own code keeps failing: give up on this run instead of spinning
		if (++botProblemsThisRun == 200 && runActive){
			report(BugReport.Kind.BOT, "bot-giveup", "The simulator gave up on a run after 200 of its own errors.", stack(t));
			endRun("abandoned (simulator errors)");
			nextRun();
			return;
		}
		report(BugReport.Kind.BOT, "bot:" + signature(t), "The simulator itself failed while " + during + ": " + t, stack(t));
	}

	void report(BugReport.Kind kind, String signature, String summary, String details){
		file(base(kind, signature, summary, details));
	}

	private BugReport base(BugReport.Kind kind, String signature, String summary, String details){
		BugReport r = new BugReport();
		r.kind = kind;
		r.signature = kind.name() + "|" + signature;
		r.summary = summary;
		r.details = details == null ? "" : details;
		r.seed = seed;
		r.seedCode = DungeonSeed.convertToCode(seed);
		r.botSeed = botSeed;
		r.heroClass = heroClass == null ? "" : heroClass.name();
		SimProfile rp = runProfile != null ? runProfile : profile();
		r.aggression = rp.aggression.title;
		r.intelligence = rp.intelligence.title;
		r.difficulty = rp.difficulty;
		r.challenges = rp.challenges;
		r.customRules = rp.customRules;
		r.apdSettings = runSettings;
		r.profileText = rp.encode();
		r.depth = Dungeon.depth;
		r.branch = Dungeon.branch;
		r.turn = Statistics.apdTurn();
		r.heroLevel = Dungeon.hero == null ? 0 : Dungeon.hero.lvl;
		r.run = run;
		r.version = Game.version + " (" + Game.versionCode + ")";
		r.time = System.currentTimeMillis();
		r.lastTurns = log.turns();
		r.lastMessages = log.messages();
		return r;
	}

	private void file(BugReport r){
		try {
			boolean fresh = reports.add(r);
			bugsThisRun++;
			if (r.kind == BugReport.Kind.CRASH || r.kind == BugReport.Kind.EXCEPTION || r.kind == BugReport.Kind.SAVELOAD) crashesThisRun++;
			if (fresh && r.kind.notifies()) Digest.newBug();
			if (r.kind.notifies() && r.kind != BugReport.Kind.SLOW) firstBugHit = true;
			if (fresh){
				newBugsTotal++;
				GameSettings.put("apdsim_bugs", newBugsTotal);
				if (r.kind.notifies()){
					platform.notifyBug(r.signature.hashCode(), "APD Simulator: " + r.kind.title,
							r.summary + " (" + r.heroClass + ", floor " + r.depth + ")");
				}
			}
		} catch (Throwable ignored){
		}
	}

	static String signature(Throwable t){
		StringBuilder b = new StringBuilder(t.getClass().getName());
		int n = 0;
		for (StackTraceElement e : t.getStackTrace()){
			if (e.getClassName().startsWith("com.shatteredpixel") || e.getClassName().startsWith("com.watabou")){
				b.append('|').append(e.getClassName().replaceAll(".*\\.", "")).append('.').append(e.getMethodName()).append(':').append(e.getLineNumber());
				if (++n >= 3) break;
			}
		}
		return b.toString();
	}

	static String stack(Throwable t){
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		String s = sw.toString();
		return s.length() > 6000 ? s.substring(0, 6000) + "\n..." : s;
	}

	//---- the actor thread ------------------------------------------------------------------------------------

	private volatile Thread actorThread;

	//Called on the actor thread by SeedActor, before any other actor of a newly shown floor acts.
	void actorThreadStarting(){
		actorThread = Thread.currentThread();
		//Turns get their own random numbers, seeded from the run, so the same seed and bot seed play out the same way
		//(the render thread's animations draw random numbers too and would otherwise shift them).
		Random.isolateThread();
		Random.pushGenerator(seed * 31 + Dungeon.depth * 1_000_003L + Dungeon.branch * 7919L + (depthEntries++) * 104_729L + botSeed);
	}

	private Thread actorThread(){ return actorThread; }

	private boolean actorBusy(){
		Thread t = actorThread;
		return t != null && t.isAlive() && t.getState() == Thread.State.RUNNABLE;
	}

	private static String topFrames(Thread t, int n){
		if (t == null) return "none";
		StringBuilder b = new StringBuilder();
		int k = 0;
		for (StackTraceElement e : t.getStackTrace()){
			if (!e.getClassName().startsWith("com.shatteredpixel")) continue;
			b.append(e.getClassName().replaceAll(".*\\.", "")).append('.').append(e.getMethodName()).append('|');
			if (++k >= n) break;
		}
		return b.toString();
	}

	private String threadDump(){
		Thread t = actorThread;
		if (t == null) return "No actor thread.";
		StringBuilder b = new StringBuilder("Actor thread state: " + t.getState() + "\n");
		for (StackTraceElement e : t.getStackTrace()) b.append("  at ").append(e).append('\n');
		return b.toString();
	}

	//SimGame: a scene was just created. A new floor's turns get their own seeded random numbers before anything acts.
	void sceneSwitched(){
		if (Game.scene() instanceof GameScene && runActive){
			StableTurnOrder.forLevel(Dungeon.level);
			TurnStepper.takeOver();
			seedSharedRandom();
			if (TurnLog.echo && Dungeon.level != null){
				long h = 7;
				for (int t : Dungeon.level.map) h = h * 31 + t;
				long heaps = 0;
				for (com.shatteredpixel.shatteredpixeldungeon.items.Heap hp : Dungeon.level.heaps.valueList()) heaps += hp.pos * 131L + hp.peek().getClass().getName().hashCode();
				long mobs = 0;
				for (Mob m : Dungeon.level.mobs) mobs += m.pos * 7919L + m.getClass().getName().hashCode();
				StringBuilder docs = new StringBuilder();
				for (com.shatteredpixel.shatteredpixeldungeon.journal.Document d : com.shatteredpixel.shatteredpixeldungeon.journal.Document.values()){
					int found = 0, read = 0, total = 0;
					for (String pg : d.pageNames()){ total++; if (d.isPageFound(pg)) found++; if (d.isPageRead(pg)) read++; }
					docs.append(d.name().charAt(0)).append(found).append('/').append(read).append('/').append(total).append(' ');
				}
				StringBuilder drops = new StringBuilder();
				for (Dungeon.LimitedDrops ld : Dungeon.LimitedDrops.values()) drops.append(ld.count).append(',');
				log.turn("floor " + Dungeon.depth + " fingerprint map=" + Long.toHexString(h) + " piles=" + Long.toHexString(heaps) + " monsters=" + Long.toHexString(mobs)
						+ " journal=" + docs + " drops=" + drops + " feeling=" + Dungeon.level.feeling);
			}
			if (TurnStepper.failed) com.shatteredpixel.shatteredpixeldungeon.actors.Actor.add(new SeedActor(this));
		}
	}

	//---- lockstep (determinism) ------------------------------------------------------------------------------

	private boolean seededShared;
	public int lockstepTimeouts;

	int stepsPerFrame(){
		if (paused || driving) return 1;
		//bug replay: fast until about 50 turns before the bug, then 1x so the lead-up can be watched
		if (replaying && replayTargetTurn >= 0) return Statistics.apdTurn() < replayTargetTurn ? SPEED_STEPS[3] : 1;
		return SPEED_STEPS[speed];
	}

	//true while a simulated run is in the dungeon
	boolean lockstep(){
		return runActive && !driving && Game.scene() instanceof GameScene;
	}

	//The floor-loading thread uses the game's shared random numbers, whose base is unseeded (respawn timers, where things land
	//on arrival). Before each loading screen can start, that shared stream is re-seeded from the run, on a helper thread
	//(this thread has its own isolated stream), so loading a floor is repeatable too.
	private int sharedSeedCounter;
	private int rotation;      //class rotation counts only normal runs (determinism replays do not shift it)
	//determinism check
	private long runDigest;
	private int digestCount;
	private java.util.List<Long> digests = new ArrayList<>();
	private boolean verifying;
	private Object[] pendingVerify;     //{class index, seed, bot seed, digests, outcome}
	public int verifiedRuns, verifyMismatches;
	CountingRandom runRandom;
	long lockFrames, freeFrames;
	private void seedSharedRandom(){
		final long s = seed * 6364136223846793005L + botSeed + (sharedSeedCounter++) * 1442695040888963407L;
		Thread t = new Thread(() -> { Random.resetGenerators(); Random.pushGenerator(s); }, "apdsim-seed");
		t.start();
		try { t.join(2000); } catch (InterruptedException ignored){}
	}

	//runs this frame's turns (on this thread) or, if that is unavailable, waits for the game's own turn thread
	void runTurns(){
		if (TurnStepper.active()){
			try { TurnStepper.step(this); }
			catch (java.lang.reflect.InvocationTargetException e){ caught(e.getCause() == null ? e : e.getCause()); }
			catch (Throwable t){ botProblem("running turns", t); }
			return;
		}
		waitForTurnThread();
	}

	//after the scene woke the turn thread, wait until it has finished (waiting for the hero or an animation)
	void waitForTurnThread(){
		Thread t = actorThread;
		if (t == null || !t.isAlive()) return;
		long until = System.nanoTime() + 250_000_000L;
		while (true){
			Thread.State st = t.getState();
			if (st == Thread.State.WAITING || st == Thread.State.TIMED_WAITING || st == Thread.State.TERMINATED) return;
			if (System.nanoTime() > until){ lockstepTimeouts++; return; }
			Thread.yield();
		}
	}

	//public for the HUD
	public boolean runActive(){ return runActive; }
}
