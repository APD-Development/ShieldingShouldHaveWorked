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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;

//A character sprite for headless tests: it does nothing, so the game's combat code can run without a screen.
class QuietSprite extends CharSprite {

	QuietSprite( Char owner ){
		super();
		ch = owner;
		owner.sprite = this;
	}

	@Override public void showStatus( int color, String text, Object... args ){}
	@Override public void showStatusWithIcon( int color, String text, int icon, Object... args ){}
	@Override public void die(){}
	@Override public void bloodBurstA( PointF from, int damage ){}
	@Override public void flash(){}
	@Override public void burst( int color, int n ){}
	@Override public Emitter emitter(){ return new Emitter(); }
	@Override public Emitter centerEmitter(){ return new Emitter(); }
	@Override public Emitter bottomEmitter(){ return new Emitter(); }
	@Override public void attack( int cell ){}
	@Override public void zap( int cell ){}
	@Override public void idle(){}
	@Override public void showSleep(){}
	@Override public void hideSleep(){}
	@Override public void showAlert(){}
	@Override public void hideAlert(){}
	@Override public void showLost(){}
	@Override public void hideLost(){}
	@Override public void add( State state ){}
	@Override public void remove( State state ){}
	@Override public void resetColor(){}
	@Override public void kill(){}
}
