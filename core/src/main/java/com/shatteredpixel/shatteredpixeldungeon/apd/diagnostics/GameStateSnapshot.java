package com.shatteredpixel.shatteredpixeldungeon.apd.diagnostics;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import com.watabou.utils.SupportActionJournal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * What the game looked like when a bug report was made (Pie's Dwarf King report had no monster state).
 * Only what the player can already see: the hero, enemies in view, and a boss fight in progress.
 * Never inventory, unseen monsters, or the seed.
 */
public final class GameStateSnapshot {
    private GameStateSnapshot() {}

    /** Full text for the extended report's APD-game-state.txt. */
    public static String full() {
        StringBuilder out = new StringBuilder("APD GAME STATE (only what the player can see)\n");
        try {
            Hero h = Dungeon.hero;
            if (h == null || Dungeon.level == null) return out.append("No run in progress.\n").toString();
            out.append("Floor: ").append(Dungeon.depth).append("  Turn: ").append(Math.round(Actor.now())).append('\n');
            out.append("Challenges mask: ").append(Dungeon.challenges).append('\n');
            CustomDungeonRules rules = Dungeon.customRules();
            if (rules == null) out.append("Custom rules: none\n");
            else {
                out.append("Custom rules:");
                for (CustomDungeonRules.Slider s : CustomDungeonRules.Slider.values()) out.append(' ').append(s.name()).append('=').append(rules.get(s));
                out.append('\n');
            }
            out.append("Boss fight in progress (floor locked): ").append(h.buff(LockedFloor.class) != null ? "yes" : "no").append("\n\n");
            out.append("HERO\n").append(describe(h)).append('\n');
            out.append(" class=").append(h.heroClass).append(" subclass=").append(h.subClass).append(" level=").append(h.lvl).append("\n\n");
            out.append("ENEMIES AND ALLIES IN VIEW, PLUS ANY BOSS\n");
            int n = 0;
            for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (!shown(m)) continue;
                n++;
                out.append(describe(m)).append('\n');
                String fields = bossFields(m);
                if (!fields.isEmpty()) out.append(" boss state:").append(fields).append('\n');
            }
            if (n == 0) out.append("none\n");
        } catch (Throwable failure) {
            out.append("Snapshot stopped early: ").append(failure.getClass().getSimpleName()).append('\n');
        }
        return out.toString();
    }

    /** One line for the action history: who is fighting and how it stands. */
    public static String compact() {
        try {
            Hero h = Dungeon.hero;
            if (h == null || Dungeon.level == null) return "no_run";
            StringBuilder out = new StringBuilder();
            out.append("floor=").append(Dungeon.depth).append(" turn=").append(Math.round(Actor.now()))
                    .append(" hero_hp=").append(h.HP).append('/').append(h.HT).append(" hero_shield=").append(h.shielding())
                    .append(" pos=").append(h.pos);
            int seen = 0;
            for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (!shown(m)) continue;
                if (Char.hasProp(m, Char.Property.BOSS)) {
                    out.append(" boss=").append(m.getClass().getSimpleName()).append(" hp=").append(m.HP).append('/').append(m.HT)
                            .append(" shield=").append(m.shielding()).append(bossFields(m));
                } else seen++;
            }
            return out.append(" others_in_view=").append(seen).toString();
        } catch (Throwable failure) {
            return "snapshot_failed=" + failure.getClass().getSimpleName();
        }
    }

    public static void journal(String kind) {
        if (SupportActionJournal.enabled()) SupportActionJournal.record(kind, compact());
    }

    /** A mob the player can see, or any boss while its fight has the floor locked. */
    static boolean shown(Mob m) {
        if (m == null || Dungeon.level == null) return false;
        boolean inView = m.pos >= 0 && m.pos < Dungeon.level.length() && Dungeon.level.heroFOV != null && Dungeon.level.heroFOV[m.pos];
        boolean bossFight = Char.hasProp(m, Char.Property.BOSS) && Dungeon.hero != null && Dungeon.hero.buff(LockedFloor.class) != null;
        return inView || bossFight;
    }

    static String describe(Char c) {
        StringBuilder out = new StringBuilder(" ").append(c.getClass().getSimpleName())
                .append(" pos=").append(c.pos).append(" hp=").append(c.HP).append('/').append(c.HT)
                .append(" shield=").append(c.shielding()).append(" alignment=").append(c.alignment);
        if (c instanceof Mob) {
            Mob m = (Mob) c;
            out.append(" state=").append(m.state == null ? "none" : m.state.getClass().getSimpleName());
        }
        out.append(" buffs=[");
        boolean first = true;
        for (Buff b : c.buffs()) {
            if (!first) out.append(", ");
            first = false;
            out.append(b.getClass().getSimpleName());
            if (b instanceof ShieldBuff) out.append(" shield ").append(((ShieldBuff) b).shielding());
            else if (b.cooldown() > 0) out.append(" next ").append(Math.round(b.cooldown()));
        }
        return out.append(']').toString();
    }

    /** A boss's own counters (phase, waves made, and so on), read from its own class. */
    static String bossFields(Mob m) {
        if (!Char.hasProp(m, Char.Property.BOSS)) return "";
        StringBuilder out = new StringBuilder();
        for (Class<?> k = m.getClass(); k != null && k != Mob.class; k = k.getSuperclass()) {
            for (Field f : k.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (Modifier.isStatic(mod)) continue;
                Class<?> t = f.getType();
                if (t != int.class && t != float.class && t != boolean.class) continue;
                try {
                    f.setAccessible(true);
                    out.append(' ').append(f.getName()).append('=').append(f.get(m));
                } catch (Throwable ignored) { }
            }
        }
        return out.toString();
    }
}
