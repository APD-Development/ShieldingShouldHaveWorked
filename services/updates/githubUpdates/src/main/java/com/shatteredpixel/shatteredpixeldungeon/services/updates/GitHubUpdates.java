/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
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

package com.shatteredpixel.shatteredpixeldungeon.services.updates;

public class GitHubUpdates extends UpdateService {
 @Override public boolean supportsUpdatePrompts() { return false; }
 @Override public boolean supportsBetaChannel() { return false; }
 @Override public void checkForUpdate(boolean useMetered, boolean includeBetas, UpdateResultCallback callback) { callback.onNoUpdateFound(); }
 @Override public void initializeUpdate(AvailableUpdateData update) { /* APD updates are distributed separately. */ }
 @Override public boolean supportsReviews() { return false; }
 @Override public void initializeReview(ReviewResultCallback callback) { callback.onComplete(); }
 @Override public void openReviewURI() {}
}
