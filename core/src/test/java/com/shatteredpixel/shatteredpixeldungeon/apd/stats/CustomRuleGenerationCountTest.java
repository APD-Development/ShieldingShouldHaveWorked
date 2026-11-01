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
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.*;
import static org.junit.Assert.*;
public class CustomRuleGenerationCountTest{
 @Rule public TemporaryFolder tmp=new TemporaryFolder();
 @Test public void generatedQuantitiesAreCountedOncePerPhysicalItemWithoutInventingOlderData()throws Exception{
  File f=new File(tmp.getRoot(),"counts.apdlog");RunLogWriter w=RunLogWriter.create(f,TelemetryVersion.CURRENT,"counts","test",1);LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();start.runId="counts";assertTrue(LifecycleEvents.emitRunStart(w,0,false,start));
  GenerationEvents.ItemGenerated upgrade=new GenerationEvents.ItemGenerated();upgrade.uid="upgrade";upgrade.trueClass="com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade";upgrade.quantity=3;assertTrue(GenerationEvents.emitItemGenerated(w,1,upgrade));assertTrue(GenerationEvents.emitItemGenerated(w,2,upgrade));
  GenerationEvents.ItemGenerated armor=new GenerationEvents.ItemGenerated();armor.uid="armor";armor.trueClass="com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor";assertTrue(GenerationEvents.emitItemGenerated(w,3,armor));GenerationEvents.ItemGenerated potion=new GenerationEvents.ItemGenerated();potion.uid="potion";potion.trueClass="com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing";assertTrue(GenerationEvents.emitItemGenerated(w,4,potion));assertTrue(w.close());RunFacts facts=RunFactsBuilder.fromLog(RunLogReader.read(f),"counts");assertEquals(3,facts.numOr(CustomRuleFacts.GENERATED_UPGRADES,-1));assertEquals(1,facts.numOr(CustomRuleFacts.GENERATED_EQUIPMENT,-1));assertFalse(facts.isKnown(CustomRuleFacts.IDENTITY));
  File old=new File(tmp.getRoot(),"older.apdlog");RunLogWriter legacy=RunLogWriter.create(old,new TelemetryVersion(1,3),"older","test",1);assertTrue(LifecycleEvents.emitRunStart(legacy,0,false,start));assertTrue(legacy.close());RunFacts historical=RunFactsBuilder.fromLog(RunLogReader.read(old),"older");assertFalse(historical.isKnown(CustomRuleFacts.GENERATED_UPGRADES));assertFalse(historical.isKnown(CustomRuleFacts.GENERATED_EQUIPMENT));
 }
}
