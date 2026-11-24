package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FloorMaps;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKinds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//UIMSD item 10 (run replay): each floor's true layout is kept once per session as an internal floor.map fact
public class FloorMapRecordingTest {

	@Before
	@After
	public void cleanUp(){
		ApdRecorder.reset();
	}

	private static List<FactEvents.Observation> maps( ReadResult result ){
		List<FactEvents.Observation> out = new ArrayList<>();
		for (ApdEvent event : result.events){
			if (!FactEvents.isFactEvent(event.type)) continue;
			for (FactEvents.Observation o : FactEvents.decode(event, result)){
				if (o.key.equals("floor.map")) out.add(o);
			}
		}
		return out;
	}

	@Test
	public void kindIsInternal(){
		assertTrue(FactKinds.get("floor.map").isInternal());
	}

	@Test
	public void eachFloorIsMappedOnceWithItsRealLayout() throws Exception {
		RecorderTestSupport.Session session = ItemTestSupport.startRun(311L);
		String runId = ApdRecorder.currentRunId();
		String floor1 = FloorMaps.encode(Dungeon.level.map, Dungeon.level.width(), Dungeon.level.height());
		int w1 = Dungeon.level.width(), h1 = Dungeon.level.height();
		int entrance = Dungeon.level.entrance();

		RecorderTestSupport.enterLevel(2);
		String floor2 = FloorMaps.encode(Dungeon.level.map, Dungeon.level.width(), Dungeon.level.height());
		//arriving on floor 2 again in the same session adds nothing
		Dungeon.switchLevel(Dungeon.level, -1);

		ApdRecorder.flushForReading();
		List<FactEvents.Observation> got = maps(session.read(runId));
		assertEquals(2, got.size());
		assertArrayEquals(new long[]{ 1, 0, w1, h1 }, got.get(0).values);
		assertEquals(floor1, got.get(0).tags[0]);
		assertEquals(2, got.get(1).values[0]);
		assertEquals(floor2, got.get(1).tags[0]);
		assertTrue(FloorMaps.valid(got.get(0).tags[0], w1, h1));
		assertEquals(FloorMaps.ENTRANCE, got.get(0).tags[0].charAt(entrance));
		assertTrue(got.get(0).tags[0].indexOf(FloorMaps.EXIT) >= 0);
	}
}
