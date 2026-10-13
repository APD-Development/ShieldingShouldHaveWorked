package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Random;
import java.util.*;

/** Explicit ordinary-generation boundaries only. Never wrap general Level.drop or player items. */
public final class CustomEquipmentHooks {
    private CustomEquipmentHooks() { }
    public interface Factory { Item create(); }
    public static boolean eligible(Item item) {
        return item instanceof Weapon || item instanceof Armor || item instanceof Ring || item instanceof Artifact;
    }
    public static List<com.shatteredpixel.shatteredpixeldungeon.items.Heap> dropGenerated(
            com.shatteredpixel.shatteredpixeldungeon.levels.Level level, Item original, int cell,
            com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type type, boolean haunted) {
        List<com.shatteredpixel.shatteredpixeldungeon.items.Heap> heaps=new ArrayList<>();
        for(Item item:adjust(original,()->extraLike(original))){
            com.shatteredpixel.shatteredpixeldungeon.items.Heap heap=level.drop(item,cell);
            heaps.add(heap);
        }
        // Add the complete batch before sealing a chest. Level.drop reroutes drops into an
        // already sealed crystal chest and can exhaust every adjacent placement cell.
        for(com.shatteredpixel.shatteredpixeldungeon.items.Heap heap:heaps){
            if(haunted)heap.setHauntedIfCursed();
            if(type!=null)heap.type=type;
        }
        return heaps;
    }
    public static Item extraLike(Item original) {
        if(original instanceof Artifact)return Generator.random(Generator.Category.ARTIFACT);
        return Generator.random(original.getClass());
    }
    public static List<Item> adjust(Item original, Factory extra) {
        if(original==null)return Collections.emptyList();
        int percent=CustomCombatHooks.percentage(CustomDungeonRules.Slider.EQUIPMENT);
        if(percent==100 || !eligible(original))return Collections.singletonList(original);
        if(percent<100)return Random.Int(100)<percent?Collections.singletonList(original):Collections.<Item>emptyList();
        if(percent<200 && Random.Int(100)>=percent-100)return Collections.singletonList(original);
        Item added=extra.create();
        if(added==null)return Collections.singletonList(original);
        if(added==original)throw new IllegalArgumentException("Equipment expansion must generate a distinct item");
        if(!eligible(added))throw new IllegalArgumentException("Equipment expansion returned a non-equipment item");
        return Arrays.asList(original,added);
    }
}
