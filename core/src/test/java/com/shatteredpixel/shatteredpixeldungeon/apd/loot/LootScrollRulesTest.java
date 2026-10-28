package com.shatteredpixel.shatteredpixeldungeon.apd.loot;
import org.junit.Test;
import static org.junit.Assert.*;
public class LootScrollRulesTest {
 @Test public void bothVariantsShareTheExactFloorNineteenCutoff(){
  assertFalse(LootScrollRules.canGenerate(-1));assertFalse(LootScrollRules.canGenerate(0));
  for(int depth=1;depth<=19;depth++)assertTrue("floor "+depth,LootScrollRules.canGenerate(depth));
  for(int depth=20;depth<=1000;depth++)assertFalse("floor "+depth,LootScrollRules.canGenerate(depth));
 }
}
