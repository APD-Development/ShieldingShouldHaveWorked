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

package com.shatteredpixel.shatteredpixeldungeon.apd.stats;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import static org.junit.Assert.*;
public class CustomRuleTelemetryTest {
    @Rule public TemporaryFolder tmp=new TemporaryFolder();
    private RunFacts recorded(String text) throws Exception {
        File file=tmp.newFile();
        // Writer requires a new file rather than replacing an existing log.
        assertTrue(file.delete());
        RunLogWriter writer=RunLogWriter.create(file,TelemetryVersion.CURRENT,"custom-run","test",1);
        LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();
        start.runId="custom-run"; start.heroClass="WARRIOR";
        start.customRules=text; if(!text.isEmpty())try{com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomScoreSnapshot.forNewRun(CustomDungeonRules.fromPreferenceText(text)).recordTo(start);}catch(IllegalArgumentException deliberatelyInvalidFixture){} start.difficulty=3; start.exoticChance=0;
        assertTrue(LifecycleEvents.emitRunStart(writer,0,false,start));
        assertTrue(writer.close());
        ReadResult read=RunLogReader.read(file);
        assertEquals(text,LifecycleEvents.decodeRunStart(read.events.get(0),read).customRules);
        return RunFactsBuilder.fromLog(read,"custom-run");
    }
    @Test public void allSixtyScrollCountsHavePermanentExactSettings() throws Exception {
        for (int n=1;n<=60;n++) {
            CustomDungeonRules rules=CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.UPGRADE_SCROLLS,n);
            RunFacts facts=recorded(rules.preferenceText());
            for (CustomDungeonRules.Slider slider : CustomDungeonRules.Slider.values())
                assertEquals(rules.get(slider),facts.numOr(CustomRuleFacts.key(slider.name()),-1));
            assertEquals(100L*n,facts.numOr(CustomRuleFacts.SCROLL_PERCENT_NUMERATOR,-1));
            assertEquals(15,facts.numOr(CustomRuleFacts.SCROLL_PERCENT_DENOMINATOR,-1));
            assertEquals(rules.identityText(),facts.textOr(CustomRuleFacts.IDENTITY,""));
            assertEquals("experimental provisional",facts.textOr(CustomRuleFacts.SCORE_STATUS,""));
        }
    }
    @Test public void missingHistoricalSnapshotIsNeverAssignedDefaults() throws Exception {
        RunFacts facts=recorded("");
        for (CustomDungeonRules.Slider s : CustomDungeonRules.Slider.values())
            assertFalse(facts.isKnown(CustomRuleFacts.key(s.name())));
        assertFalse(facts.isKnown(CustomRuleFacts.IDENTITY));
        assertFalse(facts.isKnown(CustomRuleFacts.SCORE_STATUS));
    }
    @Test public void invalidOrFutureSnapshotDoesNotInventRules() throws Exception {
        for (String text : new String[]{"2,15","1,15","not-a-ruleset"}) {
            RunFacts facts=recorded(text);
            assertEquals("invalid recorded configuration",facts.textOr(CustomRuleFacts.SCORE_STATUS,""));
            assertFalse(facts.isKnown(CustomRuleFacts.VERSION));
            assertFalse(facts.isKnown(CustomRuleFacts.IDENTITY));
            for (CustomDungeonRules.Slider s : CustomDungeonRules.Slider.values())
                assertFalse(facts.isKnown(CustomRuleFacts.key(s.name())));
        }
    }
    @Test public void knownHistoricalSettingsWithoutScoringAuthorityNeverInventAMultiplier() throws Exception {
        File file=tmp.newFile();assertTrue(file.delete());RunLogWriter writer=RunLogWriter.create(file,TelemetryVersion.CURRENT,"older-scoring","test",1);
        LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();start.runId="older-scoring";start.customRules=CustomDungeonRules.defaults().preferenceText();assertTrue(LifecycleEvents.emitRunStart(writer,0,false,start));
        LifecycleEvents.RunEnd end=new LifecycleEvents.RunEnd();end.score=43210;assertTrue(LifecycleEvents.emitRunEnd(writer,1,end));assertTrue(writer.close());RunFacts f=RunFactsBuilder.fromLog(RunLogReader.read(file),"older-scoring");
        assertEquals("scoring metadata not recorded",f.textOr(CustomRuleFacts.SCORE_STATUS,""));assertFalse(f.isKnown(CustomRuleFacts.MULTIPLIER));assertFalse(f.isKnown(CustomRuleFacts.CALCULATION_VERSION));assertFalse(f.isKnown(CustomRuleFacts.ADJUSTED_SCORE));assertEquals(43210,f.numOr(CustomRuleFacts.ORIGINAL_SCORE,-1));assertEquals(15,f.numOr(CustomRuleFacts.key(CustomDungeonRules.Slider.UPGRADE_SCROLLS.name()),-1));
    }

    @Test public void invalidRecordedScoresCannotBreakHistoryReconstructionOrRewriteRawEvidence()throws Exception{
        for(long original:new long[]{-1,Long.MAX_VALUE}){
            File file=tmp.newFile();assertTrue(file.delete());RunLogWriter writer=RunLogWriter.create(file,TelemetryVersion.CURRENT,"invalid-score","test",1);
            CustomDungeonRules rules=CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.ENEMY_HP,500);
            LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();start.runId="invalid-score";start.customRules=rules.preferenceText();com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomScoreSnapshot.forNewRun(rules).recordTo(start);assertTrue(LifecycleEvents.emitRunStart(writer,0,false,start));
            LifecycleEvents.RunEnd end=new LifecycleEvents.RunEnd();end.score=original;end.customAdjustedKnown=true;end.customAdjustedScore=original;assertTrue(LifecycleEvents.emitRunEnd(writer,1,end));assertTrue(writer.close());
            byte[] raw=java.nio.file.Files.readAllBytes(file.toPath());RunFacts facts=RunFactsBuilder.fromLog(RunLogReader.read(file),"invalid-score");assertEquals(original,facts.numOr(CustomRuleFacts.ORIGINAL_SCORE,0));assertFalse(facts.isKnown(CustomRuleFacts.ADJUSTED_SCORE));assertArrayEquals(raw,java.nio.file.Files.readAllBytes(file.toPath()));
        }
    }

}
