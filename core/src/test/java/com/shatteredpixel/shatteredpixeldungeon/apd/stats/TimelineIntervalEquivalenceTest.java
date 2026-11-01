/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

package com.shatteredpixel.shatteredpixeldungeon.apd.stats;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class TimelineIntervalEquivalenceTest {
 @Test public void overlappingAndSeparatedRollbackRangesPreserveEveryEventClassification() {
  for(int seed=0;seed<200;seed++) {
   Random random=new Random(seed);List<ApdEvent> events=new ArrayList<>();Set<Long> abandoned=new HashSet<>();long uncertain=-1;int rollbacks=0;
   for(int i=0;i<300;i++){
    int type=random.nextInt(9),eventType=type<3?EventTypes.SAVE:type==3?EventTypes.GAP:type==4?EventTypes.SESSION_START:type>=7?EventTypes.ROLLBACK:EventTypes.LEVEL_ENTER;
    byte[] payload=new byte[0];
    if(eventType==EventTypes.ROLLBACK){rollbacks++;int kind=type==8?LifecycleEvents.ROLLBACK_UNMATCHED:LifecycleEvents.ROLLBACK_MATCHED;
     long save=kind==LifecycleEvents.ROLLBACK_UNMATCHED?-1:random.nextInt(i+1);
     payload=new ByteSink().writeUnsigned(kind).writeSigned(save).writeUnsigned(0).writeSigned(0).toByteArray();
     if(kind==LifecycleEvents.ROLLBACK_UNMATCHED){if(uncertain<0)uncertain=i;}
     else for(ApdEvent event:events)if(event.index>save && !LifecycleEvents.isRecordingMetaEvent(event.type))abandoned.add(event.index);
    }
    events.add(new ApdEvent(i,eventType,0,i,payload));
   }
   RunTimeline actual=RunTimeline.of(events);assertEquals(abandoned.size(),actual.abandonedCount());assertEquals(rollbacks,actual.rollbackCount());assertEquals(uncertain,actual.uncertainFromIndex());
   List<ApdEvent> canonical=new ArrayList<>();
   for(ApdEvent event:events){assertEquals("seed "+seed+" event "+event.index,abandoned.contains(event.index),actual.isAbandoned(event));if(!abandoned.contains(event.index))canonical.add(event);}
   assertEquals(canonical,actual.canonical());
  }
 }
 @Test public void unorderedInputKeepsOriginalFallbackSemantics() {
  List<ApdEvent> events=Arrays.asList(new ApdEvent(0,EventTypes.SAVE,0,0,new byte[0]),new ApdEvent(3,EventTypes.LEVEL_ENTER,0,0,new byte[0]),
   new ApdEvent(1,EventTypes.LEVEL_ENTER,0,0,new byte[0]),new ApdEvent(2,EventTypes.ROLLBACK,0,0,new ByteSink().writeUnsigned(0).writeSigned(0).writeUnsigned(0).writeSigned(0).toByteArray()));
  // The old scan stops at index3 before reaching the later out-of-order index1.
  assertEquals(0,RunTimeline.of(events).abandonedCount());
 }
}
