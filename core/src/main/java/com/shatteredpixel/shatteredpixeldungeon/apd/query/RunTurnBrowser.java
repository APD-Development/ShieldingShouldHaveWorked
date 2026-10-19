package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKeys;
import java.util.*;

/** Read-only chronological join of public UIMSD events in one recorded run.
 * Imported ranking-only records have no turn evidence; no events are synthesized. */
public final class RunTurnBrowser {
    public final String runId;
    public final long lastTurn;
    private long current = 1;
    private long step = 1;
    private final NavigableMap<Long,List<String>> timeline = new TreeMap<>();

    public RunTurnBrowser(Catalog catalog, Row run) {
        if (catalog == null || run == null || run.runId == null || run.runId.isEmpty() )
            throw new IllegalArgumentException("A recorded APD run is required");
        runId = run.runId;
        long max = 1;
        Dataset runs = catalog.dataset(Datasets.RUNS);
        if (runs != null) {
            int ti = runs.def.indexOf(FactKeys.TURNS);
            if (ti >= 0 && run.isKnown(ti)) max = Math.max(1,run.num(ti));
        }
        for (Dataset ds : catalog.datasets()) {
            String id = ds.def.id;
            if (!EventDatasets.isEventDataset(id) && !FactDatasets.isFactDataset(id)) continue;
            if (EventDatasets.EQUIPMENT.equals(id) || EventDatasets.FLOORS.equals(id)) continue;
            String clockName = FactDatasets.isFactDataset(id) ? FactDatasets.TURN : EventDatasets.prefixOf(id)+".turn";
            int ti = ds.def.indexOf(clockName);
            if (ti < 0) continue;
            for (Row r : ds.rows) {
                if (!runId.equals(r.runId) || !r.publicRow || !r.isKnown(ti)) continue;
                long t = r.num(ti);
                if (t < 1) continue;
                max = Math.max(max,t);
                StringBuilder line = new StringBuilder();
                if (FactDatasets.isFactDataset(id)) {
                    com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKind kind =
                        com.shatteredpixel.shatteredpixeldungeon.apd.stats.FactKinds.get(id.substring(FactDatasets.PREFIX.length()));
                    line.append(kind == null ? "Event" : kind.label);
                } else line.append(ds.def.title);
                int count=0;
                for (ColumnDef c : ds.def.columns()) {
                    if (c.isInternal() || !c.defaultVisible || c.index()==ti || !r.isKnown(c.index())) continue;
                    if (c.id.endsWith(".runClass") || c.id.endsWith(".runDifficulty")) continue;
                    line.append(count++==0 ? ": " : ", ").append(c.label).append(" ")
                        .append(CellFormatter.cellOpen(r,c).text);
                    if(count>=5)break;
                }
                List<String> at = timeline.get(t);
                if (at == null) timeline.put(t,at=new ArrayList<>());
                at.add(line.toString());
            }
        }
        lastTurn = max;
    }

    public long turn(){return current;}
    public long step(){return step;}
    public void step(long amount){if(amount<1)throw new IllegalArgumentException("step");step=amount;}
    public void go(long t){current=Math.max(1,Math.min(lastTurn,t));}
    public void next(){current = current>=lastTurn-step ? lastTurn : current+step;}
    public void previous(){current = current<=step ? 1 : current-step;}
    public List<String> events(){List<String> v=timeline.get(current);return v==null?Collections.emptyList():Collections.unmodifiableList(v);}
    public int recordedTurns(){return timeline.size();}
    public String summary(){
        StringBuilder out=new StringBuilder("Turn ").append(current).append(" / ").append(lastTurn)
          .append("\nStep: ").append(step).append(" turn(s)\n");
        List<String> items=events();
        if(items.isEmpty()) return out.append("\nNo recorded event for this turn.\nOlder runs may have missing details.").toString();
        for(String item:items) out.append("\n- ").append(item);
        return out.toString();
    }
}
