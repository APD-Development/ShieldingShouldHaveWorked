package com.shatteredpixel.shatteredpixeldungeon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.RecorderStatus;

import org.junit.Test;

//the support report says whether runs are being recorded and why not, without paths or run ids
public class RecorderReportTest {

	@Test
	public void saysWhyRecordingStopped(){
		RecorderStatus s = new RecorderStatus();
		s.configured = true;
		s.enabled = true;
		s.sessionDisabled = true;
		s.errorCount = 5;
		s.lastError = "inject: java.io.IOException: disk\nfull";
		s.logPath = "/data/user/0/secret/runs/abc.apdlog";
		s.logId = "abc";
		String r = SPDSettings.recorderReport(s);
		assertTrue(r, r.contains("session_disabled=true errors=5 recording_a_run=true"));
		assertTrue(r, r.contains("apd_recorder_last_error=inject: java.io.IOException: disk full\n"));
		assertTrue(r, r.contains("apd_recorder_writer_problem=none"));
		assertFalse("no device paths", r.contains("/data/"));
		assertFalse("no run ids", r.contains("abc"));
		assertTrue(SPDSettings.recorderReport(null).contains("unavailable"));
	}
}
