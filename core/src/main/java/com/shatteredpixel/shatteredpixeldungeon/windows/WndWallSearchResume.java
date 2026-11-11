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
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchControlContext;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.SearchControlContext.Action;
import com.shatteredpixel.shatteredpixeldungeon.apd.qol.WallSearchSession;
import com.shatteredpixel.shatteredpixeldungeon.effects.StairHighlight;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public final class WndWallSearchResume {
    private WndWallSearchResume() { }
    public static void show() {
        Hero hero = Dungeon.hero;
        Level level = Dungeon.level;
        if (hero == null || level == null || !hero.isAlive() || !hero.ready || !hero.wallSearch.exists()) return;
        WallSearchSession saved = hero.wallSearch;
        String note = "Saved search on Floor " + saved.floor() + ". Manual searches completed here: " + saved.searches()
                + ".\n\nSearch here performs one normal manual search at your current position. Resume wall search returns by a safe discovered route and searches once. Movement and searching keep their normal costs. Visible enemies or danger stop the return; your saved position is kept.";
        Action[] actions = SearchControlContext.choices(hero, level);
        String[] options = new String[actions.length];
        for (int i = 0; i < actions.length; i++) options[i] = label(actions[i]);
        String title = actions[0] == Action.SEARCH ? "Search"
                : actions[0] == Action.RESUME_MOVEMENT ? "Resume" : "Remembered wall search";
        GameScene.show(new WndOptions(Icons.get(Icons.INFO), title, note, options) {
            @Override protected void onSelect(int index) {
                if (index >= 0 && index < actions.length) choose(hero, level, actions[index]);
            }
        });
    }

    private static String label(Action action) {
        switch (action) {
            case SEARCH: return "Search here";
            case WALL_SEARCH: return "Resume wall search";
            case SHOW_SAVED: return "Show saved search position";
            case RESUME_MOVEMENT: return "Resume previous movement";
            case FORGET: return "Forget this search";
            default: return "Cancel";
        }
    }

    private static void choose(Hero hero, Level level, Action action) {
        if (hero != Dungeon.hero || level != Dungeon.level || !hero.isAlive() || !hero.ready) return;
        WallSearchSession saved = hero.wallSearch;
        switch (action) {
            case SEARCH:
                hero.search(true);
                break;
            case WALL_SEARCH:
                String problem = saved.resume(hero);
                if (problem != null) GLog.w(problem);
                else if (hero.curAction instanceof WallSearchSession.Walk) hero.next();
                break;
            case SHOW_SAVED:
                if (saved.matches(level)) {
                    StairHighlight.showKnown(saved.anchor());
                    if (saved.target() >= 0) StairHighlight.showKnown(saved.target());
                } else GLog.w("The saved search is on Floor " + saved.floor() + ".");
                break;
            case RESUME_MOVEMENT:
                if (hero.lastAction != null) hero.resume();
                break;
            case FORGET:
                saved.clear();
                break;
            default:
                break;
        }
    }
}
