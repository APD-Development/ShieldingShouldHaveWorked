/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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


package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.apd.history.ApdHistory;
import com.shatteredpixel.shatteredpixeldungeon.apd.history.ApdNews;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUimsdText;
import com.watabou.noosa.*;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;
import java.util.*;

public class APDTextScene extends PixelScene {
 public enum Page {NEWS,CHANGES}
 public static Page page=Page.NEWS;
 public static Class<? extends PixelScene> backTo=AboutScene.class;
 private static int buildSelected,newsPage;
 private final List<ChangeInfo> changes=new ArrayList<>();
 private String read(String path){try{return Gdx.files.internal(path).readString("UTF-8");}catch(Exception e){return "";}}
 @Override public void create(){
  super.create();
  int w=Camera.main.width,h=Camera.main.height;
  RectF insets=getCommonInsets();
  add(new TitleBackground(w,h));add(new ColorBlock(w,h,0x44000000));
  RenderedTextBlock title=renderTextBlock(page==Page.NEWS?"APD News":"APD Changes",12);
  title.hardlight(Window.TITLE_COLOR);title.setPos((w-title.width())/2,insets.top+6);add(title);
  final int paneWidth=(int)Math.min(220,w-insets.left-insets.right-12);
  final float left=insets.left+(w-insets.left-insets.right-paneWidth)/2f;
  ScrollPane pane=new ScrollPane(new Component()){
   @Override public void onClick(float x,float y){for(ChangeInfo change:changes)if(change.onClick(x,y))return;}
  };
  com.watabou.noosa.NinePatch panel=Chrome.get(Chrome.Type.TOAST);
  float paneTop=title.bottom()+6;
  float paneHeight=Math.max(20,h-insets.bottom-28-paneTop);
  panel.size(paneWidth+panel.marginHor(),paneHeight+panel.marginVer());
  panel.x=left-panel.marginLeft();panel.y=paneTop-panel.marginTop();
  add(panel);add(pane);Component content=pane.content();float y=0;
  int total=0;
  if(page==Page.CHANGES){
   List<ApdHistory.Build> builds=ApdHistory.builds(read("apd/history/index.tsv"));
   total=builds.size();buildSelected=ApdHistory.clamp(buildSelected,total);
   if(total>0){
    ApdHistory.Build build=builds.get(buildSelected);
    ChangeInfo heading=new ChangeInfo(build.label,true,build.date+"\n"+build.status);
    heading.hardlight(Window.TITLE_COLOR);heading.setRect(0,y,paneWidth,0);content.add(heading);changes.add(heading);y=heading.bottom();
    List<ApdHistory.Entry> entries=ApdHistory.entries(read("apd/history/"+build.id+".tsv"));
    if(entries.isEmpty())entries=ApdHistory.entries("History status\tV061_PAGE\tIncomplete history\tVerified changes for this build have not yet been reconstructed.");
    for(ApdHistory.Entry entry:entries){
     ChangeInfo info=new ChangeInfo(entry.category,false,null);
     info.addButton(changeButton(entry));
     info.setRect(0,y,paneWidth,0);content.add(info);changes.add(info);y=info.bottom();
    }
   }
  }else{
   List<ApdNews.Article> articles=ApdNews.published(read("apd/news.tsv"));
   total=(articles.size()+ApdHistory.PAGE_SIZE-1)/ApdHistory.PAGE_SIZE;
   newsPage=ApdHistory.clamp(newsPage,total);
   for(ApdNews.Article article:ApdNews.page(articles,newsPage)){
    NewsCard card=new NewsCard(article);
    card.setRect(0,y,paneWidth,36);content.add(card);y=card.bottom()+3;
   }
  }
  if(total==0){RenderedTextBlock empty=renderTextBlock("No verified entries available.",6);empty.maxWidth(paneWidth);content.add(empty);y=empty.height();}
  content.setSize(paneWidth,y+4);
  pane.setRect(left,title.bottom()+6,paneWidth,Math.max(20,h-insets.bottom-28-title.bottom()-6));
  final int count=total;
  RedButton older=new RedButton("Older",6){@Override protected void onClick(){if(page==Page.CHANGES)buildSelected++;else newsPage++;ShatteredPixelDungeon.seamlessResetScene();}};
  RedButton newer=new RedButton("Newer",6){@Override protected void onClick(){if(page==Page.CHANGES)buildSelected--;else newsPage--;ShatteredPixelDungeon.seamlessResetScene();}};
  int selected=page==Page.CHANGES?buildSelected:newsPage;
  older.enable(selected+1<count);newer.enable(selected>0);
  older.setRect(left,h-insets.bottom-22,(paneWidth-2)/2f,20);newer.setRect(older.right()+2,older.top(),older.width(),20);add(older);add(newer);
  ExitButton exit=new ExitButton(){@Override protected void onClick(){onBackPressed();}};
  exit.setPos(w-insets.right-exit.width(),insets.top);add(exit);
 }
 private class NewsCard extends StyledButton {
  private final ApdNews.Article article;
  private BitmapText date;
  NewsCard(ApdNews.Article article){
   super(Chrome.Type.GREY_BUTTON_TR,article.title,6);this.article=article;
   multiline=true;icon(APDTextScene.this.icon(article.sprite));
   date=new BitmapText(article.displayDate(),pixelFont);date.scale.set(PixelScene.align(0.5f));
   date.hardlight(0x888888);date.measure();add(date);
  }
  @Override protected void layout(){
   super.layout();
   icon.x=x+bg.marginLeft()+(16-icon.width())/2f;PixelScene.align(icon);
   text.maxWidth((int)Math.max(20,width-bg.marginHor()-20));
   text.setPos(x+bg.marginLeft()+18,y+bg.marginTop()+2);
   if(date!=null){date.x=x+width-bg.marginRight()-date.width()+1;date.y=y+height-bg.marginBottom()-date.height()+2;align(date);}
  }
  @Override protected void onClick(){
   APDTextScene.this.addToFront(new com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage(
    APDTextScene.this.icon(article.sprite),article.title,article.displayDate()+"\n\n"+article.text));
  }
 }
 private Image icon(String sprite){
  try{return ChangeIcons.valueOf(sprite).get();}catch(IllegalArgumentException e){return Icons.CHANGES.get();}
 }
 private ChangeButton changeButton(ApdHistory.Entry entry){
  return new ChangeButton(icon(entry.sprite),entry.title,entry.text);
 }
 @Override protected void onBackPressed(){ShatteredPixelDungeon.switchScene(backTo);}
}

