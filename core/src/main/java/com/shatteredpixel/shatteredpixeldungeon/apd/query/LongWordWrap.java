package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.util.function.ToDoubleFunction;

//Only inserts presentation line breaks; never changes stored/exported text.
public final class LongWordWrap {
    private LongWordWrap() {}

    public static String wrap(String text, int width, ToDoubleFunction<String> measure) {
        if (width < 1) throw new IllegalArgumentException("positive text width required");
        StringBuilder out = new StringBuilder(text.length());
        for (int start = 0; start < text.length();) {
            int cp = text.codePointAt(start);
            if (Character.isWhitespace(cp)) {
                out.appendCodePoint(cp);
                start += Character.charCount(cp);
                continue;
            }
            int end = start;
            while (end < text.length() && !Character.isWhitespace(text.codePointAt(end)))
                end += Character.charCount(text.codePointAt(end));
            if (measure.applyAsDouble(text.substring(start, end)) <= width) {
                out.append(text, start, end);
            } else {
                int chunk = start, cursor = start;
                while (cursor < end) {
                    int next = cursor + Character.charCount(text.codePointAt(cursor));
                    if (cursor > chunk && measure.applyAsDouble(text.substring(chunk, next)) > width) {
                        out.append(text, chunk, cursor).append('\n');
                        chunk = cursor;
                    }
                    cursor = next;
                }
                out.append(text, chunk, end);
            }
            start = end;
        }
        return out.toString();
    }
}
