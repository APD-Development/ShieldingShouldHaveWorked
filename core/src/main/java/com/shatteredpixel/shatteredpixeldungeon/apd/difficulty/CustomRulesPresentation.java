package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import java.util.Locale;

/** Display and integer-position mapping only; no scoring or gameplay calculation. */
public final class CustomRulesPresentation {
    private CustomRulesPresentation() { }
    public static final String SCORING_NOTICE = "Custom Dungeon Rules scoring is still being developed. Difficulty settings currently do not affect your score. A scoring multiplier will be added in a future update.";
    public static String label(CustomDungeonRules.Slider slider) {
        switch(slider) {
            case ENEMY_HP: return "Enemy HP";
            case ENEMY_DAMAGE: return "Enemy Damage";
            case ENEMY_DEFENSE: return "Enemy Defense";
            case ENEMY_ACCURACY: return "Enemy Accuracy";
            case ENEMY_EVASION: return "Enemy Evasion";
            case ENEMY_SPAWN: return "Enemy Spawn Frequency";
            case BOSS_HP: return "Boss HP";
            case BOSS_DAMAGE: return "Boss Damage";
            case PLAYER_HP: return "Player Maximum HP";
            case PLAYER_DAMAGE: return "Player Damage";
            case PLAYER_ACCURACY: return "Player Accuracy";
            case PLAYER_EVASION: return "Player Evasion";
            case PLAYER_DEFENSE: return "Player Defense";
            case FOOD: return "Food Availability";
            case HUNGER: return "Hunger Depletion";
            case HEALING: return "Healing Effectiveness";
            case EQUIPMENT: return "Equipment Availability";
            case UPGRADE_SCROLLS: return "Upgrade Scrolls";
            default: throw new IllegalArgumentException("Unknown slider");
        }
    }
    public static int positions(CustomDungeonRules.Slider slider) {return (slider.max-slider.min)/slider.step();}
    public static int value(CustomDungeonRules.Slider slider,int position) {
        if(position<0 || position>positions(slider))throw new IllegalArgumentException("Invalid slider position");
        return slider.min+position*slider.step();
    }
    public static int position(CustomDungeonRules.Slider slider,int value) {
        if(!slider.valid(value))throw new IllegalArgumentException("Invalid slider value");
        return (value-slider.min)/slider.step();
    }
    public static String valueText(CustomDungeonRules.Slider slider,int value) {
        if(!slider.valid(value))throw new IllegalArgumentException("Invalid slider value");
        return slider==CustomDungeonRules.Slider.UPGRADE_SCROLLS
            ? String.format(Locale.ROOT,"%d (%.2f%%)",value,value*100.0/15.0) : value+"%";
    }
    /** Alpha assembly checkpoint: never offer unfinished effects as working gameplay controls. */
    public static boolean gameplayImplemented(CustomDungeonRules.Slider slider) {
        switch(slider) {
            case PLAYER_DAMAGE: case ENEMY_DAMAGE: case BOSS_DAMAGE: case BOSS_HP:
            case FOOD: case HEALING: case EQUIPMENT: return false;
            default: return true;
        }
    }
}
