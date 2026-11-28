package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.utils.TimeUtils;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PlatformSupport;

//The simulator app's game: the real APD game, plus the simulator watching every frame, every reported exception and every new scene.
//While a simulated run is playing, frames advance in LOCKSTEP: every frame counts as exactly 1/60 s and the turn thread finishes
//its work before the bot looks. Animation timing and thread scheduling then cannot change any random roll, so the same seed,
//class and bot seed always play out the same way (exact bug replay). APD itself never contains this class.
public class SimGame extends ShatteredPixelDungeon {

	public static final float FIXED_DT = 1f / 60f;

	public SimGame(PlatformSupport platform){
		super(platform);
	}

	//Speed is the number of fixed-length game frames per drawn frame (1x = 1, 2x = 2, 4x = 4, max = as many as fit in about
	//15 ms). The game frames are the same at every speed, so a run looks the same watched at 1x as it played at max.
	@Override
	protected void step(){
		SimRuntime r = SimRuntime.get();
		if (r == null || !r.lockstep()){
			super.step();
			return;
		}
		//paused: the game stands completely still (no frames, so nothing can shift a random roll); only taps are handled
		if (r.paused){
			applyRequestedScene();
			FrameRunnables.drain();
			inputHandler.processAllEvents();
			r.frame();
			return;
		}
		renders++;
		long t0 = System.nanoTime();
		int steps = r.stepsPerFrame();
		//headless mode gives almost the whole frame to the game (no drawing to pay for)
		long until = System.nanoTime() + (r.headless ? headlessBatchMillis * 1_000_000L : 15_000_000L);
		for (int i = 0; i < steps; i++){
			super.step();
			if (!r.lockstep() || Game.switchingScene()) break;
			if (steps > 4 && System.nanoTime() > until) break;
		}
		stepNanos += System.nanoTime() - t0;
	}

	//Headless mode: the same game frames, the same turns, the same bot; only the drawing is left out. One frame in 20 is
	//still drawn so the screen shows progress. Because nothing in the game depends on drawing, a seed gives the same result
	//in headless and in visual mode (the determinism check compares them the same way as any replay).
	private int skippedDraws;
	//for the speed line in the run summary: screen refreshes, and time spent running game frames
	static long renders, stepNanos;
	//Headless: how long the game runs between screen refreshes. 200 ms keeps the buttons responsive on a phone. On a
	//machine with no graphics card a refresh can cost most of a second, so unattended PC runs ask for much longer
	//(-Dapd.sim.batchms). The game frames are the same either way; only how often the screen is touched changes.
	public static long headlessBatchMillis = 200;
	//how many times faster the clock runs on the screens between floors in headless mode
	public static float headlessScreenSpeed = 40f;

	@Override
	protected void draw(){
		SimRuntime r = SimRuntime.get();
		if (r != null && r.headless && r.lockstep() && !r.paused && ++skippedDraws % 20 != 0) return;
		super.draw();
	}

	@Override
	protected void update(){
		SimRuntime r = SimRuntime.get();
		if (r == null || !r.lockstep()){
			if (r != null) r.freeFrames++;
			FrameRunnables.install();
			FrameRunnables.drain();
			//Headless: the screens between floors (fades, loading, the death screen) are timed by the clock, and nothing
			//in them is game play. Running their clock fast is where most of a headless run's waiting goes.
			boolean hurry = r != null && r.headless && r.playing && !r.paused && !r.driving;
			//...except while a floor is actually being built: that screen has a watchdog on the same clock ("waited more
			//than 10 seconds on levelgen"), which a fast clock would trip for no reason
			if (hurry && scene instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene
					&& "STATIC".equals(String.valueOf(SimUi.field(scene, "phase")))) hurry = false;
			if (hurry) Game.timeScale = headlessScreenSpeed;
			try {
				super.update();
			} finally {
				if (hurry) Game.timeScale = 1f;
			}
			if (r != null && r.runActive() && TurnStepper.active()) r.runTurns();
			if (r != null) r.frame();
			return;
		}
		r.lockFrames++;
		FrameRunnables.drain();
		com.watabou.utils.SupportDiagnostics.periodicMemoryEvent();
		com.watabou.utils.SupportDiagnostics.frame(scene);
		Game.elapsed = Game.timeScale * FIXED_DT;
		Game.timeTotal += Game.elapsed;
		Game.realTime = TimeUtils.millis();
		inputHandler.processAllEvents();
		Music.INSTANCE.update();
		Sample.INSTANCE.update();
		scene.update();
		Camera.updateAll();
		com.shatteredpixel.shatteredpixeldungeon.windows.GrudgeHooks.tick();
		r.runTurns();
		r.frame();
	}

	//---- running with the screen away (Android) ----------------------------------------------------------------
	//Android stops drawing when the app leaves the screen, and the game only moves when a frame is drawn. The phone
	//app then keeps the same game going from a thread of its own (see SimulatorActivity): the same frames, minus the
	//drawing. Nothing here is used on PC.

	//true when a headless run is playing and should carry on with the screen away
	public static boolean wantsBackground(){
		SimRuntime r = SimRuntime.get();
		return r != null && r.headless && r.playing && !r.paused;
	}

	//one frame without drawing; called on the thread that took over
	public void backgroundFrame(){
		try {
			step();
		} catch (Throwable t){
			logException(t);
		}
	}

	//The run's own random numbers belong to one thread (the one that draws). Whoever takes over the frames takes them
	//over too, or the run would continue on different numbers. Called on the new thread before its first frame.
	public static void adoptThread(){
		try {
			java.lang.reflect.Field f = com.watabou.utils.Random.class.getDeclaredField("isolatedThread");
			f.setAccessible(true);
			synchronized (com.watabou.utils.Random.class){
				if (f.get(null) != null) f.set(null, Thread.currentThread());
			}
		} catch (Throwable t){
			SimRuntime r = SimRuntime.get();
			if (r != null) r.caught(t);
		}
	}

	//for the notification: what the simulator is doing right now
	public static String backgroundStatus(){
		SimRuntime r = SimRuntime.get();
		if (r == null) return "";
		return r.status + (r.lastRunSummary.isEmpty() ? "" : "\nLast: " + r.lastRunSummary);
	}

	//The game closes itself in one place on purpose: a cursed wand's rare joke effect shows a fake "Abort, Retry,
	//Fail?" error and then really quits (after saving). In the simulator that must end the run, not the simulator.
	@Override
	public void finish(){
		SimRuntime r = SimRuntime.get();
		if (r != null && r.runActive()){
			r.gameClosedItself();
			return;
		}
		super.finish();
	}

	@Override
	protected void logException(Throwable tr){
		SimRuntime r = SimRuntime.get();
		if (r != null) r.caught(tr);
		super.logException(tr);
	}

	@Override
	protected void switchScene(){
		TurnStepper.sceneEnding();
		super.switchScene();
		SimRuntime r = SimRuntime.get();
		if (r != null) r.sceneSwitched();
	}
}
