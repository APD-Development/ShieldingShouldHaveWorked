/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.apd.humor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.List;
import java.util.Random;

// The permanent creative direction, as rules the packs must obey: the developer says "I", invents no community, and the self-aware
// layers (remarks on the grudge itself, long-delayed callbacks) are possible without being part of the first release.
public class CreativeDirectionTest {

	private static final long DAY = 24L * 3600 * 1000;
	private static final long T0 = 1_000_000_000_000L;

	private static void rejects( String row, String fragment ){
		try {
			LinePack.parse( row, "t" );
			fail( "should have been refused: " + row );
		} catch (LinePack.PackException e){
			assertTrue( e.getMessage(), e.getMessage().contains( fragment ) );
		}
	}

	@Test
	public void theDeveloperSpeaksAsIAndNeverAsWe(){
		for (String bad : new String[]{ "We hate Cleric.", "Cleric is bad for us.", "Our Cleric problem continues.", "Let's talk about Cleric.",
				"We're done with Cleric.", "Cleric is ours to suffer." }){
			rejects( "any.0001 | anytime | - | - | 5 | " + bad + " |", "speaks as 'I'" );
		}
		rejects( "any.0001 | anytime | - | - | 5 | Cleric is bad. | We hate Cleric.", "speaks as 'I'" );
		LinePack.parse( "any.0001 | anytime | - | - | 5 | I hate Cleric, and my reasons are my own. |", "t" );
		//the shipped lines all comply (parsing is the lint)
		for (Line l : HumorTestSupport.shipped()){
			assertFalse( l.id, LinePack.PLURAL_VOICE.matcher( l.clean ).find() );
			if (l.adult != null) assertFalse( l.id, LinePack.PLURAL_VOICE.matcher( l.adult ).find() );
		}
	}

	@Test
	public void theDeveloperNeverCallsHimselfTheDeveloper(){
		for (String bad : new String[]{ "The developer hates Cleric.", "This developer is tired of Cleric.", "Local developer insults Cleric again.",
				"As the developer, I dislike Cleric." }){
			rejects( "any.0001 | anytime | - | - | 5 | " + bad + " |", "never as" );
		}
		LinePack.parse( "any.0001 | anytime | - | - | 5 | I dislike Cleric, and I am the one who wrote this. |", "t" );
	}

	@Test
	public void noLineInventsOrQuotesACommunity(){
		for (String bad : new String[]{ "The community agrees Cleric is bad.", "Players have told me Cleric is great.", "Someone on Reddit said Cleric is fine.",
				"A streamer won with Cleric.", "The forums are full of Cleric posts.", "Players wrote to me about Cleric." }){
			rejects( "any.0001 | anytime | - | - | 5 | " + bad + " |", "community" );
		}
	}

	@Test
	public void aLongDelayedPunchlineWaitsForTheDelay(){
		List<Line> lines = LinePack.parse( "app.0001 | appreciation | appreciation,setup | - | 5 | Cleric is my favorite. |\n"
				+ "app.0002 | appreciation | appreciation,after=app.0001,delay=30 | - | 5 | Thirty days later: Cleric is still not my favorite. |\n", "t" );
		Ledger led = new Ledger( null );
		led.markShown( "app.0001", T0 );
		assertEquals( 30, lines.get( 1 ).delayDays );
		assertEquals( "app.0001", Picker.pick( lines, Moment.MENU, Context.EMPTY, led, new Random( 1 ), null, Long.MAX_VALUE, T0 + 29 * DAY ).id );
		assertEquals( "app.0002", Picker.pick( lines, Moment.MENU, Context.EMPTY, led, new Random( 1 ), null, Long.MAX_VALUE, T0 + 30 * DAY ).id );
		LinePack.PackException e = null;
		try {
			LinePack.parse( "app.0003 | appreciation | appreciation,delay=5 | - | 5 | Cleric. |", "t" );
		} catch (LinePack.PackException x){
			e = x;
		}
		assertNotNull( "delay needs a first part", e );
	}

	@Test
	public void theGameCanRemarkOnItsOwnComplainingUsingOnlyRealTotals(){
		//a future self-aware line: it knows how many complaints this player has really heard, and only speaks once that is true
		List<Line> lines = LinePack.parse(
				"any.0001 | anytime | - | - | 5 | Cleric plain complaint one. |\n"
				+ "any.0002 | anytime | - | - | 5 | Cleric plain complaint two. |\n"
				+ "any.0003 | anytime | - | - | 5 | Cleric plain complaint three. |\n"
				+ "meta2.0001 | anytime | - | complaintsHeard>=3 | 1 | You have now heard {complaintsHeard} of my {complaintsTotal} Cleric complaints. I should stop. I will not stop. |\n", "t" );
		ClericGrudge g = new ClericGrudge( lines, new Ledger( null ), new Random( 3 ), true, T0 );
		//until three complaints were really heard, the remark can never appear
		for (int i = 0; i < 3; i++){
			Shown s = g.inlineAlways( Moment.MENU, Context.EMPTY, T0 + i );
			assertFalse( s.text, s.text.contains( "I should stop" ) );
		}
		boolean said = false;
		for (int i = 0; i < 3 && !said; i++){
			Shown s = g.inlineAlways( Moment.MENU, Context.EMPTY, T0 + 100 + i );
			if (s.text.contains( "I should stop" )){
				said = true;
				assertEquals( "You have now heard 3 of my 4 Cleric complaints. I should stop. I will not stop.", s.text );
			}
		}
		assertTrue( said );
	}

	@Test
	public void theSelfAwareLayersAreNotPartOfTheFirstContentVersion(){
		//only the first content version exists; later layers arrive as new `since` versions, never dumped in at once
		java.util.Set<Integer> versions = new java.util.TreeSet<>();
		for (Line l : HumorTestSupport.shipped()) versions.add( l.since );
		assertEquals( "one content version so far", 1, versions.size() );
		for (Line l : HumorTestSupport.shipped()){
			assertNull( l.id + " uses the grudge's own totals before the layers were introduced", usesGrudgeTotals( l ) );
		}
	}

	private static String usesGrudgeTotals( Line l ){
		return (l.clean.contains( "{complaints" ) || l.clean.contains( "{appreciations" )) ? l.id : null;
	}
}
