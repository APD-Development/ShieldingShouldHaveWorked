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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.WallSearchSession;
import com.shatteredpixel.shatteredpixeldungeon.effects.StairHighlight;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public final class WndWallSearchResume {
    private WndWallSearchResume() { }
    public static void show(){
        Hero hero=Dungeon.hero;if(hero==null||!hero.wallSearch.exists())return;
        WallSearchSession saved=hero.wallSearch;
        String note="Saved search on Floor "+saved.floor()+". Manual searches completed here: "+saved.searches()+".\n\nResume returns by a safe discovered route and performs one normal manual search. Movement and searching keep their normal costs. Visible enemies or danger stop the return; your saved position is kept.";
        boolean movement=hero.lastAction!=null;
        String[] options=movement?new String[]{"Resume wall search","Show saved search position","Resume previous movement","Forget this search","Cancel"}:new String[]{"Resume wall search","Show saved search position","Forget this search","Cancel"};
        GameScene.show(new WndOptions(Icons.get(Icons.INFO),"Remembered wall search",note,options){
            @Override protected void onSelect(int index){
                if(index==0){String problem=saved.resume(hero);if(problem!=null)GLog.w(problem);else if(hero.curAction instanceof WallSearchSession.Walk)hero.next();}
                else if(index==1){if(saved.matches(Dungeon.level)){StairHighlight.showKnown(saved.anchor());if(saved.target()>=0)StairHighlight.showKnown(saved.target());}else GLog.w("The saved search is on Floor "+saved.floor()+".");}
                else if(movement&&index==2)hero.resume();
                else if(index==(movement?3:2))saved.clear();
            }
        });
    }
}
