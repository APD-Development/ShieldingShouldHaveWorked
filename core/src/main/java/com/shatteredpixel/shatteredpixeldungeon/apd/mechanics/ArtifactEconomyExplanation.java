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
package com.shatteredpixel.shatteredpixeldungeon.apd.mechanics;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.RingItemEffects;

/** Describes existing resource rules. Never ticks a charger, discovers traps, finds ghosts or spends a charge. */
public final class ArtifactEconomyExplanation {
    private ArtifactEconomyExplanation(){}
    private static String n(float x){return ObfuscationExplanation.number(x);}
    public static float baseGain(Artifact item){
        if(!item.isIdentified())return Float.NaN;
        int level=item.level(),missing=item.maximumCharges()-item.currentCharges();
        if(item instanceof AlchemistsToolkit)return 2+level;
        if(item instanceof HornOfPlenty)return (.25f+.125f*level)*5;
        if(item instanceof MasterThievesArmband)return 3+.15f*level;
        if(item instanceof SandalsOfNature)return (3f+2*level)/6f;
        if(item instanceof CloakOfShadows||item instanceof HolyTome){float adjusted=missing;if(level>7)adjusted+=5*(level-7)/3f;return 1/(45-adjusted);}
        if(item instanceof TimekeepersHourglass)return 1/(90f-missing*3f);
        if(item instanceof UnstableSpellbook)return 1/(120f-missing*5f);
        if(item instanceof SkeletonKey)return 1/(120f-missing*7.5f);
        if(item instanceof LloydsBeacon)return 1/(100f-missing*10f);
        if(item instanceof TalismanOfForesight)return .05f+level*.005f;
        if(item instanceof DriedRose)return 1/5f;
        if(item instanceof EtherealChains)return 1/(40f-(5+2*level-item.currentCharges())*2f);
        return Float.NaN;
    }
    private static float rechargeFactor(Hero hero){
        Integer energy=RingItemEffects.knownBonus(hero,RingOfEnergy.class,true);if(energy==null)return Float.NaN;
        float factor=RingOfEnergy.ringChargeMultiplier(energy);
        if(hero.heroClass!=HeroClass.ROGUE&&hero.hasTalent(Talent.LIGHT_CLOAK))factor*=1+.2f*hero.pointsInTalent(Talent.LIGHT_CLOAK)/3f;
        return factor;
    }
    public static float knownGain(Artifact item,Hero hero){
        float base=baseGain(item);if(item instanceof LloydsBeacon)return base;
        float result=base*rechargeFactor(hero);
        if(hero!=null&&!item.isEquipped(hero)){
            if(item instanceof CloakOfShadows)result*=.75f*hero.pointsInTalent(Talent.LIGHT_CLOAK)/3f;
            if(item instanceof HolyTome)result*=.75f*hero.pointsInTalent(Talent.LIGHT_READING)/3f;
        }
        return result;
    }
    private static String units(Artifact item){
        if(item instanceof AlchemistsToolkit)return "energy points per whole hero-level of qualifying experience";
        if(item instanceof HornOfPlenty||item instanceof MasterThievesArmband)return "charge units per whole hero-level of qualifying experience";
        if(item instanceof SandalsOfNature)return "resource-meter points per eligible grass tile trampled";
        if(item instanceof TalismanOfForesight||item instanceof DriedRose)return "resource-meter points per normal recharge tick";
        return "charge units per normal recharge tick";
    }
    public static String summary(Artifact item,Hero hero){
        if(!item.isIdentified())return "";
        if(item.cursed){
            if(item instanceof CloakOfShadows||item instanceof HolyTome||item instanceof LloydsBeacon||item instanceof CapeOfThorns)
                return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(item,"desc_cursed");
            // These individually reviewed artifacts already explain their curse in their worn description.
            if(item instanceof TimekeepersHourglass||item instanceof UnstableSpellbook||item instanceof MasterThievesArmband||item instanceof SandalsOfNature
                    ||item instanceof DriedRose||item instanceof EtherealChains||item instanceof HornOfPlenty||item instanceof ChaliceOfBlood||item instanceof TalismanOfForesight
                    ||item instanceof AlchemistsToolkit||item instanceof SkeletonKey)
                return item.isEquipped(hero)?"":com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(item,"desc_cursed");
            return cursed(item);
        }
        float base=baseGain(item),known=knownGain(item,hero);if(!Float.isFinite(base)||base<=0)return "";
        if(hero!=null&&hero.buff(MagicImmune.class)!=null&&!(item instanceof LloydsBeacon))return "Magic immunity prevents recharging.";
        if(item instanceof EtherealChains&&item.currentCharges()>=5+2*item.level())return "Natural recharge stops at _"+(5+2*item.level())+" charges_. Gaining experience can add more.";
        if(item.maximumCharges()>0&&item.currentCharges()>=item.maximumCharges())return "Fully charged: _"+item.currentCharges()+"/"+item.maximumCharges()+"_.";
        float gain=Float.isFinite(known)?known:base;
        if(gain<=0)return "Equip this artifact to recharge it.";
        String qualifier=Float.isFinite(known)?"":" before ring bonuses";
        String value=com.shatteredpixel.shatteredpixeldungeon.messages.Messages.decimalFormat("0.##",gain);
        if(item instanceof AlchemistsToolkit)return "Gaining a full experience level provides _"+value+" energy_"+qualifier+".";
        if(item instanceof HornOfPlenty||item instanceof MasterThievesArmband)return "Gaining a full experience level provides _"+value+" charges_"+qualifier+".";
        if(item instanceof SandalsOfNature)return "Trampling tall grass restores _"+value+"% charge_"+qualifier+".";
        String time=com.shatteredpixel.shatteredpixeldungeon.messages.Messages.decimalFormat("0.##",1f/gain);
        String recharge="At the current charge, restores _"+(item instanceof TalismanOfForesight||item instanceof DriedRose?"1% charge":"1 charge")+"_ in about _"+time+" turns_"+qualifier+" while recharging is active.";
        if(item instanceof EtherealChains)recharge+=" Natural recharge stops at _"+(5+2*item.level())+" charges_; experience can add more.";
        return recharge;
    }
    private static String cursed(Artifact item){
        if(item instanceof DriedRose)return "While this cursed Rose's passive effect is equipped, each normal tick has a 1% chance to try spawning a wraith on a valid adjacent tile. No valid tile means no spawn. Normal uncursed recharge is disabled.";
        if(item instanceof EtherealChains)return "While these cursed Chains' passive effect is equipped, each normal tick has a 1% chance to offer 10 turns of Cripple, before resistance. Normal uncursed charging is disabled.";
        if(item instanceof TimekeepersHourglass)return "While this cursed Hourglass's passive effect is equipped, each normal tick has a 10% chance to make the hero spend an extra unit of base action time. Slow/Speed can modify that time; normal uncursed recharge is disabled.";
        if(item instanceof MasterThievesArmband)return "While this cursed Armband's passive effect is equipped and you have gold, each normal tick has a 20% chance to remove one gold coin. It cannot steal gold below zero. Normal theft and charging are unavailable.";
        if(item instanceof HornOfPlenty)return "While its cursed passive is active, ordinary food's nourishment is multiplied by 0.67 after the On Diet adjustment. This reduces nourishment, not eating time. Normal Horn charging is disabled.";
        if(item instanceof SandalsOfNature)return "While the cursed footwear's Naturalism is active, its normal seed/runestone and dew rolls from trampled grass are suppressed. This does not universally cancel separate berry/talent drops. Normal footwear charging is disabled.";
        if(item instanceof ChaliceOfBlood)return "A cursed Chalice multiplies the natural-regeneration delay by 1.5, so that stage is slower rather than faster. Other hunger, regeneration and healing rules still apply. Normal pricking and direct artifact-recharge healing are unavailable.";
        if(item instanceof UnstableSpellbook)return "Its cursed passive prevents reading ordinary scrolls except Remove Curse and Anti-magic. Other reading restrictions still apply. Normal Spellbook reading, infusion and recharging are unavailable.";
        if(item instanceof CapeOfThorns)return "This legacy Cape uses its own stored-damage and active-deflection rules. No universal cursed-artifact shutdown is assumed; the ordinary Cape behavior above and its current state determine the result.";
        return "This artifact is cursed. Its normal action/recharging paths can be disabled as described above; no uncursed numerical benefit is claimed active.";
    }
    public static String details(Artifact item,Hero hero){
        if(!item.isIdentified())return "";if(item.cursed)return cursed(item);
        int level=item.level(),missing=item.maximumCharges()-item.currentCharges();String extra="";
        if(item instanceof AlchemistsToolkit)extra="Stores "+item.currentCharges()+" alchemy energy. A full hero-level of experience provides 2 + Toolkit level = "+(2+level)+" energy before recharge bonuses. Energize spends 6 energy per upgrade, up to +10.\n\n"+(level==10?"Maximum upgrade reached: it becomes ready on the next warm-up update.":"Equipping starts a warm-up. After its first setup update, a full warm-up takes (10 − Toolkit level)² = "+((10-level)*(10-level))+" turns at this level. Partial warm-up progress is kept if you upgrade it. The icon's percentage shows warm-up progress, not stored energy.");
        else if(item instanceof EtherealChains)extra="Stored chain charges: "+item.currentCharges()+". Soft recharge target = 5 + 2 × upgrade steps = "+(5+2*level)+" charges. Passive base gain = 1 / (40 − 2 × (target − current charges)) per tick while below that target. Experience can add 6 × hero-level fraction charges; above the target, that experience gain is multiplied by target / current charges. External recharge stops being accepted at twice the soft target. These are distinct limits, not a single hard capacity.";
        else if(item instanceof CloakOfShadows||item instanceof HolyTome)extra="Base turns per new charge = 45 − missing whole charges − extra high-level bonus, where the extra bonus is 5 × (upgrade steps − 7) / 3 above step7 and zero otherwise. Current base interval: "+n(1/baseGain(item))+" turns. Recharge bonuses divide this interval. "+(item instanceof CloakOfShadows?"The Cloak passively recharges only when not cloaked. Unworn Light Cloak charging multiplies gain by 0.75 × talent rank / 3. Invisibility normally uses a charge every four cloak ticks and can end early on attacks or magic.":"Unworn Light Reading charging multiplies gain by 0.75 × talent rank / 3. Each spell has its own eligibility, charge cost and effect; recharge does not reduce those costs.");
        else if(item instanceof TimekeepersHourglass)extra="Base turns per passive charge = 90 − 3 × missing charges = "+n(90f-3*missing)+" turns before recharge bonuses. Stasis spends up to two charges for five turns per charge while its separate stasis rules run. Freeze uses a two-unit action-time budget per charge; its saved partial budget and a 0.001 rounding tolerance govern exactly when another charge is spent. A resource count is not the same number of free attacks.";
        else if(item instanceof UnstableSpellbook)extra="Base turns per passive charge = 120 − 5 × missing charges = "+n(120f-5*missing)+" turns before recharge bonuses. Reading spends one charge and invokes a selected/random scroll effect under the book's selection rules; it does not grant every scroll effect at once. Feeding an eligible requested scroll consumes it and normally costs two turns. Capacity changes with upgrades; the actual current capacity is shown above.";
        else if(item instanceof SkeletonKey)extra="Base turns per passive charge = 120 − 7.5 × missing charges = "+n(120f-7.5f*missing)+" turns before recharge bonuses. Key use, wall effects and inserted-key upgrades have separate target/charge requirements.";
        else if(item instanceof LloydsBeacon)extra="Base turns per passive charge = 100 − 10 × missing charges = "+n(100f-10*missing)+" turns. This legacy passive formula does not use Ring of Energy. Marked return and targeted/random teleportation use their own destination and charge rules; charging does not permit an illegal return.";
        else if(item instanceof TalismanOfForesight)extra="Passive recharge = 0.05 + 0.005 × upgrade level = "+n(.05f+.005f*level)+" percentage points per turn before recharge bonuses, while uncursed and natural recovery is active. Its 100% meter is charge, not a chance to detect a secret.";
        else if(item instanceof SandalsOfNature)extra="Each eligible grass trample gives (3 + 2 × growth steps) / 6 = "+n((3f+2*level)/6)+" base meter points before recharge bonuses. These are growth steps, currently "+level+" / "+item.maximumUpgradeSteps()+", rather than the normalized displayed +level. Feeding distinct eligible seeds advances growth after 3 + 3 × growth steps seed types, until maximum growth. The selected seed's ability has its own displayed meter cost and target limit. Extra grass seed/dew chances use Naturalism and still obey trinket/floor rules; they are separate from this charging rate.";
        else if(item instanceof MasterThievesArmband)extra="_Enemy theft_\nCosts 1 charge and no time. Blinds and cripples for "+(3+level/2)+" turns, or "+(5+level/2)+" when the enemy is unaware. Loot chance = the enemy's usual loot chance × "+n(1+.1f*level)+", plus another 0.5 to that factor when unaware. An enemy offers only one attempt; enemies you have outleveled may have no loot.\n\n_Shop theft_\nEach charge covers "+n(10+level/2f)+" gold of the item's base value: 10 + level / 2. Uses only enough charges to cover that value, limited by your available charges. Success chance = min(100%, charges used × value per charge / item value × 100%). Charges are spent only on success. Check the shop's chance before stealing; failure makes the shopkeeper flee.";
        else if(item instanceof DriedRose)extra="Without a living summoned ghost, base recharge is 0.2 meter points per eligible tick toward its100-point meter. With a living ghost, that path instead heals the ghost at base ghost maximum HP / 500 HP per eligible tick, before recharge bonuses and accumulation. It does not simultaneously refill summon charge and heal a living ghost. Petals and ghost equipment affect other statistics, not this base meter rate. Its accumulated whole meter/healing points are credited when progress is greater than one, so an exact boundary can wait until a later tick.";
        else if(item instanceof CapeOfThorns)extra="External artifact-recharge power adds round(4 × power) meter points while inactive. The Cape's own incoming-damage charging and active-duration rules above remain separate.";
        else if(item instanceof ChaliceOfBlood)extra="External artifact-recharge power can offer base healing of 5 × supplied power / current uncursed healing delay, with fractional amounts randomly rounded and normal missing-HP/healing rules applied. It does not bypass starvation.";
        String external="";
        if(item instanceof AlchemistsToolkit||item instanceof CloakOfShadows||item instanceof HolyTome||item instanceof HornOfPlenty||item instanceof TimekeepersHourglass||item instanceof LloydsBeacon)external="0.25 charge/energy unit";
        else if(item instanceof UnstableSpellbook||item instanceof MasterThievesArmband)external="0.1 charge unit";
        else if(item instanceof SkeletonKey)external="0.133 charge unit";
        else if(item instanceof EtherealChains)external="0.5 chain charge";
        else if(item instanceof SandalsOfNature||item instanceof TalismanOfForesight)external="2 meter points";
        else if(item instanceof DriedRose)external="4 meter points when no ghost is active; with a living ghost, round((1 + upgrade steps / 3) × supplied power) HP instead";
        String value=summary(item,hero);if(!external.isEmpty())extra+="\n\nOne unit of external artifact-recharge power offers "+external+", subject to that artifact's eligibility and resource limits. Cloak/Tome unworn talent modifiers also apply to their external recharge. This is not a universal one-charge-per-turn effect.";
        return ("_Resource and recharge calculation_\n"+value+"\n\n"+extra+"\n\nUpgrade steps used by these formulas: "+level+" / "+item.maximumUpgradeSteps()+"; the normalized displayed equipment level is +"+item.visiblyUpgraded()+". A full meter and disabled regeneration/activation conditions can pause the applicable path.").trim();
    }
}
