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

package com.shatteredpixel.shatteredpixeldungeon.apd.replay;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Dataset;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Datasets;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.ReplayFloor;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.Row;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.SeedFloors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//The game-aware half of seed floors: reads the run's seed, class and rules from its row, replays the floors (FloorReplay) and hands them to
//the pure assembly in apd.query. Replays are cached in memory (a few runs).
public final class SeedFloorsRunner implements SeedFloors.Provider {

	public static final SeedFloorsRunner INSTANCE = new SeedFloorsRunner();

	private SeedFloorsRunner(){}

	private static final int CACHE_SIZE = 4;
	private static final LinkedHashMap<String, List<ReplayFloor>> CACHE = new LinkedHashMap<String, List<ReplayFloor>>(8, 0.75f, true){
		@Override
		protected boolean removeEldestEntry( Map.Entry<String, List<ReplayFloor>> eldest ){
			return size() > CACHE_SIZE;
		}
	};

	public static synchronized void clearCache(){
		CACHE.clear();
	}

	@Override
	public boolean applies( Catalog cat, Dataset ds, Row row ){
		return cat != null && ds != null && Datasets.RUNS.equals(ds.def.id) && row != null && row.finished;
	}

	@Override
	public SeedFloors.Built build( Catalog cat, Row run ) throws SeedFloors.NotAvailable {
		SeedFloors.Spec spec = SeedFloors.spec(cat, run);
		HeroClass cls;
		try {
			cls = HeroClass.valueOf(spec.heroClass);
		} catch (IllegalArgumentException e){
			throw new SeedFloors.NotAvailable("This run's hero class (" + spec.heroClass + ") is not known to this version.");
		}
		String key = spec.key() + "|" + FloorReplay.ENGINE_VERSION + "|" + FloorReplay.CONTEXT_ID;
		List<ReplayFloor> floors;
		synchronized (SeedFloorsRunner.class){
			floors = CACHE.get(key);
		}
		if (floors == null){
			try {
				floors = FloorReplay.replay(spec.seed, spec.challenges, spec.exoticChance, spec.difficulty, spec.balance, cls, FloorReplay.LAST_FLOOR, null);
			} catch (FloorReplay.Refused e){
				throw new SeedFloors.NotAvailable("Seed floors cannot be built right now: " + e.getMessage() + ".");
			}
			synchronized (SeedFloorsRunner.class){
				CACHE.put(key, floors);
			}
		}
		return SeedFloors.assemble(spec, SeedFloors.recorded(cat, spec.runId), floors);
	}
}
