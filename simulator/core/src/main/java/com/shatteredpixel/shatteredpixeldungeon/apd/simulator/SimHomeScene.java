package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.apd.difficulty.Difficulty;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.UimsdScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.GameSettings;

import java.util.ArrayList;
import java.util.List;

//The simulator's own start screen (simulator process only).
public class SimHomeScene extends PixelScene {

	@Override
	public void create(){
		super.create();
		final SimRuntime sim = SimRuntime.get();
		int w = Camera.main.width, h = Camera.main.height;
		add(new ColorBlock(w, h, 0xFF1A1A22));

		ScrollPane pane = new ScrollPane(new Component());
		add(pane);
		Component c = pane.content();
		float width = Math.min(w - 8, 220);
		float x = (w - width) / 2f;
		float y = 4;

		RenderedTextBlock title = renderTextBlock("APD Simulator", 10);
		title.hardlight(0xFFFF44);
		title.setPos(x, y);
		c.add(title);
		y = title.bottom() + 3;

		RenderedTextBlock note = renderTextBlock("Developer build only. Its saves, settings and UIMSD are its own and never touch your real game.", 6);
		note.maxWidth((int) width);
		note.setPos(x, y);
		c.add(note);
		y = note.bottom() + 4;

		final SimProfile p = sim.profile();
		RenderedTextBlock prof = renderTextBlock(p.name + ": " + p.summary(), 6);
		prof.maxWidth((int) width);
		prof.setPos(x, y);
		c.add(prof);
		y = prof.bottom() + 3;

		HistorySource hs = SimRuntime.history();
		String status = (hs == null ? "Your history: not available" : hs.status()) + "\n"
				+ "Runs played: " + Math.max(0, sim.run) + ". Bug reports: " + sim.reportCount() + "."
				+ (sim.lastRunSummary.isEmpty() ? "" : "\nLast: " + sim.lastRunSummary);
		RenderedTextBlock st = renderTextBlock(status, 6);
		st.maxWidth((int) width);
		st.setPos(x, y);
		c.add(st);
		y = st.bottom() + 5;

		List<RedButton> buttons = new ArrayList<>();
		buttons.add(new RedButton(sim.playing ? "Keep playing" : "Start", 8){ @Override protected void onClick(){ sim.paused = false; sim.setPlaying(true); sim.resetStopClock(); sim.startRun(); } });
		buttons.add(new RedButton("Mode: " + (sim.headless ? "Headless (no graphics, fastest)" : "Visual (watchable)"), 7){ @Override protected void onClick(){ sim.setHeadless(!sim.headless); refresh(); } });
		buttons.add(new RedButton("Speed: " + sim.speedLabel(), 7){ @Override protected void onClick(){ sim.cycleSpeed(); refresh(); } });
		buttons.add(new RedButton("Class: " + p.classTitle(), 7){ @Override protected void onClick(){
			p.heroClass = p.heroClass + 1 >= HeroClass.values().length ? -1 : p.heroClass + 1; save(p); } });
		buttons.add(new RedButton("Aggression: " + p.aggression.title, 7){ @Override protected void onClick(){
			p.aggression = SimProfile.Aggression.values()[(p.aggression.ordinal() + 1) % 4]; save(p); } });
		buttons.add(new RedButton("Intelligence: " + p.intelligence.title, 7){ @Override protected void onClick(){
			p.intelligence = SimProfile.Intelligence.values()[(p.intelligence.ordinal() + 1) % 4]; save(p); } });
		buttons.add(new RedButton("Difficulty: " + p.difficulty, 7){ @Override protected void onClick(){
			p.difficulty = p.difficulty >= Difficulty.MAX ? Difficulty.MIN : p.difficulty + 1; save(p); } });
		buttons.add(new RedButton("Seeds: " + p.seedMode.title + (p.seedMode == SimProfile.SeedMode.RANDOM ? "" : " " + p.seedText), 7){ @Override protected void onClick(){ chooseSeed(p); } });
		buttons.add(new RedButton("Randomize APD Settings: " + (p.randomizeSettings ? "on" : "off"), 7){ @Override protected void onClick(){
			p.randomizeSettings = !p.randomizeSettings; save(p); } });
		buttons.add(new RedButton("Stop: " + p.stop.title + (p.stop == SimProfile.Stop.RUNS || p.stop == SimProfile.Stop.MINUTES || p.stop == SimProfile.Stop.FLOOR ? " (" + p.stopAmount + ")" : ""), 7){ @Override protected void onClick(){ chooseStop(p); } });
		buttons.add(new RedButton("Chauffeur: " + (p.chauffeurFloor > 0 ? "to floor " + p.chauffeurFloor : "off"), 7){ @Override protected void onClick(){
			ShatteredPixelDungeon.scene().add(new WndTextInput("Chauffeur", "The bot plays to this floor, then hands you the controls. 0 turns it off.",
					String.valueOf(p.chauffeurFloor), 2, false, "OK", "Cancel"){
				@Override public void onSelect(boolean positive, String text){
					try { if (positive) p.chauffeurFloor = Math.max(0, Math.min(26, Integer.parseInt(text.trim()))); } catch (NumberFormatException ignored){}
					save(p);
				}
			}); } });
		buttons.add(new RedButton("Start from checkpoints: " + (p.checkpointFloor > 1 ? "floor " + p.checkpointFloor + "+" : "off") + " (" + Checkpoints.all().size() + " saved)", 7){ @Override protected void onClick(){
			ShatteredPixelDungeon.scene().add(new WndTextInput("Checkpoints", "Start runs from a saved checkpoint at this floor or deeper (saved automatically whenever a run reaches a new floor). 0 or 1 = start at floor 1.",
					String.valueOf(p.checkpointFloor), 2, false, "OK", "Cancel"){
				@Override public void onSelect(boolean positive, String text){
					try { if (positive) p.checkpointFloor = Math.max(0, Math.min(26, Integer.parseInt(text.trim()))); } catch (NumberFormatException ignored){}
					save(p);
				}
			}); } });
		buttons.add(new RedButton("Determinism check: " + (p.verifyEvery > 0 ? "replay every " + p.verifyEvery + (p.verifyEvery == 1 ? " run" : " runs") : "off"), 7){ @Override protected void onClick(){
			ShatteredPixelDungeon.scene().add(new WndTextInput("Determinism check", "Every Nth run is played again with the same seed and decisions and compared. A difference is reported as a bug. 0 turns it off.",
					String.valueOf(p.verifyEvery), 4, false, "OK", "Cancel"){
				@Override public void onSelect(boolean positive, String text){
					try { if (positive) p.verifyEvery = Math.max(0, Integer.parseInt(text.trim())); } catch (NumberFormatException ignored){}
					save(p);
				}
			}); } });
		buttons.add(new RedButton("Sound: " + (SPDSettings.soundFx() ? "on" : "off"), 7){ @Override protected void onClick(){
			boolean on = !SPDSettings.soundFx(); SPDSettings.soundFx(on); SPDSettings.music(on); GameSettings.put("apdsim_sound", on); refresh(); } });
		buttons.add(new RedButton("Your runs (Try my death)", 7){ @Override protected void onClick(){ add(new WndRealRunSeeds(p)); } });
		buttons.add(new RedButton("Bug reports (" + sim.reportCount() + ")", 7){ @Override protected void onClick(){ add(new WndSimReports(sim)); } });
		buttons.add(new RedButton("Run log (tap a run to watch it)", 7){ @Override protected void onClick(){ add(new WndSimRuns(sim)); } });
		buttons.add(new RedButton("Export log (runs, deaths, bugs)", 7){ @Override protected void onClick(){ sim.platform.share(RunLog.exportZip(), "APD simulator log"); } });
		buttons.add(new RedButton("Summary", 7){ @Override protected void onClick(){ add(new WndSimSummary()); } });
		buttons.add(new RedButton("Simulator UIMSD", 7){ @Override protected void onClick(){ ShatteredPixelDungeon.switchNoFade(UimsdScene.class); } });
		buttons.add(new RedButton("Clear simulator history", 7){ @Override protected void onClick(){ confirmClear(sim); } });
		buttons.add(new RedButton("Close simulator", 7){ @Override protected void onClick(){ sim.setPlaying(false); sim.platform.close(); } });
		for (RedButton b : buttons){
			b.setRect(x, y, width, 18);
			c.add(b);
			y += 20;
		}
		c.setSize(w, y + 4);
		pane.setRect(0, 0, w, h);
		fadeIn();
	}

