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
        String hint = SPDSettings.autoEnterStairs()
                ? "Walk to discovered stairs and take them automatically. Stops for enemies, damage or danger."
                : "Walk to discovered stairs. Stops for enemies, damage or danger. Enable Auto-enter Stairs in APD Settings to change floors on arrival.";
        GameScene.show(new WndOptions(Icons.get(Icons.STAIRS),"Stairs",hint,"Stairs Up","Stairs Down"){
            @Override protected void onSelect(int index){int target=StairNavigation.discoveredStair(Dungeon.level,index==0);com.shatteredpixel.shatteredpixeldungeon.effects.StairHighlight.showKnown(target);String problem=StairNavigation.begin(Dungeon.hero,index==0);if(problem!=null)GLog.w(problem);else Dungeon.hero.next();}
        });
    }
}
