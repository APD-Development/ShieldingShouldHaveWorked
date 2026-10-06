/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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


package com.shatteredpixel.shatteredpixeldungeon.apd.recorder;

import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.EventTypes;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ItemEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.LifecycleEvents;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.ReadResult;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogReader;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.RunLogWriter;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.StatsStore;
import com.shatteredpixel.shatteredpixeldungeon.apd.stats.TelemetryVersion;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.File;
import java.util.List;

//The one door between the game and APD run recording.
//
//RULES
// - Every hook is safe to call from gameplay code: nothing escapes. A problem inside the recorder is counted and
//   remembered (status().lastError). After MAX_ERRORS the recorder switches itself off for the rest of the app session,
//   gameplay carries on untouched.
// - The recorder only READS the game. It never changes game state and never calls SPD's random number generator.
// - Until configure() is called (the game does that at startup, tests do it explicitly) every hook does nothing, so
//   code that runs the game without the app, like the dungeon generation tests, behaves exactly as before.
// - Hooks run on the game thread and are cheap: they encode events and put them on the writer's queue. Disk work
//   happens on the writer thread, with short bounded waits only at save and run end.
public final class ApdRecorder {

	public static final int MAX_ERRORS = 5;

	private static StatsStore store;
	private static File root;
	private static boolean configured;
	private static boolean enabled;
	private static boolean sessionDisabled;
	private static int errorCount;
	private static String lastError = "";
	private static RunRecorder active;

	//test hook: makes the next recorder action fail, to prove failures are contained
	static volatile RuntimeException failureInjection;

	private ApdRecorder(){}

	//---- setup ---------------------------------------------------------------------------------------------

	//Sets where history is kept and whether recording is on. Calling it again resets the recorder (used by tests to
	//simulate an app restart).
	public static synchronized void configure( File historyRoot, boolean enable ){
		shutdownQuietly();
		root = historyRoot;
		store = historyRoot == null ? null : new StatsStore(historyRoot);
		configured = historyRoot != null;
		enabled = enable;
		sessionDisabled = false;
		errorCount = 0;
		lastError = "";
		active = null;
		failureInjection = null;
		refreshItemFlag();
	}

	//the app's normal location: an "apd" folder in the game's data folder
	public static synchronized void configureDefault( boolean enable ){
		try {
			configure(FileUtils.getFileHandle("apd").file(), enable);
		} catch (Throwable t){
			configure(null, false);
			lastError = "setup: " + t;
		}
	}

	//stops recording and forgets everything, leaving any open run log resumable
	public static synchronized void reset(){
		configure(null, false);
	}

	private static void shutdownQuietly(){
		try {
			if (active != null) active.pause();
		} catch (Throwable ignored){
		}
		active = null;
		refreshItemFlag();
	}

	private static boolean usable(){
		return configured && enabled && !sessionDisabled && store != null;
	}

	public static synchronized boolean isRecording(){
		return usable();
	}

	//---- what the game save stores -----------------------------------------------------------------------------

	//The id to write into the game save as apd_run_id, null if no run is being tracked. Never throws.
	public static synchronized String currentRunId(){
		try {
			return active == null ? null : active.logId;
		} catch (Throwable t){
			return null;
		}
	}

	//---- hooks ---------------------------------------------------------------------------------------------

	//a new run was just set up (Dungeon.init)
	public static synchronized void onNewRun(){
		if (!usable()) return;
		try {
			inject();
			closeActive();
			String id = RunRecorder.newId();
			active = RunRecorder.startNew(store, GamesInProgress.curSlot, id, id, 0, LifecycleEvents.START_NEW_RUN, false);
		} catch (Throwable t){
			failed(t);
		}
		refreshItemFlag();
	}

	//a saved game was loaded (Dungeon.loadGame). savedRunId is the apd_run_id stored in that save, null for older saves.
	public static synchronized void onGameLoaded( int slot, String savedRunId, long lastPlayed ){
		if (!usable()) return;
		try {
			inject();
			closeActive();
			if (savedRunId == null || !StatsStore.isValidRunId(savedRunId)){
				//a run that began before APD recorded runs: record from here on, marked partial
				String id = RunRecorder.newId();
				active = RunRecorder.startNew(store, slot, id, id, 0, LifecycleEvents.START_ADOPTED_RUN, true);
			} else {
				active = RunRecorder.resume(store, slot, savedRunId, lastPlayed);
			}
		} catch (Throwable t){
			failed(t);
		}
		refreshItemFlag();
	}

