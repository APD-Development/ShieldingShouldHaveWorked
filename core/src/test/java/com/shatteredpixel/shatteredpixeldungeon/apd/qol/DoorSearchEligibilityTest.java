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
package com.shatteredpixel.shatteredpixeldungeon.apd.qol;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import org.junit.*;
import java.util.Arrays;
import static org.junit.Assert.*;

public class DoorSearchEligibilityTest {
    private Level oldLevel;private com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero oldHero;private boolean oldIntro;private int oldDepth,oldBranch,oldChallenges;private GrassDewPickupTest.TestLevel level;
    @Before public void setup(){HeadlessTestSupport.start();oldLevel=Dungeon.level;oldHero=Dungeon.hero;oldIntro=SPDSettings.intro();oldChallenges=SPDSettings.challenges();oldDepth=Dungeon.depth;oldBranch=Dungeon.branch;Dungeon.depth=7;Dungeon.branch=0;level=new GrassDewPickupTest.TestLevel();level.setSize(7,7);Dungeon.level=level;Arrays.fill(level.map,Terrain.WALL);Arrays.fill(level.heroFOV,true);level.map[17]=Terrain.EMPTY;level.map[31]=Terrain.EMPTY;}
    @After public void restore(){Dungeon.level=oldLevel;Dungeon.hero=oldHero;SPDSettings.intro(oldIntro);SPDSettings.challenges(oldChallenges);Dungeon.depth=oldDepth;Dungeon.branch=oldBranch;}
    private void legacy(int cell){Bundle b=new Bundle();b.put("apd_searched_walls_v1",new int[]{cell});b.put("apd_door_checks_v1",new int[]{cell});level.searchedWalls.restore(b,level.length());}
    @Test public void onlyPlainWallAndItsSecretDisguiseCanBeCandidates(){
        for(int t=0;t<Terrain.flags.length;t++){level.map[24]=t;boolean expected=t==Terrain.WALL||t==Terrain.SECRET_DOOR;assertEquals("terrain "+t,expected,DoorSearchEligibility.candidate(level,24));}
    }
    @Test public void libraryBookshelfReproductionHasNeitherFaceNorProjectedCheck(){level.map[24]=Terrain.BOOKSHELF;legacy(24);assertFalse(DoorSearchEligibility.candidate(level,24));assertFalse(level.searchedWalls.shows(level,24));assertFalse(level.searchedWalls.confirmed(level,24));assertEquals(-1,SearchedWallMarkers.visual(level,24));assertEquals(-1,SearchedWallMarkers.visual(level,17));}
    @Test public void floorOneAndTenNeverRecordOrDisplayAnyDoorChecks(){for(int depth:new int[]{1,10}){Dungeon.depth=depth;legacy(24);assertTrue(level.searchedWalls.attempted(24));assertFalse(level.searchedWalls.mark(level,23));assertFalse(DoorSearchEligibility.candidate(level,24));assertFalse(level.searchedWalls.confirmed(level,24));assertEquals(-1,SearchedWallMarkers.visual(level,24));assertEquals(-1,SearchedWallMarkers.visual(level,17));}}
    @Test public void allOtherFixedMainFloorsAreExcludedToo(){for(int depth:new int[]{15,20,25,26}){Dungeon.depth=depth;legacy(24);assertFalse(DoorSearchEligibility.candidate(level,24));assertEquals(-1,SearchedWallMarkers.visual(level,24));}}
    @Test public void ordinaryFloorsAndRatKingFloorAreNotBlanketSuppressed(){for(int depth:new int[]{2,3,4,5,6,7,8,9,11,12,13,14,16,17,18,19,21,22,23,24}){Dungeon.depth=depth;assertTrue("floor "+depth,DoorSearchEligibility.candidate(level,24));}assertTrue(DoorSearchEligibility.supportsDoors(new SewerBossLevel(),5,0));}
    @Test public void fixedMapClassesNeverClaimDoorsAtAnUnusualDepthOrBranch(){for(Level l:new Level[]{new PrisonBossLevel(),new CavesBossLevel(),new CityBossLevel(),new HallsBossLevel(),new LastLevel(),new DeadEndLevel(),new VaultLevel(),new MiningLevel()})assertFalse(l.getClass().getSimpleName(),DoorSearchEligibility.supportsDoors(l,7,1));}
    @Test public void ordinaryUnknownBranchesAreNotGuessedFromAMainFloorNumber(){assertTrue(DoorSearchEligibility.supportsDoors(level,10,1));assertTrue(DoorSearchEligibility.supportsDoors(level,1,2));}
    @Test public void allKnownSolidBackingTilesRuleOutThatOrientation(){for(int t=0;t<Terrain.flags.length;t++)if((Terrain.flags[t]&Terrain.SOLID)!=0){level.map[17]=t;assertFalse("known backing "+t,DoorSearchEligibility.candidate(level,24));}}
    @Test public void furnitureCannotPretendToBeStructuralDoorFrames(){for(int t:new int[]{Terrain.BOOKSHELF,Terrain.ALCHEMY,Terrain.STATUE,Terrain.STATUE_SP,Terrain.MINE_CRYSTAL,Terrain.MINE_BOULDER,Terrain.CUSTOM_DECO}){level.map[23]=t;level.map[25]=t;assertFalse("furniture frame "+t,DoorSearchEligibility.candidate(level,24));}}
    @Test public void existingDoorFramesRemainValid(){for(int t:new int[]{Terrain.WALL_DECO,Terrain.DOOR,Terrain.OPEN_DOOR,Terrain.LOCKED_DOOR,Terrain.CRYSTAL_DOOR,Terrain.HERO_LKD_DR,Terrain.BARRICADE}){level.map[23]=t;assertTrue("door frame "+t,DoorSearchEligibility.candidate(level,24));}}
    @Test public void unknownBackingHasTheSameAnswerForEveryActualHiddenTile(){level.heroFOV[17]=false;for(int t=0;t<Terrain.flags.length;t++){level.map[17]=t;assertTrue("unobserved backing "+t,DoorSearchEligibility.candidate(level,24));}}
    @Test public void unknownSideFramesHaveTheSameAnswerForEveryActualHiddenTile(){level.heroFOV[23]=false;for(int t=0;t<Terrain.flags.length;t++){level.map[23]=t;assertTrue("unobserved frame "+t,DoorSearchEligibility.candidate(level,24));}}
    @Test public void cornersPillarsThickStoneAndMapEdgesNeverDisplayChecks(){legacy(24);Arrays.fill(level.map,Terrain.EMPTY);level.map[24]=Terrain.WALL;assertEquals(-1,SearchedWallMarkers.visual(level,24));level.map[17]=level.map[23]=Terrain.WALL;assertEquals(-1,SearchedWallMarkers.visual(level,24));Arrays.fill(level.map,Terrain.WALL);level.map[31]=Terrain.EMPTY;assertEquals(-1,SearchedWallMarkers.visual(level,24));for(int p:new int[]{-1,0,3,6,7,13,42,45,48,49,Integer.MAX_VALUE})assertFalse(DoorSearchEligibility.candidate(level,p));assertFalse(DoorSearchEligibility.candidate(null,24));}
    @Test public void suppressionDoesNotEraseOrRewriteLegacySearchMemory(){legacy(24);Bundle before=new Bundle();level.searchedWalls.store(before);Dungeon.depth=1;for(int p=0;p<level.length();p++)SearchedWallMarkers.visual(level,p);Bundle after=new Bundle();level.searchedWalls.store(after);assertEquals(before.toString(),after.toString());Dungeon.depth=7;assertTrue(DoorSearchEligibility.candidate(level,24));assertEquals(SearchedWallMarkers.FACE,SearchedWallMarkers.visual(level,24));}
    @Test public void eligibilityDoesNotChangeMapFogOrConsumeRandomness(){int[] map=level.map.clone();boolean[] fog=level.heroFOV.clone();Random.pushGenerator(8742);long expected=Random.Long();Random.popGenerator();Random.pushGenerator(8742);try{for(int p=0;p<level.length();p++)DoorSearchEligibility.candidate(level,p);assertEquals(expected,Random.Long());}finally{Random.popGenerator();}assertArrayEquals(map,level.map);assertArrayEquals(fog,level.heroFOV);}
    @Test public void realGeneratedSecretDoorsRemainCandidatesAndDoorlessFloorsReallyContainNone()throws Exception{
        java.util.function.BiConsumer<Integer,Level> old=LevelGenerationHarness.levelObserver;final int[] secrets={0};
        try{LevelGenerationHarness.levelObserver=(depth,l)->{
            Arrays.fill(l.heroFOV,true);
            boolean supports=DoorSearchEligibility.supportsDoors(l,depth,0);
            for(int p=0;p<l.length();p++)if(l.map[p]==Terrain.SECRET_DOOR){assertTrue("unexpected secret on "+l.getClass().getSimpleName()+" floor "+depth,supports);assertTrue("real secret geometry on floor "+depth+" at "+p,DoorSearchEligibility.candidate(l,p));secrets[0]++;}
        };for(long seed:new long[]{1,12345,987654321})LevelGenerationHarness.generate(seed,0,HeroClass.WARRIOR,LevelGenerationHarness.Context.VETERAN,26);assertTrue("actual secret doors exercised",secrets[0]>20);}finally{LevelGenerationHarness.levelObserver=old;}
    }
    @Test public void decoratedWallsNeverHaveDoorMarksButRemainValidSideFrames(){level.map[24]=Terrain.WALL_DECO;legacy(24);assertFalse(DoorSearchEligibility.candidate(level,24));assertFalse(level.searchedWalls.mark(level,24));assertFalse(level.searchedWalls.confirmed(level,24));assertEquals(-1,SearchedWallMarkers.visual(level,24));level.map[24]=Terrain.WALL;level.map[23]=Terrain.WALL_DECO;assertTrue(DoorSearchEligibility.candidate(level,24));}

    @Test public void existingDoorsCannotBeTheBackingTileOfAnotherDoor(){for(int tile:new int[]{Terrain.DOOR,Terrain.OPEN_DOOR,Terrain.LOCKED_DOOR,Terrain.HERO_LKD_DR,Terrain.CRYSTAL_DOOR}){level.map[17]=tile;assertFalse("consecutive doorway "+tile,DoorSearchEligibility.candidate(level,24));}}

    @Test public void unseenMapBordersCannotBeInventedAsSpaceBeyondADoor(){for(int[] pair:new int[][]{{10,17},{38,31},{22,23},{26,25}}){Arrays.fill(level.map,Terrain.WALL);Arrays.fill(level.heroFOV,true);level.map[pair[1]]=Terrain.EMPTY;for(int p=0;p<49;p++)if(p<7||p>=42||p%7==0||p%7==6)level.heroFOV[p]=false;assertFalse("no passage through immutable map border at "+pair[0],DoorSearchEligibility.candidate(level,pair[0]));}}

}
