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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class NormalWarriorTurnPolicyTest {

    @Test
    public void rootedStateSelectsTheSharedWaitCommand() {
        assertNull(NormalWarriorTurnPolicy.rootedWait(false, 17));
        WarriorTurnCommand command = NormalWarriorTurnPolicy.rootedWait(true, 17);
        assertNotNull(command);
        assertEquals(WarriorTurnCommand.Type.WAIT, command.type());
        assertEquals(WarriorTurnCommand.Reason.ROOTED, command.reason());
        assertEquals(17, command.target());
    }

    @Test
    public void recoveryRestPreservesTheExistingStrictLimits() {
        WarriorTurnCommand command = NormalWarriorTurnPolicy.recoveryWait(
                23, false, true, 49, 100, NormalWarriorTurnPolicy.MAX_SAFE_RESTS - 1);
        assertNotNull(command);
        assertEquals(WarriorTurnCommand.Reason.RECOVERY, command.reason());

        assertNull(NormalWarriorTurnPolicy.recoveryWait(23, false, true, 50, 100, 0));
        assertNull(NormalWarriorTurnPolicy.recoveryWait(23, true, true, 49, 100, 0));
        assertNull(NormalWarriorTurnPolicy.recoveryWait(23, false, false, 49, 100, 0));
        assertNull(NormalWarriorTurnPolicy.recoveryWait(23, false, true, 49, 100,
                NormalWarriorTurnPolicy.MAX_SAFE_RESTS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void recoveryRejectsImpossibleObservedHealth() {
        NormalWarriorTurnPolicy.recoveryWait(23, false, true, 101, 100, 0);
    }
}
