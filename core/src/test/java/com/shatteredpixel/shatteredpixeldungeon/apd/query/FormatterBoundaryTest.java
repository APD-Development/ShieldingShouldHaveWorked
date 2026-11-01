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
public class FormatterBoundaryTest {
 @Test public void smallestSignedValueKeepsSingleSignAndValidDecimals(){
  assertEquals("-92233720368547758.08", CellFormatter.value(ColumnDef.num("v","V").format(ColumnDef.Format.HUNDREDTHS), Long.MIN_VALUE));
  assertEquals("-9223372036854775.8", CellFormatter.value(ColumnDef.num("v","V").format(ColumnDef.Format.THOUSANDTHS), Long.MIN_VALUE));
 }
 @Test public void boundaryAndSmallValuesKeepExistingFixedPointConventions(){
  ColumnDef h=ColumnDef.num("v","V").format(ColumnDef.Format.HUNDREDTHS);
  ColumnDef t=ColumnDef.num("v","V").format(ColumnDef.Format.THOUSANDTHS);
  assertEquals("92233720368547758.07", CellFormatter.value(h,Long.MAX_VALUE));
  assertEquals("9223372036854775.8", CellFormatter.value(t,Long.MAX_VALUE));
  assertEquals("0.00",CellFormatter.value(h,0));
  assertEquals("-0.01",CellFormatter.value(h,-1));
  assertEquals("-0.1",CellFormatter.value(t,-100));
  assertEquals("-92233720368547758.07",CellFormatter.value(h,Long.MIN_VALUE+1));
 }
}
