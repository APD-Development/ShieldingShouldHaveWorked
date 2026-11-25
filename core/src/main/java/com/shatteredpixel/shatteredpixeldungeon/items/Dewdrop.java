/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.apd.recorder.ApdCombat;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Dewdrop extends Item {
	
	{
		image = ItemSpriteSheet.DEWDROP;
		
		stackable = true;
		dropsDownHeap = true;
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {
		
		Waterskin flask = hero.belongings.getItem( Waterskin.class );
		Catalog.setSeen(getClass());
		Statistics.itemTypesDiscovered.add(getClass());
		
		if (flask != null && !flask.isFull()){

			int extra = flask.collectDewReturningExtra( this );
			//APD: drops of a stack that did not fit in the flask are drunk, as when the flask is full;
			// if drinking would do nothing (full HP, no Barrier room) they stay on the tile, as with a full flask
			if (extra > 0) {
				Heap heap = Dungeon.level != null && Dungeon.level.heaps != null ? Dungeon.level.heaps.get(pos) : null;
				if (wouldHelp(hero, extra) || !leaveRest(heap, this, extra)) consumeDew( extra, hero, true );
			}
			GameScene.pickUp( this, pos );

		} else {

			int terr = Dungeon.level.map[pos];
			//APD (Chris, Nov 24): two drops stacked on one tile are worth two, not one
			if (!consumeDew(quantity, hero, terr == Terrain.ENTRANCE || terr == Terrain.ENTRANCE_SP
					|| terr == Terrain.EXIT || terr == Terrain.UNLOCKED_EXIT)){
				return false;
			} else {
				Catalog.countUse(getClass());
			}
			
		}
		
		Sample.INSTANCE.play( Assets.Sounds.DEWDROP );
		hero.spendAndNext( pickupDelay() );
		
		return true;
	}

    /** Base dew value before health caps, Vial allocation and run healing rules. */
    public static int baseDewEffect(Hero hero, int drops) {
        return Math.round(hero.HT * 0.05f * drops);
    }

    public static int shieldingDewCap(Hero hero) {
        return Math.round(hero.HT * 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW));
    }

	/** APD: whether drinking {@code drops} now heals or shields at all (same test consumeDew uses to refuse). */
	public static boolean wouldHelp(Hero hero, int drops){
		int effect = baseDewEffect(hero, drops);
		int shieldRoom = 0;
		if (hero.hasTalent(Talent.SHIELDING_DEW)){
			int cur = hero.buff(Barrier.class) != null ? hero.buff(Barrier.class).shielding() : 0;
			shieldRoom = shieldingDewCap(hero) - cur;
		}
		return helps(hero.HT, hero.HP, effect, shieldRoom);
	}

	public static boolean helpsForTest(int ht, int hp, int effect, int shieldRoom){ return helps(ht, hp, effect, shieldRoom); }
	static boolean helps(int ht, int hp, int effect, int shieldRoom){
		int heal = Math.min(ht - hp, effect);
		int shield = Math.min(effect - Math.max(heal, 0), shieldRoom);
		return heal > 0 || shield > 0;
	}

	/** APD: keeps {@code extra} drops of the picked-up stack on its tile as a new stack behind it
	 *  (the caller then removes {@code dew} itself). False when the stack is not in that heap. */
	public static boolean leaveRest(Heap heap, Dewdrop dew, int extra){
		if (heap == null || extra <= 0 || extra >= dew.quantity || !heap.items.contains(dew)) return false;
		Dewdrop rest = new Dewdrop();
		rest.quantity = extra;
		dew.quantity -= extra;
		heap.items.add(rest);
		return true;
	}

	public static boolean consumeDew(int quantity, Hero hero, boolean force){
		//20 drops for a full heal
		int effect = baseDewEffect(hero, quantity);

		int heal = Math.min( hero.HT - hero.HP, effect );

		int shield = 0;
		if (hero.hasTalent(Talent.SHIELDING_DEW)){

			//When vial is present, this allocates exactly as much of the effect as is needed
			// to get to 100% HP, and the rest is then given as shielding (without the vial boost)
			if (quantity > 1 && heal < effect && VialOfBlood.delayBurstHealing()){
				heal = Math.round(heal/VialOfBlood.totalHealMultiplier());
			}

			shield = effect - heal;

			int maxShield = shieldingDewCap(hero);
			int curShield = 0;
			if (hero.buff(Barrier.class) != null) curShield = hero.buff(Barrier.class).shielding();
			shield = Math.min(shield, maxShield-curShield);
		}

		if (heal > 0 || shield > 0) {

			if (heal > 0 && quantity > 1 && VialOfBlood.delayBurstHealing()){
				Healing healing = Buff.affect(hero, Healing.class);
				healing.setHeal(heal, 0, VialOfBlood.maxHealPerTurn(), true);
			} else {
				int apdHpBefore = hero.HP;
				heal = Math.min(hero.HT - hero.HP, com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomHealingHooks.amount(hero, effect));
				hero.HP += heal;
				ApdCombat.onHeal( hero, apdHpBefore, ApdCombat.HEAL_PLANT_DEWDROP, heal );
				if (heal > 0){
					hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
				}
			}

			if (shield > 0) {
				Buff.affect(hero, Barrier.class).incShield(shield);
				hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING );
			}

		} else if (!force) {
			GLog.i( Messages.get(Dewdrop.class, "already_full") );
			return false;
		}

		return true;
	}

    @Override
    public String desc() {
        if (Dungeon.hero != null && com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomHealingHooks.disabled()) {
            return Messages.get(this, "desc_no_healing");
        }
        return super.desc();
    }

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	//APD (Chris, Nov 24): a drop landing on another drop makes a stack of 2 (the base game kept only 1)

}

