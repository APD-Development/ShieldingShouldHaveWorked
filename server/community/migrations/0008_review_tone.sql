-- Run reviews (Chris, Nov 19 2026): each reviewer tags the tone of their own comment (roasts / advice / encouraging).
-- A tone that doesn't match the review type the player asked for is HELD (status 'held') for a moderator instead of shown.
ALTER TABLE reviews ADD COLUMN tone TEXT;
