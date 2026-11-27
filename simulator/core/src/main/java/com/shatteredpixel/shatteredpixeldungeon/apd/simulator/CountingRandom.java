package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import java.lang.reflect.Field;
import java.util.ArrayDeque;

//The run's seeded random generator, which also counts how many numbers were drawn. Two replays of the same run must show the
//same count at every action; the first action where they differ points at whatever drew numbers out of step.
final class CountingRandom extends java.util.Random {
	long draws;
	//trace mode only: remembers who drew the most recent numbers
	static boolean recordCallers;
	final ArrayDeque<String> lastCallers = new ArrayDeque<>();

	CountingRandom(long seed){ super(seed); }

	//Visual code (particles, sprites, UI, windows, scenes) draws from its own generator, so an effect that shows a frame earlier
	//or later can never shift a gameplay roll. Who is asking is read from the call stack: the first caller outside the
	//random-number classes decides.
	private final java.util.Random visual = new java.util.Random();
	long visualDraws;

	private static boolean isVisual(String c){
		return c.startsWith("com.watabou.noosa") || c.startsWith("com.watabou.glwrap") || c.startsWith("com.watabou.gltextures")
				|| c.contains(".shatteredpixeldungeon.effects.") || c.contains(".shatteredpixeldungeon.sprites.")
				|| c.contains(".shatteredpixeldungeon.ui.") || c.contains(".shatteredpixeldungeon.tiles.")
				|| c.contains(".shatteredpixeldungeon.windows.") || c.contains(".shatteredpixeldungeon.scenes.");
	}

	@Override protected int next(int bits){
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String first = null;
		boolean particle = false;
		int seen = 0;
		for (StackTraceElement e : st){
			String c = e.getClassName();
			if (c.startsWith("java.") || c.startsWith("dalvik.") || c.startsWith("libcore.") || c.endsWith("CountingRandom") || c.equals("com.watabou.utils.Random")) continue;
			if (first == null) first = c;
			//floor decorations keep their particle classes inside the levels package: anything run by a particle emitter is visual
			if (c.startsWith("com.watabou.noosa.particles.") || c.contains("Particle")) particle = true;
			if (++seen >= 4) break;
		}
		if (first != null && (isVisual(first) || particle)){
			visualDraws++;
			return visual.nextInt() >>> (32 - bits);
		}
		draws++;
		if (recordCallers){
			StringBuilder b = new StringBuilder();
			int n = 0;
			for (StackTraceElement e : st){
				String c = e.getClassName();
				if (c.startsWith("java.") || c.endsWith("CountingRandom") || c.endsWith("utils.Random")) continue;
				b.append(c.substring(c.lastIndexOf('.') + 1)).append('.').append(e.getMethodName()).append(':').append(e.getLineNumber()).append(" < ");
				if (++n >= 4) break;
			}
			lastCallers.addLast(b.toString());
			while (lastCallers.size() > 12) lastCallers.removeFirst();
		}
		return super.next(bits);
	}

	//puts a counting generator on top of the calling thread's isolated stack (call right after Random.isolateThread())
	@SuppressWarnings("unchecked")
	static CountingRandom pushOnIsolated(long seed){
		try {
			Field f = com.watabou.utils.Random.class.getDeclaredField("isolatedGenerators");
			f.setAccessible(true);
			ArrayDeque<java.util.Random> stack = (ArrayDeque<java.util.Random>) f.get(null);
			CountingRandom r = new CountingRandom(seed);
			stack.push(r);
			return r;
		} catch (Throwable t){
			com.watabou.utils.Random.pushGenerator(seed);
			return null;
		}
	}
}
