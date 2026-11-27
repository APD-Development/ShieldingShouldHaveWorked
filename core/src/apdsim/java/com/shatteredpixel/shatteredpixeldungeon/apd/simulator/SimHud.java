package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.PointerArea;

import java.util.Locale;

//The simulator's controls over the game screen: status, pause, speed, skip, home. Taps elsewhere are swallowed so the bot plays alone.
final class SimHud extends Component {

	private final SimRuntime sim;
	private PointerArea blocker;
	private ColorBlock bg;
	private RenderedTextBlock info;
	private RedButton pause, speed, skip, home;
	private String lastText = "";

	SimHud(SimRuntime sim){
		this.sim = sim;
		camera = PixelScene.uiCamera;
	}

	@Override
	protected void createChildren(){
		blocker = new PointerArea(0, 0, 10000, 10000){
			@Override public boolean onSignal(PointerEvent event){ return true; }
		};
		blocker.camera = PixelScene.uiCamera;
		add(blocker);
		bg = new ColorBlock(1, 1, 0xCC000000);
		add(bg);
		info = PixelScene.renderTextBlock(6);
		add(info);
		pause = new RedButton("Pause", 7){ @Override protected void onClick(){ sim.paused = !sim.paused; refresh(); } };
		speed = new RedButton("1x", 7){ @Override protected void onClick(){ sim.cycleSpeed(); refresh(); } };
		skip = new RedButton("Skip", 7){ @Override protected void onClick(){ sim.skipRun(); } };
		home = new RedButton("Home", 7){ @Override protected void onClick(){ sim.stopToHome(); } };
		add(pause); add(speed); add(skip); add(home);
	}

	void layoutFor(GameScene scene){
		float w = PixelScene.uiCamera.width;
		float h = PixelScene.uiCamera.height;
		blocker.width = w; blocker.height = h;
		setRect(0, 0, w, h);
		float barH = 30;
		float y = h - 26 - barH - 4; //above the game's toolbar
		bg.x = 0; bg.y = y; bg.size(w, barH);
		info.maxWidth((int) w - 4);
		info.setPos(2, y + 1);
		float bw = (w - 10) / 4f;
		float by = y + 12;
		pause.setRect(2, by, bw, 16);
		speed.setRect(4 + bw, by, bw, 16);
		skip.setRect(6 + 2 * bw, by, bw, 16);
		home.setRect(8 + 3 * bw, by, bw, 16);
		refresh();
	}

	void refresh(){
		pause.text(sim.paused ? "Resume" : "Pause");
		speed.text("Speed " + sim.speedLabel());
		String t = String.format(Locale.ROOT, "Run %d %s  %s/%s  Floor %d  Turn %.0f  Bugs %d",
				sim.run, sim.heroClass == null ? "" : sim.heroClass.title(), sim.profile().aggression.title, sim.profile().intelligence.title,
				Dungeon.depth, Actor.now(), sim.bugsThisRun);
		if (!t.equals(lastText)){
			lastText = t;
			info.text(t);
		}
	}
}
