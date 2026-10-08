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

//The ONE door between the game and any server. The game never calls a network API directly: it hands envelopes to the Outbox, and the
//Outbox sends them through a Gateway when (and only when) there is a connection. A different server, provider or transport is a different
//Gateway; nothing in gameplay changes. Every call is idempotent by key, so a retry after a lost reply cannot double-submit.
public interface Gateway {

	enum Status {
		//the server accepted (or already had) it
		OK,
		//no connection or server unreachable: keep it queued, try later, nothing is wrong with the item
		OFFLINE,
		//temporary server trouble: keep it queued and back off
		RETRY_LATER,
		//the server refused it for good (invalid, not allowed, version no longer supported): drop it and tell the player why
		REJECTED
	}

	final class Response {
		public final Status status;
		public final String detail;

		public Response( Status status, String detail ){
			this.status = status;
			this.detail = detail == null ? "" : detail;
		}
	}

	Response send( String idempotencyKey, Envelope envelope );
}
