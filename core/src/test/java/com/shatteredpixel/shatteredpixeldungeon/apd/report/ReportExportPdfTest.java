package com.shatteredpixel.shatteredpixeldungeon.apd.report;

import com.shatteredpixel.shatteredpixeldungeon.apd.archive.ApdFiles;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ReportExportPdfTest {

	static class Fake implements ApdFiles.Provider {
		final boolean pdf; String gotText; final ByteArrayOutputStream saved = new ByteArrayOutputStream(); String mime;
		Fake( boolean pdf ){ this.pdf = pdf; }
		public void chooseExport( String n, ApdFiles.ExportTarget t ){ chooseExport(n, "?", t); }
		public void chooseExport( String n, String m, ApdFiles.ExportTarget t ){ mime = m; t.onStream(saved, n); }
		public void chooseImport( ApdFiles.ImportSource s ){ s.onCancelled(); }
		public String where(){ return "test"; }
		public boolean canWritePdf(){ return pdf; }
		public void writePdf( String title, String text, OutputStream out ) throws IOException {
			gotText = text; out.write("%PDF-1.4 fake".getBytes(StandardCharsets.US_ASCII));
		}
	}

	private static String saveWith( Fake p, Report r ) throws Exception {
		ApdFiles.Provider old = ApdFiles.provider;
		ApdFiles.provider = p;
		try {
			AtomicReference<String> msg = new AtomicReference<>();
			java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
			ReportExport.save(new File("."), r, ReportExport.Format.PDF, m -> { msg.set(m); done.countDown(); });
			assertTrue(done.await(10, java.util.concurrent.TimeUnit.SECONDS));
			return msg.get();
		} finally {
			ApdFiles.provider = old;
		}
	}

	@Test
	public void pdfIsOfferedOnlyWhereThePlatformCanLayItOut(){
		assertFalse(Arrays.asList(ReportExport.available(new Fake(false))).contains(ReportExport.Format.PDF));
		assertFalse(Arrays.asList(ReportExport.available(new ApdFiles.FolderProvider(new File(".")))).contains(ReportExport.Format.PDF));
		assertTrue(Arrays.asList(ReportExport.available(new Fake(true))).contains(ReportExport.Format.PDF));
		assertEquals(ReportExport.Format.values().length - 1, ReportExport.available(null).length);
	}

	@Test
	public void pdfIsThePlainTextReportWrittenByThePlatform() throws Exception {
		Report r = new Report("Lifetime", 0, "test");
		Fake p = new Fake(true);
		String msg = saveWith(p, r);
		assertEquals(ReportRenderer.text(r), p.gotText);
		assertEquals("application/pdf", p.mime);
		assertTrue(new String(p.saved.toByteArray(), StandardCharsets.US_ASCII).startsWith("%PDF"));
		assertTrue(msg, msg.startsWith("Saved PDF document report"));
	}

	@Test
	public void pdfWithoutPlatformSupportSavesNothingAndSaysSo() throws Exception {
		Fake p = new Fake(false);
		String msg = saveWith(p, new Report("Lifetime", 0, "test"));
		assertNull(p.mime);
		assertTrue(msg, msg.startsWith("PDF is not available here"));
	}
}
