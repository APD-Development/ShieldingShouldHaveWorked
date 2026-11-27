package com.shatteredpixel.shatteredpixeldungeon.android;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.shatteredpixel.shatteredpixeldungeon.apd.simulator.HistorySource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

//Reads the Developer build's run history through its read-only link (a content provider only apps signed with the same key can open).
//Each log is copied into this app's cache before reading; nothing is ever written to the Developer build.
final class AndroidHistoryLink implements HistorySource {

	static final String AUTHORITY = "com.charltonchris29.adhdpixeldungeon.indev.history";
	private final Context context;

	AndroidHistoryLink(Context c){ context = c.getApplicationContext(); }

	private List<String> names(){
		List<String> out = new ArrayList<>();
		try (Cursor c = context.getContentResolver().query(Uri.parse("content://" + AUTHORITY + "/runs"), null, null, null, null)){
			if (c == null) return out;
			while (c.moveToNext()) out.add(c.getString(0));
		} catch (RuntimeException notConnected){
			//the Developer build is not installed, or is signed with another key
		}
		return out;
	}

	@Override public String status(){
		List<String> n = names();
		return n.isEmpty() ? "Developer build link: not connected (install the Developer build, or import a UIMSD export ZIP)"
				: "Developer build link: connected, " + n.size() + (n.size() == 1 ? " run" : " runs") + " visible";
	}

	@Override public List<File> runLogs(int max){
		List<File> out = new ArrayList<>();
		File dir = new File(context.getCacheDir(), "reallink");
		dir.mkdirs();
		for (String name : names()){
			if (out.size() >= max) break;
			if (!name.matches("[A-Za-z0-9._-]+\\.apdlog")) continue;
			File copy = new File(dir, name);
			try (InputStream in = context.getContentResolver().openInputStream(Uri.parse("content://" + AUTHORITY + "/runs/" + name));
			     OutputStream o = new FileOutputStream(copy)){
				if (in == null) continue;
				byte[] buf = new byte[16384];
				int r;
				while ((r = in.read(buf)) > 0) o.write(buf, 0, r);
				out.add(copy);
			} catch (Exception unreadable){
				copy.delete();
			}
		}
		return out;
	}
}
