package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import static org.junit.Assert.*;

public class CombatDetailPairingTest {
 @Rule public TemporaryFolder tmp = new TemporaryFolder();
 private int sequence;

 private void publicEvent(RunLogWriter writer, int type, String name) {
  if(type == EventTypes.ATTACK) {
   CombatEvents.Attack a = new CombatEvents.Attack();
   a.attacker.kind=CombatEvents.KIND_HERO; a.defender.kind=CombatEvents.KIND_ENEMY;
   a.defender.name=name; a.hit=true; assertTrue(CombatEvents.emitAttack(writer,1,a));
  } else if(type == EventTypes.DAMAGE) {
   CombatEvents.Damage d=new CombatEvents.Damage();d.target.kind=CombatEvents.KIND_ENEMY;d.target.name=name;
   d.source.kind=CombatEvents.SRC_CHAR;d.source.fighterKind=CombatEvents.KIND_HERO;d.shown=3;
   assertTrue(CombatEvents.emitDamage(writer,1,d));
  } else {
   CombatEvents.Kill k=new CombatEvents.Kill();k.victim.kind=CombatEvents.KIND_ENEMY;k.victim.name=name;
   k.killer.kind=CombatEvents.SRC_CHAR;k.killer.fighterKind=CombatEvents.KIND_HERO;
   assertTrue(CombatEvents.emitKill(writer,1,k));
  }
 }
 private void detail(RunLogWriter writer,int type,String name,boolean emitted) {
  String cls="actors.mobs."+name;
  if(type==EventTypes.ATTACK) {
   CombatEvents.AttackDetail d=new CombatEvents.AttackDetail();d.defenderClass=cls;d.publicEmitted=emitted;
   assertTrue(CombatEvents.emitAttackDetail(writer,1,d));
  } else if(type==EventTypes.DAMAGE) {
   CombatEvents.DamageDetail d=new CombatEvents.DamageDetail();d.targetClass=cls;d.publicEmitted=emitted;
   assertTrue(CombatEvents.emitDamageDetail(writer,1,d));
  } else {
   CombatEvents.KillDetail d=new CombatEvents.KillDetail();d.victimClass=cls;d.victimKind=CombatEvents.KIND_ENEMY;
   d.publicEmitted=emitted;assertTrue(CombatEvents.emitKillDetail(writer,1,d));
  }
 }
 private LogRows rows(int type,boolean missing,boolean missingNextPublic) throws Exception {
  String id="pair-"+sequence++;File file=new File(tmp.getRoot(),id+".apdlog");
  RunLogWriter writer=RunLogWriter.create(file,new TelemetryVersion(1,6),id,"test",1);
  LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();start.runId=id;start.heroClass="WARRIOR";
  assertTrue(LifecycleEvents.emitRunStart(writer,0,false,start));
  publicEvent(writer,type,"Rat");
  if(!missing) detail(writer,type,"Rat",true);
  else assertTrue(writer.event(EventTypes.GAP).clock(1).unsigned(EventTypes.GAP_DROPPED_EVENTS).unsigned(missingNextPublic?2:1).emit());
  if(!missingNextPublic) publicEvent(writer,type,"Goo");
  detail(writer,type,"Goo",!missingNextPublic);
  assertTrue(LifecycleEvents.emitRunEnd(writer,2,new LifecycleEvents.RunEnd()));assertTrue(writer.close());
  return RowBuilders.fromLog(RunLogReader.read(file),id);
 }
 private void check(int type) throws Exception {
  for(boolean missing:new boolean[]{false,true}) {
   LogRows rows=rows(type,missing,false);DatasetDef def=RowBuilders.def(Datasets.ENEMIES);
   assertEquals(2,rows.of(Datasets.ENEMIES).size());
   Row rat=rows.of(Datasets.ENEMIES).get(0),goo=rows.of(Datasets.ENEMIES).get(1);
   assertEquals(!missing,rat.isKnown(def.indexOf("enemy.class")));
   if(!missing)assertEquals("actors.mobs.Rat",rat.text(def.indexOf("enemy.class")));
   assertEquals("actors.mobs.Goo",goo.text(def.indexOf("enemy.class")));
   assertEquals(1,goo.num(def.indexOf("enemy.boss")));
   if(missing)assertFalse("missing identity cannot imply boss status",rat.isKnown(def.indexOf("enemy.boss")));
   else assertEquals(0,rat.num(def.indexOf("enemy.boss")));
  }
 }
 @Test public void attacksDoNotBorrowTheNextTargetsDetails() throws Exception {check(EventTypes.ATTACK);}
 @Test public void damageDoesNotBorrowTheNextTargetsDetails() throws Exception {check(EventTypes.DAMAGE);}
 @Test public void killsDoNotBorrowTheNextVictimsDetails() throws Exception {check(EventTypes.KILL);}
 @Test public void aGapCannotLendAnUnpairedDetailToAnEarlierAttack() throws Exception {
  LogRows rows=rows(EventTypes.ATTACK,true,true);
  DatasetDef def=EventDatasets.def(EventDatasets.ATTACKS);
  assertEquals(1,rows.of(EventDatasets.ATTACKS).size());
  Row first=rows.of(EventDatasets.ATTACKS).get(0);
  assertFalse(first.isKnown(def.indexOf("attack.defenderClass")));
  DatasetDef enemies=RowBuilders.def(Datasets.ENEMIES);
  assertFalse(rows.of(Datasets.ENEMIES).get(0).isKnown(enemies.indexOf("enemy.class")));
 }

