/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GameFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//Plan 3.13: the hero's condition is recorded at the start of every turn, at full detail.
public class HeroStateRecordingTest {

	@Before
	@After
	public void cleanUp(){
		ApdRecorder.reset();
	}

	@Test
	public void healthShieldAndHungerAreRecordedEveryTurnInOrder() throws Exception {
		RecorderTestSupport.Session s = ItemTestSupport.startRun(41L);
		String id = ApdRecorder.currentRunId();
		int[] hp = { 20, 20, 17, 17, 12, 12, 12, 15, 16, 5 };
		for (int i = 0; i < hp.length; i++){
			Dungeon.hero.HT = 20;
			Dungeon.hero.HP = hp[i];
			Statistics.duration += 1;
			ApdPlay.onHeroTurn();
		}
		ApdRecorder.flushForReading();
		List<FactEvents.Observation> got = observations(s.read(id));
		assertEquals("one observation per turn", hp.length, got.size());
		List<FactEvents.Observation> mine = got;
		for (int i = 0; i < hp.length; i++){
			FactEvents.Observation o = mine.get(i);
			assertEquals(GameFacts.HERO_STATE.key, o.key);
			assertEquals(hp[i], o.values[0]);
			assertEquals(20, o.values[1]);
			assertTrue("shield and hunger are recorded as numbers", o.values.length == 4 && o.values[2] >= 0 && o.values[3] >= 0);
		}
	}

	private static List<FactEvents.Observation> observations( ReadResult r ){
		List<FactEvents.Observation> out = new ArrayList<>();
		for (ApdEvent e : r.events){
			if (!FactEvents.isFactEvent(e.type)) continue;
			for (FactEvents.Observation o : FactEvents.decode(e, r)) if (o.key.equals(GameFacts.HERO_STATE.key)) out.add(o);
		}
		return out;
	}
}
