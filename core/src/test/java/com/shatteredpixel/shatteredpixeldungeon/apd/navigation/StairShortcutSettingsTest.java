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

package com.shatteredpixel.shatteredpixeldungeon.apd.navigation;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.watabou.utils.GameSettings;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

public class StairShortcutSettingsTest {
    @Test public void missingPreferenceShowsShortcutWithoutWritingPlayerData() throws Exception {
        withPreferences(prefs -> {
            assertTrue(SPDSettings.stairNavigationShortcut());
            assertFalse(prefs.contains("apd_stair_navigation_shortcut"));
        });
    }
    @Test public void autoEntryDefaultsOffAndCanBeSavedSeparately() throws Exception {
        withPreferences(prefs -> {
            assertFalse(SPDSettings.autoEnterStairs());
            assertFalse(prefs.contains("apd_auto_enter_stairs"));
            SPDSettings.autoEnterStairs(true);
            GameSettings.set(new HeadlessPreferences(prefs.getFile()));
            assertTrue(SPDSettings.autoEnterStairs());
            assertTrue(SPDSettings.stairNavigationShortcut());
            SPDSettings.autoEnterStairs(false);
            GameSettings.set(new HeadlessPreferences(prefs.getFile()));
            assertFalse(SPDSettings.autoEnterStairs());
            assertTrue(SPDSettings.stairNavigationShortcut());
        });
    }
    @Test public void explicitOffAndOnSurviveReload() throws Exception {
        withPreferences(prefs -> {
            SPDSettings.stairNavigationShortcut(false);
            GameSettings.set(new HeadlessPreferences(prefs.getFile()));
            assertFalse(SPDSettings.stairNavigationShortcut());
            SPDSettings.stairNavigationShortcut(true);
            GameSettings.set(new HeadlessPreferences(prefs.getFile()));
            assertTrue(SPDSettings.stairNavigationShortcut());
        });
    }
    private interface Check { void run(TestPreferences prefs); }
    private static class TestPreferences extends HeadlessPreferences {
        private final FileHandle file;
        TestPreferences(FileHandle file){super(file);this.file=file;}
        FileHandle getFile(){return file;}
    }
    private void withPreferences(Check check) throws Exception {
        HeadlessTestSupport.start();
        java.lang.reflect.Field field=GameSettings.class.getDeclaredField("prefs");
        field.setAccessible(true); Preferences old=(Preferences)field.get(null);
        File file=File.createTempFile("stair-preferences-", ".xml", HeadlessTestSupport.tempDir());
        file.delete();
        try { TestPreferences prefs=new TestPreferences(new FileHandle(file));GameSettings.set(prefs);check.run(prefs); }
        finally { GameSettings.set(old);file.delete(); }
    }
}
