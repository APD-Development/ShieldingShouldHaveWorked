package com.shatteredpixel.shatteredpixeldungeon.apd;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/** Read-only Arcana contribution. Never call genericProcChanceMultiplier here:
 * weapon calculations can detach one-use combat buffs. Other bonuses are deliberately excluded.
 */
public final class ArcanaInfoDisplay {
    private ArcanaInfoDisplay() {}

    private static float visibleMultiplier(Hero hero) {
        if (hero == null || hero.buff(MagicImmune.class) != null) return Float.NaN;
        boolean equipped = false;
        int bonus = 0;
        for (Item item : new Item[]{hero.belongings.ring(), hero.belongings.misc()}) {
            // Hide for any unknown ring, so suppression itself cannot reveal its type.
            if (item instanceof Ring && !item.isIdentified()) return Float.NaN;
            if (item instanceof RingOfArcana) {
                // Suppress combined totals if ANY contributing ring has hidden identity/level/curse.
                equipped = true;
                bonus += ((RingOfArcana)item).soloBuffedBonus();
            }
        }
        // Core Ring.getBuffedBonus uses Spirit Form only when the regular bonus is zero.
        // Its ring() getter updates the cached item level, so preview through the read-only accessor.
        if (equipped && bonus == 0) {
            com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff spirit =
                    hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm.SpiritFormBuff.class);
            if (spirit != null) bonus = spirit.previewRingBonus(hero, RingOfArcana.class);
        }
        return equipped ? (float)Math.pow(1.175f, bonus) : Float.NaN;
    }

    private static String number(float value) {
        return Messages.decimalFormat(Math.abs(value) >= 1000000 ? "0.##E0" : "0.##", value);
    }
    private static String text(String key, Object... values) {
        return Messages.get(ArcanaInfoDisplay.class, key, values);
    }
    private static String power(float multi) {
        return text("power", number(multi), number(100f*(multi-1f)));
    }
    private static String metric(String key, float before, float after) {
        return text(key, number(before), number(after));
    }
    private static String chance(float base, float multi) {
        // Probability saturates at 100%; excess is effect strength, never a >100% chance.
        return text("chance", number(100f*Math.max(0, Math.min(1, base))),
                number(100f*Math.max(0, Math.min(1, base*multi))));
    }

    public static String weapon(Weapon weapon) {
        Hero hero = Dungeon.hero;
        float multi = visibleMultiplier(hero);
        if (Float.isNaN(multi) || weapon == null || weapon.enchantment == null
                || (weapon.enchantment.curse() && !weapon.cursedKnown)
                || !weapon.hasEnchant(weapon.enchantment.getClass(), hero)) return "";
        if (!Float.isFinite(multi)) return "\n\n" + text("unavailable");
        String result = power(multi);
        String name = weapon.enchantment.getClass().getSimpleName();
        String pkg = weapon.enchantment.getClass().getPackage().getName();
        if (!pkg.equals("com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments")
                && !pkg.equals("com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses")) {
            return "\n\n" + text("custom", number(multi));
        }
        if (name.equals("Kinetic")) return "\n\n" + result + " " + text("kinetic");
        if (name.equals("Crystal")) return "\n\n" + metric("magic", 25, 25*multi);
        if (name.equals("Projecting")) {
            if (weapon instanceof MissileWeapon) {
                // MissileWeapon.throwPos uses four times enchantment power
                // for its wall-penetrating target radius, not melee reach.
                return "\n\n" + metric("missile_reach", 4, Math.round(4f*multi));
            }
            int base=weapon.RCH;
            int normal=Projecting.additionalReach(1f,base);
            int arcana=Projecting.additionalReach(multi,base);
            String details="\n\n" + metric("reach",normal,arcana)
                    + " " + text("total_reach",base+arcana);
            if (weapon instanceof RunicBlade && weapon.levelKnown && hero.heroClass == HeroClass.DUELIST) {
                float slash=((RunicBlade)weapon).runicSlashEnchantBoost();
                int slashBonus=Projecting.additionalReach(multi+slash,base);
                details += " " + text("runic_reach",number(100f*slash),base+slashBonus);
            }
            return details;
        }
        if (name.equals("Unstable")) return "\n\n" + result + " " + text("unstable");
        if (name.equals("Explosive")) return "\n\n" + result + " " + text("explosive");
        if (name.equals("Vampiric")) {
            float base = hero.HT > 0 ? 0.05f + 0.25f*(hero.HT-hero.HP)/(float)hero.HT : 0.05f;
            return "\n\n" + result + " " + chance(base, multi) + " " + text("current_hp");
        }
        if (!weapon.levelKnown) return "\n\n" + result + " " + text("unknown_level");
        float level = Math.max(0, weapon.buffedLvl()), base;
        switch (name) {
            case "Blazing": case "Blooming": base=(level+1)/(level+3); break;
            case "Chilling": base=(level+1)/(level+4); break;
            case "Shocking": case "Venomous": base=1/3f; break;
            case "Blocking": case "Lucky": base=(level+4)/(level+40); break;
            case "Eldritch": case "Elastic": base=(level+1)/(level+5); break;
            case "Vorpal": case "Wayward": base=1/4f; break;
            case "Corrupting": base=(level+5)/(level+25); break;
            case "Grim": return "\n\n" + result + " " + text("grim");
            case "Annoying": base=1/20f; break;
            case "Displacing": base=1/12f; break;
            case "Dazzling": case "Friendly": base=1/10f; break;
            case "Polarized": base=1/2f; break;
            case "Pressurized": case "Wondrous": base=1/8f; break;
            case "Sacrificial": return "\n\n" + result + " " + text("sacrificial");
            default: return "\n\n" + text("custom", number(multi));
        }
        return "\n\n" + result + " " + chance(base, multi);
    }

    public static String armor(Armor armor) {
        Hero hero = Dungeon.hero;
        float multi = visibleMultiplier(hero);
        if (Float.isNaN(multi) || armor == null || armor.glyph == null
                || (armor.glyph.curse() && !armor.cursedKnown)
                || !armor.hasGlyph(armor.glyph.getClass(), hero)) return "";
        if (!Float.isFinite(multi)) return "\n\n" + text("unavailable");
        String name=armor.glyph.getClass().getSimpleName(), result=power(multi);
        String pkg=armor.glyph.getClass().getPackage().getName();
        if (!pkg.equals("com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs")
                && !pkg.equals("com.shatteredpixel.shatteredpixeldungeon.items.armor.curses")) {
            return "\n\n" + text("custom", number(multi));
        }
        if (name.equals("Brimstone")) return "\n\n" + text("brimstone");
        if (!armor.levelKnown) return "\n\n" + result + " " + text("unknown_level");
        float actualLevel=armor.buffedLvl(), level=Math.max(0, actualLevel), base;
        if (actualLevel == -1 && (name.equals("Obfuscation") || name.equals("Swiftness")
                || name.equals("Flow") || name.equals("Camouflage") || name.equals("AntiMagic")
                || name.equals("Bulk"))) return "\n\n" + text("inactive");
        switch (name) {
            case "Obfuscation": return "\n\n" + metric("stealth", 1+actualLevel/3f, (1+actualLevel/3f)*multi);
            case "Swiftness": return "\n\n" + metric("speed", 1.2f+0.04f*actualLevel, (1.2f+0.04f*actualLevel)*multi) + " " + text("swiftness");
            case "Flow": return "\n\n" + metric("speed", 2+0.5f*actualLevel, (2+0.5f*actualLevel)*multi) + " " + text("water");
            case "Camouflage": return "\n\n" + metric("turns", Math.round(3+actualLevel/2f), Math.round((3+actualLevel/2f)*multi));
            case "AntiMagic": return "\n\n" + text("magic_dr", number(Math.round(actualLevel)), number(Math.round(3+1.5f*actualLevel)), number(Math.round(actualLevel*multi)), number(Math.round((3+1.5f*actualLevel)*multi)));
            case "Stone": return "\n\n" + result + " " + text("stone");
            case "Bulk": return "\n\n" + metric("speed", 1/3f, multi/3f) + " " + text("door");
            case "Viscosity": return "\n\n" + result + " " + text("viscosity", number((level+1)/(level+6)*multi));
            case "Potential": base=(level+1)/(level+6); break;
            case "Entanglement": base=1/4f; break;
            case "Repulsion": base=(level+1)/(level+5); break;
            case "Affection": base=(level+3)/(level+20); break;
            case "Thorns": base=(level+2)/(level+12); break;
            case "AntiEntropy": case "Stench": base=1/8f; break;
            case "Corrosion": base=1/10f; break;
            case "Displacement": case "Multiplicity": case "Overgrowth": base=1/20f; break;
            case "Metabolism": base=1/6f; break;
            default: return "\n\n" + text("custom", number(multi));
        }
        return "\n\n" + result + " " + chance(base, multi);
    }
}
