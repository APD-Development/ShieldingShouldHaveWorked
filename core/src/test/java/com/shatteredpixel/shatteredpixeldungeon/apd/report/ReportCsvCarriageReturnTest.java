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

package com.shatteredpixel.shatteredpixeldungeon.apd.report;
import org.junit.Test;
import static org.junit.Assert.*;
public class ReportCsvCarriageReturnTest {
 @Test public void formulaLikeCarriageReturnCellsRemainQuoted() {
  assertEquals("\"'=SUM(A1)\rcontinued\"",ReportRenderer.csv("=SUM(A1)\rcontinued"));
 }
 @Test public void ordinaryCarriageReturnCellsRemainQuoted() {
  assertEquals("\"one\rtwo\"",ReportRenderer.csv("one\rtwo"));
 }
 @Test public void numericSignsAndEscapedQuotesRemainCompatible() {
  assertEquals("-42",ReportRenderer.csv("-42"));
  assertEquals("\"'=\"\"name\"\"\rcontinued\"",ReportRenderer.csv("=\"name\"\rcontinued"));
 }
}
