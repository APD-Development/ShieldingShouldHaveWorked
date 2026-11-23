package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.GameFacts;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.List;

//The gate for UIMSD deep dive item 3: 100 questions asked the way people ask them. Each must become exactly the question written
//next to it: table | measure | measured field | breakdown | filters. A question that can't be placed must say so (null), never guess.
public class NaturalQuestionsTest {

	@ClassRule
	public static TemporaryFolder tmp = new TemporaryFolder();
	static Catalog cat;

	@BeforeClass
	public static void history() throws Exception {
		String[] classes = { "WARRIOR", "MAGE", "ROGUE", "HUNTRESS", "DUELIST", "CLERIC" };
		for (int i = 0; i < classes.length; i++){
			SyntheticLog s = new SyntheticLog(tmp.getRoot(), "r" + i, TelemetryVersion.CURRENT, 100 + i)
					.start("r" + i, 0, classes[i], "rules", 0, 30, false).enter(1);
			s.heroAttacks("rat", "Rat", true).damageFromHero("rat", "Rat", 4).heroKills("rat", "Rat", 1)
					.enemyAttacks("gnoll", "Gnoll", true).damageToHero("gnoll", "Gnoll", 3).heal(2, true, "potion");
			s.enter(2).heroAttacks("crab", "Crab", false).damageToHero("Gnoll shaman", "GnollShaman", 6).heroKills("crab", "Crab", 2);
			s.factSeries(GameFacts.MOVE_STEP, "", new long[][]{ {10, 32, 1}, {11, 32, 1} });
			s.fact(GameFacts.SPELL_CAST, "Guiding light", 1);
			s.fact(GameFacts.GOLD, "", 30, 30);
			s.acquired("w" + i, "sword", ItemEvents.CAT_WEAPON, true, 1).equip("w" + i, "sword", ItemEvents.CAT_WEAPON);
			if (i % 3 == 0) s.end(LifecycleEvents.OUTCOME_WON, "", 2, 10, 5000 + i);
			else s.heroDies(i % 2 == 0 ? "gnoll" : "Gnoll shaman", CombatEvents.SRC_CHAR)
					.end(LifecycleEvents.OUTCOME_DIED, i % 2 == 0 ? "Gnoll" : "GnollShaman", 2, 4, 100 + i);
			s.close(true);
		}
		cat = new DatasetStore(tmp.getRoot()).load();
	}

	//"table|measure|column|group|filters"
	static String describe( Question q ){
		StringBuilder f = new StringBuilder();
		for (Filter x : q.filters) f.append(f.length() == 0 ? "" : ",").append(x.column).append('=')
				.append(x.texts.length > 0 ? x.texts[0] : String.valueOf(x.nums[0]));
		return q.dataset + "|" + q.measure + "|" + q.column + "|" + (q.groupBy.isEmpty() ? "" : q.groupBy.get(0)) + "|" + f
				+ (q.per == null ? "" : "|per " + q.per);
	}

