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

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Single-entry, fully validated UIMSD preload for the title menu.
 *
 * No disk work or catalog computation happens in prewarm(); all validation
 * occurs on a daemon worker. A warmed catalog is offered only when every
 * relevant raw log and column-layout file still has matching metadata.
 * DatasetStore and SavedViews still perform their full normal reads and
 * cryptographic raw-log checks during preparation.
 */
public final class UimsdWarmCache {
    private static final Object LOCK = new Object();
    private static Entry ready;
    private static Job preparing;
    private static long generation;

    private UimsdWarmCache(){}

    private static final class Key {
        final File root;
        final String ruleset, label;
        final boolean allRules;
        Key(File root, String ruleset, String label, boolean allRules) {
            this.root = root.getAbsoluteFile();
            this.ruleset = ruleset;
            this.label = label;
            this.allRules = allRules;
        }
        boolean matches(Key key) {
            return root.equals(key.root) && ruleset.equals(key.ruleset)
                    && label.equals(key.label) && allRules == key.allRules;
        }
    }
    private static final class Entry {
        final Key key;
        final UimsdModel model;
        final String sourceStamp;
        Entry(Key key, UimsdModel model, String sourceStamp) {
            this.key = key;
            this.model = model;
            this.sourceStamp = sourceStamp;
        }
    }
    private static final class Job {
        final Key key;
        final long id;
        Thread thread;
        volatile UimsdModel model;
        Job(Key key, long id) {
            this.key = key;
            this.id = id;
        }
    }

    private static String stamp(File root) {
        try {
            StringBuilder out = new StringBuilder(root.getAbsolutePath());
            stampOne(out, new File(root, UimsdModel.COLUMN_LAYOUTS_FILE));
            stampOne(out, new File(root, DatasetStore.DEMO_MARKER_FILE));
            for (String folder : new String[]{"runs", "legacy"}) {
                File directory = new File(root, folder);
                out.append('|').append(folder).append('=').append(directory.isDirectory());
                File[] files = directory.listFiles();
                if (files == null) continue;
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".apdlog"))
                        stampOne(out, file);
                }
            }
            return out.toString();
        } catch (RuntimeException error) {
            return null; // uncertain source state must never be accepted as fresh
        }
    }
    private static void stampOne(StringBuilder out, File file) {
        out.append('|').append(file.getName()).append(':').append(file.exists())
                .append(':').append(file.length()).append(':').append(file.lastModified());
    }

    // Optional preload must leave room for gameplay and foreground screens.
    static boolean withinMemoryBudget(long limit,long used,long sourceBytes){
        long budget=Math.min(32L*1024*1024,limit/8);
        return limit>0&&sourceBytes>=0&&sourceBytes<=budget/64&&used<=limit-limit/4-budget;
    }
    private static boolean canPrepare(File root){
        long bytes=0;
        for(String folder:new String[]{"runs","legacy"}){
            File[] files=new File(root,folder).listFiles();
            if(files!=null)for(File file:files)if(file.isFile()&&file.getName().endsWith(".apdlog")){
                long length=file.length();if(length>Long.MAX_VALUE-bytes)return false;bytes+=length;
            }
        }
        Runtime runtime=Runtime.getRuntime();
        return withinMemoryBudget(runtime.maxMemory(),runtime.totalMemory()-runtime.freeMemory(),bytes);
    }

    /**
     * Begin preparing the real Home before the player taps UIMSD.
     * Repeated title appearances do not create concurrent duplicate loaders.
     */
    public static void prewarm(File root, String ruleset, String label, boolean allRules) {
        if (root == null) return;
        final Key key = new Key(root, ruleset, label, allRules);
        final Job job;
        synchronized (LOCK) {
            if (preparing != null && preparing.key.matches(key)) return;
            job = new Job(key, ++generation);
            preparing = job;
        }
        Thread worker = new Thread(() -> {
            try {
                String before = stamp(key.root);
                if (before == null) return;
                synchronized (LOCK) {
                    if (ready != null && ready.key.matches(key)
                            && before.equals(ready.sourceStamp)) return;
                    if(ready!=null){ready.model.releaseForGameplay();ready=null;}
                }
                if(!canPrepare(key.root))return; // explicit UIMSD opening still reads complete history normally
                UimsdModel prepared = new UimsdModel(key.ruleset, key.label);
                synchronized(LOCK){
                    if(preparing!=job||job.id!=generation)return;
                    job.model=prepared;
                    prepared.load(key.root, key.allRules);
                }
                prepared.awaitLoad(60000);
                String after = stamp(key.root);
                if (prepared.state() != UimsdModel.State.READY ||
                        after == null || !before.equals(after)) return;
                synchronized (LOCK) {
                    if (preparing == job && job.id == generation)
                        ready = new Entry(key, prepared, after);
                }
            } finally {
                synchronized (LOCK) {
                    if (preparing == job) preparing = null;
                }
            }
        }, "uimsd-title-prewarm");
        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        job.thread = worker;
        worker.start();
    }

    /** Return a fully READY model or nothing; never waits for the loader. */
    public static UimsdModel takeReady(File root, String ruleset, String label, boolean allRules) {
        if (root == null) return null;
        final Key key = new Key(root, ruleset, label, allRules);
        final Entry candidate;
        synchronized (LOCK) {
            candidate = ready;
            if (candidate == null || !candidate.key.matches(key)) return null;
        }
        String now = stamp(key.root);
        synchronized (LOCK) {
            if (candidate != ready) return null;
            ready = null; // only one viewer can own a mutable model
            if (now == null || !now.equals(candidate.sourceStamp)) return null;
            return candidate.model.state() == UimsdModel.State.READY ? candidate.model : null;
        }
    }

    /** Cancel optional title preparation and drop its strong model reference before a game loads. */
    public static void releaseForGameplay(){
        synchronized(LOCK){
            ++generation;
            if(ready!=null)ready.model.releaseForGameplay();
            ready=null;
            Job old=preparing;preparing=null;
            if(old!=null){
                if(old.model!=null)old.model.releaseForGameplay();
                if(old.thread!=null)old.thread.interrupt();
            }
        }
    }

    static void awaitPrewarmForTest(long millis) throws InterruptedException {
        Thread thread;
        synchronized (LOCK) {
            thread = preparing == null ? null : preparing.thread;
        }
        if (thread != null) thread.join(millis);
    }

    static void resetForTest() {
        synchronized (LOCK) {
            ++generation;
            preparing = null;
            ready = null;
        }
    }
}
