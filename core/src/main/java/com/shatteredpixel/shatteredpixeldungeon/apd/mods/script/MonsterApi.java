package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import org.mozilla.javascript.Scriptable;

import java.util.Locale;

/** A mod monster as its own script sees it: facts plus a few bounded actions on itself. */
final class MonsterApi {
	private MonsterApi(){}

	static ScriptApi.ApiObject make(Scriptable scope, ScriptMob m){
		ScriptApi.ApiObject o = new ScriptApi.ApiObject(scope, "Monster");
		o.value("hp", m.HP).value("maxHp", m.HT).value("pos", m.pos).value("id", m.monsterId());
		o.fn("heal", a -> {
			int n = Math.min(ScriptApi.intIn(a, 0, 0, 0, HeroApi.MAX_AMOUNT), m.HT - m.HP);
			if (n > 0){ m.HP += n; if (m.sprite != null) m.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(n), FloatingText.HEALING); }
			return (double) Math.max(0, n);
		});
		o.fn("damage", a -> {
			int n = ScriptApi.intIn(a, 0, 0, 0, HeroApi.MAX_AMOUNT);
			if (n > 0 && m.isAlive()) m.damage(n, new HeroApi.ScriptSource(m.modId()));
			return null;
		});
		o.fn("addBuff", a -> {
			String name = ScriptApi.str(a, 0, "", 30).toLowerCase(Locale.ROOT);
			Class<? extends FlavourBuff> c = HeroApi.BUFFS.get(name);
			if (c == null) throw new ScriptDefs.BadDef("asked for buff \"" + name + "\"; allowed: " + String.join(", ", HeroApi.BUFFS.keySet()));
			Buff.prolong(m, c, ScriptApi.intIn(a, 1, 10, 1, HeroApi.MAX_BUFF_TURNS));
			return null;
		});
		o.fn("message", a -> { GLog.i(ScriptApi.str(a, 0, "", HeroApi.MAX_MESSAGE)); return null; });
		return o;
	}
}
