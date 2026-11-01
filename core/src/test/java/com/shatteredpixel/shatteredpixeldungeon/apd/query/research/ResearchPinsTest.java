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
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
public class ResearchPinsTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void separatePopulationsRoundTripAllQueryChoicesAndFreezeCurrentRules()throws Exception{
        ResearchFixture f=new ResearchFixture();Query a=f.forClass("mage").withShowInternal(false),b=f.forClass("warrior");
        Query current=a.withScope(Scope.DEFAULT);ResearchPins p=new ResearchPins(null,null).with(true,current,"rules").with(false,b,"rules");
        File file=ResearchPins.file(temp.getRoot());p.store(file);ResearchPins read=ResearchPins.load(file);
        assertEquals(Scope.Rules.SPECIFIC,read.a.scope.rules);assertEquals("rules",read.a.scope.ruleset);assertEquals(a.filters,read.a.filters);assertFalse(read.a.showInternal);assertEquals(b,read.b);
    }
    @Test public void queryPinsDoNotModifyViewsHistoryOrOriginalQuery()throws Exception{
        File view=new File(temp.getRoot(),"saved-views.tsv"),history=new File(temp.getRoot(),"history.apd");Files.write(view.toPath(),new byte[]{1,2,3});Files.write(history.toPath(),new byte[]{4,5,6});
        Query q=Query.of("runs").addFilter(Filter.text("run.heroClass",Op.EQ,"mage"));ResearchPins p=new ResearchPins(null,null).with(true,q,"rules");q.filters.get(0).texts[0]="changed";
        assertEquals("mage",p.a.filters.get(0).texts[0]);p.store(ResearchPins.file(temp.getRoot()));assertArrayEquals(new byte[]{1,2,3},Files.readAllBytes(view.toPath()));assertArrayEquals(new byte[]{4,5,6},Files.readAllBytes(history.toPath()));
    }
    @Test public void tamperedCanonicalQueryFailsChecksumAndLeavesFileUntouched()throws Exception{
        File file=ResearchPins.file(temp.getRoot());new ResearchPins(Query.of("runs"),null).store(file);Properties properties=new Properties();try(InputStream in=new FileInputStream(file)){properties.load(in);}properties.setProperty("a",QueryCodec.encode(Query.of("items")));try(OutputStream out=new FileOutputStream(file)){properties.store(out,"");}
        byte[] before=Files.readAllBytes(file.toPath());try{ResearchPins.load(file);fail();}catch(IOException expected){}assertArrayEquals(before,Files.readAllBytes(file.toPath()));
    }
    @Test public void futureVersionsAndMalformedQueriesAreNotReinterpreted()throws Exception{
        File file=ResearchPins.file(temp.getRoot());Files.write(file.toPath(),"version=99\na=unreadable\n".getBytes("UTF-8"));byte[] before=Files.readAllBytes(file.toPath());try{ResearchPins.load(file);fail();}catch(IOException expected){}assertArrayEquals(before,Files.readAllBytes(file.toPath()));
    }
    @Test public void missingPinsAreEmptyAndUnknownCurrentRulesCannotBeFrozen()throws Exception{assertNull(ResearchPins.load(ResearchPins.file(temp.getRoot())).a);try{new ResearchPins(null,null).with(true,Query.of("runs"),"");fail();}catch(IllegalArgumentException expected){}}
}
