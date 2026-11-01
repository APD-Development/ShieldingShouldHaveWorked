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

package com.shatteredpixel.shatteredpixeldungeon.apd.history;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ApdHistoryGroupingTest {
 @Test public void repeatedCategoriesKeepEveryEntryWithoutRepeatingHeadings(){
  List<ApdHistory.Entry> entries=ApdHistory.entries("Bug Fixes\tV061_BUGFIX\tOne\tFirst fix.\nChanges\tV081_MISC\tTwo\tA change.\nBug Fixes\tV011_ANKH\tThree\tSecond fix.\n");
  Map<String,List<ApdHistory.Entry>> groups=ApdHistory.categories(entries);
  assertEquals(Arrays.asList("Bug Fixes","Changes"),new ArrayList<>(groups.keySet()));
  assertEquals(2,groups.get("Bug Fixes").size());assertSame(entries.get(2),groups.get("Bug Fixes").get(1));
  assertEquals(3,groups.values().stream().mapToInt(List::size).sum());
 }
 @Test public void commentaryKeepsItsOwnDescription(){
  ApdHistory.Entry entry=ApdHistory.entries("Developer Commentary\tV074_SHPX\tCommentary\tI fixed a problem.\n").get(0);
  assertEquals("I fixed a problem.",ApdHistory.categories(Arrays.asList(entry)).get("Developer Commentary").get(0).text);
 }
 @Test(expected=UnsupportedOperationException.class)public void renderingCannotRemoveHistoricalEntries(){
  ApdHistory.categories(ApdHistory.entries("Changes\tV081_MISC\tOne\tKept.\n")).get("Changes").clear();
 }
 @Test public void emptyBuildDoesNotInventAChange(){assertTrue(ApdHistory.categories(Collections.emptyList()).isEmpty());}
}
