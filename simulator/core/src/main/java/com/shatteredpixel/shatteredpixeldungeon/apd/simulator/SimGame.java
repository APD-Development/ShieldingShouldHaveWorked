package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.utils.PlatformSupport;

//The simulator app's game: the real APD game, plus the simulator watching every frame, every reported exception and every new scene.
//APD itself never contains this class.
public class SimGame extends ShatteredPixelDungeon {

	public SimGame(PlatformSupport platform){
		super(platform);
	}

	@Override
	protected void update(){
		super.update();
		SimRuntime r = SimRuntime.get();
		if (r != null) r.frame();
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
