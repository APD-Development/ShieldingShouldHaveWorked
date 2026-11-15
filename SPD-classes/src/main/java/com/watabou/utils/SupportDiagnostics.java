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
package com.watabou.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Small, local-only support trace. Never reads saves, player history or other apps. */
public final class SupportDiagnostics {
    private static final Object LOCK=new Object();
    private static final int LIMIT=65536, EVENTS=128, MEMORY_EVENTS=48;
    private static final long MEMORY_INTERVAL_NANOS=30_000_000_000L;
    private static final ArrayDeque<String> trace=new ArrayDeque<>();
    // Keep memory trends independently: high-volume touch events otherwise evict them.
    private static final ArrayDeque<String> memoryTrace=new ArrayDeque<>();
    private static final ExecutorService writer=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"apd-diagnostics");t.setDaemon(true);return t;});
    private static File directory;
    private static String environment="Not initialized",previous="No previous session trace available.";
    private static String writeFailure="";
    private static long revision;
    private static int traceChars;
    private static final java.util.concurrent.atomic.AtomicLong inputIds=new java.util.concurrent.atomic.AtomicLong();
    public static volatile java.util.function.Supplier<String> platformSnapshot=()->"Platform details unavailable.";
    private static boolean scheduled;
    private static long lastPeriodicMemoryNanos;
    private static final byte[] EMERGENCY="Uncaught failure; detailed capture failed, possibly because memory was exhausted. This is not proof of an out-of-memory cause.\n".getBytes(StandardCharsets.UTF_8);
    private SupportDiagnostics(){}
    public static long nextInputId(){return inputIds.incrementAndGet();}
    public static void input(String phase,String kind,long id,long queuedNanos,long sourceNanos){
        try {
            long now=System.nanoTime();
            event("input."+phase,"id="+id+" kind="+kind+" queued_nanos="+queuedNanos+" app_queue_ms="+Math.max(0,(now-queuedNanos)/1000000L)
                    +" source_nanos="+sourceNanos+" source_age_ms="+(sourceNanos>0?Long.toString(Math.max(0,(now-sourceNanos)/1000000L)):"unavailable"));
        }catch(RuntimeException|OutOfMemoryError unavailable){writeFailure="Input diagnostic capture unavailable";}
    }

    public static void configure(File folder,String details) {
        synchronized(LOCK){
            try{
                File target=folder.getCanonicalFile();
                if(target.equals(directory))return; // Activity recreation is not a new process session.
                if(!target.isDirectory()&&!target.mkdirs())throw new IOException("Cannot create diagnostics folder");
                directory=target;environment=shortText(details,2048);
                previous=read(new File(target,"session.txt"));
                trace.clear();memoryTrace.clear();traceChars=0;lastPeriodicMemoryNanos=0;revision++;
                if(!previous.isEmpty())atomic(new File(target,"previous-session.txt"),previous);
                else previous="No previous session trace available.";
            }catch(IOException e){writeFailure=e.getClass().getSimpleName();}
        }
        event("process.start", "");
    }

    private static String shortText(String text,int limit){
        if(text==null)return "";return text.length()<=limit?text:text.substring(0,limit)+" [truncated]";
    }
    public static void event(String kind,String detail){
        try { synchronized(LOCK){
            String line=System.currentTimeMillis()+" "+shortText(kind,80)+" "+shortText(detail,700).replace('\n',' ');
            trace.addLast(line);traceChars+=line.length()+1;
            if ("memory.sample".equals(kind)) {
                memoryTrace.addLast(line);
                while (memoryTrace.size()>MEMORY_EVENTS) memoryTrace.removeFirst();
            }
            while(trace.size()>EVENTS || traceChars>14000)traceChars-=trace.removeFirst().length()+1;
            revision++;
            if(directory!=null&&!scheduled){scheduled=true;writer.execute(SupportDiagnostics::drain);}
        }
        } catch(RuntimeException | OutOfMemoryError unavailable) { writeFailure="Diagnostic capture unavailable"; }
    }
    /** A bounded phase observation, without forcing GC or inspecting player history. */
    public static void memoryEvent(String phase){
        try{
            Runtime runtime=Runtime.getRuntime();
            long committed=runtime.totalMemory(),free=runtime.freeMemory();
            event("memory.sample","phase="+shortText(phase,80)+" used_bytes="+(committed-free)
                    +" committed_bytes="+committed+" limit_bytes="+runtime.maxMemory());
        }catch(RuntimeException | OutOfMemoryError unavailable){
            writeFailure="Memory diagnostic capture unavailable";
        }
    }
    /** On the render thread, sample at most once every 30 wall-clock seconds.
     * Avoids per-frame allocations, forced GC and any reads of player history. */
    public static void periodicMemoryEvent(){
        long now=System.nanoTime();
        if (lastPeriodicMemoryNanos!=0 && now-lastPeriodicMemoryNanos>=0
                && now-lastPeriodicMemoryNanos<MEMORY_INTERVAL_NANOS) return;
        lastPeriodicMemoryNanos=now;
        memoryEvent("game.active");
    }
    public static void error(Throwable error){
        if(error==null)return;
        try {
            event("error",error.getClass().getName()+": "+shortText(error.getMessage(),300));
            StackTraceElement[] frames=error.getStackTrace();
            for(int i=0;i<Math.min(frames.length,12);i++)event("at",frames[i].toString());
        } catch(RuntimeException | OutOfMemoryError unavailable) {
            // Diagnostic allocation must never replace the failure being reported.
            writeFailure="Error diagnostic capture unavailable";
        }
    }
    private static String snapshot(){
        StringBuilder out=new StringBuilder("APD diagnostic session v1\n").append(environment).append('\n');
        Runtime r=Runtime.getRuntime();
        out.append("Java heap bytes: used=").append(r.totalMemory()-r.freeMemory()).append(" committed=").append(r.totalMemory()).append(" limit=").append(r.maxMemory()).append('\n');
        out.append("MEMORY HISTORY (bounded, most recent first not guaranteed by crash)\n");
        for(String line:memoryTrace)out.append(line).append('\n');
        out.append("RECENT EVENTS\n");
        for(String line:trace)out.append(line).append('\n');
        return shortText(out.toString(),LIMIT);
    }
    private static void drain(){
        try { for(;;){
            File target;String contents;long written;
            synchronized(LOCK){target=directory;contents=snapshot();written=revision;}
            try{atomic(new File(target,"session.txt"),contents);}
            catch(IOException e){synchronized(LOCK){writeFailure=e.getClass().getSimpleName();}}
            synchronized(LOCK){if(written==revision){scheduled=false;LOCK.notifyAll();return;}}
        }
        } catch(RuntimeException | OutOfMemoryError unavailable) { synchronized(LOCK){writeFailure="Diagnostic writer unavailable";scheduled=false;LOCK.notifyAll();} }
    }
    private static void atomic(File file,String contents)throws IOException {
        File temp=new File(file.getPath()+".tmp");
        try(FileOutputStream out=new FileOutputStream(temp)){out.write(contents.getBytes(StandardCharsets.UTF_8));}
        if(!temp.renameTo(file))throw new IOException("Cannot replace diagnostic trace");
    }
    private static String read(File file)throws IOException {
        if(!file.isFile())return "";
        try(InputStream in=new FileInputStream(file);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] block=new byte[4096];int n,left=LIMIT;
            while(left>0&&(n=in.read(block,0,Math.min(block.length,left)))!=-1){bytes.write(block,0,n);left-=n;}
            return new String(bytes.toByteArray(),StandardCharsets.UTF_8);
        }
    }
    /** Call from an export worker. A report is a snapshot, not a diagnosis. */
    public static String report(String platformDetails){
        synchronized(LOCK){
            String crash="";
            try{if(directory!=null)crash=read(new File(directory,"last-crash.txt"));}catch(IOException e){crash="Unavailable: "+e.getClass().getSimpleName();}
            return "APD SUPPORT REPORT v1\nGenerated: "+new Date()+"\n"
                    +"This report records observations. Missing evidence does not rule out a failure.\n"
                    +"No saved games, inventory contents, run histories or device identifiers are included.\n\n"
                    +shortText(platformDetails,16384)+"\n\nCURRENT SESSION\n"+snapshot()
                    +"\nPREVIOUS SESSION\n"+previous+"\nLAST RECORDED UNCAUGHT FAILURE (may be older)\n"+crash
                    +"\nDiagnostic persistence status: "+(writeFailure.isEmpty()?"no write failure recorded":writeFailure)+"\n";
        }
    }
    public static String report(){
        String platform;
        try{platform=platformSnapshot.get();}catch(Throwable unavailable){platform="Platform snapshot unavailable: "+unavailable.getClass().getSimpleName();}
        return report(platform);
    }
    /** Android's existing crash handler still receives the original exception. */
    public static void uncaught(Thread thread,Throwable failure){
        try{
            error(failure);File root;String text;
            synchronized(LOCK){root=directory;text=snapshot();}
            if(root!=null)try(FileOutputStream out=new FileOutputStream(new File(root,"last-crash.txt"))){out.write(text.getBytes(StandardCharsets.UTF_8));out.getFD().sync();}
        }catch(Throwable captureFailed){
            try{File root=directory;if(root!=null)try(FileOutputStream out=new FileOutputStream(new File(root,"last-crash.txt"))){out.write(EMERGENCY);}}catch(Throwable ignored){}
        }
    }
    public static boolean awaitWrites(long millis)throws InterruptedException {
        long deadline=System.currentTimeMillis()+millis;
        synchronized(LOCK){while(scheduled){long left=deadline-System.currentTimeMillis();if(left<=0)return false;LOCK.wait(left);}return true;}
    }
}
