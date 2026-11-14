package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.watabou.utils.Bundle;
import java.math.BigInteger;

/**
 * Frozen, per-run seed-reuse score adjustment. Uses the owner's exact equation:
 * R (%) = 100 - 75 * (D / 25)^3 - 5 * (A - 1).
 * D is the deepest floor on earlier local attempts (bounded to 0..25);
 * A is the number of earlier local attempts. First/unknown use is unpenalized.
 * Retention is bounded to 0..100%. Current floor does not appear in the
 * owner's equation and is saved only for diagnostics.
 */
public final class SeedReuseScore {
    private SeedReuseScore() { }

    public static final int VERSION = 3;
    private static final int LEGACY_V1 = 1;
    private static final int LEGACY_V2 = 2;
    private static final long DEPTH_DENOMINATOR = 15625L; // 25 cubed
    private static final long SCORE_DENOMINATOR = DEPTH_DENOMINATOR * 100;
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

    /** A is the number of earlier attempts, excluding this run. */
    public static Result calculate(long scoreBeforeReuse, int priorAttempts, int previousDeepest,
                                   int currentDeepest, boolean historyKnown) {
        if (scoreBeforeReuse < 0) throw new IllegalArgumentException("Negative score");
        if (priorAttempts < 0) throw new IllegalArgumentException("Negative attempt count");
        // Calculate the owner's cubic polynomial as a rational integer to avoid
        // floating-point drift and retain the entire 66.5008% in earned points.
        int depth = Math.max(0, Math.min(25, previousDeepest));
        long depthCubed = (long)depth * depth * depth;
        BigInteger numerator;
        if (!historyKnown || priorAttempts == 0) {
            numerator = BigInteger.valueOf(100L * DEPTH_DENOMINATOR);
        } else {
            numerator = BigInteger.valueOf(100L * DEPTH_DENOMINATOR)
                    .subtract(BigInteger.valueOf(75L * depthCubed))
                    .subtract(BigInteger.valueOf(5L * DEPTH_DENOMINATOR)
                            .multiply(BigInteger.valueOf((long)priorAttempts - 1)));
            if (numerator.signum() < 0) numerator = BigInteger.ZERO;
            BigInteger max = BigInteger.valueOf(100L * DEPTH_DENOMINATOR);
            if (numerator.compareTo(max) > 0) numerator = max;
        }
        long earned = BigInteger.valueOf(scoreBeforeReuse).multiply(numerator)
                .divide(BigInteger.valueOf(SCORE_DENOMINATOR)).longValueExact();
        int bps = numerator.multiply(BigInteger.valueOf(100))
                .add(BigInteger.valueOf(DEPTH_DENOMINATOR / 2))
                .divide(BigInteger.valueOf(DEPTH_DENOMINATOR)).intValueExact();
        return new Result(priorAttempts, previousDeepest, currentDeepest, bps,
                scoreBeforeReuse, earned, historyKnown);
    }

    private static int legacyBps(int version, int attempts, int prior, int current, boolean known) {
        if (!known || attempts == 0) return 10000;
        int tier = 10000 - Math.min(attempts, 4) * 2500;
        int old = Math.max(0, Math.min(25, prior));
        int now = Math.max(0, Math.min(25, current));
        int improved = Math.max(0, now - old);
        int depthShare = Math.min(25, 25 - old + improved);
        int previousRetention = (tier * depthShare + 12) / 25;
        if (version == LEGACY_V1) return previousRetention;
        int newFloors = Math.max(0, Math.min(26, current) - Math.max(0, Math.min(26, prior)));
        return Math.max(previousRetention, (2500 * Math.min(25, newFloors) + 12) / 25);
    }

    /**
     * Validate the stored receipt against the formula of its recorded version.
     * Valid v1/v2 receipts are re-evaluated using the owner's formula from the
     * immutable run-local A/D values. Missing/corrupted receipts are untouched.
     */
    public static Result restore(Bundle data) {
        if (data == null || !data.contains(VERSION_KEY)) return null;
        int version = data.getInt(VERSION_KEY);
        if ((version != LEGACY_V1 && version != LEGACY_V2 && version != VERSION)
                || !data.contains(BASE_KEY) || !data.contains(EARNED_KEY)
                || !data.contains(ATTEMPTS_KEY) || !data.contains(PRIOR_DEPTH_KEY)
                || !data.contains(CURRENT_DEPTH_KEY) || !data.contains(RETENTION_KEY)
                || !data.contains(KNOWN_KEY)) return null;
        long before = data.getLong(BASE_KEY), earned = data.getLong(EARNED_KEY);
        int attempts = data.getInt(ATTEMPTS_KEY), prior = data.getInt(PRIOR_DEPTH_KEY),
                current = data.getInt(CURRENT_DEPTH_KEY), bps = data.getInt(RETENTION_KEY);
        boolean known = data.getBoolean(KNOWN_KEY);
        if (before < 0 || earned < 0 || earned > before || attempts < 0
                || bps < 0 || bps > 10000 || (!known && earned != before)) return null;

        Result corrected = calculate(before, attempts, prior, current, known);
        if (version == VERSION) {
            if (bps != corrected.retentionBasisPoints || earned != corrected.earned) return null;
            return corrected;
        }
        if (bps != legacyBps(version, attempts, prior, current, known)) return null;
        long legacyEarned = BigInteger.valueOf(before).multiply(BigInteger.valueOf(bps))
                .divide(BigInteger.valueOf(10000)).longValueExact();
        if (earned != legacyEarned) return null;
        return corrected;
    }

    /** Persist one-time v1/v2 historical score correction without reading later history. */
    public static boolean migrate(Bundle data) {
        if (data == null || !data.contains(VERSION_KEY)) return false;
        int version = data.getInt(VERSION_KEY);
        if (version != LEGACY_V1 && version != LEGACY_V2) return false;
        Result corrected = restore(data);
        if (corrected == null) return false;
        corrected.store(data);
        return true;
    }

    public static long earnedScore(Bundle data, long normalEarnedScore) {
        Result snapshot = restore(data);
        return snapshot == null || snapshot.before != normalEarnedScore
                ? normalEarnedScore : snapshot.earned;
    }
}
