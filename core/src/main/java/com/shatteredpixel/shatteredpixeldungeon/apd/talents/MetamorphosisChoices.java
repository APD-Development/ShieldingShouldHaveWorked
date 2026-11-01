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

package com.shatteredpixel.shatteredpixeldungeon.apd.talents;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import java.util.*;

/** Same-position alternatives; already-owned talents retain SPD's duplicate protection. */
public final class MetamorphosisChoices {
 private MetamorphosisChoices(){}
 public static final class Choice {
  public final HeroClass heroClass;
  public final Talent talent;
  public final int transferredPoints;
  public final boolean alreadyOwned;
  Choice(HeroClass heroClass,Talent talent,int points,boolean alreadyOwned){this.heroClass=heroClass;this.talent=talent;this.transferredPoints=points;this.alreadyOwned=alreadyOwned;}
 }
 public static List<Choice> atPosition(HeroClass playing,int tier,LinkedHashMap<Talent,Integer> current,Talent replacing) {
  if(playing==null || tier<1 || tier>3 || current==null || !current.containsKey(replacing))throw new IllegalArgumentException("invalid class talent position");
  int position=0;for(Talent talent:current.keySet()){if(talent==replacing)break;position++;}
  List<Choice> choices=new ArrayList<>();
  for(HeroClass cls:HeroClass.values()) {
   if(cls==playing)continue;
   ArrayList<LinkedHashMap<Talent,Integer>> base=new ArrayList<>();Talent.initClassTalents(cls,base);
   List<Talent> row=new ArrayList<>(base.get(tier-1).keySet());
   if(position>=row.size())throw new IllegalArgumentException("position is not a class talent");
   Talent candidate=row.get(position);choices.add(new Choice(cls,candidate,current.get(replacing),current.containsKey(candidate)));
  }
  return Collections.unmodifiableList(choices);
 }
 public static boolean apply(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero,int tier,Talent replacing,Talent target) {
  if(hero==null||tier<1||tier>3||tier>hero.talents.size())return false;
  LinkedHashMap<Talent,Integer> row=hero.talents.get(tier-1);
  if(!row.containsKey(replacing)||row.containsKey(target))return false;
  boolean available=false;
  for(Choice choice:atPosition(hero.heroClass,tier,row,replacing))if(choice.talent==target&&!choice.alreadyOwned)available=true;
  if(!available)return false;
  LinkedHashMap<Talent,Integer> replacement=new LinkedHashMap<>();
  for(Talent talent:row.keySet())replacement.put(talent==replacing?target:talent,row.get(talent));
  // Keep SPD's original alias simplification and the same point count/row order.
  if(!hero.metamorphedTalents.containsValue(replacing))hero.metamorphedTalents.put(replacing,target);
  else if(hero.metamorphedTalents.get(target)==replacing)hero.metamorphedTalents.remove(target);
  else for(Talent original:hero.metamorphedTalents.keySet())
   if(hero.metamorphedTalents.get(original)==replacing)hero.metamorphedTalents.put(original,target);
  hero.talents.set(tier-1,replacement);
  return true;
 }
}
