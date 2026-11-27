package com.shatteredpixel.shatteredpixeldungeon.apd.sim;

//The only simulator code that ships in every build: a few empty hook slots.
//The APD simulator itself (package apd.simulator) is compiled ONLY into the Developer build (-PapdTestBuild, see core/build.gradle),
//and it runs in its own Android process with its own save folder, settings and UIMSD. In every other build these slots stay null
//and nothing here does anything.
public final class SimHooks {

	private SimHooks(){}

	//Main game process, Developer build only: opens the simulator. Null everywhere else, so the title screen shows no Simulator button.
	public static volatile Runnable launcher;

	//The simulator process only: the bot that drives the game. Null in the real game.
	public interface Controller {
		//once per rendered frame, on the render thread, after the scene updated
		void frame();
		//a new actor thread is about to run turns (lets the simulator give it its own seeded random numbers)
		void actorThreadStarting();
	}

	public static volatile Controller controller;

	public static boolean inSimulator(){
		return controller != null;
	}

	public static void frame(){
		Controller c = controller;
		if (c != null) c.frame();
	}

	public static void actorThreadStarting(){
		Controller c = controller;
		if (c != null) c.actorThreadStarting();
	}
}
