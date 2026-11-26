package com.shatteredpixel.shatteredpixeldungeon.apd.query.research;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DiscoveryCountsPluralTest {
    @Test public void singularAndPlural(){
        assertEquals("1 eligible finished run; 1 excluded row; 1 comparison screened. 1 entry omitted by display/group limits. No finding is fabricated when data are insufficient.",
            ResearchDocument.discoveryCounts(1,1,1,1));
        assertEquals("2 eligible finished runs; 0 excluded rows; 3 comparisons screened. 0 entries omitted by display/group limits. No finding is fabricated when data are insufficient.",
            ResearchDocument.discoveryCounts(2,0,3,0));
    }
}
