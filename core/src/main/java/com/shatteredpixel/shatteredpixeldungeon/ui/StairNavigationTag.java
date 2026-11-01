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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.navigation.StairNavigation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;

public final class StairNavigationTag extends Tag {
    private Image icon;
    public StairNavigationTag(){ super(0xA3A695);setSize(SIZE,SIZE);visible=false; }
    @Override protected void createChildren(){super.createChildren();add(icon=Icons.get(Icons.STAIRS));}
    @Override protected void layout(){super.layout();icon.x=flipped ? x+width-(SIZE+icon.width())/2f-1 : x+(SIZE-icon.width())/2f+1;icon.y=y+(height-icon.height)/2f;PixelScene.align(icon);}
    @Override public void update(){visible=SPDSettings.stairNavigationShortcut() && Dungeon.hero!=null && Dungeon.hero.isAlive();super.update();}
    @Override protected String hoverText(){return "Navigate to discovered stairs";}
    @Override protected void onClick(){
        super.onClick();
        if(Dungeon.hero==null || !Dungeon.hero.ready) return;
        final StairNavigation.Route[] routes=StairNavigation.availableRoutes(Dungeon.level);
        final boolean returning=routes.length==1 && routes[0]==StairNavigation.Route.RETURN_TO_MAIN_DUNGEON;
        String hint;
        String title;
        String[] options;
        if(returning){
            title="Quest Exit";
            hint=SPDSettings.autoEnterStairs()
                    ? "Walk to the discovered quest exit and return to the main dungeon automatically. Stops for enemies, damage or danger."
                    : "Walk to the discovered quest exit. Stops for enemies, damage or danger. Enable Auto-enter Stairs in APD Settings to return to the main dungeon on arrival.";
            options=new String[]{"Return to Main Dungeon"};
        } else {
            title="Stairs";
            hint=SPDSettings.autoEnterStairs()
                    ? "Walk to discovered stairs and take them automatically. Stops for enemies, damage or danger."
                    : "Walk to discovered stairs. Stops for enemies, damage or danger. Enable Auto-enter Stairs in APD Settings to change floors on arrival.";
            options=new String[]{"Stairs Up","Stairs Down"};
        }
        GameScene.show(new WndOptions(Icons.get(Icons.STAIRS),title,hint,options){
            @Override protected void onSelect(int index){
                StairNavigation.Route route=routes[index];
                int target=StairNavigation.discoveredTarget(Dungeon.level,route);
                com.shatteredpixel.shatteredpixeldungeon.effects.StairHighlight.showKnown(target);
                String problem=StairNavigation.begin(Dungeon.hero,route);
                if(problem!=null)GLog.w(problem);else Dungeon.hero.next();
            }
        });
    }
}
