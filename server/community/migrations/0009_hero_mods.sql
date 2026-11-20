-- Hero mod workshop (Chris, Nov 19 2026, plan step 4e): DATA + ART ONLY. A mod is the exact hero.json text plus its
-- sprite.png (base64). Signed-in 16+ members upload, anyone browses and downloads, members subscribe and report.
-- The game re-validates every downloaded mod with its own HeroMod/ModLibrary rules before it can be played.
CREATE TABLE mods (
  id TEXT PRIMARY KEY,
  author_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  mod_id TEXT NOT NULL UNIQUE,     -- hero.json "id"; one author owns it, re-uploading replaces it
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  hero_json TEXT NOT NULL,         -- <= 16 KB
  sprite TEXT NOT NULL,            -- base64 PNG, <= 64 KB decoded, 256x128
  version INTEGER NOT NULL DEFAULT 1,
  status TEXT NOT NULL DEFAULT 'visible', -- 'visible', 'hidden' (admin), 'removed' (admin)
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);
CREATE INDEX mods_list ON mods(status, updated_at);

CREATE TABLE mod_subscriptions (
  mod_id TEXT NOT NULL REFERENCES mods(id) ON DELETE CASCADE,
  account_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  created_at INTEGER NOT NULL,
  PRIMARY KEY (mod_id, account_id)
);

-- Every upload or update, for the daily cap.
CREATE TABLE mod_uploads (
  account_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  created_at INTEGER NOT NULL
);
CREATE INDEX mod_uploads_by ON mod_uploads(account_id, created_at);
