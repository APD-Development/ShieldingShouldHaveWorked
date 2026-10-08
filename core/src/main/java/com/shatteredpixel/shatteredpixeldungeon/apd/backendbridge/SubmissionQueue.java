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
import com.shatteredpixel.shatteredpixeldungeon.apd.backend.Gateway;
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

// Local queue of runs the player asked to submit. Nothing here touches a network and nothing sends the queue yet.
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

	private static Id queueId( String runId ){
		try {
			return Id.of(Id.Kind.OUTBOX, "ob_" + runId);
		} catch (IllegalArgumentException e){
			return null;
		}
	}

	private static Outbox.Item find( File historyRoot, String runId ){
		Id id = queueId(runId);
		if (historyRoot == null || id == null) return null;
		try {
			Outbox box = new Outbox();
			OutboxStore.load(box, fileIn(historyRoot));
			for (Outbox.Item i : box.items()) if (i.id.equals(id)) return i;
		} catch (IOException ignored){
		}
		return null;
	}

	public static boolean isQueued( File historyRoot, String runId ){
		return find(historyRoot, runId) != null;
	}

	//The exact text that would be sent for a queued run, so the player can read it before anything ever leaves the device. null if the run is not queued.
	public static String preview( File historyRoot, String runId ){
		Outbox.Item i = find(historyRoot, runId);
		return i == null ? null : i.envelope.schema + " " + i.envelope.major + "." + i.envelope.minor + "\n" + i.envelope.payload;
	}

	//Takes a run back out of the queue. Returns false if it was not queued or the queue could not be saved.
	public static boolean withdraw( File historyRoot, String runId ){
		Id id = queueId(runId);
		if (historyRoot == null || id == null) return false;
		try {
			Outbox box = new Outbox();
			OutboxStore.load(box, fileIn(historyRoot));
			if (!box.remove(id)) return false;
			OutboxStore.save(box, fileIn(historyRoot));
			return true;
		} catch (IOException e){
			return false;
		}
	}

	//Sends what is due through 'gateway' and saves what is left. With the offline gateway (the only one that exists) nothing changes. Never throws.
	public static Outbox.FlushResult flush( File historyRoot, Gateway gateway, long nowMillis ){
		return flush(historyRoot, gateway, nowMillis, false);
	}

	//ignoreBackoff: the player pressed "send now", so every queued item is tried regardless of its retry schedule
	public static Outbox.FlushResult flush( File historyRoot, Gateway gateway, long nowMillis, boolean ignoreBackoff ){
		Outbox.FlushResult none = new Outbox.FlushResult();
		if (historyRoot == null) return none;
		try {
			Outbox box = new Outbox();
			OutboxStore.load(box, fileIn(historyRoot));
			if (box.size() == 0) return none;
			if (ignoreBackoff) for (Outbox.Item i : box.items()) i.nextAttemptAtMillis = nowMillis;
			Outbox.FlushResult r = box.flush(gateway, nowMillis);
			OutboxStore.save(box, fileIn(historyRoot));
			return r;
		} catch (IOException e){
			return none;
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
