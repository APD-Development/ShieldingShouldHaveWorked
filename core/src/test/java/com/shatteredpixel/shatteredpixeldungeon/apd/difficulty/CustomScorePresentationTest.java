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
import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;
public class CustomScorePresentationTest{
 Bundle data(CustomDungeonRules r){Bundle outer=new Bundle(),inner=new Bundle();r.store(inner);outer.put("apd_custom_rules",inner);CustomScoreSnapshot.forNewRun(r).store(outer);return outer;}
 @Test public void recordedSnapshotsDisplayExactProvisionalMultipliersForEverySlider(){Bundle neutral=data(CustomDungeonRules.defaults());assertEquals("1.0000×",CustomScorePresentation.multiplier(neutral));assertTrue(CustomScorePresentation.notice(neutral).contains("Provisional experimental"));for(CustomDungeonRules.Slider slider:CustomDungeonRules.Slider.values()){CustomDungeonRules selected=CustomDungeonRules.defaults().with(slider,slider.min);Bundle custom=data(selected);assertEquals(CustomRuleMultipliers.format(CustomRuleMultipliers.combined(selected)),CustomScorePresentation.multiplier(custom));assertTrue(CustomScorePresentation.notice(custom).contains("preserved separately"));}}
 @Test public void missingAndMalformedHistoricalSnapshotsNeverInventMultiplierOrRewriteData(){assertEquals("?",CustomScorePresentation.multiplier(null));assertEquals("?",CustomScorePresentation.multiplier(new Bundle()));Bundle invalid=new Bundle(),settings=new Bundle();settings.put("custom_rules_version",999);invalid.put("apd_custom_rules",settings);assertEquals("?",CustomScorePresentation.multiplier(invalid));assertTrue(CustomScorePresentation.notice(invalid).contains("could not be read"));assertEquals(999,invalid.getBundle("apd_custom_rules").getInt("custom_rules_version"));}
}
