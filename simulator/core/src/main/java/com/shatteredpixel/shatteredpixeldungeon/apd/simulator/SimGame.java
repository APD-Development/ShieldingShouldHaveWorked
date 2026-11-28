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
		int steps = r.stepsPerFrame();
		//headless mode gives almost the whole frame to the game (no drawing to pay for)
		long until = System.nanoTime() + (r.headless ? 200_000_000L : 15_000_000L);
		for (int i = 0; i < steps; i++){
			super.step();
			if (!r.lockstep() || Game.switchingScene()) break;
			if (steps > 4 && System.nanoTime() > until) break;
		}
	}

	//Headless mode: the same game frames, the same turns, the same bot; only the drawing is left out. One frame in 20 is
	//still drawn so the screen shows progress. Because nothing in the game depends on drawing, a seed gives the same result
	//in headless and in visual mode (the determinism check compares them the same way as any replay).
	private int skippedDraws;

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
			super.update();
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
		super.switchScene();
		SimRuntime r = SimRuntime.get();
		if (r != null) r.sceneSwitched();
	}
}
