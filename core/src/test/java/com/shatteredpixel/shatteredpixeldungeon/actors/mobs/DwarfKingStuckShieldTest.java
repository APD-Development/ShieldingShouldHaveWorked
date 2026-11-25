package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;
/** Pie's report (Nov 23): every summon dead, but the Dwarf King stayed shielded and never fought. */
public class DwarfKingStuckShieldTest {
    private Hero beforeHero;private Level beforeLevel;private int beforeChallenges;
    private static class Empty extends Level {protected boolean build(){return true;}protected void createMobs(){}protected void createItems(){}}
    private static class QuietSprite extends CharSprite {
        @Override public void showStatus(int color,String text,Object...args){}
        @Override public void showStatusWithIcon(int color,String text,int icon,Object...args){}
        @Override public Emitter centerEmitter(){return new Emitter();}
    }
    @Before public void setup(){
        HeadlessTestSupport.start();beforeHero=Dungeon.hero;beforeLevel=Dungeon.level;beforeChallenges=Dungeon.challenges;
        Dungeon.hero=new Hero();Dungeon.level=new Empty();Dungeon.level.setSize(25,25);
        Arrays.fill(Dungeon.level.map,Terrain.EMPTY);Dungeon.level.mobs=new HashSet<>();Dungeon.challenges=0;
        Actor.clear();
    }
    @After public void clear(){Actor.clear();Dungeon.hero=beforeHero;Dungeon.level=beforeLevel;Dungeon.challenges=beforeChallenges;Dungeon.apdRestoreCustomRules(new Bundle());}
    private DwarfKing king(int summonsMade,int shield){
        DwarfKing k=new DwarfKing();k.sprite=new QuietSprite();set(k,"phase",2);set(k,"summonsMade",summonsMade);
        Buff.affect(k,DwarfKing.DKBarrior.class).setShield(shield);return k;
    }
    @Test public void allWavesDeadWithShieldLeftStartsTheFinalFight(){
        DwarfKing k=king(12,40);
        k.apdUnstickShield();
        assertEquals(0,k.shielding());assertEquals(3,get(k,"phase"));
        assertFalse(k.isInvulnerable(Hero.class));
    }
    @Test public void firstWaveDeadWithShieldAboveTheNextWaveDropsToThatWave(){
        DwarfKing k=king(4,k0().HT);
        k.apdUnstickShield();
        assertEquals(200,k.shielding());assertEquals(2,get(k,"phase"));
    }
    @Test public void aLivingSummonKeepsTheShield(){
        DwarfKing k=king(12,40);
        DwarfKing.DKGhoul g=new DwarfKing.DKGhoul();g.pos=30;Dungeon.level.mobs.add(g);
        k.apdUnstickShield();
        assertEquals(40,k.shielding());assertEquals(2,get(k,"phase"));
    }
    @Test public void aSummonStillBeingSummonedKeepsTheShield(){
        DwarfKing k=king(12,40);
        new DwarfKing.Summoning(){@Override public void fx(boolean on){}}.attachTo(k);
        k.apdUnstickShield();
        assertEquals(40,k.shielding());assertEquals(2,get(k,"phase"));
    }
    @Test public void aShieldAlreadyAtTheThresholdIsLeftAlone(){
        DwarfKing k=king(4,150);
        k.apdUnstickShield();
        assertEquals(150,k.shielding());
    }
    private static void set(DwarfKing k,String f,int v){try{java.lang.reflect.Field x=DwarfKing.class.getDeclaredField(f);x.setAccessible(true);x.setInt(k,v);}catch(Exception e){throw new AssertionError(e);}}
    private static int get(DwarfKing k,String f){try{java.lang.reflect.Field x=DwarfKing.class.getDeclaredField(f);x.setAccessible(true);return x.getInt(k);}catch(Exception e){throw new AssertionError(e);}}
    private static DwarfKing k0(){return new DwarfKing();}
}
