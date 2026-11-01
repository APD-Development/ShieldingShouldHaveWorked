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

package com.shatteredpixel.shatteredpixeldungeon.android;

import static org.junit.Assert.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.android.AsynchronousSound;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Controlled scheduling diagnosis, NOT audible reproduction or a claimed game defect. */
@RunWith(AndroidJUnit4.class)
public class AudioSchedulingDiagnosticDeviceTest {
    @Test(timeout=15000) public void heldBackendCallQueuesLaterRequestsOnTheSharedWorker() throws Exception {
        HandlerThread thread=new HandlerThread("APD controlled audio diagnostic");
        CountDownLatch entered=new CountDownLatch(1), release=new CountDownLatch(1), done=new CountDownLatch(4);
        List<Integer> order=new ArrayList<>();
        List<Long> dispatched=new ArrayList<>();
        List<String> threads=new ArrayList<>();
        thread.start();
        try {
            Sound endpoint=(Sound)Proxy.newProxyInstance(Sound.class.getClassLoader(),new Class<?>[]{Sound.class},(proxy,method,args)->{
                if(method.getName().equals("play")){
                    int marker=Math.round((Float)args[0]);
                    synchronized(order){order.add(marker);dispatched.add(SystemClock.elapsedRealtimeNanos());threads.add(Thread.currentThread().getName());}
                    if(marker==1){entered.countDown();assertTrue("release controlled backend",release.await(5,TimeUnit.SECONDS));}
                    done.countDown();
                    return 1L;
                }
                return method.getReturnType()==long.class?0L:null;
            });
            AsynchronousSound sound=new AsynchronousSound(endpoint,new Handler(thread.getLooper()));
            assertEquals(0,sound.play(1,1,0));
            assertTrue("worker entered controlled backend",entered.await(5,TimeUnit.SECONDS));
            long requestTime=SystemClock.elapsedRealtimeNanos();
            for(int marker=2;marker<=4;marker++)assertEquals(0,sound.play(marker,1,0));
            synchronized(order){assertEquals("later calls remain queued while the backend is held",1,order.size());}
            long releasedAt=SystemClock.elapsedRealtimeNanos();
            release.countDown();
            assertTrue("all actual worker callbacks completed",done.await(5,TimeUnit.SECONDS));
            synchronized(order){
                assertEquals(java.util.Arrays.asList(1,2,3,4),order);
                for(int i=1;i<4;i++){assertTrue(dispatched.get(i)>=releasedAt);assertEquals(thread.getName(),threads.get(i));}
                System.out.println("CONTROLLED_AUDIO_DIAGNOSTIC requests="+requestTime+" released="+releasedAt+" dispatch="+dispatched+" order="+order+" audible_reproduction=false physical_device=false");
            }
        } finally {
            release.countDown();thread.quitSafely();thread.join(5000);
            assertFalse("diagnostic worker terminated",thread.isAlive());
        }
    }
}
