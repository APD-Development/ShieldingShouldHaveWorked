package com.shatteredpixel.shatteredpixeldungeon.apd.diagnostics;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Bundle;
import com.watabou.utils.SupportActionJournal;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.*;

/** Pie's Dwarf King report had no monster state. The extended report now carries what the player could see. */
public class GameStateSnapshotTest {
    private Hero beforeHero; private Level beforeLevel;
    private static class Empty extends Level {protected boolean build(){return true;}protected void createMobs(){}protected void createItems(){}}

    @Before public void setup(){
        HeadlessTestSupport.start(); beforeHero=Dungeon.hero; beforeLevel=Dungeon.level; Actor.clear();
        Dungeon.hero=new Hero(); Dungeon.hero.HP=Dungeon.hero.HT=20; Dungeon.hero.pos=0;
        Dungeon.level=new Empty(); Dungeon.level.setSize(10,10);
        Arrays.fill(Dungeon.level.map,Terrain.EMPTY); Dungeon.level.mobs=new HashSet<>();
        Dungeon.level.heroFOV=new boolean[Dungeon.level.length()];
    }
    @After public void clear(){ SupportActionJournal.setEnabled(false); Actor.clear(); Dungeon.hero=beforeHero; Dungeon.level=beforeLevel; Dungeon.apdRestoreCustomRules(new Bundle()); }

    private DwarfKing king(int pos){ DwarfKing k=new DwarfKing(); k.pos=pos; Dungeon.level.mobs.add(k); return k; }

    @Test public void visibleBossShowsHealthShieldAndItsOwnCounters(){
        DwarfKing k=king(15); Dungeon.level.heroFOV[15]=true;
        Buff.affect(k,DwarfKing.DKBarrior.class).setShield(40);
        String s=GameStateSnapshot.full();
        assertTrue(s, s.contains("DwarfKing pos=15"));
        assertTrue(s, s.contains("shield=40"));
        assertTrue(s, s.contains("DKBarrior shield 40"));
        assertTrue(s, s.contains("phase=1"));
        assertTrue(s, s.contains("summonsMade=0"));
        assertTrue(s, s.contains("Custom rules: none"));
        assertTrue(GameStateSnapshot.compact(), GameStateSnapshot.compact().contains("boss=DwarfKing"));
    }

    @Test public void unseenMonstersStayOutButALockedBossFightIsShown(){
        Rat r=new Rat(); r.pos=50; Dungeon.level.mobs.add(r);
        king(60);
        String s=GameStateSnapshot.full();
        assertFalse(s, s.contains("Rat pos="));
        assertFalse(s, s.contains("DwarfKing pos="));
        Buff.affect(Dungeon.hero, LockedFloor.class);
        s=GameStateSnapshot.full();
        assertTrue(s, s.contains("DwarfKing pos=60"));
        assertFalse("an unseen ordinary monster is still hidden", s.contains("Rat pos="));
        assertTrue(s, s.contains("Boss fight in progress (floor locked): yes"));
    }

    @Test public void noRunSaysSo(){
        Dungeon.hero=null;
        assertTrue(GameStateSnapshot.full().contains("No run in progress."));
        assertEquals("no_run", GameStateSnapshot.compact());
    }

    @Test public void bossDamageDeathsAndPhasesGoInTheHistoryOnlyWhenTurnedOn() throws Exception {
        DwarfKing k=king(15); Dungeon.level.heroFOV[15]=true;
        SupportActionJournal.setEnabled(false);
        File root=Files.createTempDirectory("apd-combat-journal-").toFile();
        SupportActionJournal.configure(root);
        CombatJournal.damage(k,new DwarfKing.KingDamager(),25,300,300,25,0);
        assertFalse(history().contains("combat.damage"));
        SupportActionJournal.setEnabled(true);
        CombatJournal.damage(k,new DwarfKing.KingDamager(),25,300,300,25,0);
        Rat unseen=new Rat(); unseen.pos=70; Dungeon.level.mobs.add(unseen);
        CombatJournal.damage(unseen,Dungeon.hero,3,8,0,0,3);
        CombatJournal.died(unseen,Dungeon.hero);
        Rat seen=new Rat(); seen.pos=16; Dungeon.level.heroFOV[16]=true;
        CombatJournal.died(seen,Dungeon.hero);
        CombatJournal.bossPhase("Dwarf King",2,"shielded");
        String h=history();
        assertTrue(h, h.contains("combat.damage target=DwarfKing src=KingDamager requested=25"));
        assertTrue(h, h.contains("combat.died who=Rat pos=16 cause=Hero"));
        assertFalse(h, h.contains("who=Rat pos=70"));
        assertFalse("ordinary monster damage stays out", h.contains("target=Rat"));
        assertTrue(h, h.contains("boss.phase boss=Dwarf King phase=2 state=shielded floor="));
    }

    private static String history() throws Exception {
        Thread.sleep(50);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        SupportActionJournal.export(out,SupportActionJournal.checkpoint());
        return out.toString(StandardCharsets.UTF_8.name());
    }
}
