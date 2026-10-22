package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;
public class CustomScoreSnapshotTest{
 @Test public void neutralAndAllNonNeutralScrollCountsHaveFrozenExactAuthority(){for(int count=1;count<=60;count++){CustomDungeonRules rules=CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.UPGRADE_SCROLLS,count);CustomScoreSnapshot snapshot=CustomScoreSnapshot.forNewRun(rules);Bundle saved=new Bundle();snapshot.store(saved);CustomScoreSnapshot restored=CustomScoreSnapshot.restore(saved,rules);assertEquals(snapshot.status,restored.status);assertEquals(snapshot.calculationVersion,restored.calculationVersion);assertEquals(CustomRuleMultipliers.combined(rules).toPlainString(),restored.multiplierExact);assertEquals(Long.valueOf(CustomRuleMultipliers.adjusted(987654,CustomRuleMultipliers.combined(rules))),restored.adjustedIdentityScore(987654));}}
 @Test public void ownerApprovedSixtyScrollsUseTwentyPercentOnlyForNewRunsAndKeepHistoricExactScores(){
  CustomDungeonRules sixty=CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.UPGRADE_SCROLLS,60);
  CustomDungeonRules fiftyNine=CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.UPGRADE_SCROLLS,59);
  assertEquals("0.20",CustomRuleMultipliers.individual(CustomDungeonRules.Slider.UPGRADE_SCROLLS,60).toPlainString());
  assertEquals("0.5608735153608649",CustomRuleMultipliers.combinedForVersion(sixty,CustomRuleMultipliers.PREVIOUS_VERSION).toPlainString());
  assertEquals("0.5703113964318902",CustomRuleMultipliers.combined(fiftyNine).toPlainString());
  CustomScoreSnapshot historical=CustomScoreSnapshot.recorded(2,"EXPERIMENTAL",CustomRuleMultipliers.PREVIOUS_VERSION,
    "0.5608735153608649",sixty);
  Bundle historicSave=new Bundle();historical.store(historicSave);
  CustomScoreSnapshot restored=CustomScoreSnapshot.restore(historicSave,sixty);
  assertEquals(CustomRuleMultipliers.PREVIOUS_VERSION,restored.calculationVersion);
  assertEquals("0.5608735153608649",restored.multiplierExact);
  assertEquals(Long.valueOf(561),restored.adjustedIdentityScore(1000));
  CustomScoreSnapshot newRun=CustomScoreSnapshot.forNewRun(sixty);
  assertEquals(CustomRuleMultipliers.VERSION,newRun.calculationVersion);
  assertEquals("0.2",newRun.multiplierExact);
  assertEquals(Long.valueOf(200),newRun.adjustedIdentityScore(1000));
  assertEquals("0.5608735153608649",CustomScoreSnapshot.restore(historicSave,sixty).multiplierExact);
  assertEquals("0.5608735153608649",historicSave.getString("apd_custom_score_multiplier"));
  try{CustomScoreSnapshot.recorded(2,"EXPERIMENTAL",CustomRuleMultipliers.PREVIOUS_VERSION,"0.2",sixty);fail("historic formula accepted new multiplier");}catch(IllegalArgumentException expected){}
  try{CustomScoreSnapshot.recorded(2,"EXPERIMENTAL",CustomRuleMultipliers.VERSION,"0.5608735153608649",sixty);fail("new formula accepted historic multiplier");}catch(IllegalArgumentException expected){}
 }
 @Test public void missingHistoricalMetadataRemainsUnknownAndReusedDestinationCanBeCleared(){CustomDungeonRules rules=CustomDungeonRules.defaults();assertNull(CustomScoreSnapshot.restore(new Bundle(),rules));assertNull(CustomScoreSnapshot.forNewRun(null));Bundle reused=new Bundle();CustomScoreSnapshot.forNewRun(rules).store(reused);CustomScoreSnapshot.clearStored(reused);assertNull(CustomScoreSnapshot.restore(reused,rules));}
 @Test public void incompleteFutureAndContradictoryMetadataAreRejectedWithoutMutation(){Bundle saved=new Bundle();CustomScoreSnapshot.forNewRun(CustomDungeonRules.defaults()).store(saved);saved.put("apd_custom_score_version",999);try{CustomScoreSnapshot.restore(saved,CustomDungeonRules.defaults());fail();}catch(IllegalArgumentException expected){}assertEquals(999,saved.getInt("apd_custom_score_version"));try{CustomScoreSnapshot.recorded(1,"NEUTRAL_IDENTITY","neutral-identity-v1","1.00",CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.ENEMY_HP,200));fail();}catch(IllegalArgumentException expected){}saved.remove("apd_custom_score_calculation");try{CustomScoreSnapshot.restore(saved,CustomDungeonRules.defaults());fail();}catch(IllegalArgumentException expected){}}
}
