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

package com.shatteredpixel.shatteredpixeldungeon.apd.archive;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static com.shatteredpixel.shatteredpixeldungeon.apd.archive.ArchiveSupport.*;

public class ArchiveManifestLookupCompatibilityTest {
    @Rule public TemporaryFolder tmp=new TemporaryFolder();
    @Test public void repeatedManifestPathKeepsFirstMatchAndNeverOverwritesHistory() throws Exception {
        byte[] log=smallLog(tmp.newFolder(),"a","run-a");
        ArchiveManifest manifest=new ArchiveManifest();
        ArchiveManifest.Entry first=new ArchiveManifest.Entry();
        first.path="runs/a.apdlog";first.size=log.length;
        first.sha256=ArchiveManifest.sha256Hex(log);first.runId="run-a";manifest.files.add(first);
        ArchiveManifest.Entry later=new ArchiveManifest.Entry();
        later.path=first.path;later.size=0;later.sha256=ArchiveManifest.sha256Hex(new byte[0]);
        later.runId=first.runId;manifest.files.add(later);
        byte[] archive=zip(Arrays.asList(ArchiveManifest.ENTRY_NAME,first.path),
                Arrays.asList(manifest.encode().getBytes(ArchiveManifest.utf8()),log));
        File root=tmp.newFolder();
        ArchiveImporter.Report imported=ArchiveImporter.importArchive(root,new ByteArrayInputStream(archive));
        assertTrue(imported.summary(),imported.ok);assertEquals(1,imported.imported);
        assertArrayEquals(log,Files.readAllBytes(new File(root,first.path).toPath()));
        ArchiveImporter.Report repeated=ArchiveImporter.importArchive(root,new ByteArrayInputStream(archive));
        assertTrue(repeated.summary(),repeated.ok);assertEquals(0,repeated.imported);
        assertEquals(1,repeated.skippedAlreadyHere);
        assertArrayEquals(log,Files.readAllBytes(new File(root,first.path).toPath()));
    }
}
