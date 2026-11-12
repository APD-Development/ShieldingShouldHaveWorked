package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.watabou.utils.Bundle;
import java.math.BigInteger;

/**
 * Frozen, per-run score adjustment from the player's OWN earlier seed attempts.
 * An unchanged seed does not imply shared authorship. No online database is used.
 *
 * Approved replay tiers: first use 100%, second 75%, third 50%, fourth 25%,
 * fifth and later 0%. Exploration uses the specified x/(25-x) odds, bounded
 * at floor 25; unvisited floors remain eligible. Reaching beyond one's
 * previous deepest floor restores the corresponding unexplored-floor share.
 */
public final class SeedReuseScore {
    private SeedReuseScore() { }

    public static final int VERSION = 1;
    public static final String VERSION_KEY = "apd_seed_reuse_score_version";
    public static final String BASE_KEY = "apd_seed_reuse_base_score";
    public static final String EARNED_KEY = "apd_seed_reuse_earned_score";
    public static final String ATTEMPTS_KEY = "apd_seed_reuse_prior_attempts";
    public static final String PRIOR_DEPTH_KEY = "apd_seed_reuse_prior_depth";
    public static final String CURRENT_DEPTH_KEY = "apd_seed_reuse_current_depth";
    public static final String RETENTION_KEY = "apd_seed_reuse_retention_bps";
    public static final String KNOWN_KEY = "apd_seed_reuse_history_known";

    public static final class Result {
        public final int priorAttempts, deepestBefore, currentDepth, retentionBasisPoints;
        public final long before, earned, deduction;
        public final boolean known;
        private Result(int attempts, int prior, int current, int bps, long before, long earned, boolean known) {
            priorAttempts=attempts; deepestBefore=prior; currentDepth=current;
            retentionBasisPoints=bps; this.before=before; this.earned=earned;
            deduction=before-earned; this.known=known;
        }
        public void store(Bundle data) {
            if (data == null) return;
            data.put(VERSION_KEY, VERSION);
            data.put(BASE_KEY, before);
            data.put(EARNED_KEY, earned);
            data.put(ATTEMPTS_KEY, priorAttempts);
            data.put(PRIOR_DEPTH_KEY, deepestBefore);
            data.put(CURRENT_DEPTH_KEY, currentDepth);
            data.put(RETENTION_KEY, retentionBasisPoints);
            data.put(KNOWN_KEY, known);
        }
    }

    /** Previous attempts, not including the current run. Score is already custom-rule adjusted. */
    public static Result calculate(long scoreBeforeReuse, int priorAttempts, int previousDeepest,
                                   int currentDeepest, boolean historyKnown) {
        if (scoreBeforeReuse < 0) throw new IllegalArgumentException("Negative score");
        if (priorAttempts < 0) throw new IllegalArgumentException("Negative attempt count");
        int attempts = Math.min(priorAttempts, 4);
        int tier = 10000 - attempts * 2500;
        int old = Math.max(0, Math.min(25, previousDeepest));
        int current = Math.max(0, Math.min(25, currentDeepest));
        int improved = Math.max(0, current-old);
        // Odds x/(25-x) imply remaining-floor share 1/(1+x/(25-x)).
        // Limit x to 25 before division: at Yog all earlier floors are known.
        // A new deepest floor restores the portion genuinely unexplored on earlier attempts.
        int depthShare = Math.min(25, 25-old+improved);
        int bps = !historyKnown || priorAttempts == 0 ? 10000
                : (tier*depthShare + 12)/25;
        long earned = BigInteger.valueOf(scoreBeforeReuse)
                .multiply(BigInteger.valueOf(bps))
                .divide(BigInteger.valueOf(10000)).longValueExact();
        return new Result(priorAttempts, previousDeepest, currentDeepest, bps,
                scoreBeforeReuse, earned, historyKnown);
    }

    /** A recorded snapshot never gets repriced by later history or settings changes. */
    public static Result restore(Bundle data) {
        if (data == null || !data.contains(VERSION_KEY)) return null;
        if (data.getInt(VERSION_KEY) != VERSION
                || !data.contains(BASE_KEY) || !data.contains(EARNED_KEY)
                || !data.contains(ATTEMPTS_KEY) || !data.contains(PRIOR_DEPTH_KEY)
                || !data.contains(CURRENT_DEPTH_KEY) || !data.contains(RETENTION_KEY)
                || !data.contains(KNOWN_KEY)) return null;
        long before=data.getLong(BASE_KEY), earned=data.getLong(EARNED_KEY);
        int attempts=data.getInt(ATTEMPTS_KEY), prior=data.getInt(PRIOR_DEPTH_KEY),
                current=data.getInt(CURRENT_DEPTH_KEY), bps=data.getInt(RETENTION_KEY);
        boolean known=data.getBoolean(KNOWN_KEY);
        if (before<0 || earned<0 || earned>before || bps<0 || bps>10000 || attempts<0
                || (!known && before!=earned)) return null;
        return new Result(attempts, prior, current, bps, before, earned, known);
    }

    public static long earnedScore(Bundle data, long normalEarnedScore) {
        Result snapshot=restore(data);
        // Legacy records have no reuse snapshot. Do not rewrite their historical earnings.
        return snapshot == null || snapshot.before != normalEarnedScore
                ? normalEarnedScore : snapshot.earned;
    }
}
