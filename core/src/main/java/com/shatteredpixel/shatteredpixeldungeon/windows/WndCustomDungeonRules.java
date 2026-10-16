package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRulesPresentation;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.WindowGeometry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

/** Native controls edit next-run preferences, never the locked active-run configuration. */
public class WndCustomDungeonRules extends Window {
    private CustomDungeonRules draft;
    private final RenderedTextBlock scoringNotice;
    private final ScrollPane scroll;
    private final RenderedTextBlock combined;
    public WndCustomDungeonRules() {this(false);}
    public WndCustomDungeonRules(final boolean currentRun) {
        super();
        draft=currentRun ? Dungeon.customRules() : SPDSettings.apdCustomRules();
        int width=WindowGeometry.width(PixelScene.landscape(),PixelScene.uiCamera.width);
        RenderedTextBlock title=PixelScene.renderTextBlock("Custom Dungeon Rules",9);
        title.maxWidth(width);title.hardlight(TITLE_COLOR);title.setPos(0,0);add(title);
        RenderedTextBlock experimentalHeading=PixelScene.renderTextBlock(CustomRulesPresentation.EXPERIMENTAL_HEADING,6);
        experimentalHeading.maxWidth(width);experimentalHeading.hardlight(TITLE_COLOR);
        experimentalHeading.setPos(0,title.bottom()+4);add(experimentalHeading);
        combined=PixelScene.renderTextBlock("",6);combined.maxWidth(width);combined.hardlight(TITLE_COLOR);
        combined.setPos(0,experimentalHeading.bottom()+3);add(combined);updateCombined();
        float above=combined.bottom()+4;
        // Create the scroll touch area before controls, matching WndAPDSettings' touch ordering.
        Component content=new Component();scroll=new ScrollPane(content);add(scroll);
        // The full warning leads the same scroll area, so narrow viewports retain room for controls.
        scoringNotice=PixelScene.renderTextBlock(CustomRulesPresentation.noticeText(),6);
        scoringNotice.maxWidth(width);scoringNotice.setPos(0,0);content.add(scoringNotice);
        float y=scoringNotice.bottom()+4;
        if(draft==null) {
            RenderedTextBlock unknown=PixelScene.renderTextBlock("This run's custom settings were not recorded.",6);
            unknown.maxWidth(width);unknown.setPos(0,y);content.add(unknown);y=unknown.bottom()+4;
        } else {
            RenderedTextBlock context=PixelScene.renderTextBlock(currentRun ? "Current run — locked" : "Next run only.",6);
            context.maxWidth(width);context.setPos(0,y);content.add(context);y=context.bottom()+4;
            for(final CustomDungeonRules.Slider rule:CustomDungeonRules.Slider.values()) {
                final RenderedTextBlock caption=PixelScene.renderTextBlock(caption(rule),6);
                caption.maxWidth(width);caption.setPos(0,y);content.add(caption);y+=22;
                OptionSlider slider=new OptionSlider("",CustomRulesPresentation.valueText(rule,rule.min),
                        CustomRulesPresentation.valueText(rule,rule.max),0,CustomRulesPresentation.positions(rule)) {
                    @Override protected void onChange() {
                        draft=draft.with(rule,CustomRulesPresentation.value(rule,getSelectedValue()));
                        caption.text(caption(rule));caption.maxWidth((int)width());updateCombined();
                    }
                };
                slider.scrollVerticallyWith(scroll);slider.liveChanges(true);
                slider.setRect(0,y,width,22);
                slider.setSelectedValue(CustomRulesPresentation.position(rule,draft.get(rule)));
                slider.enable(!currentRun && CustomRulesPresentation.gameplayImplemented(rule));
                content.add(slider);y=slider.bottom()+4;
            }
        }
        content.setSize(width,y);
        float paneHeight=WindowGeometry.paneHeight(y,WindowGeometry.maxPane(PixelScene.uiCamera.height,above,26));
        resize(width,(int)Math.ceil(above+paneHeight+26));scroll.setRect(0,above,width,paneHeight);
        RedButton finish=new RedButton(currentRun?"Close":"Save for new runs",6) {
            @Override protected void onClick() {
                if(!currentRun && draft!=null)SPDSettings.apdCustomRules(draft);
                hide();
            }
        };
        finish.setRect(0,above+paneHeight+4,width,22);add(finish);
    }
    private void updateCombined(){
        combined.text("Combined Custom Rules Multiplier\n"+(draft==null?"Not recorded":com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRuleMultipliers.format(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRuleMultipliers.combined(draft))));
    }
    private String caption(CustomDungeonRules.Slider rule) {
        return CustomRulesPresentation.label(rule)+": "+CustomRulesPresentation.valueText(rule,draft.get(rule))
            +" · "+CustomRulesPresentation.multiplierText(rule,draft.get(rule));
    }
}
