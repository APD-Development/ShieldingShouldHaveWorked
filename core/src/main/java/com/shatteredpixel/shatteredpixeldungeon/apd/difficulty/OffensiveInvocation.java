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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
/** A lexical callback scope; never retains actors after the callback completes. */
public final class OffensiveInvocation implements AutoCloseable {
 private static final ThreadLocal<OffensiveInvocation> CURRENT=new ThreadLocal<>();
 private final OffensiveInvocation previous;private final Char actor;private final OffensiveDamageOrigin origin;
 private boolean closed;
 private OffensiveInvocation(Char actor,OffensiveDamageOrigin origin){if(origin==null)throw new IllegalArgumentException("Missing invocation origin");this.actor=actor;this.origin=origin;previous=CURRENT.get();CURRENT.set(this);}
 public static OffensiveInvocation enter(Char actor,OffensiveDamageOrigin origin){return new OffensiveInvocation(actor,origin);}
 static OffensiveDamageOrigin captured(Char actor){for(OffensiveInvocation scope=CURRENT.get();scope!=null;scope=scope.previous)if(scope.actor==actor)return scope.origin;return null;}
 @Override public void close(){if(closed)return;if(CURRENT.get()!=this)throw new IllegalStateException("Invocation scopes must close in order");closed=true;if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
}
