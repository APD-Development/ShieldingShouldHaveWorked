package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.Fact;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class UimsdTopicSearchTest {
    private Catalog catalog() {
        DatasetDef def = QuerySupport.def();
        Row visible = QuerySupport.row(def, "one", "épée", 13L, 1L, 77L, false, "r");
        visible.setText(QuerySupport.NOTE, "secret internal note", Fact.PROV_RECORDED);
        Catalog c = new Catalog();
        c.add(QuerySupport.dataset(def, Arrays.asList(visible)));
        DatasetDef empty = new DatasetDef("empty", "Empty topic", "Should not be listed");
        empty.add(ColumnDef.num("money", "Coins"));
        c.add(QuerySupport.dataset(empty, Collections.<Row>emptyList()));
        return c;
    }

    @Test public void findsRegisteredTopicAndPublicFieldsWithoutSearchingRunValues() {
        Catalog c = catalog();
        List<UimsdTopicSearch.Hit> topics = UimsdTopicSearch.find(c, " THINGS ");
        assertEquals(1, topics.size());
        assertEquals("things", topics.get(0).datasetId);
        assertEquals("", topics.get(0).columnId);
        List<UimsdTopicSearch.Hit> scores = UimsdTopicSearch.find(c, "SCORE");
        assertEquals(1, scores.size());
        assertEquals("score", scores.get(0).columnId);
        assertTrue("individual text is never indexed", UimsdTopicSearch.find(c, "épée").isEmpty());
        assertTrue("unrecorded dataset excluded", UimsdTopicSearch.find(c, "Coins").isEmpty());
    }

    @Test public void internalColumnsAndValuesNeverEnterPublicSearch() {
        Catalog c = catalog();
        for (String term : new String[]{"secret", "note", "77", "internal note"})
            assertTrue(term, UimsdTopicSearch.find(c, term).isEmpty());
    }

    @Test public void accentFoldingMultipleTermsAndCaseAreDeterministic() {
        DatasetDef def = new DatasetDef("alchemy", "Épée statistics", "Special content");
        def.add(ColumnDef.text("weapon", "Weapon class").shown());
        Catalog c = new Catalog().add(QuerySupport.dataset(def, Arrays.asList(new Row("a", null, "r", 1))));
        assertEquals("alchemy", UimsdTopicSearch.find(c, "EPEE STATISTICS").get(0).datasetId);
        assertEquals("weapon", UimsdTopicSearch.find(c, "  WEAPON   CLASS ").get(0).columnId);
        assertTrue(UimsdTopicSearch.find(c, "unrelated").isEmpty());
        assertTrue(UimsdTopicSearch.find(c, "   ").isEmpty());
        assertTrue(UimsdTopicSearch.find(null, "weapon").isEmpty());
        assertEquals("alchemy", UimsdTopicSearch.find(c, "EPEE").get(0).datasetId);
    }

    @Test public void resultsOpenRealQuestionBuilderWithoutElevatingVisibility() {
        Catalog c = catalog();
        DatasetDef def = c.dataset("things").def;
        Question before = Question.about("other").withScope(new Scope(Scope.Rules.ALL, "", true, true, true, true));
        UimsdTopicSearch.Hit score = UimsdTopicSearch.find(c, "score").get(0);
        Question total = score.question(before, def);
        assertEquals("things", total.dataset);
        assertEquals(Question.Measure.TOTAL, total.measure);
        assertEquals("score", total.column);
        assertEquals(before.scope.rules, total.scope.rules);
        assertFalse(total.showInternal);
        UimsdTopicSearch.Hit name = UimsdTopicSearch.find(c, "name").get(0);
        Question grouped = name.question(before, def);
        assertEquals(Arrays.asList("name"), grouped.groupBy);
        assertFalse(grouped.showInternal);
    }
}