	//the game is about to be written to a save slot: items get their identity first so it is part of the save
	public static synchronized void onBeforeGameSave(){
		if (!usable() || active == null) return;
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.poll(false, false);
		} catch (Throwable t){
			failed(t);
		}
	}

	//the game was just written to a save slot (Dungeon.saveGame)
	public static synchronized void onGameSaved( long lastPlayed ){
		if (!usable() || active == null) return;
		try {
			inject();
			active.saved(lastPlayed);
		} catch (Throwable t){
			failed(t);
		}
	}

	//the hero arrived on a level (Dungeon.switchLevel)
	public static synchronized void onLevelSwitched(){
		if (!usable() || active == null) return;
		try {
			inject();
			active.levelSwitched();
		} catch (Throwable t){
			failed(t);
		}
	}

	//the run ended in death or victory (Dungeon.fail / Dungeon.win)
	public static synchronized void onRunEnded( boolean won, Object cause ){
		if (!usable() || active == null) return;
		try {
			inject();
			active.end(won ? LifecycleEvents.OUTCOME_WON : LifecycleEvents.OUTCOME_DIED, cause);
			active = null;
		} catch (Throwable t){
			failed(t);
			active = null;
		}
		refreshItemFlag();
	}

	//a save slot is about to be deleted (Dungeon.deleteGame): its run, if still open, ends as abandoned
	public static synchronized void onSlotDeleted( int slot ){
		if (!usable()) return;
		try {
			inject();
			if (active != null && !active.ended && active.slot() == slot){
				active.end(LifecycleEvents.OUTCOME_ABANDONED, null);
				active = null;
				refreshItemFlag();
				return;
			}
			String id = readSavedRunId(slot);
			if (id != null) abandonStoredRun(id);
		} catch (Throwable t){
			failed(t);
		}
	}

	//---- item identity (see ApdItems) ----------------------------------------------------------------------------

	private static ItemTracker items(){
		return usable() && active != null && !active.ended ? active.items : null;
	}

	private static void refreshItemFlag(){
		ApdItems.active = usable() && active != null && !active.ended && active.items != null;
	}

	static synchronized void itemNoteAction( Item item, String action ){
		try {
			ItemTracker t = items();
			if (t != null) t.noteAction(item, action);
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemNoteAcquire( int context ){
		try {
			ItemTracker t = items();
			if (t != null) t.noteAcquireContext(context);
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemSplit( Item parent, Item child, int amount ){
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.onSplit(parent, child, amount);
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemMerge( Item survivor, Item absorbed, int amount ){
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.onMerge(survivor, absorbed, amount);
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemDetached( Item original, Item piece ){
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.onDetached(original, piece);
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemTransformed( Item oldItem, Item newItem, String cause ){
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.onTransformed(oldItem, newItem, cause);
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemCrafted( List<Item> ingredients, Item result ){
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.onCrafted(ingredients, result);
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemClassKnown( Class<?> cls, String labelBefore, Item sample ){
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.onClassKnown(cls, labelBefore, sample == null ? ItemEvents.CAT_OTHER : ItemFacts.category(sample));
		} catch (Throwable t){
			failed(t);
		}
	}

	static synchronized void itemHeroTurn(){
		try {
			inject();
			ItemTracker t = items();
			if (t != null) t.poll(false, false);
		} catch (Throwable t){
			failed(t);
		}
	}

	//---- internals -----------------------------------------------------------------------------------------

	private static void closeActive(){
		if (active != null){
			active.pause();
			active = null;
		}
		refreshItemFlag();
	}

	private static String readSavedRunId( int slot ){
		try {
			Bundle b = FileUtils.bundleFromFile(GamesInProgress.gameFile(slot));
			String id = b.contains("apd_run_id") ? b.getString("apd_run_id") : null;
			return id != null && StatsStore.isValidRunId(id) ? id : null;
		} catch (Throwable t){
			return null;
		}
	}

	//ends a run that is not the one being played, from its log alone
	private static void abandonStoredRun( String id ){
		File f = store.logFile(id);
		if (!f.isFile()) return;
		RunLogWriter w = RunLogWriter.openForAppend(f, TelemetryVersion.CURRENT);
		if (w.isDisabled()) return; //sealed, another telemetry version or unreadable: leave it alone
		try {
			ReadResult r = RunLogReader.read(f);
			LifecycleEvents.RunEnd e = new LifecycleEvents.RunEnd();
			e.outcome = LifecycleEvents.OUTCOME_ABANDONED;
			//nothing else is known here, the flag says so instead of pretending
			LifecycleEvents.emitRunEnd(w, r.lastClock, e, LifecycleEvents.FLAG_DETAILS_UNKNOWN);
		} catch (Throwable t){
			failed(t);
		}
		w.close(true, 2000);
		store.refreshIndex(id);
	}

	private static void inject(){
		RuntimeException r = failureInjection;
		if (r != null) throw r;
	}

	private static void failed( Throwable t ){
		errorCount++;
		lastError = String.valueOf(t);
		if (errorCount >= MAX_ERRORS){
			sessionDisabled = true;
			shutdownKeepId();
		}
		refreshItemFlag();
	}

	//stop writing but remember the run id, so game saves stay linked to the log
	private static void shutdownKeepId(){
		try {
			if (active != null && active.writer != null) active.writer.close(false, 500);
		} catch (Throwable ignored){
		}
	}

	//tests: write out what is queued so the log file can be read while the run goes on
	static synchronized void flushForTests(){
		if (active != null && active.writer != null) active.writer.flush(5000, false);
	}

	//---- diagnostics ---------------------------------------------------------------------------------------

	public static synchronized RecorderStatus status(){
		RecorderStatus s = new RecorderStatus();
		try {
			s.configured = configured;
			s.enabled = enabled;
			s.sessionDisabled = sessionDisabled;
			s.errorCount = errorCount;
			s.lastError = lastError;
			s.telemetryVersion = TelemetryVersion.CURRENT.toString();
			if (active != null){
				s.logId = active.logId;
				if (active.writer != null){
					s.logPath = active.writer.file().getPath();
					s.writerDisabled = active.writer.isDisabled();
					s.eventsAccepted = active.writer.eventsAccepted();
					s.eventsDropped = active.writer.eventsDropped();
					if (active.writer.failure() != null) s.writerProblem = String.valueOf(active.writer.failure());
				}
			}
			if (store != null){
				File[] logs = store.runsDir().listFiles();
				if (logs != null){
					for (File f : logs){
						if (f.isFile()){
							s.storageBytes += f.length();
							if (f.getName().endsWith(".apdlog")) s.runLogs++;
						}
					}
				}
			}
		} catch (Throwable t){
			s.lastError = String.valueOf(t);
		}
		return s;
	}

	//where history is kept, null if not set up
	public static synchronized File historyRoot(){
		return root;
	}
}
