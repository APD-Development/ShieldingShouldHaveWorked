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

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import org.junit.Test;
import static org.junit.Assert.*;

public class StatusTextCountsTest {
	private String mixedNotice(String units, int rows) {
		DatasetDef def = QuerySupport.def();
		def.countLabel = units;
		java.util.List<Row> data = new java.util.ArrayList<>();
		for (int i = 0; i < rows; i++) {
			data.add(QuerySupport.rowIn(def, "one-run", "item-"+i, "item", 1L, 0L,
					null, true, Row.RULESET_MIXED));
		}
		QueryResult result = QueryEngine.run(QuerySupport.dataset(def, data), Query.of("things"), "current");
		assertEquals(rows, result.stats.mixedRuleset);
		for (StatusText.Line line : StatusText.lines(result, "Items", "")) {
			if (line.action == StatusText.ACT_MIXED_RULESET) return line.text;
		}
		throw new AssertionError("Missing mixed-rules exclusion explanation");
	}

	@Test
	public void multipleItemsFromOneRunAreNotPresentedAsMultipleRuns() {
		String text = mixedNotice("Items", 5);
		assertTrue(text, text.startsWith("5 items"));
		assertFalse(text, text.contains("5 runs"));
	}

	@Test
	public void singleItemUsesSingularWording() {
		String text = mixedNotice("Items", 1);
		assertTrue(text, text.startsWith("1 item belongs"));
	}
}
