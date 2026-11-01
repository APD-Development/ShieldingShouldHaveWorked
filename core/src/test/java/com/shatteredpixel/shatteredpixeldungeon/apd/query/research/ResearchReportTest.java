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

package com.shatteredpixel.shatteredpixeldungeon.apd.query.research;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.report.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ResearchReportTest {
    @Test public void exportedFingerprintRetainsUnknownMarksDefinitionsAndSourceIdentities(){ResearchFixture f=new ResearchFixture();Row row=f.add("a","mage",0,0);row.clear(f.def.indexOf("combat.damageDealt"));QueryResult r=f.result();Report report=ResearchDocument.dna(r,RunDna.build(r,row)).report(1,"test",true);assertTrue(report.synthetic);String csv=ReportRenderer.csv(report);assertTrue(csv.contains("Damage dealt per 100 turns"));assertTrue(csv.contains("combat.damageDealt"));assertTrue(csv.contains("run-a"));assertTrue(csv.contains("?"));assertTrue(csv.toLowerCase(java.util.Locale.ROOT).contains("demo"));}
    @Test public void hiddenGenerationCannotAppearInExportedLuckDocument(){ResearchFixture f=new ResearchFixture();Row row=f.add("a","mage",0,0);f.num(row,"gen.items",912341);QueryResult r=f.result(f.query().withShowInternal(false));Report report=ResearchDocument.luck(r,RunLuck.build(r,row)).report(1,"test",false);assertFalse(ReportRenderer.csv(report).contains("912341"));assertTrue(ReportRenderer.csv(report).contains("Missing or hidden exact evidence"));assertTrue(report.blocks.size()>0);}
}
