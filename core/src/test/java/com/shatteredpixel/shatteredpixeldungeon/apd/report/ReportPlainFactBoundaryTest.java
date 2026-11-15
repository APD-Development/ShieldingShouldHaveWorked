package com.shatteredpixel.shatteredpixeldungeon.apd.report;

import com.shatteredpixel.shatteredpixeldungeon.apd.query.CellText;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReportPlainFactBoundaryTest {

    @Test
    public void multilineFactMetadataStaysInItsOriginalTextRows(){
        Report report = new Report("Audit", 0L, "1.0");
        Report.Facts facts = new Report.Facts(Report.Provenance.RECORDED);
        facts.section("Part\r\nFAKE SECTION");
        facts.add("Count\nFORGED: 1000", new Report.Cell("8", CellText.EXACT),
                "read\rSPOOFED: 90");
        report.add(facts);

        String rendered = ReportRenderer.text(report);
        assertTrue(rendered, rendered.contains("\nPart FAKE SECTION:\n"));
        assertTrue(rendered, rendered.contains("  Count FORGED: 1000: 8 (read SPOOFED: 90)\n"));
        assertFalse(rendered, rendered.contains("\nFAKE SECTION:"));
        assertFalse(rendered, rendered.contains("\nFORGED: 1000"));
        assertFalse(rendered, rendered.contains("\nSPOOFED: 90"));
        assertEquals("Part\r\nFAKE SECTION", facts.facts.get(0).label);
        assertEquals("Count\nFORGED: 1000", facts.facts.get(1).label);
        assertEquals("read\rSPOOFED: 90", facts.facts.get(1).detail);
    }

    @Test
    public void ordinaryFactsAndMultilineValuesKeepExistingTextSemantics(){
        Report report = new Report("Recorded", 0L, "1.0");
        Report.Facts facts = new Report.Facts(Report.Provenance.RECORDED);
        facts.section("Inventory");
        facts.add("Gold", new Report.Cell(">=8", CellText.LOWER), "partial");
        facts.add("Notes", new Report.Cell("first\nsecond", CellText.EXACT), "");
        facts.section(null);
        report.add(facts);

        String text = ReportRenderer.text(report);
        assertTrue(text, text.contains("\nInventory:\n  Gold: >=8 (partial)\n"));
        assertTrue(text, text.contains("  Notes: first\nsecond\n"));
        assertTrue(text, text.contains("\nnull:\n"));
        assertTrue(text, text.contains("MARKS\n"));
        assertTrue(text, text.contains(">= = at least this much"));
        assertTrue(ReportRenderer.markdown(report).contains("- Gold: >=8 (partial)\n"));
        assertTrue(ReportRenderer.csv(report).contains("Inventory,Gold,>=8,partial\r\n"));
        assertEquals("first\nsecond", facts.facts.get(2).value.text);
    }
}
