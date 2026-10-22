package com.shatteredpixel.shatteredpixeldungeon.apd.healing;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.watabou.utils.GameSettings;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.files.FileHandle;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;

public class HealingConfirmationTest {
    @Test public void inclusiveBoundariesUseExactHpAndDoNotLeakUnknownPotions() {
        assertFalse(HealingConfirmation.required(true,-1,100,100));
        for(int threshold=0;threshold<=100;threshold++) {
            assertTrue(HealingConfirmation.required(true,threshold,threshold,100));
            if(threshold>0) assertFalse(HealingConfirmation.required(true,threshold,threshold-1,100));
            assertFalse(HealingConfirmation.required(false,threshold,100,100));
        }
        assertFalse(HealingConfirmation.required(true,50,1,0));
        assertFalse(HealingConfirmation.required(true,50,1,3));
        assertTrue(HealingConfirmation.required(true,50,2,3));
        assertTrue(HealingConfirmation.required(true,100,Integer.MAX_VALUE,Integer.MAX_VALUE));
    }
    @Test public void preferenceDefaultsToOneTapAndSurvivesReloadWithoutTouchingRuns() throws Exception {
        withPreferences(file->{
            assertEquals(-1,SPDSettings.healingConfirmationThreshold());
            for(int value:new int[]{0,50,100,-1}) {
                SPDSettings.healingConfirmationThreshold(value);
                GameSettings.set(new HeadlessPreferences(new FileHandle(file)));
                assertEquals(value,SPDSettings.healingConfirmationThreshold());
            }
        });
    }
    @Test public void cancellingConfirmationDoesNotConsumeItemOrTurn() throws Exception {
        withPreferences(file->{
            SPDSettings.healingConfirmationThreshold(50);
            Hero old=Dungeon.hero; Hero hero=new Hero(); Dungeon.hero=hero;
            try {
                hero.HP=hero.HT=20; hero.ready=true;
                CapturedPotion potion=new CapturedPotion(); 
                hero.belongings.backpack.items.add(potion);
                float before=hero.cooldown();
                potion.execute(hero,PotionOfHealing.AC_DRINK);
                assertNotNull(potion.confirmed);
                assertTrue(hero.belongings.backpack.contains(potion));
                assertEquals(1,potion.quantity()); assertEquals(before,hero.cooldown(),0f);
                assertEquals(20,hero.HP); assertTrue(hero.ready);
                // Closing/cancelling the native popup does not invoke the captured continuation.
            } finally { Dungeon.hero=old; }
        });
    }
    @Test public void categoryHotbarUsesOneTapOnlyForKnownHealingWhenOff() {
        assertTrue(HealingConfirmation.categoryDirectDrink(true, -1));
        assertFalse(HealingConfirmation.categoryDirectDrink(false, -1));
        for (int threshold=0; threshold<=100; threshold++) {
            assertFalse(HealingConfirmation.categoryDirectDrink(true, threshold));
        }
    }
    @Test public void offBypassesConfirmationAndDrinksThroughRealPotionExecute() throws Exception {
        withPreferences(file -> {
            SPDSettings.healingConfirmationThreshold(-1);
            Hero previous=Dungeon.hero; Hero hero=new Hero(); Dungeon.hero=hero;
            try {
                hero.HT=20; hero.HP=10; hero.ready=true;
                CapturedPotion potion=new CapturedPotion() {
                    @Override protected void drink(Hero h) { confirmedDrinkCount++; }
                };
                potion.execute(hero,PotionOfHealing.AC_DRINK);
                assertNull(potion.confirmed);
                assertEquals(1,confirmedDrinkCount);
                assertEquals(-1,SPDSettings.healingConfirmationThreshold());
            } finally { Dungeon.hero=previous; }
        });
    }
    private static class CapturedPotion extends PotionOfHealing {
        Runnable confirmed;
        int confirmedDrinkCount;
        CapturedPotion() { anonymous = true; }
        @Override protected void requestHealingConfirmation(Hero hero,Runnable confirmed){this.confirmed=confirmed;}
    }
    private interface Check { void run(File file) throws Exception; }
    private static void withPreferences(Check check) throws Exception {
        HeadlessTestSupport.start();
        java.lang.reflect.Field f=GameSettings.class.getDeclaredField("prefs");f.setAccessible(true);
        Preferences old=(Preferences)f.get(null);
        File file=File.createTempFile("healing-confirmation-",".xml",HeadlessTestSupport.tempDir());file.delete();
        try { GameSettings.set(new HeadlessPreferences(new FileHandle(file)));check.run(file); }
        finally { GameSettings.set(old);file.delete(); }
    }
}
