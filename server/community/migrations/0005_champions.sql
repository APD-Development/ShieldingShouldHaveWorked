-- Selected content (Chris, Nov 18 2026): each finished season's champion gets a page with a status the admin sets
-- (Planned -> In development -> Testing -> Released), admin dev notes, and player comments. Every past champion stays
-- browsable. The champion is frozen the first time it is recorded, so later bans or vote changes can't move it.
CREATE TABLE champions (
  season_id TEXT PRIMARY KEY REFERENCES seasons(id) ON DELETE CASCADE,
  submission_id TEXT,                 -- no FK: the page outlives the submission row
  author_id TEXT,                     -- NULL once the author deletes their account
  title TEXT NOT NULL,                -- snapshot at the moment it won
  body TEXT NOT NULL,                 -- snapshot; cleared if the author deletes their account
  status TEXT NOT NULL DEFAULT 'planned', -- 'planned', 'in_development', 'testing', 'released'
  won_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);
CREATE INDEX champions_by_win ON champions(won_at);

CREATE TABLE champion_notes (
  id TEXT PRIMARY KEY,
  season_id TEXT NOT NULL REFERENCES champions(season_id) ON DELETE CASCADE,
  body TEXT NOT NULL,
  created_at INTEGER NOT NULL
);
CREATE INDEX champion_notes_by_season ON champion_notes(season_id, created_at);

CREATE TABLE champion_comments (
  id TEXT PRIMARY KEY,
  season_id TEXT NOT NULL REFERENCES champions(season_id) ON DELETE CASCADE,
  parent_id TEXT REFERENCES champion_comments(id) ON DELETE CASCADE,
  author_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  body TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'visible',
  created_at INTEGER NOT NULL
);
CREATE INDEX champion_comments_by_season ON champion_comments(season_id, created_at);
