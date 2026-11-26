package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

// Chris, Nov 26 (five trial jurors): "All rules" picked on one table must hold on every table opened after it
public class SharedRulesScopeTest {
	private final UimsdModel m = new UimsdModel("r1", "Current");

	@Test public void untilThePlayerPicksEachTableKeepsItsOwnRules(){
		Query q = Query.of("deaths");
		assertSame(q, m.withSharedRules(q));
	}
	@Test public void allRulesPickedOnceAppliesToTheNextTable(){
		m.shareRules(Scope.Rules.ALL, "");
		Query deaths = m.withSharedRules(Query.of("deaths"));
		assertEquals(Scope.Rules.ALL, deaths.scope.rules);
		assertEquals(true, m.sharedAllRules());
	}
	@Test public void aSpecificRulesetCarriesWithItsId(){
		m.shareRules(Scope.Rules.SPECIFIC, "r42");
		Query items = m.withSharedRules(Query.of("items"));
		assertEquals(Scope.Rules.SPECIFIC, items.scope.rules);
		assertEquals("r42", items.scope.ruleset);
		assertEquals(false, m.sharedAllRules());
	}
}
