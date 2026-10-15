package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;
import org.junit.Test;
import static org.junit.Assert.*;
public class DerivedShockScalingTest {
 static class Guaranteed extends Shocking { @Override protected float procChanceMultiplier(Char attacker){return 3f;} }
 static class Quiet extends SpecialBombDamageProvenanceTest.Quiet {
  @Override public Emitter centerEmitter(){return new Emitter();}
  @Override public void flash(){}
 }
 @Test public void realShockSplashDoesNotScaleTheAlreadyScaledAttackTwice() throws Exception {
  SpecialBombDamageProvenanceTest f=new SpecialBombDamageProvenanceTest();f.setup();
  SpecialBombDamageProvenanceTest.Target defender=new SpecialBombDamageProvenanceTest.Target(false);
  SpecialBombDamageProvenanceTest.Target nearby=new SpecialBombDamageProvenanceTest.Target(false);
  try {
   defender.pos=312;nearby.pos=313;defender.sprite=new Quiet();nearby.sprite=new Quiet();
   defender.alignment=nearby.alignment=Char.Alignment.ENEMY;
   Actor.add(defender);Actor.add(nearby);
   Dungeon.hero.sprite=new Quiet();new Group().add(Dungeon.hero.sprite);
   Guaranteed shock=new Guaranteed();
   for(int p=25;p<=500;p+=25){
    f.rules(p,275,425);nearby.hits.clear();
    int attack=Math.round(CustomOffensiveHooks.attackDamage(Dungeon.hero,40));
    Random.pushGenerator(765);try{assertEquals(attack,shock.proc(new Shortsword(),Dungeon.hero,defender,attack));}finally{Random.popGenerator();}
    assertEquals(1,nearby.hits.size());assertEquals(p/5,(int)nearby.hits.get(0));assertSame(shock,nearby.sources.get(nearby.sources.size()-1));
   }
  }finally{Actor.remove(defender);Actor.remove(nearby);f.cleanup();}
 }
}

