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

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.*;
import org.junit.*;
import static org.junit.Assert.*;
public class CustomScoreRankingSnapshotTest{
 @Test public void realRankingSnapshotRetainsRunRulesAndOriginalScoreNotChangedPreferences()throws Exception{
  HeadlessTestSupport.start();CustomDungeonRules previous=SPDSettings.apdCustomRules();
  try{
   CustomDungeonRules selected=CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.UPGRADE_SCROLLS,60);SPDSettings.apdCustomRules(selected);RecorderTestSupport.Session session=new RecorderTestSupport.Session();RecorderTestSupport.newRun(session,182993);RecorderTestSupport.enterLevel(1);ApdRecorder.onRunEnded(false,new Object());Statistics.totalScore=12345;
   SPDSettings.apdCustomRules(CustomDungeonRules.defaults());Rankings.Record record=new Rankings.Record();record.score=12345;Rankings.INSTANCE.saveGameData(record);assertEquals(selected.preferenceText(),CustomDungeonRules.restore(record.gameData.getBundle("apd_custom_rules")).preferenceText());assertEquals(CustomRuleMultipliers.format(CustomRuleMultipliers.combined(selected)),CustomScorePresentation.multiplier(record.gameData));assertEquals(CustomRuleMultipliers.adjusted(12345,CustomRuleMultipliers.combined(selected)),record.earnedScore());assertEquals(12345,record.score);assertEquals(12345,Statistics.totalScore);
   assertEquals("?",CustomScorePresentation.multiplier(new com.watabou.utils.Bundle()));
  }finally{ApdRecorder.reset();SPDSettings.apdCustomRules(previous);}
 }
}
