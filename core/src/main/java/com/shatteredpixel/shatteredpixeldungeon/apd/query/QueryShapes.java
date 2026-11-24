package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * UIMSD's surprised reaction to an unusual question (owner spec in APD-HANDOFF "UNUSUAL-QUERY REACTION").
 * It fires only when a question is BOTH complex (complexity at or above a high floor; Question.complexity is one input)
 * AND its shape (what is asked, never the filter values) has never been answered on this device before, and at most once
 * per cooldown so players can forget it exists. It never changes the answer and never claims anything global: the
 * memory is local, so the line only says UIMSD itself hasn't seen that shape before.
 */
public final class QueryShapes {

	public static final String FILE_NAME = "query_shapes.txt";
	public static final int COMPLEXITY_FLOOR = 7;
	public static final int MAX_SHAPES = 2000;
	public static final long COOLDOWN_MS = 14L * 24 * 60 * 60 * 1000;
	private static final String LAST = "last ";

	private final LinkedHashSet<String> shapes = new LinkedHashSet<>();
	private long lastReaction;

	public static File fileIn( File historyRoot ){
		return new File(historyRoot, FILE_NAME);
	}

	//What the question asks for, without the values it filters on: two questions with the same shape differ only in numbers/names.
	public static String shape( Question q ){
		StringBuilder b = new StringBuilder(q.dataset).append('|').append(q.measure.name());
		if (q.column != null) b.append(':').append(q.column);
		b.append('|').append(q.per == null ? "-" : q.per.name()).append('|');
		List<String> g = new ArrayList<>(q.groupBy);
		java.util.Collections.sort(g);
		b.append(String.join(",", g)).append('|');
		List<String> f = new ArrayList<>();
		for (Filter x : q.filters) f.add(x.column + " " + x.op.name());
		java.util.Collections.sort(f);
		b.append(String.join(",", f)).append('|').append(q.showInternal ? 'i' : '-');
		return b.toString().replace('\n', ' ');
	}

	public boolean seen( String shape ){
		return shapes.contains(shape);
	}

	/**
	 * Remembers this question's shape and says whether UIMSD should react to it. Call once per answered question.
	 */
	public boolean observe( Question q, long now ){
		String s = shape(q);
		boolean novel = !shapes.contains(s);
		if (novel){
			shapes.add(s);
			while (shapes.size() > MAX_SHAPES) shapes.remove(shapes.iterator().next());
		}
		boolean react = novel && q.complexity() >= COMPLEXITY_FLOOR
				&& (lastReaction <= 0 || now - lastReaction >= COOLDOWN_MS || now < lastReaction);
		if (react) lastReaction = now;
		return react;
	}

	//The line shown above the answer. Same meaning in Clean and Adult; Adult only adds a mild swear.
	public static String line( Question q, boolean adult ){
		int n = q.complexity();
		return adult
				? "Wait... I've never been asked anything shaped like that before. You just had me tie " + n + " things together. Give me a damn second. ...huh. Here it is."
				: "Wait... I've never been asked anything shaped like that before. You just had me tie " + n + " things together. Give me a second. ...huh. Here it is.";
	}

	public static QueryShapes load( File file ){
		QueryShapes s = new QueryShapes();
		if (file == null || !file.isFile()) return s;
		try {
			for (String l : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)){
				if (l.startsWith(LAST)){
					try { s.lastReaction = Long.parseLong(l.substring(LAST.length()).trim()); } catch (NumberFormatException ignored){}
				} else if (!l.isEmpty()) s.shapes.add(l);
			}
		} catch (IOException | RuntimeException ignored){
			//unreadable memory only means UIMSD may be surprised again; never fails the answer
		}
		while (s.shapes.size() > MAX_SHAPES) s.shapes.remove(s.shapes.iterator().next());
		return s;
	}

	public void store( File file ) throws IOException {
		StringBuilder b = new StringBuilder();
		b.append(LAST).append(lastReaction).append('\n');
		for (String s : shapes) b.append(s).append('\n');
		File tmp = new File(file.getPath() + ".tmp");
		Files.write(tmp.toPath(), b.toString().getBytes(StandardCharsets.UTF_8));
		Files.move(tmp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}

	public int size(){ return shapes.size(); }
}
