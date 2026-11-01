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

package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

//The seams where the game asks the difficulty system about something. Each is one call at one central place in the game; with a neutral
//modifier each returns its input unchanged (bit for bit), which is what keeps difficulty 3 identical to the game before this system existed.
public final class DifficultyHooks {

	private DifficultyHooks(){}

	//a freshly created enemy (Level.createMob): its health is scaled (never a boss; champion and boss rules are not part of difficulty yet)
	public static void onMobCreated( Mob m ){
		if (Char.hasProp(m, Char.Property.BOSS)) return;
		int ht = m.HT, hp = m.HP;
		m.HT = Math.max(1, Difficulty.scale(Difficulty.Modifier.ENEMY_HP, ht));
		m.HP = Math.min(m.HT, Math.max(1, Difficulty.scale(Difficulty.Modifier.ENEMY_HP, hp)));
		if (Difficulty.modifier(Difficulty.Modifier.ENEMY_HP) == 1.0){
			m.HT = ht;
			m.HP = hp;
		}
	}

	//damage an attacker is about to deal (Char.attack): only hostile non-boss creatures are scaled, never the hero, allies or bosses
	public static float attackDamage( Char attacker, float damage ){
		if (!(attacker instanceof Mob) || attacker.alignment != Char.Alignment.ENEMY || Char.hasProp(attacker, Char.Property.BOSS)) return damage;
		return Difficulty.scale(Difficulty.Modifier.ENEMY_DAMAGE, damage);
	}

	//the number of ordinary random items a regular level places (RegularLevel.createItems), before they are drawn: scaled, never below one
	public static int ordinaryItemCount( int count ){
		if (count <= 0) return count;
		int k = Difficulty.scale(Difficulty.Modifier.RESOURCES, count);
		return Difficulty.modifier(Difficulty.Modifier.RESOURCES) == 1.0 ? count : Math.max(1, k);
	}
}
