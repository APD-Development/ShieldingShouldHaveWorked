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
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.ConfirmationDecision;
/** Existing Pixel Dungeon dialog; Android Back has exactly the same result as Cancel. */
public class WndActionConfirmation extends WndOptions {
    private final ConfirmationDecision decision;
    public WndActionConfirmation(String title,String message,ConfirmationDecision decision){
        super(title,message,"Confirm","Cancel");this.decision=decision;
    }
    @Override protected void onSelect(int index){decision.choose(index==0);}
    @Override public void onBackPressed(){decision.choose(false);super.onBackPressed();}
}
