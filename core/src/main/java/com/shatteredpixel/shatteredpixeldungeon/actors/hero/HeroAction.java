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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

public class HeroAction {
	
	public int dst;
	
	public static class Move extends HeroAction {
		public enum Mode {
			STEP("step"), TRAVEL("travel"), STAIRS("stairs");

			private final String tag;
			Mode( String tag ) { this.tag = tag; }
		}

		private final Mode apdMode;
		private boolean apdRecorded;

		public Move( int dst ) {
			this(dst, Mode.STEP);
		}

		public Move( int dst, Mode apdMode ) {
			this.dst = dst;
			this.apdMode = apdMode == null ? Mode.STEP : apdMode;
		}

		public String apdModeTag() {
			return apdMode.tag;
		}

		public boolean apdStartRecording() {
			if (apdRecorded) return false;
			apdRecorded = true;
			return true;
		}
	}
	
	public static class PickUp extends HeroAction {
		public PickUp( int dst ) {
			this.dst = dst;
		}
	}

	/** APD (Chris, Nov 22): pick up one chosen item from the pile the hero stands on, from the "Items on tile" list. */
	public static class PickUpItem extends PickUp {
		public final com.shatteredpixel.shatteredpixeldungeon.items.Item item;
		public PickUpItem( int dst, com.shatteredpixel.shatteredpixeldungeon.items.Item item ) {
			super( dst );
			this.item = item;
		}
	}

	/** APD walk-over pickup (Chris, Nov 21): keeps picking up the pile you stepped on, one item per pickup. */
	public static class WalkoverPickUp extends PickUp {
		/** The floor it was queued on; it never runs anywhere else. */
		public final com.shatteredpixel.shatteredpixeldungeon.levels.Level level;
		/** Where the walk was going when it stepped on the pile; it carries on there after the pickup (-1: nowhere). */
		public int resumeTo = -1;
		/** The walk itself, so it resumes with its own kind (stairs shortcut, Wall Search walk) and mode. */
		public Move resumeWalk;
		/** Chris, Nov 25: when the walk was going to pick something up (he tapped an item), that pickup, so it still happens. */
		public PickUp resumePick;
		public WalkoverPickUp( int dst ) {
			super( dst );
			level = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;
		}
	}
	
	public static class OpenChest extends HeroAction {
		public OpenChest( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class Buy extends HeroAction {
		public Buy( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class Interact extends HeroAction {
		public Char ch;
		public Interact( Char ch ) {
			this.ch = ch;
		}
	}
	
	public static class Unlock extends HeroAction {
		public Unlock( int door ) {
			this.dst = door;
		}
	}
	
	public static class LvlTransition extends HeroAction {
		public LvlTransition(int stairs ) {
			this.dst = stairs;
		}
	}

	public static class Mine extends HeroAction {
		public Mine( int wall ) {
			this.dst = wall;
		}
	}
	
	public static class Alchemy extends HeroAction {
		public Alchemy( int pot ) {
			this.dst = pot;
		}
	}
	
	public static class Attack extends HeroAction {
		public Char target;
		public Attack( Char target ) {
			this.target = target;
		}
	}
}
