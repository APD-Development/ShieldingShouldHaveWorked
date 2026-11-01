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

package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
/** Lexical origin for an identified offensive effect, not for unrelated terrain/trap activation. */
public final class OffensiveEffectContext implements AutoCloseable{
 private static final ThreadLocal<OffensiveEffectContext> CURRENT=new ThreadLocal<>();
 private final OffensiveEffectContext previous;private final OffensiveDamageOrigin origin;private boolean closed;
 private OffensiveEffectContext(OffensiveDamageOrigin role){if(role==null)throw new IllegalArgumentException("Missing effect origin");origin=role;previous=CURRENT.get();CURRENT.set(this);}
 public static OffensiveEffectContext enter(OffensiveDamageOrigin role){return new OffensiveEffectContext(role);}
 public static OffensiveDamageOrigin current(){OffensiveEffectContext c=CURRENT.get();return c==null?OffensiveDamageOrigin.UNATTRIBUTED:c.origin;}
 @Override public void close(){if(closed)return;if(CURRENT.get()!=this)throw new IllegalStateException("Effect contexts must close in order");closed=true;if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
}
