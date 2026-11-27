package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Finds text a player should never see: code names, missing-text markers, unfilled placeholders and wrong plurals.
//Pure string rules so they can be tested without the game. Returns null when the text looks fine.
public final class TextCheck {

	private TextCheck(){}

	//a Java class path such as "actors.mobs.Piranha" or "items.potions.PotionOfHealing"
	private static final Pattern CODE_PATH = Pattern.compile(
			"\\b(?:actors|items|levels|plants|scenes|windows|buffs|mobs|hero|npcs|weapon|armor|potions|scrolls|wands|rings|artifacts|food|bags|bombs|spells|stones|keys|trinkets|traps|rooms|features|effects|journal|apd)\\.(?:[a-z]+\\.)*[A-Za-z][A-Za-z0-9]*\\b");
	private static final Pattern COM_PATH = Pattern.compile("\\bcom\\.[a-z]");
	//an anonymous or inner class name leaking into text: Bomb$Conjure, Foo$1
	private static final Pattern INNER_CLASS = Pattern.compile("\\b[A-Z][A-Za-z0-9]+\\$[A-Za-z0-9]+");
	//placeholders that were never filled in
	private static final Pattern PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?[sdf]|\\{\\d\\}");
	//"1 turns", "1 coins"
	private static final String NOUNS = "turn|coin|charge|item|enemy|floor|time|level|point|second|hit|piece|use|dart|arrow|stone|seed|key|potion|scroll|bomb|ration";
	private static final Pattern ONE_PLURAL = Pattern.compile("(?<![\\d.,/+-])\\b1 (" + NOUNS.replace("enemy", "enemie").replace("|", "s|") + "s)\\b");
	//"3 turn " (a number above one followed by a singular noun that is not used as an adjective)
	private static final Pattern MANY_SINGULAR = Pattern.compile("(?<![\\d.,/+-])\\b([02-9]|[1-9]\\d+) (" + NOUNS + ")\\b(?![-'\\w])(?! (?:of|ago|limit|bonus|cost|penalty|duration|count|cap|delay|timer|cooldown|radius|range))");

	//Clean mode audit: with Adult humor off, none of these may ever be shown (whole words, any case)
	private static final Pattern SWEAR = Pattern.compile(
			"(?i)\\b(fuck\\w*|motherfuck\\w*|shit(s|ty|ting|ted|head\\w*)?|bullshit|bitch\\w*|bastard\\w*|asshole\\w*|dickhead\\w*|cunt\\w*|cock(sucker)?s?|piss(ed)?|goddamn\\w*|twat\\w*|wanker\\w*|prick\\w*|slut\\w*|whore\\w*)\\b");

	//null when the text is clean
	public static String swear(String text){
		if (text == null) return null;
		Matcher m = SWEAR.matcher(text);
		return m.find() ? "swear word shown with Adult humor off (\"" + m.group() + "\")" : null;
	}

	public static String problem(String text){
		if (text == null) return null;
		if (text.contains("NO TEXT FOUND")) return "missing text marker (NO TEXT FOUND)";
		if (text.contains("!!!")) return "missing text marker (!!!)";
		Matcher m = COM_PATH.matcher(text);
		if (m.find()) return "code package name shown (" + around(text, m.start()) + ")";
		m = CODE_PATH.matcher(text);
		if (m.find()) return "code class path shown (" + m.group() + ")";
		m = INNER_CLASS.matcher(text);
		if (m.find()) return "code class name shown (" + m.group() + ")";
		m = PLACEHOLDER.matcher(text);
		if (m.find()) return "unfilled placeholder (" + around(text, m.start()) + ")";
		m = ONE_PLURAL.matcher(text);
		if (m.find()) return "wrong plural (" + m.group() + ")";
		m = MANY_SINGULAR.matcher(text);
		if (m.find()) return "wrong plural (" + m.group() + ")";
		if (text.matches("(?s).*\\bnull\\b.*") && !text.toLowerCase().contains("nullif")) return "the word null shown (" + around(text, text.indexOf("null")) + ")";
		return null;
	}

	private static String around(String text, int at){
		int from = Math.max(0, at - 20), to = Math.min(text.length(), at + 30);
		return text.substring(from, to).replace('\n', ' ');
	}
}
