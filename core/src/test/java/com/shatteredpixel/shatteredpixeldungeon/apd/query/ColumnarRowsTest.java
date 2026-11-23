package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ColumnarRowsTest {

	@Test
	public void everyCellAndEveryRowFieldComesBackExactly(){
		List<Row> rows = new ArrayList<>();
		String[] ids = { "run/f/move.step/0@log", "run/f/move.step/812@log", "no-number", "007@x", "run/9", null, "a@b@12", "123456789012@z", "" };
		for (int i = 0; i < 70000; i++){
			Row r = new Row(i < ids.length ? ids[i] : "r/" + i + "@l" + (i % 3), i % 1000 == 0 ? "mk" + i : null, "run" + (i % 4), 5);
			r.finished = i % 2 == 0;
			r.publicRow = i % 3 != 0;
			r.imported = i % 5 == 0;
			r.duplicate = i % 7 == 0;
			r.ruleset = i % 11 == 0 ? null : i % 13 == 0 ? Row.RULESET_MIXED : "rs" + (i % 2);
			r.telemetry = "1." + (i % 3);
			r.setNum(0, i, 1);                       //70,000 distinct: codes grow past a byte and a char
			if (i % 2 == 0) r.setText(1, "t" + (i % 300), 2); //more than 256 distinct texts
			else r.setNum(1, -5, 3);
			if (i % 9 != 0) r.setNum(2, Long.MIN_VALUE + (i % 2), 0);
			r.setText(3, i % 4 == 0 ? "" : "same", 4);
			rows.add(r);
		}
		ColumnarRows c = ColumnarRows.of(5, rows);
		assertNotNull(c);
		assertEquals(rows.size(), c.size());
		for (int i = 0; i < rows.size(); i++) LazyCatalogTest.assertSameRow("row " + i, rows.get(i), c.get(i));
	}

	@Test
	public void aRowItCannotKeepExactlyIsRefused(){
		List<Row> rows = new ArrayList<>();
		Row r = new Row("x", null, "run", 2);
		r.setRaw(0, (byte) (Row.K_TEXT), 5, "text with a number behind it");
		rows.add(r);
		assertNull(ColumnarRows.of(2, rows));
		Row wrongSize = new Row("y", null, "run", 3);
		List<Row> other = new ArrayList<>();
		other.add(wrongSize);
		assertNull(ColumnarRows.of(2, other));
	}
}
