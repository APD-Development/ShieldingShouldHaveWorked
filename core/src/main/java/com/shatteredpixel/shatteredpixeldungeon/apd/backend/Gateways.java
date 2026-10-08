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

package com.shatteredpixel.shatteredpixeldungeon.apd.backend;

// Picks the Gateway the app uses. Sending is off unless the player has switched it on AND a secure server address is configured; otherwise
// everything stays queued on the device (OfflineGateway).
public final class Gateways {

	private Gateways(){}

	//https only (plain http is accepted for the local machine so a developer can test); no query or fragment; no trailing slash. null if unusable.
	public static String cleanBaseUrl( String url ){
		if (url == null) return null;
		String u = url.trim();
		while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
		if (u.isEmpty() || u.indexOf('?') >= 0 || u.indexOf('#') >= 0 || u.indexOf(' ') >= 0) return null;
		boolean https = u.startsWith("https://") && u.length() > 8;
		boolean localHttp = u.startsWith("http://localhost") || u.startsWith("http://127.0.0.1");
		return https || localHttp ? u : null;
	}

	public static Gateway choose( boolean sendingEnabled, String baseUrl, HttpGateway.TokenSource tokens ){
		if (!sendingEnabled || cleanBaseUrl(baseUrl) == null) return new OfflineGateway();
		return new HttpGateway(baseUrl, tokens, 10_000);
	}
}
