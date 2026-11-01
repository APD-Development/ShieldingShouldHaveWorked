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

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;

/** Presentation shortcuts reuse ordinary queries and their existing disclosure/scope rules. */
public final class UimsdHomeNavigation {
 private UimsdHomeNavigation() {}
 public static Query outcome(String destination, boolean allRules) {
  if (!destination.matches("!outcome:[012]")) throw new IllegalArgumentException("Unknown Home destination");
  int value = destination.charAt(destination.length()-1)-'0';
  Scope scope = allRules ? Scope.DEFAULT.withRules(Scope.Rules.ALL, "") : Scope.DEFAULT;
  return Query.of(Datasets.RUNS).withScope(scope).addFilter(Filter.num(FactKeys.OUTCOME, Op.EQ, value));
 }
}
