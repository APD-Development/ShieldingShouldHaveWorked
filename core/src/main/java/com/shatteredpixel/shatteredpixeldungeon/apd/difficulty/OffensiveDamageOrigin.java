/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

/** Immutable ownership snapshot for damage that outlives its attacker. Not a damage-source replacement. */
public enum OffensiveDamageOrigin {
    UNATTRIBUTED, PLAYER, ENEMY, BOSS;

    /** A summon explicitly inherits its creator's origin; friendliness alone does not prove ownership. */
    public static OffensiveDamageOrigin capture(Char attacker, OffensiveDamageOrigin creator) {
        if(creator==null){
            OffensiveDamageOrigin captured=OffensiveInvocation.captured(attacker);
            if(captured!=null)return captured;
            if(attacker!=null)creator=attacker.apdCreatorOffensiveOrigin();
        }
        if (creator != null && creator != UNATTRIBUTED) return creator;
        if (attacker instanceof Hero) return PLAYER;
        if (attacker == null || attacker.alignment != Char.Alignment.ENEMY) return UNATTRIBUTED;
        return Char.hasProp(attacker, Char.Property.BOSS) ? BOSS : ENEMY;
    }

    public int percentage() {
        switch (this) {
            case PLAYER: return CustomCombatHooks.percentage(CustomDungeonRules.Slider.PLAYER_DAMAGE);
            case ENEMY: return CustomCombatHooks.percentage(CustomDungeonRules.Slider.ENEMY_DAMAGE);
            case BOSS: return CustomCombatHooks.percentage(CustomDungeonRules.Slider.BOSS_DAMAGE);
            default: return 100;
        }
    }

    /** Additive metadata: old saves never become invented player/boss ownership. */
    public void store(Bundle bundle, String key) {
        if (this != UNATTRIBUTED) bundle.put(key, name());
        else bundle.remove(key);
    }

    public static OffensiveDamageOrigin restore(Bundle bundle, String key) {
        if (!bundle.contains(key)) return UNATTRIBUTED;
        try { return valueOf(bundle.getString(key)); }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported offensive damage origin; preserve this save for recovery", ex);
        }
    }
}
