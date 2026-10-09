package com.shatteredpixel.shatteredpixeldungeon.apd.age;

/** Adult-content eligibility, separate from the player's humor preference.
 * These are APD policy states, not claimed Google Play API status values.
 */
public final class AgeEligibility {
	public enum State { ELIGIBLE, INELIGIBLE, UNKNOWN, UNAVAILABLE }

	private AgeEligibility(){}

	//No verified age provider is integrated yet. A checkbox, saved preference,
	//sideload or missing Play response must never make this ELIGIBLE.
	public static State current(){
		return State.UNAVAILABLE;
	}

	public static boolean permitsAdult(State state){
		return state == State.ELIGIBLE;
	}

	public static boolean adultHumor(State state, boolean preferred){
		return preferred && permitsAdult(state);
	}
}