	static final String[][] CASES = {
			//runs, counting
			{ "how many runs", "runs|COUNT|||" },
			{ "how many runs have I played", "runs|COUNT|||" },
			{ "how many games did i play", "runs|COUNT|||" },
			{ "number of runs", "runs|COUNT|||" },
			{ "how many runs as a mage", "runs|COUNT|||run.heroClass=MAGE" },
			{ "how many runs by class", "runs|COUNT||run.heroClass|" },
			{ "runs per class", "runs|COUNT||run.heroClass|" },
			{ "how many runs for each class", "runs|COUNT||run.heroClass|" },
			{ "which class do i play most", "runs|COUNT||run.heroClass|" },
			{ "how many times have i won", "runs|COUNT|||result.outcome=1" },
			{ "how many wins", "runs|COUNT|||result.outcome=1" },
			{ "how many wins as warrior", "runs|COUNT|||run.heroClass=WARRIOR,result.outcome=1" },
			{ "wins by class", "runs|COUNT||run.heroClass|result.outcome=1" },
			{ "what is my win rate", "runs|COUNT||result.outcome|" },
			{ "how often do i win", "runs|COUNT||result.outcome|" },
			{ "win rate as rogue", "runs|COUNT||result.outcome|run.heroClass=ROGUE" },
			{ "how many victories", "runs|COUNT|||result.outcome=1" },
			{ "how did my runs end", "runs|COUNT||result.outcome|" },
			{ "runs by outcome", "runs|COUNT||result.outcome|" },
			{ "runs by difficulty", "runs|COUNT||run.difficulty|" },
			//runs, numbers
			{ "what's my best score", "runs|HIGHEST|result.score||" },
			{ "highest score", "runs|HIGHEST|result.score||" },
			{ "high score as a mage", "runs|HIGHEST|result.score||run.heroClass=MAGE" },
			{ "best score by class", "runs|HIGHEST|result.score|run.heroClass|" },
			{ "my best run", "runs|HIGHEST|result.score||" },
			{ "average score", "runs|AVERAGE|result.score||" },
			{ "average score by class", "runs|AVERAGE|result.score|run.heroClass|" },
			{ "averge score by clas", "runs|AVERAGE|result.score|run.heroClass|" },
			{ "avg score as cleric", "runs|AVERAGE|result.score||run.heroClass=CLERIC" },
			{ "what's my typical score", "runs|TYPICAL|result.score||" },
			{ "median score", "runs|TYPICAL|result.score||" },
			{ "lowest score", "runs|LOWEST|result.score||" },
			{ "worst score", "runs|LOWEST|result.score||" },
			{ "total score", "runs|TOTAL|result.score||" },
			{ "how deep do i usually get", "runs|TYPICAL|result.deepest||" },
			{ "deepest floor", "runs|HIGHEST|result.deepest||" },
			{ "deepest floor i reached as huntress", "runs|HIGHEST|result.deepest||run.heroClass=HUNTRESS" },
			{ "average deepest floor by class", "runs|AVERAGE|result.deepest|run.heroClass|" },
			{ "longest run", "runs|HIGHEST|result.turns||" },
			{ "shortest run", "runs|LOWEST|result.turns||" },
			{ "average turns", "runs|AVERAGE|result.turns||" },
			{ "highest level", "runs|HIGHEST|result.heroLevel||" },
			{ "average hero level", "runs|AVERAGE|result.heroLevel||" },
			{ "total damage taken", "runs|TOTAL|combat.damageTaken||" },
			{ "how much damage have i taken", "runs|TOTAL|combat.damageTaken||" },
			{ "total damage dealt", "runs|TOTAL|combat.damageDealt||" },
			{ "how much damage did i deal", "runs|TOTAL|combat.damageDealt||" },
			{ "average damage taken by class", "runs|AVERAGE|combat.damageTaken|run.heroClass|" },
			{ "total xp gained", "runs|TOTAL|combat.xpGained||" },
			{ "how much have i healed", "runs|TOTAL|combat.healed||" },
			{ "most gold", "runs|HIGHEST|final.gold||" },
			{ "average final gold", "runs|AVERAGE|final.gold||" },
			{ "highest strength", "runs|HIGHEST|final.strength||" },
			{ "most hero kills in a run", "runs|HIGHEST|combat.heroKills||" },
			{ "average hero kills", "runs|AVERAGE|combat.heroKills||" },
			//deaths
			{ "how many times did i die", "deaths|COUNT|||" },
			{ "how many deaths", "deaths|COUNT|||" },
			{ "what kills me", "deaths|COUNT||death.cause|" },
			{ "what kills me the most", "deaths|COUNT||death.cause|" },
			{ "what killed me", "deaths|COUNT||death.cause|" },
			{ "who killed me most", "deaths|COUNT||death.cause|" },
			{ "what do i die to", "deaths|COUNT||death.cause|" },
			{ "what kils me", "deaths|COUNT||death.cause|" },
			{ "what kills me as a duelist", "deaths|COUNT||death.cause|" },
			{ "where do i die", "deaths|COUNT||death.depth|" },
			{ "which floor do i die on", "deaths|COUNT||death.depth|" },
			{ "deaths by floor", "deaths|COUNT||death.depth|" },
			{ "how many times did a gnoll kill me", "deaths|COUNT|||death.cause=gnoll" },
			{ "how many times did i die to a gnoll shaman", "deaths|COUNT|||death.cause=Gnoll shaman" },
			{ "how many times did i die on floor 2", "deaths|COUNT|||death.depth=2" },
			{ "deaths on floor 5", "deaths|COUNT|||death.depth=5" },
			{ "average level when i die", "deaths|AVERAGE|death.heroLevel||" },
			{ "deaths by cause", "deaths|COUNT||death.cause|" },
			//kills
			{ "how many enemies have i killed", "kills|COUNT|||" },
			{ "how many kills", "kills|COUNT|||" },
			{ "how many rats did i kill", "kills|COUNT|||kill.victim=rat" },
			{ "what did i kill the most", "kills|COUNT||kill.victim|" },
			{ "kills by enemy", "kills|COUNT||kill.victim|" },
			{ "kills per floor", "kills|COUNT||||per FLOOR" },
			{ "kills per run", "kills|COUNT||||per RUN" },
			{ "kills by floor", "kills|COUNT||kill.depth|" },
			{ "how many crabs have i killed as a mage", "kills|COUNT|||kill.runClass=MAGE,kill.victim=crab" },
			//enemies
			{ "how many enemies", "enemies|COUNT|||" },
			{ "which enemy did the most damage to me", "enemies|HIGHEST|enemy.damageToHero|enemy.name|" },
			{ "total damage to hero by enemy", "enemies|TOTAL|enemy.damageToHero|enemy.name|" },
			{ "how much damage did the gnoll do to the hero", "enemies|TOTAL|enemy.damageToHero||enemy.name=gnoll" },
			//items
			{ "how many items", "items|COUNT|||" },
			{ "items by kind", "items|COUNT||item.category|" },
			{ "how many items did i find in chests", "items|COUNT|||item.foundSource=Chest" },
			{ "items by where i found them", "items|COUNT||item.foundSource|" },
			{ "items by how i got them", "items|COUNT||item.acquiredHow|" },
			{ "most times equipped", "items|HIGHEST|item.equipCount||" },
			//events and facts
			{ "how many spell casts", "fact:spell.cast|COUNT|||" },
			{ "spell casts by spell", "fact:spell.cast|COUNT||fact.subject|" },
			{ "spell casts per floor", "fact:spell.cast|COUNT||||per FLOOR" },
			{ "how many steps", "fact:move.step|COUNT|||" },
			{ "steps per run", "fact:move.step|COUNT||||per RUN" },
			{ "how many floors have i visited", "floors|COUNT|||" },
			{ "average turns spent per floor visit", "floors|AVERAGE|floor.turns||" },
			{ "total healing", "heals|TOTAL|heal.amount||" },
	};

