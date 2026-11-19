-- Run reviews (Chris, Nov 19 2026): the reviewed player picks a REVIEW TYPE instead of roasts on/off:
-- 'roasts' (roasts allowed), 'advice' (straight advice only) or 'encouraging'. Reviewers see it before writing.
-- Old rows keep their meaning: roasts=1 -> 'roasts', roasts=0 -> 'advice'.
ALTER TABLE review_requests ADD COLUMN review_type TEXT NOT NULL DEFAULT 'advice';
UPDATE review_requests SET review_type = 'roasts' WHERE roasts = 1;
