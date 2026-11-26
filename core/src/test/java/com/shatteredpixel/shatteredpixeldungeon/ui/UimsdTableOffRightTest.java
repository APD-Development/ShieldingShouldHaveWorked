package com.shatteredpixel.shatteredpixeldungeon.ui;

import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.TableGeometry;
import org.junit.Test;

// Trial juror 3 (Nov 26): "4 more columns to the right" didn't change after swiping right.
public class UimsdTableOffRightTest {
	private static TableGeometry geo(float... widths) throws Exception {
		java.lang.reflect.Constructor<TableGeometry> c = TableGeometry.class.getDeclaredConstructor(float[].class, int.class);
		c.setAccessible(true);
		return c.newInstance(widths, 1);
	}
	@Test public void theCountDropsAsYouSwipeRight() throws Exception {
		TableGeometry g = geo(50, 40, 40, 40, 40, 40);
		float visible = 130;
		int start = UimsdTable.offRight(g, 0, visible);
		int partWay = UimsdTable.offRight(g, 60, visible);
		int end = UimsdTable.offRight(g, g.totalWidth - visible, visible);
		assertEquals(true, start > partWay);
		assertEquals(0, end);
	}
}
