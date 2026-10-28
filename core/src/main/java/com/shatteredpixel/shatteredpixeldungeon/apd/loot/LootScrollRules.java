package com.shatteredpixel.shatteredpixeldungeon.apd.loot;
/** Shared spawn policy for both gathering scroll variants; reading is separate. */
public final class LootScrollRules {
 private LootScrollRules(){}
 public static boolean canGenerate(int depth){return depth>=1 && depth<=19;}
}
