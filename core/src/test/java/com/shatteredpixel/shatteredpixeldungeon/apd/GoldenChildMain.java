package com.shatteredpixel.shatteredpixeldungeon.apd;

import java.util.List;

//Runs every golden case once in a fresh JVM and prints one "APDLINE" per level.
//
//Why a fresh JVM: a few places in SPD's level generation depend on the order of identity-hashed objects
//(HashMap/HashSet iteration), which varies with the history of the JVM. The golden file records which levels came out
//identical across many JVMs that were deliberately perturbed (argument 0 = number of throw-away hashCode() calls made
//before anything else), and only those levels are compared later.
public final class GoldenChildMain {

	public static void main( String[] args ) throws Exception {
		int perturb = Integer.parseInt(args[0]);
		for (int i = 0; i < perturb; i++){
			new Object().hashCode();
		}

		for (GoldenCases.Case c : GoldenCases.all()){
			List<LevelGenerationHarness.LevelPrint> prints =
					LevelGenerationHarness.generate(c.seed, c.challenges, c.heroClass, c.context, GoldenCases.MAX_DEPTH);
			for (LevelGenerationHarness.LevelPrint p : prints){
				System.out.println("APDLINE " + c.label() + "|" + p.depth + "|" + p.line());
			}
		}
		System.out.flush();
		System.exit(0);
	}
}
