package com.shatteredpixel.shatteredpixeldungeon.apd.query;

/** Transient presentation state. Never changes queries, saved views, or history. */
public final class TableViewport {
    private final String anchor, sorting;
    private final float offset, x, y;

    private TableViewport(String anchor, String sorting, float offset, float x, float y) {
        this.anchor=anchor; this.sorting=sorting; this.offset=offset; this.x=x; this.y=y;
    }

    private static String sorting(ResultTable table) {
        StringBuilder s=new StringBuilder();
        for(ResultTable.Header h:table.headers) if(h.sortDirection!=0)
            s.append(h.id).append(':').append(h.sortDirection).append(':').append(h.sortRank).append(';');
        return s.toString();
    }

    public static TableViewport capture(ResultTable table, float x, float y) {
        if(table==null)return null;
        TableGeometry g=TableGeometry.of(table);
        String anchor=""; float offset=0;
        for(int i=1;i<table.headers.size();i++) {
            float left=g.lefts[i]-g.firstWidth();
            if(x>=left && x<left+g.widths[i]) {anchor=table.headers.get(i).id;offset=x-left;break;}
        }
        return new TableViewport(anchor,sorting(table),offset,Math.max(0,x),Math.max(0,y));
    }

    /** Width and height are the scrolling body, excluding frozen header/column. */
    public float[] position(ResultTable table, float width, float height) {
        TableGeometry g=TableGeometry.of(table);
        float sx=x;
        for(int i=1;i<table.headers.size();i++) if(table.headers.get(i).id.equals(anchor)) {
            sx=g.lefts[i]-g.firstWidth()+Math.min(offset,Math.max(0,g.widths[i]-1));break;
        }
        boolean changed=!sorting.equals(sorting(table));
        if(changed) {
            for(int i=1;i<table.headers.size();i++) if(table.headers.get(i).sortRank==1) {
                float left=g.lefts[i]-g.firstWidth(), right=left+g.widths[i];
                if(left<sx)sx=left;
                else if(right>sx+width)sx=g.widths[i]>width?left:right-width;
                break;
            }
        }
        return new float[]{clamp(sx,g.scrollingWidth()-width),
                clamp(changed?0:y,g.bodyHeight()-height)};
    }

    private static float clamp(float value,float max) {return Math.max(0,Math.min(value,Math.max(0,max)));}

    public static String heading(ResultTable.Header h) {
        // Put the indicator first so long names cannot truncate it away.
        return (h.sortDirection==0?"":h.sortDirection>0?"^ ":"v ")+h.label
                +(h.sortRank>1?" ("+h.sortRank+")":"");
    }
}
