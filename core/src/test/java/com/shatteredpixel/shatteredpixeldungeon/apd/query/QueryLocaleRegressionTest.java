package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.*;
import static com.shatteredpixel.shatteredpixeldungeon.apd.query.QuerySupport.*;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class QueryLocaleRegressionTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    private Dataset fixture() {
        DatasetDef def = def();
        return dataset(def, Arrays.asList(
                row(def, "upper", "RING OF MIGHT", 20L, 0L, null, true, "rules"),
                row(def, "lower", "ring of accuracy", 20L, 0L, null, true, "rules"),
                row(def, "other", "Sword", 100L, 0L, null, true, "rules"),
                row(def, "unknown", null, 999L, 0L, null, true, "rules")));
    }

    @Test public void containsMatchesTheSameRecordedNamesUnderDifferentDeviceLocales() {
        Locale previous = Locale.getDefault();
        try {
            Dataset data = fixture();
            for (Locale locale : new Locale[]{Locale.ROOT, Locale.ENGLISH, Locale.forLanguageTag("tr-TR"), Locale.forLanguageTag("az-AZ")}) {
                Locale.setDefault(locale);
                for (String search : new String[]{"ring", "RING", "RiNg"}) {
                    QueryResult result = QueryEngine.run(data, Query.of("things")
                            .addFilter(Filter.text("name", Op.CONTAINS, search)), "rules");
                    assertTrue(result.problems.toString(), result.problems.isEmpty());
                    assertEquals("device locale " + locale + ", search " + search,
                            Arrays.asList("RING OF MIGHT", "ring of accuracy"), names(result));
                    assertEquals("unknown names stay unknown", 1, result.stats.filters.get(0).unknown);
                }
            }
        } finally { Locale.setDefault(previous); }
    }

    @Test public void savedFilterSortAndColumnOrderAreStableAfterChangingTheDeviceLocale() throws Exception {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            Dataset data = fixture();
            Query query = Query.of("things").addFilter(Filter.text("name", Op.CONTAINS, "ring"))
                    .withSorts(Arrays.asList(new Sort("score", true)))
                    .withColumns(Arrays.asList("score", "name"));
            File file = SavedViews.fileIn(tmp.getRoot());
            SavedViews views = new SavedViews();
            views.save("Ring equipment", query, 10);
            views.store(file);
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Query reopened = SavedViews.load(file).get("Ring equipment").query();
            assertEquals(query, reopened);
            QueryResult result = QueryEngine.run(data, reopened, "rules");
            assertEquals("equal scores keep stable ordering", Arrays.asList("RING OF MIGHT", "ring of accuracy"), names(result));
            assertEquals("score", result.columns.get(0).id);
            assertEquals("name", result.columns.get(1).id);
            assertEquals("recorded names never change", "RING OF MIGHT", data.rows.get(0).text(NAME));
        } finally { Locale.setDefault(previous); }
    }

    @Test public void savedTextFiltersPreserveLiteralSeparatorsUnicodeAndCase() throws Exception {
        String[] literals = {"back\\slash", "literal\\n", "tab\there", "line\nbreak", "pipe|value",
                "élixir", "药水", "😀", "__literal__", "**literal**", " leading", "trailing "};
        File file = SavedViews.fileIn(tmp.getRoot());
        SavedViews views = new SavedViews();
        Query query = Query.of("things").addFilter(Filter.text("name", Op.IN, literals));
        views.save("Literal values", query, 20);
        views.store(file);
        assertEquals(query, SavedViews.load(file).get("Literal values").query());
    }
}
