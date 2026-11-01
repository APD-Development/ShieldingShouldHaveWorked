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

import org.junit.runner.*;
public class QuickSlotBugBaseline {
 public static void main(String[] args)throws Exception{
  Class<?> test=Class.forName("com.shatteredpixel.shatteredpixeldungeon.QuickSlotCapacityTest");
  JUnitCore runner=new JUnitCore();int failures=0;
  for(String method:new String[]{"invalidAssignmentRetainsTheExistingItemAndShortcut","invalidSavedShortcutDoesNotPreventPhysicalItemRestoration"}){
   Result result=runner.run(Request.method(test,method));System.out.println(method+": run="+result.getRunCount()+" failures="+result.getFailureCount());
   for(org.junit.runner.notification.Failure failure:result.getFailures())System.out.println(failure.getException().getClass().getName()+": "+failure.getMessage());
   failures+=result.getFailureCount();
  }
  if(failures!=2)throw new AssertionError("expected both old-source bugs reproduced");
 }
}
