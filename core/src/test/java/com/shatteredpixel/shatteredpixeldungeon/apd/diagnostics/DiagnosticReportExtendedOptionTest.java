package com.shatteredpixel.shatteredpixeldungeon.apd.diagnostics;

import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.archive.ApdFiles;
import com.watabou.utils.SupportActionJournal;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** The player must opt in, and ordinary reports must never inherit extended chronology. */
public class DiagnosticReportExtendedOptionTest {
    private abstract static class Provider implements ApdFiles.Provider {
        public void chooseExport(String n,ApdFiles.ExportTarget t){throw new AssertionError("MIME export required");}
        public void chooseImport(ApdFiles.ImportSource source){throw new AssertionError("bug reports do not import history");}
        public String where(){return "test"; }
    }
    @Test public void standardNeverIncludesExtendedEventsEvenWhenConsentIsOn() throws Exception {
        HeadlessTestSupport.start();
        File root=Files.createTempDirectory("apd-standard-private-").toFile();
        SupportActionJournal.configure(root);
        SupportActionJournal.setEnabled(true);
        SupportActionJournal.record("touch.DOWN","distinct-private-journal-sentinel");
        ApdFiles.Provider old=ApdFiles.provider;
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        CountDownLatch done=new CountDownLatch(1);
        String[] message={null};
        try {
            ApdFiles.provider=new Provider(){
                @Override public void chooseExport(String name,String mime,ApdFiles.ExportTarget t){
                    assertTrue(name.startsWith("APD-diagnostic-"));
                    assertEquals("text/plain",mime);
                    t.onStream(bytes,name);
                }
            };
            DiagnosticReport.export(m->{message[0]=m;done.countDown();});
            assertTrue(done.await(5,TimeUnit.SECONDS));
            assertTrue(message[0].startsWith("Diagnostic report saved"));
            String text=bytes.toString(StandardCharsets.UTF_8.name());
            assertTrue(text.startsWith("APD SUPPORT REPORT v1"));
            assertFalse(text.contains("distinct-private-journal-sentinel"));
            assertFalse(text.contains("APD ACTION HISTORY SINCE LAST SUCCESSFUL REPORT"));
        } finally {
            ApdFiles.provider=old;
            SupportActionJournal.setEnabled(false);
        }
    }

    @Test public void extendedIncludesJournalAndResetsOnlyAfterSuccessfulClose() throws Exception {
        HeadlessTestSupport.start();
        File root=Files.createTempDirectory("apd-extended-private-").toFile();
        SupportActionJournal.configure(root);
        SupportActionJournal.setEnabled(true);
        SupportActionJournal.record("touch.DOWN","chronology-sentinel");
        ApdFiles.Provider old=ApdFiles.provider;
        try {
            ByteArrayOutputStream failed=new ByteArrayOutputStream(){
                @Override public void close() throws IOException {
                    super.close(); throw new IOException("injected close failure");
                }
            };
            CountDownLatch failDone=new CountDownLatch(1);
            ApdFiles.provider=new Provider() {
                @Override public void chooseExport(String n,String mime,ApdFiles.ExportTarget t){t.onStream(failed,n);}
            };
            DiagnosticReport.export(m->{
                assertTrue(m.contains("could not be saved"));
                failDone.countDown();
            },true);
            assertTrue(failDone.await(5,TimeUnit.SECONDS));

            ByteArrayOutputStream successful=new ByteArrayOutputStream();
            CountDownLatch successDone=new CountDownLatch(1);
            ApdFiles.provider=new Provider() {
                @Override public void chooseExport(String n,String mime,ApdFiles.ExportTarget t){t.onStream(successful,n);}
            };
            DiagnosticReport.export(m->{
                assertTrue(m.contains("Diagnostic report saved"));
                successDone.countDown();
            },true);
            assertTrue(successDone.await(5,TimeUnit.SECONDS));
            String text=successful.toString(StandardCharsets.UTF_8.name());
            assertTrue(text.contains("APD ACTION HISTORY SINCE LAST SUCCESSFUL REPORT"));
            assertTrue(text.contains("chronology-sentinel"));
            ByteArrayOutputStream next=new ByteArrayOutputStream();
            SupportActionJournal.export(next,SupportActionJournal.checkpoint());
            assertFalse(next.toString(StandardCharsets.UTF_8.name()).contains("chronology-sentinel"));
        } finally {
            ApdFiles.provider=old;
            SupportActionJournal.setEnabled(false);
        }
    }
}
