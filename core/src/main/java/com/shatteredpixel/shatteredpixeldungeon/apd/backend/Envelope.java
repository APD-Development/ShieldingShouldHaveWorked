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

//The wrapper of everything that crosses the client/server boundary: which schema it is, which version of it, and the payload. Versioning
//rule (the same one the run logs use): a MINOR version only adds fields at the end, so a reader that knows the major version reads what it
//knows and ignores the rest; a MAJOR version is a different contract and is refused, never guessed at.
public final class Envelope {

	public enum Compat { SAME, NEWER_MINOR_READABLE, OLDER_MINOR_READABLE, UNSUPPORTED_MAJOR }

	public final String schema;
	public final int major, minor;
	public final String payload;

	public Envelope( String schema, int major, int minor, String payload ){
		if (schema == null || schema.isEmpty() || major < 1 || minor < 0) throw new IllegalArgumentException("bad envelope header");
		this.schema = schema;
		this.major = major;
		this.minor = minor;
		this.payload = payload == null ? "" : payload;
	}

	//can a reader that supports (major, minor) of this schema read the envelope?
	public Compat compatibleWith( int supportedMajor, int supportedMinor ){
		if (major != supportedMajor) return Compat.UNSUPPORTED_MAJOR;
		if (minor == supportedMinor) return Compat.SAME;
		return minor > supportedMinor ? Compat.NEWER_MINOR_READABLE : Compat.OLDER_MINOR_READABLE;
	}
}
