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
package com.shatteredpixel.shatteredpixeldungeon.apd;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.GrassDewPickupTest;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.AuraOfProtection;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.watabou.utils.Random;
import org.junit.*;
import static org.junit.Assert.*;
public class MechanicsExplanationTest {
 private Hero oldHero,hero;private com.shatteredpixel.shatteredpixeldungeon.levels.Level oldLevel;private MailArmor armor;
 @Before public void setup(){HeadlessTestSupport.start();oldHero=Dungeon.hero;oldLevel=Dungeon.level;Dungeon.level=new GrassDewPickupTest.TestLevel();Dungeon.level.setSize(7,7);hero=new Hero();hero.heroClass=HeroClass.WARRIOR;Talent.initClassTalents(hero);hero.pos=24;hero.HP=hero.HT=20;Dungeon.hero=hero;armor=new MailArmor();armor.levelKnown=armor.cursedKnown=true;armor.inscribe(new Obfuscation());hero.belongings.armor=armor;}
 @After public void restore(){Dungeon.hero=oldHero;Dungeon.level=oldLevel;}
 private RingOfArcana ring(int level){RingOfArcana ring=new RingOfArcana();ring.level(level);ring.anonymize();ring.levelKnown=ring.cursedKnown=true;hero.belongings.ring=ring;ring.activate(hero);return ring;}
 @Test public void mailPlusThreeExplainsUnitsAndUsesActualObfuscationScore(){armor.level(3);RingOfArcana ring=ring(3);float expected=Obfuscation.stealthBoost(hero,hero.glyphLevel(Obfuscation.class));String text=armor.info();assertTrue(text,text.contains("2 stealth points"));assertTrue(text,text.contains(ObfuscationExplanation.number(expected)+" points"));assertTrue(text,text.contains("not percentages"));assertTrue(text,text.contains("unaware enemy"));assertFalse(text,text.contains("added stealth 2 to"));}
 @Test public void upgradesAndArcanaRefreshBothReadableValuesAndDetailedResults(){armor.level(0);RingOfArcana ring=ring(0);String first=ObfuscationExplanation.details(armor,hero);armor.level(6);ring.upgrade();String next=ObfuscationExplanation.details(armor,hero);assertNotEquals(first,next);assertTrue(next,next.contains(ObfuscationExplanation.number(Obfuscation.stealthBoost(hero,hero.glyphLevel(Obfuscation.class)))));assertTrue(next,next.contains("3 stealth points"));}
 @Test public void enhancedRingsAndProtectiveAuraMatchLiveCalculation(){armor.level(3);ring(1);Buff.affect(hero,EnhancedRings.class);Buff.affect(hero,AuraOfProtection.AuraBuff.class);float actual=Obfuscation.stealthBoost(hero,hero.glyphLevel(Obfuscation.class));assertTrue(ObfuscationExplanation.summary(armor,hero).contains(ObfuscationExplanation.number(actual)+" points"));}
 @Test public void unknownArmorDoesNotRevealItsHiddenUpgradeThroughEitherSection(){ring(3);armor.levelKnown=false;armor.level(0);String plain=ObfuscationExplanation.summary(armor,hero),detail=ObfuscationExplanation.details(armor,hero);armor.level(9);assertEquals(plain,ObfuscationExplanation.summary(armor,hero));assertEquals(detail,ObfuscationExplanation.details(armor,hero));assertTrue(plain.contains("Identify"));}
 @Test public void unknownRingDoesNotRevealIdentityOrLevelViaTheNumericalPreview(){armor.level(3);RingOfArcana ring=ring(0);ring.levelKnown=false;String first=ObfuscationExplanation.summary(armor,hero);ring.level(9);assertEquals(first,ObfuscationExplanation.summary(armor,hero));RingOfHaste other=new RingOfHaste();other.levelKnown=false;hero.belongings.ring=other;assertEquals(first,ObfuscationExplanation.summary(armor,hero));assertFalse(first.contains("3.812"));}
 @Test public void magicImmunityExplainsSuppressionRatherThanClaimingAnActiveBenefit(){armor.level(3);ring(2);Buff.affect(hero,MagicImmune.class);assertEquals(0,Obfuscation.stealthBoost(hero,hero.glyphLevel(Obfuscation.class)),0);assertTrue(ObfuscationExplanation.summary(armor,hero).contains("disables this glyph"));assertFalse(ObfuscationExplanation.summary(armor,hero).contains("2 stealth points"));}
 @Test public void negativeArmorLevelsMatchTheActualGlyphLevelSentinel(){for(int level=-5;level<=0;level++){armor.level(level);float expected=Obfuscation.stealthBoost(hero,hero.glyphLevel(Obfuscation.class));assertEquals(expected,ObfuscationExplanation.base(Math.max(-1,armor.buffedLvl())),0);if(level<0)assertTrue(ObfuscationExplanation.summary(armor,hero).contains("inactive"));}}
 @Test public void detectionDetailsDistinguishConditionsAndDoNotInventAFixedPercentage(){String details=ObfuscationExplanation.details(armor,hero);assertTrue(details.contains("standard sleeping"));assertTrue(details.contains("standard wandering"));assertTrue(details.contains("distance in tiles / 2"));assertTrue(details.contains("visibility"));assertTrue(details.contains("No fixed detection percentage"));}
 @Test public void repeatedExplanationQueriesPreserveRngTimeHealthAndInventory(){armor.level(3);RingOfArcana ring=ring(2);float time=hero.cooldown();int hp=hero.HP,level=ring.level(),quantity=armor.quantity();Random.pushGenerator(991);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(991);try{for(int i=0;i<100;i++){armor.info();MechanicsDetails.item(armor,armor.info());}assertEquals(expected,Random.Int());}finally{Random.popGenerator();}assertEquals(time,hero.cooldown(),0);assertEquals(hp,hero.HP);assertEquals(level,ring.level());assertEquals(quantity,armor.quantity());}
 @Test public void shieldingAndDurationDetailsReadLivePublicState(){Barrier barrier=new Barrier();barrier.setShield(17);assertTrue(MechanicsDetails.buff(barrier,"shielding for several turns").contains("17 shield points"));barrier.setShield(23);assertTrue(MechanicsDetails.buff(barrier,"shielding").contains("23 shield points"));}
 @Test public void barrierDecayExplanationTracksProgressAndDoesNotDetachInactiveHoldFast(){
  Barrier barrier=new Barrier();barrier.setShield(10);barrier.attachTo(hero);HoldFast hold=Buff.affect(hero,HoldFast.class);hold.pos=hero.pos+1;
  com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();barrier.storeInBundle(before);String text=MechanicsDetails.buff(barrier,barrier.desc());assertTrue(text,text.contains("0.5 per tick"));assertSame("reading does not invoke the mutating HoldFast helper",hold,hero.buff(HoldFast.class));
  com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();barrier.storeInBundle(after);assertEquals(before.toString(),after.toString());
  barrier.act();assertEquals(.5f,barrier.decayProgress(),0);assertTrue(MechanicsDetails.buff(barrier,barrier.desc()).contains("50%"));barrier.act();assertEquals(9,barrier.shielding());
 }
 @Test public void poisonDetailsReadLiveScheduledDamageWithoutChangingSavedState(){
  Poison poison=new Poison();poison.set(9);com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();poison.storeInBundle(before);String details=MechanicsDetails.buff(poison,poison.desc());assertTrue(details,details.contains(poison.apdNextInput()+" damage points"));assertTrue(details.contains("9 turns"));com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();poison.storeInBundle(after);assertEquals(before.toString(),after.toString());
 }
 @Test public void protectiveBuffDescriptionsDefineDamageUnitsAndDoNotClaimPercentReduction(){
  Barkskin bark=new Barkskin();bark.set(7,1);assertTrue(bark.desc(),bark.desc().contains("7 damage points"));assertTrue(bark.desc().contains("not a percentage"));
  ArcaneArmor arcane=new ArcaneArmor();arcane.set(9,1);assertTrue(arcane.desc(),arcane.desc().contains("9 damage points"));
 }
 @Test public void blessHexAndDazeExplainScoresInsteadOfDirectHitProbability(){
  for(Buff buff:new Buff[]{new Bless(),new Hex(),new Daze()}){String desc=buff.desc();assertTrue(desc,desc.contains("scores"));assertTrue(desc,desc.contains("chance"));}
 }
 @Test public void ordinaryAccuracyExplanationDoesNotConfuseScoresWithHitPercentages(){String text=MechanicsDetails.glossary("accuracy and evasion");assertTrue(text.contains("not hit/dodge percentages"));assertTrue(text.contains("1 − evasion / (2 × accuracy)"));assertTrue(text.contains("accuracy / (2 × evasion)"));}
 @Test public void identifiedRingDetailsFollowLiveUpgradeAndTemporaryBonuses(){
  RingOfAccuracy r=new RingOfAccuracy();r.anonymize();r.levelKnown=r.cursedKnown=true;r.level(0);hero.belongings.ring=r;r.activate(hero);
  String first=RingExplanation.details(r);assertTrue(first.contains(ObfuscationExplanation.number(RingOfAccuracy.accuracyMultiplier(hero))));
  r.upgrade();Buff.affect(hero,EnhancedRings.class);String next=RingExplanation.details(r);
  assertNotEquals(first,next);assertTrue(next,next.contains("× "+ObfuscationExplanation.number(RingOfAccuracy.accuracyMultiplier(hero))));
 }
 @Test public void unidentifiedRingDetailsDoNotRevealTypeOrHiddenLevel(){
  Ring[] rings={new RingOfAccuracy(),new RingOfArcana(),new RingOfForce(),new RingOfTenacity()};String expected=null;
  for(Ring r:rings){r.levelKnown=false;for(int level:new int[]{0,3,9}){r.level(level);String actual=RingExplanation.details(r);if(expected==null)expected=actual;assertEquals(expected,actual);}}
 }
 @Test public void tenacityDetailsUseCurrentHealthAndDoNotTreatMaximumAsCurrent(){
  RingOfTenacity r=new RingOfTenacity();r.anonymize();r.levelKnown=r.cursedKnown=true;r.level(3);hero.belongings.ring=r;r.activate(hero);hero.HP=hero.HT;
  String full=RingExplanation.details(r);hero.HP=hero.HT/2;String half=RingExplanation.details(r);
  assertNotEquals(full,half);assertTrue(half,half.contains("× "+ObfuscationExplanation.number(RingOfTenacity.damageMultiplier(hero))));assertTrue(half.contains("not your reduction on every hit"));
 }
 @Test public void magicImmuneRingDetailsDoNotClaimAnActiveBonus(){RingOfHaste r=new RingOfHaste();r.anonymize();r.levelKnown=r.cursedKnown=true;Buff.affect(hero,MagicImmune.class);assertTrue(RingExplanation.details(r).contains("not active benefits"));}
 @Test public void tinyPositiveMultipliersNeverDisplayAsZero(){assertNotEquals("0",ObfuscationExplanation.number(.00001f));assertFalse(ObfuscationExplanation.number(Float.POSITIVE_INFINITY).contains("Infinity"));}
}
