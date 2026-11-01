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
import android.util.Log;
import com.badlogic.gdx.*;
import com.watabou.input.BackGate;
import com.watabou.input.BackTrace;
import com.watabou.input.KeyBindings;
import com.watabou.noosa.Game;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Passive observation around the unchanged failing native fixture; no app rebuild. */
@RunWith(AndroidJUnit4.class)
public class BackRoutingDiagnosticDeviceTest {
    @Test public void traceRawBackAndActionRoutingThroughTheExistingNativeRegression() throws Exception {
        AtomicBoolean running=new AtomicBoolean(true);
        BackTrace.setEnabled(true);
        InputAdapter observer=new InputAdapter(){
            private void record(String edge){
                String scene=Game.scene()==null?"none":Game.scene().getClass().getSimpleName();
                String action=String.valueOf(KeyBindings.getActionForKey(new com.watabou.input.KeyEvent(Input.Keys.BACK,true)));
                Log.w("APDBACKDIAG",edge+" scene="+scene+" active="+(Game.scene()!=null&&Game.scene().isActive())
                    +" registered="+BackGate.systemBackOwned()+" action="+action
                    +" isBack="+(KeyBindings.getActionForKey(new com.watabou.input.KeyEvent(Input.Keys.BACK,true))==com.watabou.input.GameAction.BACK)
                    +" caught="+Gdx.input.isCatchKey(Input.Keys.BACK)+" event_ns="+Gdx.input.getCurrentEventTime());
            }
            @Override public boolean keyDown(int code){if(code==Input.Keys.BACK)record("raw-down");return false;}
            @Override public boolean keyUp(int code){if(code==Input.Keys.BACK)record("raw-up");return false;}
        };
        Thread watcher=new Thread(()->{
            InputProcessor last=null;
            Object lastScene=null;
            while(running.get()){
                if(Game.instance!=null){
                    Object scene=Game.scene();
                    if(scene!=lastScene){lastScene=scene;Log.w("APDBACKDIAG","scene-now="+(scene==null?"none":scene.getClass().getSimpleName()));}
                }
                Input source=Gdx.input;
                InputProcessor processor=source==null?null:source.getInputProcessor();
                if(processor instanceof InputMultiplexer&&processor!=last){
                    last=processor;
                    Game.runOnRenderThread(()->{
                        if(running.get()&&source.getInputProcessor()==processor){
                            ((InputMultiplexer)processor).addProcessor(0,observer);
                            Log.w("APDBACKDIAG","observer-attached caught="+source.isCatchKey(Input.Keys.BACK));
                        }
                    });
                }
                try{Thread.sleep(25);}catch(InterruptedException done){return;}
            }
        },"APD-Back-diagnostic");
        watcher.start();
        try{
            new TitleMenuLayoutDeviceTest().titleControlsStaySeparateAndNativeChangesAndHideUiRemainUsable();
        }finally{
            running.set(false);watcher.interrupt();watcher.join(3000);
            Log.w("APDBACKDIAG","DIAGNOSTIC_ONLY: original assertions preserved; observer never consumes input");
            for(String event:BackTrace.snapshot())Log.w("APDBACKDIAG",event);
            for(String event:com.watabou.utils.SupportDiagnostics.report("Native Back diagnosis").split("\\n"))Log.w("APDBACKDIAG",event);
            BackTrace.setEnabled(false);
        }
    }
}
