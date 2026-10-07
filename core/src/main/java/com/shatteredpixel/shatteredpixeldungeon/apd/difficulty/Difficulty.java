package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.replay.ReplayContext;

//APD's global difficulty system: levels 1-5, level 3 is the game as it is (the baseline). This class is the ONE place that knows what a
//level changes: gameplay code asks for a Modifier and never looks at the level itself, so a later balance pass edits the table below and
//nothing else (the few seams in the game call DifficultyHooks, which call here).
//
//STATE: no balance values are approved yet. Every level's modifiers are neutral (1.0) and every level but 3 has balance revision 0, "placeholder".
//Level 3 is neutral by definition, whatever the table says, and its balance revision is always 0: the baseline never changes.
//
//IDENTITY: a run's difficulty is fixed when it starts (Dungeon.difficulty, saved with the run) and is part of its ruleset identity, so UIMSD
//always knows it. Changing the setting only affects runs started afterwards. The balance revision is recorded too, so runs played with
//placeholder balance can never be mixed with runs played with approved balance.
//
//NON-P2W (permanent): difficulty is a plain gameplay setting, free for everyone, and nothing may ever be sold that changes it or its results.
public final class Difficulty {

	private Difficulty(){}

	public static final int MIN = 1, DEFAULT = 3, MAX = 5;

	//what a level can change. Add a constant here when a new difficulty-sensitive system is brought under control; nothing else moves.
	public enum Modifier {
		ENEMY_HP,        //enemies' health when they are created
		ENEMY_DAMAGE,    //damage enemies deal
		RESOURCES,       //how much loot/consumables the dungeon generates
		HEALING,         //healing the hero receives
		FOOD,            //hunger and food
		ENEMY_SPAWN,     //how many enemies are generated and how fast new ones arrive
		BOSS,            //boss strength
		CHAMPION,        //chance of champion enemies
		TRAPS            //trap frequency and strength
	}

	//modifier by level (row = level 1..5, column = Modifier ordinal). PLACEHOLDER: all neutral until the owner approves values.
	private static final double[][] TABLE = new double[MAX][Modifier.values().length];

	//balance revision by level (index = level - 1): 0 = placeholder (no modifiers applied), 1+ = an approved balance pass.
	private static final int[] REVISION = new int[MAX];

	static {
		for (double[] row : TABLE) java.util.Arrays.fill(row, 1.0);
	}

	public static int normalize( int level ){
		return level < MIN ? DEFAULT : level > MAX ? DEFAULT : level;
	}

	public static boolean isValid( int level ){
		return level >= MIN && level <= MAX;
	}

	//the difficulty the game is being played at now: the run's (Dungeon.difficulty), or the run being replayed by a floor replay
	public static int level(){
		if (ReplayContext.active()) return ReplayContext.difficulty();
		return Dungeon.difficulty;
	}

	public static int balanceRevision( int level ){
		return level == DEFAULT || !isValid(level) ? 0 : REVISION[level - 1];
	}

	//1.0 for the baseline level, whatever the table says
	public static double modifier( int level, Modifier m ){
		if (level == DEFAULT || !isValid(level)) return 1.0;
		return TABLE[level - 1][m.ordinal()];
	}

	public static double modifier( Modifier m ){
		return modifier(level(), m);
	}

	//v scaled by the current modifier; exactly v (no float rounding) when the modifier is neutral
	public static float scale( Modifier m, float v ){
		double k = modifier(m);
		return k == 1.0 ? v : (float) (v * k);
	}

	public static int scale( Modifier m, int v ){
		double k = modifier(m);
		return k == 1.0 ? v : (int) Math.round(v * k);
	}

	//true when this level changes nothing yet (every level now, until balance is approved)
	public static boolean isNeutral( int level ){
		for (Modifier m : Modifier.values()) if (modifier(level, m) != 1.0) return false;
		return true;
	}

	//placeholder wording (final names come with the language pass)
	public static String label( int level ){
		switch (level){
			case 1: return "1 - Easiest";
			case 2: return "2 - Easier";
			case 3: return "3 - Standard";
			case 4: return "4 - Harder";
			case 5: return "5 - Hardest";
			default: return "unknown";
		}
	}
}
