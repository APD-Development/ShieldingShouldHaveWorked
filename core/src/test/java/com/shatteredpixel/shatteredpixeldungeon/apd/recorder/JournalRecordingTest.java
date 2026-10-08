package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//Landmarks the journal notes and keys picked up or used are recorded when they happen; repeating one that is already there is not a change.
public class JournalRecordingTest {

	@Before
	@After
	public void cleanUp(){
		ApdRecorder.reset();
	}

	@Test
	public void landmarksAndKeysAreRecordedWhenTheJournalChanges() throws Exception {
		RecorderTestSupport.Session s = ItemTestSupport.startRun(43L);
		String id = ApdRecorder.currentRunId();
		Notes.add(Notes.Landmark.SHOP, 7);
		Notes.add(Notes.Landmark.SHOP, 7);              //already noted: not a change
		Notes.remove(Notes.Landmark.SHOP, 7);
		Notes.remove(Notes.Landmark.SHOP, 7);           //already gone
		Notes.add(new IronKey(7));
		Notes.remove(new IronKey(7));
		ApdRecorder.flushForReading();
		ReadResult r = s.read(id);
		List<FactEvents.Observation> got = new ArrayList<>();
		for (ApdEvent e : r.events){
			if (!FactEvents.isFactEvent(e.type)) continue;
			for (FactEvents.Observation o : FactEvents.decode(e, r)) if (o.key.startsWith("journal.")) got.add(o);
		}
		assertEquals(4, got.size());
		assertEquals("journal.landmark", got.get(0).key);
		assertEquals("SHOP", got.get(0).subject);
		assertArrayEquals(new long[]{ 7 }, got.get(0).values);
		assertArrayEquals(new String[]{ "added" }, got.get(0).tags);
		assertArrayEquals(new String[]{ "removed" }, got.get(1).tags);
		assertEquals("journal.key", got.get(2).key);
		assertEquals("items.keys.IronKey", got.get(2).subject);
		assertArrayEquals(new long[]{ 7, 1 }, got.get(2).values);
		assertArrayEquals(new String[]{ "added" }, got.get(2).tags);
		assertArrayEquals(new String[]{ "used" }, got.get(3).tags);
	}
}
