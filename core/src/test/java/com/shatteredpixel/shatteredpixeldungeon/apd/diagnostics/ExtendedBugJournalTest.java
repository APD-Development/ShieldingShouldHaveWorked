package com.shatteredpixel.shatteredpixeldungeon.apd.diagnostics;

import com.watabou.utils.SupportActionJournal;
import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class ExtendedBugJournalTest {
    private static String history() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SupportActionJournal.export(out,SupportActionJournal.checkpoint());
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test public void defaultOffDoesNotRecordUntilPlayerOptIn() throws Exception {
        SupportActionJournal.setEnabled(false);
        File root=Files.createTempDirectory("apd-extended-off-").toFile();
        SupportActionJournal.configure(root);
        assertFalse(SupportActionJournal.enabled());
        SupportActionJournal.record("touch.UP","x=20 y=21");
        SupportActionJournal.paused("GameScene");
        SupportActionJournal.resumed("TitleScene");
        assertFalse(history().contains("touch.UP"));
        assertFalse(new File(root,"activity-paused.txt").exists());
    }

    @Test public void optInRecordsAndOptOutErasesJournal() throws Exception {
        SupportActionJournal.setEnabled(false);
        File root=Files.createTempDirectory("apd-extended-consent-").toFile();
        SupportActionJournal.configure(root);
        SupportActionJournal.setEnabled(true);
        try {
            SupportActionJournal.record("touch.UP","x=20 y=21");
            SupportActionJournal.paused("GameScene");
            Thread.sleep(20);
            SupportActionJournal.resumed("GameScene");
            String report=history();
            assertTrue(report.contains("touch.UP x=20 y=21"));
            assertTrue(report.contains("activity.background scene=GameScene"));
            assertTrue(report.contains("activity.foreground scene=GameScene away_ms="));
        } finally {
            SupportActionJournal.setEnabled(false);
        }
        assertFalse(SupportActionJournal.enabled());
        assertFalse(new File(root,"action-history.txt").exists());
        assertFalse(history().contains("touch.UP"));
    }

    @Test public void failedExportRetainsHistoryAndSuccessAcknowledgesExactlyCheckpoint() throws Exception {
        SupportActionJournal.setEnabled(false);
        File root=Files.createTempDirectory("apd-extended-checkpoint-").toFile();
        SupportActionJournal.configure(root);
        SupportActionJournal.setEnabled(true);
        try {
            SupportActionJournal.record("ui.click","control=before");
            SupportActionJournal.Checkpoint cp=SupportActionJournal.checkpoint();
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            SupportActionJournal.export(out,cp);
            assertTrue(out.toString(StandardCharsets.UTF_8.name()).contains("control=before"));
            // A failed export intentionally does not call acknowledge.
            assertTrue(history().contains("control=before"));
            SupportActionJournal.record("ui.click","control=after");
            SupportActionJournal.acknowledge(cp);
            String next=history();
            assertFalse(next.contains("control=before"));
            assertTrue(next.contains("control=after"));
        } finally {
            SupportActionJournal.setEnabled(false);
        }
    }

    @Test public void restartingWithConsentOffErasesInterruptedOldJournal() throws Exception {
        SupportActionJournal.setEnabled(false);
        File root=Files.createTempDirectory("apd-old-disabled-").toFile();
        SupportActionJournal.configure(root);
        File leftover=new File(root,"action-history.txt");
        Files.write(leftover.toPath(),"old tracking must not survive opt out".getBytes(StandardCharsets.UTF_8));
        SupportActionJournal.setEnabled(false);
        assertFalse(leftover.exists());
        assertFalse(SupportActionJournal.enabled());
    }

    @Test public void standardReportDoesNotNeedEnabledJournal() throws Exception {
        SupportActionJournal.setEnabled(false);
        assertFalse(SupportActionJournal.enabled());
        assertTrue(com.watabou.utils.SupportDiagnostics.report("").startsWith("APD SUPPORT REPORT v1"));
    }
}
