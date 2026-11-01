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

package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.watabou.input.BackTrace;

import org.junit.After;
import org.junit.Test;

import java.util.List;

public class BackTraceTest {

	@After
	public void reset(){
		BackTrace.resetForTest();
	}

	@Test
	public void disabledTracingIsACompleteNoOp(){
		BackTrace.resetForTest();
		long route = BackTrace.route("android-system");
		BackTrace.accepted(route);
		BackTrace.stage(route, "window-hide-end", "example.Window");
		assertEquals(0, route);
		assertTrue(BackTrace.snapshot().isEmpty());
	}

	@Test
	public void acceptedBackKeepsOneCorrelationAcrossThePipeline(){
		BackTrace.setEnabled(true);
		long route = BackTrace.route("android-system");
		BackTrace.accepted(route);
		BackTrace.stage(route, "key-down-enqueued", "");
		BackTrace.beginDispatch(route);
		BackTrace.stage(route, "window-hide-begin", "example.Window");
		BackTrace.stage(route, "window-hide-end", "example.Window");
		BackTrace.endDispatch(route);

		String lines = String.join("\n", BackTrace.snapshot());
		assertTrue(lines.contains("route=" + route + " stage=route"));
		assertTrue(lines.contains("route=" + route + " stage=accepted"));
		assertTrue(lines.contains("route=" + route + " stage=render-dispatch-begin"));
		assertTrue(lines.contains("route=" + route + " stage=window-hide-end"));
		assertTrue(lines.contains("route=" + route + " stage=render-dispatch-end"));
		assertEquals(0, BackTrace.currentDispatch());
	}

	@Test
	public void rejectedDuplicateDoesNotReplaceTheAcceptedRoute(){
		BackTrace.setEnabled(true);
		long accepted = BackTrace.route("android-system");
		BackTrace.accepted(accepted);
		long duplicate = BackTrace.route("gdx-key");
		BackTrace.rejected(duplicate, "BackGate duplicate");
		BackTrace.stage(accepted, "window-hide-end", "example.Window");

		List<String> lines = BackTrace.snapshot();
		String all = String.join("\n", lines);
		assertTrue(all.contains("route=" + duplicate + " stage=rejected"));
		assertTrue(all.contains("route=" + accepted + " stage=window-hide-end"));
	}
}
