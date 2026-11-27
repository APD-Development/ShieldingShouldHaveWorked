package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.util.LinkedHashMap;
import java.util.Map;

//A scrolling text window with a row of buttons under it.
class WndSimText extends Window {

	WndSimText(String title, String body, LinkedHashMap<String, Runnable> actions){
		super();
		int width = PixelScene.landscape() ? 240 : 150;
		int height = (int) (PixelScene.uiCamera.height * 0.8f);
		RenderedTextBlock t = PixelScene.renderTextBlock(title, 8);
		t.hardlight(Window.TITLE_COLOR);
		t.maxWidth(width);
		t.setPos(0, 0);
		add(t);
		float top = t.bottom() + 2;
		int rows = actions == null ? 0 : (actions.size() + 1) / 2;
		float buttonsH = rows * 18;
		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		RenderedTextBlock text = PixelScene.renderTextBlock(body.length() > 20000 ? body.substring(0, 20000) + "\n..." : body, 6);
		text.maxWidth(width - 4);
		text.setPos(0, 0);
		pane.content().add(text);
		pane.content().setSize(width, text.height() + 2);
		pane.setRect(0, top, width, height - top - buttonsH - 2);
		int i = 0;
		if (actions != null){
			for (final Map.Entry<String, Runnable> e : actions.entrySet()){
				RedButton b = new RedButton(e.getKey(), 7){ @Override protected void onClick(){ e.getValue().run(); } };
				float bw = (width - 2) / 2f;
				b.setRect((i % 2) * (bw + 2), height - buttonsH + (i / 2) * 18, bw, 16);
				add(b);
				i++;
			}
		}
		resize(width, height);
	}
}
