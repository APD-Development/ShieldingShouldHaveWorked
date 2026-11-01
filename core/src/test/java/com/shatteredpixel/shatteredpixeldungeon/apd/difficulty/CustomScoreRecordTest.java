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
import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;
public class CustomScoreRecordTest {
    @Test public void neutralIdentityIsExactAndDoesNotReapplyChallenges() {
        CustomScoreRecord r=CustomScoreRecord.preserve(CustomDungeonRules.defaults(),523818);
        assertEquals(1.0,r.multiplier(),0);
        assertEquals(Integer.valueOf(523818),r.adjustedScore());
        assertEquals(523818,r.earnedScore());
    }
    @Test public void everyNonNeutralSliderPreservesEarnedScoreWithoutInventedReward() {
        for (CustomDungeonRules.Slider s : CustomDungeonRules.Slider.values()) {
            CustomScoreRecord r=CustomScoreRecord.preserve(CustomDungeonRules.defaults().with(s,s.min),523818);
            assertNull(r.multiplier()); assertNull(r.adjustedScore());
            assertEquals(523818,r.earnedScore());
            assertEquals(CustomScoreRecord.Status.UNRATED_PENDING_CALIBRATION,r.status);
            Bundle b=new Bundle(); r.store(b); CustomScoreRecord restored=CustomScoreRecord.restore(b);
            assertEquals(r.rules.identityText(),restored.rules.identityText());
            assertEquals(r.status,restored.status); assertEquals(r.originalScore,restored.originalScore);
        }
    }
    @Test public void historicalUnknownIsNotCertifiedNeutral() {
        CustomScoreRecord r=CustomScoreRecord.preserve(null,523818);
        Bundle b=new Bundle(); CustomScoreRecord.preserve(CustomDungeonRules.defaults(),1).store(b);
        r.store(b); r=CustomScoreRecord.restore(b);
        assertEquals(CustomScoreRecord.Status.HISTORICAL_SETTINGS_UNKNOWN,r.status);
        assertNull(r.rules); assertNull(r.multiplier()); assertNull(r.adjustedScore());
        assertEquals(523818,r.earnedScore());
    }
}
