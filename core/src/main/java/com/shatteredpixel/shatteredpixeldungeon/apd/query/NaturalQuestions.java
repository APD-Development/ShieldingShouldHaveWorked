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

package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

//Questions the way people ask them (UIMSD deep dive item 3, approved Nov 23): "what kills me the most", "my best score as a
//mage", "how many times did I die on floor 5", "averge score by class". UimsdTopicSearch.interpret only took exact field labels
//("how many runs by class"); a probe of 25 everyday questions found 2 it understood.
//
//This is a fixed, offline set of rules, not a language model. It knows the common question shapes, synonyms for the measures,
//hero class and enemy names as filters, floor numbers, and fixes typos against the words UIMSD actually uses (and says so: "did
//you mean"). It only ever builds an ordinary Question from public fields, so the answer is the same table the builder would make,
//and anything it can't place it leaves out rather than guess (the readback shows exactly what was understood).
public final class NaturalQuestions {

	private NaturalQuestions(){}

	public static final class Result {
		public final Question question;
		//"averge -> average": every word that was read as another
		public final List<String> corrections;
		//names said that this table can't be narrowed by ("as a rogue" of deaths, which don't record the class): said, not dropped silently
		public final List<String> leftOut;

		Result( Question question, List<String> corrections, List<String> leftOut ){
			this.question = question;
			this.corrections = Collections.unmodifiableList(corrections);
			this.leftOut = Collections.unmodifiableList(leftOut);
		}

		public String leftOutText(){
			if (leftOut.isEmpty()) return "";
			StringBuilder b = new StringBuilder("Left out (this table doesn't record it): ");
			for (int i = 0; i < leftOut.size(); i++) b.append(i == 0 ? "" : ", ").append(leftOut.get(i));
			return b.toString();
		}

		public String didYouMean(){
			if (corrections.isEmpty()) return "";
			StringBuilder b = new StringBuilder("Read ");
			for (int i = 0; i < corrections.size(); i++) b.append(i == 0 ? "" : ", ").append(corrections.get(i));
			return b.toString();
		}
	}

	private static final Set<String> FILLER = new HashSet<>(Arrays.asList(
			"what", "whats", "is", "are", "was", "were", "the", "a", "an", "my", "me", "i", "im", "ive", "do", "did", "does", "have", "has",
			"had", "to", "show", "tell", "give", "list", "please", "ever", "so", "far", "all", "time", "there", "been", "get", "got", "you",
			"your", "it", "its", "of", "in", "with", "on", "at", "for", "that", "this", "which", "who", "when", "where", "and", "or", "as",
			"by", "per", "each", "every", "times", "often", "much", "many", "how", "number", "count", "total", "average", "mean", "avg",
			"most", "least", "highest", "lowest", "best", "worst", "max", "min", "maximum", "minimum", "median", "typical", "biggest",
			"smallest", "fewest", "top", "usually", "normally", "overall", "am", "be", "can", "could", "would", "should", "will", "from",
			"than", "more", "less", "about", "into", "playing", "played", "play", "using", "used", "use", "while", "during"));

	//whether the question being read says "damage" (so "to me" can mean "to the hero")
	private static final ThreadLocal<Boolean> DAMAGE_SAID = new ThreadLocal<Boolean>(){
		@Override protected Boolean initialValue(){ return false; }
	};

	private static final String[] CLASSES = { "warrior", "mage", "rogue", "huntress", "duelist", "cleric" };

	public static Result interpret( Catalog cat, String words, Question previous ){
		try {
			return build(cat, words, previous == null ? Question.about(Datasets.RUNS) : previous);
		} catch (RuntimeException e){
			return null;
		}
	}

	//---- reading the words ----

	private static final class Words {
		final List<String> t = new ArrayList<>();
		final List<String> corrections = new ArrayList<>();

		boolean has( String w ){
			return t.contains(w);
		}

		boolean hasAny( String... ws ){
			for (String w : ws) if (t.contains(w)) return true;
			return false;
		}

		boolean phrase( String p ){
			return (" " + join(t) + " ").contains(" " + p + " ");
		}

