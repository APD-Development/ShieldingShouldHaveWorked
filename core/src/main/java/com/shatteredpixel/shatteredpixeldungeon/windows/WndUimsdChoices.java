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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import java.util.ArrayList;
import java.util.List;

//UIMSD confirmations/report actions use the same wrapped, responsive rows as its other menus.
//The explanation scrolls with the choices, so long messages cannot push buttons off screen.
public class WndUimsdChoices extends WndUimsdList {
	public WndUimsdChoices(String title, String message, String... options){
		this(title, message, options, new Selection());
	}

	private WndUimsdChoices(String title, String message, String[] options, Selection selection){
		super(title, "", false, contentEntries(message, options), selection);
		selection.window = this;
	}

	static List<Entry> contentEntries(String message, String... options){
		List<Entry> entries = new ArrayList<>();
		if (message != null && !message.isEmpty()) entries.add(Entry.note(message, 6));
		for (String option : options) entries.add(Entry.action(option));
		return entries;
	}

	protected void onSelect(int index){}

	private static final class Selection implements Picker {
		WndUimsdChoices window;
		@Override public void onPick(int index, boolean checked){
			window.onSelect(index);
		}
	}
}
