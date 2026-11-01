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

package com.watabou.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.Input;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KeyEventBackPriorityTest {

	@Test
	public void noQueuedEventsOrOnlyOrdinaryEventsDoNotNeedEarlyDispatch(){
		assertFalse(KeyEvent.containsBackPress(Collections.emptyList()));
		assertFalse(KeyEvent.containsBackPress(Arrays.asList(
				new KeyEvent(Input.Keys.ENTER, true),
				new KeyEvent(Input.Keys.BACK, false))));
	}

	@Test
	public void queuedBackDownIsRecognizedWithOrWithoutRelease(){
		assertTrue(KeyEvent.containsBackPress(Collections.singletonList(
				new KeyEvent(Input.Keys.BACK, true))));
		assertTrue(KeyEvent.containsBackPress(Arrays.asList(
				new KeyEvent(Input.Keys.A, true),
				new KeyEvent(Input.Keys.BACK, true),
				new KeyEvent(Input.Keys.BACK, false))));
	}

	@Test
	public void PriorityCheckDoesNotConsumeReorderOrDuplicateInputs(){
		KeyEvent first = new KeyEvent(Input.Keys.A, true);
		KeyEvent back = new KeyEvent(Input.Keys.BACK, true, 31);
		KeyEvent release = new KeyEvent(Input.Keys.BACK, false, 31);
		List<KeyEvent> pending = new ArrayList<>(Arrays.asList(first, back, release));
		assertTrue(KeyEvent.containsBackPress(pending));
		assertTrue(KeyEvent.containsBackPress(pending));
		assertEquals(Arrays.asList(first, back, release), pending);
		assertEquals(31, pending.get(1).backTraceId);
	}
}
