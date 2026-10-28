package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/** Reuse is immediate only for a complete, unchanged model. */
public class UimsdWarmCacheTest {
    @After public void cleanup() { UimsdWarmCache.resetForTest(); }

    @Test public void titlePrewarmProvidesAnImmediatelyReadyModel() throws Exception {
        File root = Files.createTempDirectory("apd-warm-title").toFile();
        try {
            UimsdWarmCache.prewarm(root, "rules-a", "label", false);
            UimsdWarmCache.awaitPrewarmForTest(10000);
            UimsdModel prepared = UimsdWarmCache.takeReady(root, "rules-a", "label", false);
            assertNotNull("title preload should hand over the actual ready model", prepared);
            assertEquals(UimsdModel.State.READY, prepared.state());
            assertNotNull(prepared.catalog());
            assertNotNull(prepared.preparedHome(prepared.catalog(), false));
            assertNull("cache must never give two owners one mutable model",
                    UimsdWarmCache.takeReady(root, "rules-a", "label", false));
        } finally {
            assertTrue(root.delete());
        }
    }

    @Test public void changingRawHistoryForcesFreshLoadingInsteadOfUsingStaleStats() throws Exception {
        File root = Files.createTempDirectory("apd-warm-stale").toFile();
        File runs = new File(root, "runs");
        try {
            UimsdWarmCache.prewarm(root, "rules-a", "label", false);
            UimsdWarmCache.awaitPrewarmForTest(10000);
            assertTrue(runs.mkdir());
            Files.write(new File(runs, "new-run.apdlog").toPath(), new byte[]{1,2,3});
            assertNull("new recorded log invalidates even a fully prepared model",
                    UimsdWarmCache.takeReady(root, "rules-a", "label", false));
        } finally {
            new File(runs, "new-run.apdlog").delete();
            runs.delete();
            assertTrue(root.delete());
        }
    }

    @Test public void changedRulesOrHomeScopeCannotReuseSomeoneElsesSummary() throws Exception {
        File root = Files.createTempDirectory("apd-warm-scope").toFile();
        try {
            UimsdWarmCache.prewarm(root, "rules-a", "label", true);
            UimsdWarmCache.awaitPrewarmForTest(10000);
            assertNull(UimsdWarmCache.takeReady(root, "rules-a", "label", false));
            assertNull(UimsdWarmCache.takeReady(root, "rules-b", "label", true));
            assertNotNull(UimsdWarmCache.takeReady(root, "rules-a", "label", true));
        } finally {
            assertTrue(root.delete());
        }
    }
}
