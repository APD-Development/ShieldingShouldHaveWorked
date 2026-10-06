package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.SelfTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

//Runs the statistics storage self-test on a real Android runtime and file system (an emulator in CI, a phone if run by hand).
@RunWith(AndroidJUnit4.class)
public class ApdStatsDeviceTest {

	@Test
	public void storageSelfTestPassesOnAndroid() {
		Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
		File work = new File(ctx.getFilesDir(), "apd-selftest-work");
		assertTrue(work.isDirectory() || work.mkdirs());
		SelfTest.Report r = SelfTest.run(work, 20000);
		for (String line : r.text().split("\n")) Log.i("APD-SELFTEST", line);
		assertTrue(r.text(), r.passed);
		work.delete();
	}
}
