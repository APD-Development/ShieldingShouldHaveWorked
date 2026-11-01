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

package com.shatteredpixel.shatteredpixeldungeon.apd;

import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.GameSettings;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

public class HeadlessSettingsStorageTest {
    @Test public void gamePreferencesAreWrittenInsideTheThrowAwayFolder() throws Exception {
        HeadlessTestSupport.start();
        String key = "apd-test-storage-isolation";
        GameSettings.put(key, "temporary");
        File settings = new File(HeadlessTestSupport.tempDir(), "prefs/settings.xml");
        assertTrue(settings.isFile());
        assertTrue(settings.getCanonicalPath().startsWith(HeadlessTestSupport.tempDir().getCanonicalPath() + File.separator));
        assertEquals("temporary", new HeadlessPreferences(new FileHandle(settings)).getString(key));
    }
}
