-- Post types (Chris, Nov 25 2026, option 1): each idea has one type, shown on the post and usable as a browse filter.
-- One vote, one bracket, one champion per season regardless of type. Old posts become 'other'.
ALTER TABLE submissions ADD COLUMN post_type TEXT NOT NULL DEFAULT 'other';
