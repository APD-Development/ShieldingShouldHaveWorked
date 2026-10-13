package com.shatteredpixel.shatteredpixeldungeon.apd.healing;

/** Confirmation changes only whether an identified healing potion requires consent. */
public final class HealingConfirmation {
    private HealingConfirmation() {}
    public static boolean required(boolean identified, int threshold, int hp, int maxHp) {
        return identified && threshold >= 0 && threshold <= 100 && maxHp > 0
                && 100L * hp >= (long) threshold * maxHp;
    }
}
