package com.shatteredpixel.shatteredpixeldungeon.apd.navigation;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.*;
import com.watabou.utils.PathFinder;

/** A transient, ordinary Move action: never activates a level transition. */
public final class StairNavigation {
    private StairNavigation() {}
    public static final class Walk extends HeroAction.Move {
        public final Level level;
        public Walk(int destination, Level level) { super(destination, HeroAction.Move.Mode.STAIRS); this.level=level; }
    }
    public static int discoveredStair(Level level, boolean up) {
        if (level==null || level.transitions==null) return -1;
        for (LevelTransition transition:level.transitions) {
            boolean direction=up ? transition.type==LevelTransition.Type.REGULAR_ENTRANCE || transition.type==LevelTransition.Type.SURFACE
                    : transition.type==LevelTransition.Type.REGULAR_EXIT;
            int cell=transition.cell();
            if (direction && cell>=0 && cell<level.length() && (level.visited[cell] || level.mapped[cell])) return cell;
        }
        return -1;
    }
    public static boolean safeStep(Level level, Hero hero, int cell) {
        if (level==null || cell<0 || cell>=level.length() || !(level.visited[cell] || level.mapped[cell])
                || !level.passable[cell] || level.avoid[cell] || level.pit[cell]) return false;
        Trap trap=level.traps.get(cell);
        if (trap!=null && trap.visible && trap.active) return false;
        if (hero.fieldOfView!=null && hero.fieldOfView[cell]) {
            if (Actor.findChar(cell)!=null && cell!=hero.pos) return false;
            if (level.blobs!=null) for (Class<? extends Blob> type: DANGERS) {
                Blob blob=level.blobs.get(type);
                if (blob!=null && blob.cur!=null && cell<blob.cur.length && blob.cur[cell]>0) return false;
            }
        }
        return true;
    }
    @SuppressWarnings("unchecked") private static final Class<? extends Blob>[] DANGERS = new Class[]{Fire.class,ToxicGas.class,CorrosiveGas.class,Electricity.class,Freezing.class};
    public static String begin(Hero hero, boolean up) {
        Level level=Dungeon.level;
        if (hero==null || !hero.isAlive() || !hero.ready || level==null) return "Wait until you can move.";
        int target=discoveredStair(level,up);
        if (target<0) return "Those stairs have not been discovered.";
        if (hero.pos==target) return "You are already at the stairs.";
        if (hero.visibleEnemies()>0) return "An enemy is nearby. Stair navigation stopped.";
        if (hero.rooted || level.locked) return "You cannot navigate to those stairs right now.";
        if (!safeStep(level,hero,target)) return "No safe discovered route reaches those stairs.";
        boolean[] passable=new boolean[level.length()];
        for(int i=0;i<passable.length;i++) passable[i]=safeStep(level,hero,i);
        passable[hero.pos]=true;
        PathFinder.Path path=Dungeon.findPath(hero,target,passable,hero.fieldOfView,true);
        if(path==null || path.isEmpty()) return "No safe discovered route reaches those stairs.";
        hero.beginStairWalk(new Walk(target,level),path);
        return null;
    }
}