	//asked after the hundred were passing, before any change for them: 9 of these 15 were right (Nov 23). Kept so they stay right.
	static final String[][] LATER = {
			{ "how many times have i died as a rogue", "deaths|COUNT|||" },
			{ "what floor do i die on most", "deaths|COUNT||death.depth|" },
			{ "my highest score ever", "runs|HIGHEST|result.score||" },
			{ "how often do I lose", "runs|COUNT||result.outcome|" },
			{ "who kills me most as mage", "deaths|COUNT||death.cause|" },
			{ "total kills", "kills|COUNT|||" },
			{ "average kills per run", "kills|COUNT||||per RUN" },
			{ "which class has the best average score", "runs|AVERAGE|result.score|run.heroClass|" },
			{ "how many gnolls have i killed", "kills|COUNT|||kill.victim=gnoll" },
			{ "most damage taken in a run", "runs|HIGHEST|combat.damageTaken||" },
			{ "how many items have i picked up", "items|COUNT|||" },
			{ "what is my average level", "runs|AVERAGE|result.heroLevel||" },
			{ "deaths per run", "deaths|COUNT||||per RUN" },
			{ "how many heals", "heals|COUNT|||" },
			{ "average depth", "runs|AVERAGE|result.deepest||" },
	};

	@Test
	public void laterQuestionsStayRight(){
		List<String> wrong = new ArrayList<>();
		for (String[] c : LATER){
			NaturalQuestions.Result r = NaturalQuestions.interpret(cat, c[0], Question.about(Datasets.RUNS));
			String got = r == null ? "null" : describe(r.question);
			if (!got.equals(c[1])) wrong.add("\"" + c[0] + "\": wanted " + c[1] + " got " + got);
		}
		assertTrue(wrong.size() + " wrong:\n" + String.join("\n", wrong), wrong.isEmpty());
	}

	@Test
	public void aNameTheTableCantUseIsSaidNotDropped(){
		NaturalQuestions.Result r = NaturalQuestions.interpret(cat, "how many times have i died as a rogue", Question.about(Datasets.RUNS));
		assertEquals("Left out (this table doesn't record it): rogue", r.leftOutText());
		assertEquals("", NaturalQuestions.interpret(cat, "how many runs as a mage", Question.about(Datasets.RUNS)).leftOutText());
	}

	@Test
	public void aHundredEverydayQuestions(){
		assertEquals(100, CASES.length);
		List<String> wrong = new ArrayList<>();
		for (String[] c : CASES){
			NaturalQuestions.Result r = NaturalQuestions.interpret(cat, c[0], Question.about(Datasets.RUNS));
			String got = r == null ? "null" : describe(r.question);
			if (!got.equals(c[1])) wrong.add("\"" + c[0] + "\": wanted " + c[1] + " got " + got);
		}
		assertTrue(wrong.size() + " of 100 wrong:\n" + String.join("\n", wrong), wrong.isEmpty());
	}

	@Test
	public void typosAreReadAndSaidSo(){
		NaturalQuestions.Result r = NaturalQuestions.interpret(cat, "averge score by clas", Question.about(Datasets.RUNS));
		assertNotNull(r);
		assertTrue(r.didYouMean(), r.didYouMean().contains("averge -> average"));
		assertTrue(r.didYouMean(), r.didYouMean().contains("clas -> class"));
	}

	@Test
	public void nonsenseIsNotGuessed(){
		assertNull(NaturalQuestions.interpret(cat, "bananas", Question.about(Datasets.RUNS)));
		assertNull(NaturalQuestions.interpret(cat, "", Question.about(Datasets.RUNS)));
		assertNull(NaturalQuestions.interpret(null, "how many runs", Question.about(Datasets.RUNS)));
	}

	@Test
	public void everyAnswerIsAnOrdinaryAnswerableQuestion(){
		for (String[] c : CASES){
			NaturalQuestions.Result r = NaturalQuestions.interpret(cat, c[0], Question.about(Datasets.RUNS));
			if (r == null) continue;
			Questions.Answer a = Questions.answer(cat, r.question, "rules");
			assertNotNull(c[0], a);
		}
	}
}
