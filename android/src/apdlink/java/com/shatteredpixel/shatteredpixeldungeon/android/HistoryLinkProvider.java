package com.shatteredpixel.shatteredpixeldungeon.android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

//Developer build only (never in Play or Alpha builds): lets the APD Simulator app, signed with the same key, READ this phone's
//run logs. Lists file names and opens them read only. Every write operation is refused.
public class HistoryLinkProvider extends ContentProvider {

	private File runsDir(){
		//the game's UIMSD lives in <files>/apd/runs (ApdRecorder.configureDefault)
		return new File(new File(getContext().getFilesDir(), "apd"), "runs");
	}

	@Override public boolean onCreate(){ return true; }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
		MatrixCursor c = new MatrixCursor(new String[]{"name", "size", "modified"});
		if (!"/runs".equals(uri.getPath())) return c;
		File[] logs = runsDir().listFiles();
		if (logs == null) return c;
		Arrays.sort(logs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
		for (File f : logs){
			if (f.isFile() && f.getName().endsWith(".apdlog")) c.addRow(new Object[]{f.getName(), f.length(), f.lastModified()});
		}
		return c;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		if (!"r".equals(mode)) throw new SecurityException("read only");
		String name = uri.getLastPathSegment();
		if (name == null || !name.matches("[A-Za-z0-9._-]+\\.apdlog") || !uri.getPath().equals("/runs/" + name)) throw new FileNotFoundException();
		File f = new File(runsDir(), name);
		if (!f.isFile()) throw new FileNotFoundException();
		return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
	}

	@Override public String getType(Uri uri){ return "application/octet-stream"; }
	@Override public Uri insert(Uri uri, ContentValues values){ throw new UnsupportedOperationException("read only"); }
	@Override public int delete(Uri uri, String selection, String[] selectionArgs){ throw new UnsupportedOperationException("read only"); }
	@Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){ throw new UnsupportedOperationException("read only"); }
}
