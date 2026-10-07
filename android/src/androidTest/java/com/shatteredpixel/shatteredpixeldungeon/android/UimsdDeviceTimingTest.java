package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoData;
import com.shatteredpixel.shatteredpixeldungeon.apd.demo.DemoGenerator;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.DatasetStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Query;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryEngine;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.QueryResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Scope;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ViewDef;
import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.Ruleset;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

//Phase 9 measures before it optimises: how long UIMSD takes on this device for a realistic history (the 72-run demo history, which now
//includes exact healing). The numbers go to logcat (tag APDPERF, streamed into the CI job log); the budgets are generous for an emulator
//but a real regression would break them.
@RunWith(AndroidJUnit4.class)
public class UimsdDeviceTimingTest {

	private static final String TAG = "APDPERF";

	private static void delete( File f ){
		File[] kids = f.listFiles();
		if (kids != null) for (File k : kids) delete(k);
		f.delete();
	}

	@Test
	public void uimsdIsFastEnoughOnThisDevice() throws Exception {
		Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
		File base = new File(ctx.getFilesDir(), "apd-timing-" + System.nanoTime());
		File demo = DemoData.demoRootFor(new File(base, "apd"));
		try {
			Ruleset rules = Ruleset.of(30, 0);
			long g0 = System.nanoTime();
			DemoGenerator.generate(demo, rules, System.currentTimeMillis());
			long g1 = System.nanoTime();

			DatasetStore store = new DatasetStore(demo);
			long t0 = System.nanoTime();
			Catalog cold = store.load();
			long t1 = System.nanoTime();
			Catalog warm = store.load();
			long t2 = System.nanoTime();

			long slowest = 0;
			String slowestView = "";
			long all = 0;
			int rows = 0, views = 0;
			for (ViewDef v : warm.views()){
				Dataset ds = warm.datasetOf(v);
				Query q = v.initialQuery().withMinSample(0).withScope(Scope.DEFAULT.withUnfinished(true).withRules(Scope.Rules.ALL, "")).withShowInternal(true);
				long s0 = System.nanoTime();
				QueryResult r = QueryEngine.run(ds, q, rules.id);
				long ms = (System.nanoTime() - s0) / 1000000;
				all += ms;
				views++;
				rows += r.rows.size();
				if (ms >= slowest){
					slowest = ms;
					slowestView = v.id;
				}
			}
			long genMs = (g1 - g0) / 1000000, coldMs = (t1 - t0) / 1000000, warmMs = (t2 - t1) / 1000000;
			Log.w(TAG, "demo history generated in " + genMs + " ms; first build of every dataset " + coldMs + " ms; reload from the row cache " + warmMs
					+ " ms; " + views + " views run in " + all + " ms in total, slowest " + slowestView + " " + slowest + " ms (" + rows + " rows shown)");
			assertTrue(cold.logsRebuilt > 0);
			assertTrue("the second load must come from the cache", warm.logsCached > 0);
			assertTrue("first build too slow: " + coldMs + " ms", coldMs < 30000);
			assertTrue("reload from the cache too slow: " + warmMs + " ms", warmMs < 5000);
			assertTrue("one view too slow: " + slowestView + " " + slowest + " ms", slowest < 3000);
		} finally {
			delete(base);
		}
	}
}
