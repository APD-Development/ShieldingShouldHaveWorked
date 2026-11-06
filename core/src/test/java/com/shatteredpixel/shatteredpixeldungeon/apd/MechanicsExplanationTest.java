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
 public static class QuietMovementSprite extends com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite {@Override public com.watabou.noosa.particles.Emitter emitter(){return new com.watabou.noosa.particles.Emitter();} @Override public void add(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.State state){} @Override public void remove(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.State state){} @Override public void sprint(float speed){} @Override public void showStatus(int color,String text,Object...args){} @Override public void showStatusWithIcon(int color,String text,int icon,Object...args){}}
 private QuietMovementSprite quietSprite()throws Exception{java.lang.reflect.Field field=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");field.setAccessible(true);Object unsafe=field.get(null);return (QuietMovementSprite)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,QuietMovementSprite.class);}
 @Test public void hasteDetailsAgreeWithActualMovementFactorAndDoNotClaimFasterAttacks()throws Exception{java.lang.reflect.Field field=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");field.setAccessible(true);Object unsafe=field.get(null);hero.sprite=(QuietMovementSprite)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,QuietMovementSprite.class);float before=hero.speed();Haste haste=Buff.affect(hero,Haste.class,5);assertEquals(before*3,hero.speed(),.00001f);assertTrue(MechanicsDetails.buff(haste,haste.desc()).contains("does not multiply attack"));assertTrue(MechanicsDetails.buff(haste,haste.desc()).contains("scheduled update: "+ObfuscationExplanation.number(haste.cooldown())+" game-time units"));assertTrue(MechanicsDetails.buff(haste,haste.desc()).contains("counter adds one"));}
 @Test public void talentDetailsRefreshRankAndDistinguishTemporaryFromActualUpgrades(){String first=MechanicsDetails.talent(Talent.ENHANCED_RINGS,1,"");String next=MechanicsDetails.talent(Talent.ENHANCED_RINGS,3,"");assertTrue(first.contains("= 3 turns"));assertTrue(next.contains("= 9 turns"));assertTrue(next.contains("ordinary rather than effective"));assertTrue(MechanicsDetails.talent(Talent.HOLD_FAST,3,"").contains("decay by 0"));assertTrue(MechanicsDetails.talent(Talent.PROTECTIVE_SHADOWS,2,"").contains("5 shield points"));}
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

 @Test public void growingChampionDetailsFollowActualGrowthAndSavedMultiplier(){
  ChampionEnemy.Growing growing=new ChampionEnemy.Growing();
  for(int tick=0;tick<20;tick++){
   String text=MechanicsDetails.buff(growing,growing.desc());assertTrue(text.contains("attack damage by "+ObfuscationExplanation.number(growing.meleeDamageFactor())));assertTrue(text.contains("scores by "+ObfuscationExplanation.number(growing.evasionAndAccuracyFactor())));assertTrue(text.contains("step by "+ObfuscationExplanation.number(growing.damageTakenFactor())));
   float before=growing.meleeDamageFactor();float time=growing.cooldown();growing.act();assertEquals(before+.01f,growing.meleeDamageFactor(),0);assertEquals(time+4,growing.cooldown(),0);
  }
  com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();growing.storeInBundle(saved);ChampionEnemy.Growing loaded=new ChampionEnemy.Growing();loaded.restoreFromBundle(saved);assertEquals(MechanicsDetails.buff(growing,growing.desc()),MechanicsDetails.buff(loaded,loaded.desc()));
 }
 @Test public void championDetailsUseRealFactorsAndNeverRollOrGrow(){
  ChampionEnemy[] champions={new ChampionEnemy.Blazing(),new ChampionEnemy.Projecting(),new ChampionEnemy.AntiMagic(),new ChampionEnemy.Giant(),new ChampionEnemy.Blessed(),new ChampionEnemy.Growing()};
  Random.pushGenerator(412);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(412);
  try{for(ChampionEnemy champion:champions){float time=champion.cooldown(),factor=champion.meleeDamageFactor();for(int i=0;i<30;i++){String text=MechanicsDetails.buff(champion,champion.desc());assertTrue(text.contains("scores by "+ObfuscationExplanation.number(champion.evasionAndAccuracyFactor())));assertTrue(text.contains("rounds upward"));}assertEquals(time,champion.cooldown(),0);assertEquals(factor,champion.meleeDamageFactor(),0);}assertEquals(expected,Random.Int());}finally{Random.popGenerator();}
 }

 @Test public void championIncomingRoundingExplainsSmallHitsUsingActualDamageHandler(){
  for(int damage:new int[]{1,2,3,7,20}){
   PlainStatusTarget giant=new PlainStatusTarget();ChampionEnemy.Giant effect=Buff.affect(giant,ChampionEnemy.Giant.class);giant.damage(damage,new Object());assertEquals((int)Math.ceil(damage*effect.damageTakenFactor()),100-giant.HP);
   PlainStatusTarget antimagic=new PlainStatusTarget();ChampionEnemy.AntiMagic half=Buff.affect(antimagic,ChampionEnemy.AntiMagic.class);antimagic.damage(damage,new Object());assertEquals((int)Math.ceil(damage*half.damageTakenFactor()),100-antimagic.HP);
  }
 }

 @Test public void wandStatsUnderWildMagicNeverConsumeRandomRoll(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;wand.charge(hero);Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic.WildMagicTracker.class);
  Random.pushGenerator(412);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(412);try{for(int i=0;i<30;i++)wand.statsDesc();assertEquals(expected,Random.Int());}finally{Random.popGenerator();}
 }

 @Test public void wandPreviewBoundsMatchActualWildMagicLevelsAcrossRanksAndModifiers(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;wand.charge(hero);wand.curCharges=1;hero.talents.get(1).put(Talent.DESPERATE_POWER,1);Buff.affect(hero,ScrollEmpower.class).reset(3);Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic.WildMagicTracker.class);
  for(int rank=0;rank<=4;rank++){hero.talents.get(1).put(Talent.WILD_POWER,rank);for(int level=0;level<=7;level++){wand.level(level);int[] bounds=wand.previewEffectiveLevels();int seenMin=Integer.MAX_VALUE,seenMax=Integer.MIN_VALUE;for(int seed=0;seed<30;seed++){Random.pushGenerator(seed);int actual;try{actual=wand.buffedLvl();}finally{Random.popGenerator();}seenMin=Math.min(seenMin,actual);seenMax=Math.max(seenMax,actual);}assertEquals(seenMin,bounds[0]);assertEquals(seenMax,bounds[1]);}}
 }
 @Test public void unknownWandDetailsCannotRevealLevelsOrCharges(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.levelKnown=false;String first=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.WandExplanation.details(wand);wand.level(12);wand.curCharges=9;assertEquals(first,com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.WandExplanation.details(wand));assertArrayEquals(new int[]{0,0},wand.previewEffectiveLevels());
 }
 @Test public void directWandExplanationsUseActualBoundsAndDoNotSpendEmpower(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand[] wands={new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion()};
  WandEmpower empower=Buff.affect(hero,WandEmpower.class);empower.set(3,2);
  for(com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand wand:wands){wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;for(int level=0;level<=9;level++){wand.level(level);String text=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.WandExplanation.details(wand);assertTrue(text.contains("Current roll bounds: "+wand.min(level)+"–"+wand.max(level)));assertEquals(wand.min(0)+(wand.min(1)-wand.min(0))*level,wand.min(level));assertEquals(wand.max(0)+(wand.max(1)-wand.max(0))*level,wand.max(level));}assertEquals(2,empower.left);}
 }
 @Test public void staleUncursedInfusionPreviewDoesNotCleanStateOrChangeCharges(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.levelKnown=true;wand.level(3);wand.cursed=false;wand.curseInfusionBonus=true;wand.curCharges=4;assertArrayEquals(new int[]{3,3},wand.previewEffectiveLevels());assertTrue(wand.curseInfusionBonus);assertEquals(4,wand.curCharges);
 }

 @Test public void allNumericalWandInfoPreviewsPreserveRandomnessAndEmpowerUses(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand[] wands={new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion()};
  hero.talents.get(1).put(Talent.WILD_POWER,1);Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic.WildMagicTracker.class);WandEmpower empower=Buff.affect(hero,WandEmpower.class);empower.set(3,2);
  for(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand:wands){wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;wand.charge(hero);Random.pushGenerator(312);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(312);try{for(int i=0;i<10;i++){wand.info();MechanicsDetails.item(wand,wand.info());}assertEquals(expected,Random.Int());}finally{Random.popGenerator();}assertEquals(2,empower.left);}
 }
 @Test public void wandPreviewMatchesDegradeInfusionResinAndStrongerMagicCharge(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.levelKnown=true;wand.level(6);wand.cursed=true;wand.curseInfusionBonus=true;wand.resinBonus=2;wand.charge(hero);Buff.affect(hero,Degrade.class);assertEquals(wand.buffedLvl(),wand.previewEffectiveLevels()[0]);assertEquals(wand.buffedLvl(),wand.previewEffectiveLevels()[1]);
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile.MagicCharge charge=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile.MagicCharge.class);com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();charge.storeInBundle(state);state.put("level",20);charge.restoreFromBundle(state);assertEquals(wand.buffedLvl(),wand.previewEffectiveLevels()[0]);assertEquals(wand.buffedLvl(),wand.previewEffectiveLevels()[1]);
 }

 @Test public void strongmanUsesBaseStrengthAndDoesNotMultiplyRingContribution(){
  for(int rank=1;rank<=3;rank++){hero.talents.get(2).put(Talent.STRONGMAN,rank);for(int strength=10;strength<=20;strength++){hero.STR=strength;int added=(int)Math.floor(strength*(.03f+.05f*rank));assertEquals(strength+5+added,hero.previewStrength(5));String text=MechanicsDetails.talent(Talent.STRONGMAN,rank,"");assertTrue(text.contains("this rank adds "+added+" strength points"));assertTrue(text.contains("excluding Ring of Might"));}}
 }
 public static class RangeAccuracyDart extends com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart {public float rangeFactor(Hero owner,com.shatteredpixel.shatteredpixeldungeon.actors.Char enemy){return adjacentAccFactor(owner,enemy);}}
 @Test public void pointBlankDetailsMatchActualAdjacentAndDistantFactors(){
  RangeAccuracyDart dart=new RangeAccuracyDart();PlainStatusTarget enemy=new PlainStatusTarget();hero.pos=24;
  for(int rank=1;rank<=3;rank++){hero.talents.get(2).put(Talent.POINT_BLANK,rank);enemy.pos=25;float adjacent=dart.rangeFactor(hero,enemy);assertTrue(MechanicsDetails.talent(Talent.POINT_BLANK,rank,"").contains("by "+ObfuscationExplanation.number(adjacent)));enemy.pos=26;assertEquals(1.5f,dart.rangeFactor(hero,enemy),0);}
 }
 @Test public void empoweredStrikeDamageDetailsMatchActualStaffProcAndPowerFactor(){
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();PlainStatusTarget target=new PlainStatusTarget();
  for(int rank=1;rank<=3;rank++){hero.talents.get(2).put(Talent.EMPOWERED_STRIKE,rank);for(int damage:new int[]{1,7,18}){Buff.affect(hero,Talent.EmpoweredStrikeTracker.class);assertEquals(Math.round(damage*(1f+rank/6f)),staff.proc(hero,target,damage));assertNull(hero.buff(Talent.EmpoweredStrikeTracker.class));}
   String text=MechanicsDetails.talent(Talent.EMPOWERED_STRIKE,rank,"");assertTrue(text.contains("by "+ObfuscationExplanation.number(1f+rank/6f)));Buff.affect(hero,Talent.EmpoweredStrikeTracker.class);assertTrue(text.contains("factor of "+ObfuscationExplanation.number(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand.procChanceMultiplier(hero))));hero.buff(Talent.EmpoweredStrikeTracker.class).detach();}
 }

 @Test public void adrenalineTimingExplanationMatchesHeroAndMonsterActualPaths()throws Exception{
  hero.sprite=quietSprite();float heroMovement=hero.speed(),heroAttack=hero.attackDelay();Adrenaline effect=Buff.affect(hero,Adrenaline.class,5);assertEquals(heroMovement*2,hero.speed(),.00001f);assertEquals(heroAttack,hero.attackDelay(),0);assertTrue(MechanicsDetails.buff(effect,effect.desc()).contains("hero does not gain"));
  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();float delay=rat.attackDelay();Buff.affect(rat,Adrenaline.class,5);assertEquals(delay/1.5f,rat.attackDelay(),.00001f);
 }
 @Test public void furyConditionKeepsExactHalfHpAndEndsAboveIt(){
  PlainStatusTarget target=new PlainStatusTarget();target.HP=50;Fury fury=Buff.affect(target,Fury.class);fury.act();assertSame(fury,target.buff(Fury.class));assertTrue(MechanicsDetails.buff(fury,fury.desc()).contains("strictly above 50%"));target.HP=51;fury.act();assertNull(target.buff(Fury.class));assertFalse(fury.desc().contains("%%"));
 }
 @Test public void fireImbueDescriptionMatchesActualIgnitionChecksAndDuration()throws Exception{
  FireImbue fire=Buff.affect(hero,FireImbue.class);fire.set(7);fire.extend(3);assertTrue(MechanicsDetails.buff(fire,fire.desc()).contains("duration: 10 turns"));
  for(int seed=0;seed<20;seed++){PlainStatusTarget enemy=new PlainStatusTarget();enemy.sprite=quietSprite();Random.pushGenerator(seed);boolean expected=Random.Int(2)==0;Random.popGenerator();Random.pushGenerator(seed);try{fire.proc(enemy);}finally{Random.popGenerator();}assertEquals(expected,enemy.buff(Burning.class)!=null);}
 }
 @Test public void frostImbueDescriptionMatchesStackedChillAndActualImmunity()throws Exception{
  PlainStatusTarget enemy=new PlainStatusTarget();enemy.sprite=quietSprite();FrostImbue frost=Buff.affect(hero,FrostImbue.class,8);frost.proc(enemy);float first=enemy.buff(Chill.class).cooldown();frost.proc(enemy);assertEquals(first+3,enemy.buff(Chill.class).cooldown(),.00001f);assertTrue(hero.isImmune(Chill.class));assertTrue(hero.isImmune(Frost.class));assertTrue(MechanicsDetails.buff(frost,frost.desc()).contains("three turns of Chill"));
 }

 @Test public void armorAbilityExplanationMatchesActualRankAndConditionalCost(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap leap=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap();
  for(int rank=0;rank<=4;rank++){hero.talents.get(2).put(Talent.HEROIC_ENERGY,rank);String text=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.AbilityExplanation.summary(leap,hero);assertTrue(text.contains(ObfuscationExplanation.number(leap.chargeUse(hero))+" charge points"));}
  hero.talents.get(2).put(Talent.DOUBLE_JUMP,2);Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap.DoubleJumpTracker.class);assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.AbilityExplanation.details(leap,hero).contains("cost: "+ObfuscationExplanation.number(leap.chargeUse(hero))+" charge points"));hero.armorAbility=leap;assertTrue(MechanicsDetails.item(new WarriorArmor(),"").contains("Actual current ability cost: "+ObfuscationExplanation.number(leap.chargeUse(hero))+" charge points"));
 }
 @Test public void abilityDetailsNeverConsumeReadyDiscountOrExposeUnknownRingLevel(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap leap=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap();hero.talents.get(2).put(Talent.DOUBLE_JUMP,1);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap.DoubleJumpTracker ready=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap.DoubleJumpTracker.class);RingOfEnergy energy=new RingOfEnergy();energy.levelKnown=false;hero.belongings.ring=energy;energy.activate(hero);String first=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.AbilityExplanation.details(leap,hero);energy.level(9);assertEquals(first,com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.AbilityExplanation.details(leap,hero));assertSame(ready,hero.buff(ready.getClass()));
 }

 @Test public void armorRechargeDetailsMatchActualChargerAndMeterCap(){
  com.shatteredpixel.shatteredpixeldungeon.items.armor.WarriorArmor equipment=new com.shatteredpixel.shatteredpixeldungeon.items.armor.WarriorArmor();com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor.Charger charger=equipment.new Charger();charger.target=hero;
  RingOfEnergy energy=new RingOfEnergy();energy.anonymize();energy.levelKnown=energy.cursedKnown=true;hero.belongings.ring=energy;energy.activate(hero);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap();
  for(int level=0;level<=3;level++){energy.level(level);equipment.charge=0;charger.act();float actual=equipment.charge;assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.AbilityExplanation.details(ability,hero).contains("per eligible update: "+ObfuscationExplanation.number(actual)+" charge points"));equipment.charge=99.99f;charger.act();assertEquals(100,equipment.charge,0);}
 }

 @Test public void paralysisExplanationMatchesActualEscapeRollBeforeShielding(){
  for(int seed=0;seed<30;seed++){PlainStatusTarget target=new PlainStatusTarget();target.pos=24;Paralysis paralysis=Buff.affect(target,Paralysis.class,5);Barrier shield=Buff.affect(target,Barrier.class);shield.setShield(100);
   Random.pushGenerator(seed);boolean breaks=Random.NormalIntRange(0,20)>=Random.NormalIntRange(0,100);Random.popGenerator();Random.pushGenerator(seed);try{target.damage(20,new Object());}finally{Random.popGenerator();}
   assertEquals(breaks,target.buff(Paralysis.class)==null);assertEquals(100,target.HP);assertEquals(20,target.buff(Paralysis.ParalysisResist.class).accumulatedDamage());assertTrue(BuffExplanation.details(paralysis).contains("before shield points"));}
 }
 @Test public void paralysisDetailsHideEnemyCounterAndPreserveHeroCounter(){
  Paralysis heroEffect=Buff.affect(hero,Paralysis.class,5);heroEffect.processDamage(3);Paralysis.ParalysisResist count=hero.buff(Paralysis.ParalysisResist.class);int before=count.accumulatedDamage();for(int i=0;i<30;i++)assertTrue(BuffExplanation.details(heroEffect).contains("counted damage: "+before+" damage points"));assertEquals(before,count.accumulatedDamage());
  PlainStatusTarget enemy=new PlainStatusTarget();Paralysis other=Buff.affect(enemy,Paralysis.class,5);assertFalse(BuffExplanation.details(other).contains("current counted damage"));
 }
 @Test public void magicalSleepExplanationMatchesActualHealingAndFullHpEnding()throws Exception{
  hero.sprite=quietSprite();hero.HP=18;MagicalSleep sleep=Buff.affect(hero,MagicalSleep.class);assertTrue(BuffExplanation.details(sleep).contains("one unmodified HP"));float fraction=hero.apdCustomHealingFraction;for(int i=0;i<30;i++)MechanicsDetails.buff(sleep,sleep.desc());assertEquals(18,hero.HP);assertEquals(fraction,hero.apdCustomHealingFraction,0);sleep.act();assertEquals(19,hero.HP);sleep.act();assertEquals(20,hero.HP);assertNull(hero.buff(MagicalSleep.class));
 }

 @Test public void regrowthAllowanceExplanationMatchesRealFormulaRatherThanStaleComments()throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth();wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;
  java.lang.reflect.Method actual=wand.getClass().getDeclaredMethod("chargeLimit",int.class);actual.setAccessible(true);
  for(int heroLevel:new int[]{1,10,20,30}){hero.lvl=heroLevel;for(int level=0;level<=10;level++){wand.level(level);int expected=(Integer)actual.invoke(wand,heroLevel);assertEquals(expected,wand.previewChargeLimit(heroLevel));assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.WandExplanation.details(wand).contains("Current allowance: "+expected+" charge units"));}}
 }
 @Test public void regrowthDepletionDetailsFollowSavedCountersAndIgnoreTemporaryLevelForAllowance(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth();wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;wand.level(3);int limit=wand.previewChargeLimit(hero.lvl);com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();wand.storeInBundle(saved);saved.put("totChrgUsed",limit);saved.put("chargesOverLimit",3);wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth();wand.restoreFromBundle(saved);
  assertEquals(.8f,wand.previewFurrowedChance(hero.lvl),0);String text=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.WandExplanation.details(wand);assertTrue(text.contains("grass checks: 80%"));wand.charge(hero);Buff.affect(hero,ScrollEmpower.class).reset(3);assertEquals(limit,wand.previewChargeLimit(hero.lvl));assertEquals(.8f,wand.previewFurrowedChance(hero.lvl),0);
 }
 @Test public void corruptionDetailsUseSharedLivePowerAndDoNotClaimOneSuccessChance(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption();wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;
  for(int level=0;level<=9;level++){wand.level(level);String text=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.WandExplanation.details(wand);assertTrue(text.contains("Current strength: "+ObfuscationExplanation.number(com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption.corruptionPower(level))));assertTrue(text.contains("not the final corruption chance"));assertTrue(text.contains("strictly greater"));}
 }
 @Test public void wardSupportUsesOrdinaryKnownLevelRatherThanTemporaryCastingLevel(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding();wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;wand.level(3);wand.charge(hero);Buff.affect(hero,ScrollEmpower.class).reset(3);assertEquals(5,wand.previewEffectiveLevels()[0]);String text=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.WandExplanation.details(wand);assertTrue(text.contains("known level = 5 support points"));assertTrue(text.contains("Temporary casting levels do not increase"));
 }

 private com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff describedStaff(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand,int level)throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();staff.level(level);staff.levelKnown=true;wand.level(level);wand.levelKnown=wand.cursedKnown=wand.curChargeKnown=true;wand.charge(hero);java.lang.reflect.Field field=staff.getClass().getDeclaredField("wand");field.setAccessible(true);field.set(staff,wand);return staff;
 }
 @Test public void staffCorruptionChanceAndDurationMatchActualOnHitChecks()throws Exception{
  hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.BATTLEMAGE;hero.talents.get(2).put(Talent.EMPOWERED_STRIKE,2);Buff.affect(hero,Talent.EmpoweredStrikeTracker.class);com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption();com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=describedStaff(wand,3);float chance=4f/9f*com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand.procChanceMultiplier(hero);String text=staff.mechanicsDetails();assertTrue(text.contains(ObfuscationExplanation.number(100*chance)+"% per eligible hit"));
  for(int seed=0;seed<40;seed++){PlainStatusTarget target=new PlainStatusTarget();Random.pushGenerator(seed);boolean expected=Random.Float()<chance;Random.popGenerator();Random.pushGenerator(seed);try{wand.onHit(staff,hero,target,10);}finally{Random.popGenerator();}assertEquals(expected,target.buff(Amok.class)!=null);if(expected)assertEquals(Math.round(10*Math.max(1,chance)),target.buff(Amok.class).cooldown(),.00001f);}
 }
 @Test public void staffRegrowthPoolMatchesActualTwoRoundingSteps()throws Exception{
  hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.BATTLEMAGE;hero.talents.get(2).put(Talent.EMPOWERED_STRIKE,1);Buff.affect(hero,Talent.EmpoweredStrikeTracker.class);Dungeon.level.map[hero.pos]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.GRASS;PlainStatusTarget target=new PlainStatusTarget();target.pos=25;com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth();com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=describedStaff(wand,3);
  for(int damage:new int[]{1,7,18,31}){com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health old=hero.buff(com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health.class);if(old!=null)old.detach();wand.onHit(staff,hero,target,damage);int expected=Math.round(Math.round(damage*5f/9f/2f)*1.5f);assertEquals(Integer.toString(expected),hero.buff(com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health.class).iconTextDisplay());}assertTrue(staff.mechanicsDetails().contains("round again"));
 }
 @Test public void staffPreviewUnderWildMagicNeverConsumesRandomnessOrReadyStrike()throws Exception{
  hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.BATTLEMAGE;hero.talents.get(2).put(Talent.EMPOWERED_STRIKE,2);Talent.EmpoweredStrikeTracker ready=Buff.affect(hero,Talent.EmpoweredStrikeTracker.class);Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic.WildMagicTracker.class);hero.talents.get(1).put(Talent.WILD_POWER,1);com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=describedStaff(new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning(),0);
  Random.pushGenerator(67);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(67);try{for(int i=0;i<100;i++){assertTrue(staff.mechanicsDetails().contains("single fixed activation percentage is not shown"));MechanicsDetails.item(staff,staff.info());}assertEquals(expected,Random.Int());}finally{Random.popGenerator();}assertSame(ready,hero.buff(Talent.EmpoweredStrikeTracker.class));
 }
 @Test public void unidentifiedStaffOnHitDetailsDoNotRevealHiddenUpgrade()throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=describedStaff(new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion(),0);staff.levelKnown=false;String first=staff.mechanicsDetails();staff.level(9);assertEquals(first,staff.mechanicsDetails());assertTrue(first.contains("Identify"));
 }

 @Test public void herbalHealingPreviewMatchesActualRequestsAndDoesNotSpendPool()throws Exception{
  hero.sprite=quietSprite();hero.HP=1;com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health healing=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health.class);healing.boost(100);
  for(int tick=0;tick<12;tick++){int expected=healing.nextUnmodifiedRequest(),pool=healing.remainingPool();float progress=healing.healingProgress();String text=MechanicsDetails.buff(healing,healing.desc());assertTrue(text.contains("next unmodified request: "+expected+" HP"));assertEquals(pool,healing.remainingPool());assertEquals(progress,healing.healingProgress(),0);hero.HP=1;healing.act();assertEquals(1+expected,hero.HP);assertEquals(pool-expected,healing.remainingPool());}
  int remaining=healing.remainingPool(),next=healing.nextUnmodifiedRequest();float fraction=healing.healingProgress();com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();healing.storeInBundle(saved);healing.detach();com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health loaded=new com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Health();loaded.restoreFromBundle(saved);loaded.attachTo(hero);assertEquals(remaining,loaded.remainingPool());assertEquals(fraction,loaded.healingProgress(),0);assertEquals(next,loaded.nextUnmodifiedRequest());
 }
 @Test public void rockArmorExplanationMatchesActualSmallHitRoundingAndLimitedReserve(){
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.RockArmor armor=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.RockArmor.class);com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();armor.storeInBundle(state);state.put("armor",3);state.put("wand_level",3);armor.restoreFromBundle(state);assertTrue(MechanicsDetails.buff(armor,armor.desc()).contains("Current reserve: 3 damage-blocking points"));assertEquals(20,armor.guardianThreshold());assertEquals(0,armor.absorb(1));assertEquals(2,armor.remainingReserve());assertEquals(5,armor.absorb(7));assertNull(hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.RockArmor.class));
 }
 @Test public void earthrootExplanationPreservesItsLastHitRuleRatherThanRockArmorRule(){
  com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot.Armor earth=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot.Armor.class);earth.level(1);int limit=earth.perHitLimit();assertTrue(MechanicsDetails.buff(earth,earth.desc()).contains("Remaining reserve: 1 points"));assertEquals(11-Math.min(11,limit),earth.absorb(11));assertNull(hero.buff(com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot.Armor.class));
 }

 @Test public void magicalSightDescriptionUsesLiveRadiusAndFieldOfViewDoesNotDiscoverSecret(){
  Dungeon.level.blobs=new java.util.HashMap<>();Dungeon.level.mobs=new java.util.HashSet<>();Dungeon.level.heaps=new com.watabou.utils.SparseArray<>();Dungeon.level.traps=new com.watabou.utils.SparseArray<>();Dungeon.level.plants=new com.watabou.utils.SparseArray<>();Dungeon.level.customTiles=new java.util.ArrayList<>();Dungeon.level.customWalls=new java.util.ArrayList<>();Dungeon.level.setSize(33,33);java.util.Arrays.fill(Dungeon.level.map,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);Dungeon.level.buildFlagMaps();Dungeon.level.discoverable=new boolean[Dungeon.level.length()];java.util.Arrays.fill(Dungeon.level.discoverable,true);hero.pos=16*33+16;hero.viewDistance=2;MagicalSight sight=Buff.affect(hero,MagicalSight.class,8);boolean[] view=new boolean[Dungeon.level.length()];int secret=hero.pos+3;com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(secret,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.SECRET_DOOR);Dungeon.level.updateFieldOfView(hero,view);assertTrue(view[hero.pos+MagicalSight.DISTANCE]);assertFalse(view[hero.pos+MagicalSight.DISTANCE+1]);assertEquals(com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.SECRET_DOOR,Dungeon.level.map[secret]);assertTrue(sight.desc().contains(MagicalSight.DISTANCE+" tiles"));
 }
 @Test public void invisibilityDescriptionQueriesPreserveIndependentSources(){
  Invisibility first=Buff.append(hero,Invisibility.class,4),second=Buff.append(hero,Invisibility.class,8);assertEquals(2,hero.invisible);for(int i=0;i<30;i++)assertTrue(MechanicsDetails.buff(first,first.desc()).contains("last position"));assertEquals(2,hero.invisible);first.detach();assertEquals(1,hero.invisible);assertSame(second,hero.buff(Invisibility.class));
 }
 @Test public void invulnerabilityBlocksActualDamageWithoutPretendingAllStatusImmunity()throws Exception{
  PlainStatusTarget target=new PlainStatusTarget();target.sprite=quietSprite();Invulnerability protect=Buff.affect(target,Invulnerability.class,3);Barrier shield=Buff.affect(target,Barrier.class);shield.setShield(20);target.damage(15,new Object());assertEquals(100,target.HP);assertEquals(20,shield.shielding());assertFalse(target.isImmune(Poison.class));assertNotNull(Buff.affect(target,Roots.class));assertTrue(target.rooted);assertTrue(MechanicsDetails.buff(protect,protect.desc()).contains("before resistance, shielding"));
 }

 public static class MindVisionObject extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat {public MindVisionObject(){properties.add(com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.OBJECT);}}
 @Test public void mindVisionDescriptionMatchesRealCreatureSensingExclusions()throws Exception{
  Dungeon.level.blobs=new java.util.HashMap<>();Dungeon.level.mobs=new java.util.HashSet<>();Dungeon.level.heaps=new com.watabou.utils.SparseArray<>();Dungeon.level.traps=new com.watabou.utils.SparseArray<>();Dungeon.level.plants=new com.watabou.utils.SparseArray<>();Dungeon.level.customTiles=new java.util.ArrayList<>();Dungeon.level.customWalls=new java.util.ArrayList<>();Dungeon.level.setSize(33,33);Dungeon.level.buildFlagMaps();Dungeon.level.discoverable=new boolean[Dungeon.level.length()];java.util.Arrays.fill(Dungeon.level.discoverable,true);hero.pos=16*33+16;hero.viewDistance=1;MindVision sense=Buff.affect(hero,MindVision.class,8);
  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat ordinary=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();ordinary.pos=hero.pos+10;Dungeon.level.mobs.add(ordinary);
  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic mimic=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic();mimic.pos=hero.pos-10;mimic.alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL;java.lang.reflect.Field stealth=mimic.getClass().getDeclaredField("stealthy");stealth.setAccessible(true);stealth.setBoolean(mimic,true);Dungeon.level.mobs.add(mimic);
  MindVisionObject object=new MindVisionObject();object.pos=hero.pos+10*33;Dungeon.level.mobs.add(object);boolean[] view=new boolean[Dungeon.level.length()];Dungeon.level.updateFieldOfView(hero,view);assertTrue(view[ordinary.pos]);assertFalse(view[mimic.pos]);assertFalse(view[object.pos]);assertTrue(MechanicsDetails.buff(sense,sense.desc()).contains("stealthy mimics"));
 }

 @Test public void spellDescriptionsReadCurrentCostWithoutCastingOrConsumingMealBonus()throws Exception{
  hero.sprite=quietSprite();hero.talents.get(1).put(Talent.SATIATED_SPELLS,1);Talent.SatiatedSpellsTracker ready=Buff.affect(hero,Talent.SatiatedSpellsTracker.class);com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome tome=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome();tome.directCharge(3);int before=tome.currentCharges();float time=hero.cooldown();
  Random.pushGenerator(512);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(512);try{for(int i=0;i<100;i++){String text=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE,hero);assertTrue(text.contains("offers 3 Barrier points"));assertTrue(text.contains("cost: 2 charge units"));}assertEquals(expected,Random.Int());}finally{Random.popGenerator();}assertEquals(before,tome.currentCharges());assertSame(ready,hero.buff(Talent.SatiatedSpellsTracker.class));assertEquals(time,hero.cooldown(),0);
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE.onSpellCast(tome,hero);assertEquals(before-2,tome.currentCharges());assertNull(hero.buff(Talent.SatiatedSpellsTracker.class));assertEquals(3,hero.buff(Barrier.class).shielding());
 }
 @Test public void priestGuidingLightCostDescriptionTracksRealFreeCastCooldown(){
  hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.PRIEST;com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight spell=com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight.INSTANCE;assertEquals(0,spell.chargeUse(hero),0);assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.summary(spell,hero).contains("cost: 0 Tome charge units"));Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight.GuidingLightPriestCooldown.class,50);assertEquals(1,spell.chargeUse(hero),0);assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.summary(spell,hero).contains("cost: 1 Tome charge units"));
 }
 public static class UndeadSpellTarget extends PlainStatusTarget {public UndeadSpellTarget(){properties.add(com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.UNDEAD);}}
 @Test public void smiteExplanationMatchesActualUndeadMaximumAcrossHeroLevels(){
  UndeadSpellTarget undead=new UndeadSpellTarget();for(int level:new int[]{1,5,20,30}){hero.lvl=level;int actual=com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Smite.bonusDmg(hero,undead);String text=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Smite.INSTANCE,hero);assertEquals(10+level,actual);assertTrue(text.contains((5+level/2)+"–"+actual+" points"));}
 }
 @Test public void judgementExplanationRefreshesCounterWithoutSpendingItOrGrantingShield(){
  hero.talents.get(2).put(Talent.JUDGEMENT,2);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.AscendedForm.AscendBuff ascend=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.AscendedForm.AscendBuff.class);for(int count:new int[]{0,3,7}){ascend.spellCasts=count;int minimum=15+Math.round(15*count/3f);for(int i=0;i<30;i++)assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Judgement.INSTANCE,hero).contains("Current raw range: "+minimum+"–"+(2*minimum)));assertEquals(count,ascend.spellCasts);assertEquals(0,ascend.shielding());}
 }
 @Test public void auraDamageExplanationMatchesActualAllyDamageStage(){
  Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.AuraOfProtection.AuraBuff.class);for(int rank=1;rank<=4;rank++){hero.talents.get(2).put(Talent.AURA_OF_PROTECTION,rank);PlainStatusTarget ally=new PlainStatusTarget();ally.alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY;ally.pos=hero.pos;ally.damage(20,new Object());assertEquals(100-Math.round(20*(.9f-.1f*rank)),ally.HP);assertTrue(com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.AuraOfProtection.INSTANCE,hero).contains("= "+ObfuscationExplanation.number(.9f-.1f*rank)+" at this aura"));}
 }
 @Test public void holySpellPreviewDoesNotRevealUnknownArcanaLevel(){
  RingOfArcana ring=ring(0);ring.levelKnown=false;String ward=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard.INSTANCE,hero),weapon=com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE,hero);ring.level(9);assertEquals(ward,com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard.INSTANCE,hero));assertEquals(weapon,com.shatteredpixel.shatteredpixeldungeon.apd.mechanics.SpellExplanation.details(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.INSTANCE,hero));
 }

 @Test public void activeHolyBuffExplanationsMatchActualArcanaScaledWeaponAndArmorProcs(){
  com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();sword.levelKnown=sword.cursedKnown=true;hero.belongings.weapon=sword;
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.HolyWepBuff weaponBuff=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon.HolyWepBuff.class,50);com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard.HolyArmBuff armorBuff=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard.HolyArmBuff.class,50);RingOfArcana ring=ring(0);
  for(int level=0;level<=2;level++){ring.level(level);StatusDamageObserver enemy=new StatusDamageObserver();sword.proc(hero,enemy,10);assertTrue(MechanicsDetails.buff(weaponBuff,weaponBuff.desc()).contains("offers "+enemy.received+" raw magic damage points"));int block=10-armor.proc(enemy,hero,10);assertTrue(MechanicsDetails.buff(armorBuff,armorBuff.desc()).contains("offers "+block+" blocking points when active"));}
 }

 public static class SelectedEnemyProbe extends com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator {public com.shatteredpixel.shatteredpixeldungeon.actors.Char selected;@Override public com.shatteredpixel.shatteredpixeldungeon.actors.Char target(){return selected;}}
 @Test public void alternateShieldOfLightDescriptionMatchesActualSelectedEnemyDefense()throws Exception{
  java.lang.reflect.Field field=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");field.setAccessible(true);Object unsafe=field.get(null);SelectedEnemyProbe probe=(SelectedEnemyProbe)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,SelectedEnemyProbe.class);com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator previous=com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance;
  try{com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance=probe;PlainStatusTarget enemy=new PlainStatusTarget();probe.selected=enemy;
   for(int rank=1;rank<=2;rank++){hero.talents.get(0).put(Talent.SHIELD_OF_LIGHT,rank);assertTrue(MechanicsDetails.talent(Talent.SHIELD_OF_LIGHT,rank,"").contains(ObfuscationExplanation.number(100f*(1+rank)/6f)+"% for that matching attack"));
    for(int seed=0;seed<50;seed++){Random.pushGenerator(seed);boolean blocks=Random.Int(6)<1+rank;Random.popGenerator();Random.pushGenerator(seed);try{assertEquals(blocks?9:10,hero.defenseProc(enemy,10));}finally{Random.popGenerator();}}
    assertEquals(10,hero.defenseProc(new PlainStatusTarget(),10));
   }
  }finally{com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance=previous;}
 }
 public static class ContestScoreTarget extends PlainStatusTarget {@Override public int attackSkill(com.shatteredpixel.shatteredpixeldungeon.actors.Char other){return 20;}@Override public int defenseSkill(com.shatteredpixel.shatteredpixeldungeon.actors.Char other){return 20;}}
 @Test public void alternateBlessDescriptionMatchesActualAlliedAccuracyAndEvasionRolls(){
  ContestScoreTarget ally=new ContestScoreTarget(),enemy=new ContestScoreTarget();ally.alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY;enemy.alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY;
  for(int rank=1;rank<=2;rank++){hero.talents.get(1).put(Talent.BLESS,rank);float factor=1.01f+.02f*rank;assertTrue(MechanicsDetails.talent(Talent.BLESS,rank,"").contains("= "+ObfuscationExplanation.number(factor)));
   for(int seed=0;seed<50;seed++){Random.pushGenerator(seed);boolean hit=Random.Float(20)*factor>=Random.Float(20);Random.popGenerator();Random.pushGenerator(seed);try{assertEquals(hit,com.shatteredpixel.shatteredpixeldungeon.actors.Char.hit(ally,enemy,1,false));}finally{Random.popGenerator();}
    Random.pushGenerator(seed);boolean enemyHit=Random.Float(20)>=Random.Float(20)*factor;Random.popGenerator();Random.pushGenerator(seed);try{assertEquals(enemyHit,com.shatteredpixel.shatteredpixeldungeon.actors.Char.hit(enemy,ally,1,false));}finally{Random.popGenerator();}}
  }
 }
 @Test public void talentDetailsRespectExplicitAlternatePreviewEvenOnACleric(){
  hero.heroClass=HeroClass.CLERIC;String regular=MechanicsDetails.talent(Talent.SHIELD_OF_LIGHT,1,"",false),alternate=MechanicsDetails.talent(Talent.SHIELD_OF_LIGHT,1,"",true);assertTrue(regular.contains("block roll from 2 to 4"));assertTrue(alternate.contains("33.333%"));assertFalse(alternate.contains("block roll from 2 to 4"));assertTrue(MechanicsDetails.talent(Talent.BLESS,2,"",true).contains("1.05"));assertTrue(MechanicsDetails.talent(Talent.BLESS,2,"",false).contains("15 Barrier points"));
 }
 @Test public void heartyMealExplanationMatchesActualThresholdAndHealing()throws Exception{
  hero.sprite=quietSprite();
  for(int rank=1;rank<=2;rank++){hero.talents.get(0).put(Talent.HEARTY_MEAL,rank);hero.HT=100;hero.HP=33;Talent.onFoodEaten(hero,1,null);assertEquals(33+2+2*rank,hero.HP);assertTrue(MechanicsDetails.talent(Talent.HEARTY_MEAL,rank,"").contains((2+2*rank)+" HP of healing"));hero.HP=34;Talent.onFoodEaten(hero,1,null);assertEquals(34,hero.HP);}
 }
 @Test public void ironStomachExplanationMatchesRealDamageProtection(){
  Buff.affect(hero,Talent.WarriorFoodImmunity.class);hero.damageInterrupt=false;hero.HT=hero.HP=100;
  for(int rank=1;rank<=2;rank++){hero.talents.get(1).put(Talent.IRON_STOMACH,rank);hero.HP=100;hero.damage(20,new Object());assertEquals(rank==1?95:100,hero.HP);assertTrue(MechanicsDetails.talent(Talent.IRON_STOMACH,rank,"").contains(rank==1?"divided by four":"set to zero"));}
 }
 @Test public void holdFastDescriptionMatchesArmorRollAndActualDecayWithoutQuerySideEffects(){
  HoldFast hold=Buff.affect(hero,HoldFast.class);hold.pos=hero.pos;
  for(int rank=1;rank<=3;rank++){hero.talents.get(2).put(Talent.HOLD_FAST,rank);String text=MechanicsDetails.talent(Talent.HOLD_FAST,rank,"");assertTrue(text.contains(rank+"–"+(2*rank)+" damage points"));assertTrue(text.contains("decay by "+ObfuscationExplanation.number(HoldFast.buffDecayFactor(hero))));
   for(int seed=0;seed<30;seed++){Random.pushGenerator(seed);int expected=Random.NormalIntRange(rank,2*rank);Random.popGenerator();Random.pushGenerator(seed);try{assertEquals(expected,hold.armorBonus());}finally{Random.popGenerator();}}
  }
  hero.heroClass=HeroClass.ROGUE;hero.belongings.armor=null;assertEquals(0,hold.armorBonus());hold.pos=hero.pos+1;MechanicsDetails.talent(Talent.HOLD_FAST,3,"");assertSame(hold,hero.buff(HoldFast.class));
 }
 @Test public void intuitionDescriptionsMatchRealIdentificationFactorsAndStacking(){
  Talent[] types={Talent.VETERANS_INTUITION,Talent.ADVENTURERS_INTUITION,Talent.SCHOLARS_INTUITION,Talent.SURVIVALISTS_INTUITION,Talent.THIEFS_INTUITION};
  com.shatteredpixel.shatteredpixeldungeon.items.Item[] items={new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword(),new MailArmor(),new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile(),new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife(),new RingOfAccuracy()};
  for(Talent talent:types)for(int rank=1;rank<=2;rank++){for(Talent other:types)hero.talents.get(0).put(other,0);hero.talents.get(0).put(talent,rank);String text=MechanicsDetails.talent(talent,rank,"");assertTrue(text.contains("not damage"));for(int j=0;j<items.length;j++){float expected=1;
    if(talent==Talent.VETERANS_INTUITION)expected=j==0?1+.75f*rank:j==1?1+rank:1;
    if(talent==Talent.ADVENTURERS_INTUITION)expected=j==0?1+1.5f*rank:j==1?1+.75f*rank:1;
    if(talent==Talent.SCHOLARS_INTUITION&&j==2)expected=1+2*rank;
    if(talent==Talent.SURVIVALISTS_INTUITION&&j==3)expected=1+2*rank;
    if(talent==Talent.THIEFS_INTUITION&&j==4)expected=1+rank;
    assertEquals(expected,Talent.itemIDSpeedFactor(hero,items[j]),0);
   }
  }
  hero.talents.get(0).put(Talent.VETERANS_INTUITION,2);hero.talents.get(0).put(Talent.ADVENTURERS_INTUITION,2);assertEquals(10f,Talent.itemIDSpeedFactor(hero,items[0]),0);assertEquals(7.5f,Talent.itemIDSpeedFactor(hero,items[1]),0);
 }
 @Test public void mealEmpowerDescriptionsMatchActualBuffOffersAndReplacementRules(){
  for(int rank=1;rank<=2;rank++){hero.talents.get(0).put(Talent.EMPOWERING_MEAL,rank);hero.talents.get(0).put(Talent.STRENGTHENING_MEAL,rank);Talent.onFoodEaten(hero,1,null);WandEmpower wand=hero.buff(WandEmpower.class);PhysicalEmpower physical=hero.buff(PhysicalEmpower.class);assertEquals(1+rank,wand.dmgBoost);assertEquals(3,wand.left);assertEquals(3,physical.dmgBoost);assertEquals(1+rank,physical.left);assertTrue(MechanicsDetails.talent(Talent.EMPOWERING_MEAL,rank,"").contains((1+rank)+" points per qualifying"));assertTrue(MechanicsDetails.talent(Talent.STRENGTHENING_MEAL,rank,"").contains("next "+(1+rank)));assertTrue(MechanicsDetails.buff(physical,physical.desc()).contains("+3 damage points"));}
  PhysicalEmpower physical=hero.buff(PhysicalEmpower.class);physical.set(1,2);assertEquals(3,physical.dmgBoost);assertEquals(3,physical.left);physical.set(5,2);assertEquals(5,physical.dmgBoost);assertEquals(2,physical.left);
  WandEmpower wand=hero.buff(WandEmpower.class);wand.set(1,2);assertEquals(1,wand.dmgBoost);assertEquals(3,wand.left);
 }
 @Test public void physicalEmpowerInspectionPreservesSavedStateAndHiddenWeaponLevel(){
  PhysicalEmpower physical=Buff.affect(hero,PhysicalEmpower.class);physical.set(4,7);com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();physical.storeInBundle(before);String first=MechanicsDetails.buff(physical,physical.desc());assertTrue(first.contains("7 rolls remain"));armor.levelKnown=false;armor.level(9);assertEquals(first,MechanicsDetails.buff(physical,physical.desc()));com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();physical.storeInBundle(after);assertEquals(before.toString(),after.toString());
 }
 @Test public void focusedMealAlternateDamageUsesLiveHeroLevelAndActualOffer(){
  for(int rank=1;rank<=2;rank++)for(int level:new int[]{1,7,20}){PhysicalEmpower old=hero.buff(PhysicalEmpower.class);if(old!=null)old.detach();hero.lvl=level;hero.talents.get(0).put(Talent.FOCUSED_MEAL,rank);Talent.onFoodEaten(hero,1,null);int amount=Math.round(level/(4f-rank));PhysicalEmpower buff=hero.buff(PhysicalEmpower.class);assertEquals(amount,buff.dmgBoost);assertEquals(amount>0?1:0,buff.left);assertTrue(MechanicsDetails.talent(Talent.FOCUSED_MEAL,rank,"").contains("+"+amount+" points"));}
  hero.heroClass=HeroClass.DUELIST;assertTrue(MechanicsDetails.talent(Talent.FOCUSED_MEAL,1,"",false).contains("0.667 weapon-ability charges"));assertTrue(MechanicsDetails.talent(Talent.FOCUSED_MEAL,1,"",true).contains("Alternate next-hit damage"));
 }
 @Test public void enlighteningMealTomeOfferMatchesActualFractionalChargeAndCap()throws Exception{
  hero.heroClass=HeroClass.CLERIC;java.lang.reflect.Field charge=com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.class.getDeclaredField("charge"),partial=com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.class.getDeclaredField("partialCharge");charge.setAccessible(true);partial.setAccessible(true);com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome tome=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome();hero.belongings.artifact=tome;
  for(int rank=1;rank<=2;rank++){charge.setInt(tome,0);partial.setFloat(tome,0);hero.talents.get(0).put(Talent.ENLIGHTENING_MEAL,rank);Talent.onFoodEaten(hero,1,null);assertEquals((rank+1)/3f,charge.getInt(tome)+partial.getFloat(tome),.00001f);assertTrue(MechanicsDetails.talent(Talent.ENLIGHTENING_MEAL,rank,"").contains(ObfuscationExplanation.number((rank+1)/3f)+" Tome charges"));assertTrue(MechanicsDetails.talent(Talent.ENLIGHTENING_MEAL,rank,"",true).contains((1+rank)+" game-time units"));}
  tome.directCharge(100);int full=charge.getInt(tome);Talent.onFoodEaten(hero,1,null);assertEquals(full,charge.getInt(tome));assertEquals(0,partial.getFloat(tome),0);
 }
 @Test public void endureExplanationMatchesActualProtectionAndPerHitStorageTruncation(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure();
  for(int rank=0;rank<=4;rank++){hero.talents.get(3).put(Talent.SHRUG_IT_OFF,rank);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure.EndureTracker tracker=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure.EndureTracker();tracker.target=hero;float factor=.5f*(float)Math.pow(.8f,rank);assertEquals(20*factor,tracker.adjustDamageTaken(20),.00001f);assertEquals(10,tracker.damageBonus);tracker.adjustDamageTaken(3);assertEquals(11,tracker.damageBonus);assertTrue(AbilityExplanation.effects(ability,hero).contains("current factor: "+ObfuscationExplanation.number(factor)));assertTrue(MechanicsDetails.buff(tracker,tracker.desc()).contains("stored retaliation amount 11 points"));}
 }
 @Test public void endureInspectorPreservesReadyDamageRollsAndSavedState(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure.EndureTracker tracker=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure.EndureTracker.class);tracker.enduring=false;tracker.damageBonus=7;tracker.hitsLeft=2;com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();tracker.storeInBundle(before);for(int i=0;i<30;i++){String text=MechanicsDetails.buff(tracker,tracker.desc());assertTrue(text.contains("+7 damage points on each of 2"));}com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();tracker.storeInBundle(after);assertEquals(before.toString(),after.toString());assertEquals(17,tracker.damageFactor(10),0);assertEquals(1,tracker.hitsLeft);assertEquals(17,tracker.damageFactor(10),0);assertNull(hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure.EndureTracker.class));
 }
 @Test public void shockwavePreviewUsesKnownStrengthAndTalentUnitsWithoutRevealingUnknownRings(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave();hero.STR=14;hero.talents.get(3).put(Talent.SHOCK_FORCE,2);hero.talents.get(3).put(Talent.STRIKING_WAVE,4);hero.talents.get(3).put(Talent.EXPANDING_WAVE,3);String text=AbilityExplanation.effects(ability,hero);assertTrue(text.contains("8 tiles in a 105 degree cone"));assertTrue(text.contains("base roll 9–18 damage points"));assertTrue(text.contains("13–25 points"));assertTrue(text.contains("current chance 100%"));assertTrue(text.contains("= 50%"));RingOfMight ring=new RingOfMight();ring.levelKnown=ring.cursedKnown=true;ring.anonymize();hero.belongings.ring=ring;ring.activate(hero);ring.level(2);assertTrue(AbilityExplanation.effects(ability,hero).contains("strength "+hero.STR()));ring.levelKnown=false;String hidden=AbilityExplanation.effects(ability,hero);ring.level(9);assertEquals(hidden,AbilityExplanation.effects(ability,hero));assertTrue(hidden.contains("Identify equipped rings"));
 }
 @Test public void leapExplanationNeverRollsOrExposesHiddenArmor(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap();hero.talents.get(3).put(Talent.BODY_SLAM,2);hero.talents.get(3).put(Talent.IMPACT_WAVE,3);armor.levelKnown=false;String text=AbilityExplanation.effects(ability,hero);assertTrue(text.contains("from 2 to 8 points"));assertTrue(text.contains("= 75%"));armor.level(8);assertEquals(text,AbilityExplanation.effects(ability,hero));Random.pushGenerator(1834);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(1834);try{for(int i=0;i<50;i++)AbilityExplanation.details(ability,hero);assertEquals(expected,Random.Int());}finally{Random.popGenerator();}
 }
 @Test public void shadowCloneDescriptionMatchesActualCreationHealthAndScores(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone();
  for(int rank=0;rank<=4;rank++)for(int level:new int[]{1,12,25}){hero.lvl=level;hero.talents.get(3).put(Talent.PERFECT_COPY,rank);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone.ShadowAlly clone=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone.ShadowAlly(level);String text=AbilityExplanation.effects(ability,hero);assertTrue(text.contains("= "+clone.HT+" maximum HP"));assertEquals(clone.HT,clone.HP);assertTrue(text.contains("= "+clone.attackSkill(hero)+"; these are opposed scores"));}
 }
 @Test public void smokeBombDescriptionMatchesActualLogHealthAndArmorRoll(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb();
  for(int rank=1;rank<=4;rank++){hero.talents.get(3).put(Talent.BODY_REPLACEMENT,rank);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb.NinjaLog log=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb.NinjaLog();assertTrue(AbilityExplanation.effects(ability,hero).contains("log with "+log.HT+" HP"));for(int seed=0;seed<20;seed++){Random.pushGenerator(seed);int expected=Random.NormalIntRange(0,Barkskin.currentLevel(log))+Random.NormalIntRange(rank,3*rank);Random.popGenerator();Random.pushGenerator(seed);try{assertEquals(expected,log.drRoll());}finally{Random.popGenerator();}}}
 }
 @Test public void deathMarkDescriptionMatchesActualDamageStageWithoutReadingEnemyStoredHp(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark();hero.talents.get(3).put(Talent.DEATHLY_DURABILITY,3);PlainStatusTarget target=new PlainStatusTarget();target.alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY;Buff.affect(target,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark.DeathMarkTracker.class);target.damage(20,new Object());assertEquals(75,target.HP);assertTrue(target.deathMarked);String text=AbilityExplanation.effects(ability,hero);assertTrue(text.contains("= 1.25")||text.contains("multiplied by 1.25"));assertTrue(text.contains("37.5%"));target.HP=62;assertEquals(text,AbilityExplanation.effects(ability,hero));
 }
 @Test public void rogueAbilityInspectionDoesNotConsumeHeroAttackBenefitsOrHiddenEquipment(){
  Talent.LiquidAgilACCTracker agility=Buff.affect(hero,Talent.LiquidAgilACCTracker.class);com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();hero.belongings.weapon=sword;sword.levelKnown=false;armor.levelKnown=false;com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone();String text=AbilityExplanation.effects(ability,hero);sword.level(12);armor.level(9);assertEquals(text,AbilityExplanation.effects(ability,hero));Random.pushGenerator(728);int expected=Random.Int();Random.popGenerator();Random.pushGenerator(728);try{for(int i=0;i<50;i++)AbilityExplanation.effects(ability,hero);assertEquals(expected,Random.Int());}finally{Random.popGenerator();}assertSame(agility,hero.buff(Talent.LiquidAgilACCTracker.class));
 }
 @Test public void naturesPowerDescriptionMatchesActualMovementAndBowRates()throws Exception{
  hero.sprite=quietSprite();com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow bow=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow();float movement=hero.speed(),delay=bow.delayFactor(hero);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower.naturesPowerTracker power=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower.naturesPowerTracker.class);
  for(int rank=0;rank<=4;rank++){hero.talents.get(3).put(Talent.GROWING_POWER,rank);assertEquals(movement*(2+.25f*rank),hero.speed(),.00001f);assertEquals(delay/(1+(8+rank)/24f),bow.delayFactor(hero),.00001f);String text=MechanicsDetails.buff(power,power.desc());assertTrue(text.contains("= "+ObfuscationExplanation.number(2+.25f*rank)));assertTrue(text.contains("= "+ObfuscationExplanation.number((8+rank)/24f)));}
 }
 @Test public void naturesPowerExtensionDescriptionMatchesRealLimitedExtensionAndInspection(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower.naturesPowerTracker power=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower.naturesPowerTracker.class);float initial=power.cooldown();power.extend(0);assertEquals(2,power.extensionsLeft);power.extend(3);assertEquals(initial+3,power.cooldown(),0);assertEquals(1,power.extensionsLeft);for(int i=0;i<20;i++)assertTrue(MechanicsDetails.buff(power,power.desc()).contains("opportunities: 1"));assertEquals(initial+3,power.cooldown(),0);power.extend(3);power.extend(3);assertEquals(initial+6,power.cooldown(),0);assertEquals(0,power.extensionsLeft);
 }
 @Test public void hawkDescriptionMatchesActualCreationStatsAndDoesNotSpendGuaranteedDodges(){
  hero.talents.get(3).put(Talent.EAGLE_EYE,4);hero.talents.get(3).put(Talent.SWIFT_SPIRIT,2);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk.HawkAlly hawk=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk.HawkAlly();String text=AbilityExplanation.effects(new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk(),hero);assertEquals(10,hawk.HT);assertEquals(60,hawk.attackSkill(hero));assertEquals(8,hawk.viewDistance);assertTrue(text.contains("current 8"));assertTrue(text.contains("= 10 tiles"));assertTrue(text.contains("factor is 2 + Swift Spirit rank / 2 = 3"));for(int i=0;i<50;i++)AbilityExplanation.effects(new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk(),hero);for(int i=0;i<4;i++)assertEquals(com.shatteredpixel.shatteredpixeldungeon.actors.Char.INFINITE_EVASION,hawk.defenseSkill(new PlainStatusTarget()));assertNotEquals(com.shatteredpixel.shatteredpixeldungeon.actors.Char.INFINITE_EVASION,hawk.defenseSkill(new PlainStatusTarget()));
 }
 @Test public void wildMagicDescriptionMatchesActualFractionalPaymentAndTimeCheck()throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic();java.lang.reflect.Method after=ability.getClass().getDeclaredMethod("afterZap",com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand.class,java.util.ArrayList.class,Hero.class,int.class);after.setAccessible(true);
  for(int rank=0;rank<=4;rank++)for(int seed=0;seed<8;seed++){hero.talents.get(3).put(Talent.CONSERVED_MAGIC,rank);com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.curCharges=2;wand.partialCharge=.2f;float start=hero.cooldown(),cost=.5f*(float)Math.pow(.67f,rank);Random.pushGenerator(seed);boolean spend=Random.Int(4)>=rank;Random.popGenerator();Random.pushGenerator(seed);try{after.invoke(ability,wand,new java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand>(),hero,hero.pos);}finally{Random.popGenerator();}assertEquals(2.2f-cost,wand.curCharges+wand.partialCharge,.00001f);assertEquals(start+(spend?1:0),hero.cooldown(),.00001f);assertTrue(AbilityExplanation.effects(ability,hero).contains("= "+ObfuscationExplanation.number(cost)+" wand charges"));}
 }
 @Test public void warpBeaconDescriptionSeparatesPlacementFromReturnWithoutReadingOccupants(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon();hero.talents.get(3).put(Talent.REMOTE_BEACON,3);hero.talents.get(3).put(Talent.LONGRANGE_WARP,2);assertTrue(AbilityExplanation.summary(ability,hero).contains("without deducting armor charge"));String text=AbilityExplanation.effects(ability,hero);assertTrue(text.contains("= 12 tiles"));assertTrue(text.contains("= "+ObfuscationExplanation.number(1.833f-.333f*2)));float time=hero.cooldown();assertEquals(text,AbilityExplanation.effects(ability,hero));assertEquals(time,hero.cooldown(),0);
 }
 @Test public void elementalBlastDescriptionsUseActualTypeFactorsAndDoNotDependOnHiddenLevels()throws Exception{
  java.lang.reflect.Field factors=com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast.class.getDeclaredField("damageFactors");factors.setAccessible(true);java.util.Map<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand>,Float> actual=(java.util.Map<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand>,Float>)factors.get(null);assertEquals(13,actual.size());
  for(java.util.Map.Entry<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand>,Float> entry:actual.entrySet()){com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand=entry.getKey().getConstructor().newInstance();com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=describedStaff(wand,0);hero.belongings.weapon=staff;staff.levelKnown=false;wand.levelKnown=false;
   for(int rank=0;rank<=4;rank++){hero.talents.get(3).put(Talent.ELEMENTAL_POWER,rank);float power=1+.25f*rank;String text=BlastExplanation.details(hero);assertEquals(entry.getValue(),com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast.blastDamageFactor(entry.getKey()),0);assertTrue(text.contains("Current initial range: "+Math.round(15*power*entry.getValue())+"–"+Math.round(25*power*entry.getValue())));staff.level(10);wand.level(12);assertEquals(text,BlastExplanation.details(hero));}
  }
 }
 @Test public void elementalBlastWardRepairExplanationMatchesActualSentryCapsAndWardZapRestoration()throws Exception{
  com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding.Ward ward=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding.Ward();float power=1.5f;for(int tier=1;tier<=6;tier++){ward.tier=tier;ward.HT=100;ward.HP=5;ward.totalZaps=4;Random.pushGenerator(667);int expected=tier==1?0:tier==2?Math.round(power):tier==3?Math.round(Random.IntRange(1,2)*power):Math.round((tier==4?9:tier==5?12:16)*power);Random.popGenerator();Random.pushGenerator(667);try{ward.wandHeal(0,power);}finally{Random.popGenerator();}if(tier<=3)assertEquals(Math.max(0,4-expected),ward.totalZaps);else assertEquals(5+expected,ward.HP);}
  hero.belongings.weapon=describedStaff(new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding(),0);assertTrue(BlastExplanation.details(hero).contains("available zaps"));assertTrue(BlastExplanation.details(hero).contains("9, 12 or 16"));
 }
 @Test public void ascendedFormExplanationMatchesResetShieldAndPreservesExistingCounters()throws Exception{
  hero.sprite=quietSprite();com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.AscendedForm.AscendBuff form=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.AscendedForm.AscendBuff.class);form.spellCasts=4;form.flashCasts=2;form.setShield(45);form.left=2;form.reset();assertEquals(45,form.shielding());assertEquals(10,form.left);assertEquals(4,form.spellCasts);assertEquals(2,form.flashCasts);String text=MechanicsDetails.buff(form,form.desc());assertTrue(text.contains("Form shield: 45 points"));assertTrue(text.contains("Counted spell casts: 4"));float time=hero.cooldown();form.act();assertEquals(9,form.left);assertEquals(time,hero.cooldown(),0);
 }
 @Test public void powerOfManyDescriptionMatchesActualIncomingDamageFactors(){
  for(int rank=0;rank<=4;rank++){hero.talents.get(3).put(Talent.LIFE_LINK,rank);PlainStatusTarget ally=new PlainStatusTarget();ally.alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY;Buff.affect(ally,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany.PowerBuff.class);ally.damage(20,new Object());assertEquals(85,ally.HP);Buff.affect(ally,com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.LifeLinkSpell.LifeLinkSpellBuff.class);ally.HP=100;ally.damage(20,new Object());assertEquals(100-Math.round(20*(.70f-.05f*rank)),ally.HP);assertTrue(AbilityExplanation.effects(new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany(),hero).contains("= "+ObfuscationExplanation.number(.70f-.05f*rank)+" while the linked"));}
 }
 @Test public void powerOfManyCreationDescriptionMatchesRealLightAllyStats(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany ability=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany();for(int level:new int[]{1,12,25}){hero.lvl=level;com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany.LightAlly ally=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany.LightAlly(level);assertEquals(80,ally.HT);String text=AbilityExplanation.effects(ability,hero);assertTrue(text.contains("base accuracy hero level + 9 = "+ally.attackSkill(hero)));}
 }
 @Test public void challengeInspectorReadsOnlyOwnTrackedDamageAndDoesNotAdvanceDuel(){
  com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge.DuelParticipant duel=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge.DuelParticipant.class);duel.addDamage(17);com.watabou.utils.Bundle before=new com.watabou.utils.Bundle();duel.storeInBundle(before);for(int i=0;i<50;i++){String text=MechanicsDetails.buff(duel,duel.desc());assertTrue(text.contains("HP/shield damage suffered: 17 points"));assertEquals(10,duel.remainingDuelUpdates());}com.watabou.utils.Bundle after=new com.watabou.utils.Bundle();duel.storeInBundle(after);assertEquals(before.toString(),after.toString());PlainStatusTarget enemy=new PlainStatusTarget();com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge.DuelParticipant other=Buff.affect(enemy,com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge.DuelParticipant.class);other.addDamage(123);assertFalse(MechanicsDetails.buff(other,other.desc()).contains("123"));
 }
 @Test public void feintExplanationMatchesActualDefenseTriggerDurations()throws Exception{
  hero.sprite=quietSprite();for(int rank=1;rank<=4;rank++){Buff.detach(hero,Haste.class);hero.talents.get(3).put(Talent.FEIGNED_RETREAT,rank);hero.talents.get(3).put(Talent.EXPOSE_WEAKNESS,rank);hero.talents.get(3).put(Talent.COUNTER_ABILITY,rank);com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint.AfterImage image=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint.AfterImage();PlainStatusTarget enemy=new PlainStatusTarget();enemy.alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY;String text=AbilityExplanation.effects(new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint(),hero);assertNull(hero.buff(Haste.class));assertEquals(0,image.defenseSkill(enemy));assertEquals(2f*rank,hero.buff(Haste.class).cooldown(),0);assertEquals(2f*rank,enemy.buff(Vulnerable.class).cooldown(),0);assertEquals(2f*rank,enemy.buff(Weakness.class).cooldown(),0);assertTrue(text.contains((2*rank)+" units of Haste"));assertTrue(text.contains("= "+ObfuscationExplanation.number(.375f*rank)+" weapon-ability charges"));}
 }
 @Test public void feintCounterRefundExplanationMatchesActualWeaponAbilityCompletion()throws Exception{
  java.lang.reflect.Method used=com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.class.getDeclaredMethod("afterAbilityUsed",Hero.class);used.setAccessible(true);com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword weapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.Charger charger=Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.Charger.class);
  for(int rank=1;rank<=4;rank++){charger.charges=0;charger.partialCharge=0;hero.talents.get(3).put(Talent.COUNTER_ABILITY,rank);Buff.affect(hero,Talent.CounterAbilityTacker.class);used.invoke(weapon,hero);assertEquals(.375f*rank,charger.charges+charger.partialCharge,0);assertNull(hero.buff(Talent.CounterAbilityTacker.class));}
 }
}
