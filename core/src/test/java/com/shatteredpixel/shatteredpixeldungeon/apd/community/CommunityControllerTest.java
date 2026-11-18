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
	private boolean left;

	private final CommunityController.Ui ui = new CommunityController.Ui(){
		public void show(CommunityController.Screen s){ screens.add(s); }
		public void ask(String title, String body, String initial, int max, boolean multi, Consumer<String> answer){
			asked.add(title);
			answer.accept(answers.isEmpty() ? null : answers.poll());
		}
		public void busy(String m){}
		public void leave(){ left = true; }
	};

	private static final class Store implements CommunityController.Storage {
		String token, age;
		public String token(){ return token; }
		public void token(String t){ token = t; }
		public String get(){ return age; }
		public void put(String v){ age = v; }
	}
	private final Store store = new Store();

	@Before public void start() throws Exception {
		CommunityAgeGate.play = null; // no Google Play in tests: the birth month/year question is used
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

	@After public void stop(){ server.stop(0); }

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
	private static boolean has(CommunityController.Screen s, String text){ for (String l : labels(s)) if (l.contains(text)) return true; return s.message != null && s.message.contains(text); }

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
		assertTrue("voting phase, someone else's idea", has(last(), "Upvote"));
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
}
