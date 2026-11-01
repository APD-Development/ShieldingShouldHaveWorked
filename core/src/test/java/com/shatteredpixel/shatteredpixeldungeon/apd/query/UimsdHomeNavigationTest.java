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
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
public class UimsdHomeNavigationTest {
 @Test public void eachOutcomeUsesNormalRunFiltering(){for(int n=0;n<3;n++){
  Query q=UimsdHomeNavigation.outcome("!outcome:"+n,false);
  assertEquals(Datasets.RUNS,q.dataset);assertSame(Scope.DEFAULT,q.scope);
  assertEquals(1,q.filters.size());assertEquals(Filter.num(FactKeys.OUTCOME,Op.EQ,n),q.filters.get(0));
  assertFalse(q.showInternal);assertTrue(q.columns.isEmpty());assertTrue(q.sorts.isEmpty());
 }}
 @Test public void mixedScopeKeepsDefaultIsolation(){Query q=UimsdHomeNavigation.outcome("!outcome:1",true);
  assertEquals(Scope.DEFAULT.withRules(Scope.Rules.ALL,""),q.scope);
 }
 @Test(expected=IllegalArgumentException.class) public void invalidDestinationCannotBecomeAnUnfilteredTable(){UimsdHomeNavigation.outcome("!outcome:99",false);}
}