	private static void refresh(){
		ShatteredPixelDungeon.switchNoFade(SimHomeScene.class);
	}

	private static void save(SimProfile p){
		p.save();
		refresh();
	}

	private void chooseSeed(final SimProfile p){
		add(new WndOptions("Seeds", "Which seeds should this simulator play? Random seeds are best for balancing.",
				SimProfile.SeedMode.RANDOM.title, SimProfile.SeedMode.TYPED.title, SimProfile.SeedMode.REAL_RUN.title){
			@Override protected void onSelect(int index){
				if (index == 0){ p.seedMode = SimProfile.SeedMode.RANDOM; save(p); }
				else if (index == 1){
					ShatteredPixelDungeon.scene().add(new WndTextInput("Seed", "Type a seed (letters or a number).", p.seedText, 30, false, "Use", "Cancel"){
						@Override public void onSelect(boolean positive, String text){
							if (positive && !text.trim().isEmpty()){ p.seedMode = SimProfile.SeedMode.TYPED; p.seedText = text.trim(); save(p); }
						}
					});
				} else {
					ShatteredPixelDungeon.scene().add(new WndRealRunSeeds(p));
				}
			}
		});
	}

	private void chooseStop(final SimProfile p){
		add(new WndOptions("Stop when", "When should this simulator stop?", SimProfile.Stop.FOREVER.title, SimProfile.Stop.RUNS.title,
				SimProfile.Stop.MINUTES.title, SimProfile.Stop.FIRST_BUG.title, SimProfile.Stop.FIRST_WIN.title, SimProfile.Stop.FLOOR.title){
			@Override protected void onSelect(final int index){
				p.stop = SimProfile.Stop.values()[index];
				if (p.stop == SimProfile.Stop.RUNS || p.stop == SimProfile.Stop.MINUTES || p.stop == SimProfile.Stop.FLOOR){
					ShatteredPixelDungeon.scene().add(new WndTextInput(p.stop.title, "How many?", String.valueOf(p.stopAmount), 6, false, "OK", "Cancel"){
						@Override public void onSelect(boolean positive, String text){
							try { if (positive) p.stopAmount = Math.max(1, Integer.parseInt(text.trim())); } catch (NumberFormatException ignored){}
							save(p);
						}
					});
				} else save(p);
			}
		});
	}

	private void confirmClear(final SimRuntime sim){
		add(new WndOptions("Clear simulator history", "This deletes every run in the SIMULATOR's UIMSD and its bug reports. Your real UIMSD, saves and runs are in a different folder and are not touched.",
				"Delete simulator history", "Keep it"){
			@Override protected void onSelect(int index){
				if (index == 0){ sim.clearHistory(); refresh(); }
			}
		});
	}

	@Override
	protected void onBackPressed(){
		SimRuntime.get().setPlaying(false);
		SimRuntime.get().platform.close();
	}
}
