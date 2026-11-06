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
package com.shatteredpixel.shatteredpixeldungeon.apd.mechanics;

import com.shatteredpixel.shatteredpixeldungeon.items.keys.*;

/** Explains existing lock matching without testing or opening any map location. */
public final class KeyExplanation {
    private KeyExplanation(){}
    public static String details(Key key){
        String target=key instanceof GoldenKey?"a locked chest":key instanceof CrystalKey?"a crystal chest or crystal door":key instanceof IronKey?"an ordinary locked door":key instanceof WornKey?"a locked exit":"a matching lock";
        String text="_Key type, floor and use_\nThis key opens "+target+". ";
        text+=key.depth>0?"It belongs to Floor "+key.depth+"; a key from a different floor cannot substitute. ":"No floor is recorded for this key; a matching floor cannot be assumed. ";
        return text+"Collected keys are recorded in the journal rather than occupying ordinary backpack slots. One ordinary successful unlocking consumes one matching key and costs "+ObfuscationExplanation.number(Key.TIME_TO_UNLOCK)+" game-time unit, in addition to any movement needed to reach the lock. Doors require an adjacent position; chests can also be opened while standing on them. Ordinary floor keys do not unlock sub-floor branch locks. Crystal keys are shared between crystal doors and crystal chests on their floor; using one spends that resource for either kind of lock. A key does not reveal unseen rooms or guarantee that every remaining lock can be opened. Skeleton Key artifact effects retain their separate replacement and curse rules.";
    }
}
