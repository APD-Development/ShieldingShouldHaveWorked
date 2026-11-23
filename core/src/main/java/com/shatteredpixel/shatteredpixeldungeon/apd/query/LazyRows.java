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

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.lang.ref.SoftReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//The rows of a heavy dataset (every step, every turn's health, every hit), read from the row caches the first time something
//asks for them rather than when UIMSD opens. Opening UIMSD used to build every row of every dataset for every log: 20 long runs
//took 247 MB of a 256 MB phone heap (measured, Nov 23). Nothing is dropped: every recorded observation can still be asked for.
//
//Once read, the rows are kept compactly (ColumnarRows) behind a soft reference, so if the phone runs short of memory they are
//let go and read again the next time they are needed. isEmpty() and expected() answer from the counts noted at load time
//without reading anything.
final class LazyRows extends AbstractList<Row> {

	interface Source {
		/** Every row of the dataset (onlyRun null), or only one run's, in catalog order. */
		List<Row> load( String onlyRun );
	}

	private final int expected;
	private final Source source;
	private SoftReference<List<Row>> loaded;
	private int loads;

	LazyRows( int expected, Source source ){
		this.expected = expected;
		this.source = source;
	}

	/** How many rows the logs said this dataset has, without reading them. */
	int expected(){
		return expected;
	}

	/** How many times the rows were read from the caches (for tests). */
	synchronized int loads(){
		return loads;
	}

	synchronized boolean isLoaded(){
		return loaded != null && loaded.get() != null;
	}

	synchronized List<Row> data(){
		List<Row> d = loaded == null ? null : loaded.get();
		if (d == null){
			d = expected == 0 ? new ArrayList<Row>() : source.load(null);
			loads++;
			loaded = new SoftReference<>(d);
		}
		return d;
	}

	/** One run's rows. Reads only that run's logs unless the whole dataset is already in memory. */
	List<Row> forRun( String runId ){
		List<Row> d;
		synchronized (this){
			d = loaded == null ? null : loaded.get();
		}
		if (d == null) return expected == 0 ? new ArrayList<Row>() : source.load(runId);
		List<Row> out = new ArrayList<>();
		for (Row r : d) if (runId.equals(r.runId)) out.add(r);
		return out;
	}

	@Override
	public boolean isEmpty(){
		return expected == 0;
	}

	@Override
	public int size(){
		return data().size();
	}

	@Override
	public Row get( int index ){
		return data().get(index);
	}

	//one strong hold on the rows for the whole walk, so a walk never reads them twice
	@Override
	public Iterator<Row> iterator(){
		return data().iterator();
	}
}
