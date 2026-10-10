package com.shatteredpixel.shatteredpixeldungeon.apd.simulation;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;

// Raw melee damage only: not total damage, status effects or proof a heal prevents death.
public final class AuditedSewersMeleeBounds {
    private AuditedSewersMeleeBounds() {}
    public static int maximum(Mob mob, int depth) {
        if (mob == null) return 0;
        Class<?> type = mob.getClass();
        if (type == Rat.class || type == Albino.class || type == FetidRat.class
                || type == Snake.class || type == Swarm.class) return 4;
        if (type == Gnoll.class || type == GnollTrickster.class) return 6;
        if (type == GnollExile.class) return 10;
        if (type == Crab.class || type == GreatCrab.class || type == HermitCrab.class) return 7;
        if (type == Slime.class || type == CausticSlime.class) return 5;
        if (type == Thief.class || type == Bandit.class) return 10;
        if (type == Piranha.class || type == PhantomPiranha.class) return 4 + 2 * depth;
        // Exact Wraith methods use the same stored level: attackSkill=10+level, maxDamage=2+level.
        if (type == Wraith.class || type == TormentedSpirit.class) return mob.attackSkill(null) - 8;
        // These exact variants use attackSkill(null)=6+storedLevel and maxDamage=2+2*storedLevel.
        // CrystalMimic delegates neutral damage to the normal enemy roll, with the same maximum.
        if (type == Mimic.class || type == GoldenMimic.class || type == CrystalMimic.class)
            return 2 + 2 * (mob.attackSkill(null) - 6);
        throw new IllegalStateException("unaudited melee bound: " + type.getSimpleName());
    }
}
