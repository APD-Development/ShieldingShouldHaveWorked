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


package com.shatteredpixel.shatteredpixeldungeon.apd.stats;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

//Reads a run log. Reading never throws on bad data: damage is reported through ReadResult.status, and everything
//before the damage is returned.
//
//Ordinary file reads stream one bounded frame at a time. Explicit resynchronization retains the byte-array
//parser because recovery must scan ahead for the next checksum-valid frame.
public final class RunLogReader {

	private RunLogReader(){}

	public static ReadResult read( File file ) throws IOException {
		return read(file, new ReadOptions());
	}

	//IOException only for problems opening/reading the file itself
	public static ReadResult read( File file, ReadOptions options ) throws IOException {
		//Resynchronization requires random access while scanning for the next valid marker.
		if (options.resync) return parse(readFully(file), options);
		return readStreaming(file, options);
	}

	private static ReadResult readStreaming( File file, ReadOptions options ) throws IOException {
		ReadResult r = new ReadResult();
		r.fileLength = file.length();
		InputStream in = new FileInputStream(file);
		try {
			byte[] headerBytes = readHeader(in);
			int fmt = LogHeader.peekContainerFormat(headerBytes);
			if (fmt > LogFormat.CONTAINER_FORMAT){
				r.status = ReadStatus.UNSUPPORTED_CONTAINER;
				r.problem = "container format " + fmt + " is newer than " + LogFormat.CONTAINER_FORMAT;
				return r;
			}
			try {
				r.header = LogHeader.parse(headerBytes);
			} catch (FormatException e){
				r.status = ReadStatus.BAD_HEADER;
				r.problem = e.getMessage();
				return r;
			}

			long pos = r.header.length;
			r.validBytes = pos;
			ReadStatus firstFailure = null;
			boolean ended = false;

			while (!ended){
				FrameResult f = readFrame(in, pos);
				if (f == null) break;
				if (f.ok){
					try {
						applyFrame(r, f, options);
					} catch (FormatException e){
						f = FrameResult.failure(ReadStatus.CORRUPT,
								"frame at " + pos + ": " + e.getMessage());
					}
				}
				if (f.ok){
					pos = f.streamEnd;
					r.validBytes = pos;
					if (f.type == LogFormat.FRAME_SEAL) ended = true;
				} else {
					firstFailure = f.failure;
					r.problem = f.message;
					break;
				}
			}

			if (r.sealed){
				if (r.sealEventCount != r.eventCount){
					r.status = ReadStatus.SEAL_MISMATCH;
					if (r.problem == null) r.problem = "seal says " + r.sealEventCount
							+ " events, found " + r.eventCount;
				} else if (firstFailure != null){
					r.status = firstFailure;
				} else {
					r.status = ReadStatus.SEALED;
				}
			} else if (firstFailure != null){
				r.status = firstFailure;
			} else {
				r.status = ReadStatus.UNSEALED;
			}
			return r;
		} finally {
			in.close();
		}
	}

