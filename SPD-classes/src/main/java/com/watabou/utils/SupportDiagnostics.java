package com.watabou.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Small, local-only support trace. Never reads saves, player history or other apps. */
public final class SupportDiagnostics {
    private static final Object LOCK=new Object();
    private static final int LIMIT=65536, EVENTS=128;
    private static final ArrayDeque<String> trace=new ArrayDeque<>();
    private static final ExecutorService writer=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"apd-diagnostics");t.setDaemon(true);return t;});
    private static File directory;
    private static String environment="Not initialized",previous="No previous session trace available.";
    private static String writeFailure="";
    private static long revision;
    private static int traceChars;
    private static final java.util.concurrent.atomic.AtomicLong inputIds=new java.util.concurrent.atomic.AtomicLong();
    public static volatile java.util.function.Supplier<String> platformSnapshot=()->"Platform details unavailable.";
    private static boolean scheduled;
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
                trace.clear();traceChars=0;revision++;
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
            while(trace.size()>EVENTS || traceChars>14000)traceChars-=trace.removeFirst().length()+1;
            revision++;
            if(directory!=null&&!scheduled){scheduled=true;writer.execute(SupportDiagnostics::drain);}
        }
        } catch(RuntimeException | OutOfMemoryError unavailable) { writeFailure="Diagnostic capture unavailable"; }
    }
    public static void error(Throwable error){
        if(error==null)return;
        event("error",error.getClass().getName()+": "+shortText(error.getMessage(),300));
        StackTraceElement[] frames=error.getStackTrace();
        for(int i=0;i<Math.min(frames.length,12);i++)event("at",frames[i].toString());
    }
    private static String snapshot(){
        StringBuilder out=new StringBuilder("APD diagnostic session v1\n").append(environment).append('\n');
        Runtime r=Runtime.getRuntime();
        out.append("Java heap bytes: used=").append(r.totalMemory()-r.freeMemory()).append(" committed=").append(r.totalMemory()).append(" limit=").append(r.maxMemory()).append('\n');
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
