package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

//The seams where the game asks the difficulty system about something. Each is one call at one central place in the game; with a neutral
//modifier each returns its input unchanged (bit for bit), which is what keeps difficulty 3 identical to the game before this system existed.
public final class DifficultyHooks {

	private DifficultyHooks(){}

	//a freshly created enemy (Level.createMob): health now; champion and boss rules join here when they are approved
	public static void onMobCreated( Mob m ){
		double k = Difficulty.modifier(Difficulty.Modifier.ENEMY_HP);
		if (k == 1.0) return;
		m.HT = Math.max(1, (int) Math.round(m.HT * k));
		m.HP = Math.min(m.HT, Math.max(1, (int) Math.round(m.HP * k)));
	}

	//damage an attacker is about to deal (Char.attack): only hostile creatures are scaled, never the hero or allies
	public static float attackDamage( Char attacker, float damage ){
		if (!(attacker instanceof Mob) || attacker.alignment != Char.Alignment.ENEMY) return damage;
		return Difficulty.scale(Difficulty.Modifier.ENEMY_DAMAGE, damage);
	}
}
