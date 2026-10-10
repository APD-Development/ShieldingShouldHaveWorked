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
        if (type == Crab.class || type == GreatCrab.class) return 7;
        if (type == Piranha.class) return 4 + 2 * depth;
        // Exact Wraith methods use the same stored level: attackSkill=10+level, maxDamage=2+level.
        if (type == Wraith.class) return mob.attackSkill(null) - 8;
        throw new IllegalStateException("unaudited melee bound: " + type.getSimpleName());
    }
}
