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
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdGuide;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.UimsdTutorial;

import java.util.ArrayList;
import java.util.List;

//APD: the UIMSD Guide: the interactive tour first, then every topic. A topic can be read, and (for most) shown on the real screen by
//starting the tour at the step that teaches it.
public final class WndUimsdGuide {

	private WndUimsdGuide(){}

	public interface Starter {
		//start the interactive tour at this step (0 for the beginning)
		void startTour( int stepIndex );
	}

	public static void show( final Starter starter ){
		final List<UimsdGuide.Topic> topics = UimsdGuide.topics();
		List<String> labels = new ArrayList<>();
		labels.add("> Take the interactive tour");
		for (UimsdGuide.Topic t : topics) labels.add(t.title);
		ShatteredPixelDungeon.scene().addToFront(new WndUimsdList("Guide", "Read a topic, or be shown it on the real screen.", labels, null, new WndUimsdList.Picker(){
			@Override
			public void onPick( int i, boolean checked ){
				if (i == 0){
					starter.startTour(0);
					return;
				}
				final UimsdGuide.Topic topic = topics.get(i - 1);
				final int step = UimsdTutorial.indexOf(topic.tourStep);
				ShatteredPixelDungeon.scene().addToFront(new WndUimsdTopic(topic, step >= 0, new Runnable(){
					@Override
					public void run(){
						starter.startTour(step);
					}
				}));
			}
		}));
	}
}
