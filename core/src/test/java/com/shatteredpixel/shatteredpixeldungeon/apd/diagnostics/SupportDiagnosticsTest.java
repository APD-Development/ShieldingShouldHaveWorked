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
    private static class Provider implements ApdFiles.Provider{
        public void chooseExport(String n,ApdFiles.ExportTarget t){throw new AssertionError("MIME-aware export expected");}
        public void chooseImport(ApdFiles.ImportSource s){throw new AssertionError("Diagnostics never import or read history");}
        public String where(){return "test stream";}
    }
}
