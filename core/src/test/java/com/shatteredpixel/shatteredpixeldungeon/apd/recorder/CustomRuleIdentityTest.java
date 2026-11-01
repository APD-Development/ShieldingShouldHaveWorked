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

package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class CustomRuleIdentityTest {
 @Test public void historicalAndNeutralIdentitiesArePreserved(){for(int difficulty=1;difficulty<=5;difficulty++)for(int challenges:new int[]{0,511}){Ruleset base=Ruleset.of(0,challenges,difficulty);assertSame(base,Ruleset.withCustomRules(base,null));assertSame(base,Ruleset.withCustomRules(base,CustomDungeonRules.defaults()));}assertSame(Ruleset.unknown().getClass(),Ruleset.withCustomRules(Ruleset.unknown(),CustomDungeonRules.defaults()).getClass());assertTrue(Ruleset.withCustomRules(Ruleset.unknown(),CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.ENEMY_HP,200)).isUnknown());}
 @Test public void everySliderAndScrollCountSeparatesCustomScopesWithoutChangingBaseRules(){Ruleset base=Ruleset.of(0,511,5,1,true);Set<String> ids=new HashSet<>();ids.add(base.id);for(CustomDungeonRules.Slider slider:CustomDungeonRules.Slider.values()){int v=slider==CustomDungeonRules.Slider.UPGRADE_SCROLLS?60:slider.min;Ruleset custom=Ruleset.withCustomRules(base,CustomDungeonRules.defaults().with(slider,v));assertTrue(custom.text,custom.text.startsWith(base.text+";custom-v1"));assertTrue(ids.add(custom.id));assertEquals(base.exoticChance,custom.exoticChance);assertEquals(custom.id,Ruleset.withCustomRules(base,CustomDungeonRules.defaults().with(slider,v)).id);}for(int count=1;count<=59;count++){if(count==15)continue;assertTrue(ids.add(Ruleset.withCustomRules(base,CustomDungeonRules.defaults().with(CustomDungeonRules.Slider.UPGRADE_SCROLLS,count)).id));}}
}
