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

/**
 * Pure selectors for synchronous passive actions in the normal Warrior profile.
 * The Android and JVM adapters execute the returned command through the same boundary.
 */
public final class NormalWarriorTurnPolicy {
    public static final int MAX_SAFE_RESTS = 30;

    private NormalWarriorTurnPolicy() {}

    public static WarriorTurnCommand rootedWait(boolean rooted, int position) {
        return rooted ? WarriorTurnCommand.waitAt(WarriorTurnCommand.Reason.ROOTED, position) : null;
    }

    public static WarriorTurnCommand recoveryWait(int position, boolean enemiesVisible,
            boolean canSafelyRest, int hp, int maximumHp, int safeRestActions) {
        if (position < 0 || maximumHp <= 0 || hp < 0 || hp > maximumHp || safeRestActions < 0)
            throw new IllegalArgumentException("invalid recovery observation");
        if (!enemiesVisible && canSafelyRest && hp * 2 < maximumHp
                && safeRestActions < MAX_SAFE_RESTS) {
            return WarriorTurnCommand.waitAt(WarriorTurnCommand.Reason.RECOVERY, position);
        }
        return null;
    }
}
