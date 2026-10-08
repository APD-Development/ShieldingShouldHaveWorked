/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.apd.backendbridge;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogWriter;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.StatsStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class SubmissionQueueTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private void log( String runId, boolean finished, int difficulty, int segment ){
		File f = new StatsStore(tmp.getRoot()).logFile(runId);
		f.getParentFile().mkdirs();
		RunLogWriter w = RunLogWriter.create(f, TelemetryVersion.CURRENT, runId, "test", 1);
		LifecycleEvents.RunStart s = new LifecycleEvents.RunStart();
		s.runId = runId; s.segment = segment; s.heroClass = "MAGE"; s.rulesetId = "exotic30"; s.exoticChance = 30; s.apdVersionName = "4.0.8-apd";
		s.difficulty = difficulty; s.difficultyBalance = 0;
		LifecycleEvents.emitRunStart(w, 1, false, s);
		if (finished){
			LifecycleEvents.RunEnd e = new LifecycleEvents.RunEnd();
			e.outcome = LifecycleEvents.OUTCOME_DIED; e.cause = "Rat"; e.depth = 4; e.deepest = 4; e.heroLevel = 6; e.score = 900; e.turns = 1500;
			LifecycleEvents.emitRunEnd(w, 2, e);
		}
		w.close(finished);
	}

	@Test
	public void aFinishedRunIsQueuedOnceAndSurvivesARestart(){
		log("run-a", true, 3, 0);
		SubmissionQueue.Queued q = SubmissionQueue.enqueueRun(tmp.getRoot(), "run-a", "4.0.8-apd", 100);
		assertEquals(q.detail + q.refusal, SubmissionQueue.Result.QUEUED, q.result);
		assertEquals(1, SubmissionQueue.pending(tmp.getRoot()));
		assertEquals("queuing it again does not duplicate it", SubmissionQueue.Result.ALREADY_QUEUED, SubmissionQueue.enqueueRun(tmp.getRoot(), "run-a", "4.0.8-apd", 200).result);
		assertEquals(1, SubmissionQueue.pending(tmp.getRoot()));
		assertTrue(SubmissionQueue.fileIn(tmp.getRoot()).exists());
	}

	@Test
	public void unfinishedAndIncompleteRunsAreRefusedAndNothingIsWritten(){
		log("run-live", false, 3, 0);
		assertEquals(SubmissionQueue.Result.REFUSED, SubmissionQueue.enqueueRun(tmp.getRoot(), "run-live", "b", 1).result);
		log("run-old", true, 0, 0);
		SubmissionQueue.Queued old = SubmissionQueue.enqueueRun(tmp.getRoot(), "run-old", "b", 1);
		assertEquals("a run from before difficulty existed", RunSubmissionFactory.Refusal.VALUE_NOT_RECORDED, old.refusal);
		log("run-seg", true, 3, 1);
		assertEquals(RunSubmissionFactory.Refusal.HISTORY_STARTS_MID_RUN, SubmissionQueue.enqueueRun(tmp.getRoot(), "run-seg", "b", 1).refusal);
		assertEquals("no such run", SubmissionQueue.Result.REFUSED, SubmissionQueue.enqueueRun(tmp.getRoot(), "run-missing", "b", 1).result);
		assertEquals("bad run id", SubmissionQueue.Result.REFUSED, SubmissionQueue.enqueueRun(tmp.getRoot(), "../x", "b", 1).result);
		assertEquals(0, SubmissionQueue.pending(tmp.getRoot()));
		assertFalse(SubmissionQueue.fileIn(tmp.getRoot()).exists());
	}

	@Test
	public void flushingThroughTheOfflineGatewayKeepsTheQueueAndAWorkingGatewayEmptiesIt(){
		log("run-a", true, 3, 0);
		SubmissionQueue.enqueueRun(tmp.getRoot(), "run-a", "b", 1);
		com.shatteredpixel.shatteredpixeldungeon.apd.backend.Outbox.FlushResult off = SubmissionQueue.flush(tmp.getRoot(), new com.shatteredpixel.shatteredpixeldungeon.apd.backend.OfflineGateway(), 10_000_000);
		assertEquals(0, off.sent);
		assertEquals(1, SubmissionQueue.pending(tmp.getRoot()));
		final int[] got = new int[1];
		com.shatteredpixel.shatteredpixeldungeon.apd.backend.Gateway ok = new com.shatteredpixel.shatteredpixeldungeon.apd.backend.Gateway(){
			public Response send( String key, com.shatteredpixel.shatteredpixeldungeon.apd.backend.Envelope e ){
				got[0]++;
				return new Response(Status.OK, "");
			}
		};
		assertEquals(1, SubmissionQueue.flush(tmp.getRoot(), ok, 1_000_000_000L).sent);
		assertEquals(1, got[0]);
		assertEquals(0, SubmissionQueue.pending(tmp.getRoot()));
		assertEquals("an empty or missing queue is a no-op", 0, SubmissionQueue.flush(tmp.getRoot(), ok, 2_000_000_000L).sent);
		assertEquals(1, got[0]);
	}
}
