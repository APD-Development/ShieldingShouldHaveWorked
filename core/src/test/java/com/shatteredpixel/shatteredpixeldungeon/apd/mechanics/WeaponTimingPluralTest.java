package com.shatteredpixel.shatteredpixeldungeon.apd.mechanics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WeaponTimingPluralTest {
    @Test
    public void oneTurnIsSingularOthersPlural() {
        assertEquals("1 turn", WeaponExplanation.turns(1f));
        assertEquals("2 turns", WeaponExplanation.turns(2f));
        assertEquals(ObfuscationExplanation.number(2f / 3f) + " turns", WeaponExplanation.turns(2f / 3f));
        assertEquals(ObfuscationExplanation.number(1.5f) + " turns", WeaponExplanation.turns(1.5f));
    }
}
