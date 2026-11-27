package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//The simulator app never talks to the internet: no Community sign-ins, posts, votes, uploads or reports, no news, no update checks.
//Every connection the game tries is logged (network audit) and sent to a dead address so it fails. A connection to anything other than
//APD's own Community server is also reported as a bug, because the game should never contact anyone else.
final class NetworkGuard extends ProxySelector {

	static final String OWN_SERVER = "apd-community.charltonchris29.workers.dev";

	private static final Map<String, Integer> attempts = Collections.synchronizedMap(new LinkedHashMap<>());
	private final SimRuntime sim;

	private NetworkGuard(SimRuntime sim){ this.sim = sim; }

	static void install(SimRuntime sim){
		ProxySelector.setDefault(new NetworkGuard(sim));
	}

	@Override
	public List<Proxy> select(URI uri){
		String host = uri == null || uri.getHost() == null ? "?" : uri.getHost();
		attempts.merge(host, 1, Integer::sum);
		if (!host.equals(OWN_SERVER) && !host.equals("localhost") && !host.equals("127.0.0.1")){
			final String what = String.valueOf(uri);
			com.watabou.noosa.Game.runOnRenderThread(() -> sim.report(BugReport.Kind.NETWORK, "net:" + host,
					"The game tried to connect to " + host + ", which is not APD's own server.", "Address: " + what));
		}
		//a closed local port: the connection fails at once, nothing leaves the phone
		return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 9)));
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe){}

	static Map<String, Integer> attempts(){
		synchronized (attempts){ return new LinkedHashMap<>(attempts); }
	}
}
