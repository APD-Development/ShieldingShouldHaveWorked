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
package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/** Reuse is immediate only for a complete, unchanged model. */
public class UimsdWarmCacheTest {
    @Test public void lowHeapPhonesDoNotPreloadLargeHistoriesBesideGameplay(){assertFalse(UimsdWarmCache.withinMemoryBudget(256L*1024*1024,100L*1024*1024,5000000));}
    @Test public void smallHistoriesCanStillOpenWithPreparedHome(){assertTrue(UimsdWarmCache.withinMemoryBudget(256L*1024*1024,80L*1024*1024,100000));assertTrue("normal 72-run fixture remains eligible on a 192MiB heap",UimsdWarmCache.withinMemoryBudget(192L*1024*1024,64L*1024*1024,1470194));}
    @Test public void optionalPreparationStopsWhenTheHeapIsAlreadyBusy(){assertFalse(UimsdWarmCache.withinMemoryBudget(256L*1024*1024,200L*1024*1024,0));assertFalse(UimsdWarmCache.withinMemoryBudget(256L*1024*1024,0,Long.MAX_VALUE));}
    private static void deleteTree(File root){
        File[] children=root.listFiles();if(children!=null)for(File child:children)deleteTree(child);
        assertTrue(root.delete());
    }
    @Test public void gameplayDropsReadyPreloadAndCanWarmAgain()throws Exception{
        File root=Files.createTempDirectory("apd-release-warm").toFile();
        try{
            UimsdWarmCache.prewarm(root,"rules-a","label",false);UimsdWarmCache.awaitPrewarmForTest(10000);
            UimsdModel prior=readyModel();UimsdWarmCache.releaseForGameplay();assertNull(prior.catalog());assertNull(UimsdWarmCache.takeReady(root,"rules-a","label",false));
            UimsdWarmCache.prewarm(root,"rules-a","label",false);UimsdWarmCache.awaitPrewarmForTest(10000);assertNotNull(UimsdWarmCache.takeReady(root,"rules-a","label",false));
        }finally{deleteTree(root);}
    }
    private UimsdModel readyModel() throws Exception {
        java.lang.reflect.Field ready=UimsdWarmCache.class.getDeclaredField("ready");ready.setAccessible(true);
        Object entry=ready.get(null);assertNotNull(entry);
        java.lang.reflect.Field model=entry.getClass().getDeclaredField("model");model.setAccessible(true);
        return (UimsdModel)model.get(entry);
    }

    @Test public void asynchronousClaimReturnsTheExactValidatedModel() throws Exception {
        File root=Files.createTempDirectory("apd-open-warm").toFile();
        try {
            UimsdWarmCache.prewarm(root,"rules-a","label",false);UimsdWarmCache.awaitPrewarmForTest(10000);
            UimsdModel prepared=readyModel();
            java.util.concurrent.CountDownLatch claimed=new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicReference<UimsdModel> opened=new java.util.concurrent.atomic.AtomicReference<>();
            UimsdModel.claimForOpen(root,"rules-a","label",false,()->{},model->{opened.set(model);claimed.countDown();});
            assertTrue(claimed.await(10,java.util.concurrent.TimeUnit.SECONDS));
            assertSame("the viewer owns the exact mutable READY model",prepared,opened.get());
            assertEquals(UimsdModel.State.READY,opened.get().state());
            assertNull(UimsdWarmCache.takeReady(root,"rules-a","label",false));
        }finally{assertTrue(root.delete());}
    }

    @Test public void recorderFlushInvalidatesBeforeTheExactModelCanBeClaimed() throws Exception {
        File root=Files.createTempDirectory("apd-open-flush").toFile();File runs=new File(root,"runs");
        try {
            UimsdWarmCache.prewarm(root,"rules-a","label",false);UimsdWarmCache.awaitPrewarmForTest(10000);
            UimsdModel prepared=readyModel();
            java.util.concurrent.CountDownLatch claimed=new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicReference<UimsdModel> opened=new java.util.concurrent.atomic.AtomicReference<>();
            UimsdModel.claimForOpen(root,"rules-a","label",false,()->{
                try {assertTrue(runs.mkdir());Files.write(new File(runs,"flushed.apdlog").toPath(),new byte[]{1,2,3});}
                catch(java.io.IOException error){throw new RuntimeException(error);}
            },model->{opened.set(model);claimed.countDown();});
            assertTrue(claimed.await(10,java.util.concurrent.TimeUnit.SECONDS));
            opened.get().awaitLoad(10000);assertEquals(UimsdModel.State.READY,opened.get().state());
            assertNotSame("newly flushed bytes require a fresh model",prepared,opened.get());
            assertNotSame("newly flushed bytes require a fresh catalog",prepared.catalog(),opened.get().catalog());
        }finally{deleteTree(root);}
    }

