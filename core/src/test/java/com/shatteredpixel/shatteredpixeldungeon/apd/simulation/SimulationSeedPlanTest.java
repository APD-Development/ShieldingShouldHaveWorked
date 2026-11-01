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

public class SimulationSeedPlanTest {
    @Test public void defaultPlanPreservesExistingContiguousSeeds() {
        assertArrayEquals(new long[]{1000000,1000001,1000002}, SimulationSeedPlan.parse("",1000000,3));
    }
    @Test public void recoveryListPreservesOnlyRequestedSeedOrder() {
        assertArrayEquals(new long[]{1000042,1000043,1000058}, SimulationSeedPlan.parse("1000042, 1000043,1000058",0,3));
    }
    @Test public void missingDuplicateMalformedAndWrongCountsAreRejected() {
        for (String value : new String[]{"1,1", "1,", "x,2", "1", "1,2,3"}) {
            try { SimulationSeedPlan.parse(value,0,2); fail(value); }
            catch (IllegalArgumentException expected) { }
        }
        for (int count : new int[]{0,101}) {
            try { SimulationSeedPlan.parse("",0,count); fail(); }
            catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void contiguousOverflowDoesNotSilentlyWrapToAnotherSeed() {
        try { SimulationSeedPlan.parse("",Long.MAX_VALUE,2); fail(); }
        catch (ArithmeticException expected) { }
    }
}
