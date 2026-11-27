package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;

//Acts once, first, on the actor thread of a newly shown floor, and gives that thread its own seeded random numbers.
//That makes the same seed and bot seed play out the same way (determinism and bug replay).
final class SeedActor extends Actor {

	private final SimRuntime sim;

	SeedActor(SimRuntime sim){
		this.sim = sim;
		actPriority = Integer.MAX_VALUE;
	}

	@Override
	protected boolean act(){
		Actor.remove(this);
		sim.actorThreadStarting();
		return true;
	}
}
