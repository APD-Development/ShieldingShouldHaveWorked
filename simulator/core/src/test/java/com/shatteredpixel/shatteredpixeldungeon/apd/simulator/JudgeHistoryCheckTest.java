package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ApdEvent;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.CombatEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.EventTypes;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

//Checks the fight judge against real recorded fights. Reads an unpacked APD history export (read only; point at it with
//-Papd.history.dir=...) and, for every recorded weapon swing, compares what the judge would have predicted with what
//happened: how often each side hits, how hard monsters hit before armor, and how much monster armor takes off.
//What it cannot know from a log is said in the output: the hero's weapon accuracy, armor, rings, talents and surprise
//attacks are not rebuilt, so the hero's side uses level alone.
//Prints a report; fails only if a history folder was given and could not be read.
public class JudgeHistoryCheckTest {

	private static final class Row {
		long n, hits;
		double predictedHits;      //sum of predicted hit chance
		long hitsWithDamage;
		double rolled, predictedRolled;
		double armor, predictedArmor;
	}

	private static final Pattern RULES = Pattern.compile("1(,\\d+){18}");

	@Test
	public void compareJudgeWithRecordedFights() throws Exception {
		String dir = System.getProperty("apd.history.dir", "");
		if (dir.isEmpty()){
			System.out.println("APD_JUDGE_CHECK skipped (no -Papd.history.dir)");
			return;
		}
		HeadlessTestSupport.start();
		SPDSettings.intro(false);
		Map<String, Row> mobHitsHero = new TreeMap<>(), heroHitsMob = new TreeMap<>();
		Map<String, Row> byRules = new TreeMap<>();
		Map<String, Mob> copies = new HashMap<>();
		int logs = 0, customLogs = 0;
		long swings = 0, skipped = 0;
		List<File> files = new ArrayList<>();
		File[] fs = new File(dir, "runs").listFiles();
		if (fs != null) files.addAll(Arrays.asList(fs));
		files.sort((a, b) -> a.getName().compareTo(b.getName()));
		for (File f : files){
			ReadResult r;
			try { r = RunLogReader.read(f); } catch (Throwable t){ continue; }
			logs++;
			//the run's rule settings, when it was played on custom rules: percent multipliers in a fixed order
			double enemyDamage = 1, enemyDefense = 1, enemyAccuracy = 1, enemyEvasion = 1, bossDamage = 1, playerAccuracy = 1, playerEvasion = 1;
			boolean custom = false;
			for (String s : r.strings){
				if (s != null && RULES.matcher(s).matches()){
					String[] v = s.split(",");
					enemyDamage = Integer.parseInt(v[2]) / 100.0;
					enemyDefense = Integer.parseInt(v[3]) / 100.0;
					enemyAccuracy = Integer.parseInt(v[4]) / 100.0;
					enemyEvasion = Integer.parseInt(v[5]) / 100.0;
					bossDamage = Integer.parseInt(v[8]) / 100.0;
					playerAccuracy = Integer.parseInt(v[11]) / 100.0;
					playerEvasion = Integer.parseInt(v[12]) / 100.0;
					custom = enemyDamage != 1 || enemyDefense != 1 || enemyAccuracy != 1 || enemyEvasion != 1 || playerAccuracy != 1 || playerEvasion != 1 || bossDamage != 1;
					break;
				}
			}
			if (custom) customLogs++;
			int heroLevel = 1;
			for (ApdEvent e : r.events){
				try {
					if (e.type == EventTypes.LEVEL_UP){
						heroLevel = Math.max(1, CombatEvents.decodeLevelUp(e, r).newLevel);
						continue;
					}
					if (e.type != EventTypes.ATTACK_DETAIL) continue;
					CombatEvents.AttackDetail d = CombatEvents.decodeAttackDetail(e, r);
					if (d.magic) continue;
					boolean heroAttacks = d.attackerClass.endsWith("hero.Hero");
					boolean heroDefends = d.defenderClass.endsWith("hero.Hero");
					if (heroAttacks == heroDefends) continue;
					String mobClass = heroAttacks ? d.defenderClass : d.attackerClass;
					if (!mobClass.contains("actors.mobs.") || mobClass.contains(".npcs.")){ skipped++; continue; }
					Dungeon.depth = Math.max(1, d.where.depth);
					String key = mobClass + "@" + Dungeon.depth;
					Mob m = copies.get(key);
					if (m == null && !copies.containsKey(key)){
						try {
							String cn = mobClass.startsWith("com.") ? mobClass : "com.shatteredpixel.shatteredpixeldungeon." + mobClass;
							m = (Mob) Class.forName(cn).getDeclaredConstructor().newInstance();
						} catch (Throwable t){ m = null; }
						copies.put(key, m);
					}
					if (m == null){ skipped++; continue; }
					FightJudge.Stats st = FightJudge.stats(m);
					boolean boss = Char.hasProp(m, Char.Property.BOSS);
					String name = mobClass.substring(mobClass.lastIndexOf('.') + 1);
					swings++;
					if (heroDefends){
						float mobAccuracy;
						try { mobAccuracy = m.attackSkill(null); } catch (Throwable t){ skipped++; continue; }
						Row row = mobHitsHero.computeIfAbsent(name, k -> new Row());
						Row rr = byRules.computeIfAbsent((custom ? "custom rules" : "standard rules") + ": monster swings at hero", k -> new Row());
						float predicted = FightJudge.hitChance((float) (mobAccuracy * enemyAccuracy), (float) ((5 + heroLevel - 1) * playerEvasion));
						for (Row x : new Row[]{row, rr}){
							x.n++;
							x.predictedHits += predicted;
							if (d.hit){
								x.hits++;
								x.hitsWithDamage++;
								x.rolled += d.rolled;
								x.predictedRolled += st.avgDamage * (boss ? bossDamage : enemyDamage);
							}
						}
					} else {
						Row row = heroHitsMob.computeIfAbsent(name, k -> new Row());
						Row rr = byRules.computeIfAbsent((custom ? "custom rules" : "standard rules") + ": hero swings at monster", k -> new Row());
						float predicted = FightJudge.hitChance((float) ((10 + heroLevel - 1) * playerAccuracy), (float) (m.defenseSkill * enemyEvasion));
						for (Row x : new Row[]{row, rr}){
							x.n++;
							x.predictedHits += predicted;
							if (d.hit){
								x.hits++;
								x.hitsWithDamage++;
								x.armor += d.armorReduction;
								x.predictedArmor += st.avgArmor * enemyDefense;
							}
						}
					}
				} catch (Throwable t){
					skipped++;
				}
			}
		}
		System.out.println("APD_JUDGE_CHECK logs=" + logs + " customRuleLogs=" + customLogs + " swingsCompared=" + swings + " skipped=" + skipped);
		print("OVERALL", byRules, 1);
		print("MONSTER SWINGS AT HERO (hit chance uses hero level only; damage is before armor)", mobHitsHero, 300);
		print("HERO SWINGS AT MONSTER (hit chance uses hero level only: no weapon accuracy, talents or surprise attacks)", heroHitsMob, 300);
		assertTrue("no logs could be read from " + dir, logs > 0);
	}

	private static void print(String title, Map<String, Row> rows, int minSwings){
		System.out.println("APD_JUDGE_CHECK == " + title);
		for (Map.Entry<String, Row> e : rows.entrySet()){
			Row r = e.getValue();
			if (r.n < minSwings) continue;
			StringBuilder b = new StringBuilder(String.format(Locale.ROOT, "APD_JUDGE_CHECK %-44s swings %6d  hit real %5.1f%% judge %5.1f%%",
					e.getKey(), r.n, 100.0 * r.hits / r.n, 100.0 * r.predictedHits / r.n));
			if (r.hitsWithDamage > 0 && r.predictedRolled > 0) b.append(String.format(Locale.ROOT, "  damage real %5.2f judge %5.2f",
					r.rolled / r.hitsWithDamage, r.predictedRolled / r.hitsWithDamage));
			if (r.hitsWithDamage > 0 && r.predictedArmor + r.armor > 0) b.append(String.format(Locale.ROOT, "  armor real %5.2f judge %5.2f",
					r.armor / r.hitsWithDamage, r.predictedArmor / r.hitsWithDamage));
			System.out.println(b);
		}
	}
}
