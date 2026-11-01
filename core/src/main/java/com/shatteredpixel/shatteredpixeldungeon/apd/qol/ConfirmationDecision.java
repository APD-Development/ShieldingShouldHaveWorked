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

package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

/** A dialog can resolve once, including Back and repeated/late input. No game time or RNG. */
public final class ConfirmationDecision {
    private Runnable confirmed, cancelled;
    private boolean decided;
    public ConfirmationDecision(Runnable confirmed,Runnable cancelled){this.confirmed=confirmed;this.cancelled=cancelled;}
    public void choose(boolean confirm){
        Runnable action;
        synchronized(this){if(decided)return;decided=true;action=confirm?confirmed:cancelled;confirmed=cancelled=null;}
        if(action!=null)action.run();
    }
    public synchronized boolean decided(){return decided;}
}
