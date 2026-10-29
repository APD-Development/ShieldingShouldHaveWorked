package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.watabou.input.BackGate;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

//One press of Back is one Back, however many routes deliver it; two real presses are two.
public class BackGateTest {

	private static final long MS = 1_000_000L;

	@Before @After public void clearState(){
		BackGate.setSystemBackOwner(null);
		BackGate.reset();
	}

	@Test
	public void theSamePressByTwoRoutesCountsOnce(){
		BackGate.reset();
		assertTrue(BackGate.accept(1000 * MS));
		assertFalse("the second route delivering the same press", BackGate.accept(1000 * MS + 5 * MS));
		assertFalse(BackGate.accept(1000 * MS + 60 * MS));
	}

	@Test
	public void twoRealPressesAreTwoBacks(){
		BackGate.reset();
		assertTrue(BackGate.accept(5000 * MS));
		assertTrue("a person cannot press twice this fast, so this is a second press", BackGate.accept(5000 * MS + 200 * MS));
		assertTrue(BackGate.accept(5000 * MS + 600 * MS));
	}

	@Test
	public void theFirstPressIsAlwaysAccepted(){
		BackGate.reset();
		assertTrue(BackGate.accept(0));
	}
	@Test public void delayedLegacyCopyCannotCloseTheReturnedTitle(){
		BackGate.setSystemBackOwner(new Object());
		assertTrue(BackGate.accept(1000 * MS));
		assertFalse("Observed API36 duplicate arrived 356ms later", BackGate.acceptLegacy(1356 * MS));
		assertFalse("A longer stall must not defeat route ownership", BackGate.acceptLegacy(6000 * MS));
		assertTrue("Rejected copies do not alter the next real Back", BackGate.accept(1400 * MS));
	}

	@Test public void legacyRouteWorksWithoutModernAndroidCallback(){
		assertTrue(BackGate.acceptLegacy(1000 * MS));
		assertFalse(BackGate.acceptLegacy(1005 * MS));
		assertTrue(BackGate.acceptLegacy(1200 * MS));
	}

	@Test public void oldActivityCannotClearNewActivityOwnership(){
		Object oldActivity=new Object(),newActivity=new Object();
		BackGate.setSystemBackOwner(oldActivity);
		BackGate.setSystemBackOwner(newActivity);
		BackGate.clearSystemBackOwner(oldActivity);
		assertTrue(BackGate.systemBackOwned());
		assertFalse(BackGate.acceptLegacy(1000 * MS));
		BackGate.clearSystemBackOwner(newActivity);
		assertFalse(BackGate.systemBackOwned());
		assertTrue(BackGate.acceptLegacy(1000 * MS));
	}

	@Test public void inputTimingResetDoesNotReleaseRegisteredSystemRoute(){
		BackGate.setSystemBackOwner(new Object());
		BackGate.reset();
		assertTrue(BackGate.systemBackOwned());
		assertFalse(BackGate.acceptLegacy(0));
		assertTrue(BackGate.accept(0));
	}

	@Test public void distinctSystemPressesKeepOriginalTimingWithoutExtraLatency(){
		BackGate.setSystemBackOwner(new Object());
		assertTrue(BackGate.accept(1000 * MS));
		assertTrue(BackGate.accept(1120 * MS));
		assertTrue(BackGate.accept(1240 * MS));
	}
}
