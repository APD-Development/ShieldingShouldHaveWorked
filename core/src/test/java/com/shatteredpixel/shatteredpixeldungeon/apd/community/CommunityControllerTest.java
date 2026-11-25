package com.shatteredpixel.shatteredpixeldungeon.apd.community;

import static org.junit.Assert.*;

import com.shatteredpixel.shatteredpixeldungeon.apd.age.CommunityAgeGate;
import com.sun.net.httpserver.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** The live Community screens, driven through a stub server: age gate, sign-up, hub, threads, voting, errors. */
public class CommunityControllerTest {
	private HttpServer server;
	private final Map<String, String[]> replies = new HashMap<>(); // "GET /v1/me" -> {status, body}
	private final List<String> requests = new ArrayList<>();

	private final List<CommunityController.Screen> screens = new ArrayList<>();
	private final Deque<String> answers = new ArrayDeque<>();
	private final List<String> asked = new ArrayList<>();
	private boolean left, planned;

	private final CommunityController.Ui ui = new CommunityController.Ui(){
		public void show(CommunityController.Screen s){ screens.add(s); }
		public void ask(String title, String body, String initial, int max, boolean multi, Consumer<String> answer){
			asked.add(title);
			answer.accept(answers.isEmpty() ? null : answers.poll());
		}
		public void busy(String m){}
		public void leave(){ left = true; }
		public void planned(){ planned = true; }
		public void playReview(ReviewClaim c){ played.add(c); }
	};
	private final List<ReviewClaim> played = new ArrayList<>();

	private static final class Store implements CommunityController.Storage {
		String token, age;
		public String token(){ return token; }
		public void token(String t){ token = t; }
		public String get(){ return age; }
		public void put(String v){ age = v; }
	}
	private final Store store = new Store();

