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

    public static final int VERSION = 2;
    private static final int LEGACY_VERSION = 1;
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
        int previousRetention = (tier*depthShare + 12)/25;
        // The existing 25% fourth-attempt tier is the recovery ceiling for genuinely
        // new floors. It is independent of the zeroed fifth+ replay tier: otherwise
        // no amount of first-time progress can ever earn a point after four retries.
        // Floors previously reached keep the established replay/depth deduction.
        int newFloorRetention = (2500*improved + 12)/25;
        int bps = !historyKnown || priorAttempts == 0 ? 10000
                : Math.max(previousRetention, newFloorRetention);
        long earned = BigInteger.valueOf(scoreBeforeReuse)
                .multiply(BigInteger.valueOf(bps))
                .divide(BigInteger.valueOf(10000)).longValueExact();
        return new Result(priorAttempts, previousDeepest, currentDeepest, bps,
                scoreBeforeReuse, earned, historyKnown);
    }

    /** A recorded snapshot is evaluated from its frozen per-run history, never later attempts. */
    public static Result restore(Bundle data) {
        if (data == null || !data.contains(VERSION_KEY)) return null;
        int version=data.getInt(VERSION_KEY);
        if ((version != VERSION && version != LEGACY_VERSION)
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
        // A ranking snapshot must be self-consistent. Damage to an earned-score
        // field must never silently reprice a saved run or contradict its UI
        // retention percentage. Do not regenerate history or change valid scores.
        long expected = BigInteger.valueOf(before).multiply(BigInteger.valueOf(bps))
                .divide(BigInteger.valueOf(10000)).longValueExact();
        if (earned != expected) return null;
        Result recalculated=calculate(before,attempts,prior,current,known);
        if (version == LEGACY_VERSION) {
            // A v1 receipt is valid only if it matches the historical formula.
            // Its frozen attempts/depths supply the retroactive correction.
            int old=Math.max(0,Math.min(25,prior));
            int now=Math.max(0,Math.min(25,current));
            int improved=Math.max(0,now-old);
            int tier=10000-Math.min(attempts,4)*2500;
            int oldBps=!known || attempts==0 ? 10000
                    : (tier*Math.min(25,25-old+improved)+12)/25;
            if (bps != oldBps) return null;
            return recalculated;
        }
        if (recalculated.retentionBasisPoints != bps || recalculated.earned != earned) return null;
        return new Result(attempts, prior, current, bps, before, earned, known);
    }

    /** Upgrade an authentic v1 receipt in-place, without inspecting mutable run history. */
    public static boolean migrate(Bundle data) {
        if (data == null || !data.contains(VERSION_KEY)
                || data.getInt(VERSION_KEY) != LEGACY_VERSION) return false;
        Result corrected=restore(data);
        if (corrected == null) return false;
        corrected.store(data);
        return true;
    }

    public static long earnedScore(Bundle data, long normalEarnedScore) {
        Result snapshot=restore(data);
        // Legacy records have no reuse snapshot. Do not rewrite their historical earnings.
        return snapshot == null || snapshot.before != normalEarnedScore
                ? normalEarnedScore : snapshot.earned;
    }
}
