-- Images on ideas (Chris, Nov 25 2026, community item 3): up to 8 jpg/png per idea, each checked by an automatic content
-- filter on upload; refused images are never stored. 'held' = borderline, waiting for a moderator; only 'approved' is shown.
-- The bytes live in the image store binding (R2), keyed by id; this table is the index. Off until the bindings exist.
CREATE TABLE idea_images (
  id TEXT PRIMARY KEY,
  submission_id TEXT NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
  author_id TEXT NOT NULL,
  content_type TEXT NOT NULL,
  size INTEGER NOT NULL,
  status TEXT NOT NULL, -- 'approved' or 'held'
  created_at INTEGER NOT NULL
);
CREATE INDEX idea_images_by_submission ON idea_images (submission_id, created_at);