    @Test public void openingNeverWaitsForTheRecorderOnTheCallingThread() throws Exception {
        File root=Files.createTempDirectory("apd-open-worker").toFile();
        java.util.concurrent.CountDownLatch entered=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch claimed=new java.util.concurrent.CountDownLatch(1);
        Thread caller=Thread.currentThread();
        java.util.concurrent.atomic.AtomicReference<Thread> worker=new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<UimsdModel> opened=new java.util.concurrent.atomic.AtomicReference<>();
        try {
            Thread claim=UimsdModel.claimForOpen(root,"rules-a","label",false,()->{
                worker.set(Thread.currentThread());entered.countDown();
                try {release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}
            },model->{opened.set(model);claimed.countDown();});
            assertTrue(entered.await(5,java.util.concurrent.TimeUnit.SECONDS));
            assertNotSame(caller,worker.get());assertSame(claim,worker.get());
            assertTrue("interactive warm claim receives temporary scheduling priority",
                    worker.get().getPriority() > Thread.NORM_PRIORITY);
            release.countDown();assertTrue(claimed.await(10,java.util.concurrent.TimeUnit.SECONDS));
            opened.get().awaitLoad(10000);assertEquals(UimsdModel.State.READY,opened.get().state());
            assertEquals("cold fallback returns to ordinary worker priority",
                    Thread.NORM_PRIORITY, worker.get().getPriority());
        }finally{release.countDown();assertTrue(root.delete());}
    }

