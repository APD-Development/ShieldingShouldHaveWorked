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

package com.shatteredpixel.shatteredpixeldungeon.apd.loot;
/** Shared generation and routing authority for both gathering scroll variants. */
public final class LootScrollRules {
 private LootScrollRules(){}
 public static boolean canGenerate(int depth){return depth>=1 && depth<=19;}
 /** Main-dungeon shop, independent of spawn eligibility; final casts return to the Imp. */
 public static int destinationDepth(int sourceDepth){
  if(sourceDepth<1 || sourceDepth>26)return -1;
  if(sourceDepth<=5)return 6;
  if(sourceDepth<=10)return 11;
  if(sourceDepth<=15)return 16;
  return 20;
 }
}
