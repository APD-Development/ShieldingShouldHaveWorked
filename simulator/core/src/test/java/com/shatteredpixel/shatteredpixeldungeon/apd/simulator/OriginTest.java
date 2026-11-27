package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

//The origin test (the reason this repository is called ShieldingShouldHaveWorked): a STARVING hero at 1 HP with a Potion of
//Shielding active must survive starvation for as long as the shield lasts. Runs on every build, forever, for every class.
public class OriginTest {

	static final class QuietSprite extends CharSprite {
		@Override public void idle(){}
		@Override public void showStatus(int color, String text, Object... args){}
		@Override public void showStatusWithIcon(int color, String text, int icon, Object... args){}
		@Override public void showAlert(){}
		@Override public void hideAlert(){}
		@Override public void hideLost(){}
		@Override public void hideInvestigate(){}
	}

	@Test
	public void starvingAtOneHpWithShieldingSurvives() throws Exception {
		HeadlessTestSupport.start();
		SPDSettings.intro(false); //the tutorial pauses hunger
		for (HeroClass cls : HeroClass.values()){
			Dungeon.daily = Dungeon.dailyReplay = false;
			Dungeon.customSeedText = "";
			Dungeon.seed = 1_000_000L + cls.ordinal();
			GamesInProgress.selectedClass = cls;
			Dungeon.hero = null;
			Dungeon.init();
			Dungeon.depth = 1;
			Dungeon.branch = 0;
			Dungeon.level = Dungeon.newLevel();
			Hero hero = Dungeon.hero;
			hero.pos = Dungeon.level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE).cell();
			QuietSprite sprite = new QuietSprite();
			hero.sprite = sprite;
			sprite.ch = hero;

			Hunger hunger = Buff.affect(hero, Hunger.class);
			Field level = Hunger.class.getDeclaredField("level");
			level.setAccessible(true);
			level.setFloat(hunger, Hunger.STARVING);
			hero.HP = 1;
			new PotionOfShielding().apply(hero);
			int shield = hero.shielding();
			assertTrue(cls + ": the potion gave no shield", shield > 0);

			//starvation ticks until the shield would be used up
			int ticks = 0;
			while (hero.shielding() > 0 && ticks < 5000){
				Field act = null;
				java.lang.reflect.Method m = Hunger.class.getDeclaredMethod("act");
				m.setAccessible(true);
				m.invoke(hunger);
				ticks++;
				assertTrue(cls + ": died from starvation after " + ticks + " turns while " + hero.shielding()
						+ " shield points were still up (started with " + shield + ")", hero.isAlive());
			}
			assertTrue(cls + ": dead at the end", hero.isAlive() || hero.shielding() == 0);
			Buff.detach(hero, Barrier.class);
		}
	}
}
