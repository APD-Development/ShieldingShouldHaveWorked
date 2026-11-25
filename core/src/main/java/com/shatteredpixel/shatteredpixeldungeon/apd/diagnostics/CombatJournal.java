package com.shatteredpixel.shatteredpixeldungeon.apd.diagnostics;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.SupportActionJournal;

/**
 * Fight events for the extended bug report: damage to the hero and to bosses (including how much
 * shield it took), deaths the player could see, and boss phase changes with the fight's state.
 * Nothing is written unless the player turned the extended report on.
 */
public final class CombatJournal {
    private CombatJournal() {}

    public static void damage(Char target, Object src, int requested, int hpBefore, int shieldBefore, int absorbed, int hpLost) {
        try {
            if (!SupportActionJournal.enabled() || target == null) return;
            boolean boss = Char.hasProp(target, Char.Property.BOSS);
            if (!(target instanceof Hero) && !boss) return;
            if (!(target instanceof Hero) && !(target instanceof Mob && GameStateSnapshot.shown((Mob) target))) return;
            SupportActionJournal.record("combat.damage", "target=" + name(target) + " src=" + name(src)
                    + " requested=" + requested + " hp=" + hpBefore + "->" + target.HP
                    + " shield=" + shieldBefore + "->" + target.shielding() + " absorbed=" + absorbed + " hp_lost=" + hpLost);
        } catch (Throwable ignored) { }
    }

    public static void died(Char ch, Object src) {
        try {
            if (!SupportActionJournal.enabled() || ch == null) return;
            if (!(ch instanceof Hero) && !(ch instanceof Mob && GameStateSnapshot.shown((Mob) ch))) return;
            SupportActionJournal.record("combat.died", "who=" + name(ch) + " pos=" + ch.pos + " cause=" + name(src));
        } catch (Throwable ignored) { }
    }

    public static void bossPhase(String boss, int phase, String state) {
        try {
            if (!SupportActionJournal.enabled()) return;
            SupportActionJournal.record("boss.phase", "boss=" + boss + " phase=" + phase + " state=" + state + " " + GameStateSnapshot.compact());
        } catch (Throwable ignored) { }
    }

    static String name(Object o) {
        if (o == null) return "none";
        Class<?> c = o instanceof Class ? (Class<?>) o : o.getClass();
        String n = c.getSimpleName();
        if (n.isEmpty()) n = c.getName().substring(c.getName().lastIndexOf('.') + 1);
        return n;
    }
}
