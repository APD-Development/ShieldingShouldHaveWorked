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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.apd.community.CommunityApi;
import com.shatteredpixel.shatteredpixeldungeon.apd.community.CommunityController;
import com.shatteredpixel.shatteredpixeldungeon.apd.community.CommunityPreview;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdList;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Separate from UIMSD and its unlock gate; reuses the game's wrapped, scrolling list UI.
// With no server address configured it shows the offline preview of what's planned; with one, the live Community
// (CommunityController holds all the logic; this scene only draws its screens and asks for text).
public class CommunityScene extends PixelScene {
    private static CommunityPreview.Page selected;
    private WndUimsdList shown;

    public static void open() {
        selected = null;
        ShatteredPixelDungeon.switchNoFade(CommunityScene.class);
    }

    @Override public void create() {
        super.create();
        com.shatteredpixel.shatteredpixeldungeon.apd.age.CommunityAgeGate.deliver = r -> Game.runOnRenderThread(r::run);
        add(new TitleBackground(Camera.main.width, Camera.main.height));
        String server = SPDSettings.communityServer();
        if (server == null || server.trim().isEmpty()) { showPage(); return; }
        CommunityApi api;
        try { api = new CommunityApi(server, 15000); } catch (IllegalArgumentException e) { showPage(); return; }
        try {
            CommunityController.modsDir = com.watabou.utils.FileUtils.getFileHandle(com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModHeroSelection.FOLDER).file();
        } catch (RuntimeException e) { CommunityController.modsDir = null; }
        new CommunityController(api, new SceneUi(), CommunityScene::runAsync, new Prefs(), CommunityController.now()).start();
    }

    private static void runAsync(java.util.function.Supplier<CommunityApi.Result> call, Consumer<CommunityApi.Result> then) {
        Thread t = new Thread(() -> {
            CommunityApi.Result r = call.get();
            Game.runOnRenderThread(() -> then.accept(r));
        }, "apd-community");
        t.setDaemon(true);
        t.start();
    }

    private static final class Prefs implements CommunityController.Storage {
        public String token() { return SPDSettings.communityToken(); }
        public void token(String token) { SPDSettings.communityToken(token); }
        public String get() { return SPDSettings.communityAgeGate(); }
        public void put(String value) { SPDSettings.communityAgeGate(value); }
    }

    private final class SceneUi implements CommunityController.Ui {
        // Android rebuilds this scene when the game resumes (e.g. after the Google account picker). An answer that
        // arrives for the old, destroyed scene must not draw on it (crash, Nov 18): reload the live one instead, which
        // picks up the saved sign-in.
        private boolean reloaded;
        private boolean stale() {
            if (Game.scene() == CommunityScene.this) return false;
            if (!reloaded && Game.scene() instanceof CommunityScene) { reloaded = true; ShatteredPixelDungeon.switchNoFade(CommunityScene.class); }
            return true;
        }

        @Override public void show(CommunityController.Screen screen) {
            if (stale()) return;
            if (shown != null) { shown.hide(); shown = null; }
            List<WndUimsdList.Entry> entries = new ArrayList<>();
            final List<CommunityController.Row> items = screen.items();
            for (CommunityController.Row r : screen.rows) {
                switch (r.kind) {
                    case 0: entries.add(WndUimsdList.Entry.heading(r.label)); break;
                    case 1: entries.add(WndUimsdList.Entry.note(r.label)); break;
                    case 3: entries.add(WndUimsdList.Entry.warning(r.label)); break;
                    default:
                        WndUimsdList.Entry e = r.detail.isEmpty() ? WndUimsdList.Entry.item(r.label) : WndUimsdList.Entry.item(r.label, r.detail);
                        if (r.checked != null) e.current(r.checked);
                        e.icon(communityIcon(r.label));
                        entries.add(e);
                }
            }
            final Runnable back = screen.back;
            com.watabou.noosa.Image preview = screen.previewPng == null ? null
                    : com.shatteredpixel.shatteredpixeldungeon.apd.mods.ModSprites.avatar(screen.previewPng, screen.previewKey, 0);
            shown = new WndUimsdList(screen.title, screen.message == null ? "" : screen.message, false, entries, (index, checked) -> {
                shown = null; // the list closes itself when a row is tapped
                if (index >= 0 && index < items.size() && items.get(index).action != null) items.get(index).action.run();
            }, preview) {
                @Override public void onBackPressed() {
                    hide();
                    shown = null;
                    if (back != null) back.run(); else leave();
                }
            };
            addToFront(shown);
        }

        @Override public void ask(String title, String body, String initial, int maxLength, boolean multiLine, Consumer<String> answer) {
            if (stale()) return;
            if (shown != null) { shown.hide(); shown = null; }
            addToFront(new WndTextInput(title, body, initial, maxLength, multiLine, "OK", "Cancel") {
                @Override public void onSelect(boolean positive, String text) { answer.accept(positive ? text : null); }
            });
        }

        @Override public void busy(String message) {
            if (stale()) return;
            // A short wait: the next screen replaces this as soon as the server answers.
            if (shown != null) { shown.hide(); shown = null; }
            List<WndUimsdList.Entry> entries = new ArrayList<>();
            entries.add(WndUimsdList.Entry.note(message));
            shown = new WndUimsdList("Community", "", false, entries, (i, c) -> {}) {
                @Override public void onBackPressed() { }
            };
            addToFront(shown);
        }

        @Override public void leave() { ShatteredPixelDungeon.switchNoFade(TitleScene.class); }

