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

package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import org.junit.*;
import static org.junit.Assert.*;
public class OffensiveSummonOwnershipTest {
 static class Ally extends Char {Ally(){alignment=Alignment.ALLY;HP=HT=10;}}
 @Before public void init(){HeadlessTestSupport.start();}
 @Test public void creatorOwnershipSurvivesMultipleSummonGenerationsAndRealBundleRestoration(){
  Char[] creators={new Hero(),new Goo(),new Rat()};
  OffensiveDamageOrigin[] origins={OffensiveDamageOrigin.PLAYER,OffensiveDamageOrigin.BOSS,OffensiveDamageOrigin.ENEMY};
  for(int i=0;i<creators.length;i++){
   Ally child=new Ally();child.apdInheritOffensiveOrigin(creators[i]);assertEquals(origins[i],OffensiveDamageOrigin.capture(child,null));
   Bundle b=new Bundle();child.storeInBundle(b);Ally loaded=new Ally();loaded.restoreFromBundle(b);
   assertEquals(origins[i],OffensiveDamageOrigin.capture(loaded,null));
   Ally grandchild=new Ally();grandchild.apdInheritOffensiveOrigin(loaded);assertEquals(origins[i],OffensiveDamageOrigin.capture(grandchild,null));
   assertEquals(10,loaded.HP);assertEquals(10,loaded.HT);
  }
 }
 @Test public void legacyAlliesRemainUnattributedAndUnownedSavesDoNotAcquireNewFields(){
  Ally original=new Ally();Bundle b=new Bundle();original.storeInBundle(b);assertFalse(b.contains("apd_damage_creator_origin"));
  Ally loaded=new Ally();loaded.restoreFromBundle(b);assertEquals(OffensiveDamageOrigin.UNATTRIBUTED,OffensiveDamageOrigin.capture(loaded,null));
  original.apdInheritOffensiveOrigin(new Hero());original.apdInheritOffensiveOrigin(null);original.storeInBundle(b);
  assertFalse(b.contains("apd_damage_creator_origin"));
 }
 @Test public void attributionIsCapturedBeforeCreatorChangesOrDisappearsAndConsumesNoRng(){
  Random.pushGenerator(17832);long reference;try{reference=Random.Long();}finally{Random.popGenerator();}
  Random.pushGenerator(17832);try{
   Goo boss=new Goo();Ally child=new Ally();child.apdInheritOffensiveOrigin(boss);boss.alignment=Char.Alignment.ALLY;
   assertEquals(OffensiveDamageOrigin.BOSS,OffensiveDamageOrigin.capture(child,null));assertEquals(reference,Random.Long());
  }finally{Random.popGenerator();}
 }
 @Test public void unsupportedOwnershipMetadataRejectsWithoutRewritingTheEvidence(){
  Ally original=new Ally();Bundle b=new Bundle();original.storeInBundle(b);b.put("apd_damage_creator_origin","future-origin");
  assertThrows(IllegalArgumentException.class,()->new Ally().restoreFromBundle(b));
  assertEquals("future-origin",b.getString("apd_damage_creator_origin"));
 }
}
