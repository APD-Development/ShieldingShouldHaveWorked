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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BattleAxe;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import org.junit.Test;
import static org.junit.Assert.*;
public class InventoryPickupMessageTest {
 private interface Check {void run(Hero hero);}
 private void withHero(Check check){HeadlessTestSupport.start();Hero previous=Dungeon.hero;try{Hero hero=new Hero();Talent.initClassTalents(hero);hero.HP=0;Dungeon.hero=hero;check.run(hero);}finally{Dungeon.hero=previous;}}
 @Test public void normalCapacityFailureKeepsOriginalMessage(){withHero(h->assertTrue(h.pickupFailureMessage(new Pasty()).startsWith("You can't carry:")));}
 @Test public void lostBackpackExplainsAllReportedPickupFailuresWithFreeSlots(){withHero(h->{
  LostInventory lost=new LostInventory();assertTrue(lost.attachTo(h));
  try{for(Item item:new Item[]{new BattleAxe(),new Pasty(),new Sungrass.Seed()}){
   assertFalse(item.collect(h.belongings.backpack));assertTrue(h.belongings.backpack.items.isEmpty());
   int quantity=item.quantity();float cooldown=h.cooldown();
   assertTrue(h.pickupFailureMessage(item).startsWith("Recover your lost backpack"));
   assertEquals(quantity,item.quantity());assertEquals(cooldown,h.cooldown(),0);
  }}finally{lost.detach();}
 });}
 @Test public void retainedItemsDoNotReceiveTheWrongRestrictionMessage(){withHero(h->{
  LostInventory lost=new LostInventory();assertTrue(lost.attachTo(h));
  try{Pasty item=new Pasty();item.keptThoughLostInvent=true;assertTrue(h.pickupFailureMessage(item).startsWith("You can't carry:"));}finally{lost.detach();}
 });}
 @Test public void recoveryRemovesLostBackpackExplanation(){withHero(h->{
  LostInventory lost=new LostInventory();assertTrue(lost.attachTo(h));lost.detach();assertTrue(h.pickupFailureMessage(new Pasty()).startsWith("You can't carry:"));
 });}
}
