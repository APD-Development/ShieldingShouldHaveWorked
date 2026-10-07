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


package com.shatteredpixel.shatteredpixeldungeon.apd.query;

//A named way into a dataset: the dataset plus a starting query. Weapons, Armor, Rings, Trinkets and Artifacts are views of
//Items (a filter on category), Characters and Challenges are views of Runs (a grouping). Views store nothing: they are
//queries, which is also what saved queries will be.
public final class ViewDef {

	public final String id;
	public final String title;
	public final String description;
	private final Query initial;

	public ViewDef( String id, String title, String description, Query initial ){
		this.id = id;
		this.title = title;
		this.description = description;
		this.initial = initial;
	}

	public String dataset(){
		return initial.dataset;
	}

	//the query a screen starts from when this view is chosen
	public Query initialQuery(){
		return initial;
	}
}
