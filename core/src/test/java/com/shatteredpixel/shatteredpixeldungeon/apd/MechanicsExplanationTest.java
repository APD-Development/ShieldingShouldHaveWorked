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
 private com.watabou.utils.Bundle oldRules;private Object oldNotes;private int oldChallenges;private Hero oldHero,hero;private com.shatteredpixel.shatteredpixeldungeon.levels.Level oldLevel;private MailArmor armor;
 @Before public void setup()throws Exception{HeadlessTestSupport.start();java.lang.reflect.Field notes=com.shatteredpixel.shatteredpixeldungeon.journal.Notes.class.getDeclaredField("records");notes.setAccessible(true);oldNotes=notes.get(null);com.shatteredpixel.shatteredpixeldungeon.journal.Notes.reset();oldChallenges=Dungeon.challenges;Dungeon.challenges=0;oldRules=new com.watabou.utils.Bundle();Dungeon.apdStoreCustomRules(oldRules);Dungeon.apdRestoreCustomRules(new com.watabou.utils.Bundle());oldHero=Dungeon.hero;oldLevel=Dungeon.level;Dungeon.level=new GrassDewPickupTest.TestLevel();Dungeon.level.setSize(7,7);hero=new Hero();hero.heroClass=HeroClass.WARRIOR;Talent.initClassTalents(hero);hero.pos=24;hero.HP=hero.HT=20;Dungeon.hero=hero;armor=new MailArmor();armor.levelKnown=armor.cursedKnown=true;armor.inscribe(new Obfuscation());hero.belongings.armor=armor;}
 @After public void restore()throws Exception{Dungeon.hero=oldHero;Dungeon.level=oldLevel;Dungeon.challenges=oldChallenges;Dungeon.apdRestoreCustomRules(oldRules);java.lang.reflect.Field notes=com.shatteredpixel.shatteredpixeldungeon.journal.Notes.class.getDeclaredField("records");notes.setAccessible(true);notes.set(null,oldNotes);}
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
 public static class QuietMovementSprite extends com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite {@Override public void add(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.State state){} @Override public void remove(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.State state){} @Override public void sprint(float speed){} @Override public void showStatus(int color,String text,Object...args){} @Override public void showStatusWithIcon(int color,String text,int icon,Object...args){}}
 private QuietMovementSprite quietSprite()throws Exception{java.lang.reflect.Field field=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");field.setAccessible(true);Object unsafe=field.get(null);return (QuietMovementSprite)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,QuietMovementSprite.class);}
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
 @Test public void enchantmentPowerMatchesAllActiveContributionsWithoutConsumingThem(){
  ring(2);
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade.RunicSlashTracker slash=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade.RunicSlashTracker.class);slash.boost=4;
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike.DirectedPowerTracker directed=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike.DirectedPowerTracker.class);directed.enchBoost=2;
  Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Smite.SmiteTracker.class);
  float expected=com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment.genericProcChanceMultiplierReadOnly(hero);assertEquals(expected,EnchantmentExplanation.knownPower(hero),0);
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.levelKnown=true;sword.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling());
  Random.pushGenerator(773);int next=Random.Int();Random.popGenerator();Random.pushGenerator(773);try{for(int i=0;i<100;i++)EnchantmentExplanation.describe(sword,hero,true);assertEquals(next,Random.Int());}finally{Random.popGenerator();}
  assertSame(slash,hero.buff(slash.getClass()));assertEquals(4,slash.boost,0);assertSame(directed,hero.buff(directed.getClass()));assertEquals(2,directed.enchBoost,0);
 }
 @Test public void vampiricDescriptionUsesActualCurrentHpChanceAndHealingAmount()throws Exception{
  hero.sprite=quietSprite();ring(0);com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.levelKnown=true;sword.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vampiric());com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat target=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
  for(int seed=0;seed<30;seed++){hero.HP=10;float chance=(.05f+.25f*.5f)*EnchantmentExplanation.knownPower(hero);String description=EnchantmentExplanation.describe(sword,hero,false);assertTrue(description,description.contains(ObfuscationExplanation.number(100*chance)+"%"));Random.pushGenerator(seed);boolean trigger=Random.Float()<chance;Random.popGenerator();Random.pushGenerator(seed);try{sword.enchantment.proc(sword,hero,target,10);}finally{Random.popGenerator();}assertEquals(trigger?15:10,hero.HP);}
  hero.HP=10;String before=EnchantmentExplanation.describe(sword,hero,false);sword.level(9);assertEquals(before,EnchantmentExplanation.describe(sword,hero,false));
 }
 @Test public void spiritPowerPreviewKeepsCachedRingUnchangedEvenWhenItsTypeIsKnown(){
  hero.heroClass=HeroClass.CLERIC;hero.armorAbility=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.Trinity();Talent.initClassTalents(hero);Talent.initArmorTalents(hero);hero.talents.get(3).put(Talent.SPIRIT_FORM,2);RingOfArcana spirit=new RingOfArcana();spirit.level(90);Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff.class).setEffect(spirit);
  assertTrue(Float.isNaN(EnchantmentExplanation.knownPower(hero)));assertEquals(90,spirit.level());spirit.anonymize();spirit.levelKnown=spirit.cursedKnown=true;assertEquals((float)Math.pow(1.175f,3),EnchantmentExplanation.knownPower(hero),0);assertEquals(90,spirit.level());
 }
 @Test public void spiritBowProjectingDescribesProjectileRadiusInsteadOfMeleeReach(){com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow bow=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow();bow.levelKnown=true;bow.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting());String description=EnchantmentExplanation.describe(bow,hero,false);assertTrue(description,description.contains("wall-piercing target radius"));assertFalse(description,description.contains("Adds 1 melee reach"));}
 @Test public void runicSlashReachComparisonRetainsPotentialAbilityPreview(){hero.heroClass=HeroClass.DUELIST;com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade blade=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade();blade.levelKnown=true;blade.level(6);blade.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting());ring(2);float power=EnchantmentExplanation.knownPower(hero);int expected=blade.RCH+com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting.additionalReach(power+blade.runicSlashEnchantBoost(),blade.RCH);String text=EnchantmentExplanation.describe(blade,hero,false);assertTrue(text,text.contains("total base reach would be "+expected+" tiles"));}
 @Test public void everyBuiltInOrdinaryEnchantmentHasReadableAndDetailedExplanations()throws Exception{
  String[] names={"Blazing","Blocking","Blooming","Chilling","Corrupting","Crystal","Eldritch","Elastic","Grim","Kinetic","Lucky","Projecting","Shocking","Unstable","Vampiric","Venomous","Vorpal"};com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.levelKnown=true;
  for(String name:names){sword.enchant((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments."+name).getConstructor().newInstance());String plain=EnchantmentExplanation.describe(sword,hero,false),details=EnchantmentExplanation.describe(sword,hero,true);assertFalse(name,plain.isEmpty());assertTrue(name,details.contains(name+" calculations"));assertFalse(name,plain.contains("NaN"));assertFalse(name,details.contains("Infinity"));}
 }
 @Test public void knownCurseDescriptionsCoverAllBuiltInsWithoutRevealingUnknownCurseIdentity()throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.levelKnown=false;
  for(String name:new String[]{"Annoying","Dazzling","Displacing","Explosive","Friendly","Polarized","Pressurized","Sacrificial","Wayward","Wondrous"}){sword.enchant((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses."+name).getConstructor().newInstance());sword.cursedKnown=false;assertEquals(name,"",CurseExplanation.weapon(sword,hero,true));sword.cursedKnown=true;sword.level(0);String first=CurseExplanation.weapon(sword,hero,true);sword.level(9);assertFalse(name,first.isEmpty());assertEquals(name,first,CurseExplanation.weapon(sword,hero,true));}
  armor.levelKnown=false;
  for(String name:new String[]{"AntiEntropy","Bulk","Corrosion","Displacement","Metabolism","Multiplicity","Overgrowth","Stench"}){armor.inscribe((Armor.Glyph)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.armor.curses."+name).getConstructor().newInstance());armor.cursedKnown=false;assertEquals(name,"",CurseExplanation.armor(armor,hero,true));armor.cursedKnown=true;armor.level(0);String first=CurseExplanation.armor(armor,hero,true);armor.level(9);assertFalse(name,first.isEmpty());assertEquals(name,first,CurseExplanation.armor(armor,hero,true));}
 }
 @Test public void polarizedChanceAndDamageFactorMatchRealProcAtDifferentArcanaLevels(){
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.cursedKnown=true;sword.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Polarized());RingOfArcana arcana=ring(0);
  for(int level:new int[]{0,4,12}){arcana.level(level);float chance=.5f*EnchantmentExplanation.knownPower(hero);String text=CurseExplanation.weapon(sword,hero,true);assertTrue(text,text.contains(ObfuscationExplanation.number(100*Math.min(1,chance))+"%"));for(int seed=0;seed<20;seed++){Random.pushGenerator(seed);boolean trigger=Random.Float()<chance;Random.popGenerator();Random.pushGenerator(seed);try{assertEquals(trigger?15:0,sword.enchantment.proc(sword,hero,null,10));}finally{Random.popGenerator();}}}
 }
 @Test public void activeWaywardClearsWithoutANewChanceRollOrTooltipMutation(){
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.cursedKnown=true;sword.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward());com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward.WaywardBuff penalty=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward.WaywardBuff.class,10);assertTrue(CurseExplanation.weapon(sword,hero,false).contains("next eligible activation clears"));assertSame(penalty,hero.buff(penalty.getClass()));
  Random.pushGenerator(123);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(123);try{assertEquals(10,sword.enchantment.proc(sword,hero,null,10));assertEquals(expected,Random.Int());}finally{Random.popGenerator();}assertNull(hero.buff(penalty.getClass()));
 }
 @Test public void metabolismPreviewCannotSpendHungerOrHealingFractionalCredit(){
  com.watabou.utils.Bundle all=new com.watabou.utils.Bundle(),settings=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.defaults().with(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.Slider.HEALING,25).store(settings);all.put("apd_custom_rules",settings);Dungeon.apdRestoreCustomRules(all);hero.HP=10;hero.apdCustomHealingFraction=.75f;armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Metabolism());armor.cursedKnown=true;float time=hero.cooldown();
  for(int i=0;i<100;i++){String text=CurseExplanation.armor(armor,hero,true);assertTrue(text,text.contains("40 hunger points"));}
  assertEquals(.75f,hero.apdCustomHealingFraction,0);assertEquals(10,hero.HP);assertEquals(time,hero.cooldown(),0);assertNull(hero.buff(Hunger.class));
 }
 @Test public void sacrificialTwoChecksUseCurrentHealthRatherThanMissingHealth(){
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.cursedKnown=true;sword.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial());
  for(int hp:new int[]{3,16}){hero.HP=hp;float first=.1f*EnchantmentExplanation.knownPower(hero),second=(float)Math.pow(hp/(float)hero.HT,2)*hero.HT/8f;String text=CurseExplanation.weapon(sword,hero,false);assertTrue(text,text.contains(ObfuscationExplanation.number(100*Math.min(1,first)*Math.min(1,second))+"%"));
   for(int seed=0;seed<100;seed++){Buff.detach(hero,Bleeding.class);Random.pushGenerator(seed);boolean trigger=Random.Float()<first&&Random.Float()<second;Random.popGenerator();Random.pushGenerator(seed);try{sword.enchantment.proc(sword,hero,null,10);}finally{Random.popGenerator();}assertEquals(trigger,hero.buff(Bleeding.class)!=null);}
  }
 }
 @Test public void artifactRechargeRatesMatchRealPassiveAndEventHandlers()throws Exception{
  RingOfEnergy energy=new RingOfEnergy();energy.anonymize();energy.level(2);energy.levelKnown=energy.cursedKnown=true;hero.belongings.ring=energy;energy.activate(hero);
  java.lang.reflect.Field charge=com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.class.getDeclaredField("charge"),partial=com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.class.getDeclaredField("partialCharge");charge.setAccessible(true);partial.setAccessible(true);
  for(String name:new String[]{"AlchemistsToolkit","HornOfPlenty","MasterThievesArmband","SandalsOfNature","CloakOfShadows","HolyTome","TimekeepersHourglass","UnstableSpellbook","SkeletonKey","LloydsBeacon","TalismanOfForesight","DriedRose","EtherealChains"}){
   com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact item=(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)Class.forName("com.shatteredpixel.shatteredpixeldungeon.items.artifacts."+name).getConstructor().newInstance();item.levelKnown=item.cursedKnown=true;hero.belongings.artifact=item;charge.setInt(item,0);partial.setFloat(item,0);
   java.lang.reflect.Method factory=item.getClass().getDeclaredMethod("passiveBuff");factory.setAccessible(true);Buff effect=(Buff)factory.invoke(item);effect.target=hero;
   float expected=ArtifactEconomyExplanation.knownGain(item,hero);
   if(name.equals("AlchemistsToolkit")||name.equals("HornOfPlenty")||name.equals("MasterThievesArmband"))effect.getClass().getMethod("gainCharge",float.class).invoke(effect,1f);
   else if(name.equals("SandalsOfNature"))effect.getClass().getMethod("charge").invoke(effect);
   else effect.getClass().getMethod("act").invoke(effect);
   assertEquals(name,expected,item.currentCharges()+partial.getFloat(item),.00001f);assertFalse(name,ArtifactEconomyExplanation.details(item,hero).isEmpty());
  }
 }
 @Test public void unidentifiedRechargeRingCannotBeInferredFromArtifactRate(){
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass item=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass();item.levelKnown=item.cursedKnown=true;RingOfEnergy energy=new RingOfEnergy();hero.belongings.ring=energy;energy.levelKnown=false;String first=ArtifactEconomyExplanation.summary(item,hero);assertTrue(Float.isNaN(ArtifactEconomyExplanation.knownGain(item,hero)));hero.belongings.ring=new RingOfHaste();assertEquals(first,ArtifactEconomyExplanation.summary(item,hero));assertTrue(Float.isNaN(ArtifactEconomyExplanation.knownGain(item,hero)));
 }
 @Test public void artifactRatesExplainDifferentEventsAndPausedFullMeters(){
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty horn=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty();horn.levelKnown=horn.cursedKnown=true;assertTrue(ArtifactEconomyExplanation.summary(horn,hero).contains("per whole hero-level"));
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature sandals=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature();sandals.levelKnown=sandals.cursedKnown=true;assertTrue(ArtifactEconomyExplanation.summary(sandals,hero).contains("per eligible grass tile"));
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows cloak=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows();cloak.levelKnown=cloak.cursedKnown=true;assertTrue(ArtifactEconomyExplanation.summary(cloak,hero).contains("currently full"));
 }
 @Test public void cursedArmbandDescriptionMatchesActualGoldChanceAndZeroGoldGuard()throws Exception{
  int prior=Dungeon.gold;try{com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband armband=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband();armband.cursed=armband.levelKnown=armband.cursedKnown=true;assertTrue(ArtifactEconomyExplanation.summary(armband,hero).contains("20%"));java.lang.reflect.Method factory=armband.getClass().getDeclaredMethod("passiveBuff");factory.setAccessible(true);Buff effect=(Buff)factory.invoke(armband);effect.target=hero;
   for(int seed=0;seed<100;seed++){Dungeon.gold=10;Random.pushGenerator(seed);boolean loss=Random.Int(5)==0;Random.popGenerator();Random.pushGenerator(seed);try{effect.getClass().getMethod("act").invoke(effect);}finally{Random.popGenerator();}assertEquals(loss?9:10,Dungeon.gold);}
   Dungeon.gold=0;Random.pushGenerator(456);int next=Random.Int();Random.popGenerator();Random.pushGenerator(456);try{effect.getClass().getMethod("act").invoke(effect);assertEquals(next,Random.Int());}finally{Random.popGenerator();}assertEquals(0,Dungeon.gold);
  }finally{Dungeon.gold=prior;}
 }
 @Test public void cursedHourglassDescriptionMatchesExtraTimeChance()throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass item=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass();item.cursed=item.levelKnown=item.cursedKnown=true;assertTrue(ArtifactEconomyExplanation.summary(item,hero).contains("10%"));java.lang.reflect.Method factory=item.getClass().getDeclaredMethod("passiveBuff");factory.setAccessible(true);Buff effect=(Buff)factory.invoke(item);effect.target=hero;
  for(int seed=0;seed<100;seed++){Random.pushGenerator(seed);boolean delay=Random.Int(10)==0;Random.popGenerator();float before=hero.cooldown();Random.pushGenerator(seed);try{effect.getClass().getMethod("act").invoke(effect);}finally{Random.popGenerator();}assertEquals(delay?1:0,hero.cooldown()-before,0);}
 }
 @Test public void cursedChaliceExplainsActualSlowerRegeneration(){
  hero.HP=10;com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood item=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood();item.cursed=item.levelKnown=item.cursedKnown=true;hero.belongings.artifact=item;item.activate(hero);Regeneration regen=Buff.affect(hero,Regeneration.class);regen.act();com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();regen.storeInBundle(state);assertEquals(1/15f,state.getFloat(Regeneration.PARTIAL_REGEN),.00001f);assertTrue(ArtifactEconomyExplanation.summary(item,hero).contains("delay by 1.5"));
 }
 @Test public void cursedHornNourishmentTextMatchesRealFoodHandler()throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty horn=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty();horn.cursed=horn.levelKnown=horn.cursedKnown=true;hero.belongings.artifact=horn;horn.activate(hero);Hunger hunger=Buff.affect(hero,Hunger.class);java.lang.reflect.Field level=Hunger.class.getDeclaredField("level");level.setAccessible(true);level.setFloat(hunger,300);com.shatteredpixel.shatteredpixeldungeon.items.food.Food food=new com.shatteredpixel.shatteredpixeldungeon.items.food.Food();food.energy=100;java.lang.reflect.Method satisfy=com.shatteredpixel.shatteredpixeldungeon.items.food.Food.class.getDeclaredMethod("satisfy",Hero.class);satisfy.setAccessible(true);satisfy.invoke(food,hero);assertEquals(300-100*.67f,level.getFloat(hunger),.00001f);assertTrue(ArtifactEconomyExplanation.summary(horn,hero).contains("0.67"));
 }
 @Test public void brimstoneBarrierPreviewMatchesActualIgnitionRollAndExistingShieldLimit(){
  armor.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone());armor.level(3);ring(2);float power=Armor.Glyph.genericProcChanceMultiplier(hero),gain=2*(power-1);int cap=Math.round(gain*4);
  for(int seed=0;seed<30;seed++){Buff.detach(hero,Barrier.class);Random.pushGenerator(seed);int offered=(int)gain+(Random.Float()<gain%1?1:0);Random.popGenerator();Random.pushGenerator(seed);try{new Burning().reignite(hero,4);}finally{Random.popGenerator();}assertEquals(Math.min(cap,offered),hero.buff(Barrier.class).shielding());}
  Barrier barrier=hero.buff(Barrier.class);barrier.setShield(cap+5);int before=barrier.shielding();assertEquals(0,GlyphExplanation.brimstoneExpectedGain(power,before),0);String description=GlyphExplanation.details(armor,hero);assertTrue(description,description.contains("per attempted Burning application"));new Burning().reignite(hero,4);assertEquals(before,barrier.shielding());
 }
 @Test public void berserkDescriptionIsReadOnlyWhileActivationKeepsItsRecoveryScaling()throws Exception{
  Berserk rage=Buff.affect(hero,Berserk.class);java.lang.reflect.Field power=Berserk.class.getDeclaredField("power"),levels=Berserk.class.getDeclaredField("levelRecovery"),turns=Berserk.class.getDeclaredField("turnRecovery");for(java.lang.reflect.Field field:new java.lang.reflect.Field[]{power,levels,turns})field.setAccessible(true);power.setFloat(rage,1.3334f);levels.setFloat(rage,2);turns.setInt(rage,100);com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();rage.storeInBundle(before);
  int preview=rage.previewShieldBoost();assertTrue(MechanicsDetails.buff(rage,rage.desc()).contains("Rage and damage"));for(int i=0;i<100;i++)rage.desc();com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();rage.storeInBundle(after);assertEquals(before.toString(),after.toString());assertEquals(preview,rage.currentShieldBoost());assertEquals(2*(2-1.3334f),levels.getFloat(rage),.00001f);assertEquals((int)(100*(2-1.3334f)),turns.getInt(rage));
 }
 @Test public void healingExplanationShowsRequestedPoolWithoutSpendingModifiedHealingCredit()throws Exception{
  com.watabou.utils.Bundle all=new com.watabou.utils.Bundle(),settings=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.defaults().with(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.Slider.HEALING,25).store(settings);all.put("apd_custom_rules",settings);Dungeon.apdRestoreCustomRules(all);hero.HP=10;hero.apdCustomHealingFraction=.5f;hero.sprite=quietSprite();Healing heal=new Healing();heal.target=hero;heal.setHeal(100,.25f,2);
  for(int i=0;i<100;i++){String text=BuffExplanation.details(heal);assertTrue(text,text.contains("27 HP"));assertTrue(text,text.contains("25% of the remaining pool"));}assertEquals(.5f,hero.apdCustomHealingFraction,0);assertEquals(100,heal.remainingHealingPool());
  heal.act();assertEquals(17,hero.HP);assertEquals(.25f,hero.apdCustomHealingFraction,0);assertEquals(73,heal.remainingHealingPool());assertEquals(20,heal.nextRequestedHealing());
 }
 @Test public void degradationExplanationMatchesActualCurveAndHidesUnidentifiedLevels(){
  Degrade degrade=Buff.affect(hero,Degrade.class,20);armor.levelKnown=false;armor.level(0);String hidden=BuffExplanation.details(degrade);armor.level(9);assertEquals(hidden,BuffExplanation.details(degrade));armor.levelKnown=true;assertEquals(Degrade.reduceLevel(9),armor.buffedLvl());String known=BuffExplanation.details(degrade);assertTrue(known,known.contains("known level 9; degradation-only result "+armor.buffedLvl()));assertTrue(known.contains("square-root curve"));
 }
 public static class StatusDamageObserver extends com.shatteredpixel.shatteredpixeldungeon.actors.Char {int received;public StatusDamageObserver(){HP=HT=100;}@Override public void damage(int amount,Object source){received=amount;}}
 @Test public void corrosionDisplayedBaseDamageTracksRealTickGrowth(){
  int depth=Dungeon.depth,branch=Dungeon.branch;try{Dungeon.depth=8;Dungeon.branch=0;StatusDamageObserver target=new StatusDamageObserver();Corrosion corrosion=new Corrosion();corrosion.target=target;corrosion.set(8,2);for(int expected:new int[]{2,3,4,5,6,6}){assertEquals(expected,corrosion.apdNextRaw());assertTrue(BuffExplanation.details(corrosion).contains(expected+" damage points"));corrosion.act();assertEquals(expected,target.received);}assertEquals(7,corrosion.apdNextRaw());}finally{Dungeon.depth=depth;Dungeon.branch=branch;}
 }
 @Test public void burningExplanationUsesLegitimateRangeWithoutRevealingCachedRandomOutcome(){
  Burning burning=new Burning();burning.reignite(hero,8);com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();burning.storeInBundle(state);state.put("next_dmg",2);burning.restoreFromBundle(state);String first=BuffExplanation.details(burning);state.put("next_dmg",3);burning.restoreFromBundle(state);assertEquals(first,BuffExplanation.details(burning));assertTrue(first,first.contains("1–"+burning.apdRandomMaximum()+" damage points"));
 }
 @Test public void wandEmpowerDescriptionMatchesActualAddedDamageAndRollCounter(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();Random.pushGenerator(991);int base=wand.damageRoll(0);Random.popGenerator();WandEmpower empower=Buff.affect(hero,WandEmpower.class);empower.set(3,2);assertTrue(BuffExplanation.details(empower).contains("Adds 3 damage points"));Random.pushGenerator(991);try{assertEquals(base+3,wand.damageRoll(0));}finally{Random.popGenerator();}assertEquals(1,empower.left);assertTrue(BuffExplanation.details(empower).contains("next 1 qualifying"));
 }
 public static class PlainStatusTarget extends com.shatteredpixel.shatteredpixeldungeon.actors.Char {public PlainStatusTarget(){HP=HT=100;}}
 @Test public void doomDescriptionMatchesActualIncomingDamageFactor(){PlainStatusTarget target=new PlainStatusTarget();Doom doom=Buff.affect(target,Doom.class);target.damage(20,new Object());assertEquals(67,target.HP);assertTrue(BuffExplanation.details(doom).contains("1.67"));}
 @Test public void healingDetailsExposeActualVialLimitAndItsEffectOnNextHeal(){Healing heal=new Healing();heal.setHeal(100,.25f,2);com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();heal.storeInBundle(state);state.put("vial_level",0);heal.restoreFromBundle(state);int limit=com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood.maxHealPerTurn(0);assertEquals(Math.min(27,limit),heal.nextRequestedHealing());assertTrue(BuffExplanation.details(heal).contains("Current Blood Vial limit: "+limit+" HP per tick"));}

 @Test public void liquidWillpowerExplanationMatchesActualPotionShieldAndMaximumHp()throws Exception{
  hero.sprite=quietSprite();hero.talents.get(1).put(Talent.LIQUID_WILLPOWER,1);
  for(int rank=1;rank<=2;rank++){hero.talents.get(1).put(Talent.LIQUID_WILLPOWER,rank);
   for(int hp:new int[]{20,81,143}){hero.HT=hp;Barrier old=hero.buff(Barrier.class);if(old!=null)old.detach();
    Talent.onPotionUsed(hero,hero.pos,1);int actual=hero.buff(Barrier.class).shielding();
    String text=MechanicsDetails.talent(Talent.LIQUID_WILLPOWER,rank,"");
    assertTrue(text,text.contains("At your current "+hp+" maximum HP: "+actual+" shield points"));
   }
  }
 }
 @Test public void liquidWillpowerKeepsStrongerBarrierAndPreviewDoesNotApplyShield()throws Exception{
  hero.sprite=quietSprite();hero.talents.get(1).put(Talent.LIQUID_WILLPOWER,2);Barrier barrier=Buff.affect(hero,Barrier.class);barrier.setShield(100);
  Talent.onPotionUsed(hero,hero.pos,1);assertEquals(100,barrier.shielding());
  for(int i=0;i<50;i++)MechanicsDetails.talent(Talent.LIQUID_WILLPOWER,2,"");assertEquals(100,barrier.shielding());
 }
 @Test public void inscribedPowerMatchesActualUseCounterAndKeepsLargerApplication(){
  hero.talents.get(1).put(Talent.INSCRIBED_POWER,1);
  for(int rank=1;rank<=2;rank++){hero.talents.get(1).put(Talent.INSCRIBED_POWER,rank);
   Talent.onScrollUsed(hero,hero.pos,1,com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify.class);
   assertEquals(Integer.toString(1+rank),hero.buff(ScrollEmpower.class).iconTextDisplay());
   assertTrue(MechanicsDetails.talent(Talent.INSCRIBED_POWER,rank,"").contains((1+rank)+" empowered wand uses"));
  }
  Talent.onScrollUsed(hero,hero.pos,.25f,com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify.class);
  assertEquals("3",hero.buff(ScrollEmpower.class).iconTextDisplay());
 }
 @Test public void talentDetailsDoNotInventChargeCountsAndRefreshWithRank(){
  assertTrue(MechanicsDetails.talent(Talent.SHIELD_BATTERY,1,"").contains("4% of maximum HP"));
  assertTrue(MechanicsDetails.talent(Talent.SHIELD_BATTERY,2,"").contains("6% of maximum HP"));
  assertTrue(MechanicsDetails.talent(Talent.LIQUID_NATURE,2,"").contains("up to 6 nearby tiles"));
  assertEquals("",com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.TalentExplanation.details(Talent.SHIELD_BATTERY,0));
 }

 @Test public void accuracyExplanationsNeverConsumeLiquidAgilityUsesOrRandomness(){
  hero.talents.get(1).put(Talent.LIQUID_AGILITY,1);Talent.LiquidAgilACCTracker ready=Buff.affect(hero,Talent.LiquidAgilACCTracker.class,5);ready.uses=2;
  Random.pushGenerator(482);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(482);
  try{for(int i=0;i<100;i++){String text=MechanicsDetails.buff(ready,"");assertTrue(text.contains("2 qualifying attack uses remain"));assertTrue(text.contains("multiplies its accuracy score by 3"));}assertEquals(expected,Random.Int());}finally{Random.popGenerator();}
  assertEquals(2,ready.uses);hero.talents.get(1).put(Talent.LIQUID_AGILITY,2);assertTrue(MechanicsDetails.buff(ready,"").contains("uses guaranteed-hit accuracy"));assertEquals(2,ready.uses);
 }
 @Test public void preciseAssaultExplainsRankMultipliersAndGuaranteedEvasionPriority(){
  assertTrue(MechanicsDetails.talent(Talent.PRECISE_ASSAULT,1,"").contains("2 times"));
  assertTrue(MechanicsDetails.talent(Talent.PRECISE_ASSAULT,2,"").contains("5 times"));
  String third=MechanicsDetails.talent(Talent.PRECISE_ASSAULT,3,"");assertTrue(third.contains("guaranteed-hit accuracy"));assertTrue(third.contains("1.3"));assertTrue(third.contains("Guaranteed evasion wins"));
 }

 @Test public void flatAttackTalentDescriptionsMatchRealProcAndConsumeOnlyActualAttack(){
  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat enemy=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();enemy.HP=enemy.HT=20;
  for(int rank=1;rank<=2;rank++){
   hero.talents.get(0).put(Talent.PROVOKED_ANGER,rank);Buff.affect(hero,Talent.ProvokedAngerTracker.class,5);
   String text=MechanicsDetails.talent(Talent.PROVOKED_ANGER,rank,"");assertNotNull(hero.buff(Talent.ProvokedAngerTracker.class));
   int bonus=1+2*rank;assertTrue(text.contains("adds "+bonus+" damage points"));assertEquals(10+bonus,Talent.onAttackProc(hero,enemy,10));assertNull(hero.buff(Talent.ProvokedAngerTracker.class));hero.talents.get(0).remove(Talent.PROVOKED_ANGER);
   hero.talents.get(0).put(Talent.LINGERING_MAGIC,rank);
   for(int seed=0;seed<30;seed++){Buff.affect(hero,Talent.LingeringMagicTracker.class,5);Random.pushGenerator(seed);int actual;try{actual=Talent.onAttackProc(hero,enemy,10)-10;}finally{Random.popGenerator();}assertTrue(actual>=rank&&actual<=2);assertNull(hero.buff(Talent.LingeringMagicTracker.class));}
   assertTrue(MechanicsDetails.talent(Talent.LINGERING_MAGIC,rank,"").contains(rank+"–2 damage points"));hero.talents.get(0).remove(Talent.LINGERING_MAGIC);
  }
 }
 @Test public void deadlyFollowupMatchesActualRoundedMultiplicationAndRetainsMark(){
  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat enemy=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();enemy.HP=enemy.HT=20;
  for(int rank=1;rank<=3;rank++){hero.talents.get(2).put(Talent.DEADLY_FOLLOWUP,rank);Talent.DeadlyFollowupTracker mark=Buff.affect(hero,Talent.DeadlyFollowupTracker.class,5);mark.object=enemy.id();
   for(int damage:new int[]{1,7,11,30})assertEquals(Math.round(damage*(1+.1f*rank)),Talent.onAttackProc(hero,enemy,damage));
   String text=MechanicsDetails.talent(Talent.DEADLY_FOLLOWUP,rank,"");assertTrue(text.contains("by "+ObfuscationExplanation.number(1+.1f*rank)));assertTrue(text.contains("one melee hit does not consume"));assertSame(mark,hero.buff(Talent.DeadlyFollowupTracker.class));
  }
 }
}