	private static byte[] readHeader( InputStream in ) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(LogFormat.MAX_HEADER_BODY + 11);
		for (int i = 0; i < LogFormat.MAGIC.length + 3; i++){
			int b = in.read();
			if (b < 0) return out.toByteArray();
			out.write(b);
		}
		byte[] prefix = out.toByteArray();
		int bodyLength = ((prefix[LogFormat.MAGIC.length + 1] & 0xFF) << 8)
				| (prefix[LogFormat.MAGIC.length + 2] & 0xFF);
		if (bodyLength > LogFormat.MAX_HEADER_BODY) return prefix;
		for (int i = 0; i < bodyLength + 4; i++){
			int b = in.read();
			if (b < 0) break;
			out.write(b);
		}
		return out.toByteArray();
	}

	static byte[] readFully( File file ) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream((int) Math.min(Math.max(file.length(), 16), 1 << 24));
			byte[] buf = new byte[16384];
			int n;
			while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
			return out.toByteArray();
		} finally {
			in.close();
		}
	}

	public static ReadResult parse( byte[] data ){
		return parse(data, new ReadOptions());
	}

	public static ReadResult parse( byte[] data, ReadOptions options ){
		ReadResult r = new ReadResult();
		r.fileLength = data.length;

		int fmt = LogHeader.peekContainerFormat(data);
		if (fmt > LogFormat.CONTAINER_FORMAT){
			r.status = ReadStatus.UNSUPPORTED_CONTAINER;
			r.problem = "container format " + fmt + " is newer than " + LogFormat.CONTAINER_FORMAT;
			return r;
		}
		try {
			r.header = LogHeader.parse(data);
		} catch (FormatException e){
			r.status = ReadStatus.BAD_HEADER;
			r.problem = e.getMessage();
			return r;
		}

		int pos = r.header.length;
		r.validBytes = pos;

		ReadStatus firstFailure = null; //TRUNCATED or CORRUPT
		boolean sealedOk = true;
		boolean ended = false;

		while (pos < data.length && !ended){
			FrameResult f = readFrame(data, pos);
			if (f.ok){
				try {
					applyFrame(r, f, options);
				} catch (FormatException e){
					//checksum was fine but the content is not what the format allows
					f = FrameResult.failure(ReadStatus.CORRUPT, "frame at " + pos + ": " + e.getMessage());
				}
			}

			if (f.ok){
				pos = f.end;
				if (firstFailure == null) r.validBytes = pos;
				if (f.type == LogFormat.FRAME_SEAL) ended = true;
				continue;
			}

			//a bad frame at 'pos'
			if (firstFailure == null){
				firstFailure = f.failure;
				r.problem = f.message;
			}
			if (!options.resync){
				break;
			}
			int next = findNextFrame(data, pos + 1);
			if (next < 0){
				break;
			}
			r.resyncs++;
			r.clockUncertain = true;
			r.skippedBytes += next - pos;
			pos = next;
		}

		if (r.sealed){
			if (r.sealEventCount != r.eventCount){
				r.status = ReadStatus.SEAL_MISMATCH;
				if (r.problem == null) r.problem = "seal says " + r.sealEventCount + " events, found " + r.eventCount;
			} else if (firstFailure != null){
				r.status = firstFailure;
			} else {
				r.status = ReadStatus.SEALED;
			}
		} else if (firstFailure != null){
			r.status = firstFailure;
		} else {
			r.status = ReadStatus.UNSEALED;
		}
		return r;
	}

	private static final class FrameResult {
		boolean ok;
		ReadStatus failure;
		String message;
		int type;
		int end;
		byte[] payload;
		long streamEnd;

		static FrameResult failure( ReadStatus s, String message ){
			FrameResult f = new FrameResult();
			f.failure = s;
			f.message = message;
			return f;
		}
	}

	//Streaming counterpart used by ordinary file reads. Returns null only for clean EOF between frames.
	private static FrameResult readFrame( InputStream in, long pos ) throws IOException {
		int marker = in.read();
		if (marker < 0) return null;
		if (marker != LogFormat.SYNC){
			return FrameResult.failure(ReadStatus.CORRUPT, "no frame marker at " + pos);
		}
		int type = in.read();
		if (type < 0){
			return FrameResult.failure(ReadStatus.TRUNCATED, "file ends inside the frame header at " + pos);
		}

		CRC32 crc = new CRC32();
		crc.update(type);
		ByteArrayOutputStream encodedLength = new ByteArrayOutputStream(10);
		boolean completeLength = false;
		for (int i = 0; i < 10; i++){
			int b = in.read();
			if (b < 0){
				return FrameResult.failure(ReadStatus.TRUNCATED,
						"file ends inside the frame header at " + pos);
			}
			encodedLength.write(b);
			crc.update(b);
			if ((b & 0x80) == 0){
				completeLength = true;
				break;
			}
		}
		if (!completeLength){
			return FrameResult.failure(ReadStatus.TRUNCATED, "file ends inside the frame header at " + pos);
		}

		long len;
		try {
			len = new ByteSource(encodedLength.toByteArray()).readUnsigned();
		} catch (FormatException e){
			return FrameResult.failure(ReadStatus.TRUNCATED, "file ends inside the frame header at " + pos);
		}
		if (len < 0 || len > LogFormat.MAX_FRAME_PAYLOAD){
			return FrameResult.failure(ReadStatus.CORRUPT, "frame at " + pos + " claims " + len + " bytes");
		}

		byte[] payload = new byte[(int) len];
		if (!readExact(in, payload)){
			return FrameResult.failure(ReadStatus.TRUNCATED, "file ends inside the frame at " + pos);
		}
		crc.update(payload, 0, payload.length);
		byte[] checksum = new byte[4];
		if (!readExact(in, checksum)){
			return FrameResult.failure(ReadStatus.TRUNCATED, "file ends inside the frame at " + pos);
		}
		long stored = ((checksum[0] & 0xFFL) << 24) | ((checksum[1] & 0xFFL) << 16)
				| ((checksum[2] & 0xFFL) << 8) | (checksum[3] & 0xFFL);
		if (crc.getValue() != stored){
			return FrameResult.failure(ReadStatus.CORRUPT, "checksum mismatch in the frame at " + pos);
		}

		FrameResult f = new FrameResult();
		f.ok = true;
		f.type = type;
		f.payload = payload;
		f.streamEnd = pos + 1 + 1 + encodedLength.size() + payload.length + 4;
		return f;
	}

	private static boolean readExact( InputStream in, byte[] data ) throws IOException {
		int pos = 0;
		while (pos < data.length){
			int n = in.read(data, pos, data.length - pos);
			if (n < 0) return false;
			if (n == 0){
				int b = in.read();
				if (b < 0) return false;
				data[pos++] = (byte) b;
			} else {
				pos += n;
			}
		}
		return true;
	}

	//Checks the frame at 'pos' (sync, length, checksum). Does not interpret the payload.
	private static FrameResult readFrame( byte[] data, int pos ){
		if ((data[pos] & 0xFF) != LogFormat.SYNC){
			return FrameResult.failure(ReadStatus.CORRUPT, "no frame marker at " + pos);
		}
		try {
			ByteSource src = new ByteSource(data, pos + 1, data.length - pos - 1);
			int type = src.readByte();
			long len = src.readUnsigned();
			if (len < 0 || len > LogFormat.MAX_FRAME_PAYLOAD){
				return FrameResult.failure(ReadStatus.CORRUPT, "frame at " + pos + " claims " + len + " bytes");
			}
			int payloadStart = src.position();
			if (src.remaining() < len + 4){
				return FrameResult.failure(ReadStatus.TRUNCATED, "file ends inside the frame at " + pos);
			}
			src.skip((int) len);
			long stored = src.readInt32() & 0xFFFFFFFFL;
			if (LogFormat.crc(data, pos + 1, payloadStart + (int) len - (pos + 1)) != stored){
				return FrameResult.failure(ReadStatus.CORRUPT, "checksum mismatch in the frame at " + pos);
			}
			FrameResult f = new FrameResult();
			f.ok = true;
			f.type = type;
			f.end = src.position();
			f.payload = java.util.Arrays.copyOfRange(data, payloadStart, payloadStart + (int) len);
			return f;
		} catch (FormatException e){
			//the length field itself runs off the end of the file
			return FrameResult.failure(ReadStatus.TRUNCATED, "file ends inside the frame header at " + pos);
		}
	}

	private static void applyFrame( ReadResult r, FrameResult f, ReadOptions options ){
		switch (f.type){
			case LogFormat.FRAME_STRING: {
				ByteSource p = new ByteSource(f.payload);
				int id = p.readUnsignedInt();
				String value = Utf8.decode(p.readRemaining());
				if (id < r.strings.size()){
					if (r.strings.get(id) != null) throw new FormatException("string " + id + " defined twice");
					r.strings.set(id, value);
				} else if (id == r.strings.size()){
					r.strings.add(value);
				} else if (options.resync){
					// Every original string definition needs bytes; impossible IDs must not allocate unbounded holes.
					if (id > r.fileLength) throw new FormatException("string id exceeds the log size");
					//definitions were lost in a damaged stretch, leave holes
					while (r.strings.size() < id) r.strings.add(null);
					r.strings.add(value);
				} else {
					throw new FormatException("string " + id + " is out of order");
				}
				break;
			}
			case LogFormat.FRAME_EVENT: {
				ByteSource p = new ByteSource(f.payload);
				int type = p.readUnsignedInt();
				int flags = p.readUnsignedInt();
				long delta = p.readSigned();
				if (type == EventTypes.INVALID) throw new FormatException("event type 0");
				byte[] payload = p.readRemaining();
				long clock = r.lastClock + delta;
				r.lastClock = clock;
				if (r.clockUncertain) r.uncertainClockEvents++;
				long index = r.eventCount++;
				if (type == EventTypes.GAP) r.gapEvents++;
				if (options.knownTypesOnly && !EventTypes.isKnown(type)){
					r.skippedUnknownEvents++;
				} else if (!options.onlyRunStartEvents || type == EventTypes.RUN_START){
					//Import checks typed RUN_START identity but needs no retained
					//combat, item, or other gameplay events. Keep all counters,
					//string definitions, checksums and seal validation unchanged.
					r.events.add(new ApdEvent(index, type, flags, clock, payload));
				}
				break;
			}
			case LogFormat.FRAME_CLOCK: {
				ByteSource p = new ByteSource(f.payload);
				r.lastClock = p.readSigned();
				r.clockUncertain = false;
				break;
			}
			case LogFormat.FRAME_SEAL: {
				ByteSource p = new ByteSource(f.payload);
				r.sealEventCount = p.readUnsigned();
				p.readSigned(); //last clock, informational
				r.sealed = true;
				break;
			}
			default:
				r.skippedUnknownFrames++;
		}
	}

	//scans for the next position that starts a fully valid frame
	private static int findNextFrame( byte[] data, int from ){
		for (int i = from; i < data.length; i++){
			if ((data[i] & 0xFF) != LogFormat.SYNC) continue;
			if (readFrame(data, i).ok) return i;
		}
		return -1;
	}
}
