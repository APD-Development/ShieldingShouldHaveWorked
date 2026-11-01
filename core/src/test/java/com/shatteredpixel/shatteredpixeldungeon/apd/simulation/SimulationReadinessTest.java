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
import static org.junit.Assert.*;

public class SimulationReadinessTest {
    @Test public void pendingCommandMustNotReuseThePreviousReadyFlag() {
        assertFalse(SimulationReadiness.actionReleased(23, 23, 2, 2, true, true, true));
        assertFalse(SimulationReadiness.actionReleased(23, 23, 2, 2, true, false, false));
    }
    @Test public void freePickupOrInterruptedMoveCanReleaseInputWithoutTurnAdvance() {
        assertTrue(SimulationReadiness.actionReleased(23, 23, 2, 2, true, true, false));
    }
    @Test public void paidActionDeathAndFloorTransitionReleaseTheInitialWait() {
        assertTrue(SimulationReadiness.actionReleased(23, 24, 2, 2, true, false, true));
        assertTrue(SimulationReadiness.actionReleased(23, 23, 2, 2, false, false, true));
        assertTrue(SimulationReadiness.actionReleased(23, 0, 2, 3, true, false, true));
    }
}