    @Test public void staleOpenCannotPublishAfterAnotherLoad() throws Exception {
        File first=Files.createTempDirectory("apd-open-old").toFile();
        File second=Files.createTempDirectory("apd-open-new").toFile();
        java.util.concurrent.CountDownLatch entered=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Thread> oldWorker=new java.util.concurrent.atomic.AtomicReference<>();
        UimsdModel model=new UimsdModel("rules-a","label");
        try {
            model.loadForOpen(first,false,()->{
                oldWorker.set(Thread.currentThread());entered.countDown();
                try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}
            });
            assertTrue(entered.await(5,java.util.concurrent.TimeUnit.SECONDS));
            model.load(second,false);model.awaitLoad(10000);Catalog current=model.catalog();
            assertEquals(UimsdModel.State.READY,model.state());release.countDown();
            oldWorker.get().join(5000);assertFalse(oldWorker.get().isAlive());
            assertSame(current,model.catalog());
        }finally{release.countDown();assertTrue(first.delete());assertTrue(second.delete());}
    }

    @Test public void returningTitleReplenishesTheConsumedPreloadWithoutSharingModels() throws Exception {
        File root = Files.createTempDirectory("apd-warm-return").toFile();
        try {
            UimsdWarmCache.prewarm(root,"rules-a","label",false);
            UimsdWarmCache.awaitPrewarmForTest(10000);
            UimsdModel first=UimsdWarmCache.takeReady(root,"rules-a","label",false);
            assertNotNull(first);
            assertNull(UimsdWarmCache.takeReady(root,"rules-a","label",false));
            UimsdWarmCache.prewarm(root,"rules-a","label",false);
            UimsdWarmCache.awaitPrewarmForTest(10000);
            UimsdModel second=UimsdWarmCache.takeReady(root,"rules-a","label",false);
            assertNotNull(second);assertNotSame(first,second);
            assertEquals(UimsdModel.State.READY,second.state());
            assertNotNull(second.preparedHome(second.catalog(),false));
            java.nio.file.Path rootPath=java.nio.file.Paths.get("").toAbsolutePath();
            java.nio.file.Path sourcePath=null;
            for(java.nio.file.Path dir=rootPath;dir!=null;dir=dir.getParent()){
                java.nio.file.Path candidate=dir.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java");
                if(Files.isRegularFile(candidate)){sourcePath=candidate;break;}
            }
            assertNotNull("Repository source must be available to the lifecycle guard",sourcePath);
            String source=new String(Files.readAllBytes(sourcePath),java.nio.charset.StandardCharsets.UTF_8);
            int switchStart=source.indexOf("protected void switchScene()");
            int switchEnd=source.indexOf("protected boolean isReturnMenu",switchStart);
            assertTrue("restored titles must replenish prewarming without changing Back",
                source.substring(switchStart,switchEnd).contains(
                    "if (scene instanceof TitleScene) UimsdScene.prewarmTitle();"));
        } finally { assertTrue(root.delete()); }
    }

    @Test public void titleOpenHandsFreshnessToTheWorkerBeforeSwitchingScenes() throws Exception {
        java.nio.file.Path rootPath=java.nio.file.Paths.get("").toAbsolutePath();
        java.nio.file.Path sourcePath=null;
        for(java.nio.file.Path dir=rootPath;dir!=null;dir=dir.getParent()){
            java.nio.file.Path candidate=dir.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/UimsdScene.java");
            if(Files.isRegularFile(candidate)){sourcePath=candidate;break;}
        }
        assertNotNull("UIMSD scene source must be available to the title handoff guard",sourcePath);
        String source=new String(Files.readAllBytes(sourcePath),java.nio.charset.StandardCharsets.UTF_8);
        int open=source.indexOf("public static void openNow");
        int create=source.indexOf("@Override\n\tpublic void create",open);
        assertTrue("openNow source section must remain inspectable",open>=0 && create>open);
        String path=source.substring(open,create);
        int loader=path.indexOf("UimsdModel.claimForOpen");
        int handoff=path.indexOf("Game.runOnRenderThread");
        int change=path.indexOf("ShatteredPixelDungeon.switchNoFade(UimsdScene.class)");
        assertTrue("title open delegates flush and exact-model freshness to the claim worker",loader>=0);
        assertTrue("claim worker starts before the render-thread handoff",loader<handoff);
        assertTrue("the exact model is handed off before requesting the new scene",handoff<change);
        assertTrue("stale open workers cannot replace a later scene",path.contains("generation != openGeneration"));
        assertFalse("render thread must not run the recorder flush",path.contains("beforeRead.run()"));
        assertFalse("render thread must not validate the warm cache",path.contains("UimsdWarmCache.takeReady"));
    }

    @Test public void changedPrewarmCancelsSupersededWorkerBeforeStartingReplacement() throws Exception {
        File first=Files.createTempDirectory("apd-warm-old").toFile();
        File second=Files.createTempDirectory("apd-warm-new").toFile();
        Thread superseded;
        java.lang.reflect.Field lockField=UimsdWarmCache.class.getDeclaredField("LOCK");
        java.lang.reflect.Field preparingField=UimsdWarmCache.class.getDeclaredField("preparing");
        lockField.setAccessible(true);preparingField.setAccessible(true);
        Object lock=lockField.get(null);
        try {
            synchronized(lock){
                UimsdWarmCache.prewarm(first,"rules-a","old",false);
                Object oldJob=preparingField.get(null);
                assertNotNull(oldJob);
                java.lang.reflect.Field threadField=oldJob.getClass().getDeclaredField("thread");
                threadField.setAccessible(true);
                superseded=(Thread)threadField.get(oldJob);
                assertNotNull(superseded);assertTrue(superseded.isAlive());
                UimsdWarmCache.prewarm(second,"rules-b","new",true);
                assertTrue("a different preload must interrupt the superseded optional worker",
                        superseded.isInterrupted());
            }
            superseded.join(10000);
            assertFalse("the superseded worker must stop before histories can overlap",superseded.isAlive());
            UimsdWarmCache.awaitPrewarmForTest(10000);
            assertNull(UimsdWarmCache.takeReady(first,"rules-a","old",false));
            UimsdModel replacement=UimsdWarmCache.takeReady(second,"rules-b","new",true);
            assertNotNull(replacement);assertEquals(UimsdModel.State.READY,replacement.state());
        } finally {
            assertTrue(first.delete());assertTrue(second.delete());
        }
    }

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
            UimsdModel stale = readyModel();
            assertTrue(runs.mkdir());
            Files.write(new File(runs, "new-run.apdlog").toPath(), new byte[]{1,2,3});
            assertNull("new recorded log invalidates even a fully prepared model",
                    UimsdWarmCache.takeReady(root, "rules-a", "label", false));
            assertEquals("the rejected catalog is released before a cold replacement loads",
                    UimsdModel.State.EMPTY, stale.state());
            assertNull("stale derived tables must not overlap the replacement catalog", stale.catalog());
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

