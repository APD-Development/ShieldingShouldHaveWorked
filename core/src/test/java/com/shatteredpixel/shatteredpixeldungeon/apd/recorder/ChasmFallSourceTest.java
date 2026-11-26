package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import org.junit.Test;

// Trial juror 5 (Nov 26): fall damage showed as "Unknown source" in What happened
public class ChasmFallSourceTest {
	private static CombatEvents.SourceRef describe(Object src) throws Exception {
		java.lang.reflect.Method m = CombatTracker.class.getDeclaredMethod("describe", Object.class);
		m.setAccessible(true);
		return (CombatEvents.SourceRef) m.invoke(null, src);
	}
	@Test public void aChasmFallIsNamed() throws Exception {
		assertEquals("falling into a chasm", describe(new Chasm()).label);
	}
	@Test public void anyOtherSourceKeepsItsName() throws Exception {
		assertEquals("StringBuilder", describe(new StringBuilder()).label);
		assertEquals("", describe(null).label);
	}
}
