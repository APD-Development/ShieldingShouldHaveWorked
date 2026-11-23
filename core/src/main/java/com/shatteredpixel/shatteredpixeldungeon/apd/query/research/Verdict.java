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

package com.shatteredpixel.shatteredpixeldungeon.apd.query.research;

/** One plain sentence on top of a Research lab result. It restates the numbers; it never gives advice or causes. */
public final class Verdict {
    private Verdict(){}
    /** @param first name of side A, e.g. "A" or "Recent runs"; @param second name of side B. */
    public static String compare(PopulationStudy.Comparison c,String metric,String first,String second,double divisor){
        if(c==null)return "In short: these two can't be compared.";
        if(c.a.n==0 || c.b.n==0 || !Double.isFinite(c.difference))
            return "In short: there's no exact "+metric+" on "+(c.a.n==0 ? first:second)+"'s side yet, so there's nothing to compare.";
        String gap=ResearchData.value(Math.abs(c.difference)/divisor);
        if(c.difference==0)return "In short: "+first+" and "+second+" have the same average "+metric+" ("+ResearchData.value(c.a.mean/divisor)+").";
        String higher=c.difference>0 ? first:second,lower=c.difference>0 ? second:first;
        if(!c.intervalKnown)
            return "In short: "+higher+" averages "+gap+" more "+metric+" than "+lower+", but there aren't enough comparable runs ("+c.a.n+" vs "+c.b.n+") to tell that apart from chance.";
        if(c.low>0 || c.high<0)
            return "In short: "+higher+" has a higher "+metric+" than "+lower+" by about "+gap+", and that gap is bigger than chance would usually make. It doesn't say why.";
        return "In short: "+higher+" averages "+gap+" more "+metric+" than "+lower+", but that's within what chance alone could make, so there's no clear difference.";
    }
}
