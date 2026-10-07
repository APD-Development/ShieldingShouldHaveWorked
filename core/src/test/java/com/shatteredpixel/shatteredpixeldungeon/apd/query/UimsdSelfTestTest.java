package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.SelfTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UimsdSelfTestTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void theUimsdSelfTestPassesAndCleansUpAfterItself(){
		SelfTest.Report r = new SelfTest.Report();
		UimsdSelfTest.run(tmp.getRoot(), r);
		assertTrue(r.text(), r.passed);
		assertTrue(r.text().contains("UIMSD datasets and queries"));
		assertTrue(r.text().contains("reload from cache"));
		assertEquals("the self-test removes what it made", 0, tmp.getRoot().list().length);
	}

	@Test
	public void itFailsLoudlyIfItCannotWrite() throws Exception {
		java.io.File file = tmp.newFile("not-a-folder");
		SelfTest.Report r = new SelfTest.Report();
		UimsdSelfTest.run(new java.io.File(file, "inside"), r);
		assertTrue(!r.passed);
	}
}
