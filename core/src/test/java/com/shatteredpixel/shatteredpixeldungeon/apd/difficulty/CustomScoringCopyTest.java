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

package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
/** Catch stale current-status explanations in every developer-authored UI source and offline asset. */
public class CustomScoringCopyTest{
 @Test public void playerFacingFilesDoNotClaimActiveMultipliersAreStillAbsent()throws Exception{
  List<String> stale=Arrays.asList("These settings currently do not affect your earned score", "Custom scoring is still in development", "currently leave your earned score unchanged", "current settings preserve original earned scores");
  for(Path root:new Path[]{Paths.get(System.getProperty("apd.game.src")),Paths.get("apd")})try(java.util.stream.Stream<Path> paths=Files.walk(root)){
   Iterator<Path> files=paths.iterator();while(files.hasNext()){Path p=files.next();if(!Files.isRegularFile(p))continue;String n=p.toString();if(!(n.endsWith(".java")||n.endsWith(".txt")||n.endsWith(".tsv")||n.endsWith(".properties")))continue;String text=new String(Files.readAllBytes(p),StandardCharsets.UTF_8);for(String phrase:stale)assertFalse(p+": "+phrase,text.contains(phrase));}
  }
 }
}
