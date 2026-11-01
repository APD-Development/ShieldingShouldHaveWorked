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

package com.shatteredpixel.shatteredpixeldungeon.apd.diagnostics;

import com.shatteredpixel.shatteredpixeldungeon.apd.archive.ApdFiles;
import com.watabou.utils.FileUtils;
import com.watabou.utils.SupportDiagnostics;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DiagnosticReport {
    private DiagnosticReport(){}
    public interface Listener { void finished(String message); }
    public static void export(Listener listener){
        SupportDiagnostics.event("report.requested","");
        SimpleDateFormat date=new SimpleDateFormat("yyyyMMdd-HHmmss",Locale.ROOT);date.setTimeZone(TimeZone.getTimeZone("UTC"));
        String name="APD-diagnostic-"+date.format(new Date())+"-UTC.txt";
        ApdFiles.provider(FileUtils.getFileHandle("apd-diagnostics").file()).chooseExport(name,"text/plain",new ApdFiles.ExportTarget(){
            public void onStream(OutputStream stream,String displayName){
                Thread worker=new Thread(()->{
                    String result;
                    try(OutputStream out=stream){
                        out.write(SupportDiagnostics.report().getBytes(StandardCharsets.UTF_8));
                        result="Diagnostic report saved. You can attach this file to your bug report.\n\n"+displayName;
                    }catch(IOException|RuntimeException error){result="The diagnostic report could not be saved: "+error.getClass().getSimpleName();}
                    catch(OutOfMemoryError exhausted){result="The diagnostic report could not be saved because memory was exhausted during export.";}
                    listener.finished(result);
                },"apd-report-export");worker.setDaemon(true);worker.start();
            }
            public void onCancelled(){listener.finished("Diagnostic report cancelled. No report was saved.");}
            public void onFailed(String why){listener.finished("The diagnostic report could not be opened for saving: "+why);}
        });
    }
}
