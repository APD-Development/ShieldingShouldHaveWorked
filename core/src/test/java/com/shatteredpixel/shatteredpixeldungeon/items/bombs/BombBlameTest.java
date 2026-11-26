package com.shatteredpixel.shatteredpixeldungeon.items.bombs;

import static org.junit.Assert.assertSame;

import org.junit.Test;

// Trial juror 2 (Nov 25): a cursed wand's explosion killed the hero and the death recap and "Ended by" blamed the bomb.
public class BombBlameTest {
	@Test public void aCursedWandsBlastIsBlamedOnTheWand(){
		Object wand = new Object();
		Bomb blast = new Bomb.ConjuredBomb().apdCausedBy(wand);
		assertSame(wand, Bomb.apdBlame(blast));
	}
	@Test public void anOrdinaryBombIsStillTheBomb(){
		Bomb bomb = new Bomb.ConjuredBomb();
		assertSame(bomb, Bomb.apdBlame(bomb));
		Object other = new Object();
		assertSame(other, Bomb.apdBlame(other));
		assertSame(null, Bomb.apdBlame(null));
	}
}
