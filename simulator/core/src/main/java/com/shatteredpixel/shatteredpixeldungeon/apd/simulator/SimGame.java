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
			inputHandler.processAllEvents();
			r.frame();
			return;
		}
		int steps = r.stepsPerFrame();
		long until = System.nanoTime() + 15_000_000L;
		for (int i = 0; i < steps; i++){
			super.step();
			if (!r.lockstep() || Game.switchingScene()) break;
			if (steps > 4 && System.nanoTime() > until) break;
		}
	}

	@Override
	protected void update(){
		SimRuntime r = SimRuntime.get();
		if (r == null || !r.lockstep()){
			super.update();
			if (r != null && r.runActive() && TurnStepper.active()) r.runTurns();
			if (r != null) r.frame();
			return;
		}
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
