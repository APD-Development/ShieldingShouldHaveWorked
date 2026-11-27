package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Game;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

//Runs the game's turns on the render thread, in the same order the game's own turn thread would (earliest time first,
//higher priority first on ties, a character waits while its sprite is still moving), but only between frames. With nothing
//running on a second thread, a seed plus the bot's seed replays exactly. Used only inside the simulator app during a run;
//the game's own turn thread is given a placeholder that does nothing.
final class TurnStepper {
	private TurnStepper(){}

	private static Field fAll, fCurrent, fNow, fTime, fPriority, fActorThread;
	private static final Map<Class<?>, Method> actMethods = new HashMap<>();
	private static Actor waitingForSprite;
	private static Thread placeholder;
	static boolean failed;

	private static void init() throws Exception {
		if (fAll != null) return;
		fAll = Actor.class.getDeclaredField("all"); fAll.setAccessible(true);
		fCurrent = Actor.class.getDeclaredField("current"); fCurrent.setAccessible(true);
		fNow = Actor.class.getDeclaredField("now"); fNow.setAccessible(true);
		fTime = Actor.class.getDeclaredField("time"); fTime.setAccessible(true);
		fPriority = Actor.class.getDeclaredField("actPriority"); fPriority.setAccessible(true);
		fActorThread = GameScene.class.getDeclaredField("actorThread"); fActorThread.setAccessible(true);
	}

	//called when a GameScene is created during a run: the game must not start its own turn thread
	static void takeOver(){
		try {
			init();
			if (placeholder == null || !placeholder.isAlive()){
				placeholder = new Thread("apdsim-turn-placeholder"){
					@Override public void run(){
						//answers the game's "is the turn thread done?" waits quickly; never runs a turn
						while (true){
							synchronized (this){
								notifyAll();
								try { wait(5); } catch (InterruptedException ignored){}
							}
						}
					}
				};
				placeholder.setDaemon(true);
				placeholder.start();
			}
			fActorThread.set(null, placeholder);
			Actor.keepActorThreadAlive = true;
			waitingForSprite = null;
		} catch (Throwable t){
			failed = true;
		}
	}

	static boolean active(){
		try { return !failed && fActorThread != null && fActorThread.get(null) == placeholder && placeholder != null; }
		catch (Throwable t){ return false; }
	}

	private static Method act(Actor a) throws NoSuchMethodException {
		Method m = actMethods.get(a.getClass());
		if (m != null) return m;
		Class<?> c = a.getClass();
		while (c != null){
			try { m = c.getDeclaredMethod("act"); break; } catch (NoSuchMethodException e){ c = c.getSuperclass(); }
		}
		if (m == null) throw new NoSuchMethodException("act");
		m.setAccessible(true);
		actMethods.put(a.getClass(), m);
		return m;
	}

	private static boolean moving(Actor a){
		return a instanceof Char && ((Char) a).sprite != null && ((Char) a).sprite.isMoving;
	}

	//one frame's worth of turns: until the hero waits for input, a sprite must finish moving, or the scene changes
	static void step(SimRuntime sim) throws Exception {
		init();
		for (int guard = 0; guard < 20000; guard++){
			if (Game.switchingScene()) return;
			Actor acting;
			if (waitingForSprite != null){
				acting = waitingForSprite;
				if (moving(acting)) return;
				waitingForSprite = null;
			} else {
				if (Actor.processing()) return; //the hero is waiting for an action
				acting = null;
				float earliest = Float.MAX_VALUE;
				int bestPriority = Integer.MIN_VALUE;
				synchronized (Actor.class){
					@SuppressWarnings("unchecked")
					HashSet<Actor> all = (HashSet<Actor>) fAll.get(null);
					for (Actor a : all){
						float t = fTime.getFloat(a);
						int p = fPriority.getInt(a);
						if (t < earliest || (t == earliest && (acting == null || p > bestPriority))){
							earliest = t;
							bestPriority = p;
							acting = a;
						}
					}
				}
				if (acting == null) return;
				fCurrent.set(null, acting);
				fNow.setFloat(null, earliest);
				if (moving(acting)){
					waitingForSprite = acting;
					return;
				}
			}
			boolean doNext = (Boolean) act(acting).invoke(acting);
			if (doNext && (Dungeon.hero == null || !Dungeon.hero.isAlive())){
				fCurrent.set(null, null);
				return;
			}
			if (!doNext) return; //waiting (normally the hero, for the bot); current stays set, exactly as in the game
			fCurrent.set(null, null);
		}
		sim.report(BugReport.Kind.STUCK, "turnloop", "More than 20,000 actor turns in one frame without the hero getting a turn (an endless loop of turns).", "");
	}
}
