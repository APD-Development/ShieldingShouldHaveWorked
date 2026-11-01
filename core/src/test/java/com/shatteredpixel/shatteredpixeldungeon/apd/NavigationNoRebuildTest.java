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

package com.shatteredpixel.shatteredpixeldungeon.apd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.scenes.JournalScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.APDTextScene;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Compiled scene guards: ordinary local navigation must not request scene resets. */
public class NavigationNoRebuildTest {
 private static String classConstants(Class<?> type) throws Exception {
  try(InputStream in=type.getResourceAsStream("/"+type.getName().replace('.','/')+".class")){
   ByteArrayOutputStream data=new ByteArrayOutputStream();
   byte[] buf=new byte[4096];
   for(int n;(n=in.read(buf))>0;)data.write(buf,0,n);
   return new String(data.toByteArray(),StandardCharsets.ISO_8859_1);
  }
 }
 @Test public void journalTabSwitchHasNoWholeSceneResetCall() throws Exception {
  JournalScene.class.getDeclaredMethod("selectTab",int.class);
  assertFalse(classConstants(JournalScene.class).contains("seamlessResetScene"));
  assertTrue(classConstants(JournalScene.class).contains("showSelectedTab"));
 }
 @Test public void historyAndNewsPagingDoNotResetTheScene() throws Exception {
  APDTextScene.class.getDeclaredMethod("refreshPage");
  assertFalse(classConstants(APDTextScene.class).contains("seamlessResetScene"));
  assertTrue(classConstants(APDTextScene.class).contains("visiblePage"));
 }
}
