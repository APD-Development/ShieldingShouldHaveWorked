package com.shatteredpixel.shatteredpixeldungeon.apd.report;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

// Trial juror 5 (Nov 26): empty headings in the saved report, and "Saved Markdown report to 15"
public class JurorReportFixesTest {
	@Test public void aSavedFileNamedByAnIdShowsTheReportsOwnName(){
		assertEquals("apd-home.md", ReportExport.shownName("15", "apd-home.md"));
		assertEquals("apd-home.md", ReportExport.shownName("", "apd-home.md"));
		assertEquals("apd-home.md", ReportExport.shownName(null, "apd-home.md"));
		assertEquals("my report.md", ReportExport.shownName("my report.md", "apd-home.md"));
	}
	@Test public void headingsWithNothingUnderThemAreLeftOut(){
		Report r = new Report("T", 0L, "test");
		r.blocks.add(new Report.Heading(1, "What do you want to know?"));
		r.blocks.add(new Report.Heading(1, "Explore statistics"));
		r.blocks.add(new Report.Heading(1, "Runs"));
		r.blocks.add(new Report.Note(Report.NoteKind.INFO, "3 runs"));
		r.blocks.add(new Report.Heading(1, "Empty at the end"));
		String md = ReportRenderer.markdown(r);
		assertEquals(false, md.contains("What do you want to know?"));
		assertEquals(false, md.contains("Explore statistics"));
		assertEquals(true, md.contains("Runs"));
	}
}
