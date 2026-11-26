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

package com.shatteredpixel.shatteredpixeldungeon.apd.community;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.shatteredpixel.shatteredpixeldungeon.apd.age.CommunityAgeGate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

// The live Community screens as plain data: every screen is a title, a message and rows, and every tappable row carries
// the action it runs. CommunityScene only draws them. Keeping the logic here means it can be tested without a phone.
public final class CommunityController {

	// ---- what the UI must provide ----

	public interface Ui {
		void show(Screen screen);
		// Ask for one line (or several) of text. answer gets null when the player cancels.
		void ask(String title, String body, String initial, int maxLength, boolean multiLine, Consumer<String> answer);
		void busy(String message);
		void leave();
		// The read-only pages about what's planned (run reviews, mods, events...). Returning from them restarts Community.
		void planned();
		// Run reviews: start the one review playthrough from a validated claim (seed hidden, setup locked).
		default void playReview(ReviewClaim claim){}
	}

	// Runs a network call off the render thread, then hands the result back on it. Tests run both at once.
	public interface Runner {
		void run(Supplier<CommunityApi.Result> call, Consumer<CommunityApi.Result> then);
	}

	// Where the session token and the age-gate answer live on the device.
	public interface Storage extends CommunityAgeGate.Store {
		String token();
		void token(String token);
	}

	// Google sign-in on the platform (Android Credential Manager). Calls done once with a Google ID token, or null
	// when the player cancels or Google sign-in isn't available. Null source = no Google button.
	public interface GoogleSource {
		void request(Consumer<String> done);
	}

	public static volatile GoogleSource google = null;
	// Launch is Google sign-in only (Chris, Nov 18). The email screens stay for a possible Steam build; off = hidden.
	public static volatile boolean emailSignIn = false;
	// Where downloaded hero mods go (<files>/mods, the folder hero select scans). Null = no workshop downloads/uploads.
	public static volatile java.io.File modsDir = null;

	public static final class Row {
		public final String label, detail;
		public final int kind; // 0 heading, 1 note, 2 item, 3 warning
		public final Runnable action;
		public final Boolean checked;
		Row(int kind, String label, String detail, Runnable action, Boolean checked){
			this.kind = kind; this.label = label; this.detail = detail == null ? "" : detail; this.action = action; this.checked = checked;
		}
	}

	public static final class Screen {
		public final String title, message;
		public final List<Row> rows = new ArrayList<>();
		public Runnable back;
		/** Optional preview art (a mod hero's sprite.png bytes) and a cache key for it; null = no picture. */
		public byte[] previewPng;
		public String previewKey;
		Screen(String title, String message){ this.title = title; this.message = message; }
		Screen heading(String t){ rows.add(new Row(0, t, null, null, null)); return this; }
		Screen note(String t){ rows.add(new Row(1, t, null, null, null)); return this; }
		Screen warning(String t){ rows.add(new Row(3, t, null, null, null)); return this; }
		Screen item(String label, String detail, Runnable action){ rows.add(new Row(2, label, detail, action, null)); return this; }
		Screen check(String label, String detail, boolean on, Runnable action){ rows.add(new Row(2, label, detail, action, on)); return this; }
		public List<Row> items(){
			List<Row> out = new ArrayList<>();
			for (Row r : rows) if (r.kind == 2) out.add(r);
			return out;
		}
	}

	private final CommunityApi api;
	private final Ui ui;
	private final Runner runner;
	private final Storage storage;
	private final int[] today; // {month 1-12, year}
	private JsonValue me;
	private JsonValue season;

	public CommunityController(CommunityApi api, Ui ui, Runner runner, Storage storage, int[] today){
		this.api = api; this.ui = counting(ui); this.runner = runner; this.storage = storage; this.today = today;
		if (api != null && storage.token() != null) api.token(storage.token());
	}

