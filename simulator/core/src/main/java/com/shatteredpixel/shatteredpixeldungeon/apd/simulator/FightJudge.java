package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//The fight judge: plain arithmetic on the game's own combat numbers, the sums a careful player does in their head.
//For one monster it works out how often each side hits, how hard, and from that how much health a toe-to-toe melee
//fight should cost the hero. Nothing here is learned or trained, and nothing in the game is changed by asking:
//the monster's damage and armor are sampled from a throwaway copy of it, on a throwaway random stream.
final class FightJudge {

	//what one kind of monster does on this depth, measured once from a throwaway copy
	static final class Stats {
		float avgDamage, maxDamage, avgArmor;
	}

	private static final Map<String, Stats> cache = new HashMap<>();
	private static final int SAMPLES = 200;

	//off = the bot plays by its older fixed rules (kept so the judge can be measured against them)
	public static boolean enabled = true;

	static void reset(){ cache.clear(); }

	static final class Verdict {
		//chance per swing
		float heroHits, mobHits;
		//health per landed hit, after armor
		float heroDamage, mobDamage;
		//the hero's swings needed to kill it, and the health that should cost
		float turnsToKill, cost;
		//the worst single hit it can land
		float worstHit;
		//health the hero loses per turn while it stands next to them
		float perTurn;

		boolean cannotWin(){ return heroHits <= 0f || heroDamage <= 0f; }
	}

	//the game's hit rule: both sides roll from 0 up to their skill, the attacker hits on a tie or better
	static float hitChance(float accuracy, float evasion){
		if (evasion >= Char.INFINITE_EVASION) return 0f;
		if (accuracy >= Char.INFINITE_ACCURACY) return 1f;
		if (accuracy <= 0) return 0f;
		if (evasion <= 0) return 1f;
		return accuracy >= evasion ? 1f - evasion / (2f * accuracy) : accuracy / (2f * evasion);
	}

	static Stats stats(Mob m){
		int wraithLevel = 0;
		if (m instanceof Wraith){
			Object l = SimUi.field(m, "level");
			wraithLevel = l instanceof Integer ? (Integer) l : 0;
		}
		String key = m.getClass().getName() + "@" + Dungeon.depth + "/" + wraithLevel;
		Stats s = cache.get(key);
		if (s != null) return s;
		s = new Stats();
		//before anything is known: a guess that grows with depth
		s.avgDamage = 1.5f + Dungeon.depth;
		s.maxDamage = 3f + 2f * Dungeon.depth;
		s.avgArmor = Dungeon.depth / 3f;
		Random.pushGenerator(0x5EEDL + key.hashCode());
		try {
			Mob copy = Reflection.newInstance(m.getClass());
			if (copy != null){
				if (copy instanceof Wraith) ((Wraith) copy).adjustStats(wraithLevel);
				float dmg = 0, max = 0, armor = 0;
				for (int i = 0; i < SAMPLES; i++){
					int d = copy.damageRoll();
					dmg += d;
					max = Math.max(max, d);
					armor += copy.drRoll();
				}
				s.avgDamage = dmg / SAMPLES;
				s.maxDamage = max;
				s.avgArmor = armor / SAMPLES;
			}
		} catch (Throwable ignored){
			//keeps the guess
		} finally {
			Random.popGenerator();
		}
		cache.put(key, s);
		return s;
	}

	private static float heroAccuracy(Hero h){
		Object a = SimUi.field(h, "attackSkill");
		return a instanceof Integer ? (Integer) a : 10 + h.lvl - 1;
	}

	private static float heroEvasion(Hero h){
		Object d = SimUi.field(h, "defenseSkill");
		float ev = d instanceof Integer ? (Integer) d : 5 + h.lvl - 1;
		//armor heavier than the hero's strength cuts evasion for every missing point (game rule)
		Armor a = h.belongings.armor();
		if (a != null && a.STRReq() > h.STR()) ev /= (float) Math.pow(1.5, a.STRReq() - h.STR());
		if (h.paralysed > 0) ev = 0;
		return ev;
	}

	private static float heroArmor(Hero h){
		Armor a = h.belongings.armor();
		if (a == null) return 0f;
		float dr = (a.DRMin() + a.DRMax()) / 2f;
		if (a.STRReq() > h.STR()) dr -= 2f * (a.STRReq() - h.STR());
		return Math.max(0f, dr);
	}

	private static float heroWeaponDamage(Hero h){
		if (h.belongings.weapon() instanceof MeleeWeapon){
			MeleeWeapon w = (MeleeWeapon) h.belongings.weapon();
			float d = (w.min() + w.max()) / 2f;
			if (h.STR() > w.STRReq()) d += (h.STR() - w.STRReq()) / 2f;
			return d;
		}
		//bare hands
		return (1 + Math.max(h.STR() - 8, 1)) / 2f;
	}

	static Verdict judge(Mob m){
		Hero h = Dungeon.hero;
		Verdict v = new Verdict();
		Stats s = stats(m);
		float mobAccuracy;
		try { mobAccuracy = m.attackSkill(h); } catch (Throwable t){ mobAccuracy = 8 + Dungeon.depth; }
		float mobEvasion = m.defenseSkill;
		//asleep or not yet aware of the hero: the first hit can't be dodged
		v.heroHits = hitChance(heroAccuracy(h), mobEvasion);
		v.mobHits = hitChance(mobAccuracy, heroEvasion(h));
		v.heroDamage = Math.max(0f, heroWeaponDamage(h) - s.avgArmor);
		//armor rolls can swallow a whole swing; never count on less than a quarter getting through
		v.mobDamage = Math.max(s.avgDamage * 0.25f, s.avgDamage - heroArmor(h));
		v.worstHit = Math.max(1f, s.maxDamage - heroArmor(h) / 2f);
		v.perTurn = v.mobHits * v.mobDamage;
		if (v.cannotWin()){
			v.turnsToKill = Float.MAX_VALUE;
			v.cost = Float.MAX_VALUE;
		} else {
			v.turnsToKill = (float) Math.ceil(m.HP / (double) v.heroDamage) / v.heroHits;
			//it swings once for each of the hero's turns, minus the last (it is dead by then)
			v.cost = Math.max(0f, v.turnsToKill - 0.5f) * v.perTurn;
		}
		return v;
	}

	//health all of these should cost if fought one after another while the rest keep swinging
	static float costOfAll(List<Mob> foes){
		float cost = 0, pressure = 0;
		for (Mob m : foes) pressure += judge(m).perTurn;
		for (Mob m : foes){
			Verdict v = judge(m);
			if (v.cannotWin()) return Float.MAX_VALUE;
			cost += v.turnsToKill * pressure;
			pressure -= v.perTurn;
		}
		return cost;
	}

	static String describe(Mob m){
		Verdict v = judge(m);
		if (v.cannotWin()) return "can't be hurt by melee right now";
		return String.format(java.util.Locale.ROOT, "I hit %d%% for %.1f, it hits %d%% for %.1f, about %.0f turns, costs about %.0f health",
				Math.round(v.heroHits * 100), v.heroDamage, Math.round(v.mobHits * 100), v.mobDamage, v.turnsToKill, v.cost);
	}
}
