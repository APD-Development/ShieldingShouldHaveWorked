package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.util.concurrent.atomic.AtomicBoolean;

/** A background history load that a repeated tap must not start twice (each one reads the whole history and opens its own window). */
public final class OneLoad {
	private final AtomicBoolean busy = new AtomicBoolean(false);

	/** True when no load is running; the caller then owns it and must call {@link #done()} when the window is shown. */
	public boolean start(){ return busy.compareAndSet(false, true); }

	public void done(){ busy.set(false); }
}
