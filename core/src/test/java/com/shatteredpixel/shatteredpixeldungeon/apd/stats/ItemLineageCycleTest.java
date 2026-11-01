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

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class ItemLineageCycleTest {
    @Rule public TemporaryFolder tmp=new TemporaryFolder();
    private File log(String[][] edges) throws Exception {
        File file=new File(tmp.getRoot(),"lineage.apdlog");
        RunLogWriter writer=RunLogWriter.create(file,TelemetryVersion.CURRENT,"lineage","fixture",1);
        try {
            for(String[] edge:edges){
                ItemEvents.Lineage lineage=new ItemEvents.Lineage();
                lineage.kind=ItemEvents.LIN_TRANSFORM;lineage.from.add(edge[0]);lineage.to=edge[1];
                assertTrue(ItemEvents.emitLineage(writer,0,lineage));
            }
        } finally { writer.close(); }
        return file;
    }
    @Test public void aSelfReferenceDoesNotMakeAnItemItsOwnAncestor() throws Exception {
        File file=log(new String[][]{{"a","a"}});byte[] original=Files.readAllBytes(file.toPath());
        ReadResult read=RunLogReader.read(file);
        assertEquals(ReadStatus.SEALED,read.status);
        assertEquals(Collections.emptyList(),ItemHistory.ancestors(read.events,read,"a"));
        assertArrayEquals(original,Files.readAllBytes(file.toPath()));
    }
    @Test public void cyclicImportedHistoryKeepsRealAncestorsInOrderWithoutRepeatingTheRoot() throws Exception {
        File file=log(new String[][]{{"b","a"},{"a","b"},{"c","b"},{"d","c"}});
        byte[] original=Files.readAllBytes(file.toPath());ReadResult read=RunLogReader.read(file);
        assertEquals(Arrays.asList("b","c","d"),ItemHistory.ancestors(read.events,read,"a"));
        assertArrayEquals(original,Files.readAllBytes(file.toPath()));
    }
    @Test public void sparseLogsRetainIdentifiersFromEveryItemObservation() throws Exception {
        File file=new File(tmp.getRoot(),"sparse.apdlog");RunLogWriter writer=RunLogWriter.create(file,TelemetryVersion.CURRENT,"sparse","fixture",1);
        try {
            ItemEvents.Equip equip=new ItemEvents.Equip();equip.uid="equipped";assertTrue(ItemEvents.emitEquip(writer,1,0,true,equip));
            ItemEvents.Left left=new ItemEvents.Left();left.uid="left";assertTrue(ItemEvents.emitLeft(writer,2,0,left));
            ItemEvents.Knowledge known=new ItemEvents.Knowledge();known.uid="known";assertTrue(ItemEvents.emitKnowledge(writer,3,0,known));
            ItemEvents.TruthChanged truth=new ItemEvents.TruthChanged();truth.uid="truth";assertTrue(ItemEvents.emitTruthChanged(writer,4,0,truth));
            equip.uid="equipped";assertTrue(ItemEvents.emitEquip(writer,5,0,false,equip));
        } finally { writer.close(); }
        byte[] original=Files.readAllBytes(file.toPath());ReadResult read=RunLogReader.read(file);
        assertEquals(Arrays.asList("equipped","left","known","truth"),ItemHistory.allUids(read.events,read));
        assertArrayEquals(original,Files.readAllBytes(file.toPath()));
    }
    @Test public void lineageOnlyIdentifiersKeepTheirFirstAppearanceOrderAndAreNotLost() throws Exception {
        File file=log(new String[][]{{"parent","child"},{"child","grandchild"},{"parent","grandchild"}});
        ReadResult read=RunLogReader.read(file);
        assertEquals(Arrays.asList("child","parent","grandchild"),ItemHistory.allUids(read.events,read));
        assertEquals(Arrays.asList("child","parent"),ItemHistory.ancestors(read.events,read,"grandchild"));
    }
}
