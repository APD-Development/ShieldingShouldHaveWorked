package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.Difficulty;
import com.watabou.utils.GameSettings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

//One simulator's settings. They only steer the bot and pick the run setup; they never change how the game plays.
public final class SimProfile {

	public enum Aggression { CAUTIOUS("Cautious"), NORMAL("Normal"), AGGRESSIVE("Aggressive"), RECKLESS("Reckless");
		public final String title; Aggression(String t){ title = t; } }

	//Five levels, hand-written rules only. Target full-win rates (Chris, Nov 27; Standard difficulty, no challenges):
	//Clueless 0%, Average 0.5%, Smart 3%, Pro 7.5%, Expert 15%. Targets to tune towards, measured by the benchmark.
	public enum Intelligence { CLUELESS("Clueless", 0f), AVERAGE("Average", 0.5f), SMART("Smart", 3f), PRO("Pro", 7.5f), EXPERT("Expert", 15f);
		public final String title; public final float targetWinPercent;
		Intelligence(String t, float w){ title = t; targetWinPercent = w; } }

	public enum SeedMode { RANDOM("Random"), TYPED("Typed seed"), REAL_RUN("Seed of a real run");
		public final String title; SeedMode(String t){ title = t; } }

	public enum Stop { FOREVER("Run forever"), RUNS("After N runs"), MINUTES("After N minutes"), FIRST_BUG("On the first bug"),
		FIRST_WIN("On the first win"), FLOOR("At floor N");
		public final String title; Stop(String t){ title = t; } }

	public int index;
	public String name = "Simulator";
	public int heroClass = -1;        //-1 rotates through every class
	public int difficulty = Difficulty.DEFAULT;
	public int challenges = 0;
	public String customRules = "";   //CustomDungeonRules preference text, empty = defaults
	public SeedMode seedMode = SeedMode.RANDOM;
	public String seedText = "";      //typed seed, or the real run's seed code
	public boolean randomizeSettings = false;
	public Aggression aggression = Aggression.NORMAL;
	public Intelligence intelligence = Intelligence.AVERAGE;
	public Stop stop = Stop.FOREVER;
	public int stopAmount = 10;
	public int chauffeurFloor = 0;
	public int verifyEvery = 10;     //determinism check: replay every Nth run and compare (0 = off)
	public int checkpointFloor = 0;  //start runs from a saved checkpoint at this floor or deeper (0 = always start at floor 1)   //Chauffeur: play to this floor, then hand the controls to the player (0 = off)

	//APD Settings the simulator may randomize each run (setters on SPDSettings taking a boolean). Recording is never touched.
	static final String[] RANDOMIZABLE = {
			"autoPickupWhenFull", "continueOnTrapDiscovery", "adaptiveSearchChoice", "singleTapSearch", "autoEnterStairs",
			"autoCollectGrassDew", "autoCollectDewWithEnemies", "autoEatWhileWaiting", "autoEatWhenHungry", "confirmKnownTraps",
			"fullPackSwapPrompt", "ratKingQuickSearch", "stairNavigationShortcut", "trophyOutlines", "dotOverlayEnabled",
			"enhancedTileVisibility", "waitStopCritical", "waitStopFullHealth", "waitStopStarving", "apdFoodInHealing", "apdAdultHumor"
	};

	public String classTitle(){
		return heroClass < 0 ? "Rotate all" : HeroClass.values()[heroClass].title();
	}

	public String encode(){
		return String.join("\t", name.replace('\t', ' '), String.valueOf(heroClass), String.valueOf(difficulty), String.valueOf(challenges),
				customRules, seedMode.name(), seedText.replace('\t', ' '), String.valueOf(randomizeSettings), aggression.name(),
				intelligence.name(), stop.name(), String.valueOf(stopAmount), String.valueOf(chauffeurFloor), String.valueOf(checkpointFloor), String.valueOf(verifyEvery));
	}

	public static SimProfile decode(int index, String text){
		SimProfile p = new SimProfile();
		p.index = index;
		p.name = "Simulator " + (index + 1);
		if (text == null || text.isEmpty()) return p;
		try {
			String[] f = text.split("\t", -1);
			p.name = f[0];
			p.heroClass = Integer.parseInt(f[1]);
			p.difficulty = Integer.parseInt(f[2]);
			p.challenges = Integer.parseInt(f[3]);
			p.customRules = f[4];
			p.seedMode = SeedMode.valueOf(f[5]);
			p.seedText = f[6];
			p.randomizeSettings = Boolean.parseBoolean(f[7]);
			p.aggression = Aggression.valueOf(f[8]);
			p.intelligence = Intelligence.valueOf(f[9]);
			p.stop = Stop.valueOf(f[10]);
			p.stopAmount = Integer.parseInt(f[11]);
			if (f.length > 12) p.chauffeurFloor = Integer.parseInt(f[12]);
			if (f.length > 13) p.checkpointFloor = Integer.parseInt(f[13]);
			if (f.length > 14) p.verifyEvery = Integer.parseInt(f[14]);
		} catch (RuntimeException damaged){
			//a damaged or older entry keeps whatever was read; the rest stays default
		}
		if (p.heroClass >= HeroClass.values().length) p.heroClass = -1;
		return p;
	}

	public static SimProfile load(int index){
		return decode(index, GameSettings.getString("apdsim_profile_" + index, ""));
	}

	public void save(){
		GameSettings.put("apdsim_profile_" + index, encode());
	}

	//Puts this profile's run setup into the simulator's own settings (never the real game's), just before a run starts.
	//Returns the settings that were randomized, for the run's tags and bug reports.
	String applyForRun(Random rng){
		SPDSettings.apdDifficulty(difficulty);
		SPDSettings.challenges(challenges);
		CustomDungeonRules rules = CustomDungeonRules.defaults();
		if (!customRules.isEmpty()){
			try { rules = CustomDungeonRules.fromPreferenceText(customRules); } catch (RuntimeException ignored){}
		}
		SPDSettings.apdCustomRules(rules);
		StringBuilder settings = new StringBuilder();
		for (String name : RANDOMIZABLE){
			try {
				Method set = SPDSettings.class.getMethod(name, boolean.class);
				Method get = SPDSettings.class.getMethod(name);
				if (randomizeSettings) set.invoke(null, rng.nextBoolean());
				if (settings.length() > 0) settings.append(',');
				settings.append(name).append('=').append(get.invoke(null));
			} catch (Exception missing){
				//a setting that no longer exists in this game version is skipped
			}
		}
		return settings.toString();
	}

	public String summary(){
		List<String> parts = new ArrayList<>();
		parts.add(classTitle());
		parts.add(aggression.title + " / " + intelligence.title);
		parts.add("difficulty " + difficulty);
		if (challenges != 0) parts.add("challenges " + Integer.bitCount(challenges));
		if (!customRules.isEmpty()) parts.add("custom rules");
		parts.add(seedMode == SeedMode.RANDOM ? "random seeds" : "seed " + seedText);
		if (randomizeSettings) parts.add("random APD Settings");
		parts.add(stop == Stop.RUNS ? "stop after " + stopAmount + " runs" : stop == Stop.MINUTES ? "stop after " + stopAmount + " min"
				: stop == Stop.FLOOR ? "stop at floor " + stopAmount : stop.title.toLowerCase(Locale.ROOT));
		if (chauffeurFloor > 0) parts.add("Chauffeur to floor " + chauffeurFloor);
		if (checkpointFloor > 0) parts.add("starts from checkpoints at floor " + checkpointFloor + "+");
		return String.join(", ", parts);
	}
}
