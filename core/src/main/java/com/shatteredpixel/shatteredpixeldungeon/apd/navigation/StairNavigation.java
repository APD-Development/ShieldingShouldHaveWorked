package com.shatteredpixel.shatteredpixeldungeon.apd.navigation;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.*;
import com.watabou.utils.PathFinder;

/** Transient safe stair routing. Only an explicitly opted-in shortcut may activate the target transition. */
public final class StairNavigation {
    private StairNavigation() {}
    public static final class Walk extends HeroAction.Move {
        public final Level level;
        public final boolean up;
        public final boolean enterOnArrival;
        // Preserve the original pure-walk constructor for telemetry and existing callers.
        public Walk(int destination, Level level) { this(destination,level,false,false); }
        public Walk(int destination, Level level, boolean up, boolean enterOnArrival) {
            super(destination, HeroAction.Move.Mode.STAIRS);
            this.level = level;
            this.up = up;
            this.enterOnArrival = enterOnArrival;
        }
    }
    public static int discoveredStair(Level level, boolean up) {
        if (level==null || level.transitions==null) return -1;
        for (LevelTransition transition:level.transitions) {
            boolean direction=matchesDirection(transition, up);
            int cell=transition.cell();
            if (direction && cell>=0 && cell<level.length() && (level.visited[cell] || level.mapped[cell])) return cell;
        }
        return -1;
    }
    private static boolean matchesDirection(LevelTransition transition, boolean up) {
        return transition != null && (up
                ? transition.type == LevelTransition.Type.REGULAR_ENTRANCE
                    || transition.type == LevelTransition.Type.SURFACE
                : transition.type == LevelTransition.Type.REGULAR_EXIT);
    }
    // Validate again after walking: foes, floor seals, obstacles and transition identity can change.
    public static boolean canEnterOnArrival(Hero hero, Walk walk) {
        Level level = Dungeon.level;
        if (walk == null || !walk.enterOnArrival || hero == null || !hero.isAlive()
                || level == null || walk.level != level || hero.pos != walk.dst
                || hero.visibleEnemies() > 0 || hero.rooted || level.locked
                || !safeStep(level, hero, walk.dst)) return false;
        LevelTransition transition = level.getTransition(walk.dst);
        return transition != null && transition.cell() == walk.dst
                && matchesDirection(transition, walk.up) && transition.inside(hero.pos);
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
        boolean enterOnArrival = SPDSettings.autoEnterStairs();
        if (hero.pos==target && !enterOnArrival) return "You are already at the stairs.";
        if (hero.visibleEnemies()>0) return "An enemy is nearby. Stair navigation stopped.";
        if (hero.rooted || level.locked) return "You cannot navigate to those stairs right now.";
        if (!safeStep(level,hero,target)) return "No safe discovered route reaches those stairs.";
        if (hero.pos==target) {
            LevelTransition transition=level.getTransition(target);
            if (transition==null || transition.cell()!=target || !matchesDirection(transition,up))
                return "The requested stairs are no longer available.";
            hero.interrupt();
            hero.lastAction=null;
            hero.curAction=new HeroAction.LvlTransition(target);
            return null;
        }
        boolean[] passable=new boolean[level.length()];
        for(int i=0;i<passable.length;i++) passable[i]=safeStep(level,hero,i);
        passable[hero.pos]=true;
        PathFinder.Path path=Dungeon.findPath(hero,target,passable,hero.fieldOfView,true);
        if(path==null || path.isEmpty()) return "No safe discovered route reaches those stairs.";
        hero.beginStairWalk(new Walk(target,level,up,enterOnArrival),path);
        return null;
    }
}
