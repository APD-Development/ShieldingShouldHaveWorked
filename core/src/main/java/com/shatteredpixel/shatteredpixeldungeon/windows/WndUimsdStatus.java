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


package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.StatusText;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdModel;

import java.util.ArrayList;
import java.util.List;

//APD: every line of the status strip in full. A line that says something was left out can be tapped to include it.
public final class WndUimsdStatus {

	private WndUimsdStatus(){}

	public static void show( final UimsdModel m ){
		final List<StatusText.Line> lines = m.status();
		List<String> labels = new ArrayList<>();
		for (StatusText.Line l : lines) labels.add((l.action.isEmpty() ? "" : "> ") + l.text);
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("What you are looking at",
				"Lines starting with > can be tapped to include what was left out.", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				String a = lines.get(i).action;
				if (!a.isEmpty()) m.applyAction(a);
			}
		}));
	}
}
