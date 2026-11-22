package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.HeapExamination;
import com.shatteredpixel.shatteredpixeldungeon.apd.query.WindowGeometry;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.List;

/** Open visible pile inspection. Labels, icons and individual item info use normal SPD identification. */
public final class WndInfoPile extends Window {
    private static final int MARGIN = 2;
    private static final float GAP = 2f;
    private static final float MIN_ROW = 25f;

    private final List<Item> items;
    private final ScrollPane scroll;
    private final float[] tops;
    private final int width;
    //APD (Chris, Nov 22): the pile's cell, so items can be picked up when the hero stands on it
    private final int cell;
    private static final float TAKE_W = 30f;

    public WndInfoPile(Heap heap) {
        this(HeapExamination.snapshot(heap), heap == null ? -1 : heap.pos);
    }

    /** True when the hero stands on this open pile and can act, so each row gets a "Take" button. */
    public static boolean canTakeFrom(int cell){
        if (Dungeon.hero == null || Dungeon.level == null || cell < 0 || Dungeon.hero.pos != cell) return false;
        Heap heap = Dungeon.level.heaps.get(cell);
        return heap != null && heap.type == Heap.Type.HEAP;
    }

    private WndInfoPile(List<Item> snapshot, int cell) {
        super();
        this.cell = cell;
        items = new ArrayList<>(snapshot);
        final boolean take = canTakeFrom(cell);
        width = WindowGeometry.width(PixelScene.landscape(), PixelScene.uiCamera.width);
        RenderedTextBlock title = PixelScene.renderTextBlock("Items on tile (" + items.size() + ")", 9);
        title.hardlight(TITLE_COLOR);
        title.maxWidth(width - 2*MARGIN);
        title.setPos(MARGIN, MARGIN);
        add(title);
        float y = title.bottom() + 2*GAP;

        // Large heaps use one clipped, scrollable pane. Sprites are the SAME appearance
        // shown by the regular item-info window, so unidentified items stay unidentified.
        Component body = new Component();
        tops = new float[items.size()+1];
        float contentY=0f;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            RenderedTextBlock name = PixelScene.renderTextBlock(HeapExamination.visibleLabel(item),7);
            name.maxWidth(Math.max(1,width-25-(take?(int)TAKE_W+2:0)));
            float h=Math.max(MIN_ROW,name.height()+2*GAP+4f);
            tops[i]=contentY;
            NinePatch backdrop = Chrome.get(Chrome.Type.GREY_BUTTON_TR);
            backdrop.size(width - 2*MARGIN,h-1);
            backdrop.x=MARGIN;
            backdrop.y=contentY;
            body.add(backdrop);
            ItemSprite sprite = new ItemSprite(item);
            sprite.x=MARGIN+3f;
            sprite.y=contentY+(h-sprite.height())/2f;
            body.add(sprite);
            name.setPos(MARGIN+22f,contentY+(h-name.height())/2f);
            body.add(name);
            if (take) {
                NinePatch btn = Chrome.get(Chrome.Type.RED_BUTTON);
                btn.size(TAKE_W, h-7);
                btn.x = width - MARGIN - TAKE_W - 2;
                btn.y = contentY + 3;
                body.add(btn);
                RenderedTextBlock t = PixelScene.renderTextBlock("Take", 7);
                t.setPos(btn.x + (TAKE_W - t.width())/2f, btn.y + (btn.height - t.height())/2f);
                PixelScene.align(t);
                body.add(t);
            }
            contentY+=h+GAP;
        }
        tops[items.size()]=contentY;
        body.setSize(width,contentY);

        scroll = new ScrollPane(body) {
            @Override public void onClick(float x, float y) {
                if (x < MARGIN || x >= width - MARGIN) return;
                int index=WindowGeometry.rowAt(tops,y);
                Item selected=HeapExamination.select(items,index);
                if (selected != null && x >= width - MARGIN - TAKE_W - 2 && canTakeFrom(WndInfoPile.this.cell)) {
                    take(selected);
                    return;
                }
                if (selected != null) {
                    ShatteredPixelDungeon.scene().addToFront(new WndInfoItem(selected));
                }
            }
        };
        // Any item description opens above the list. Closing it returns to the
        // same scroll position without recreating or touching the dungeon.
        float height=WindowGeometry.paneHeight(contentY,
                WindowGeometry.maxPane(PixelScene.uiCamera.height,y,0));
        resize(width,(int)Math.ceil(y+height));
        add(scroll);
        scroll.setRect(0,y,width,height);
    }

    private void take(Item item){
        hide();
        if (Dungeon.hero == null || !Dungeon.hero.ready) return;
        Dungeon.hero.curAction = new HeroAction.PickUpItem(cell, item);
        Dungeon.hero.next();
    }

    @Override public Runnable restoreOnSceneReset() {
        final int savedCell=cell;
        final List<Item> saved=new ArrayList<>(items);
        final float scrollY=scroll.content().camera.scroll.y;
        return () -> {
            WndInfoPile restored=new WndInfoPile(saved,savedCell);
            restored.scroll.scrollTo(0,scrollY);
            ShatteredPixelDungeon.scene().addToFront(restored);
        };
    }
}
