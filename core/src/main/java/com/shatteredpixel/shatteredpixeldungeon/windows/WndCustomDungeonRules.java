package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomDungeonRules;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRulePresets;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomScoreSnapshot;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRulesPresentation;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.WindowGeometry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.ColorBlock;

/** Native controls edit next-run preferences, never the locked active-run configuration. */
public class WndCustomDungeonRules extends Window {
    private CustomDungeonRules draft;
    private final boolean currentRun;
    private final CustomScoreSnapshot frozenScoring;
    private final RenderedTextBlock scoringNotice;
    private final ScrollPane scroll;
    private final RenderedTextBlock combined;
    private final OptionSlider[] sliderControls=new OptionSlider[CustomDungeonRules.Slider.values().length];
    private final RenderedTextBlock[] sliderCaptions=new RenderedTextBlock[CustomDungeonRules.Slider.values().length];
    public WndCustomDungeonRules() {this(false);}
    //UI polish (Chris, Nov 22: "too blocky", "All of the above"): three tabs, rule sliders grouped in cards,
    //the long experimental text moved to the Scoring tab. Presentation only: values, presets and saving are unchanged.
    public static final int TAB_RULES=0,TAB_PRESETS=1,TAB_SCORING=2;
    static final String[] TAB_NAMES={"Rules","Presets","Scoring"};
    private final Component[] tabs=new Component[3];
    private final float[] tabHeights=new float[3];
    private final RedButton[] tabButtons=new RedButton[3];
    private int tab=TAB_RULES;
    public WndCustomDungeonRules(final boolean currentRun) {
        super();
        this.currentRun=currentRun;
        frozenScoring=currentRun ? Dungeon.customScoring() : null;
        draft=currentRun ? Dungeon.customRules() : SPDSettings.apdCustomRules();
        final int width=WindowGeometry.width(PixelScene.landscape(),PixelScene.uiCamera.width);
        final int captionWidth=width-6;
        RenderedTextBlock title=PixelScene.renderTextBlock("Custom Dungeon Rules",9);
        title.maxWidth(width);title.hardlight(TITLE_COLOR);title.setPos(0,0);add(title);
        RenderedTextBlock mode=PixelScene.renderTextBlock(draft==null?"":(currentRun?"This run (locked)":"Next run"),6);
        mode.hardlight(0x999999);mode.setPos(width-mode.width(),title.top()+(title.height()-mode.height())/2f);add(mode);
        combined=PixelScene.renderTextBlock("",6);combined.maxWidth(width);combined.hardlight(TITLE_COLOR);
        combined.setPos(0,title.bottom()+3);add(combined);updateCombined();
        float ty=combined.bottom()+4;
        float tw=(width-4)/3f;
        for(int i=0;i<3;i++){
            final int which=i;
            RedButton b=new RedButton(TAB_NAMES[i],7){
                @Override protected void onClick(){showTab(which);}
            };
            b.setRect(i*(tw+2),ty,tw,16);add(b);tabButtons[i]=b;
        }
        float above=ty+16+4;
        // Create the scroll touch area before controls, matching WndAPDSettings' touch ordering.
        Component content=new Component();scroll=new ScrollPane(content);add(scroll);
        for(int i=0;i<3;i++){tabs[i]=new Component();content.add(tabs[i]);}

        //Scoring tab: the full experimental notice, the multiplier and the long explanation behind a button
        Component sc=tabs[TAB_SCORING];
        scoringNotice=PixelScene.renderTextBlock(CustomRulesPresentation.noticeText(),6);
        scoringNotice.maxWidth(width);scoringNotice.setPos(0,0);sc.add(scoringNotice);
        float y=scoringNotice.bottom()+5;
        RedButton scoringInfo=new RedButton(CustomRulesPresentation.SCORING_INFO_BUTTON,6) {
            @Override protected void onClick() {
                com.watabou.noosa.Game.scene().addToFront(new WndUimsdText(
                        CustomRulesPresentation.SCORING_INFO_TITLE,CustomRulesPresentation.SCORING_VISION));
            }
        };
        scoringInfo.setRect(0,y,width,18);sc.add(scoringInfo);y=scoringInfo.bottom()+4;
        if(currentRun&&draft!=null&&(Dungeon.customScoring()==null||Dungeon.customScoring().recordVersion<2)){
            RenderedTextBlock kept=PixelScene.renderTextBlock("Original score kept; multipliers preview new runs.",6);
            kept.hardlight(0x999999);kept.maxWidth(width);kept.setPos(0,y);sc.add(kept);y=kept.bottom()+4;
        }
        tabHeights[TAB_SCORING]=y;

        //Presets tab
        Component pr=tabs[TAB_PRESETS];
        y=0;
        if(currentRun||draft==null){
            RenderedTextBlock locked=PixelScene.renderTextBlock(draft==null?"This run's custom settings were not recorded."
                    :"Presets change next-run settings. This run's rules are locked.",6);
            locked.hardlight(0x999999);locked.maxWidth(width);locked.setPos(0,y);pr.add(locked);y=locked.bottom()+4;
        } else {
            String[][] rows={{"Quick presets","Start from a ready-made difficulty"},{"Load saved settings","Use one of your saved slots"},
                    {"Save settings to slot","Keep these sliders for later"},{"Rename preset slot","Give a slot your own name"}};
            for(int i=0;i<rows.length;i++){
                final int which=i;
                RedButton b=new RedButton(rows[i][0],7){
                    @Override protected void onClick(){
                        if(which==0)choosePreset();else if(which==1)chooseSlot(false);else if(which==2)chooseSlot(true);else chooseRenameSlot();
                    }
                };
                b.setRect(0,y,width,18);pr.add(b);y=b.bottom()+1;
                RenderedTextBlock d=PixelScene.renderTextBlock(rows[i][1],6);d.hardlight(0x999999);d.maxWidth(width-4);d.setPos(2,y);pr.add(d);
                y=d.bottom()+5;
            }
        }
        tabHeights[TAB_PRESETS]=y;

        //Rules tab: a slim warning line, then the sliders grouped in cards
        Component ru=tabs[TAB_RULES];
        y=0;
        RenderedTextBlock warn=PixelScene.renderTextBlock("Experimental: these values may change. Details in the Scoring tab.",6);
        warn.hardlight(0xFF9933);warn.maxWidth(width);warn.setPos(0,y);ru.add(warn);y=warn.bottom()+4;
        if(draft==null) {
            RenderedTextBlock unknown=PixelScene.renderTextBlock("This run's custom settings were not recorded.",6);
            unknown.maxWidth(width);unknown.setPos(0,y);ru.add(unknown);y=unknown.bottom()+4;
        } else {
            String group=null;
            for(final CustomDungeonRules.Slider rule:CustomDungeonRules.Slider.values()) {
                String g=groupOf(rule);
                if(!g.equals(group)){
                    group=g;
                    RenderedTextBlock gh=PixelScene.renderTextBlock(g.toUpperCase(java.util.Locale.ROOT),6);
                    gh.hardlight(TITLE_COLOR);gh.setPos(1,y+2);ru.add(gh);
                    ColorBlock rule2=new ColorBlock(width,1,0xFF4A4D44);rule2.x=0;rule2.y=gh.bottom()+2;ru.add(rule2);
                    y=rule2.y+3;
                }
                com.watabou.noosa.NinePatch card=com.shatteredpixel.shatteredpixeldungeon.Chrome.get(com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.TOAST_TR);
                ru.add(card);
                final RenderedTextBlock caption=PixelScene.renderTextBlock(caption(rule),6);
                caption.maxWidth(width-6);caption.setPos(3,y+3);ru.add(caption);sliderCaptions[rule.ordinal()]=caption;
                float sy=caption.bottom()+1;
                OptionSlider slider=new OptionSlider("",CustomRulesPresentation.valueText(rule,rule.min),
                        CustomRulesPresentation.valueText(rule,rule.max),0,CustomRulesPresentation.positions(rule)) {
                    @Override protected void onChange() {
                        draft=draft.with(rule,CustomRulesPresentation.value(rule,getSelectedValue()));
                        caption.text(caption(rule));caption.maxWidth(captionWidth);updateCombined();
                    }
                };
                slider.scrollVerticallyWith(scroll);slider.liveChanges(true);slider.hideBackground();
                slider.setRect(2,sy-3,width-4,22);
                slider.setSelectedValue(CustomRulesPresentation.position(rule,draft.get(rule)));
                sliderControls[rule.ordinal()]=slider;
                slider.enable(!currentRun && CustomRulesPresentation.gameplayImplemented(rule));
                ru.add(slider);
                card.x=0;card.y=y;card.size(width,slider.bottom()+2-y);
                y=card.y+card.height()+3;
            }
        }
        tabHeights[TAB_RULES]=y;

        float tallest=Math.max(tabHeights[0],Math.max(tabHeights[1],tabHeights[2]));
        content.setSize(width,tabHeights[TAB_RULES]);
        float paneHeight=WindowGeometry.paneHeight(tallest,WindowGeometry.maxPane(PixelScene.uiCamera.height,above,26));
        resize(width,(int)Math.ceil(above+paneHeight+26));scroll.setRect(0,above,width,paneHeight);
        RedButton finish=new RedButton(currentRun?"Close":"Save for new runs",7) {
            @Override protected void onClick() {
                if(!currentRun && draft!=null)SPDSettings.apdCustomRules(draft);
                hide();
            }
        };
        finish.setRect(0,above+paneHeight+4,width,22);add(finish);
        showTab(TAB_RULES);
    }
    /** Switches the visible tab; hidden tabs neither draw nor take touches. */
    public void showTab(int which){
        tab=which;
        for(int i=0;i<3;i++){
            tabs[i].visible=tabs[i].active=(i==which);
            tabButtons[i].textColor(i==which?TITLE_COLOR:0x888888);
        }
        scroll.content().setSize(scroll.content().width(),tabHeights[which]);
        scroll.scrollTo(0,0);
    }
    public int tab(){return tab;}
    static String groupOf(CustomDungeonRules.Slider rule){
        switch(rule){
            case BOSS_HP: case BOSS_DAMAGE: return "Bosses";
            case PLAYER_HP: case PLAYER_DAMAGE: case PLAYER_ACCURACY: case PLAYER_EVASION: case PLAYER_DEFENSE: return "Hero";
            case FOOD: case HUNGER: case HEALING: case EQUIPMENT: case UPGRADE_SCROLLS: return "Resources";
            default: return "Enemies";
        }
    }
    /** All changes remain in the next-run draft until Save for new runs is pressed. */
    private void applyDraft(CustomDungeonRules selected){
        if(currentRun||selected==null)return;
        draft=selected;
        for(CustomDungeonRules.Slider rule:CustomDungeonRules.Slider.values()){
            int index=rule.ordinal();
            OptionSlider control=sliderControls[index];
            RenderedTextBlock label=sliderCaptions[index];
            if(control!=null)control.setSelectedValue(CustomRulesPresentation.position(rule,draft.get(rule)));
            if(label!=null){label.text(caption(rule));label.maxWidth((int)width-6);}
        }
        updateCombined();
    }
    private void choosePreset(){
        if(currentRun)return;
        final CustomRulePresets.Preset[] presets=CustomRulePresets.Preset.values();
        String[] options=new String[presets.length];
        for(int i=0;i<presets.length;i++)options[i]=presets[i].label;
        com.watabou.noosa.Game.scene().addToFront(new WndUimsdChoices(
                "Apply preset","Apply to the next-run draft. Individual sliders can still be adjusted.",options){
            @Override protected void onSelect(int index){
                if(index>=0&&index<presets.length)applyDraft(CustomRulePresets.apply(presets[index]));
            }
        });
    }
    private void chooseSlot(final boolean saving){
        if(currentRun)return;
        String[] options=new String[SPDSettings.APD_CUSTOM_RULE_SLOT_COUNT];
        for(int i=0;i<options.length;i++){
            boolean occupied=SPDSettings.apdCustomRuleSlotOccupied(i+1);
            options[i]="Slot "+(i+1)+": "+SPDSettings.apdCustomRuleSlotName(i+1)+(occupied?" (saved)":" (empty)");
        }
        com.watabou.noosa.Game.scene().addToFront(new WndUimsdChoices(
                saving?"Save settings":"Load settings",
                saving?"Save the current slider draft to one slot. Existing saved settings in that slot will be replaced."
                      :"Load a stored slider configuration into the next-run draft. Empty slots cannot be loaded.",options){
            @Override protected void onSelect(int index){
                if(index<0||index>=SPDSettings.APD_CUSTOM_RULE_SLOT_COUNT)return;
                int slot=index+1;
                // The scrollable choices widget deliberately allows every row to be
                // reached. Empty saved slots are inert and must not reset a draft.
                if(!saving&&!SPDSettings.apdCustomRuleSlotOccupied(slot)){
                    com.watabou.noosa.Game.scene().addToFront(
                            new WndMessage("Slot "+slot+" has no saved settings yet."));
                    return;
                }
                if(saving){
                    if(SPDSettings.apdCustomRuleSlotOccupied(slot)){
                        com.watabou.noosa.Game.scene().addToFront(new WndOptions(
                                "Overwrite slot "+slot+"?","This replaces the settings stored in this slot.",
                                "Overwrite","Cancel"){
                            @Override protected void onSelect(int choice){
                                if(choice==0)SPDSettings.apdCustomRuleSlot(slot,draft);
                            }
                        });
                    }else SPDSettings.apdCustomRuleSlot(slot,draft);
                }else applyDraft(SPDSettings.apdCustomRuleSlot(slot));
            }
        });
    }
    private void chooseRenameSlot(){
        if(currentRun)return;
        String[] options=new String[SPDSettings.APD_CUSTOM_RULE_SLOT_COUNT];
        for(int i=0;i<options.length;i++)
            options[i]="Slot "+(i+1)+": "+SPDSettings.apdCustomRuleSlotName(i+1);
        com.watabou.noosa.Game.scene().addToFront(new WndUimsdChoices(
                "Rename preset","Choose a slot to rename. This does not change its saved slider values.",options){
            @Override protected void onSelect(int index){
                if(index<0||index>=SPDSettings.APD_CUSTOM_RULE_SLOT_COUNT)return;
                final int slot=index+1;
                com.watabou.noosa.Game.scene().addToFront(new WndTextInput(
                        "Rename slot "+slot,
                        "Up to 24 characters. Leave blank to restore the class name.",
                        SPDSettings.apdCustomRuleSlotName(slot),24,false,"Save","Cancel"){
                    @Override public void onSelect(boolean confirmed,String text){
                        if(confirmed)SPDSettings.apdCustomRuleSlotName(slot,text);
                    }
                });
            }
        });
    }
    private void updateCombined(){
        String factor=draft==null?"Not recorded":
                currentRun && frozenScoring!=null && frozenScoring.status==CustomScoreSnapshot.Status.EXPERIMENTAL
                    ? com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRuleMultipliers.format(new java.math.BigDecimal(frozenScoring.multiplierExact))
                    : com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRuleMultipliers.format(com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.CustomRuleMultipliers.combined(draft));
        combined.text("Score multiplier: "+factor);
    }
    private String caption(CustomDungeonRules.Slider rule) {
        String factor=currentRun && frozenScoring!=null && frozenScoring.status==CustomScoreSnapshot.Status.EXPERIMENTAL
                ? CustomRulesPresentation.multiplierTextForVersion(rule,draft.get(rule),frozenScoring.calculationVersion)
                : CustomRulesPresentation.multiplierText(rule,draft.get(rule));
        return CustomRulesPresentation.label(rule)+": _"+CustomRulesPresentation.valueText(rule,draft.get(rule))
            +"_  ("+factor+")";
    }
}
