package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//Found by the simulator (MAGE seed GAI-RVB-UDM, save/load check at turn 300, after a Scroll of Transmutation on the staff):
//when the Mage's staff takes a new wand, the old wand's charging effect must leave the hero.
public class StaffChargerTest {

	@Test
	public void replacingTheStaffsWandLeavesExactlyOneCharger(){
		HeadlessTestSupport.start();
		SPDSettings.intro(false);
		Dungeon.daily = Dungeon.dailyReplay = false;
		Dungeon.customSeedText = "";
		Dungeon.seed = 424242L;
		GamesInProgress.selectedClass = HeroClass.MAGE;
		Dungeon.hero = null;
		Dungeon.init();
		Dungeon.depth = 1;
		Dungeon.branch = 0;
		Dungeon.level = Dungeon.newLevel();
		Hero hero = Dungeon.hero;
		hero.pos = Dungeon.level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE).cell();

		assertTrue("the Mage starts with a staff", hero.belongings.weapon() instanceof MagesStaff);
		MagesStaff staff = (MagesStaff) hero.belongings.weapon();
		assertEquals("one charger for the staff's wand at the start", 1, hero.buffs(Wand.Charger.class).size());

		//what a Scroll of Transmutation does to a staff (owner null), and what imbuing does (owner the hero)
		staff.imbueWand(new WandOfFireblast(), null);
		assertEquals("after transmuting the staff", 1, hero.buffs(Wand.Charger.class).size());
		staff.imbueWand(new WandOfFireblast(), hero);
		assertEquals("after imbuing a wand", 1, hero.buffs(Wand.Charger.class).size());
	}
}
