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

	//a stack bigger than the flask's room: only the room goes in, the rest is not thrown away
	@Test public void flaskTakesOnlyWhatFits(){
		assertEquals(1, com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.dropsThatFit(19, 2));
		assertEquals(2, com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.dropsThatFit(0, 2));
		assertEquals(0, com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.dropsThatFit(20, 3));
	}
}
