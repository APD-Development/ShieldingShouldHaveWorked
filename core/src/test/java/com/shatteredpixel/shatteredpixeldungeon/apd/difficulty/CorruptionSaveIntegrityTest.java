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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.watabou.utils.Bundle;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;
public class CorruptionSaveIntegrityTest {
    private float partial(Corruption buff) throws Exception {
        Field f=Corruption.class.getDeclaredField("partialDamage");
        f.setAccessible(true); return f.getFloat(buff);
    }
    private Corruption reload(Corruption buff) {
        Bundle data=new Bundle(); buff.storeInBundle(data);
        Corruption copy=new Corruption(); copy.restoreFromBundle(data); return copy;
    }
    private static class Target extends Char {
        int received;
        Target(int hp){HT=HP=hp;}
        @Override public void damage(int amount,Object source){received+=amount;}
    }
    @Test public void preservesFractionalAccumulationAcrossSave() throws Exception {
        Target target=new Target(25); Corruption buff=new Corruption(); buff.target=target;
        for(int i=0;i<3;i++)buff.act();
        assertEquals(.75f,partial(buff),0);
        Corruption copy=reload(buff); assertEquals(.75f,partial(copy),0);
        copy.target=target; copy.act(); assertEquals(1,target.received);
        assertEquals(0f,partial(copy),0);
    }
    @Test public void repeatedReloadMatchesUninterruptedDecay() throws Exception {
        Target a=new Target(37),b=new Target(37);
        Corruption continuous=new Corruption(),saved=new Corruption();
        continuous.target=a;saved.target=b;
        for(int i=0;i<300;i++){
            continuous.act();saved.act();saved=reload(saved);saved.target=b;
            assertEquals(a.received,b.received);
            assertEquals(partial(continuous),partial(saved),0);
        }
    }
    @Test public void acceptsLegacyIntegerAndAbsentFields() throws Exception {
        Corruption buff=new Corruption();Bundle old=new Bundle();
        old.put(Corruption.PARTIAL_DAMAGE,1);buff.restoreFromBundle(old);
        assertEquals(1f,partial(buff),0);
        buff.restoreFromBundle(new Bundle());assertEquals(0f,partial(buff),0);
    }
    @Test public void serializedBundlePreservesFractionalValue() throws Exception {
        Corruption buff=new Corruption();Target t=new Target(25);buff.target=t;buff.act();
        Bundle data=new Bundle();buff.storeInBundle(data);
        java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
        assertTrue(Bundle.write(data,bytes));
        Bundle read=Bundle.read(new java.io.ByteArrayInputStream(bytes.toByteArray()));
        Corruption copy=new Corruption();copy.restoreFromBundle(read);
        assertEquals(.25f,partial(copy),0);
    }
}

