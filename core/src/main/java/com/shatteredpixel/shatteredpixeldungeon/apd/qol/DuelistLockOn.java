/* ADHD Pixel Dungeon (C) 2026 Chris Charlton, GPL-3.0-or-later */
package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Duelist weapon-ability lock-on (Chris, Nov 21; APD change, not in SPD). SPD kept the crosshair on the last enemy
 * you targeted even when that enemy was out of the ability's reach and another enemy was right next to you.
 *  1. The last target keeps the lock while it is alive, in view and within the ability's reach.
 *  2. Otherwise the lock goes to the closest visible enemy within reach.
 *  3. If no enemy is within reach, the lock goes to the closest visible enemy.
 * Ties go to the last target, then to the enemy listed first (stable, no randomness).
 */
public final class DuelistLockOn {
	private DuelistLockOn(){}

	/** The rule itself, independent of the game (tested directly). */
	public static <T> T pick(T last, List<T> visible, Predicate<T> inReach, ToIntFunction<T> distance){
		if (last != null && visible.contains(last) && inReach.test(last)) return last;
		T bestIn = null, bestAny = null;
		for (T t : visible){
			int d = distance.applyAsInt(t);
			if (inReach.test(t) && (bestIn == null || d < distance.applyAsInt(bestIn) || (d == distance.applyAsInt(bestIn) && t == last))) bestIn = t;
			if (bestAny == null || d < distance.applyAsInt(bestAny) || (d == distance.applyAsInt(bestAny) && t == last)) bestAny = t;
		}
		return bestIn != null ? bestIn : bestAny;
	}

	/** Enemies the crosshair may lock onto (same filter SPD uses for the last target). */
	static boolean lockable(Hero hero, Char c){
		return c != null && c != hero && Actor.chars().contains(c) && c.isAlive() && !Char.hasProp(c, Char.Property.OBJECT)
				&& c.alignment != Char.Alignment.ALLY && Dungeon.level != null
				&& c.pos >= 0 && c.pos < Dungeon.level.length() && Dungeon.level.heroFOV[c.pos];
	}

	/** The target for a Duelist ability of this weapon, or null when no enemy is in view. */
	public static Char pick(Hero hero, MeleeWeapon weapon, Char last){
		List<Char> visible = new ArrayList<>();
		for (Mob m : Dungeon.level.mobs) if (lockable(hero, m)) visible.add(m);
		if (lockable(hero, last) && !visible.contains(last)) visible.add(last);
		return pick(lockable(hero, last) ? last : null, visible,
				c -> weapon.abilityReaches(hero, c),
				c -> Dungeon.level.distance(hero.pos, c.pos));
	}
}
