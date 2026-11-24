package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.*;
import org.junit.Test;

public class OneLoadTest {
	@Test
	public void secondTapWhileLoadingIsRefusedThenAllowedAfterDone(){
		OneLoad l = new OneLoad();
		assertTrue(l.start());
		assertFalse(l.start());
		l.done();
		assertTrue(l.start());
	}
}
