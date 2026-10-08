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

import com.shatteredpixel.shatteredpixeldungeon.apd.backend.Canon;
import com.shatteredpixel.shatteredpixeldungeon.apd.backend.Id;
import com.shatteredpixel.shatteredpixeldungeon.apd.backend.Outbox;
import com.shatteredpixel.shatteredpixeldungeon.apd.backend.OutboxStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.backend.RunSubmissionCodec;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunFactsBuilder;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.StatsStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

//Local queue of runs the player asked to submit. Nothing here talks to a network: queuing a run only writes a line to a file in the history
//folder, and nothing sends the queue yet (no server is chosen). The point is that the "submit" action, the minimal-data rule and the stored queue
//exist and are tested before any provider does.
public final class SubmissionQueue {

	public static final String FILE_NAME = "submission_queue.txt";

	public enum Result { QUEUED, ALREADY_QUEUED, REFUSED, FAILED }

	public static final class Queued {
		public final Result result;
		public final RunSubmissionFactory.Refusal refusal;   //only for REFUSED by the factory
		public final String detail;

		Queued( Result result, RunSubmissionFactory.Refusal refusal, String detail ){
			this.result = result;
			this.refusal = refusal;
			this.detail = detail;
		}
	}

	private SubmissionQueue(){}

	public static File fileIn( File historyRoot ){
		return new File(historyRoot, FILE_NAME);
	}

	public static int pending( File historyRoot ){
		try {
			Outbox box = new Outbox();
			OutboxStore.load(box, fileIn(historyRoot));
			return box.size();
		} catch (IOException e){
			return 0;
		}
	}

	//Builds the submission from the run's own log and queues it. A run is only taken from a single, complete log: runs spread over several
	//log segments are refused for now (merging segments for a submission is not designed yet).
	public static Queued enqueueRun( File historyRoot, String runId, String appBuild, long nowMillis ){
		if (historyRoot == null || !StatsStore.isValidRunId(runId)) return new Queued(Result.REFUSED, null, "no such run");
		File log = new StatsStore(historyRoot).logFile(runId);
		byte[] bytes;
		try {
			bytes = readAll(log);
		} catch (IOException e){
			return new Queued(Result.REFUSED, null, "the run's log cannot be read");
		}
		RunFacts facts = RunFactsBuilder.fromLog(RunLogReader.parse(bytes), runId);
		if (facts.numOr(FactKeys.SEGMENT, 0) != 0) return new Queued(Result.REFUSED, RunSubmissionFactory.Refusal.HISTORY_STARTS_MID_RUN, "this run is spread over several log segments");
		RunSubmissionFactory.Outcome o = RunSubmissionFactory.from(facts, Canon.hex(Canon.sha256(bytes)), appBuild);
		if (o.submission == null) return new Queued(Result.REFUSED, o.refusal, o.detail);
		Id id;
		try {
			id = Id.of(Id.Kind.OUTBOX, "ob_" + runId);
		} catch (IllegalArgumentException e){
			return new Queued(Result.REFUSED, null, "the run id is too long to queue");
		}
		try {
			Outbox box = new Outbox();
			OutboxStore.load(box, fileIn(historyRoot));
			int before = box.size();
			box.enqueue(id, RunSubmissionCodec.toEnvelope(o.submission), nowMillis);
			if (box.size() == before) return new Queued(Result.ALREADY_QUEUED, null, "");
			OutboxStore.save(box, fileIn(historyRoot));
			return new Queued(Result.QUEUED, null, "");
		} catch (IOException e){
			return new Queued(Result.FAILED, null, "the queue could not be saved: " + e.getMessage());
		}
	}

	private static byte[] readAll( File f ) throws IOException {
		try (InputStream in = new FileInputStream(f)){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int n;
			while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
			return out.toByteArray();
		}
	}
}
