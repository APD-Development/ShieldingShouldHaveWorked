package com.shatteredpixel.shatteredpixeldungeon.apd.age;

/** Verified age signals and the independent player humor preference.
 * These are APD policy states, not claimed Google Play API status values.
 */
public final class AgeEligibility {
	public enum State { ELIGIBLE, INELIGIBLE, UNKNOWN, UNAVAILABLE }

	private AgeEligibility(){}

	//No age provider is integrated. Missing signals must not be treated as verified adulthood.
	public static State current(){
		return State.UNAVAILABLE;
	}

	public static boolean permitsAdult(State state){
		return state != State.INELIGIBLE;
	}

	//Verified adults default to Adult; otherwise Clean. An explicit Clean choice wins.
	public static boolean defaultAdultHumor(State state){
		return state == State.ELIGIBLE;
	}

	public static boolean adultHumor(State state, boolean preferred){
		return preferred && permitsAdult(state);
	}
}
