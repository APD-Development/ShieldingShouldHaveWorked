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
