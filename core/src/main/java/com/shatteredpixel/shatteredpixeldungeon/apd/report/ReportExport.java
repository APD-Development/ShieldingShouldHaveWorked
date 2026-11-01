/*
 * ADHD Pixel Dungeon
 * Copyright (C) 2026 Chris Charlton
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

package com.shatteredpixel.shatteredpixeldungeon.apd.report;

import com.shatteredpixel.shatteredpixeldungeon.apd.archive.ApdFiles;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

//Getting a Report out of the app: as text handed to the system share sheet, or as a file the player saves wherever they choose (the same
//file picker the history archive uses). Formats are plain text files on purpose: they open on every device, need no library, and a web page
//can be printed to PDF from any browser. A native PDF writer would add a layout and font engine for one platform only (see
//docs/APD-REPORTS.md).
public final class ReportExport {

	private ReportExport(){}

	public enum Format {
		MARKDOWN("Markdown", "md", "text/markdown"),
		TEXT("Plain text", "txt", "text/plain"),
		HTML("Web page (print to PDF from a browser)", "html", "text/html"),
		CSV("Spreadsheet (CSV, tables only)", "csv", "text/csv");

		public final String label, extension, mime;

		Format( String label, String extension, String mime ){
			this.label = label;
			this.extension = extension;
			this.mime = mime;
		}
	}

	//the longest text the share sheet is given (larger reports are saved as a file instead)
	public static final int SHARE_LIMIT = 100000;

	public interface Done {
		void finished( String message );
	}

	public static String render( Report r, Format f ){
		switch (f){
			case TEXT: return ReportRenderer.text(r);
			case HTML: return ReportRenderer.html(r);
			case CSV: return ReportRenderer.csv(r);
			default: return ReportRenderer.markdown(r);
		}
	}

	static String slug( String title ){
		StringBuilder sb = new StringBuilder();
		for (char c : title.toLowerCase(Locale.ROOT).toCharArray()){
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) sb.append(c);
			else if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') sb.append('-');
		}
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '-') sb.setLength(sb.length() - 1);
		return sb.length() == 0 ? "report" : sb.toString();
	}

	public static String fileName( Report r, Format f ){
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT);
		df.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
		return "apd-" + slug(r.title) + "-" + df.format(new java.util.Date(r.generatedAt)) + "." + f.extension;
	}

	//Saves through the file picker. Never throws: the outcome is passed to 'done' (which may be called on another thread).
	public static void save( File fallbackBase, final Report r, final Format f, final Done done ){
		final byte[] bytes;
		try {
			bytes = render(r, f).getBytes(Charset.forName("UTF-8"));
		} catch (Throwable e){
			done.finished("The report could not be made: " + e);
			return;
		}
		try {
			ApdFiles.provider(fallbackBase).chooseExport(fileName(r, f), f.mime, new ApdFiles.ExportTarget(){
				@Override
				public void onStream( final OutputStream out, final String displayName ){
					Thread t = new Thread(new Runnable(){
						@Override
						public void run(){
							String message;
							try (OutputStream target = out) {
								target.write(bytes);
								target.flush();
								message = "Saved " + f.label.split(" \\(")[0] + " report to " + displayName + " (" + bytes.length + " bytes).";
							} catch (IOException e){
								message = "The report could not be saved: " + e;
							}
							done.finished(message);
						}
					}, "apd-report-save");
					t.setDaemon(true);
					t.start();
				}

				@Override
				public void onCancelled(){
					done.finished("Cancelled. Nothing was saved.");
				}

				@Override
				public void onFailed( String why ){
					done.finished("The report could not be saved: " + why);
				}
			});
		} catch (Throwable e){
			done.finished("The report could not be saved: " + e);
		}
	}

	//Shares as plain text through the system share sheet.
	public static void share( File fallbackBase, Report r, Done done ){
		String text;
		try {
			text = ReportRenderer.text(r);
		} catch (Throwable e){
			done.finished("The report could not be made: " + e);
			return;
		}
		if (text.length() > SHARE_LIMIT){
			done.finished("This report is too long to share as text (" + text.length() + " characters). Save it as a file instead.");
			return;
		}
		boolean ok;
		try {
			ok = ApdFiles.provider(fallbackBase).shareText(r.title, text);
		} catch (Throwable e){
			ok = false;
		}
		done.finished(ok ? "Choose where to send the report." : "Sharing is not available here. Save the report as a file instead.");
	}
}
