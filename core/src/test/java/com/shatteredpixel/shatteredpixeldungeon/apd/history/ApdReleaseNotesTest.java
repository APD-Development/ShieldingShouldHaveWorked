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

package com.shatteredpixel.shatteredpixeldungeon.apd.history;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeIcons;

public class ApdReleaseNotesTest {
    private Path assets() {
        Path root=Paths.get(System.getProperty("apd.game.src")).toAbsolutePath();
        while(root!=null&&!Files.isDirectory(root.resolve("core/src/main/assets")))root=root.getParent();
        assertNotNull(root);return root.resolve("core/src/main/assets/apd/releases");
    }
    @Test public void everyCuratedReleaseHasConciseNativeSpriteNotes() throws Exception {
        List<ApdHistory.Build> releases=ApdReleaseNotes.releases(Files.readString(assets().resolve("index.tsv")));
        assertFalse(releases.isEmpty());
        for(ApdHistory.Build release:releases) {
            List<ApdHistory.Entry> entries=ApdReleaseNotes.entries(Files.readString(assets().resolve(release.id+".tsv")));
            assertFalse(entries.isEmpty());
            for(ApdHistory.Entry entry:entries) {
                if(entry.sprite.startsWith("ITEM_"))assertNotNull(entry.sprite,ApdSpriteRef.itemImage(entry.sprite));
                else if(entry.sprite.startsWith("ICON_"))Icons.valueOf(entry.sprite.substring(5));
                else ChangeIcons.valueOf(entry.sprite);
                assertFalse(entry.text.contains("github.com"));
            }
        }
    }
    @Test public void initialPreviewDoesNotInventAPublicReleaseOrDate() throws Exception {
        ApdHistory.Build preview=ApdReleaseNotes.releases(Files.readString(assets().resolve("index.tsv"))).get(0);
        assertEquals("APD Alpha Preview",preview.label);assertEquals("",preview.date);
        assertEquals("In development; not a public release.",preview.status);
    }
    @Test public void technicalLabelsKeepOriginalIdentifiersUntouched() {
        ApdHistory.Build build=ApdHistory.builds("codex-test-222\tCodex TEST 222\t2026-10-15\tAPK change record\n").get(0);
        assertEquals("APD Test 222",ApdReleaseNotes.technicalLabel(build));
        assertEquals("Codex TEST 222",build.label);assertEquals("codex-test-222",build.id);
    }
    @Test(expected=IllegalArgumentException.class) public void toolBrandedReleaseIsRejected() {
        ApdReleaseNotes.releases("preview\tAPD Codex TEST 222\t\tPreview\n");
    }
    @Test(expected=IllegalArgumentException.class) public void internalDevelopmentCategoriesAreRejected() {
        ApdReleaseNotes.entries("Testing\tICON_INFO\tInternal checks\tRan internal checks.\n");
    }
    @Test(expected=IllegalArgumentException.class) public void duplicatePlayerDescriptionsAreRejected() {
        ApdReleaseNotes.entries("Bug Fixes\tICON_INFO\tOne\tA specific fix.\nQuality of Life\tICON_INFO\tTwo\tA specific fix.\n");
    }
    @Test(expected=IllegalArgumentException.class) public void previewCannotPretendToHaveAPublicationDate() {
        ApdReleaseNotes.releases("apd-alpha-preview\tAPD Alpha Preview\t2026-10-16\tPreview\n");
    }
}
