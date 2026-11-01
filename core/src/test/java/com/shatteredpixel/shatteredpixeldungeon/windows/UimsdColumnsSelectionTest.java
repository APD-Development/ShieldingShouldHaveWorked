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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class UimsdColumnsSelectionTest {
    private UimsdModel model(){
        DatasetDef def = new DatasetDef("fixture", "Fixture", "Isolated checkbox test");
        def.add(ColumnDef.text("first", "First").shown());
        def.add(ColumnDef.text("second", "Second").shown());
        Catalog cat = new Catalog();
        cat.add(new Dataset(def, new ArrayList<Row>(), null));
        cat.add(new ViewDef("fixture", "Fixture", "", Query.of("fixture")));
        UimsdModel model = new UimsdModel("test", "Test");
        model.setCatalog(cat);
        model.selectView("fixture");
        return model;
    }

    private List<WndUimsdList.Entry> entries(UimsdModel model){
        List<WndUimsdList.Entry> entries = new ArrayList<>();
        entries.add(WndUimsdList.Entry.heading("Fields"));
        entries.add(WndUimsdList.Entry.item("First").checked(model.query().columns.contains("first")));
        entries.add(WndUimsdList.Entry.note("Notes do not consume item indices."));
        entries.add(WndUimsdList.Entry.item("Second").checked(model.query().columns.contains("second")));
        return entries;
    }

    @Test public void lastColumnRemainsCheckedWhenRemovalIsRefused(){
        UimsdModel model = model();
        model.setColumns(Collections.singletonList("first"));
        List<WndUimsdList.Entry> entries = entries(model);
        //WndUimsdList toggles the tapped entry before invoking the production callback.
        entries.get(1).checked(false);
        WndUimsdColumns.toggleSelection(model, model.offeredColumns(), entries, 0);
        assertEquals(Collections.singletonList("first"), model.query().columns);
        assertEquals(1, model.table().headers.size());
        assertEquals("a refused removal must stay visibly checked", Boolean.TRUE, entries.get(1).checked);
        assertEquals(Boolean.FALSE, entries.get(3).checked);
    }

    @Test public void deselectionAndReselectionMatchTheActualColumnsAcrossNotes(){
        UimsdModel model = model();
        model.setColumns(Arrays.asList("first", "second"));
        List<WndUimsdList.Entry> entries = entries(model);
        entries.get(1).checked(false);
        WndUimsdColumns.toggleSelection(model, model.offeredColumns(), entries, 0);
        assertEquals(Collections.singletonList("second"), model.query().columns);
        assertEquals(Boolean.FALSE, entries.get(1).checked);
        assertEquals(Boolean.TRUE, entries.get(3).checked);
        entries.get(1).checked(true);
        WndUimsdColumns.toggleSelection(model, model.offeredColumns(), entries, 0);
        assertEquals(Arrays.asList("second", "first"), model.query().columns);
        assertEquals(Boolean.TRUE, entries.get(1).checked);
        assertEquals(Boolean.TRUE, entries.get(3).checked);
    }
}
