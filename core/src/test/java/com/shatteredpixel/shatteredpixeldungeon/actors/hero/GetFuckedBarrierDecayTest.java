package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import org.junit.*;
import static org.junit.Assert.*;

/** Isolated decay measurements. These supplied shields are never credited to a route. */
public class GetFuckedBarrierDecayTest {
    private Hero oldHero,hero;
    private int oldChallenges;
    @Before public void setup(){
        oldHero=Dungeon.hero;oldChallenges=Dungeon.challenges;
        Dungeon.challenges=Challenges.GET_FUCKED;
        hero=new Hero();Dungeon.hero=hero;hero.updateHT(false);
        assertEquals(1,hero.HT);assertEquals(1,hero.HP);
    }
    @After public void cleanup(){Actor.clear();Dungeon.hero=oldHero;Dungeon.challenges=oldChallenges;}
    private int lifetime(int shield){
        Barrier b=Buff.affect(hero,Barrier.class);b.setShield(shield);
        int ticks=0;
        while(b.shielding()>0 && ticks<1000){b.act();ticks++;}
        assertEquals(1,hero.HP);assertEquals(1,hero.HT);
        return ticks;
    }
    @Test public void tenBarrierLifetime(){assertEquals(61,lifetime(10));}
    @Test public void seventyFiveBarrierLifetime(){assertEquals(135,lifetime(75));}
    @Test public void twentyFiveEveryFortyThreeTicksSurvivesWithoutDamage(){
        Barrier b=Buff.affect(hero,Barrier.class);b.setShield(25);
        for(int cycle=0;cycle<100;cycle++){
            for(int tick=0;tick<43;tick++){b.act();assertTrue(b.shielding()>0);}
            if(cycle==0)assertEquals(4,b.shielding());
            if(cycle>=2)assertEquals(6,b.shielding());
            b.incShield(25);
            if(cycle>=2)assertEquals(31,b.shielding());
        }
        assertEquals(1,hero.HP);
    }
}
