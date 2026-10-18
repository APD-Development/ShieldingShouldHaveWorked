/*
 * Shattered Pixel Dungeon / ADHD Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 * Licensed under the GNU GPL, version 3 or later.
 */
package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// Offline discovery of *schema*, not arbitrary recorded values.
// A topic result opens the existing Questions builder. Nothing is inferred,
// transmitted, or searched in hidden cells or raw gameplay history.
public final class UimsdTopicSearch {
    private UimsdTopicSearch(){}

    private static final int LIMIT = 48;

    public static final class Hit {
        public final String datasetId;
        public final String columnId; // "" = the dataset itself
        public final String label;
        public final String detail;

        private Hit(String datasetId, String columnId, String label, String detail) {
            this.datasetId = datasetId;
            this.columnId = columnId;
            this.label = label;
            this.detail = detail;
        }

        public Question question(Question previous, DatasetDef definition) {
            Question q = previous.withDataset(datasetId);
            if (columnId.isEmpty()) return q;
            ColumnDef c = definition.column(columnId);
            if (c == null || c.isInternal()) return q;
            if (c.type == ColumnDef.Type.NUM)
                return q.withMeasure(Question.Measure.TOTAL, columnId);
            if (Questions.groupable(definition, false).contains(c))
                return q.withGroupBy(Collections.singletonList(columnId));
            return q;
        }
    }

    public static List<Hit> find(Catalog catalog, String words) {
        if (catalog == null || words == null || words.trim().isEmpty())
            return Collections.emptyList();
        String query = normalize(words);
        if (query.isEmpty()) return Collections.emptyList();
        String[] terms = query.split(" ");
        List<Hit> hits = new ArrayList<>();
        // Match only datasets with recorded rows, just like the existing Ask picker.
        for (Dataset dataset : Questions.askable(catalog)) {
            DatasetDef def = dataset.def;
            if (matches(terms, def.title + " " + def.countLabel + " " + def.description + " " + def.id)) {
                hits.add(new Hit(def.id, "", def.title, "Ask about " + def.countLabel.toLowerCase(Locale.ROOT)));
                if (hits.size() == LIMIT) return hits;
            }
            for (ColumnDef column : def.columns()) {
                // An internal field must not become a public suggestion.
                if (column.isInternal()) continue;
                if (!matches(terms, column.label + " " + column.description + " " + column.id)) continue;
                String action = column.type == ColumnDef.Type.NUM ? "Start with its total" : "Start with a breakdown";
                hits.add(new Hit(def.id, column.id, def.title + " / " + column.label, action));
                if (hits.size() == LIMIT) return hits;
            }
        }
        return hits;
    }


    // A deliberately small, deterministic English grammar. Only exact public schema labels
    // are accepted. Unsupported phrases fail closed, never silently dropping conditions.
    // No recorded values are indexed or searched here.
    public static Question interpret(Catalog catalog, String words, Question previous) {
        if (catalog == null || words == null || previous == null) return null;
        String phrase = normalize(words);
        Question.Measure measure;
        String tail;
        if (phrase.startsWith("how many ")) {
            measure = Question.Measure.COUNT;
            tail = phrase.substring("how many ".length());
        } else {
            String[] prefixes = {"total ", "sum ", "average ", "mean ", "highest ", "lowest ", "median "};
            Question.Measure[] measures = {Question.Measure.TOTAL, Question.Measure.TOTAL,
                    Question.Measure.AVERAGE, Question.Measure.AVERAGE,
                    Question.Measure.HIGHEST, Question.Measure.LOWEST, Question.Measure.TYPICAL};
            int index = -1;
            for (int i = 0; i < prefixes.length; i++) {
                if (phrase.startsWith(prefixes[i])) { index = i; break; }
            }
            if (index < 0) return null;
            measure = measures[index];
            tail = phrase.substring(prefixes[index].length());
        }

        int split = tail.indexOf(" by ");
        if (split >= 0 && tail.indexOf(" by ", split + 4) >= 0) return null;
        String group = split < 0 ? "" : tail.substring(split + 4);
        if (split >= 0) tail = tail.substring(0, split);
        if (tail.isEmpty() || (split >= 0 && group.isEmpty())) return null;

        String measured = "";
        if (measure != Question.Measure.COUNT) {
            int of = tail.indexOf(" of ");
            if (of < 1 || of + 4 >= tail.length() || tail.indexOf(" of ", of + 4) >= 0) return null;
            measured = tail.substring(0, of);
            tail = tail.substring(of + 4);
        }
        Dataset selected = null;
        for (Dataset ds : Questions.askable(catalog)) {
            DatasetDef def = ds.def;
            if (!tail.equals(normalize(def.title))
                    && !tail.equals(normalize(def.countLabel))
                    && !tail.equals(normalize(def.unitSingular()))) continue;
            if (selected != null) return null; // ambiguous topic, ask the player to choose
            selected = ds;
        }
        if (selected == null) return null;
        DatasetDef def = selected.def;
        ColumnDef measuredColumn = null;
        if (measure != Question.Measure.COUNT) {
            for (ColumnDef col : Questions.measurable(def, false)) {
                if (!measured.equals(normalize(col.label))) continue;
                if (measuredColumn != null) return null;
                measuredColumn = col;
            }
            if (measuredColumn == null) return null;
        }
        ColumnDef grouping = null;
        if (!group.isEmpty()) {
            for (ColumnDef col : Questions.groupable(def, false)) {
                if (!group.equals(normalize(col.label))) continue;
                if (grouping != null) return null;
                grouping = col;
            }
            if (grouping == null) return null;
        }
        Question q = Question.about(def.id).withScope(previous.scope);
        if (measuredColumn != null) q = q.withMeasure(measure, measuredColumn.id);
        if (grouping != null) q = q.withGroupBy(Collections.singletonList(grouping.id));
        return q;
    }

    private static boolean matches(String[] terms, String haystack) {
        String text = normalize(haystack);
        for (String term : terms) if (!text.contains(term)) return false;
        return true;
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();
    }
}
