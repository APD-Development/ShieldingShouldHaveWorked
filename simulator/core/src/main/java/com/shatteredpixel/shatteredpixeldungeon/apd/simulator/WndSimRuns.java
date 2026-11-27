package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

//The run log, newest first. Tapping a run replays that exact seed in VISUAL mode (same class, seed, bot seed and settings).
final class WndSimRuns extends Window {

	WndSimRuns(final SimRuntime sim){
		super();
		int width = PixelScene.landscape() ? 240 : 150;
		int height = (int) (PixelScene.uiCamera.height * 0.8f);
		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		Component c = pane.content();
		float y = 0;
		for (final String[] r : RunLog.rows(150)){
			String label = "Run " + r[0] + " " + r[3].toLowerCase() + " (" + r[2] + "): " + r[11] + "\nfloor " + r[13] + ", level " + r[14] + ", turn " + r[15]
					+ ", seed " + r[4] + (r[16].equals("0") ? "" : ", " + r[16] + " reports") + "  [tap: watch it]";
			RedButton b = new RedButton(label, 6){
				@Override protected void onClick(){
					hide();
					try {
						sim.replayVisual(r[3], Long.parseLong(r[5]), Long.parseLong(r[6]), r.length > 23 ? r[23].replace('|', '\t') : "");
					} catch (RuntimeException bad){
						sim.botProblem("replaying a logged run", bad);
					}
				}
			};
			b.multiline = true;
			b.setRect(0, y, width, 30);
			c.add(b);
			y += 32;
		}
		if (y == 0){
			RedButton none = new RedButton("No runs logged yet", 7);
			none.enable(false);
			none.setRect(0, 0, width, 18);
			c.add(none);
			y = 20;
		}
		c.setSize(width, y);
		pane.setRect(0, 0, width, height);
		resize(width, height);
	}
}
