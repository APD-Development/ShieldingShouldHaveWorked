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
package com.shatteredpixel.shatteredpixeldungeon.apd.mechanics;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.RingItemEffects;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.*;

/** Describes thrown effects without querying future targets or invoking dart wear calculations. */
public final class MissileExplanation {
    private MissileExplanation(){}
    private static String n(float value){return ObfuscationExplanation.number(value);}
    public static String details(MissileWeapon weapon,Hero hero){
        if(weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow.SpiritArrow)return "";
        String text="_Thrown-weapon durability units_\nA full durability reserve is 100 points. The ordinary expected-use input starts at "+n(weapon.baseDurabilityUses())+" × 1.5^(ordinary weapon level). Durable Projectiles, holster storage, damage/speed augmentation and Sharpshooting apply their separate factors. This uses the ordinary weapon-level path, separate from temporary effective damage-level bonuses. Crystal enchantment halves that use input and caps it at 50. For the ordinary rounded path, an unrounded input of at least 100 gives no wear; otherwise round the use input to whole uses and spend 100 / rounded uses + 0.001 durability points per qualifying use. That small correction is existing rounding protection, not extra damage. Subclasses such as tipped darts apply their additional wear rules and their own rounding stage. The uses shown above are durability estimates, not a promise that every throw hits or a random independent break chance. Shared stacks retain their normal wear and recovery rules. Inspection does not spend durability, choose a target or reset a dart's pending impact position.";
        if(weapon instanceof Bolas)text+="\n_Bolas_\nA qualifying hit offers Cripple for "+n(Cripple.DURATION/2)+" game-time units before normal resistance and reapplication rules. This slows supported movement; it is not extra damage or a guaranteed number of missed enemy attacks.";
        if(weapon instanceof Tomahawk){
            text+="\n_Tomahawk bleeding_\nA qualifying hit offers a middle-biased random bleeding strength between 3 + damage level / 2 and 6 + damage level, then applies damage augmentation. Damage level includes the normal effective weapon level and Sharpshooting damage-level bonus. Bleeding then uses its own random strength-reduction, damage-tick and defense rules; the strength is not guaranteed total health lost.";
            Integer sharpshooting=RingItemEffects.knownBonus(hero,RingOfSharpshooting.class,true);
            if(weapon.isIdentified()&&sharpshooting!=null){int level=weapon.buffedLvl()+sharpshooting;text+=" Current offered strength range before subsequent defenses: "+n(weapon.augment.damageFactor(((Tomahawk)weapon).minBleed(level)))+"–"+n(weapon.augment.damageFactor(((Tomahawk)weapon).maxBleed(level)))+" points.";}
        }
        if(weapon instanceof FishingSpear)text+="\n_Fishing spear against piranhas_\nAt its on-hit processing step, the damage input becomes the greater of its incoming damage and half the piranha's current HP, rounded down. It does not guarantee half of maximum HP as final damage, bypass later defenses or apply this minimum to every enemy type. No target HP is queried while inspecting.";
        if(weapon instanceof Shuriken)text+="\n_Shuriken's time-free throw_\nWithout the Shuriken cooldown tracker, the cast's normal time is zero and throwing starts the existing 19-unit scheduled cooldown (the displayed convention includes one additional unit). While the tracker is present, subsequent throws use normal cast timing. This is not free ammunition, guaranteed damage or a change to the visual Game speed setting.";
        if(weapon instanceof HeavyBoomerang)text+="\n_Returning boomerang_\nA surviving boomerang schedules a return after five supported updates on the recorded floor and branch. The destination is the hero's recorded throwing cell, rather than following the hero wherever they move. A return to the hero can be collected with no normal pickup delay; an occupied destination follows the existing return-hit rules and can attack another character occupying that return cell. Collision, durability, inventory space and floor changes keep their normal effects.";
        if(weapon instanceof ForceCube)text+="\n_Force cube impact area_\nA supported impact processes the center cell and its eight immediate neighbors: a 3 × 3 area. Characters there receive separate attack processing; allies and the hero can be affected. Cell presses can trigger traps, with the existing Tengu-dart-trap exception for neighboring cells. An empty pit follows the ordinary dropped-projectile branch. Area size is not a guarantee that every target takes the displayed maximum damage.";
        if(weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart)text+="\n_Dart tips_\nTip wear follows the dart's special calculation, including Durable Tips, supported lotus preservation and Charged Shot. Used-up tips return ordinary darts under the normal rules; a status effect's strength or duration is separate from how many tipped uses remain. This inspector does not simulate a future lotus position or charged impact.";
        return text;
    }
}