 @Test public void malformedHiddenDetailsCannotEraseReadablePublicCombat() throws Exception {
  for(int type:new int[]{EventTypes.ATTACK,EventTypes.DAMAGE,EventTypes.KILL}) {
   String id="malformed-detail-"+sequence++;File file=new File(tmp.getRoot(),id+".apdlog");
   RunLogWriter writer=RunLogWriter.create(file,new TelemetryVersion(1,6),id,"test",1);
   LifecycleEvents.RunStart start=new LifecycleEvents.RunStart();start.runId=id;start.heroClass="WARRIOR";
   assertTrue(LifecycleEvents.emitRunStart(writer,0,false,start));publicEvent(writer,type,"Rat");
   int detailType=type==EventTypes.ATTACK?EventTypes.ATTACK_DETAIL:type==EventTypes.DAMAGE?EventTypes.DAMAGE_DETAIL:EventTypes.KILL_DETAIL;
   assertTrue(writer.append(detailType,0,1,new byte[0]));
   LifecycleEvents.RunEnd end=new LifecycleEvents.RunEnd();assertTrue(LifecycleEvents.emitRunEnd(writer,2,end));assertTrue(writer.close());
   byte[] original=java.nio.file.Files.readAllBytes(file.toPath());
   LogRows rows=RowBuilders.fromLog(RunLogReader.read(file),id);DatasetDef def=RowBuilders.def(Datasets.ENEMIES);
   assertEquals(1,rows.of(Datasets.ENEMIES).size());Row rat=rows.of(Datasets.ENEMIES).get(0);
   assertEquals("Rat",rat.text(def.indexOf("enemy.name")));assertFalse(rat.isKnown(def.indexOf("enemy.class")));
   if(type==EventTypes.ATTACK)assertEquals(1,rat.num(def.indexOf("enemy.attacksByHero")));
   else if(type==EventTypes.DAMAGE)assertEquals(3,rat.num(def.indexOf("enemy.damageFromHero")));
   else {assertEquals(1,rat.num(def.indexOf("enemy.kills")));assertEquals(1,rat.num(def.indexOf("enemy.deaths")));}
   assertArrayEquals(original,java.nio.file.Files.readAllBytes(file.toPath()));
  }
 }
}
