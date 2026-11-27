package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.lang.reflect.Field;

//The game hands some work to "the next drawn frame" (for example opening a quest dialog). At high speed the simulator plays
//many game frames per drawn frame, so when that work runs would depend on how fast the device is. Inside the simulator the
//pending work is taken from libGDX's queue and run at the start of every GAME frame instead, which is the same on every
//device and on every replay.
final class FrameRunnables {
	private FrameRunnables(){}

	private static Array<Runnable> pending;
	private static boolean unavailable;

	static void install(){
		if (pending != null || unavailable || Gdx.app == null) return;
		Class<?> c = Gdx.app.getClass();
		while (c != null){
			try {
				Field f = c.getDeclaredField("runnables");
				f.setAccessible(true);
				Object v = f.get(Gdx.app);
				if (v instanceof Array){
					@SuppressWarnings("unchecked")
					Array<Runnable> a = (Array<Runnable>) v;
					pending = a;
					return;
				}
			} catch (NoSuchFieldException e){
				//look in the parent class
			} catch (Throwable t){
				break;
			}
			c = c.getSuperclass();
		}
		unavailable = true;
	}

	//start of every game frame, on the render thread
	static void drain(){
		if (pending == null) return;
		Runnable[] run;
		synchronized (pending){
			if (pending.size == 0) return;
			run = pending.toArray(Runnable.class);
			pending.clear();
		}
		for (Runnable r : run) r.run();
	}
}
