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

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ByteSink;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.EventTypes;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunTimeline;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.StatsStore;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

//Recorder recovery must isolate damaged lifecycle payloads just as the statistics readers do.
public class RunRecorderMalformedLifecycleTest {

	@Test
	public void malformedLifecyclePayloadCannotHideLaterRulesetState() throws Exception {
		ReadResult log = new ReadResult();
		log.strings.add("old-rules");
		log.strings.add("new-rules");
		log.strings.add("new-rules-text");
		log.events.add(new ApdEvent(0, EventTypes.RUN_START, 0, 0, new byte[0]));
		byte[] change = new ByteSink().writeUnsigned(0).writeUnsigned(1).writeUnsigned(2).writeUnsigned(4).toByteArray();
		log.events.add(new ApdEvent(1, EventTypes.RULESET_CHANGE, 0, 1, change));

		Constructor<RunRecorder> constructor = RunRecorder.class.getDeclaredConstructor(
				StatsStore.class, int.class, String.class, String.class, int.class);
		constructor.setAccessible(true);
		RunRecorder recorder = constructor.newInstance(null, 0, "segment", "logical", 0);
		Method restore = RunRecorder.class.getDeclaredMethod("restoreTrackedState", ReadResult.class, RunTimeline.class);
		restore.setAccessible(true);
		restore.invoke(recorder, log, RunTimeline.of(log.events));

		assertEquals("new-rules", field(recorder, "rulesetId"));
		assertEquals(1, field(recorder, "rulesetChanges"));
	}

	private static Object field( RunRecorder recorder, String name ) throws Exception {
		Field field = RunRecorder.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(recorder);
	}
}
