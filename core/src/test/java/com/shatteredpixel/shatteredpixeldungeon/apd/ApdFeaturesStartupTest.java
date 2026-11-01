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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ApdFeaturesStartupTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private Files previousFiles;

    @Before public void beforeFileServiceExists() throws Exception {
        previousFiles = Gdx.files;
        Gdx.files = null;
        ApdFeatures.resetForTest();
        Field loaded = ApdFeatures.class.getDeclaredField("loaded");
        loaded.setAccessible(true);
        loaded.setBoolean(null, false);
    }

    @After public void restore() {
        ApdFeatures.resetForTest();
        Gdx.files = previousFiles;
    }

    @Test public void earlyCheckDoesNotHideThePrivateBuildAsset() throws Exception {
        assertFalse(ApdFeatures.demoUnlock());
        assertFalse(ApdFeatures.testBuild());
        File asset = folder.newFile("features.txt");
        java.nio.file.Files.write(asset.toPath(), "testBuild\ndemoUnlock\n".getBytes(StandardCharsets.UTF_8));
        installFileService(asset);
        assertTrue(ApdFeatures.testBuild());
        assertTrue(ApdFeatures.demoUnlock());
        assertFalse(ApdFeatures.clericPlayable());
        assertFalse(ApdFeatures.grudge());
    }

    @Test public void normalBuildWithoutTheAssetStaysOff() {
        assertFalse(ApdFeatures.demoUnlock());
        installFileService(new File(folder.getRoot(), "absent.txt"));
        assertFalse(ApdFeatures.demoUnlock());
        assertFalse(ApdFeatures.testBuild());
        assertFalse(ApdFeatures.clericPlayable());
        assertFalse(ApdFeatures.grudge());
    }

    @Test public void unreadableAssetDoesNotEnablePrivateFeatures() throws Exception {
        assertFalse(ApdFeatures.demoUnlock());
        installFileService(folder.newFolder("not-a-readable-file"));
        assertFalse(ApdFeatures.demoUnlock());
        assertFalse(ApdFeatures.testBuild());
        assertFalse(ApdFeatures.clericPlayable());
    }

    private void installFileService(File asset) {
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(), new Class<?>[]{Files.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("internal")) {
                        assertEquals("apd/features.txt", arguments[0]);
                        return new FileHandle(asset);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