		int index( String w ){
			return t.indexOf(w);
		}
	}

	private static String join( List<String> l ){
		StringBuilder b = new StringBuilder();
		for (String s : l) b.append(b.length() == 0 ? "" : " ").append(s);
		return b.toString();
	}

	static String singular( String w ){
		if (w.length() > 4 && w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";
		if (w.length() > 3 && w.endsWith("es") && (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses"))) return w.substring(0, w.length() - 2);
		if (w.length() > 3 && w.endsWith("s") && !w.endsWith("ss")) return w.substring(0, w.length() - 1);
		return w;
	}

	private static List<String> tokens( String text ){
		List<String> out = new ArrayList<>();
		for (String s : UimsdSearch.normalize(text).split(" ")) if (!s.isEmpty()) out.add(s);
		return out;
	}

	//words that mean something to UIMSD: names of what it records, its field labels, the measures, classes and enemies
	private static Set<String> vocabulary( Catalog cat, List<Dataset> askable, List<String> enemyNames ){
		Set<String> v = new HashSet<>(FILLER);
		v.addAll(Arrays.asList(CLASSES));
		v.addAll(Arrays.asList("kill", "kills", "killed", "die", "died", "dies", "dying", "death", "deaths", "win", "won", "wins", "winning",
				"victory", "victories", "lose", "lost", "losses", "rate", "score", "scores", "floor", "floors", "depth", "deepest", "deep",
				"longest", "shortest", "damage", "hit", "hits", "class", "classes", "hero", "turn", "turns", "hour", "hours", "run", "runs",
				"enemy", "enemies", "monster", "monsters", "item", "items", "level", "levels", "taken", "dealt", "took", "heal", "healed",
				"healing", "gold", "spell", "spells", "cast", "step", "steps", "boss", "bosses", "challenge", "challenges", "difficulty",
				"chest", "chests", "tomb", "tombs", "shop", "mimic", "statue", "quest", "crafted", "skeleton", "remains", "outcome", "end",
				"ended", "kind", "cause", "spent", "found", "reached", "strength", "xp", "experience", "times", "rate", "often", "usually"));
		for (Dataset d : askable){
			v.addAll(tokens(d.def.title));
			v.addAll(tokens(d.def.countLabel));
			v.addAll(tokens(d.def.unitSingular()));
			for (String a : UimsdTerminology.standard().datasetAliases(d.def.id)) v.addAll(tokens(a));
			for (ColumnDef c : d.def.columns()) if (!c.isInternal()) v.addAll(tokens(c.label));
		}
		for (String n : enemyNames) v.addAll(tokens(n));
		return v;
	}

	static int distance( String a, String b ){
		int[] prev = new int[b.length() + 1], cur = new int[b.length() + 1];
		for (int j = 0; j <= b.length(); j++) prev[j] = j;
		for (int i = 1; i <= a.length(); i++){
			cur[0] = i;
			for (int j = 1; j <= b.length(); j++){
				int sub = prev[j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
				cur[j] = Math.min(sub, Math.min(prev[j] + 1, cur[j - 1] + 1));
				if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1))
					cur[j] = Math.min(cur[j], prev[j - 1]);   //swapped letters count once (a fair approximation of a transposition)
			}
			int[] t = prev; prev = cur; cur = t;
		}
		return prev[b.length()];
	}

	private static Words read( String text, Set<String> vocab ){
		Words w = new Words();
		for (String raw : tokens(text)){
			String s = raw;
			if (s.equals("'s")) continue;
			if (!vocab.contains(s) && !vocab.contains(singular(s)) && s.length() >= 4 && !isNumber(s)){
				String best = null;
				int bestD = Integer.MAX_VALUE;
				boolean tie = false;
				int allowed = s.length() >= 8 ? 2 : 1;
				for (String v : vocab){
					if (Math.abs(v.length() - s.length()) > allowed || v.length() < 3) continue;
					int d = distance(s, v);
					if (d > allowed) continue;
					if (d < bestD){ bestD = d; best = v; tie = false; }
					else if (d == bestD && !singular(v).equals(singular(best))) tie = true;
				}
				if (best != null && !tie){
					w.corrections.add(raw + " -> " + best);
					s = best;
				}
			}
			w.t.add(s);
		}
		return w;
	}

	private static boolean isNumber( String s ){
		if (s.isEmpty() || s.length() > 6) return false;
		for (int i = 0; i < s.length(); i++) if (s.charAt(i) < '0' || s.charAt(i) > '9') return false;
		return true;
	}

	//---- building the question ----

	private static Result build( Catalog cat, String text, Question previous ){
		if (cat == null || text == null || text.trim().isEmpty()) return null;
		List<Dataset> askable = Questions.askable(cat);
		Map<String, Dataset> byId = new LinkedHashMap<>();
		for (Dataset d : askable) byId.put(d.def.id, d);
		List<String> enemies = enemyNames(byId.get(Datasets.ENEMIES));
		Words w = read(text, vocabulary(cat, askable, enemies));
		if (w.t.isEmpty()) return null;
		//"per run", "each floor": the unit divides the answer, it does not name the table
		Words naming = withoutUnits(w);
		DAMAGE_SAID.set(w.has("damage"));
		Set<String> named = named(naming, byId);
		String shaped = shape(w, byId);
		if (shaped != null) named.add(shaped);
		//an enemy named by name ("the gnoll") names the enemies table
		String said = " " + join(w.t) + " ";
		for (String n : enemies){
			String nn = UimsdSearch.normalize(n);
			if (!nn.isEmpty() && (said.contains(" " + nn + " ") || said.contains(" " + nn + "s "))) named.add(Datasets.ENEMIES);
		}

		boolean most = w.has("most") && !w.phrase("most often");
		Question.Measure measure = measure(w);
		boolean numeric = measure != Question.Measure.COUNT || most;
		String dataset = null, column = null;

		//"total damage taken", "damage to hero by enemy": a field named in full says which table it is in
		String[] full = numeric ? fullLabel(naming, byId, named) : null;
		if (full != null){
			dataset = full[0];
			column = full[1];
		} else {
			dataset = shaped;
			if (dataset == null) dataset = datasetFor(naming, byId);
			if (numeric){
				if (dataset != null) column = bestColumn(naming, byId.get(dataset).def);
				else {
					String[] dc = null;
					int bestScore = 0;
					for (Dataset ds : byId.values()){
						String c = bestColumn(naming, ds.def);
						if (c == null) continue;
						int score = labelScore(naming, ds.def.column(c), ds.def.id) * 10 + rank(ds.def.id);
						if (score > bestScore){ bestScore = score; dc = new String[]{ ds.def.id, c }; }
					}
					if (dc != null){ dataset = dc[0]; column = dc[1]; }
				}
				if (column == null && dataset != null) column = defaultMeasured(w, byId.get(dataset).def);
			}
		}
		//"what floor do I die on most", "what kills me the most": most of a breakdown is its biggest group, not a highest number
		if (most && dataset != null && measure == Question.Measure.COUNT && impliedGroup(w, byId.get(dataset).def) != null) column = null;
		if (most && measure == Question.Measure.COUNT && column != null) measure = Question.Measure.HIGHEST;
		//"total kills" is how many; "average kills per run" is how many per run
		if (column == null && dataset != null && (measure == Question.Measure.TOTAL
				|| (measure == Question.Measure.AVERAGE && per(w, Question.Measure.COUNT, dataset) != null))) measure = Question.Measure.COUNT;
		if (measure != Question.Measure.COUNT && column == null) return null;
		if (most && column == null) measure = Question.Measure.COUNT;
		//"which class do I play most": a breakdown with nothing else named is a breakdown of runs
		if (dataset == null && byId.containsKey(Datasets.RUNS) && groupColumn(w, byId.get(Datasets.RUNS).def, null) != null) dataset = Datasets.RUNS;
		if (dataset == null) return null;
		DatasetDef def = byId.get(dataset).def;

		List<Filter> filters = filters(w, def, enemies, null);
		boolean nameFilter = false;
		for (Filter f : filters) if (f.column.equals("death.cause") || f.column.equals("kill.victim") || f.column.equals("enemy.name")) nameFilter = true;
		String group = nameFilter ? null : impliedGroup(w, def);
		if (group == null) group = groupColumn(w, def, column);
		if (group != null){
			List<Filter> kept = new ArrayList<>();
			for (Filter f : filters) if (!f.column.equals(group)) kept.add(f);
			filters = kept;
		}

		Question q = Question.about(def.id).withScope(previous.scope);
		if (column != null) q = q.withMeasure(measure, column);
		if (group != null && !group.equals(column)) q = q.withGroupBy(Collections.singletonList(group));
		PerUnit.Unit per = per(w, measure, def.id);
		if (per != null) q = q.withPer(per);
		for (Filter f : filters) q = q.addFilter(f);
		return new Result(q, w.corrections, leftOut(w, def, q));
	}

	private static boolean unit( String word ){
		String u = singular(word);
		return u.equals("run") || u.equals("floor") || u.equals("turn") || u.equals("hour");
	}

	private static Words withoutUnits( Words w ){
		Words out = new Words();
		for (int i = 0; i < w.t.size(); i++){
			String t = w.t.get(i);
			if (i > 0 && unit(t)){
				String before = w.t.get(i - 1);
				if (before.equals("per") || before.equals("each") || before.equals("every") || before.equals("a") || before.equals("an")) continue;
			}
			out.t.add(t);
		}
		return out;
	}

	//every main word of a field's label said (and at least two of them): the strongest sign of what is asked
	private static String[] fullLabel( Words w, Map<String, Dataset> d, Set<String> named ){
		String[] best = null;
		int bestScore = 0;
		for (Dataset ds : d.values()){
			//only runs answer without being named: "average hero level" is a run's level, not the level at a death
			if (!ds.def.id.equals(Datasets.RUNS) && !named.contains(ds.def.id)) continue;
			for (ColumnDef c : Questions.measurable(ds.def, false)){
				if (!plain(w, c)) continue;
				List<String> label = mainWords(c.label);
				if (label.size() < 2) continue;
				boolean all = true;
				for (String l : label) if (!said(w, l, c.label)) { all = false; break; }
				if (!all) continue;
				int score = label.size() * 10 + rank(ds.def.id);
				if (score > bestScore){ bestScore = score; best = new String[]{ ds.def.id, c.id }; }
			}
		}
		return best;
	}

	//old imported-record fields and custom-rule settings are only meant when asked for by name
	private static boolean plain( Words w, ColumnDef c ){
		if (c.id.startsWith("legacy.")) return w.hasAny("legacy", "imported", "old");
		if (c.id.startsWith("custom.")) return w.hasAny("custom", "rules", "rule", "setting", "settings");
		return true;
	}

	//the tables the words name (title, what it counts, alias), whole titles for details tables
	private static Set<String> named( Words w, Map<String, Dataset> d ){
		Set<String> out = new HashSet<>();
		for (Dataset ds : d.values()) if (nameScore(w, ds) > 0) out.add(ds.def.id);
		return out;
	}

	private static List<String> mainWords( String label ){
		List<String> l = tokens(label);
		l.removeAll(FILLER);
		return l;
	}

	private static boolean said( Words w, String labelWord, String label ){
		for (String t : w.t) if (singular(t).equals(singular(labelWord)) || synonym(t, labelWord, label)) return true;
		return false;
	}

	private static String bestColumn( Words w, DatasetDef def ){
		String best = null;
		int bestScore = 0;
		for (ColumnDef c : Questions.measurable(def, false)){
			if (!plain(w, c)) continue;
			int score = labelScore(w, c, def.id);
			if (score > bestScore){ bestScore = score; best = c.id; }
		}
		return best;
	}

	private static String[] columnIn( Words w, Dataset ds ){
		String c = bestColumn(w, ds.def);
		return c == null ? null : new String[]{ ds.def.id, c };
	}

	private static Question.Measure measure( Words w ){
		if (w.hasAny("average", "avg", "mean", "averages") || w.phrase("on average")) return Question.Measure.AVERAGE;
		if (w.hasAny("median", "typical", "usually", "normally")) return Question.Measure.TYPICAL;
		if (w.hasAny("highest", "high", "best", "max", "maximum", "biggest", "top", "deepest", "longest", "record") && !w.phrase("most often"))
			return Question.Measure.HIGHEST;
		if (w.hasAny("lowest", "low", "worst", "min", "minimum", "smallest", "shortest")) return Question.Measure.LOWEST;
		if (w.has("total") || w.phrase("how much") || w.phrase("in total") || w.phrase("altogether")) return Question.Measure.TOTAL;
		return Question.Measure.COUNT;
	}

	private static boolean killedMe( Words w ){
		return w.phrase("killed me") || w.phrase("kills me") || w.phrase("kill me") || w.phrase("killing me") || w.phrase("killed by")
				|| w.phrase("died to") || w.phrase("die to") || w.phrase("dies to");
	}

	//common shapes that name their table by meaning, not by its title
	private static String shape( Words w, Map<String, Dataset> d ){
		boolean dying = killedMe(w) || w.hasAny("die", "died", "dies", "dying", "death", "deaths");
		if (dying && d.containsKey(Datasets.DEATHS) && !w.hasAny("score", "gold", "turns")) return Datasets.DEATHS;
		if (w.hasAny("deepest", "deep") && d.containsKey(Datasets.RUNS)) return Datasets.RUNS;
		if (w.hasAny("kill", "kills", "killed") && d.containsKey(EventDatasets.KILLS) && !killedMe(w)) return EventDatasets.KILLS;
		if ((w.phrase("win rate") || w.hasAny("win", "won", "wins", "winning", "victory", "victories", "lose", "lost", "losses"))
				&& d.containsKey(Datasets.RUNS) && !w.hasAny("score", "turns", "level", "gold", "floor")) return Datasets.RUNS;
		return null;
	}

	private static String impliedGroup( Words w, DatasetDef def ){
		if (def.id.equals(Datasets.DEATHS)){
			if (killedMe(w) || (w.has("what") && w.hasAny("kill", "kills", "killed")))
				return col(def, "death.cause");
			if (w.has("where") || w.phrase("which floor") || w.phrase("what floor")) return col(def, "death.depth");
		}
		if (def.id.equals(Datasets.RUNS) && (w.phrase("win rate") || (w.has("often") && w.hasAny("win", "won", "lose", "lost"))
				|| w.phrase("wins and losses") || w.has("outcome") || w.hasAny("end", "ended")))
			return col(def, "result.outcome");
		if (def.id.equals(EventDatasets.KILLS) && (w.phrase("what did i kill") || w.phrase("which enemies") || w.phrase("what enemies")
				|| w.phrase("what i killed") || w.phrase("which monsters")))
			return col(def, "kill.victim");
		return null;
	}

	private static String col( DatasetDef def, String id ){
		ColumnDef c = def.column(id);
		return c == null || c.isInternal() ? null : id;
	}

	//the table the words name: its title, what it counts, one of its rows, or a registered alias
	private static String datasetFor( Words w, Map<String, Dataset> d ){
		String best = null;
		int bestScore = 0;
		for (Dataset ds : d.values()){
			int score = nameScore(w, ds);
			if (score == 0) continue;
			score += rank(ds.def.id);
			if (score > bestScore){
				bestScore = score;
				best = ds.def.id;
			}
		}
		return best;
	}

	private static int nameScore( Words w, Dataset ds ){
		int score = 0;
		Set<String> names = new HashSet<>();
		for (String s : tokens(ds.def.title)) names.add(singular(s));
		for (String s : tokens(ds.def.countLabel)) names.add(singular(s));
		for (String s : tokens(ds.def.unitSingular())) names.add(singular(s));
		for (String a : UimsdTerminology.standard().datasetAliases(ds.def.id)) for (String s : tokens(a)) names.add(singular(s));
		names.removeAll(FILLER);
		//"level" on its own is the hero's level, not a floor
		if (ds.def.id.equals(EventDatasets.FLOORS)) names.remove("level");
		for (String t : w.t) if (names.contains(singular(t))) score += 10;
		if (score == 0) return 0;
		//a details table ("Gold changes", "Spell casts") only when its whole title is said: "most gold" is a run's gold
		if (FactDatasets.isFactDataset(ds.def.id)){
			for (String tw : mainWords(ds.def.title)){
				boolean f = false;
				for (String t : w.t) if (singular(t).equals(singular(tw))) f = true;
				if (!f) return 0;
			}
		}
		return score;
	}

	//when words fit several tables equally, the everyday ones win
	private static int rank( String id ){
		if (id.equals(Datasets.RUNS)) return 5;
		if (id.equals(Datasets.DEATHS)) return 4;
		if (id.equals(EventDatasets.KILLS)) return 4;
		if (id.equals(Datasets.ENEMIES) || id.equals(Datasets.ITEMS)) return 3;
		if (EventDatasets.isEventDataset(id)) return 2;
		return 0;
	}

	//how well the words name a column: words of its label (and aliases) found, minus words of the label not asked for
	private static int labelScore( Words w, ColumnDef c, String dataset ){
		List<String> label = new ArrayList<>(tokens(c.label));
		for (String a : UimsdTerminology.standard().columnAliases(dataset, c.id)) if (w.phrase(UimsdSearch.normalize(a))) return 6;
		label.removeAll(FILLER);
		if (label.isEmpty()) return 0;
		int found = 0;
		for (String l : label){
			boolean f = false;
			for (String t : w.t) if (singular(t).equals(singular(l)) || synonym(t, l, c.label)) f = true;
			if (f) found++;
		}
		if (found == 0) return 0;
		return found * 2 - (label.size() - found);
	}

	private static boolean synonym( String said, String label, String fullLabel ){
		String[][] same = { {"deep", "deepest"}, {"depth", "deepest"}, {"took", "taken"}, {"received", "taken"}, {"deal", "dealt"}, {"longest", "turns"},
				{"shortest", "turns"}, {"long", "turns"}, {"experience", "xp"}, {"exp", "xp"}, {"healing", "healed"}, {"heal", "healed"},
				{"points", "score"}, {"health", "hp"} };
		for (String[] s : same) if (s[0].equals(said) && s[1].equals(label)) return true;
		//"the most damage to me" is damage to the hero; "kills me" is never the hero's kills
		if ((said.equals("me") || said.equals("i")) && label.equals("hero") && fullLabel.toLowerCase(Locale.ROOT).contains("damage")
				&& DAMAGE_SAID.get()) return true;
		return false;
	}

	//"my best run" with no number named: runs are measured by score
	private static String defaultMeasured( Words w, DatasetDef def ){
		if (def.id.equals(Datasets.RUNS)){
			if (w.hasAny("deepest", "deep")) return col(def, "result.deepest");
			if (w.hasAny("longest", "shortest")) return col(def, "result.turns");
			return col(def, "result.score");
		}
		return null;
	}

	//"by class", "for each enemy", "which floor": a column to break the answer down by
	private static String groupColumn( Words w, DatasetDef def, String measured ){
		for (int at = 0; at < w.t.size(); at++){
			String m = w.t.get(at);
			boolean which = m.equals("which") || m.equals("what");
			if (!which && !m.equals("by") && !m.equals("per") && !m.equals("each")) continue;
			List<String> after = new ArrayList<>();
			for (String t : w.t.subList(at + 1, w.t.size()))
				if (!FILLER.contains(t) || t.equals("how") || t.equals("got") || t.equals("get") || t.equals("where")) after.add(t);
			if (after.isEmpty()) continue;
			if ((m.equals("per") || m.equals("each")) && unit(after.get(0))) continue;
			//"which class", "what floor": only the very next word names the breakdown
			if (which){
				if (at + 1 >= w.t.size()) continue;
				after = new ArrayList<>();
				after.add(w.t.get(at + 1));
			}
			String c = groupFor(after, def, measured);
			if (c != null) return c;
		}
		return null;
	}

	private static String groupFor( List<String> after, DatasetDef def, String measured ){
		String alias = groupAlias(after, def);
		if (alias != null && !alias.equals(measured)) return alias;
		String best = null;
		int bestScore = 0;
		for (ColumnDef c : Questions.groupable(def, false)){
			if (c.id.equals(measured) || c.id.startsWith("custom.") || c.id.startsWith("legacy.")) continue;
			List<String> label = mainWords(c.label);
			if (label.isEmpty()) continue;
			int found = 0;
			for (String l : label) for (String t : after) if (singular(t).equals(singular(l))) { found++; break; }
			if (found == 0) continue;
			int score = found * 4 - (label.size() - found) + (singular(after.get(0)).equals(singular(label.get(0))) ? 2 : 0);
			if (score > bestScore){
				bestScore = score;
				best = c.id;
			}
		}
		return best;
	}

	//everyday words for a breakdown that the field labels don't use
	private static String groupAlias( List<String> after, DatasetDef def ){
		String first = singular(after.get(0));
		String id = def.id;
		if (first.equals("class") || first.equals("hero") || first.equals("character"))
			return id.equals(Datasets.RUNS) ? col(def, "run.heroClass") : prefixed(def, ".runClass");
		if (first.equals("enemy") || first.equals("monster") || first.equals("cause") || first.equals("killer") || first.equals("mob")){
			if (id.equals(Datasets.DEATHS)) return col(def, "death.cause");
			if (id.equals(EventDatasets.KILLS)) return col(def, "kill.victim");
			if (id.equals(Datasets.ENEMIES)) return col(def, "enemy.name");
		}
		if (first.equals("floor") || first.equals("level") || first.equals("depth")){
			if (id.equals(Datasets.DEATHS)) return col(def, "death.depth");
			if (id.equals(EventDatasets.KILLS)) return col(def, "kill.depth");
		}
		if (id.equals(Datasets.ITEMS)){
			if (after.contains("got") || after.contains("get") || after.contains("acquired")) return col(def, "item.acquiredHow");
			if (first.equals("where") || after.contains("found") || first.equals("source")) return col(def, "item.foundSource");
		}
		if (id.equals(Datasets.RUNS) && (first.equals("outcome") || first.equals("result"))) return col(def, "result.outcome");
		return null;
	}

	private static boolean groupSynonym( String said, String label, String column ){
		if ((said.equals("hero") || said.equals("character")) && label.equals("class")) return true;
		if ((said.equals("monster") || said.equals("enemy") || said.equals("monsters") || said.equals("enemies"))
				&& (label.equals("enemy") || label.equals("victim") || column.equals("death.cause"))) return true;
		if ((said.equals("level") || said.equals("depth")) && label.equals("floor")) return true;
		if (said.equals("cause") && column.equals("death.cause")) return true;
		return false;
	}

	private static PerUnit.Unit per( Words w, Question.Measure m, String dataset ){
		if (m != Question.Measure.COUNT && m != Question.Measure.TOTAL) return null;
		if (dataset.equals(Datasets.RUNS)) return null;
		if (w.phrase("per run") || w.phrase("each run") || w.phrase("a run") || w.phrase("every run")) return PerUnit.Unit.RUN;
		if (w.phrase("per floor") || w.phrase("each floor") || w.phrase("a floor") || w.phrase("every floor")) return PerUnit.Unit.FLOOR;
		if (w.phrase("per turn") || w.phrase("a turn") || w.phrase("each turn")) return PerUnit.Unit.TURN;
		if (w.phrase("per hour") || w.phrase("an hour") || w.phrase("each hour")) return PerUnit.Unit.HOUR;
		return null;
	}

	//names said in the question that narrow it: a hero class, an enemy, a floor number, winning
	private static List<Filter> filters( Words w, DatasetDef def, List<String> enemies, String group ){
		List<Filter> out = new ArrayList<>();
		String classCol = def.id.equals(Datasets.RUNS) ? "run.heroClass" : prefixed(def, ".runClass");
		for (String c : CLASSES){
			if ((w.has(c) || w.has(c + "s")) && classCol != null && def.column(classCol) != null && !classCol.equals(group)){
				out.add(Filter.text(classCol, Op.EQ, c.toUpperCase(Locale.ROOT)));
				break;
			}
		}
		String nameCol = def.id.equals(Datasets.DEATHS) ? "death.cause" : def.id.equals(Datasets.ENEMIES) ? "enemy.name"
				: def.id.equals(EventDatasets.KILLS) ? "kill.victim" : null;
		if (nameCol != null && def.column(nameCol) != null && !nameCol.equals(group)){
			String said = " " + join(w.t) + " ";
			String found = null;
			for (String n : enemies){
				String nn = UimsdSearch.normalize(n);
				if (nn.isEmpty()) continue;
				if ((said.contains(" " + nn + " ") || said.contains(" " + nn + "s ")) && (found == null || nn.length() > found.length())) found = nn;
			}
			if (found != null){
				for (String n : enemies) if (UimsdSearch.normalize(n).equals(found)){ out.add(Filter.text(nameCol, Op.EQ, n)); break; }
			}
		}
		int fi = w.index("floor");
		if (fi < 0) fi = w.index("depth");
		if (fi >= 0 && fi + 1 < w.t.size() && isNumber(w.t.get(fi + 1))){
			String depthCol = def.id.equals(Datasets.DEATHS) ? "death.depth" : def.id.equals(EventDatasets.KILLS) ? "kill.depth"
					: FactDatasets.isFactDataset(def.id) ? FactDatasets.DEPTH : null;
			if (depthCol != null && def.column(depthCol) != null && !depthCol.equals(group))
				out.add(Filter.num(depthCol, Op.EQ, Long.parseLong(w.t.get(fi + 1))));
		}
		if (def.id.equals(Datasets.ITEMS) && def.column("item.foundSource") != null && !"item.foundSource".equals(group)){
			String[][] places = { {"chest", "Chest"}, {"tomb", "Tomb"}, {"skeleton", "Skeleton"}, {"remains", "Remains"}, {"shop", "Shop"},
					{"mimic", "Mimic"}, {"statue", "Statue"}, {"quest", "Quest reward"}, {"crafted", "Crafted"} };
			for (String[] p : places) if (w.has(p[0]) || w.has(p[0] + "s")){ out.add(Filter.text("item.foundSource", Op.EQ, p[1])); break; }
		}
		boolean won = w.hasAny("won", "wins", "victory", "victories", "winning") && !w.phrase("win rate") && !w.has("often");
		String outcomeCol = def.id.equals(Datasets.RUNS) ? "result.outcome" : prefixed(def, ".runOutcome");
		if (won && outcomeCol != null && def.column(outcomeCol) != null && !outcomeCol.equals(group))
			out.add(Filter.num(outcomeCol, Op.EQ, 1));
		return out;
	}

	private static List<String> leftOut( Words w, DatasetDef def, Question q ){
		List<String> out = new ArrayList<>();
		boolean classFiltered = false;
		for (Filter f : q.filters) if (f.column.endsWith("heroClass") || f.column.endsWith(".runClass")) classFiltered = true;
		boolean classGrouped = !q.groupBy.isEmpty() && (q.groupBy.get(0).endsWith("heroClass") || q.groupBy.get(0).endsWith(".runClass"));
		if (!classFiltered && !classGrouped) for (String c : CLASSES) if (w.has(c) || w.has(c + "s")) out.add(c);
		return out;
	}

	private static String prefixed( DatasetDef def, String suffix ){
		for (ColumnDef c : def.columns()) if (c.id.endsWith(suffix) && !c.isInternal()) return c.id;
		return null;
	}

	private static List<String> enemyNames( Dataset enemies ){
		if (enemies == null) return Collections.emptyList();
		int i = enemies.def.indexOf("enemy.name");
		if (i < 0) return Collections.emptyList();
		Set<String> names = new LinkedHashSet<>();
		for (Row r : enemies.rows) if (r.publicRow && r.isKnown(i)) names.add(r.asText(i));
		return new ArrayList<>(names);
	}
}
