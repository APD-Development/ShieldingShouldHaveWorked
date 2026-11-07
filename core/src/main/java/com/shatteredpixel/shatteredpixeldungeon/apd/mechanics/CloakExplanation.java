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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;

/** Public known-item state only. Does not activate, tick, recharge or upgrade a Cloak. */
public final class CloakExplanation {
    private CloakExplanation() {}
    public static int experiencePerPayment(CloakOfShadows cloak,Hero hero){
        if(!cloak.isIdentified()||hero==null)return -1;
        int level=cloak.level(),difference=hero.lvl-(1+2*level);
        if(level>=7)difference-=level-6;
        return (int)Math.round(10f*Math.pow(difference>=0?1.1f:0.75f,Math.abs(difference)));
    }
    public static String summary(CloakOfShadows cloak){
        if(!cloak.isIdentified())return "";
        return "Makes you invisible while active. Activating stealth takes 1 base game-time unit. While active, its charge countdown normally resets to 4 scheduled updates after each paid charge. Invisibility is not damage immunity, and actions that break invisibility can end it early. Ordinary passive recharging pauses while cloaked.";
    }
    public static String details(CloakOfShadows cloak,Hero hero){
        if(!cloak.isIdentified())return "";
        int remaining=cloak.remainingStealthUpdates(),level=cloak.level();
        String text="_Cloak stealth and charge use_\n"+summary(cloak)
            +" Activation requires a charge, no curse and no Magic Immunity, and normally requires wearing the Cloak; Light Cloak permits carried use. The activation time still advances normal game consequences. Turning it off uses its existing no-extra-time path."
            +"\nA fresh stealth effect starts with its first charge payment due on its first scheduled update. Each active update subtracts 1 from the countdown. When it reaches zero, 1 charge is spent and the countdown resets to 4 if that payment succeeds. When no charge remains at a due payment, stealth ends and interrupts the hero. The displayed charge count is not that many free attacks or real-world seconds."
            +(remaining<0?"\nThis Cloak is not currently sustaining stealth.":remaining==0?"\nThe next scheduled stealth update requires a charge payment.":"\nCurrent countdown: "+remaining+" scheduled updates until the next charge payment.")
            +"\nAn ordinary invisibility-breaking action dispels the effect. If a charge payment is already due and a charge is available, that dispel spends 1 charge; otherwise it does not add a separate charge cost. A separate source of invisibility can remain active. Manual deactivation and involuntary dispelling are separate paths."
            +"\nSuccessful paid stealth updates provide Cloak upgrade progress, not hero XP. Target hero level = 1 + 2 × Cloak upgrade level, plus (upgrade level − 6) above step6. Difference = hero level − target. Progress per payment = round(10 × 1.1^difference) for nonnegative difference; otherwise round(10 × 0.75^(−difference))."
            +(hero==null?" Current personal progress is withheld without a loaded hero.":" At your current level, a successful paid update offers "+experiencePerPayment(cloak,hero)+" Cloak progress points.")
            +(level>=cloak.maximumUpgradeSteps()?" The Cloak is already at its maximum upgrade.":" The next upgrade threshold is (current upgrade level + 1) × 50 = "+((level+1)*50)+" progress points. A payment performs one upgrade check and retains the remainder.")
            +" Upgrading increases charge capacity by 1, capped at 10; the actual saved capacity is displayed above. Capacity is not an immediate grant of that many charges. Ordinary and external recharging keep their separate eligibility, rate and capacity rules. Inspection does not spend charge, advance the countdown or earn progress.";
        return text;
    }
}
