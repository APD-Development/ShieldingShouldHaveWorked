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

import java.util.ArrayList;
import java.util.List;

//What the game wants the server to know, waiting to be sent. The game ADDS to it and moves on - it never waits for the network, and a
//server outage only means items wait longer. Nothing that is local-only can be queued, and an item that the server permanently refuses is
//removed (and reported), not retried forever. This class is the in-memory logic; the stored copy of the queue (so it survives an app restart)
//is a later, small, replaceable layer on top of the same Item data.
public final class Outbox {

	public static final class Item {
		public final Id id;
		public final Envelope envelope;
		public final long createdAtMillis;
		public int attempts;
		public long nextAttemptAtMillis;
		public String lastDetail = "";

		Item( Id id, Envelope envelope, long createdAtMillis ){
			this.id = id;
			this.envelope = envelope;
			this.createdAtMillis = createdAtMillis;
			this.nextAttemptAtMillis = createdAtMillis;
		}
	}

	public static final class FlushResult {
		public int sent, kept, rejected;
		public final List<String> rejectedDetails = new ArrayList<>();
	}

	private final List<Item> items = new ArrayList<>();

	//false (and nothing queued) if the schema must stay on the device
	public Item enqueue( Id id, Envelope e, long nowMillis ){
		if (id.kind != Id.Kind.OUTBOX) throw new IllegalArgumentException("not an outbox id");
		if (!DataPolicy.mayLeaveDevice(e.schema)) throw new IllegalArgumentException("this data must stay on the device: " + e.schema);
		for (Item i : items) if (i.id.equals(id)) return i;     //queuing the same item twice is a no-op
		Item it = new Item(id, e, nowMillis);
		items.add(it);
		return it;
	}

	//puts back an item read from storage (keeps its attempt count and schedule); same rules as enqueue
	Item restore( Id id, Envelope e, long createdAtMillis, int attempts, long nextAttemptAtMillis, String lastDetail ){
		Item it = enqueue(id, e, createdAtMillis);
		it.attempts = attempts;
		it.nextAttemptAtMillis = nextAttemptAtMillis;
		it.lastDetail = lastDetail;
		return it;
	}

	public int size(){
		return items.size();
	}

	public List<Item> items(){
		return items;
	}

	//Sends what is due. Never throws: a gateway that fails in any way leaves the items queued. Items are sent in the order they were added.
	public FlushResult flush( Gateway gateway, long nowMillis ){
		FlushResult r = new FlushResult();
		List<Item> snapshot = new ArrayList<>(items);
		boolean offline = false;
		for (Item it : snapshot){
			if (offline || it.nextAttemptAtMillis > nowMillis){
				r.kept++;
				continue;
			}
			Gateway.Response resp;
			try {
				resp = gateway.send(it.id.value, it.envelope);
			} catch (RuntimeException ex){
				resp = new Gateway.Response(Gateway.Status.RETRY_LATER, "gateway failed");
			}
			it.attempts++;
			it.lastDetail = resp.detail;
			switch (resp.status){
				case OK:
					items.remove(it);
					r.sent++;
					break;
				case REJECTED:
					items.remove(it);
					r.rejected++;
					r.rejectedDetails.add(resp.detail);
					break;
				case OFFLINE:
					offline = true;      //no point trying the rest right now
					it.nextAttemptAtMillis = nowMillis + Backoff.delayMillis(it.attempts);
					r.kept++;
					break;
				default:
					it.nextAttemptAtMillis = nowMillis + Backoff.delayMillis(it.attempts);
					r.kept++;
			}
		}
		return r;
	}
}
