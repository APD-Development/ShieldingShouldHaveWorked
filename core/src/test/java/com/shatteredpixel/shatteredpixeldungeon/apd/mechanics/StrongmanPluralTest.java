package com.shatteredpixel.shatteredpixeldungeon.apd.mechanics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StrongmanPluralTest {
    @Test
    public void oneAddedPointIsSingular() {
        assertEquals("1 strength point", TalentExplanation.strongmanAdded(10, .13f));
        assertEquals("0 strength points", TalentExplanation.strongmanAdded(10, .08f));
        assertEquals("2 strength points", TalentExplanation.strongmanAdded(12, .18f));
    }
}
