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

package com.shatteredpixel.shatteredpixeldungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import org.junit.Test;
import static org.junit.Assert.*;

public class WandmakerRewardLevelsTest {
    @Test public void bothChoicesRevealUpgradeLevelButKeepIdentityAndCurseHidden() {
        Wand previous1=Wandmaker.Quest.wand1,previous2=Wandmaker.Quest.wand2;
        try {
            Wand first=new WandOfMagicMissile(),second=new WandOfCorrosion();
            first.level(2);
            second.level(1);
            first.levelKnown=second.levelKnown=false;
            first.cursed=true;
            first.cursedKnown=false;
            second.cursedKnown=false;
            Wandmaker.Quest.wand1=first;
            Wandmaker.Quest.wand2=second;
            Wandmaker.Quest.revealOfferedLevels();
            assertTrue(first.levelKnown);
            assertTrue(second.levelKnown);
            assertEquals(2,first.level());
            assertEquals(1,second.level());
            assertTrue(first.cursed);
            assertFalse(first.cursedKnown);
            assertFalse(second.cursedKnown);
        } finally {
            Wandmaker.Quest.wand1=previous1;
            Wandmaker.Quest.wand2=previous2;
        }
    }
    @Test public void absentChoicesStayAbsentAndDoNotCrash(){
        Wand before1=Wandmaker.Quest.wand1,before2=Wandmaker.Quest.wand2;
        try { Wandmaker.Quest.wand1=null; Wandmaker.Quest.wand2=null; Wandmaker.Quest.revealOfferedLevels();assertNull(Wandmaker.Quest.wand1); }
        finally{Wandmaker.Quest.wand1=before1;Wandmaker.Quest.wand2=before2;}
    }
}
