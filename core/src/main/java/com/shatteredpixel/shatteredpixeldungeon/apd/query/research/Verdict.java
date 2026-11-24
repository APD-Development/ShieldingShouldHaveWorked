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
    /** One luck-lab metric in plain words: where the run sits among comparable runs. Restates the percentile only. */
    public static String luckMetric(String label,Long observed,int n,double percentile){
        String what=label.toLowerCase(java.util.Locale.ROOT);
        if(observed==null)return "this run's "+what+" isn't known exactly";
        if(n<RunLuck.MIN_PEERS || !Double.isFinite(percentile))return what+" ("+observed+") can't be ranked yet: "+n+" comparable runs, "+RunLuck.MIN_PEERS+" needed";
        long p=Math.round(percentile);
        String where=p>=75 ? "more than most":p<=25 ? "fewer than most":"about as many as usual";
        return what+" ("+observed+"): "+where+", above "+p+"% of "+n+" comparable runs";
    }
    /** The luck lab's "In short:" line. */
    public static String luck(RunLuck.Result r){
        if(r==null)return "In short: this run can't be checked.";
        if(!r.unavailable.isEmpty())return "In short: this run can't be checked: "+r.unavailable;
        StringBuilder b=new StringBuilder("In short: ");
        for(int i=0;i<r.metrics.size();i++){RunLuck.Metric m=r.metrics.get(i);if(i>0)b.append("; ");
            String t=luckMetric(m.label,m.observed,m.n,m.percentile);b.append(i==0 ? Character.toUpperCase(t.charAt(0))+t.substring(1):t);}
        return b.append(". This compares your own history only; it is not a chance score.").toString();
    }
    /** One discovery in plain words: who won more often, by how much. Restates the counts only. */
    public static String discovery(Discoveries.Finding f){
        if(f==null)return "In short: nothing to show.";
        Discoveries.Group hi=f.difference>=0 ? f.a:f.b,lo=f.difference>=0 ? f.b:f.a;
        return "In short: "+hi.label+" won "+pct(hi)+" of runs ("+hi.wins+" of "+hi.rows.size()+") and "+lo.label+" won "+pct(lo)+" ("+lo.wins+" of "+lo.rows.size()
                +"), a gap bigger than chance would usually make. It doesn't say why.";
    }
    /** The Automatic discoveries "In short:" line. */
    public static String discoveries(Discoveries.Result r){
        if(r==null)return "In short: discoveries need the plain runs list (no grouping, no errors).";
        if(r.findings.isEmpty())return r.eligible==0 ? "In short: no finished runs here can be compared yet."
                :"In short: nothing stood out across "+r.eligible+" finished runs; that doesn't mean the groups are the same.";
        Discoveries.Finding top=r.findings.get(0);
        String n=r.findings.size()==1 ? "1 gap stood out":r.findings.size()+" gaps stood out";
        return "In short: "+n+" across "+r.eligible+" finished runs. Biggest: "+discovery(top).substring("In short: ".length());
    }
    private static String pct(Discoveries.Group g){return ResearchData.value(100d*g.wins/g.rows.size())+"%";}
}
