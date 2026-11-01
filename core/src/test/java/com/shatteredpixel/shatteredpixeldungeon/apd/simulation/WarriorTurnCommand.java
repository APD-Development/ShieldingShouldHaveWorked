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
 * One adapter-neutral command selected by the normal Warrior test policy.
 * Commands are test-only and execute real game actions in each adapter.
 */
public final class WarriorTurnCommand {
    public enum Type { WAIT }
    public enum Reason { ROOTED, RECOVERY }

    private final Type type;
    private final Reason reason;
    private final int target;

    private WarriorTurnCommand(Type type, Reason reason, int target) {
        if (type == null || reason == null || target < 0)
            throw new IllegalArgumentException("invalid turn command");
        this.type = type;
        this.reason = reason;
        this.target = target;
    }

    public static WarriorTurnCommand waitAt(Reason reason, int target) {
        return new WarriorTurnCommand(Type.WAIT, reason, target);
    }

    public Type type() { return type; }
    public Reason reason() { return reason; }
    public int target() { return target; }
}
