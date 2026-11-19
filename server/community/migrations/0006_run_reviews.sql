-- Run reviews (Chris's design, Nov 18 2026). A player submits a finished run; reviewers get its seed and setup, play it,
-- and vote salvageable or not with advice. The seed and how the run ended are never shown publicly; a reviewer sees
-- the ending only after sending their own result. Closes at 5 finished reviews (a failed review counts). No retries.
-- Loot generation is unchanged until Chris answers the "luck drop" question.
CREATE TABLE review_requests (
  id TEXT PRIMARY KEY,
  account_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  seed TEXT NOT NULL,              -- secret: only sent to a reviewer who has claimed this run
  setup TEXT NOT NULL,             -- JSON from the game: class, challenges, difficulty, settings
  death_depth INTEGER NOT NULL,    -- the point a reviewer must reach to count as surviving it
  ending TEXT NOT NULL,            -- JSON: how the run ended and its loot; hidden until a reviewer has played
  roasts INTEGER NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'open', -- 'open', 'closed', 'removed'
  created_at INTEGER NOT NULL,
  closed_at INTEGER
);
CREATE INDEX review_requests_open ON review_requests(status, created_at);

CREATE TABLE reviews (
  request_id TEXT NOT NULL REFERENCES review_requests(id) ON DELETE CASCADE,
  reviewer_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  id TEXT NOT NULL UNIQUE,
  claimed_at INTEGER NOT NULL,
  finished_at INTEGER,             -- NULL while still playing
  reached_depth INTEGER,
  survived INTEGER,                -- 1 reached the original death point; 0 a failed review (still counts)
  salvageable INTEGER,
  advice TEXT,
  status TEXT NOT NULL DEFAULT 'visible',
  PRIMARY KEY (request_id, reviewer_id)
);

-- A moderator can take review access away from a reviewer reported for bullying.
ALTER TABLE accounts ADD COLUMN review_banned INTEGER NOT NULL DEFAULT 0;
