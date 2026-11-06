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
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.RingItemEffects;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar;

/** Standard attack timing only: never calls attackDelay, accuracyFactor or a damage roll. */
public final class WeaponExplanation {
    private WeaponExplanation() {}

    public static Float normalAttackDelay(Weapon weapon, Hero hero) {
        if (!weapon.levelKnown || weapon instanceof SpiritBow || weapon instanceof SpiritBow.SpiritArrow) return null;
        Integer strength = ArmorExplanation.knownStrength(hero);
        Integer furor = RingItemEffects.knownBonus(hero, RingOfFuror.class, true);
        if (strength == null || furor == null) return null;
        float base = weapon.augment.delayFactor(weapon.DLY);
        int missing = Math.max(0, weapon.STRReq() - strength);
        if (missing > 0) base *= Math.pow(1.2, missing);
        float speed = RingOfFuror.attackSpeedMultiplier(furor);
        if (hero.buff(Scimitar.SwordDance.class) != null) speed += .6f;
        return base * (1f / speed);
    }

    public static String summary(Weapon weapon, Hero hero) {
        String result = "Attack time is measured in game turns: a smaller time lets you attack sooner. Being below this weapon's strength requirement makes ordinary attacks slower and less accurate; an accuracy bonus changes your attack score, not your chance to hit by that percentage.";
        Float delay = normalAttackDelay(weapon, hero);
        if (delay != null) result += " Used normally with your known bonuses: _" + ObfuscationExplanation.number(delay)
                + " turns per attack_ before effects that change all action times, special abilities or free attacks. Unarmed stances use their own timing.";
        return result;
    }

    public static String details(Weapon weapon, Hero hero) {
        String result = "_Weapon damage, accuracy and time_\nThe displayed damage range describes a damage roll, not guaranteed HP lost. The item description states its upgrade and augmentation effects; excess strength, talents, rings, enchantments, abilities and the target's defenses can change the result. Do not treat the range as a flat percentage.\n"
                + "In the standard weapon calculation, each missing strength point divides the weapon's accuracy factor by 1.5 and multiplies its attack time by 1.2. Upgrades reduce strength requirements at +1, +3, +6, +10 and so on; negative levels count as zero for this reduction. Mastery reduces the requirement by 2. Temporary effective levels and permanent upgrades need not affect the requirement in the same way.\n"
                + "Speed augmentation multiplies rolled damage by 0.7 and base attack time by 2/3. Damage augmentation multiplies rolled damage by 1.5 and base attack time by 5/3. Damage rounds to whole points. These are separate factors: faster does not mean a matching percentage reduction in damage.\n"
                + "Standard attack time = augmented base time × 1.2^missing strength / attack-speed factor. Furor contributes 1.09051^effective ring bonus to that factor. Sword Dance adds 0.6 to the factor before division; it does not multiply final time by 0.6. Other all-action effects, free attacks, unarmed stances and special abilities apply their own rules.\n";
        if (weapon instanceof SpiritBow || weapon instanceof SpiritBow.SpiritArrow)
            return result + "Spirit Bow shots also have special Sniper-shot timing and Nature's Power bonuses; the standard timing formula alone is not a complete shot preview.";
        if (!weapon.levelKnown) return result + "Personal upgrade-dependent timing stays hidden until this weapon's upgrades are identified.";
        Float delay = normalAttackDelay(weapon, hero);
        if (delay == null) return result + "Personal attack timing is withheld while contributing ring bonuses are unknown or unavailable.";
        int missing = Math.max(0, weapon.STRReq() - ArmorExplanation.knownStrength(hero));
        return result + "This weapon's base attack time: " + ObfuscationExplanation.number(weapon.DLY)
                + " turns; after augmentation: " + ObfuscationExplanation.number(weapon.augment.delayFactor(weapon.DLY))
                + " turns; missing strength: " + missing + " points. Normal weapon attack time with your known bonuses: "
                + ObfuscationExplanation.number(delay) + " turns before the other exceptions above. This does not consume a waiting free attack or ability.";
    }
}
