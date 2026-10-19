package com.shatteredpixel.shatteredpixeldungeon.apd.query;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Offline UIMSD search over discoverable public table/view metadata.
 *
 * This deliberately indexes NO row values, run secrets or internal columns.
 * A search result merely opens a normal UIMSD view; all data continues through
 * QueryEngine's existing scope, recording-coverage and hidden-info checks.
 */
public final class UimsdSearch {
    private UimsdSearch(){}

    public static final class Hit {
        public final String viewId, title, detail;
        public final int score;
        Hit(ViewDef view, String detail, int score){
            this.viewId=view.id;
            this.title=view.title;
            this.detail=detail;
            this.score=score;
        }
    }

    public static List<Hit> find(Catalog catalog, String question, int limit) {
        if(catalog==null || limit<=0 || question==null)return Collections.emptyList();
        String input=normalize(question);
        if(input.isEmpty())return Collections.emptyList();
        String[] terms=input.split(" +");
        ArrayList<Hit> found=new ArrayList<>();
        for(ViewDef view:catalog.views()){
            Dataset dataset=catalog.dataset(view.dataset());
            if(dataset==null)continue;
            String heading=normalize(view.title);
            String id=normalize(view.id);
            String overview=normalize(view.description+" "+view.group+" "+dataset.def.title+" "+
                    dataset.def.description);
            StringBuilder columnText=new StringBuilder();
            String firstColumn="";
            for(ColumnDef col:dataset.def.columns()) {
                // Hidden column labels and derived secrets aren't advertised by search.
                if(col.isInternal())continue;
                columnText.append(' ').append(col.label).append(' ').append(col.group)
                        .append(' ').append(col.description);
                if(firstColumn.isEmpty())firstColumn=col.label;
            }
            String columns=normalize(columnText.toString());
            String aliases=normalize(aliasesFor(view.id,view.dataset()));
            int rank=0;
            boolean all=true;
            for(String term:terms){
                int score=score(term,heading,id,overview,columns,aliases);
                if(score==0){all=false;break;}
                rank+=score;
            }
            if(!all)continue;
            String detail=view.description==null?"":view.description;
            if(detail.length()>140)detail=detail.substring(0,137)+"...";
            found.add(new Hit(view,detail,rank));
        }
        found.sort(Comparator.comparingInt((Hit h)->h.score).reversed()
                .thenComparing(h->h.title, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(h->h.viewId));
        return Collections.unmodifiableList(new ArrayList<>(found.subList(0,Math.min(limit,found.size()))));
    }

    private static int score(String term,String title,String id,String overview,String cols,String aliases){
        if(title.equals(term)||id.equals(term))return 120;
        if(title.startsWith(term))return 95;
        if(hasWord(title,term)||hasWord(id,term))return 75;
        if(hasWord(aliases,term))return 68;
        if(title.contains(term)||id.contains(term))return 55;
        if(hasWord(overview,term))return 35;
        if(hasWord(cols,term))return 26;
        if(aliases.contains(term)||overview.contains(term))return 20;
        return cols.contains(term)?12:0;
    }

    private static boolean hasWord(String in,String term){
        return (" "+in+" ").contains(" "+term+" ");
    }

    /** Built-in aliases work entirely on device. Unlisted languages still match
     * original Unicode view names, columns, and descriptions, without sending text anywhere.
     */
    private static String aliasesFor(String view,String dataset) {
        String target=(view+" "+dataset).toLowerCase(Locale.ROOT);
        if(target.contains("death")||target.contains("died")||target.contains("outcome"))
            return "deaths died dying mortality muerte muertes morir mort décès mort morti tot tod sterben "
                    +"смерть смерти умер смертью décèses 死亡 死 去世 death";
        if(target.contains("attack")||target.contains("combat")||target.contains("damage"))
            return "attack attacks damage fight fighting attacks combate daño daño ataques "
                    +"attaques degats dégats combat schaden angriffe kampf урон атаки атака бой "
                    +"伤害 攻击 攻擊 戦闘 攻撃 ダメージ 전투 공격 피해";
        if(target.contains("item")||target.contains("weapon")||target.contains("armor")
                ||target.contains("wand")||target.contains("potion")||target.contains("scroll"))
            return "item items gear loot inventory objetos objetos equipo armas ítems "
                    +"objets equipement équipement armes gegenstände ausrüstung waffen "
                    +"предмет вещи оружие 物品 装备 裝備 アイテム 装備 아이템 장비";
        if(target.contains("run")||target.contains("victor")||target.contains("win"))
            return "run runs game games victoire victory win wins carreras partidas partidas "
                    +"parties runs siege sieg победа забег прохождение "
                    +"胜利 游戏 游戏局 記録 勝利 승리";
        if(target.contains("boss")||target.contains("enemy")||target.contains("enemies")||target.contains("kill"))
            return "boss bosses enemy enemies monster monsters killed enemies enemigos jefes "
                    +"ennemis patrons boss gegner feinde босс враги убийство 敌人 首领 首領 ボス 敵 보스 적";
        if(target.contains("floor")||target.contains("step")||target.contains("explor"))
            return "floor floors step steps movement travel movement suelo pisos étage étages "
                    +"etage etagen stockwerke schritte этаж этажи шаги 楼层 步数 階層 歩数 층 걸음";
        return "";
    }

    /** NFKC folds full-width input; accent removal is for Latin text only.
     * All other scripts remain intact, including Chinese, Cyrillic and Korean.
     */
    public static String normalize(String text) {
        if(text==null)return "";
        String compatible=Normalizer.normalize(text,Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        String decomposed=Normalizer.normalize(compatible,Normalizer.Form.NFD);
        StringBuilder sb=new StringBuilder();
        boolean prevSpace=true;
        for(int i=0;i<decomposed.length();) {
            int cp=decomposed.codePointAt(i);i+=Character.charCount(cp);
            int type=Character.getType(cp);
            if(type==Character.NON_SPACING_MARK)continue;
            if(Character.isLetterOrDigit(cp)){
                sb.appendCodePoint(cp);prevSpace=false;
            }else if(!prevSpace){
                sb.append(' ');prevSpace=true;
            }
        }
        return sb.toString().trim();
    }
}
