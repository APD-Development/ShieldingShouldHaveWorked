package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;
public class CustomScorePresentationTest{
 Bundle data(CustomDungeonRules r){Bundle outer=new Bundle(),inner=new Bundle();r.store(inner);outer.put("apd_custom_rules",inner);CustomScoreSnapshot.forNewRun(r).store(outer);return outer;}
 @Test public void onlyRecordedNeutralRulesDisplayIdentityAndEveryChangedSliderIsPending(){Bundle neutral=data(CustomDungeonRules.defaults());assertEquals("1.00×",CustomScorePresentation.multiplier(neutral));assertEquals("",CustomScorePresentation.notice(neutral));for(CustomDungeonRules.Slider slider:CustomDungeonRules.Slider.values()){Bundle custom=data(CustomDungeonRules.defaults().with(slider,slider==CustomDungeonRules.Slider.UPGRADE_SCROLLS?60:slider.min));assertEquals("Pending",CustomScorePresentation.multiplier(custom));assertTrue(CustomScorePresentation.notice(custom).contains("Original score unchanged"));}}
 @Test public void missingAndMalformedHistoricalSnapshotsNeverInventMultiplierOrRewriteData(){assertEquals("?",CustomScorePresentation.multiplier(null));assertEquals("?",CustomScorePresentation.multiplier(new Bundle()));Bundle invalid=new Bundle(),settings=new Bundle();settings.put("custom_rules_version",999);invalid.put("apd_custom_rules",settings);assertEquals("?",CustomScorePresentation.multiplier(invalid));assertTrue(CustomScorePresentation.notice(invalid).contains("could not be read"));assertEquals(999,invalid.getBundle("apd_custom_rules").getInt("custom_rules_version"));}
}
