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
package com.shatteredpixel.shatteredpixeldungeon.apd.diagnostics;

import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.archive.ApdFiles;
import com.watabou.utils.SupportDiagnostics;
import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.*;
import static org.junit.Assert.*;

public class SupportDiagnosticsTest {
    @Test public void diagnosticCaptureCannotReplaceTheOriginalFailureWhenStackAllocationFails()throws Exception{
        File dir=Files.createTempDirectory("apd-diag-low-memory-").toFile();SupportDiagnostics.configure(dir,"capture failure fixture");
        RuntimeException original=new RuntimeException("original failure"){
            @Override public StackTraceElement[] getStackTrace(){throw new OutOfMemoryError("injected stack allocation failure");}
        };
        SupportDiagnostics.error(original);
        String report=SupportDiagnostics.report("");
        assertTrue(report.contains("original failure"));
        assertTrue(report.contains("Error diagnostic capture unavailable"));
    }
    @Test public void traceIsBoundedAndKeepsTheMostRecentEvents()throws Exception{
        File dir=Files.createTempDirectory("apd-diagnostic-test-").toFile();SupportDiagnostics.configure(dir,"TEST fixture build / fixture device");
        for(int i=0;i<1000;i++)SupportDiagnostics.event("fixture."+i,String.join("",java.util.Collections.nCopies(100,"detail")));
        assertTrue(SupportDiagnostics.awaitWrites(5000));
        String report=SupportDiagnostics.report("fixture memory / exit reasons");
        assertTrue(report.contains("fixture.999"));assertFalse(report.contains("fixture.0 "));
        assertTrue(new File(dir,"session.txt").length()<=65536);
        assertTrue(report.contains("Java heap bytes: used="));assertTrue(report.contains("TEST fixture build / fixture device"));
    }
    @Test public void previousSessionAndUncaughtEvidenceSurviveRestart()throws Exception{
        File first=Files.createTempDirectory("apd-diag-first-").toFile(),other=Files.createTempDirectory("apd-diag-other-").toFile();
        SupportDiagnostics.configure(first,"first build");SupportDiagnostics.event("before.failure","native trace fixture");assertTrue(SupportDiagnostics.awaitWrites(5000));
        SupportDiagnostics.uncaught(Thread.currentThread(),new OutOfMemoryError("controlled test, not a real allocation failure"));
        SupportDiagnostics.configure(other,"other process fixture");assertTrue(SupportDiagnostics.awaitWrites(5000));
        SupportDiagnostics.configure(first,"new build");String report=SupportDiagnostics.report("OS reasons unavailable in a core test");
        assertTrue(report.contains("PREVIOUS SESSION\nAPD diagnostic session"));assertTrue(report.contains("before.failure"));
        assertTrue(report.contains("java.lang.OutOfMemoryError"));assertTrue(report.contains("controlled test"));
    }
    @Test public void activityReconfigurationDoesNotEraseTheCurrentSession()throws Exception{
        File dir=Files.createTempDirectory("apd-diag-recreate-").toFile();SupportDiagnostics.configure(dir,"same process");
        SupportDiagnostics.event("before.recreate","kept");SupportDiagnostics.configure(dir,"same process");
        assertTrue(SupportDiagnostics.report("").contains("before.recreate"));
    }
    @Test public void exportWritesReadableTextClosesStreamAndReportsRealSuccess()throws Exception{
        HeadlessTestSupport.start();ApdFiles.Provider original=ApdFiles.provider;
        CountDownLatch done=new CountDownLatch(1);String[] message={null};boolean[] closed={false};
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(){@Override public void close()throws IOException{closed[0]=true;super.close();}};
        try{
            ApdFiles.provider=new Provider(){@Override public void chooseExport(String name,String mime,ApdFiles.ExportTarget target){
                assertTrue(name.startsWith("APD-diagnostic-"));assertTrue(name.endsWith("-UTC.txt"));assertEquals("text/plain",mime);target.onStream(bytes,name);
            }};
            DiagnosticReport.export(s->{message[0]=s;done.countDown();});assertTrue(done.await(5,TimeUnit.SECONDS));
            assertTrue(closed[0]);assertTrue(message[0].startsWith("Diagnostic report saved."));
            String text=bytes.toString(StandardCharsets.UTF_8.name());assertTrue(text.startsWith("APD SUPPORT REPORT v1"));assertTrue(text.contains("CURRENT SESSION"));
        }finally{ApdFiles.provider=original;}
    }
    @Test public void cancellationAndFailedWriteNeverClaimAReportWasSaved()throws Exception{
        HeadlessTestSupport.start();ApdFiles.Provider original=ApdFiles.provider;String[] message={null};CountDownLatch failed=new CountDownLatch(1);
        try{
            ApdFiles.provider=new Provider(){@Override public void chooseExport(String n,String m,ApdFiles.ExportTarget t){t.onCancelled();}};
            DiagnosticReport.export(s->message[0]=s);assertTrue(message[0].contains("No report was saved"));
            ApdFiles.provider=new Provider(){@Override public void chooseExport(String n,String m,ApdFiles.ExportTarget t){t.onStream(new OutputStream(){public void write(int b)throws IOException{throw new IOException("controlled failure");}},n);}};
            DiagnosticReport.export(s->{message[0]=s;failed.countDown();});assertTrue(failed.await(5,TimeUnit.SECONDS));assertTrue(message[0].contains("could not be saved"));
        }finally{ApdFiles.provider=original;}
    }
    @Test public void phaseHeapSamplesHaveTimestampsUnitsAndSurviveExport()throws Exception{
        File dir=Files.createTempDirectory("apd-diag-memory-phase-").toFile();
        SupportDiagnostics.configure(dir,"heap phase fixture");
        SupportDiagnostics.memoryEvent("uimsd.open_begin");SupportDiagnostics.memoryEvent("uimsd.model_READY");
        assertTrue(SupportDiagnostics.awaitWrites(5000));
        String report=SupportDiagnostics.report("");
        java.util.regex.Matcher samples=java.util.regex.Pattern.compile("(?m)^(\\d+) memory.sample phase=(uimsd\\.\\w+) used_bytes=(\\d+) committed_bytes=(\\d+) limit_bytes=(\\d+)$").matcher(report.substring(report.indexOf("MEMORY HISTORY"),report.indexOf("RECENT EVENTS")));
        int count=0;
        while(samples.find()){
            assertTrue(Long.parseLong(samples.group(1))>0);
            long used=Long.parseLong(samples.group(3)),committed=Long.parseLong(samples.group(4)),limit=Long.parseLong(samples.group(5));
            assertTrue(used>=0&&used<=committed);assertTrue(committed<=limit);count++;
        }
        assertEquals(2,count);
        String persisted=new String(Files.readAllBytes(new File(dir,"session.txt").toPath()),StandardCharsets.UTF_8);
        assertTrue(persisted.contains("phase=uimsd.open_begin"));assertTrue(persisted.contains("phase=uimsd.model_READY"));
    }
    @Test public void repeatedHeapSamplingRetainsOnlyBoundedRecentPhases()throws Exception{
        File dir=Files.createTempDirectory("apd-diag-memory-bound-").toFile();SupportDiagnostics.configure(dir,"bounded phase fixture");
        for(int i=0;i<1000;i++)SupportDiagnostics.memoryEvent("phase_"+i);
        assertTrue(SupportDiagnostics.awaitWrites(5000));String report=SupportDiagnostics.report("");
        assertTrue(report.contains("phase=phase_999 "));assertFalse(report.contains("phase=phase_0 "));
        assertTrue(new File(dir,"session.txt").length()<=65536);
    }
    @Test public void heapHistorySurvivesBurstOfInputWithoutUnboundedRetention()throws Exception{
        File dir=Files.createTempDirectory("apd-heap-history-").toFile();
        SupportDiagnostics.configure(dir,"memory history fixture");
        SupportDiagnostics.memoryEvent("activity.pause");
        SupportDiagnostics.memoryEvent("activity.resume");
        SupportDiagnostics.memoryEvent("game.active");
        for(int i=0;i<500;i++)SupportDiagnostics.event("input.dispatched","id="+i+" kind=touch.DOWN");
        assertTrue(SupportDiagnostics.awaitWrites(5000));
        String report=SupportDiagnostics.report("");
        assertTrue(report.contains("MEMORY HISTORY"));
        assertTrue(report.contains("phase=activity.pause "));
        assertTrue(report.contains("phase=activity.resume "));
        assertTrue(report.contains("phase=game.active "));
        assertTrue(report.contains("input.dispatched id=499"));
        assertFalse(report.contains("input.dispatched id=0 "));
        String persisted=new String(Files.readAllBytes(new File(dir,"session.txt").toPath()),StandardCharsets.UTF_8);
        assertTrue(persisted.contains("MEMORY HISTORY"));
        assertTrue(persisted.contains("phase=activity.pause "));
        assertTrue(new File(dir,"session.txt").length()<=65536);
    }

    @Test public void periodicMemorySamplingIsRateLimited()throws Exception{
        File dir=Files.createTempDirectory("apd-rate-limited-memory-").toFile();
        SupportDiagnostics.configure(dir,"periodic fixture");
        SupportDiagnostics.periodicMemoryEvent();
        SupportDiagnostics.periodicMemoryEvent();
        SupportDiagnostics.periodicMemoryEvent();
        assertTrue(SupportDiagnostics.awaitWrites(5000));
        String report=SupportDiagnostics.report("");
        String history=report.substring(report.indexOf("MEMORY HISTORY"),report.indexOf("RECENT EVENTS"));
        assertEquals(1,history.split("phase=game.active",-1).length-1);
        assertTrue(history.contains("limit_bytes="));
    }

    private static class Provider implements ApdFiles.Provider{
        public void chooseExport(String n,ApdFiles.ExportTarget t){throw new AssertionError("MIME-aware export expected");}
        public void chooseImport(ApdFiles.ImportSource s){throw new AssertionError("Diagnostics never import or read history");}
        public String where(){return "test stream";}
    }
}