        // Run reviews: start the claimed run with the reviewer's own class slot for the locked hero class. The seed and
        // setup come from the claim (Dungeon.initSeed/init); the player's own settings are left untouched.
        @Override public void playReview(com.shatteredpixel.shatteredpixeldungeon.apd.community.ReviewClaim claim) {
            if (shown != null) { shown.hide(); shown = null; }
            com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass cl = claim.setup.heroClass;
            int slot = com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.firstEmpty(cl);
            if (slot < 0) {
                ShatteredPixelDungeon.scene().addToFront(new com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage(
                        "All 20 " + cl.title() + " runs are occupied. Finish or remove one, then take this run again."));
                return;
            }
            com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass = cl;
            com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.curSlot = slot;
            com.shatteredpixel.shatteredpixeldungeon.apd.community.ReviewPlay.starting = claim;
            com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = null;
            com.shatteredpixel.shatteredpixeldungeon.Dungeon.daily = com.shatteredpixel.shatteredpixeldungeon.Dungeon.dailyReplay = false;
            com.shatteredpixel.shatteredpixeldungeon.Dungeon.initSeed();
            com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.clearAction();
            InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
            com.watabou.noosa.Game.switchScene(InterlevelScene.class);
        }

        @Override public void planned() {
            if (shown != null) { shown.hide(); shown = null; }
            fromLive = true;
            selected = null;
            showPage();
        }
    }

    // True while the planned-features pages were opened from the live hub: backing out goes to the hub, not the title.
    private boolean fromLive;

    // ---- offline preview (no server address yet) ----

    /** UI polish (Chris, Nov 22): a small icon on each Community card, picked by what the item is. */
    static com.shatteredpixel.shatteredpixeldungeon.ui.Icons communityIcon(String label){
        String l = label == null ? "" : label.toLowerCase(java.util.Locale.ROOT);
        if (l.equals("back") || l.startsWith("back ")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.LEFTARROW;
        if (l.startsWith("submission")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.NEWS;
        if (l.startsWith("voting") || l.contains("result") || l.contains("tournament")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.RANKINGS;
        if (l.startsWith("selected") || l.contains("champion")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.BADGES;
        if (l.contains("review")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.MAGNIFY;
        if (l.startsWith("account") || l.contains("sign out") || l.contains("delete")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.PREFS;
        if (l.startsWith("moderation") || l.contains("report")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.WARNING;
        if (l.contains("coming next")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.CALENDAR;
        if (l.contains("sign in") || l.contains("create an account") || l.contains("verification")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.ENTER;
        if (l.contains("try again")) return com.shatteredpixel.shatteredpixeldungeon.ui.Icons.REPEAT;
        return null;
    }

    private void showPage() {

        List<WndUimsdList.Entry> entries = new ArrayList<>();
        String title;
        String message;
        if (selected == null) {
            title = fromLive ? "What's coming next" : "Community";
            message = fromLive ? "What's live now and what's planned for Community." : "Offline alpha preview: explore APD's planned community hub. Online features are not available here yet.";
            for (CommunityPreview.Page page : CommunityPreview.Page.values())
                entries.add(WndUimsdList.Entry.item(page.title, page.summary));
            //Developer build only: point Community at a test server without a new build
            if (com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures.testBuild())
                entries.add(WndUimsdList.Entry.item("Community server address (Developer)", SPDSettings.communityServer().isEmpty() ? "Not set" : SPDSettings.communityServer()));
        } else {
            title = selected.title;
            message = "Planned feature — offline preview";
            entries.add(WndUimsdList.Entry.note(selected.explanation));
            if (selected == CommunityPreview.Page.SUBMISSIONS) {
                entries.add(WndUimsdList.Entry.heading("Submission"));
                entries.add(WndUimsdList.Entry.note("The author's idea and description will appear here. This preview contains no example pitches or player submissions."));
                entries.add(WndUimsdList.Entry.heading("Comments and replies — planned"));
                entries.add(WndUimsdList.Entry.note(CommunityPreview.COMMENTS));
                entries.add(WndUimsdList.Entry.heading("Comment threads"));
                entries.add(WndUimsdList.Entry.note(CommunityPreview.COMMENT_PLACEHOLDER));
            }
            entries.add(WndUimsdList.Entry.heading("Availability"));
            entries.add(WndUimsdList.Entry.note(selected.availability()));
        }
        entries.add(WndUimsdList.Entry.item(selected == null && !fromLive ? "Main menu" : "Back to Community"));
        addToFront(new WndUimsdList(title, message, false, entries, (index, checked) -> {
            if (selected == null && index < CommunityPreview.Page.values().length) {
                selected = CommunityPreview.Page.values()[index];
                showPage();
            } else if (selected == null && index == CommunityPreview.Page.values().length
                    && com.shatteredpixel.shatteredpixeldungeon.apd.ApdFeatures.testBuild()) {
                addToFront(new WndTextInput("Community server", "https address of the Community server (empty to clear).",
                        SPDSettings.communityServer(), 200, false, "Save", "Cancel") {
                    @Override public void onSelect(boolean positive, String text) {
                        if (positive) SPDSettings.communityServer(text == null ? "" : text.trim());
                        ShatteredPixelDungeon.switchNoFade(CommunityScene.class);
                    }
                });
            } else onBackPressed();
        }) {
            @Override public void onBackPressed() {
                hide();
                CommunityScene.this.onBackPressed();
            }
        });
    }
    @Override protected void onBackPressed() {
        if (selected != null) { selected = null; showPage(); }
        else if (fromLive) ShatteredPixelDungeon.switchNoFade(CommunityScene.class);
        else ShatteredPixelDungeon.switchNoFade(TitleScene.class);
    }
}
