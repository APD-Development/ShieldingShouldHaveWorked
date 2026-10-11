package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.apd.hotbar.ClericQuickCasts;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndClericQuickCasts;
import com.watabou.noosa.Image;

/** The original SPD-sized blue spell tag, repeated vertically without changing the item hotbar. */
public final class ClericQuickCastTag extends Tag implements ActionIndicator.Action {
    private final int slot;
    private ClericSpell shown;
    private Image icon;
    private Image plus;
    private RenderedTextBlock number;

    public ClericQuickCastTag(int slot) {
        super(0x002157);
        this.slot=slot;
        plus=Icons.PLUS.get(); add(plus);
        number=PixelScene.renderTextBlock(Integer.toString(slot+1),5); add(number);
        setSize(SIZE,SIZE);
        visible=false;
    }
    public static int count() {
        if(Dungeon.hero==null || !Dungeon.hero.isAlive()) return 0;
        HolyTome tome=Dungeon.hero.belongings.getItem(HolyTome.class);
        return tome!=null && (tome.isEquipped(Dungeon.hero) || Dungeon.hero.hasTalent(Talent.LIGHT_READING))
                ? SPDSettings.apdClericQuickSlots() : 0;
    }
    @Override public void update() {
        super.update();
        visible=slot<count();
        active=visible;
        if(!visible) return;
        HolyTome tome=Dungeon.hero.belongings.getItem(HolyTome.class);
        ClericSpell spell=ClericQuickCasts.saved(slot,tome);
        if(shown!=spell) {
            if(icon!=null) { icon.killAndErase(); icon.destroy(); icon=null; }
            shown=spell;
            if(spell!=null) { icon=new HeroIcon(this); add(icon); }
            layout();
        }
        plus.visible=spell==null;
        boolean usable=ClericQuickCasts.unlocked(Dungeon.hero,spell) && !tome.cursed
                && tome.canCast(Dungeon.hero,spell) && Dungeon.hero.ready;
        if(icon!=null) icon.alpha(usable ? 1f : 0.35f);
        plus.alpha(Dungeon.hero.ready ? 1f : 0.35f);
        setColor(indicatorColor());
    }
    @Override protected void layout() {
        super.layout();
        if(plus==null) return;
        float center=flipped ? x+width-SIZE/2f-1 : x+SIZE/2f+1;
        plus.x=center-plus.width()/2f; plus.y=y+(height-plus.height())/2f;
        if(icon!=null) { icon.x=center-icon.width()/2f; icon.y=y+(height-icon.height())/2f; }
        number.setPos(center+6-number.width(),y+height-number.height()-1);
        PixelScene.align(plus); if(icon!=null) PixelScene.align(icon); PixelScene.align(number);
    }
    @Override protected void onClick() {
        super.onClick();
        if(Dungeon.hero==null || !Dungeon.hero.ready) return;
        HolyTome tome=Dungeon.hero.belongings.getItem(HolyTome.class);
        if(ClericQuickCasts.saved(slot,tome)==null) WndClericQuickCasts.edit(slot);
        else doAction();
    }
    @Override protected boolean onLongClick() { WndClericQuickCasts.edit(slot); return true; }
    @Override protected String hoverText() { return actionName(); }
    @Override public String actionName() { return shown==null ? "Choose spell " + (slot+1) : shown.name(); }
    @Override public int actionIcon() { return shown==null ? HeroIcon.NONE : shown.icon()+HeroIcon.SPELL_ACTION_OFFSET; }
    @Override public int indicatorColor() {
        return shown==GuidingLight.INSTANCE && Dungeon.hero!=null && shown.chargeUse(Dungeon.hero)==0 ? 0x0063ff : 0x002157;
    }
    @Override public void doAction() {
        if (!ClericQuickCasts.cast(Dungeon.hero,Dungeon.hero==null ? null : Dungeon.hero.belongings.getItem(HolyTome.class),slot))
            com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(HolyTome.class,"no_spell"));
    }
}