	@Before public void start() throws Exception {
		com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModHeroSelection.ENABLED = true;
		CommunityController.CACHE.clear();
		CommunityAgeGate.play = null; // no Google Play in tests: the birth month/year question is used
		CommunityController.google = null;
		CommunityController.emailSignIn = true; //most tests sign up through the (dormant) email path
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", ex -> {
			String key = ex.getRequestMethod() + " " + ex.getRequestURI().getPath();
			requests.add(key + " " + new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			String[] r = replies.getOrDefault(key, new String[]{"404", "{\"error\":\"no_route\"}"});
			byte[] out = r[1].getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(Integer.parseInt(r[0]), out.length);
			try (OutputStream o = ex.getResponseBody()) { o.write(out); }
		});
		server.start();
	}

	@After public void stop(){ server.stop(0); com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModHeroSelection.ENABLED = false; }

	private CommunityController controller(){
		CommunityApi api = new CommunityApi("http://127.0.0.1:" + server.getAddress().getPort(), 3000);
		return new CommunityController(api, ui, (call, then) -> then.accept(call.get()), store, new int[]{11, 2026});
	}

	private void reply(String route, int status, String json){ replies.put(route, new String[]{String.valueOf(status), json}); }
	private CommunityController.Screen last(){ return screens.get(screens.size() - 1); }
	private void tap(String labelStart){
		for (CommunityController.Row r : last().items()) if (r.label.startsWith(labelStart)){ r.action.run(); return; }
		fail("no row '" + labelStart + "' on " + last().title + ": " + labels(last()));
	}
	private static List<String> labels(CommunityController.Screen s){ List<String> l = new ArrayList<>(); for (CommunityController.Row r : s.rows) l.add(r.label); return l; }
	private static boolean has(CommunityController.Screen s, String text){ for (CommunityController.Row r : s.rows) if (r.label.contains(text) || r.detail.contains(text)) return true; return s.message != null && s.message.contains(text); }

	private void signedInWorld(boolean admin){
		reply("GET /v1/me", 200, "{\"id\":\"aa01\",\"displayName\":\"Chris\",\"emailVerified\":true,\"ageOk\":true,\"admin\":" + admin + "}");
		reply("GET /v1/season", 200, "{\"season\":{\"id\":\"s1\",\"title\":\"Season 1\",\"phase\":\"voting\"}}");
	}

	@Test public void underSixteenLocksForGood_andTheDateIsNeverStored(){
		answers.add("5"); answers.add("2012");
		controller().start();
		tap("Enter birth month and year");
		assertEquals("fail", store.age);
		assertTrue(has(last(), "16 and older"));
		assertTrue(requests.isEmpty());
		screens.clear(); answers.add("1"); answers.add("1990");
		controller().start();
		assertTrue("no second try", has(last(), "16 and older"));
		assertFalse(asked.size() > 2);
	}

	@Test public void oldAppVersion_blocksCommunityUntilTheUpdateIsInstalled(){
		store.age = "pass"; store.token = "tok";
		reply("GET /v1/me", 426, "{\"error\":\"update_required\"}");
		controller().start();
		CommunityController.Screen s = last();
		assertTrue(has(s, "Download and install the update"));
		assertEquals(java.util.Arrays.asList("Back"), itemLabels(s));
		assertEquals("still signed in for after the update", "tok", store.token);
		tap("Back");
		assertTrue(left);
	}

	@Test public void signUpVerifyAndLandOnTheHub(){
		store.age = "pass";
		signedInWorld(false);
		reply("POST /v1/auth/register", 201, "{\"accountId\":\"aa01\"}");
		reply("POST /v1/auth/verify", 200, "{\"token\":\"tok123\"}");
		answers.add("me@x.com"); answers.add("longpassword"); answers.add("Chris"); answers.add("123456");
		controller().start();
		tap("Create an account");
		assertEquals("tok123", store.token);
		CommunityController.Screen hub = last();
		assertEquals("Community", hub.title);
		assertTrue(has(hub, "Signed in as Chris"));
		assertTrue(has(hub, "Month 2: voting"));
		assertFalse("not an admin", has(hub, "Moderation"));
		assertTrue(requests.get(0).startsWith("POST /v1/auth/register {\"email\":\"me@x.com\",\"password\":\"longpassword\",\"displayName\":\"Chris\"}"));
	}

	@Test public void listScreensShowTheCachedAnswerAtOnce_thenRedrawOnlyWhenItChanged(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/submissions", 200, "{\"submissions\":[{\"id\":\"x1\",\"title\":\"Old idea\",\"author\":{\"displayName\":\"A\"},\"comments\":0}]}");
		CommunityController c = controller();
		c.start();
		tap("Submissions");
		assertTrue(has(last(), "Old idea"));
		tap("Back");
		// Same answer again: the cached screen is drawn once and not redrawn.
		int before = screens.size();
		tap("Submissions");
		assertEquals(before + 1, screens.size());
		assertTrue(has(last(), "Old idea"));
		tap("Back");
		// The server changed: cached screen first, then the fresh one.
		reply("GET /v1/submissions", 200, "{\"submissions\":[{\"id\":\"x2\",\"title\":\"New idea\",\"author\":{\"displayName\":\"B\"},\"comments\":1}]}");
		before = screens.size();
		tap("Submissions");
		assertEquals(before + 2, screens.size());
		assertTrue(has(screens.get(before), "Old idea"));
		assertTrue(has(last(), "New idea"));
		tap("Back");
		// The server is down: the cached screen stays, no error replaces it.
		reply("GET /v1/submissions", 500, "{\"error\":\"server\"}");
		before = screens.size();
		tap("Submissions");
		assertEquals(before + 1, screens.size());
		assertTrue(has(last(), "New idea"));
	}

	@Test public void signingOutForgetsTheCache(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		controller().start();
		assertFalse(CommunityController.CACHE.isEmpty());
		reply("GET /v1/season", 401, "{\"error\":\"unauthorized\"}");
		tap("Submissions"); // any call; reply 404 then back to hub cached+refresh 401
		tap("Back");
		assertTrue(CommunityController.CACHE.isEmpty());
		assertNull(store.token);
	}

	@Test public void savedSessionGoesStraightToTheHub_andAdminsSeeModeration(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(true);
		controller().start();
		assertTrue(has(last(), "Moderation"));
	}

	@Test public void threadsShowRepliesUnderTheirParent(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/submissions", 200, "{\"submissions\":[{\"id\":\"5501\",\"title\":\"Shield rework\",\"author\":{\"id\":\"bb02\",\"displayName\":\"Bo\"},\"comments\":3}]}");
		reply("GET /v1/submissions/5501", 200, "{\"id\":\"5501\",\"title\":\"Shield rework\",\"body\":\"Shields should have worked\",\"author\":{\"id\":\"bb02\",\"displayName\":\"Bo\"},\"comments\":["
				+ "{\"id\":\"c1\",\"parentId\":null,\"body\":\"Love it\",\"author\":{\"id\":\"aa01\",\"displayName\":\"Chris\"}},"
				+ "{\"id\":\"c2\",\"parentId\":null,\"body\":null,\"author\":null},"
				+ "{\"id\":\"c3\",\"parentId\":\"c1\",\"body\":\"Thanks\",\"author\":{\"id\":\"bb02\",\"displayName\":\"Bo\"}}]}");
		controller().start();
		tap("Submissions");
		assertTrue(has(last(), "by Bo"));
		tap("Shield rework");
		List<String> l = labels(last());
		int a = l.indexOf("Chris: Love it"), b = l.indexOf("  > Bo: Thanks"), c = l.indexOf("[removed]");
		assertTrue(l.toString(), a >= 0 && b == a + 1 && c > b);
		assertTrue("voting phase, someone else's idea", has(last(), "Upvote this idea"));
		tap("  > Bo: Thanks");
		assertTrue(has(last(), "Report this comment"));
	}

	@Test public void votingStandingsAndPlainErrors(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/season/results", 200, "{\"season\":{\"phase\":\"voting\"},\"standings\":[{\"id\":\"5501\",\"title\":\"Idea A\",\"upvotes\":4}],\"myUpvotes\":[\"5501\"]}");
		controller().start();
		tap("Voting and results");
		assertTrue(has(last(), "1. Idea A"));
		assertEquals("4 upvotes · you upvoted", last().items().get(0).detail);
		CommunityApi.Result r = new CommunityApi.Result(409, null, "own_submission");
		assertEquals("You can't vote for your own idea.", CommunityController.message(r));
		assertTrue(CommunityController.message(new CommunityApi.Result(0, null, "offline")).contains("Can't reach"));
	}

	@Test public void anExpiredSessionSignsOutCleanly(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		controller().start();
		reply("GET /v1/submissions", 401, "{\"error\":\"signed_out\"}");
		tap("Submissions");
		assertNull(store.token);
		assertTrue(has(last(), "Sign in with email"));
	}

	@Test public void launchShowsOnlyGoogleSignIn(){
		CommunityController.emailSignIn = false;
		store.age = "pass";
		CommunityController.google = done -> done.accept(null);
		controller().start();
		assertTrue(has(last(), "Sign in with Google"));
		assertFalse(has(last(), "Sign in with email"));
		assertFalse(has(last(), "Create an account"));
		assertFalse(has(last(), "I have a verification code"));
		CommunityController.google = null;
		controller().start();
		assertFalse(has(last(), "Sign in with email"));
	}

	@Test public void googleSignInSendsTheTokenAndCancelGoesBack(){
		store.age = "pass";
		signedInWorld(false);
		reply("POST /v1/auth/google", 200, "{\"token\":\"gtok\"}");
		CommunityController.google = done -> done.accept("google-id-token");
		controller().start();
		tap("Sign in with Google");
		assertEquals("gtok", store.token);
		assertTrue(requests.get(0).startsWith("POST /v1/auth/google {\"idToken\":\"google-id-token\"}"));
		assertTrue(has(last(), "Signed in as Chris"));
		store.token = null; screens.clear(); requests.clear();
		CommunityController.google = done -> done.accept(null);
		controller().start();
		tap("Sign in with Google");
		assertTrue(has(last(), "Sign in with email"));
		assertTrue("cancelling sends nothing", requests.isEmpty());
		CommunityController.google = null;
	}

	@Test public void unblockFromTheAccountScreen(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/blocks", 200, "{\"blocked\":[{\"id\":\"dd04\",\"displayName\":\"Pest\"}]}");
		reply("DELETE /v1/blocks/dd04", 200, "{\"ok\":true}");
		controller().start();
		tap("Account");
		tap("Blocked players");
		tap("Pest");
		assertTrue(has(last(), "Pest is unblocked."));
		assertTrue(requests.stream().anyMatch(r -> r.startsWith("DELETE /v1/blocks/dd04")));
	}

	@Test public void adminStartsASeasonWithDaysTurnedIntoTheServersMilliseconds(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(true);
		reply("GET /v1/admin/reports", 200, "{\"reports\":[]}");
		reply("POST /v1/admin/seasons", 201, "{\"id\":\"s2\"}");
		answers.add("Season 2"); answers.add("10"); answers.add("10"); answers.add("28");
		controller().start();
		tap("Moderation");
		tap("Start a new season");
		assertEquals("Start Season 2?", last().title);
		tap("Start it");
		assertTrue(requests.stream().anyMatch(r -> r.startsWith("POST /v1/admin/seasons {\"title\":\"Season 2\",\"submissionMs\":864000000,\"votingMs\":864000000,\"tournamentMs\":2419200000}")));
		assertTrue(has(last(), "Season started."));
	}

	@Test public void adminActsOnReports_hideBanAndFailuresStayOnModeration(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(true);
		reply("GET /v1/admin/reports", 200, "{\"reports\":["
				+ "{\"target_kind\":\"comment\",\"target_id\":\"c9\",\"author_id\":\"ee05\",\"preview\":\"mean words\",\"reports\":3,\"reasons\":\"rude | spam\"},"
				+ "{\"target_kind\":\"account\",\"target_id\":\"ee06\",\"author_id\":\"ee06\",\"preview\":\"BadName\",\"reports\":1,\"reasons\":\"name\"}]}");
		reply("POST /v1/admin/hide", 200, "{\"ok\":true}");
		reply("POST /v1/admin/ban", 200, "{\"ok\":true}");
		reply("POST /v1/admin/remove", 403, "{\"error\":\"not_admin\"}");
		controller().start();
		tap("Moderation");
		assertTrue(has(last(), "3x comment: mean words"));
		assertTrue(has(last(), "rude | spam"));

		tap("3x comment");
		assertEquals("Moderate comment", last().title);
		tap("Hide");
		assertTrue(requests.stream().anyMatch(r -> r.equals("POST /v1/admin/hide {\"kind\":\"comment\",\"id\":\"c9\"}")));
		assertEquals("back on the reports list", "Moderation", last().title);

		tap("3x comment");
		tap("Ban the author");
		assertTrue(requests.stream().anyMatch(r -> r.equals("POST /v1/admin/ban {\"accountId\":\"ee05\"}")));
		assertEquals("Moderation", last().title);

		tap("3x comment");
		tap("Remove");
		assertTrue("server refusal shown in plain words", has(last(), "Only moderators can do that."));
		tap("OK");
		assertEquals("Moderation", last().title);

		tap("1x account");
		assertFalse("an account can't be hidden", has(last(), "Hide"));
		assertFalse(has(last(), "Remove"));
		assertTrue(has(last(), "Ban the author"));
	}

	@Test public void adminFindsAndUnbansAPlayer(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(true);
		reply("GET /v1/admin/reports", 200, "{\"reports\":[]}");
		reply("GET /v1/admin/accounts", 200, "{\"accounts\":[{\"id\":\"ee05\",\"displayName\":\"Troll\",\"email\":\"t@x.com\",\"banned\":true}]}");
		reply("POST /v1/admin/unban", 200, "{\"ok\":true}");
		answers.add("tro ll");
		controller().start();
		tap("Moderation");
		tap("Find a player");
		assertTrue(has(last(), "Troll  (banned)"));
		tap("Troll");
		assertTrue(has(last(), "Troll is unbanned."));
		assertTrue(requests.stream().anyMatch(r -> r.startsWith("POST /v1/admin/unban {\"accountId\":\"ee05\"}")));
	}

	@Test public void theUpvoteButtonSaysWhetherIUpvotedAndTheCount(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/submissions", 200, "{\"submissions\":[{\"id\":\"5501\",\"title\":\"Idea A\",\"author\":{\"id\":\"bb02\",\"displayName\":\"Bo\"},\"comments\":0}]}");
		reply("GET /v1/submissions/5501", 200, "{\"id\":\"5501\",\"title\":\"Idea A\",\"body\":\"b\",\"author\":{\"id\":\"bb02\",\"displayName\":\"Bo\"},\"comments\":[]}");
		reply("GET /v1/season/results", 200, "{\"season\":{\"phase\":\"voting\"},\"standings\":[{\"id\":\"5501\",\"title\":\"Idea A\",\"upvotes\":3}],\"myUpvotes\":[\"5501\"]}");
		reply("DELETE /v1/submissions/5501/upvote", 200, "{\"upvoted\":false}");
		controller().start();
		tap("Submissions");
		tap("Idea A");
		assertTrue(has(last(), "Take back my upvote"));
		assertTrue(has(last(), "3 upvotes"));
		tap("Take back my upvote");
		assertTrue(requests.stream().anyMatch(r -> r.startsWith("DELETE /v1/submissions/5501/upvote")));
		assertFalse("an upvote drops the cached submissions/results lists (counts changed)",
				CommunityController.CACHE.keySet().stream().anyMatch(k -> k.endsWith("|submissions") || k.endsWith("|results")));
	}

	@Test public void commentingDropsTheCachedSubmissionsList(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/submissions", 200, "{\"submissions\":[{\"id\":\"5501\",\"title\":\"Idea A\",\"author\":{\"id\":\"bb02\",\"displayName\":\"Bo\"},\"comments\":0}]}");
		reply("GET /v1/submissions/5501", 200, "{\"id\":\"5501\",\"title\":\"Idea A\",\"body\":\"b\",\"author\":{\"id\":\"bb02\",\"displayName\":\"Bo\"},\"comments\":[]}");
		reply("POST /v1/submissions/5501/comments", 201, "{\"id\":\"cc09\"}");
		controller().start();
		tap("Submissions");
		assertTrue(CommunityController.CACHE.keySet().stream().anyMatch(k -> k.endsWith("|submissions")));
		tap("Idea A");
		answers.add("Nice");
		tap("Comment");
		assertTrue(requests.stream().anyMatch(r -> r.startsWith("POST /v1/submissions/5501/comments")));
		assertFalse("a comment drops the cached submissions list (its comment count changed)",
				CommunityController.CACHE.keySet().stream().anyMatch(k -> k.endsWith("|submissions")));
	}

	@Test public void theHubLinksToWhatsComingNext(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		controller().start();
		tap("What's coming next");
		assertTrue(planned);
	}

	@Test public void noConnectionKeepsTheSignInAndOffersRetry(){
		store.age = "pass"; store.token = "tok";
		CommunityApi dead = new CommunityApi("http://127.0.0.1:1", 500);
		new CommunityController(dead, ui, (call, then) -> then.accept(call.get()), store, new int[]{11, 2026}).start();
		assertEquals("tok", store.token);
		assertTrue(has(last(), "You're still signed in."));
		assertTrue(has(last(), "Try again"));
	}

	@Test public void selectedContent_historyPageCommentsAndAdminControls(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(true);
		reply("GET /v1/champions", 200, "{\"champions\":[{\"seasonId\":\"5e02\",\"seasonTitle\":\"Season 2\",\"title\":\"Pet dragon\",\"status\":\"testing\"},"
				+ "{\"seasonId\":\"5e01\",\"seasonTitle\":\"Season 1\",\"title\":\"Fishing\",\"status\":\"released\"}]}");
		reply("GET /v1/champions/5e01", 200, "{\"seasonId\":\"5e01\",\"seasonTitle\":\"Season 1\",\"title\":\"Fishing\",\"status\":\"released\",\"body\":\"Fish in water\","
				+ "\"author\":null,\"notes\":[{\"id\":\"ab01\",\"body\":\"Shipped in 930\"}],"
				+ "\"comments\":[{\"id\":\"cc01\",\"parentId\":null,\"body\":\"Love it\",\"author\":{\"id\":\"bb01\",\"displayName\":\"Bo\"}},"
				+ "{\"id\":\"cc02\",\"parentId\":\"cc01\",\"body\":\"Same\",\"author\":{\"id\":\"aa01\",\"displayName\":\"Chris\"}}]}");
		reply("POST /v1/admin/champions/5e01/status", 200, "{\"ok\":true}");
		reply("POST /v1/champions/5e01/comments", 201, "{\"id\":\"cc03\"}");
		reply("POST /v1/reports", 201, "{\"ok\":true}");
		controller().start();
		tap("Selected content");
		CommunityController.Screen list = last();
		assertEquals(java.util.Arrays.asList("Pet dragon", "Fishing", "Back"), itemLabels(list));
		assertTrue(has(list, "Season 2 - Testing"));
		tap("Fishing");
		CommunityController.Screen page = last();
		assertTrue(has(page, "Champion of Season 1, by [deleted]"));
		assertTrue(has(page, "Status: Released"));
		assertTrue(has(page, "Shipped in 930"));
		assertTrue(has(page, "  > Chris: Same"));
		tap("Set status");
		tap("Testing");
		assertTrue(requests.stream().anyMatch(q -> q.equals("POST /v1/admin/champions/5e01/status {\"status\":\"testing\"}")));
		answers.add("Nice");
		tap("Comment");
		assertTrue(requests.stream().anyMatch(q -> q.equals("POST /v1/champions/5e01/comments {\"body\":\"Nice\"}")));
		tap("Bo: Love it");
		answers.add("rude");
		tap("Report this comment");
		assertTrue(requests.stream().anyMatch(q -> q.startsWith("POST /v1/reports {\"kind\":\"championComment\",\"id\":\"cc01\"")));
	}

	@Test public void selectedContent_playersSeeNoAdminControls(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/champions", 200, "{\"champions\":[]}");
		controller().start();
		tap("Selected content");
		assertTrue(has(last(), "No season has finished yet"));
		reply("GET /v1/champions/5e01", 200, "{\"seasonId\":\"5e01\",\"seasonTitle\":\"S\",\"title\":\"T\",\"status\":\"planned\",\"body\":\"\","
				+ "\"author\":{\"id\":\"bb01\",\"displayName\":\"Bo\"},\"notes\":[{\"id\":\"ab01\",\"body\":\"Note\"}],\"comments\":[]}");
		reply("GET /v1/champions", 200, "{\"champions\":[{\"seasonId\":\"5e01\",\"seasonTitle\":\"S\",\"title\":\"T\",\"status\":\"planned\"}]}");
		tap("Back");
		tap("Selected content");
		tap("T");
		assertTrue(has(last(), "Status: Planned"));
		assertFalse(has(last(), "Set status"));
		assertFalse(has(last(), "Add a development note"));
		assertFalse("notes are plain text for players", itemLabels(last()).contains("Note"));
	}

	@Test public void runReviews_progressHiddenUntilClosed_verdictsAfter_noSeedUntilTaken(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/reviews/mine", 200, "{\"runs\":[{\"id\":\"r1\",\"setup\":{\"heroClass\":\"WARRIOR\",\"challenges\":3,\"difficulty\":\"normal\"},\"status\":\"open\",\"done\":2,\"needed\":5},"
				+ "{\"id\":\"r2\",\"setup\":{\"heroClass\":\"MAGE\"},\"status\":\"closed\",\"done\":5,\"needed\":5,\"reviews\":["
				+ "{\"id\":\"v1\",\"survived\":true,\"reachedDepth\":6,\"salvageable\":true,\"advice\":\"Drink the potion earlier\",\"reviewer\":{\"id\":\"b1\",\"displayName\":\"Bo\"}},"
				+ "{\"survived\":false,\"reachedDepth\":3,\"salvageable\":false,\"advice\":null,\"reviewer\":null}]}]}");
		reply("GET /v1/reviews/open", 200, "{\"runs\":[{\"id\":\"r9\",\"player\":\"Pat\",\"setup\":{\"heroClass\":\"ROGUE\",\"challenges\":1},\"roasts\":true,\"done\":1,\"needed\":5}]}");
		controller().start();
		tap("Run reviews");
		CommunityController.Screen s = last();
		assertTrue(has(s, "Warrior · normal · 2 challenges: 2/5 reviewers so far"));
		assertFalse("open runs show no verdicts", has(s, "salvageable"));
		assertTrue(has(s, "Reviewed: 5/5"));
		assertTrue(has(s, "1/5 reviewed · roasts allowed"));
		tap("Pat: Rogue");
		assertTrue(itemLabels(last()).contains("Take this run"));
		assertFalse("opening a run never claims or fetches a seed", requests.stream().anyMatch(q -> q.contains("/claim")));
		tap("Back");
		tap("Mage");
		CommunityController.Screen v = last();
		assertTrue(has(v, "1 of 2 said it was salvageable"));
		assertTrue(has(v, "Bo: salvageable (got past where you died)"));
		assertTrue(has(v, "Drink the potion earlier"));
		assertTrue(has(v, "[removed]: not salvageable (died earlier, on floor 3)"));
		// the reviewed player can report a review (bullying goes to a moderator); removed reviews have no button
		assertTrue(itemLabels(v).contains("Report Bo's review"));
		assertEquals(1, itemLabels(v).stream().filter(l -> l.startsWith("Report")).count());
		reply("POST /v1/reports", 201, "{\"ok\":true}");
		answers.add("Bullying");
		tap("Report Bo's review");
		String sent = requests.stream().filter(q -> q.startsWith("POST /v1/reports")).findFirst().orElse("");
		assertTrue(sent, sent.contains("\"kind\":\"review\"") && sent.contains("\"id\":\"v1\"") && sent.contains("Bullying"));
	}

	@Test public void runReviews_takeThisRun_confirmsThenClaimsAndStartsTheGame_seedNeverShown(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/reviews/mine", 200, "{\"runs\":[]}");
		reply("GET /v1/reviews/open", 200, "{\"runs\":[{\"id\":\"ab12\",\"player\":\"Pat\",\"setup\":{\"heroClass\":\"ROGUE\"},\"reviewType\":\"advice\",\"done\":1,\"needed\":5}]}");
		reply("POST /v1/reviews/ab12/claim", 200, "{\"id\":\"ab12\",\"seed\":\"XYZ-ABC-DEF\",\"deathDepth\":7,\"reviewType\":\"advice\","
				+ "\"setup\":{\"challenges\":\"5\",\"customRules\":\"0\",\"difficulty\":\"3\",\"exotic\":\"1\",\"heroClass\":\"ROGUE\"}}");
		controller().start();
		tap("Run reviews");
		tap("Pat: Rogue");
		tap("Take this run");
		assertTrue(has(last(), "one attempt"));
		assertFalse("asking again never claims", requests.stream().anyMatch(q -> q.contains("/claim")));
		tap("Not now");
		assertTrue(played.isEmpty());
		tap("Take this run");
		tap("Start the run");
		assertTrue(requests.stream().anyMatch(q -> q.startsWith("POST /v1/reviews/ab12/claim")));
		assertEquals(1, played.size());
		ReviewClaim c = played.get(0);
		assertEquals("ab12", c.requestId);
		assertEquals(7, c.deathDepth);
		assertEquals(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.ROGUE, c.setup.heroClass);
		for (CommunityController.Screen sc : screens) assertFalse("the seed is never shown", has(sc, "XYZ-ABC-DEF"));
	}

	@Test public void runReviews_takeThisRun_refusedOrBadClaim_neverStartsTheGame(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("GET /v1/reviews/mine", 200, "{\"runs\":[]}");
		reply("GET /v1/reviews/open", 200, "{\"runs\":[{\"id\":\"ab12\",\"player\":\"Pat\",\"setup\":{\"heroClass\":\"ROGUE\"},\"done\":1,\"needed\":5}]}");
		reply("POST /v1/reviews/ab12/claim", 409, "{\"error\":\"already_reviewed\"}");
		controller().start();
		tap("Run reviews");
		tap("Pat: Rogue");
		tap("Take this run");
		tap("Start the run");
		assertTrue(has(last(), "no retries"));
		reply("POST /v1/reviews/ab12/claim", 200, "{\"id\":\"ab12\",\"seed\":\"XYZ\",\"deathDepth\":7,\"setup\":{\"heroClass\":\"NOBODY\"}}");
		tap("Back");
		tap("Pat: Rogue");
		tap("Take this run");
		tap("Start the run");
		assertTrue(has(last(), "can't be played on this version"));
		assertTrue(played.isEmpty());
	}

	@Test public void runReviews_sendFromRankings_showsOnceAfterSignIn_roastsChoiceSent(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		reply("POST /v1/reviews", 200, "{\"id\":\"r5\"}");
		java.util.Map<String, String> setup = new java.util.HashMap<>();
		setup.put("heroClass", "HUNTRESS"); setup.put("challenges", "1");
		java.util.Map<String, String> ending = new java.util.HashMap<>();
		ending.put("cause", "Gnoll");
		CommunityController.pendingReview = new CommunityController.ReviewRun("ABC-DEF-GHI", 4, setup, ending);
		try {
			controller().start();
			CommunityController.Screen s = last();
			assertEquals("Send this run for review", s.title);
			assertTrue(has(s, "Huntress · 1 challenge · died on floor 4"));
			assertFalse("the seed is never shown", has(s, "ABC-DEF-GHI"));
			assertNull("shown once", CommunityController.pendingReview);
			tap("Straight advice only");
			String sent = requests.stream().filter(q -> q.startsWith("POST /v1/reviews")).findFirst().orElse("");
			assertTrue(sent, sent.contains("\"seed\":\"ABC-DEF-GHI\"") && sent.contains("\"deathDepth\":4") && sent.contains("\"reviewType\":\"advice\""));
			assertTrue(has(last(), "Sent."));
			tap("Back");
			assertEquals("Community", last().title);
		} finally { CommunityController.pendingReview = null; }
	}

	@Test public void runReviews_dontSend_sendsNothing(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		CommunityController.pendingReview = new CommunityController.ReviewRun("S", 2, null, null);
		try {
			controller().start();
			tap("Don't send");
			assertEquals("Community", last().title);
			assertFalse(requests.stream().anyMatch(q -> q.startsWith("POST /v1/reviews")));
		} finally { CommunityController.pendingReview = null; }
	}

	@Test public void runReviews_finishedReview_asksVerdict_keepsItUntilServerAnswers(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		ReviewClaim c = ReviewClaim.parse(new com.badlogic.gdx.utils.JsonReader().parse(
				"{\"id\":\"ab12\",\"seed\":\"XYZ-ABC-DEF\",\"deathDepth\":7,\"setup\":{\"challenges\":\"0\",\"customRules\":\"0\",\"difficulty\":\"3\",\"exotic\":\"0\",\"heroClass\":\"ROGUE\"}}"));
		assertNotNull(c);
		c.reached(4);
		CommunityController.finishedReview = c;
		try {
			controller().start();
			CommunityController.Screen s = last();
			assertEquals("Your review", s.title);
			assertTrue(has(s, "You died on floor 4") && has(s, "still counts"));
			assertFalse("the seed is never shown", has(s, "XYZ-ABC-DEF"));
			// empty advice is refused before any request
			answers.add("   ");
			tap("Not salvageable");
			tap("Straight advice");
			assertTrue(has(last(), "Write some advice"));
			assertFalse(requests.stream().anyMatch(q -> q.contains("/result")));
			// server down: the verdict is kept, nothing lost
			reply("POST /v1/reviews/ab12/result", 503, "{\"error\":\"unavailable\"}");
			answers.add("Use the scroll earlier");
			tap("Not salvageable");
			tap("Straight advice");
			assertEquals("Your review", last().title);
			assertSame(c, CommunityController.finishedReview);
			// accepted: exact body sent, then cleared
			reply("POST /v1/reviews/ab12/result", 200, "{\"survived\":false,\"held\":false,\"ending\":{\"cause\":\"GreatCrab\",\"gear\":\"sword +2, leather armor\",\"heroLevel\":\"6\"},\"done\":1,\"needed\":5}");
			requests.clear();
			answers.add("Use the scroll earlier");
			tap("Salvageable");
			assertTrue("reviewer sees what was asked", has(last(), "This player asked for: straight advice only"));
			tap("Straight advice");
			String sent = requests.stream().filter(q -> q.startsWith("POST /v1/reviews/ab12/result")).findFirst().orElse("");
			assertTrue(sent, sent.contains("\"reachedDepth\":4") && sent.contains("\"salvageable\":true") && sent.contains("\"tone\":\"advice\"") && sent.contains("\"advice\":\"Use the scroll earlier\""));
			assertTrue(has(last(), "Sent."));
			// only now: how the original run ended
			assertTrue(has(last(), "Killed by: Great Crab") && has(last(), "sword +2, leather armor") && has(last(), "Hero level: 6"));
			assertNull(CommunityController.finishedReview);
		} finally { CommunityController.finishedReview = null; }
	}

	@Test public void runReviews_toneThatDoesntMatchIsHeld_playerSeesWhyAndVoteCounts(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		ReviewClaim c = ReviewClaim.parse(new com.badlogic.gdx.utils.JsonReader().parse(
				"{\"id\":\"ef56\",\"seed\":\"S\",\"deathDepth\":3,\"reviewType\":\"encouraging\",\"setup\":{\"challenges\":\"0\",\"customRules\":\"0\",\"difficulty\":\"3\",\"exotic\":\"0\",\"heroClass\":\"MAGE\"}}"));
		assertEquals("encouraging", c.reviewType);
		c.reached(3);
		CommunityController.finishedReview = c;
		try {
			reply("POST /v1/reviews/ef56/result", 200, "{\"survived\":true,\"held\":true,\"done\":1,\"needed\":5}");
			controller().start();
			tap("Not salvageable");
			assertTrue(has(last(), "This player asked for: encouraging"));
			answers.add("Skill issue");
			tap("Roast");
			String sent = requests.stream().filter(q -> q.startsWith("POST /v1/reviews/ef56/result")).findFirst().orElse("");
			assertTrue(sent, sent.contains("\"tone\":\"roasts\""));
			assertTrue(has(last(), "a moderator will check it") && has(last(), "vote still counts"));
			assertNull(CommunityController.finishedReview);
		} finally { CommunityController.finishedReview = null; }
	}

	@Test public void runReviews_finishedReview_alreadyReviewedClearsIt(){
		store.age = "pass"; store.token = "tok";
		signedInWorld(false);
		ReviewClaim c = ReviewClaim.parse(new com.badlogic.gdx.utils.JsonReader().parse(
				"{\"id\":\"cd34\",\"seed\":\"S\",\"deathDepth\":2,\"setup\":{\"challenges\":\"0\",\"customRules\":\"0\",\"difficulty\":\"3\",\"exotic\":\"0\",\"heroClass\":\"MAGE\"}}"));
		c.reached(3);
		CommunityController.finishedReview = c;
		try {
			reply("POST /v1/reviews/cd34/result", 409, "{\"error\":\"already_reviewed\"}");
			controller().start();
			assertTrue(has(last(), "You reached floor 2"));
			answers.add("Fine");
			tap("Salvageable");
			tap("Straight advice");
			assertTrue(has(last(), "already reviewed"));
			assertNull(CommunityController.finishedReview);
		} finally { CommunityController.finishedReview = null; }
	}

	private static List<String> itemLabels(CommunityController.Screen s){ List<String> l = new ArrayList<>(); for (CommunityController.Row r : s.items()) l.add(r.label); return l; }

	// Every error code the server can send has plain words in the app (except a few that only a broken client causes).
	@Test public void everyServerErrorHasPlainWords() throws Exception {
		java.io.File app = new java.io.File(System.getProperty("apd.game.src"), "../../../../../../../server/community/src/app.js");
		String src = new String(java.nio.file.Files.readAllBytes(app.toPath()), java.nio.charset.StandardCharsets.UTF_8);
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("fail\\(\\d+, '([a-z_]+)'\\)").matcher(src);
		java.util.Set<String> internal = new java.util.HashSet<>(java.util.Arrays.asList("bad_json", "bad_kind", "only_pass_is_recorded"));
		List<String> unmapped = new ArrayList<>();
		int seen = 0;
		while (m.find()){
			seen++;
			String code = m.group(1);
			if (internal.contains(code)) continue;
			String text = CommunityController.message(new CommunityApi.Result(400, null, code));
			if (text.startsWith("Something went wrong")) unmapped.add(code);
		}
		assertTrue("found the server's errors", seen > 30);
		assertTrue(unmapped.toString(), unmapped.isEmpty());
	}

	@Test public void workshopSpritePreview_refusesBadOrOversizedData(){
		assertNull(CommunityController.decodeSprite(null));
		assertNull(CommunityController.decodeSprite(""));
		assertNull(CommunityController.decodeSprite("%%not base64%%"));
		byte[] big = new byte[com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibrary.MAX_SPRITE_BYTES + 1];
		assertNull(CommunityController.decodeSprite(java.util.Base64.getEncoder().encodeToString(big)));
		assertArrayEquals(new byte[]{1, 2, 3}, CommunityController.decodeSprite(java.util.Base64.getEncoder().encodeToString(new byte[]{1, 2, 3})));
	}

	@Test public void workshop_browseInstallSubscribeReportAndUpload() throws Exception {
		java.io.File mods = java.nio.file.Files.createTempDirectory("apdmods").toFile();
		CommunityController.modsDir = mods;
		try {
			store.age = "pass"; store.token = "tok";
			signedInWorld(false);
			String json = com.shatteredpixel.shatteredpixeldungeon.apd.mods.HeroModTest.hero(null);
			String png = java.util.Base64.getEncoder().encodeToString(com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModLibraryTest.pngHeader(256, 128));
			String quoted = new com.badlogic.gdx.utils.Json().toJson(json, String.class);
			reply("GET /v1/mods", 200, "{\"mods\":[{\"id\":\"a1\",\"modId\":\"chris.battle-mage\",\"name\":\"Battle Mage\",\"version\":2,\"subscribers\":3,\"author\":{\"id\":\"bb02\",\"displayName\":\"Ann\"}}]}");
			reply("GET /v1/mods/subscribed", 200, "{\"mods\":[]}");
			reply("GET /v1/mods/a1", 200, "{\"id\":\"a1\",\"modId\":\"chris.battle-mage\",\"name\":\"Battle Mage\",\"version\":2,\"author\":{\"id\":\"bb02\",\"displayName\":\"Ann\"},\"json\":" + quoted + ",\"sprite\":\"" + png + "\"}");
			reply("POST /v1/mods/a1/subscribe", 200, "{}");
			reply("POST /v1/reports", 201, "{}");
			controller().start();
			tap("Hero mods workshop");
			assertTrue(has(last(), "by Ann"));
			assertFalse(has(last(), "· installed"));
			tap("Battle Mage");
			assertFalse("not mine: no delete", has(last(), "Delete my mod"));
			assertArrayEquals("mod page carries the download's sprite for the preview", java.util.Base64.getDecoder().decode(png), last().previewPng);
			assertEquals("workshop:chris.battle-mage:2:" + last().previewPng.length, last().previewKey);
			tap("Download and install");
			assertTrue(has(last(), "Installed."));
			assertEquals(json, new String(java.nio.file.Files.readAllBytes(new java.io.File(mods, "chris.battle-mage/hero.json").toPath()), StandardCharsets.UTF_8));
			tap("OK");
			assertTrue("now offered as reinstall", has(last(), "Reinstall"));
			tap("Subscribed");
			assertTrue(requests.stream().anyMatch(q -> q.startsWith("POST /v1/mods/a1/subscribe")));
			assertFalse("subscribing drops the cached lists (subscriber count changed)",
					CommunityController.CACHE.keySet().stream().anyMatch(k -> k.endsWith("|mods") || k.endsWith("|mods-subscribed")));
			answers.add("stolen art");
			tap("Report");
			assertTrue(requests.stream().anyMatch(q -> q.startsWith("POST /v1/reports") && q.contains("\"kind\":\"mod\"") && q.contains("\"id\":\"a1\"")));

			// A damaged download is refused and the installed copy stays.
			reply("GET /v1/mods/a1", 200, "{\"id\":\"a1\",\"modId\":\"chris.battle-mage\",\"name\":\"Battle Mage\",\"author\":{\"id\":\"bb02\"},\"json\":\"not json\",\"sprite\":\"" + png + "\"}");
			controller().start();
			tap("Hero mods workshop"); tap("Battle Mage"); tap("Reinstall");
			assertTrue(has(last(), "can't be installed"));
			assertEquals(json, new String(java.nio.file.Files.readAllBytes(new java.io.File(mods, "chris.battle-mage/hero.json").toPath()), StandardCharsets.UTF_8));

			// Upload sends the exact installed hero.json and sprite; server refusals in plain words.
			reply("POST /v1/mods", 409, "{\"error\":\"mod_id_taken\"}");
			controller().start();
			tap("Hero mods workshop"); tap("Upload one of my installed mods"); tap("Battle Mage");
			assertTrue(has(last(), "Another player already uses that mod id"));
			assertTrue(requests.stream().anyMatch(q -> q.startsWith("POST /v1/mods {") && q.contains(png)));
			reply("POST /v1/mods", 200, "{\"id\":\"a1\",\"updated\":true}");
			tap("OK"); tap("Battle Mage");
			assertTrue(has(last(), "Updated"));

			// My own mod: delete instead of report.
			reply("GET /v1/mods/a1", 200, "{\"id\":\"a1\",\"modId\":\"chris.battle-mage\",\"name\":\"Battle Mage\",\"author\":{\"id\":\"aa01\",\"displayName\":\"Chris\"},\"json\":" + quoted + ",\"sprite\":\"" + png + "\"}");
			reply("DELETE /v1/mods/a1", 200, "{}");
			controller().start();
			tap("Hero mods workshop"); tap("Battle Mage");
			assertFalse(itemLabels(last()).contains("Report"));
			tap("Delete my mod");
			assertTrue(has(last(), "Deleted from the workshop."));
		} finally { CommunityController.modsDir = null; }
	}

	@Test public void seasonSummary_saysOneDayNotOneDays(){
		assertEquals("Submissions 1 day, voting 30 days, tournament 28 days. Starts now.", CommunityController.seasonSummary(1, 30, 28));
		assertEquals("Submissions 30 days, voting 1 day, tournament 4 days. Starts now.", CommunityController.seasonSummary(30, 1, 4));
	}
}
