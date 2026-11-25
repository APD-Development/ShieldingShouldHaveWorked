package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import org.junit.*;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.Assert.*;
/** Runs the real Dwarf King phase 2 turn by turn, headless. Pie's report (Nov 23): every summon dead, king still shielded. */
public class DwarfKingPhaseTwoSimTest {
    static class KS extends CharSprite { @Override public void showStatus(int c,String t,Object...a){} @Override public void showStatusWithIcon(int c,String t,int i,Object...a){} @Override public Emitter centerEmitter(){return new Emitter();} @Override public void idle(){} @Override public void die(){} @Override public void link(Char ch){this.ch=ch;ch.sprite=this;} }
    static class QS extends GhoulSprite { @Override public void showStatus(int c,String t,Object...a){} @Override public void showStatusWithIcon(int c,String t,int i,Object...a){} @Override public Emitter centerEmitter(){return new Emitter();} @Override public void crumple(){} @Override public void die(){} @Override public void idle(){} }
    DwarfKing k; static Object originalScene; static com.watabou.noosa.Camera originalCamera; static boolean saved;
    static Field f(Class<?> c,String n)throws Exception{Field x=c.getDeclaredField(n);x.setAccessible(true);return x;}
    static void now(float t)throws Exception{f(Actor.class,"now").setFloat(null,t);}
    @Before public void setup() throws Exception {
        HeadlessTestSupport.start();Actor.clear();now(0);if(com.watabou.noosa.Camera.main==null)com.watabou.noosa.Camera.main=new com.watabou.noosa.Camera(0,0,100,100,1);
        Dungeon.hero=new Hero();Dungeon.hero.HP=Dungeon.hero.HT=100000;new KS().link(Dungeon.hero);Dungeon.hero.pos=7+44*15;
        CityBossLevel L=new CityBossLevel();Dungeon.level=L;L.setSize(15,48);Arrays.fill(L.map,Terrain.EMPTY);
        L.blobs=new HashMap<>();L.heaps=new com.watabou.utils.SparseArray<>();L.plants=new com.watabou.utils.SparseArray<>();L.traps=new com.watabou.utils.SparseArray<>();L.buildFlagMaps();L.mobs=new HashSet<>();
        L.heroFOV=new boolean[L.length()];Arrays.fill(L.heroFOV,true);Dungeon.challenges=0;Field bg=f(Badges.class,"global");if(bg.get(null)==null)bg.set(null,new HashSet<>());Field bl=f(Badges.class,"local");if(bl.get(null)==null)bl.set(null,new HashSet<>());
        Field sc=f(GameScene.class,"scene");if(!saved){originalScene=sc.get(null);originalCamera=com.watabou.noosa.Camera.main;saved=true;}
        Field uf=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");uf.setAccessible(true);
        Object unsafe=uf.get(null);GameScene gs=(GameScene)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,GameScene.class);
        for(Field g:GameScene.class.getDeclaredFields()) if(!Modifier.isStatic(g.getModifiers()) && Group.class.isAssignableFrom(g.getType()) && g.getType()==Group.class){g.setAccessible(true);g.set(gs,new Group());}sc.set(null,gs);
        Actor.add(Dungeon.hero);
        k=new DwarfKing();new KS().link(k);k.pos=CityBossLevel.throne;L.mobs.add(k);
        f(DwarfKing.class,"phase").setInt(k,2);Buff.affect(k,DwarfKing.DKBarrior.class).setShield(k.HT);
    }
    @After public void done() throws Exception { f(GameScene.class,"scene").set(null,originalScene); com.watabou.noosa.Camera.main=originalCamera; Actor.clear(); Dungeon.challenges=0; Dungeon.apdRestoreCustomRules(new com.watabou.utils.Bundle()); }
    interface Policy{void turn(int t,List<Mob> alive)throws Exception;}
    String run(Policy p,int turns)throws Exception{
        StringBuilder log=new StringBuilder();
        for(int t=0;t<turns;t++){
            now(t);
            if(k.cooldown()<=0) k.act();
            for(Buff b:new ArrayList<>(k.buffs())) if(b.target==k) b.act();
            List<Mob> alive=new ArrayList<>();
            for(Mob m:new ArrayList<>(Dungeon.level.mobs)) if(m!=k){ if(m.sprite==null) new QS().link(m); m.lootChance=0; alive.add(m); }
            for(Mob m:alive) for(Buff b:new ArrayList<>(m.buffs())) if(b.target==m) b.act();
            alive.removeIf(m->!Dungeon.level.mobs.contains(m));
            p.turn(t,alive);
            int phase=f(DwarfKing.class,"phase").getInt(k);
            if(phase==3) return "PHASE3 at turn "+t;
        }
        return "STUCK shield="+k.shielding()+" summonsMade="+f(DwarfKing.class,"summonsMade").getInt(k)+" mobs="+(Dungeon.level.mobs.size()-1)+" summoning="+k.buffs(DwarfKing.Summoning.class).size();
    }

    @Test public void heroStandsOnAPedestal() throws Exception {
        Dungeon.hero.pos=424;
        String r=run((t,alive)->{for(Mob m:alive) if(m.isAlive()){m.die(Dungeon.hero);break;}},600);
        System.out.println("SIM pedestal: "+r); assertTrue(r,r.startsWith("PHASE3"));
    }
    @Test public void letThemPileUpThenKill() throws Exception {
        String r=run((t,alive)->{long n=alive.stream().filter(Mob::isAlive).count(); if(n>=3||t%7==0) for(Mob m:alive) if(m.isAlive()){m.die(Dungeon.hero);break;}},900);
        System.out.println("SIM pileup: "+r); assertTrue(r,r.startsWith("PHASE3"));
    }
    @Test public void corruptThenKill() throws Exception {
        String r=run((t,alive)->{for(Mob m:alive) if(m.isAlive()){ if(m.alignment==Char.Alignment.ENEMY && t%2==0){ Buff.affect(m,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption.class); m.alignment=Char.Alignment.ALLY;} else m.die(Dungeon.hero); break;}},900);
        System.out.println("SIM corrupt: "+r); assertTrue(r,r.startsWith("PHASE3"));
    }
    @Test public void saveAndReloadEveryFewTurns() throws Exception {
        String r=run(new Policy(){public void turn(int t,List<Mob> alive)throws Exception{
            if(t%5==2) reload();
            List<Mob> now=new ArrayList<>(); for(Mob m:Dungeon.level.mobs) if(m!=k) now.add(m);
            for(Mob m:now) if(m.isAlive()){ if(m.sprite==null) new QS().link(m); m.lootChance=0; m.die(Dungeon.hero); break;}
        }},900);
        System.out.println("SIM reload: "+r); assertTrue(r,r.startsWith("PHASE3"));
    }
    void reload() throws Exception {
        com.watabou.utils.Bundle b=new com.watabou.utils.Bundle(); b.put("mobs",Dungeon.level.mobs);
        java.io.ByteArrayOutputStream o=new java.io.ByteArrayOutputStream(); com.watabou.utils.Bundle.write(b,o);
        com.watabou.utils.Bundle in=com.watabou.utils.Bundle.read(new java.io.ByteArrayInputStream(o.toByteArray()));
        float t=Actor.now(); Actor.clear(); now(t);
        Dungeon.level.mobs=new HashSet<>();
        for(com.watabou.utils.Bundlable x:in.getCollection("mobs")){ Mob m=(Mob)x; Dungeon.level.mobs.add(m); if(m instanceof DwarfKing){k=(DwarfKing)m; new KS().link(k); for(Buff kb:k.buffs()) kb.fx(true);} else new QS().link(m); m.lootChance=0;
            for(Buff bf:m.buffs()) if(bf instanceof Ghoul.GhoulLifeLink){Field gf=f(Ghoul.GhoulLifeLink.class,"ghoul");Ghoul g=(Ghoul)gf.get(bf);g.sprite=null;new QS().link(g);g.lootChance=0;} }
    }

    @Test public void fuzz() throws Exception {
        int stalls=0; StringBuilder first=new StringBuilder();
        for(int seed=1;seed<=400;seed++){
            setup(); final Random R=new Random(seed);
            int[] spots={424,430,514,520,423,425,409,439,471,473,457,487};
            Dungeon.hero.pos = R.nextInt(3)==0 ? spots[R.nextInt(spots.length)] : 7+44*15;
            final int killChance=1+R.nextInt(4), corrupt=R.nextInt(6), reload=R.nextInt(4)==0?3+R.nextInt(6):0, move=R.nextInt(3);
            String r=run(new Policy(){public void turn(int t,List<Mob> a)throws Exception{
                if(reload>0 && t%reload==1) reload();
                if(move==0 && R.nextInt(4)==0) Dungeon.hero.pos=spots[R.nextInt(spots.length)];
                List<Mob> now=new ArrayList<>(); for(Mob m:Dungeon.level.mobs) if(m!=k) now.add(m);
                Collections.shuffle(now,R);
                int kills=R.nextInt(killChance+1);
                for(Mob m:now){ if(kills<=0) break; if(!m.isAlive()) continue; if(m.sprite==null) new QS().link(m); m.lootChance=0;
                    if(corrupt==0 && m.alignment==Char.Alignment.ENEMY && R.nextInt(3)==0){ Buff.affect(m,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption.class); m.alignment=Char.Alignment.ALLY; }
                    else m.die(Dungeon.hero);
                    kills--; }
            }},1500);
            if(!r.startsWith("PHASE3")){ stalls++; if(first.length()<2000) first.append("seed ").append(seed).append(" kc=").append(killChance).append(" corrupt=").append(corrupt==0).append(" reload=").append(reload).append(" move=").append(move==0).append(" -> ").append(r).append('\n'); }
            done();
        }
        System.out.println("SIM fuzz stalls="+stalls+"\n"+first);
    }

    @Test public void customBossHealth() throws Exception {
        StringBuilder out=new StringBuilder();
        boolean bad=false;
        for(int ch:new int[]{0,Challenges.STRONGER_BOSSES}) for(int pct=25;pct<=500;pct+=25){
            com.watabou.utils.Bundle w=new com.watabou.utils.Bundle(),r=new com.watabou.utils.Bundle();
            com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.defaults().with(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules.Slider.BOSS_HP,pct).store(r);
            w.put("apd_custom_rules",r);Dungeon.apdRestoreCustomRules(w);
            setup(); Dungeon.challenges=ch; k=new DwarfKing();new KS().link(k);k.pos=CityBossLevel.throne;Dungeon.level.mobs.clear();Dungeon.level.mobs.add(k);f(DwarfKing.class,"phase").setInt(k,2);Buff.affect(k,DwarfKing.DKBarrior.class).setShield(k.HT); k.apdCustomHealthApplied=false; com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomMobHooks.applyHealth(k);
            k.buff(DwarfKing.DKBarrior.class).detach(); Buff.affect(k,DwarfKing.DKBarrior.class).setShield(k.HT);
            String res=run((t2,alive)->{if(t2%10==0)for(Mob m:alive) if(m.isAlive()){m.die(Dungeon.hero);break;}},1500);
            if(!res.startsWith("PHASE3")) bad=true;
            out.append(ch==0?"":"stronger ").append("BOSS_HP ").append(pct).append("% HT=").append(k.HT).append(" -> ").append(res).append('\n');
            done();
        }
        Dungeon.apdRestoreCustomRules(new com.watabou.utils.Bundle());
        System.out.println("SIM custom\n"+out);
        assertFalse(out.toString(), bad);
    }
    @Test public void killEverythingAtOnce() throws Exception {
        String r=run((t,alive)->{for(Mob m:alive) if(m.isAlive()) m.die(Dungeon.hero);},600);
        System.out.println("SIM killAll: "+r); assertTrue(r,r.startsWith("PHASE3"));
    }
    @Test public void killOneATurn() throws Exception {
        String r=run((t,alive)->{for(Mob m:alive) if(m.isAlive()){m.die(Dungeon.hero);break;}},600);
        System.out.println("SIM killOne: "+r); assertTrue(r,r.startsWith("PHASE3"));
    }
    @Test public void killEveryThirdTurn() throws Exception {
        String r=run((t,alive)->{if(t%3==0)for(Mob m:alive) if(m.isAlive()){m.die(Dungeon.hero);break;}},900);
        System.out.println("SIM killSlow: "+r); assertTrue(r,r.startsWith("PHASE3"));
    }
}
