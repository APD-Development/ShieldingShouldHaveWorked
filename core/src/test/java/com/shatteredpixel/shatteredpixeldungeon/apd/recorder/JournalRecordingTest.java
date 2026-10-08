package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
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

	@Test
	public void revealedSecretsAreRecordedWithWhatTheyWere() throws Exception {
		RecorderTestSupport.Session s = ItemTestSupport.startRun(44L);
		String id = ApdRecorder.currentRunId();
		int door = ItemTestSupport.freeCell(), trap = ItemTestSupport.freeCell(), plain = ItemTestSupport.freeCell();
		Dungeon.level.map[door] = Terrain.SECRET_DOOR;
		Dungeon.level.map[trap] = Terrain.SECRET_TRAP;
		Dungeon.level.discover(door);
		Dungeon.level.discover(trap);
		Dungeon.level.discover(plain);                  //nothing hidden there: nothing to record
		ApdRecorder.flushForReading();
		ReadResult r = s.read(id);
		List<FactEvents.Observation> got = new ArrayList<>();
		for (ApdEvent e : r.events){
			if (!FactEvents.isFactEvent(e.type)) continue;
			for (FactEvents.Observation o : FactEvents.decode(e, r)) if (o.key.equals("explore.discover")) got.add(o);
		}
		assertEquals(2, got.size());
		assertEquals("Secret door", got.get(0).subject);
		assertArrayEquals(new long[]{ door }, got.get(0).values);
		assertEquals("Secret trap", got.get(1).subject);
	}

	@Test
	public void journalPagesFoundAndReadForTheFirstTimeAreRecorded() throws Exception {
		RecorderTestSupport.Session s = ItemTestSupport.startRun(45L);
		String id = ApdRecorder.currentRunId();
		Document doc = Document.ADVENTURERS_GUIDE;
		String page = doc.pageNames().iterator().next();
		doc.deletePage(page);                          //back to "not found" (debug builds start with every page read)
		assertEquals(true, doc.findPage(page));
		doc.findPage(page);                            //already found: not a change
		doc.readPage(page);
		doc.readPage(page);                            //already read: not a change
		ApdRecorder.flushForReading();
		ReadResult r = s.read(id);
		List<FactEvents.Observation> got = new ArrayList<>();
		for (ApdEvent e : r.events){
			if (!FactEvents.isFactEvent(e.type)) continue;
			for (FactEvents.Observation o : FactEvents.decode(e, r)) if (o.key.equals("journal.page")) got.add(o);
		}
		assertEquals(2, got.size());
		assertArrayEquals(new String[]{ "found", page }, got.get(0).tags);
		assertArrayEquals(new String[]{ "read", page }, got.get(1).tags);
		assertEquals("ADVENTURERS_GUIDE", got.get(0).subject);
	}
}