	public static int[] now(){
		Calendar c = Calendar.getInstance();
		return new int[]{ c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR) };
	}

	// ---- entry: age gate, then account, then the hub ----

	public void start(){
		if (api == null){ offline(); return; }
		ui.busy("Checking Community access...");
		CommunityAgeGate.askPlay(play -> gateThen(CommunityAgeGate.check(play, storage)));
	}

	// Shows the locked or birth-date screen, or goes on to sign-in. (Never shows a screen after starting sign-in:
	// with a fast answer that would cover the screen sign-in just showed.)
	private void gateThen(CommunityAgeGate.Result r){
		if (r == CommunityAgeGate.Result.LOCKED) ui.show(locked());
		else if (r == CommunityAgeGate.Result.ASK_BIRTH_DATE) ui.show(askBirth());
		else signedInOrOut();
	}

	private Screen offline(){
		Screen s = new Screen("Community", "The Community server isn't live yet. Here's what's coming.");
		s.back = ui::leave;
		ui.show(s);
		return s;
	}

	/** Chris, Nov 19: Community is blocked until the player has downloaded and installed the update. The server
	 *  refuses older app versions with update_required; nothing else in Community opens, and cached screens are dropped. */
	static boolean updateRequired(CommunityApi.Result r){
		return r != null && (r.status == 426 || "update_required".equals(r.error));
	}

	private Screen updateScreen(){
		synchronized (CACHE){ CACHE.clear(); }
		Screen s = new Screen("Community", "Update required");
		s.note("Community needs the newest version of ADHD Pixel Dungeon. Download and install the update from Google Play, then open Community again. The rest of the game still works.");
		s.item("Back", null, ui::leave);
		s.back = ui::leave;
		return s;
	}

	private Screen locked(){
		Screen s = new Screen("Community", "Community is for players 16 and older.");
		s.note("The rest of ADHD Pixel Dungeon is yours to play. Community's submissions, comments and votes are only open to players 16 and up.");
		s.item("Back", null, ui::leave);
		s.back = ui::leave;
		return s;
	}

	private Screen askBirth(){
		Screen s = new Screen("Community", "Community is for players 16 and older.");
		s.note("What month and year were you born? Your answer only checks if you're 16 or older. The date itself is never saved, and you only get one try.");
		s.item("Enter birth month and year", null, this::askMonth);
		s.item("Not now", null, ui::leave);
		s.back = ui::leave;
		return s;
	}

	private void askMonth(){
		ui.ask("Birth month", "What month were you born? Enter 1 to 12.", "", 2, false, m -> {
			Integer month = number(m, 1, 12);
			if (month == null){ if (m != null) ui.show(askBirth().warning("That isn't a month from 1 to 12.")); else ui.show(askBirth()); return; }
			ui.ask("Birth year", "What year were you born? For example, 1995.", "", 4, false, y -> {
				Integer year = number(y, today[1] - 120, today[1]);
				if (year == null){ if (y != null) ui.show(askBirth().warning("That isn't a real birth year.")); else ui.show(askBirth()); return; }
				gateThen(CommunityAgeGate.answer(month, year, today[0], today[1], storage));
			});
		});
	}

	private static Integer number(String s, int min, int max){
		if (s == null) return null;
		try { int v = Integer.parseInt(s.trim()); return v < min || v > max ? null : v; } catch (NumberFormatException e){ return null; }
	}

	private void signedInOrOut(){
		if (!api.signedIn()){ ui.show(signedOut(null)); return; }
		// Coming back to Community (Back from "What's coming next", reopening it from the title) asked the server who you are every time,
		// behind "Talking to the Community server..." (Chris, Nov 25). With an answer kept from earlier, show the menu at once and check
		// in the background; a sign-out found there still signs you out.
		final String k = cacheKey("me");
		final String old;
		synchronized (CACHE){ old = CACHE.get(k); }
		if (old != null && !EMPTY_BODY.equals(old)){
			me = new JsonReader().parse(old);
			if (me.getBoolean("ageOk", false)){
				hub();
				runner.run(api::me, r -> {
					if (updateRequired(r)){ ui.show(updateScreen()); return; }
					if (r.status == 401){ forget(); ui.show(signedOut("You were signed out. Please sign in again.")); return; }
					if (r.ok() && r.body != null){ store(k, r); me = r.body; }
				});
				return;
			}
		}
		call(api::me, r -> {
			if (r.status == 0 || r.status >= 500){ ui.show(cantReach()); return; } // keep the sign-in: it's the connection, not the account
			if (!r.ok()){ forget(); ui.show(signedOut(message(r))); return; }
			store(k, r);
			me = r.body;
			if (!me.getBoolean("ageOk", false)) call(api::ageGatePassed, x -> hub());
			else hub();
		});
	}

	private Screen cantReach(){
		Screen s = new Screen("Community", null);
		s.warning("Can't reach the Community server. Check your connection and try again. You're still signed in.");
		s.item("Try again", null, this::signedInOrOut);
		s.item("Back", null, ui::leave);
		s.back = ui::leave;
		return s;
	}

	// ---- signed out ----

	private Screen signedOut(String problem){
		Screen s = new Screen("Community", "Sign in to post ideas, comment and vote.");
		if (problem != null) s.warning(problem);
		if (google != null) s.item("Sign in with Google", null, this::signInGoogle);
		else if (!emailSignIn) s.warning("Community needs Google sign-in, which isn't available on this device.");
		if (emailSignIn){
			s.item("Sign in with email", null, this::signIn);
			s.item("Create an account", null, this::register);
			s.item("I have a verification code", null, () -> askEmailThen(this::askCode));
		}
		s.item("Back", null, ui::leave);
		s.back = ui::leave;
		return s;
	}

	private void signInGoogle(){
		ui.busy("Waiting for Google...");
		google.request(idToken -> CommunityAgeGate.deliver.accept(() -> {
			if (idToken == null){ ui.show(signedOut(null)); return; }
			call(() -> api.google(idToken), r -> {
				if (r.ok()){ remember(); signedInOrOut(); }
				else ui.show(signedOut(message(r)));
			});
		}));
	}

	private void askEmailThen(Consumer<String> next){
		ui.ask("Email", "Your email address.", "", 254, false, e -> { if (e == null) ui.show(signedOut(null)); else next.accept(e.trim()); });
	}

	private void signIn(){
		askEmailThen(email -> ui.ask("Password", "Your Community password.", "", 200, false, pw -> {
			if (pw == null){ ui.show(signedOut(null)); return; }
			call(() -> api.login(email, pw), r -> {
				if (r.ok()){ remember(); signedInOrOut(); }
				else if ("email_not_verified".equals(r.error)) askCode(email);
				else ui.show(signedOut(message(r)));
			});
		}));
	}

	private void register(){
		askEmailThen(email -> ui.ask("Password", "Pick a password, at least 8 characters.", "", 200, false, pw -> {
			if (pw == null){ ui.show(signedOut(null)); return; }
			ui.ask("Name", "The name other players see (up to 24 characters).", "", 24, false, name -> {
				if (name == null){ ui.show(signedOut(null)); return; }
				call(() -> api.register(email, pw, name), r -> {
					if (r.ok()) askCode(email);
					else ui.show(signedOut(message(r)));
				});
			});
		}));
	}

	private void askCode(String email){
		ui.ask("Verification code", "Enter the 6-digit code we emailed to " + email + ". Leave it empty to get a new code.", "", 6, false, code -> {
			if (code == null){ ui.show(signedOut(null)); return; }
			if (code.trim().isEmpty()){ call(() -> api.resendCode(email), r -> askCode(email)); return; }
			call(() -> api.verify(email, code.trim()), r -> {
				if (r.ok()){ remember(); signedInOrOut(); }
				else ui.show(signedOut(message(r)));
			});
		});
	}

	private void remember(){ storage.token(api.token()); }
	private void forget(){ api.token(null); storage.token(null); me = null; synchronized (CACHE){ CACHE.clear(); } }

	// ---- hub ----

	private void hub(){
		ReviewRun pending = pendingReview;
		if (pending != null){ pendingReview = null; sendForReview(pending); return; }
		ReviewClaim done = finishedReview;
		if (done != null){ reviewVerdict(done, null); return; }
		cached("season", api::season, r -> {
			season = r.ok() ? r.body.get("season") : null;
			if (season != null && season.isNull()) season = null;
			Screen s = new Screen("Community", "Signed in as " + me.getString("displayName", "?") + ".");
			JsonValue next = r.ok() ? r.body.get("next") : null;
			if (next != null && next.isNull()) next = null;
			if (season == null && next == null) s.note("No season is running right now.");
			else if (season != null) s.heading(season.getString("title", "Season")).note(phaseText(season.getString("phase", "")));
			if (next != null && (season == null || "finished".equals(phase()))) s.heading(next.getString("title", "Next season")).note(startsText(next.getLong("startsAt", 0)));
			s.item("Submissions", "Ideas and their discussions", this::submissions);
			s.item("Voting and results", "Upvotes, the top 16 and the tournament", this::results);
			s.item("Selected content", "Every champion and how its development is going", this::champions);
			s.item("Run reviews", "Your runs' review progress, and runs waiting for reviewers", this::reviews);
			if (com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModHeroSelection.ENABLED) s.item("Hero mods workshop", "Browse, download and share mod heroes", this::workshop);
			s.item("Account", "Sign out or delete your account", this::account);
			if (me.getBoolean("admin", false)) s.item("Moderation", "Reports, hide, remove, ban", this::moderation);
			s.item("What's coming next", "Run reviews, mods, events and more", ui::planned);
			s.item("Back", null, ui::leave);
			s.back = ui::leave;
			ui.show(s);
		});
	}

	/** Plan item 1 (Chris, Nov 25): players see when the next season opens, Eastern date. */
	static String startsText(long startsAt){
		if (startsAt <= 0) return "This season hasn't started yet.";
		java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.US);
		f.setTimeZone(java.util.TimeZone.getTimeZone("America/New_York"));
		return "Season starts " + f.format(new java.util.Date(startsAt)) + " (Eastern). Submissions open then.";
	}

	static String phaseText(String phase){
		switch (phase){
			case "submissions": return "Month 1: submissions are open. Post your ideas and discuss everyone's.";
			case "voting": return "Month 2: voting. Upvote the ideas you want; the top 16 go to the tournament. You can't upvote your own.";
			case "tournament": return "Month 3: the bracket. The top 16 go one-on-one, 16 to 8 to 4 to 2 to 1, one round a week.";
			case "finished": return "This season is over. See the results for the winner.";
			default: return "The season hasn't started yet.";
		}
	}

	private String phase(){ return season == null ? "" : season.getString("phase", ""); }

	// ---- submissions ----

	// Post types (Chris, Nov 25, option 1). Server ids in order; one vote, one bracket, one champion regardless of type.
	static final String[] POST_TYPES = {"hero", "item", "enemy", "boss", "mechanic", "qol", "other"};
	static final String[] POST_TYPE_NAMES = {"Hero", "Item", "Enemy", "Boss", "Mechanic", "Quality of life", "Other"};

	// Unknown or missing (older server) types read as Other, as the server stores old posts.
	static String postTypeName(String id){
		for (int i = 0; i < POST_TYPES.length; i++) if (POST_TYPES[i].equals(id)) return POST_TYPE_NAMES[i];
		return "Other";
	}

	private String typeFilter; // null = every type

	private void submissions(){
		cached("submissions", api::submissions, r -> {
			Screen s = new Screen("Submissions", null);
			s.back = this::hub;
			if (!r.ok()){ s.warning(message(r)); s.item("Back", null, this::hub); ui.show(s); return; }
			if ("submissions".equals(phase())) s.item("+ Post an idea", null, this::post);
			s.item("Show: " + (typeFilter == null ? "All types" : postTypeName(typeFilter)), "Filter ideas by type", this::chooseFilter);
			JsonValue list = r.body.get("submissions");
			int shown = 0;
			if (list != null) for (JsonValue v = list.child; v != null; v = v.next){
				final String id = v.getString("id");
				String type = v.getString("type", "other");
				if (typeFilter != null && !typeFilter.equals(postTypeName(type).equals("Other") ? "other" : type)) continue;
				shown++;
				s.item(v.getString("title", ""), postTypeName(type) + " · by " + v.get("author").getString("displayName", "?") + " · " + v.getInt("comments", 0) + " comments", () -> detail(id));
			}
			if (shown == 0) s.note(typeFilter == null ? "No ideas yet this season." : "No " + postTypeName(typeFilter) + " ideas yet this season.");
			s.item("Back", null, this::hub);
			ui.show(s);
		});
	}

	private void chooseFilter(){
		Screen s = new Screen("Show ideas", null);
		s.back = this::submissions;
		s.item("All types", null, () -> { typeFilter = null; submissions(); });
		for (int i = 0; i < POST_TYPES.length; i++){
			final String t = POST_TYPES[i];
			s.item(POST_TYPE_NAMES[i], null, () -> { typeFilter = t; submissions(); });
		}
		s.item("Back", null, this::submissions);
		ui.show(s);
	}

	private void post(){
		Screen s = new Screen("Post an idea", "What kind of idea is it?");
		s.back = this::submissions;
		for (int i = 0; i < POST_TYPES.length; i++){
			final String t = POST_TYPES[i];
			s.item(POST_TYPE_NAMES[i], null, () -> post(t));
		}
		s.item("Back", null, this::submissions);
		ui.show(s);
	}

	private void post(String type){
		ui.ask("Title", "A short title for your idea.", "", 80, false, title -> {
			if (title == null){ submissions(); return; }
			ui.ask("Your idea", "Describe it. Others can comment and ask you questions.", "", 4000, true, body -> {
				if (body == null){ submissions(); return; }
				call(() -> api.submit(title, body, type), r -> { if (r.ok()){ uncache("submissions"); detail(r.string("id")); } else problem(r, this::submissions); });
			});
		});
	}

	private void detail(String id){
		// In Month 2 the results say how many upvotes it has and whether I've upvoted it, so the button can say so.
		if ("voting".equals(phase())){
			call(api::results, rr -> {
				int count = -1; boolean mine = false;
				if (rr.ok()){
					if (rr.body.get("myUpvotes") != null) for (JsonValue v = rr.body.get("myUpvotes").child; v != null; v = v.next) if (id.equals(v.asString())) mine = true;
					if (rr.body.get("standings") != null) for (JsonValue v = rr.body.get("standings").child; v != null; v = v.next) if (id.equals(v.getString("id", ""))) count = v.getInt("upvotes", 0);
				}
				detail(id, count, mine);
			});
		} else detail(id, -1, false);
	}

	private void detail(String id, int upvotes, boolean upvoted){
		call(() -> api.submission(id), r -> {
			if (!r.ok()){ problem(r, this::submissions); return; }
			JsonValue b = r.body;
			String authorId = b.get("author").getString("id");
			boolean mine = me != null && authorId.equals(me.getString("id"));
			Screen s = new Screen(b.getString("title", ""), postTypeName(b.getString("type", "other")) + " · by " + b.get("author").getString("displayName", "?"));
			s.back = this::submissions;
			s.note(b.getString("body", ""));
			if ("voting".equals(phase()) && !mine){
				String count = upvotes < 0 ? null : upvotes + (upvotes == 1 ? " upvote" : " upvotes");
				s.item(upvoted ? "Take back my upvote" : "Upvote this idea", count, () ->
						call(() -> api.upvote(id, !upvoted), x -> { if (x.ok()){ uncache("submissions", "results"); detail(id); } else problem(x, () -> detail(id)); }));
			} else if ("voting".equals(phase()) && upvotes >= 0) s.note(upvotes + (upvotes == 1 ? " upvote" : " upvotes") + " (you can't upvote your own idea)");
			s.item("Comment", null, () -> comment(id, null));
			if (!mine){
				s.item("Report this idea", null, () -> report("submission", id, () -> detail(id)));
				s.item("Block " + b.get("author").getString("displayName", "this player"), "Hide everything they post from you", () -> block(authorId, this::submissions));
			}
			s.heading("Discussion");
			JsonValue comments = b.get("comments");
			if (comments == null || comments.size == 0) s.note("No comments yet.");
			else threaded(s, id, comments);
			s.item("Back", null, this::submissions);
			ui.show(s);
		});
	}

	// Comments in thread order: each reply right under its parent, indented by depth.
	static List<JsonValue> threadOrder(JsonValue comments, List<Integer> depths){
		List<JsonValue> all = new ArrayList<>();
		for (JsonValue c = comments.child; c != null; c = c.next) all.add(c);
		List<JsonValue> out = new ArrayList<>();
		addChildren(all, null, 0, out, depths);
		return out;
	}

	private static void addChildren(List<JsonValue> all, String parent, int depth, List<JsonValue> out, List<Integer> depths){
		for (JsonValue c : all){
			String p = c.get("parentId") == null || c.get("parentId").isNull() ? null : c.getString("parentId");
			if (parent == null ? p != null : !parent.equals(p)) continue;
			out.add(c); depths.add(depth);
			addChildren(all, c.getString("id"), Math.min(depth + 1, 6), out, depths);
		}
	}

	private void threaded(Screen s, String submissionId, JsonValue comments){
		List<Integer> depths = new ArrayList<>();
		List<JsonValue> ordered = threadOrder(comments, depths);
		for (int i = 0; i < ordered.size(); i++){
			JsonValue c = ordered.get(i);
			String indent = "";
			for (int d = 0; d < depths.get(i); d++) indent += "  ";
			final String cid = c.getString("id");
			JsonValue author = c.get("author");
			if (author == null || author.isNull()){
				s.item(indent + (depths.get(i) > 0 ? "> " : "") + "[removed]", null, () -> comment(submissionId, cid));
				continue;
			}
			final String aid = author.getString("id"), name = author.getString("displayName", "?");
			s.item(indent + (depths.get(i) > 0 ? "> " : "") + name + ": " + c.getString("body", ""), null, () -> commentOptions(submissionId, cid, aid, name));
		}
	}

	private void commentOptions(String submissionId, String commentId, String authorId, String name){
		Screen s = new Screen("Comment", "by " + name);
		s.back = () -> detail(submissionId);
		s.item("Reply", null, () -> comment(submissionId, commentId));
		if (me == null || !authorId.equals(me.getString("id"))){
			s.item("Report this comment", null, () -> report("comment", commentId, () -> detail(submissionId)));
			s.item("Block " + name, "Hide everything they post from you", () -> block(authorId, () -> detail(submissionId)));
		}
		s.item("Back", null, () -> detail(submissionId));
		ui.show(s);
	}

	private void comment(String submissionId, String parentId){
		ui.ask(parentId == null ? "Comment" : "Reply", "Be kind. Comments never count as votes.", "", 2000, true, body -> {
			if (body == null || body.trim().isEmpty()){ detail(submissionId); return; }
			call(() -> api.comment(submissionId, parentId, body), r -> { if (r.ok()){ uncache("submissions"); detail(submissionId); } else problem(r, () -> detail(submissionId)); });
		});
	}

	private void report(String kind, String id, Runnable after){
		ui.ask("Report", "What's wrong with it? (one line)", "", 200, false, reason -> {
			if (reason == null || reason.trim().isEmpty()){ after.run(); return; }
			call(() -> api.report(kind, id, reason), r -> {
				if (r.ok()) done("Reported. Thanks, a moderator will look at it.", after); else problem(r, after);
			});
		});
	}

	private void block(String accountId, Runnable after){
		call(() -> api.block(accountId), r -> { if (r.ok()){ uncacheLists(); done("Blocked. You won't see their posts or comments.", after); } else problem(r, after); });
	}

	// ---- voting and the tournament ----


	private void results(){
		cached("results", api::results, this::showResults);
	}

	private CommunityApi.Result lastResults;

	private void showResults(CommunityApi.Result r){
		lastResults = r;
		{
			Screen s = new Screen("Voting and results", null);
			s.back = this::hub;
			if (!r.ok()){ s.warning(message(r)); s.item("Back", null, this::hub); ui.show(s); return; }
			JsonValue b = r.body;
			String phase = b.get("season") == null || b.get("season").isNull() ? "" : b.get("season").getString("phase", "");
			s.note(phaseText(phase));
			if ("voting".equals(phase)){
				s.heading("Upvotes so far");
				Set<String> mine = new LinkedHashSet<>();
				if (b.get("myUpvotes") != null) for (JsonValue v = b.get("myUpvotes").child; v != null; v = v.next) mine.add(v.asString());
				int rank = 1;
				for (JsonValue v = b.get("standings").child; v != null; v = v.next, rank++){
					final String id = v.getString("id");
					s.item(rank + ". " + v.getString("title", ""), v.getInt("upvotes", 0) + " upvotes" + (mine.contains(id) ? " · you upvoted" : ""), () -> detail(id));
				}
			} else if ("tournament".equals(phase) || "finished".equals(phase)){
				int week = b.getInt("week", 1);
				if (b.get("champion") != null) s.heading("Winner: " + b.get("champion").getString("title", ""));
				JsonValue rounds = b.get("rounds");
				if (rounds != null) for (JsonValue round = rounds.child; round != null; round = round.next) matchups(s, round, "tournament".equals(phase) && round.getInt("week") == week);
			}
			s.item("Back", null, this::hub);
			ui.show(s);
		}
	}

	private void matchups(Screen s, JsonValue round, boolean open){
		s.heading("Week " + round.getInt("week") + (open ? " (voting now)" : ""));
		for (JsonValue m = round.get("matchups").child; m != null; m = m.next){
			final int idx = m.getInt("matchup");
			if (m.getBoolean("bye", false)){
				JsonValue a = m.get("a"), b = m.get("b");
				JsonValue only = a != null && !a.isNull() ? a : b;
				if (only != null && !only.isNull()) s.note(only.getString("title", "") + " goes through (no opponent)");
				continue; // an empty slot (fewer than 8 entries) shows nothing
			}
			JsonValue a = m.get("a"), bb = m.get("b");
			String winner = m.get("winner") == null || m.get("winner").isNull() ? null : m.getString("winner");
			for (JsonValue side : new JsonValue[]{a, bb}){
				final String id = side.getString("id");
				String label = side.getString("title", "") + (id.equals(winner) ? "  (won)" : "");
				String detail = side.getInt("votes", 0) + " votes";
				if (open) s.item("Vote: " + label, detail, () -> call(() -> api.vote(idx, id), r -> { if (r.ok()){ uncache("results"); results(); } else problem(r, this::results); }));
				else s.item(label, detail, () -> detail(id));
			}
		}
	}

	// ---- account ----

	private void account(){
		Screen s = new Screen("Account", "Signed in as " + me.getString("displayName", "?") + ".");
		s.back = this::hub;
		s.item("Blocked players", "See and unblock players you blocked", this::blockedList);
		s.item("Sign out", null, () -> call(api::logout, r -> { forget(); ui.show(signedOut(null)); }));
		s.item("Delete my account", "Removes your account, ideas, comments and votes for good", () ->
				ui.ask("Delete account", "Type DELETE to remove your account and everything you posted. This can't be undone.", "", 10, false, t -> {
					if (t == null || !t.trim().equals("DELETE")){ account(); return; }
					call(api::deleteAccount, r -> { if (r.ok()){ forget(); done("Your account and everything you posted are gone.", () -> ui.show(signedOut(null))); } else problem(r, this::account); });
				}));
		s.item("Back", null, this::hub);
		ui.show(s);
	}

	private void blockedList(){
		call(api::blocked, r -> {
			Screen s = new Screen("Blocked players", "Tap a player to unblock them.");
			s.back = this::account;
			if (!r.ok()){ s.warning(message(r)); s.item("Back", null, this::account); ui.show(s); return; }
			JsonValue list = r.body.get("blocked");
			if (list == null || list.size == 0) s.note("You haven't blocked anyone.");
			else for (JsonValue v = list.child; v != null; v = v.next){
				final String id = v.getString("id"), name = v.getString("displayName", "?");
				s.item(name, "Tap to unblock", () -> call(() -> api.unblock(id), x -> {
					if (x.ok()){ uncacheLists(); done(name + " is unblocked.", this::blockedList); } else problem(x, this::blockedList);
				}));
			}
			s.item("Back", null, this::account);
			ui.show(s);
		});
	}

	// ---- selected content (Chris, Nov 18): champions, their status, dev notes and comments ----

	static final String[] CHAMPION_STATUSES = {"planned", "in_development", "testing", "released"};

	static String statusText(String status){
		switch (status == null ? "" : status){
			case "planned": return "Planned";
			case "in_development": return "In development";
			case "testing": return "Testing";
			case "released": return "Released";
			default: return "?";
		}
	}

	// ---- run reviews (Chris's design, Nov 18). "Take this run" asks once more (one attempt, no retry), then claims the
	// run and starts the game from the claim. The seed is never shown.

	static String setupText(JsonValue setup){
		if (setup == null || setup.isNull()) return "";
		StringBuilder b = new StringBuilder();
		String hero = setup.getString("heroClass", null);
		if (hero != null && !hero.isEmpty()) b.append(hero.substring(0, 1).toUpperCase()).append(hero.substring(1).toLowerCase());
		String diff = setup.getString("difficulty", null);
		if (diff != null && !diff.isEmpty()) b.append(b.length() > 0 ? " · " : "").append(diff);
		int ch = setup.getInt("challenges", 0);
		if (ch > 0) b.append(b.length() > 0 ? " · " : "").append(Integer.bitCount(ch)).append(Integer.bitCount(ch) == 1 ? " challenge" : " challenges");
		return b.toString();
	}

	private void reviews(){
		call(api::reviewsMine, mine -> call(api::reviewsOpen, open -> {
			Screen s = new Screen("Run reviews", "Other players replay your finished run from the same start. You see their verdicts once 5 have played it.");
			s.back = this::hub;
			s.heading("Your runs");
			if (!mine.ok()) s.warning(message(mine));
			else {
				JsonValue list = mine.body.get("runs");
				if (list == null || list.size == 0) s.note("You haven't sent a run for review.");
				else for (JsonValue v = list.child; v != null; v = v.next){
					final JsonValue run = v;
					boolean closed = "closed".equals(v.getString("status", ""));
					if (closed) s.item(setupText(v.get("setup")), "Reviewed: " + v.getInt("done", 0) + "/" + v.getInt("needed", 5) + " · tap for verdicts", () -> verdictsScreen(run));
					else s.note(setupText(v.get("setup")) + ": " + v.getInt("done", 0) + "/" + v.getInt("needed", 5) + " reviewers so far");
				}
			}
			s.heading("Waiting for reviewers");
			if (!open.ok()) s.warning(message(open));
			else {
				JsonValue list = open.body.get("runs");
				if (list == null || list.size == 0) s.note("No runs are waiting for review.");
				else for (JsonValue v = list.child; v != null; v = v.next){
					final JsonValue run = v;
					s.item(v.getString("player", "?") + ": " + setupText(v.get("setup")), v.getInt("done", 0) + "/" + v.getInt("needed", 5) + " reviewed · " + reviewTypeText(v), () -> openRun(run));
				}
			}
			s.item("Back", null, this::hub);
			ui.show(s);
		}));
	}

	/** A finished (lost) run the player chose to send for review from the rankings screen. Set before opening
	 *  Community; the hub shows the send screen once, after age gate and sign-in. */
	public static final class ReviewRun {
		public final String seed; public final int deathDepth;
		public final java.util.Map<String, String> setup, ending;
		public ReviewRun(String seed, int deathDepth, java.util.Map<String, String> setup, java.util.Map<String, String> ending){
			this.seed = seed; this.deathDepth = deathDepth; this.setup = setup; this.ending = ending;
		}
	}
	public static volatile ReviewRun pendingReview = null;

	private void sendForReview(ReviewRun run){
		Screen s = new Screen("Send this run for review", "5 other players will replay it from the same start. "
				+ "They never see the seed's code or how you died until they've played. You see their verdicts once all 5 are done.");
		s.back = this::hub;
		s.heading(setupText(setupJson(run.setup)) + (run.deathDepth > 0 ? " · died on floor " + run.deathDepth : ""));
		s.note("Pick what kind of review you want. Reviewers see your choice before they write. Bullying can still be reported.");
		s.item("Roasts allowed", null, () -> submitReview(run, "roasts"));
		s.item("Straight advice only", null, () -> submitReview(run, "advice"));
		s.item("Encouraging", null, () -> submitReview(run, "encouraging"));
		s.item("Don't send", null, this::hub);
		ui.show(s);
	}

	private void submitReview(ReviewRun run, String reviewType){
		ui.busy("Sending your run...");
		call(() -> api.reviewSubmit(run.seed, run.deathDepth, run.setup, run.ending, reviewType), r -> {
			Screen s = new Screen("Run reviews", r.ok() ? "Sent. Check Run reviews to see how many players have reviewed it." : message(r));
			s.back = this::hub;
			if (r.ok()) s.item("Run reviews", null, this::reviews);
			s.item("Back", null, this::hub);
			ui.show(s);
		});
	}

	/** The review type the reviewed player picked; an old server's roasts flag maps onto it. */
	static String reviewTypeText(JsonValue run){
		return typeText(run.getString("reviewType", run.getBoolean("roasts", false) ? "roasts" : "advice"));
	}

	private static JsonValue setupJson(java.util.Map<String, String> m){
		JsonValue o = new JsonValue(JsonValue.ValueType.object);
		if (m != null) for (java.util.Map.Entry<String, String> e : m.entrySet()){
			if ("challenges".equals(e.getKey())){
				try { o.addChild(e.getKey(), new JsonValue(Long.parseLong(e.getValue()))); continue; } catch (NumberFormatException ignored) {}
			}
			o.addChild(e.getKey(), new JsonValue(e.getValue()));
		}
		return o;
	}

	/** A review playthrough that has ended (reached the original death floor, or died first). Set by the game when the
	 *  review run ends; the hub then asks for the verdict until it is sent. Kept while the server can't be reached, so
	 *  the one attempt is never lost; cleared once the server answers (accepted, or refused for good). */
	public static volatile ReviewClaim finishedReview = null;

	private void reviewVerdict(ReviewClaim claim, String warning){
		Screen s = new Screen("Your review", claim.reachedDeathFloor()
				? "You reached floor " + claim.deathDepth + ", where this player died."
				: "You died on floor " + claim.reachedDepth() + ", before the floor where this player died. Your review still counts.");
		s.back = this::reviewVerdictLater;
		if (warning != null) s.warning(warning);
		s.note("Could this run have been saved? Then say what the player should have done. There are no retries.");
		s.item("Salvageable", null, () -> reviewTone(claim, true));
		s.item("Not salvageable", null, () -> reviewTone(claim, false));
		s.item("Later", null, this::reviewVerdictLater);
		ui.show(s);
	}

	/** Leaving without sending keeps the verdict waiting; Community opens on it next time. */
	private void reviewVerdictLater(){
		Screen s = new Screen("Your review", "Your verdict hasn't been sent. Community will ask for it again next time.");
		s.item("Back", null, ui::leave);
		ui.show(s);
	}

	/** Chris, Nov 19: the reviewer tags the tone of their own comment. A tone that doesn't match what the player asked
	 *  for is held for a moderator instead of shown (the vote still counts). */
	private void reviewTone(ReviewClaim claim, boolean salvageable){
		Screen s = new Screen("Your comment's tone", "This player asked for: " + typeText(claim.reviewType) + ".");
		s.back = () -> reviewVerdict(claim, null);
		s.note("Pick the tone of what you're about to write. If it doesn't match what they asked for, a moderator checks it before it's shown.");
		s.item("Roast", null, () -> reviewAdvice(claim, salvageable, "roasts"));
		s.item("Straight advice", null, () -> reviewAdvice(claim, salvageable, "advice"));
		s.item("Encouraging", null, () -> reviewAdvice(claim, salvageable, "encouraging"));
		ui.show(s);
	}

	/** Chris's design: how the original run ended is shown only after the reviewer has played and sent their result. */
	static void endingNotes(Screen s, JsonValue e){
		if (e == null || !e.isObject()) return;
		s.heading("How this player's run ended");
		String cause = e.getString("cause", null), gear = e.getString("gear", null), lvl = e.getString("heroLevel", null);
		if (cause != null) s.note("Killed by: " + cause.replaceAll("([a-z])([A-Z])", "$1 $2"));
		if (lvl != null) s.note("Hero level: " + lvl);
		if (gear != null) s.note("Their gear: " + gear);
	}

	static String typeText(String t){
		if ("roasts".equals(t)) return "roasts allowed";
		if ("encouraging".equals(t)) return "encouraging";
		return "straight advice only";
	}

	private void reviewAdvice(ReviewClaim claim, boolean salvageable, String tone){
		ui.ask("What should they have done?", "Your advice for this player (up to 2000 characters).", "", 2000, true, advice -> {
			if (advice == null){ reviewVerdict(claim, null); return; }
			if (advice.trim().isEmpty()){ reviewVerdict(claim, "Write some advice before sending."); return; }
			ui.busy("Sending your review...");
			call(() -> api.reviewResult(claim.requestId, claim.reachedDepth(), salvageable, tone, advice.trim()), r -> {
				if (r.status == 0 || r.status >= 500){ reviewVerdict(claim, message(r)); return; } // try again later: keep it
				if (!r.ok() && "not_allowed_words".equals(r.error)){ reviewVerdict(claim, message(r)); return; }
				claim.sent = true; if (finishedReview == claim) finishedReview = null;
				Screen s = new Screen("Your review", !r.ok() ? message(r)
						: r.body != null && r.body.getBoolean("held", false)
						? "Sent. Your comment doesn't match what this player asked for, so a moderator will check it before it's shown. Your vote still counts."
						: "Sent. Thanks for reviewing.");
				s.back = this::hub;
				if (r.ok() && r.body != null) endingNotes(s, r.body.get("ending"));
				s.item("Run reviews", null, this::reviews);
				s.item("Back", null, this::hub);
				ui.show(s);
			});
		});
	}

	private void openRun(JsonValue run){
		Screen s = new Screen(run.getString("player", "?") + "'s run", setupText(run.get("setup")));
		s.back = this::reviews;
		s.note("You'd play this exact start once, with no retry. How it ended stays hidden until you've played.");
		s.note("This player asked for: " + reviewTypeText(run) + ".");
		s.item("Take this run", null, () -> confirmTake(run));
		s.item("Back", null, this::reviews);
		ui.show(s);
	}

	private void confirmTake(JsonValue run){
		Screen s = new Screen("Take this run?", "You get one attempt. If you die before the floor where this player died, "
				+ "it's a failed review, and it still counts. You can't retry.");
		s.back = () -> openRun(run);
		s.item("Start the run", null, () -> takeRun(run));
		s.item("Not now", null, () -> openRun(run));
		ui.show(s);
	}

	private void takeRun(JsonValue run){
		ui.busy("Getting the run...");
		call(() -> api.reviewClaim(run.getString("id", "")), r -> {
			ReviewClaim claim = r.ok() ? ReviewClaim.parse(r.body) : null;
			if (claim != null){ ui.playReview(claim); return; }
			Screen s = new Screen(run.getString("player", "?") + "'s run", setupText(run.get("setup")));
			s.back = this::reviews;
			s.warning(r.ok() ? "This run can't be played on this version of the game." : message(r));
			s.item("Back", null, this::reviews);
			ui.show(s);
		});
	}

	private void verdictsScreen(JsonValue run){
		Screen s = new Screen("Verdicts", setupText(run.get("setup")));
		s.back = this::reviews;
		JsonValue list = run.get("reviews");
		int salvageable = 0, n = 0;
		if (list != null) for (JsonValue v = list.child; v != null; v = v.next){ n++; if (v.getBoolean("salvageable", false)) salvageable++; }
		s.heading(salvageable + " of " + n + " said it was salvageable");
		if (list != null) for (JsonValue v = list.child; v != null; v = v.next){
			JsonValue who = v.get("reviewer");
			String name = who == null || who.isNull() ? "[removed]" : who.getString("displayName", "?");
			String how = v.getBoolean("survived", false) ? "got past where you died" : "died earlier, on floor " + v.getInt("reachedDepth", 0);
			s.heading(name + ": " + (v.getBoolean("salvageable", false) ? "salvageable" : "not salvageable") + " (" + how + ")");
			String advice = v.getString("advice", null);
			if (advice != null && !advice.isEmpty()){
				s.note(advice);
				String rid = v.getString("id", null);
				if (rid != null && who != null && !who.isNull()) s.item("Report " + name + "'s review", null, () -> report("review", rid, () -> verdictsScreen(run)));
			}
		}
		s.item("Back", null, this::reviews);
		ui.show(s);
	}

	private void champions(){
		cached("champions", api::champions, r -> {
			Screen s = new Screen("Selected content", "Each season's champion, newest first, and where its development is.");
			s.back = this::hub;
			if (!r.ok()) s.warning(message(r));
			else {
				JsonValue list = r.body.get("champions");
				if (list == null || list.size == 0) s.note("No season has finished yet. The first champion will show up here.");
				else for (JsonValue c = list.child; c != null; c = c.next){
					final String sid = c.getString("seasonId");
					s.item(c.getString("title", "?"), c.getString("seasonTitle", "") + " - " + statusText(c.getString("status", null)), () -> champion(sid));
				}
			}
			s.item("Back", null, this::hub);
			ui.show(s);
		});
	}

	private void champion(String sid){
		call(() -> api.champion(sid), r -> {
			if (!r.ok()){ problem(r, this::champions); return; }
			JsonValue b = r.body;
			JsonValue author = b.get("author");
			String by = author == null || author.isNull() ? "[deleted]" : author.getString("displayName", "?");
			Screen s = new Screen(b.getString("title", ""), "Champion of " + b.getString("seasonTitle", "?") + ", by " + by);
			s.back = this::champions;
			s.heading("Status: " + statusText(b.getString("status", null)));
			String body = b.getString("body", "");
			if (!body.isEmpty()) s.note(body);
			boolean admin = me != null && me.getBoolean("admin", false);
			s.heading("Development notes");
			JsonValue notes = b.get("notes");
			if (notes == null || notes.size == 0) s.note("No notes yet.");
			else for (JsonValue n = notes.child; n != null; n = n.next){
				final String nid = n.getString("id");
				if (admin) s.item(n.getString("body", ""), "Tap to delete this note", () ->
						call(() -> api.adminDeleteChampionNote(sid, nid), x -> { if (x.ok()){ uncache("champions"); champion(sid); } else problem(x, () -> champion(sid)); }));
				else s.note(n.getString("body", ""));
			}
			if (admin){
				s.item("Set status", "Planned, In development, Testing, Released", () -> championStatus(sid, b.getString("status", null)));
				s.item("Add a development note", null, () -> ui.ask("Development note", "Everyone can read this.", "", 4000, true, text -> {
					if (text == null || text.trim().isEmpty()){ champion(sid); return; }
					call(() -> api.adminChampionNote(sid, text), x -> { if (x.ok()){ uncache("champions"); champion(sid); } else problem(x, () -> champion(sid)); });
				}));
			}
			s.item("Comment", null, () -> championComment(sid, null));
			s.heading("Discussion");
			JsonValue comments = b.get("comments");
			if (comments == null || comments.size == 0) s.note("No comments yet.");
			else {
				List<Integer> depths = new ArrayList<>();
				List<JsonValue> ordered = threadOrder(comments, depths);
				for (int i = 0; i < ordered.size(); i++){
					JsonValue c = ordered.get(i);
					String indent = "";
					for (int d = 0; d < depths.get(i); d++) indent += "  ";
					if (depths.get(i) > 0) indent += "> ";
					final String cid = c.getString("id");
					JsonValue a = c.get("author");
					if (a == null || a.isNull()){ s.item(indent + "[removed]", null, () -> championComment(sid, cid)); continue; }
					final String aid = a.getString("id"), name = a.getString("displayName", "?");
					s.item(indent + name + ": " + c.getString("body", ""), null, () -> championCommentOptions(sid, cid, aid, name));
				}
			}
			s.item("Back", null, this::champions);
			ui.show(s);
		});
	}

	private void championStatus(String sid, String current){
		Screen s = new Screen("Set status", "Everyone sees this on the champion's page.");
		s.back = () -> champion(sid);
		for (String st : CHAMPION_STATUSES) s.check(statusText(st), null, st.equals(current), () ->
				call(() -> api.adminChampionStatus(sid, st), x -> { if (x.ok()){ uncache("champions"); champion(sid); } else problem(x, () -> champion(sid)); }));
		s.item("Back", null, () -> champion(sid));
		ui.show(s);
	}

	private void championCommentOptions(String sid, String commentId, String authorId, String name){
		Screen s = new Screen("Comment", "by " + name);
		s.back = () -> champion(sid);
		s.item("Reply", null, () -> championComment(sid, commentId));
		if (me == null || !authorId.equals(me.getString("id"))){
			s.item("Report this comment", null, () -> report("championComment", commentId, () -> champion(sid)));
			s.item("Block " + name, "Hide everything they post from you", () -> block(authorId, () -> champion(sid)));
		}
		s.item("Back", null, () -> champion(sid));
		ui.show(s);
	}

	private void championComment(String sid, String parentId){
		ui.ask(parentId == null ? "Comment" : "Reply", "Be kind.", "", 2000, true, body -> {
			if (body == null || body.trim().isEmpty()){ champion(sid); return; }
			call(() -> api.championComment(sid, parentId, body), r -> { if (r.ok()) champion(sid); else problem(r, () -> champion(sid)); });
		});
	}

	// ---- moderation (Chris) ----

	// ---- hero mod workshop (Chris, Nov 19: data + art only; downloads are re-checked by ModInstaller) ----

	private void workshop(){ workshopList(false); }

	private void workshopList(boolean subscribedOnly){
		cached(subscribedOnly ? "mods-subscribed" : "mods", subscribedOnly ? api::modsSubscribed : api::mods, r -> {
			Screen s = new Screen(subscribedOnly ? "Subscribed mods" : "Hero mods workshop",
					"Mod heroes are local only: their runs never go to Community and never unlock normal badges.");
			s.back = this::hub;
			if (!r.ok()) s.warning(message(r));
			else {
				JsonValue list = r.body.get("mods");
				if (list == null || list.size == 0) s.note(subscribedOnly ? "You haven't subscribed to any mods." : "No mods have been shared yet.");
				else for (JsonValue m = list.child; m != null; m = m.next){
					final String id = m.getString("id");
					JsonValue a = m.get("author");
					String by = a == null || a.isNull() ? "?" : a.getString("displayName", "?");
					s.item(m.getString("name", "?"), "by " + by + " · v" + m.getInt("version", 1) + " · " + m.getInt("subscribers", 0) + " subscribers"
							+ (installedIds().contains(m.getString("modId", "")) ? " · installed" : ""), () -> modDetail(id, subscribedOnly));
				}
			}
			if (subscribedOnly) s.item("All mods", null, () -> workshopList(false));
			else s.item("My subscriptions", null, () -> workshopList(true));
			if (modsDir != null) s.item("Upload one of my installed mods", "Signed in, 16+; hero.json up to 16 KB, sprite.png up to 64 KB", this::modUploadPick);
			s.item("Back", null, this::hub);
			ui.show(s);
		});
	}

	/** The download's base64 sprite as PNG bytes for the preview, or null if missing, unreadable or over the size limit. */
	static byte[] decodeSprite(String b64){
		if (b64 == null || b64.isEmpty()) return null;
		try {
			byte[] png = java.util.Base64.getDecoder().decode(b64);
			return png.length == 0 || png.length > com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.MAX_SPRITE_BYTES ? null : png;
		} catch (IllegalArgumentException e){ return null; }
	}

	private java.util.Set<String> installedIds(){
		java.util.Set<String> ids = new java.util.HashSet<>();
		if (modsDir == null) return ids;
		for (com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.Installed i
				: com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.scan(modsDir).heroes) ids.add(i.mod.id);
		return ids;
	}

	private void modDetail(String id, boolean fromSubscribed){
		Runnable back = () -> workshopList(fromSubscribed);
		call(api::modsSubscribed, subs -> call(() -> api.modDownload(id), r -> {
			if (!r.ok()){ problem(r, back); return; }
			JsonValue b = r.body;
			boolean subscribed = false;
			if (subs.ok() && subs.body.get("mods") != null)
				for (JsonValue m = subs.body.get("mods").child; m != null; m = m.next) if (id.equals(m.getString("id", ""))) subscribed = true;
			final boolean sub = subscribed;
			JsonValue a = b.get("author");
			final String authorId = a == null || a.isNull() ? "" : a.getString("id", "");
			String by = a == null || a.isNull() ? "?" : a.getString("displayName", "?");
			boolean installed = installedIds().contains(b.getString("modId", ""));
			Screen s = new Screen(b.getString("name", "?"), "by " + by + " · version " + b.getInt("version", 1) + " · id " + b.getString("modId", "?"));
			s.back = back;
			s.previewPng = decodeSprite(b.getString("sprite", null));
			if (s.previewPng != null) s.previewKey = "workshop:" + b.getString("modId", "?") + ":" + b.getInt("version", 1) + ":" + s.previewPng.length;
			String desc = b.getString("description", "");
			if (!desc.isEmpty()) s.note(desc);
			if (modsDir == null) s.warning("Downloads aren't available on this device.");
			else s.item(installed ? "Reinstall (replace my copy)" : "Download and install", "Then pick it from Mods on hero select", () -> {
				try {
					com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModInstaller.installDownload(modsDir, b.getString("json", null), b.getString("sprite", null));
					done("Installed. Pick it from Mods on the hero select screen.", () -> modDetail(id, fromSubscribed));
				} catch (com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModInstaller.Refused e){
					Screen x = new Screen("Community", null);
					x.warning("This mod can't be installed: " + e.getMessage() + ". Your installed mods weren't changed.");
					x.item("OK", null, () -> modDetail(id, fromSubscribed));
					x.back = () -> modDetail(id, fromSubscribed);
					ui.show(x);
				}
			});
			s.check("Subscribed", "Keep it in My subscriptions", sub, () ->
					call(() -> api.modSubscribe(id, !sub), x -> { if (x.ok()){ uncache("mods", "mods-subscribed"); modDetail(id, fromSubscribed); } else problem(x, () -> modDetail(id, fromSubscribed)); }));
			if (me != null && authorId.equals(me.getString("id", "")))
				s.item("Delete my mod from the workshop", "Copies already installed stay on players' devices", () ->
						call(() -> api.modDelete(id), x -> {
							if (x.ok()){ uncache("mods", "mods-subscribed"); done("Deleted from the workshop.", back); }
							else problem(x, () -> modDetail(id, fromSubscribed));
						}));
			else s.item("Report", null, () -> report("mod", id, () -> modDetail(id, fromSubscribed)));
			s.item("Back", null, back);
			ui.show(s);
		}));
	}

	private void modUploadPick(){
		com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary lib = com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.scan(modsDir);
		Screen s = new Screen("Upload a mod", "Pick one of your installed mods. Uploading a mod id you uploaded before replaces it (new version).");
		s.back = this::workshop;
		if (lib.heroes.isEmpty()) s.note("No valid mods are installed. Put your mod in the mods folder (see the modding guide) first.");
		for (com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.Installed i : lib.heroes)
			s.item(i.mod.name, "id " + i.mod.id, () -> modUpload(i));
		for (com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.Refused f : lib.refused)
			s.warning(f.folder + ": " + f.reason);
		s.item("Back", null, this::workshop);
		ui.show(s);
	}

	private void modUpload(com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.Installed i){
		// bounded no-follow read (readAllBytes loaded a swapped/grown or linked file whole before any size check)
		final byte[] png = com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.readBounded(i.sprite,
				com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.MAX_SPRITE_BYTES);
		if (png == null){
			Screen x = new Screen("Community", null);
			x.warning("Couldn't read that mod's sprite.png.");
			x.item("OK", null, this::modUploadPick);
			x.back = this::modUploadPick;
			ui.show(x);
			return;
		}
		call(() -> {
			try { return api.modUpload(i.json, png); }
			catch (IllegalArgumentException e){ return new CommunityApi.Result(400, null, "sprite".equals(e.getMessage()) ? "sprite_too_large" : "json_too_large"); }
		}, r -> {
			if (!r.ok()){ problem(r, this::modUploadPick); return; }
			uncache("mods", "mods-subscribed");
			done(r.body != null && r.body.getBoolean("updated", false) ? "Updated: players see the new version." : "Uploaded. Other players can find it in the workshop now.", this::workshop);
		});
	}

	private void moderation(){
		call(api::adminReports, r -> {
			Screen s = new Screen("Moderation", "Open reports, most reported first.");
			s.back = this::hub;
			s.item("Start a new season", "Opens on the 1st of next month", this::startSeason);
			s.item("Find a player", "Ban or unban anyone by name or email", this::findPlayer);
			if (!r.ok()){ s.warning(message(r)); s.item("Back", null, this::hub); ui.show(s); return; }
			JsonValue list = r.body.get("reports");
			if (list == null || list.size == 0) s.note("No open reports.");
			else for (JsonValue v = list.child; v != null; v = v.next){
				final String kind = v.getString("target_kind"), id = v.getString("target_id");
				final String author = v.getString("author_id", null), preview = v.getString("preview", "(deleted)");
				s.item(v.getInt("reports", 0) + "x " + kind + ": " + (preview == null ? "(deleted)" : preview), v.getString("reasons", ""), () -> moderate(kind, id, author, preview));
			}
			s.item("Back", null, this::hub);
			ui.show(s);
		});
	}

	private void moderate(String kind, String id, String authorId, String preview){
		Screen s = new Screen("Moderate " + kind, preview);
		s.back = this::moderation;
		if (!kind.equals("account")){
			s.item("Hide", "Hidden from everyone; can be restored", () -> call(() -> api.adminSetStatus("hide", kind, id), r -> after(r)));
			s.item("Remove", "Removed for good", () -> call(() -> api.adminSetStatus("remove", kind, id), r -> after(r)));
			s.item("Restore", "Make it visible again", () -> call(() -> api.adminSetStatus("restore", kind, id), r -> after(r)));
		}
		if (authorId != null){
			s.item("Ban the author", "Signs them out and hides everything they posted", () -> call(() -> api.adminBan(authorId, true), r -> after(r)));
			s.item("Unban the author", null, () -> call(() -> api.adminBan(authorId, false), r -> after(r)));
		}
		s.item("Back", null, this::moderation);
		ui.show(s);
	}

	private void findPlayer(){
		ui.ask("Find a player", "Part of their name, or their exact email.", "", 254, false, q -> {
			if (q == null || q.trim().isEmpty()){ moderation(); return; }
			call(() -> api.adminFind(q.trim()), r -> {
				Screen s = new Screen("Players matching \"" + q.trim() + "\"", null);
				s.back = this::moderation;
				if (!r.ok()){ s.warning(message(r)); s.item("Back", null, this::moderation); ui.show(s); return; }
				JsonValue list = r.body.get("accounts");
				if (list == null || list.size == 0) s.note("Nobody found.");
				else for (JsonValue v = list.child; v != null; v = v.next){
					final String id = v.getString("id"), name = v.getString("displayName", "?");
					final boolean banned = v.getBoolean("banned", false);
					String email = v.getString("email", null);
					s.item(name + (banned ? "  (banned)" : ""), (email == null ? "Google account" : email) + " · tap to " + (banned ? "unban" : "ban"),
							() -> call(() -> api.adminBan(id, !banned), x -> {
								if (x.ok()){ uncacheLists(); done(name + (banned ? " is unbanned." : " is banned and signed out."), this::moderation); }
								else problem(x, this::moderation);
							}));
				}
				s.item("Back", null, this::moderation);
				ui.show(s);
			});
		});
	}

	/** Chris, Nov 25: a season runs on calendar months, Eastern time. Submissions open at midnight on the 1st of the
	 *  month after the button is pressed, Month 2 voting on the next 1st, the Month 3 bracket on the 1st after that,
	 *  and the season ends on the following 1st. Returns those four instants (ms). */
	static long[] seasonMonths(long nowMs){
		java.util.Calendar c = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/New_York"));
		c.setTimeInMillis(nowMs);
		c.set(java.util.Calendar.DAY_OF_MONTH, 1);
		c.set(java.util.Calendar.HOUR_OF_DAY, 0);
		c.set(java.util.Calendar.MINUTE, 0);
		c.set(java.util.Calendar.SECOND, 0);
		c.set(java.util.Calendar.MILLISECOND, 0);
		long[] out = new long[4];
		for (int n = 0; n < 4; n++){ c.add(java.util.Calendar.MONTH, 1); out[n] = c.getTimeInMillis(); }
		return out;
	}

	static String seasonDates(long[] m){
		java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.US);
		f.setTimeZone(java.util.TimeZone.getTimeZone("America/New_York"));
		return "Submissions open " + f.format(new java.util.Date(m[0])) + ". Voting starts " + f.format(new java.util.Date(m[1]))
				+ ". The tournament starts " + f.format(new java.util.Date(m[2])) + " and ends " + f.format(new java.util.Date(m[3])) + ".";
	}

	// Title, then confirm the calendar-month dates.
	private void startSeason(){
		ui.ask("Season title", "What should this season be called?", "", 80, false, title -> {
			if (title == null || title.trim().isEmpty()){ moderation(); return; }
			final long[] m = seasonMonths(System.currentTimeMillis());
			Screen s = new Screen("Schedule " + title.trim() + "?", seasonDates(m));
			s.item("Schedule it", null, () -> call(() -> api.adminScheduleSeason(title.trim(), m), r -> {
				if (r.ok()){ uncacheLists(); done("Season scheduled. " + seasonDates(m), this::hub); }
				else problem(r, this::moderation);
			}));
			s.item("Cancel", null, this::moderation);
			s.back = this::moderation;
			ui.show(s);
		});
	}

	private void after(CommunityApi.Result r){ if (r.ok()){ uncacheLists(); moderation(); } else problem(r, this::moderation); }

	// ---- helpers ----

	// Speed (Chris, Nov 18: "cache the last Community screen, refresh in background"). The last good answer for each
	// list screen is kept for this app session (cleared on sign-out), shown at once, then refreshed quietly; the screen
	// is redrawn only if the answer changed and the player hasn't moved on. Errors never replace a cached screen.
	static final Map<String, String> CACHE = new HashMap<>();
	private int uiChanges;

	private Ui counting(Ui inner){
		return new Ui(){
			public void show(Screen s){ uiChanges++; inner.show(s); }
			public void ask(String t, String b, String i, int m, boolean ml, Consumer<String> a){ uiChanges++; inner.ask(t, b, i, m, ml, a); }
			public void busy(String m){ uiChanges++; inner.busy(m); }
			public void leave(){ uiChanges++; inner.leave(); }
			public void planned(){ uiChanges++; inner.planned(); }
			public void playReview(ReviewClaim c){ uiChanges++; inner.playReview(c); }
		};
	}

	private String cacheKey(String key){ return (api.token() == null ? "" : api.token()) + "|" + key; }

	// Drops cached list screens after a change the player made (the subscriber count / version shown would be stale).
	// Locked: background refreshes write the same map from another thread.
	private void uncache(String... keys){
		synchronized (CACHE){ for (String k : keys) CACHE.remove(cacheKey(k)); }
	}

	// After blocking someone or a moderator action, every list could show posts that are now hidden: drop them all.
	private void uncacheLists(){
		uncache("season", "submissions", "results", "champions", "mods", "mods-subscribed");
	}

	private void cached(String key, Supplier<CommunityApi.Result> call, Consumer<CommunityApi.Result> then){
		final String k = cacheKey(key);
		final String old;
		synchronized (CACHE){ old = CACHE.get(k); }
		if (old == null){
			call(call, r -> { store(k, r); then.accept(r); });
			return;
		}
		then.accept(new CommunityApi.Result(200, EMPTY_BODY.equals(old) ? null : new JsonReader().parse(old), null));
		final int shownAt = uiChanges;
		runner.run(call, r -> {
			if (updateRequired(r)){ ui.show(updateScreen()); return; }
			if (r.status == 401 && me != null){ forget(); ui.show(signedOut("You were signed out. Please sign in again.")); return; }
			if (!r.ok()) return; // keep showing the cached screen
			String fresh = store(k, r);
			if (!fresh.equals(old) && uiChanges == shownAt) then.accept(r);
		});
	}

	// A successful answer with no body (no season running, for one) is kept too: before, it was never cached, so every Back to the
	// Community menu showed "Talking to the Community server..." again (Chris, Nov 25).
	private static final String EMPTY_BODY = "\u0000empty";

	private static String store(String k, CommunityApi.Result r){
		if (!r.ok()) return "";
		String json = r.body == null ? EMPTY_BODY : r.body.toJson(JsonWriter.OutputType.json);
		synchronized (CACHE){ CACHE.put(k, json); }
		return json;
	}

	private void call(Supplier<CommunityApi.Result> call, Consumer<CommunityApi.Result> then){
		ui.busy("Talking to the Community server...");
		runner.run(call, r -> {
			if (updateRequired(r)){ ui.show(updateScreen()); return; }
			if (r.status == 401 && me != null){ forget(); ui.show(signedOut("You were signed out. Please sign in again.")); return; }
			then.accept(r);
		});
	}

	private void done(String text, Runnable after){
		Screen s = new Screen("Community", text);
		s.item("OK", null, after);
		s.back = after;
		ui.show(s);
	}

	private void problem(CommunityApi.Result r, Runnable after){
		Screen s = new Screen("Community", null);
		s.warning(message(r));
		s.item("OK", null, after);
		s.back = after;
		ui.show(s);
	}

	// Plain-language text for every error code the server sends.
	static String message(CommunityApi.Result r){
		if (r.status == 0) return "Can't reach the Community server. Check your connection and try again.";
		String e = r.error == null ? "" : r.error;
		switch (e){
			case "wrong_login": return "That email and password don't match.";
			case "email_taken": return "There's already an account with that email. Try signing in.";
			case "weak_password": return "Passwords need at least 8 characters.";
			case "bad_email": return "That doesn't look like an email address.";
			case "bad_code": return "That code isn't right.";
			case "code_expired": return "That code expired or had too many wrong tries. Ask for a new one.";
			case "email_not_verified": return "Verify your email first.";
			case "email_not_set_up": return "Sign-up emails aren't working yet. Try again later.";
			case "banned": return "This account has been banned from Community.";
			case "age_gate": return "Community is for players 16 and older.";
			case "not_allowed_words": return "That contains words that aren't allowed in Community.";
			case "submissions_closed": return "Submissions are closed for this season.";
			case "voting_closed": return "Voting isn't open right now.";
			case "own_submission": return "You can't vote for your own idea.";
			case "too_many_submissions": return "You've posted a lot of ideas today. Try again tomorrow.";
			case "too_many_comments": return "Slow down a little: too many comments in a short time.";
			case "too_many_championComments": return "Slow down a little: too many comments in a short time.";
			case "too_many_reports": return "You've sent a lot of reports today.";
			case "not_found": return "That isn't there any more.";
			case "no_season": return "No season is running right now.";
			case "not_admin": return "Only moderators can do that.";
			case "cannot_block_self": return "You can't block yourself.";
			case "bad_parent": case "bad_id": return "That comment isn't there any more.";
			case "bad_type": return "Pick a type for your idea and try again.";
			case "images_not_enabled": return "Pictures on ideas aren't switched on yet.";
			case "bad_image_type": return "Only JPG and PNG pictures can be added.";
			case "bad_image": return "That file isn't a working JPG or PNG picture.";
			case "image_too_large": return "That picture is over 1 MB. Try a smaller one.";
			case "not_author": return "Only the person who posted this idea can add pictures to it.";
			case "too_many_images": return "An idea can have up to 8 pictures.";
			case "image_filter_unavailable": return "Pictures can't be checked right now. Try again later.";
			case "image_refused": return "That picture can't be posted here.";
			case "bad_decision": return "Choose approve or remove.";
			case "not_a_qualifier": return "That idea isn't in the tournament.";
			case "bye_no_vote": case "bad_matchup": case "not_in_matchup": return "That vote isn't open right now.";
			case "signed_out": return "You were signed out. Please sign in again.";
			case "server_error": return "The Community server had a problem. Try again in a moment.";
			case "slow_down": return "Too many tries. Wait a few minutes and try again.";
			case "bad_google_token": return "Google sign-in didn't work. Try again or use email.";
			case "search_too_short": return "Type at least 2 letters.";
			case "cannot_ban_self": return "You can't ban yourself.";
			case "season_overlaps": return "A season is already running in that time. Wait for it to finish first.";
			case "no_matchups_open": return "That vote isn't open right now.";
			case "bad_status": return "That isn't one of the four statuses.";
			case "review_access_removed": return "A moderator has taken away your review access.";
			case "own_run": return "You can't review your own run.";
			case "already_reviewed": return "You've already reviewed this run. There are no retries.";
			case "bad_sprite": return "That mod's sprite.png isn't a readable PNG.";
			case "sprite_too_large": return "That mod's sprite.png is larger than 64 KB.";
			case "sprite_wrong_size": return "That mod's sprite.png must be 256x128, the same layout as the built-in hero sprites.";
			case "json_too_large": return "That mod's hero.json is larger than 16 KB.";
			case "bad_json_mod": return "That mod's hero.json can't be read.";
			case "bad_mod_id": return "That mod's id may only use a-z, 0-9, '.', '_' and '-' (up to 64 characters).";
			case "too_many_mods": return "You've uploaded 5 mods or mod updates today. Try again tomorrow.";
			case "mod_id_taken": return "Another player already uses that mod id. Change the id in hero.json.";
			case "mod_removed": return "A moderator removed that mod, so it can't be uploaded again.";
			case "already_claimed": return "You already took this run. It can only be played once, so it can't be started again.";
			case "not_claimed": return "Take this run to review first.";
			case "review_closed": return "This run already has its 5 reviews.";
			case "bad_tone": return "Pick the tone of your comment before sending.";
			case "too_many_reviewRequests": return "You've sent a lot of runs for review today. Try again tomorrow.";
			default:
				if (e.startsWith("too_long_")) return "That's too long.";
				if (e.startsWith("missing_")) return "Something was left empty.";
				return "Something went wrong (" + e + "). Try again.";
		}
	}
}
