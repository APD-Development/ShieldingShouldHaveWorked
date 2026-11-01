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

package com.shatteredpixel.shatteredpixeldungeon.apd.simulation;

// Explicit experimental strategy, not a balance recommendation or production gameplay code.
public final class RecklessWarriorPolicy {
    public enum Action { HEAL, ATTACK, EXPLORE, STOP }
    public static final class Observation {
        public final int hp, maximumHp, nextHitUpperBound, healingPotions;
        public final boolean adjacentEnemy, finished;
        public Observation(int hp, int maximumHp, int nextHitUpperBound, int healingPotions,
                           boolean adjacentEnemy, boolean finished) {
            if (maximumHp <= 0 || hp < 0 || hp > maximumHp || nextHitUpperBound < 0 || healingPotions < 0)
                throw new IllegalArgumentException("invalid observed state");
            this.hp=hp; this.maximumHp=maximumHp; this.nextHitUpperBound=nextHitUpperBound;
            this.healingPotions=healingPotions; this.adjacentEnemy=adjacentEnemy; this.finished=finished;
        }
    }
    public Action choose(Observation state) {
        if (state.finished || state.hp == 0) return Action.STOP;
        if (state.adjacentEnemy && state.hp <= state.nextHitUpperBound && state.healingPotions > 0)
            return Action.HEAL;
        return state.adjacentEnemy ? Action.ATTACK : Action.EXPLORE;
    }
}
