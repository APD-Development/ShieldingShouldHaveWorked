package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

//Generates whole dungeons headless and checks every floor against the real map. Findings are printed (they are game
//findings for Chris, not test failures); the test fails only if the audit itself cannot run.
public class FloorAuditTest {

	@Test
	public void auditRandomDungeons(){
		HeadlessTestSupport.start();
		SPDSettings.intro(false);
		int seeds = Integer.parseInt(System.getProperty("apd.audit.seeds", "8"));
		long started = System.currentTimeMillis();
		List<FloorAudit.Finding> findings = FloorAudit.run(seeds, 20261127L, null);
		for (FloorAudit.Finding f : findings) System.out.println("APD_FLOOR_AUDIT " + f.line());
		System.out.println("APD_FLOOR_AUDIT_SUMMARY seeds=" + seeds + " floors=" + seeds * 26 + " findings=" + findings.size()
				+ " ms=" + (System.currentTimeMillis() - started));
		assertTrue(seeds > 0);
	}
}
