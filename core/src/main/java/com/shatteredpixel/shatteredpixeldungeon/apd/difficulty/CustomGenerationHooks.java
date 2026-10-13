package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
/** Guaranteed upgrade scheduling only; quest items and other loot are untouched. */
public final class CustomGenerationHooks {
    private CustomGenerationHooks() { }
    public static int upgradeScrollsForFloor() {
        int selected=CustomCombatHooks.percentage(CustomDungeonRules.Slider.UPGRADE_SCROLLS);
        // This is deliberately the original RNG-consuming predicate at neutral settings.
        if(selected==UpgradeScrollPlan.NEUTRAL) return Dungeon.souNeeded() ? 1 : 0;
        return new UpgradeScrollPlan(selected).floorCount(Dungeon.depth);
    }
}
