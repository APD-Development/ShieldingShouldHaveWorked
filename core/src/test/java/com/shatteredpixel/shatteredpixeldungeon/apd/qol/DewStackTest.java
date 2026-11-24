package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import static org.junit.Assert.assertEquals;

import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;

import org.junit.Test;

//Chris, Nov 24: a dewdrop landing on another dewdrop makes a stack of 2, not 1
public class DewStackTest {
	@Test public void twoDropsOnOneTileStackToTwo(){
		Heap h = new Heap();
		h.drop(new Dewdrop());
		h.drop(new Dewdrop());
		assertEquals(1, h.items.size());
		assertEquals(2, h.peek().quantity());
		h.drop(new Dewdrop());
		assertEquals(3, h.peek().quantity());
	}
}
