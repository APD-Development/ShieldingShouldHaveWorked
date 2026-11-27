package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.LinkedHashMap;
import java.util.List;

//The list of bug reports kept on this phone. Tapping one opens it (Share, Copy, Replay).
final class WndSimReports extends Window {

	WndSimReports(final SimRuntime sim){
		super();
		int width = PixelScene.landscape() ? 240 : 150;
		int height = (int) (PixelScene.uiCamera.height * 0.8f);
		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		Component c = pane.content();
		float y = 0;
		List<FileHandle> files = sim.store().files();
		if (files.isEmpty()){
			RedButton none = new RedButton("No bugs found yet", 7);
			none.enable(false);
			none.setRect(0, 0, width, 18);
			c.add(none);
			y = 20;
		}
		for (final FileHandle f : files){
			String[] head;
			try { head = BugReport.header(f.readString("UTF-8")); } catch (Exception e){ continue; }
			RedButton b = new RedButton(head[0] + ": " + head[1] + "\n" + head[3] + ", floor " + head[4] + ", " + head[2], 6){
				@Override protected void onClick(){ open(sim, f); }
			};
			b.multiline = true;
			b.setRect(0, y, width, 30);
			c.add(b);
			y += 32;
		}
		c.setSize(width, y);
		pane.setRect(0, 0, width, height);
		resize(width, height);
	}

	static void open(final SimRuntime sim, final FileHandle f){
		final String text;
		try { text = f.readString("UTF-8"); } catch (Exception e){ return; }
		LinkedHashMap<String, Runnable> actions = new LinkedHashMap<>();
		actions.put("Share", () -> sim.platform.share(ReportShare.zip(f, text), "APD simulator bug report"));
		actions.put("Copy", () -> Gdx.app.getClipboard().setContents(text));
		actions.put("Replay", () -> {
			try {
				HeroClass cls = HeroClass.valueOf(BugReport.field(text, "Class"));
				String seedLine = BugReport.field(text, "Seed");
				long seed = Long.parseLong(seedLine.replaceAll(".*\\(", "").replace(")", "").trim());
				String runLine = BugReport.field(text, "Simulator run");
				long botSeed = Long.parseLong(runLine.replaceAll(".*bot seed ", "").trim());
				String profile = BugReport.field(text, "Simulator settings");
				sim.startReplay(cls, seed, botSeed, profile, (int) Float.parseFloat(BugReport.field(text, "Turn")));
			} catch (RuntimeException bad){
				Game.reportException(bad);
			}
		});
		actions.put("Delete", f::delete);
		Game.scene().add(new WndSimText("Bug report", text, actions));
	}
}
