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
 private Object oldNotes;private int oldChallenges;private Hero oldHero,hero;private com.shatteredpixel.shatteredpixeldungeon.levels.Level oldLevel;private MailArmor armor;
 @Before public void setup()throws Exception{HeadlessTestSupport.start();java.lang.reflect.Field notes=com.shatteredpixel.shatteredpixeldungeon.journal.Notes.class.getDeclaredField("records");notes.setAccessible(true);oldNotes=notes.get(null);com.shatteredpixel.shatteredpixeldungeon.journal.Notes.reset();oldChallenges=Dungeon.challenges;Dungeon.challenges=0;oldHero=Dungeon.hero;oldLevel=Dungeon.level;Dungeon.level=new GrassDewPickupTest.TestLevel();Dungeon.level.setSize(7,7);hero=new Hero();hero.heroClass=HeroClass.WARRIOR;Talent.initClassTalents(hero);hero.pos=24;hero.HP=hero.HT=20;Dungeon.hero=hero;armor=new MailArmor();armor.levelKnown=armor.cursedKnown=true;armor.inscribe(new Obfuscation());hero.belongings.armor=armor;}
 @After public void restore()throws Exception{Dungeon.hero=oldHero;Dungeon.level=oldLevel;Dungeon.challenges=oldChallenges;java.lang.reflect.Field notes=com.shatteredpixel.shatteredpixeldungeon.journal.Notes.class.getDeclaredField("records");notes.setAccessible(true);notes.set(null,oldNotes);}
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
 @Test public void forceDetailsUseActualStrengthTierAndCombatEndpointsWithoutRolling(){
  RingOfForce r=new RingOfForce();r.anonymize();r.levelKnown=r.cursedKnown=true;r.level(3);hero.belongings.ring=r;r.activate(hero);
  for(int strength:new int[]{8,10,18,22}){hero.STR=strength;String detail=RingExplanation.details(r);int b=r.soloBuffedBonus();assertTrue(detail,detail.contains(RingOfForce.unarmedMinimum(hero.STR(),b)+"–"+RingOfForce.unarmedMaximum(hero.STR(),b)+" damage points"));
   Random.pushGenerator(35);try{for(int j=0;j<50;j++){int actual=RingOfForce.damageRoll(hero);assertTrue(actual>=RingOfForce.unarmedMinimum(hero.STR(),b));assertTrue(actual<=RingOfForce.unarmedMaximum(hero.STR(),b));}}finally{Random.popGenerator();}
  }
 }
 @Test public void chaliceDescriptionDelayMatchesActualRegenerationAcrossLevels(){
  hero.HP=10;
  for(int level:new int[]{0,3,9}){
   com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood chalice=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood();chalice.level(level);chalice.levelKnown=chalice.cursedKnown=true;hero.belongings.artifact=chalice;chalice.activate(hero);
   Regeneration regen=Buff.affect(hero,Regeneration.class);regen.act();com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();regen.storeInBundle(state);float actualDelay=1/state.getFloat(Regeneration.PARTIAL_REGEN);
   assertEquals(10f-(1.33f+level*.667f),actualDelay,.00001f);String detail=ArtifactExplanation.details(chalice);assertTrue(detail,detail.contains(ObfuscationExplanation.number(actualDelay)+" turns per HP"));
   regen.detach();hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood.chaliceRegen.class).detach();
  }
 }
 @Test public void hornExperienceChargeNumbersMatchActualRecharge(){
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty horn=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty();horn.levelKnown=horn.cursedKnown=true;hero.belongings.artifact=horn;horn.activate(hero);
  hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty.hornRecharge.class).gainCharge(1);
  com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();horn.storeInBundle(state);assertEquals(1.25f,horn.currentCharges()+state.getFloat("partialcharge"),.0001f);
  assertTrue(ArtifactExplanation.details(horn).contains("1.25 charges per whole hero-level"));assertTrue(ArtifactExplanation.summary(horn).contains("90 hunger points"));
 }
 @Test public void unknownArtifactNumbersStayHiddenAcrossLevelAndChargeChanges(){
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood a=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood();a.levelKnown=false;String detail=ArtifactExplanation.details(a);a.level(9);assertEquals(detail,ArtifactExplanation.details(a));assertEquals("",ArtifactExplanation.summary(a));
 }
 @Test public void capeChargeCoefficientMatchesActualProcWithoutAnActivation(){
  for(int level:new int[]{0,3,6}){com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns cape=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns();cape.level(level);cape.levelKnown=cape.cursedKnown=true;cape.activate(hero);com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns.Thorns effect=hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns.Thorns.class);assertEquals(10,effect.proc(10,null,hero));assertEquals((int)(10*(.5+level*.05)),cape.currentCharges());String details=ArtifactExplanation.details(cape);assertTrue(details,details.contains(ObfuscationExplanation.number(.5f+.05f*level)+" meter points per damage point"));effect.detach();}
 }
 @Test public void slowChillAndSpeedDetailsMatchActualActionTimeAndPriority(){
  Chill chill=Buff.affect(hero,Chill.class,3);float factor=chill.speedFactor(),start=hero.cooldown();String detail=MechanicsDetails.buff(chill,chill.desc());hero.spend(1);assertEquals(1/factor,hero.cooldown()-start,.00001f);assertTrue(detail.contains("Current factor: "+ObfuscationExplanation.number(factor)));
  Slow slow=Buff.affect(hero,Slow.class,5);start=hero.cooldown();hero.spend(1);assertEquals(2,hero.cooldown()-start,.00001f);assertTrue(MechanicsDetails.buff(slow,slow.desc()).contains("Slow takes priority"));
  Speed speed=Buff.affect(hero,Speed.class,5);start=hero.cooldown();hero.spend(1);assertEquals(1,hero.cooldown()-start,.00001f);assertTrue(MechanicsDetails.buff(speed,"speed").contains("multiplicatively"));
 }
 public static class QuietMovementSprite extends com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite {@Override public void sprint(float speed){}}
 @Test public void hasteDetailsAgreeWithActualMovementFactorAndDoNotClaimFasterAttacks()throws Exception{java.lang.reflect.Field field=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");field.setAccessible(true);Object unsafe=field.get(null);hero.sprite=(QuietMovementSprite)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,QuietMovementSprite.class);float before=hero.speed();Haste haste=Buff.affect(hero,Haste.class,5);assertEquals(before*3,hero.speed(),.00001f);assertTrue(MechanicsDetails.buff(haste,haste.desc()).contains("does not multiply attack"));}
 @Test public void talentDetailsRefreshRankAndDistinguishTemporaryFromActualUpgrades(){String first=MechanicsDetails.talent(Talent.ENHANCED_RINGS,1,"");String next=MechanicsDetails.talent(Talent.ENHANCED_RINGS,3,"");assertTrue(first.contains("= 3 turns"));assertTrue(next.contains("= 9 turns"));assertTrue(next.contains("ordinary rather than effective"));assertTrue(MechanicsDetails.talent(Talent.HOLD_FAST,3,"").contains("factor of 0"));assertTrue(MechanicsDetails.talent(Talent.PROTECTIVE_SHADOWS,2,"").contains("5 shield points"));}
 @Test public void passiveGlyphPreviewUsesInactiveSentinelForAllNegativeArmorLevels(){
  ring(1);Armor.Glyph[] glyphs={new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow(),new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness(),new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Camouflage(),new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic()};
  for(Armor.Glyph glyph:glyphs){armor.inscribe(glyph);for(int level:new int[]{-1,-2,-5}){armor.level(level);assertEquals(-1,hero.glyphLevel(glyph.getClass()));String info=ArcanaInfoDisplay.armor(armor);assertTrue(info,info.contains("inactive"));}}
 }
 @Test public void flowSummaryMatchesActualMovementContributionWithRingAndAura(){armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow());armor.level(3);ring(2);Buff.affect(hero,AuraOfProtection.AuraBuff.class);Dungeon.level.water[hero.pos]=true;float actual=com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow.speedBoost(hero,hero.glyphLevel(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow.class));String text=GlyphExplanation.summary(armor,hero);assertTrue(text,text.contains(ObfuscationExplanation.number(actual)+" times normal"));}
 @Test public void antiMagicDetailsMatchActualProtectionRollBounds(){armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic());armor.level(3);ring(2);float power=Armor.Glyph.genericProcChanceMultiplier(hero);int min=Math.round(3*power),max=Math.round(7.5f*power);String text=GlyphExplanation.summary(armor,hero);assertTrue(text,text.contains(min+"–"+max+" damage-point"));Random.pushGenerator(45);try{for(int i=0;i<50;i++){int actual=com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic.drRoll(hero,3);assertTrue(actual>=min&&actual<=max);}}finally{Random.popGenerator();}}
 @Test public void glyphCalculationsDoNotRevealUnknownLevelsOrConsumeRng(){armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity());armor.levelKnown=false;armor.level(0);String text=GlyphExplanation.details(armor,hero);armor.level(9);assertEquals(text,GlyphExplanation.details(armor,hero));Random.pushGenerator(45);int next=Random.Int();Random.popGenerator();Random.pushGenerator(45);try{armor.levelKnown=true;for(int i=0;i<100;i++)GlyphExplanation.details(armor,hero);assertEquals(next,Random.Int());}finally{Random.popGenerator();}}
 @Test public void stoneAndViscosityExplainDifferentMechanicsRatherThanUniversalProcChance(){armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone());String stone=GlyphExplanation.details(armor,hero);assertTrue(stone.contains("at most 75%"));assertTrue(stone.contains("depending on that attacker"));armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity());String viscosity=GlyphExplanation.details(armor,hero);assertTrue(viscosity.contains("For q > 1"));assertTrue(viscosity.contains("not automatically cancelled"));}
 private static class ChargeObserver extends Belongings {float offered;boolean called;ChargeObserver(Hero hero){super(hero);}@Override public int charge(float amount){called=true;offered=amount;return 0;}}
 @Test public void potentialPreviewMatchesRealActivationBoundaryAndChargeStrength(){
  ChargeObserver observed=new ChargeObserver(hero);hero.belongings=observed;hero.belongings.armor=armor;armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential());RingOfArcana arcana=ring(0);
  for(int ringLevel:new int[]{0,4,12}){arcana.level(ringLevel);for(int level:new int[]{0,3,9}){armor.level(level);float chance=(level+1f)/(level+6f)*Armor.Glyph.genericProcChanceMultiplier(hero);String description=GlyphExplanation.summary(armor,hero);assertTrue(description,description.contains(ObfuscationExplanation.number(100*Math.min(1,chance))+"%"));
   for(int seed=0;seed<20;seed++){observed.called=false;observed.offered=0;Random.pushGenerator(seed);float roll=Random.Float();int next=Random.Int();Random.popGenerator();Random.pushGenerator(seed);try{armor.glyph.proc(armor,null,hero,5);assertEquals(roll<chance,observed.called);if(observed.called)assertEquals(Math.max(1,chance),observed.offered,0);assertEquals(next,Random.Int());}finally{Random.popGenerator();}}
  }}
 }
 @Test public void blockingShieldDurationDetailsMatchHoldFastDecayAndNeverDetachIt(){
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking.BlockBuff block=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking.BlockBuff();block.target=hero;block.setShield(10);HoldFast hold=Buff.affect(hero,HoldFast.class);hold.pos=hero.pos;hero.talents.get(2).put(Talent.HOLD_FAST,1);
  String first=MechanicsDetails.buff(block,block.desc());assertTrue(first,first.contains("Current decay rate: 0.5 units per tick"));block.act();assertEquals(4.5f,block.remainingDecay(),0);
  hero.talents.get(2).put(Talent.HOLD_FAST,3);block.act();assertEquals(4.5f,block.remainingDecay(),0);assertTrue(MechanicsDetails.buff(block,block.desc()).contains("Current decay rate: 0 units per tick"));
  hero.pos++;assertTrue(MechanicsDetails.buff(block,block.desc()).contains("Current decay rate: 1 units per tick"));assertSame(hold,hero.buff(HoldFast.class));
 }
}
