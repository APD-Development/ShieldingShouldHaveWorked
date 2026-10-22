package com.shatteredpixel.shatteredpixeldungeon.apd.healing;

/** Confirmation changes only whether an identified healing potion requires consent. */
public final class HealingConfirmation {
    private HealingConfirmation() {}
    // Category hotbar normally offers a second action menu. With confirmation Off,
    // selecting a known healing potion is itself the one-tap DRINK action.
    public static boolean categoryDirectDrink(boolean identified, int threshold) {
        return identified && threshold == -1;
    }

    public static boolean required(boolean identified, int threshold, int hp, int maxHp) {
        return identified && threshold >= 0 && threshold <= 100 && maxHp > 0
                && 100L * hp >= (long) threshold * maxHp;
    }
}
