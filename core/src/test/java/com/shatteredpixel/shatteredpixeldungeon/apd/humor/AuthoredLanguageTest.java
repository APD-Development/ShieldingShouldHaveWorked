package com.shatteredpixel.shatteredpixeldungeon.apd.humor;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.apd.HeadlessTestSupport;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthoredLanguageTest {
 @Test public void everyRegisteredAdultTemplateHasAnUnchangedAdultAndCleanAlternative(){
  assertTrue(AuthoredLanguage.alternatives().size()>=25);
  Pattern formats=Pattern.compile("%(?:[0-9]+\\$)?[-#+ 0,(]*[0-9]*(?:\\.[0-9]+)?[a-zA-Z%]");
  for(Map.Entry<String,String> pair:AuthoredLanguage.alternatives().entrySet()){
   assertSame(pair.getKey(),AuthoredLanguage.select(pair.getKey(),true));
   assertEquals(pair.getValue(),AuthoredLanguage.select(pair.getKey(),false));
   List<String> before=new ArrayList<>(),after=new ArrayList<>();
   Matcher m=formats.matcher(pair.getKey());while(m.find())before.add(m.group());
   m=formats.matcher(pair.getValue());while(m.find())after.add(m.group());assertEquals(before,after);
  }
 }
 @Test public void everyEnglishMessageHasAProfanityFreeOffVariantAndKeepsAdultText()throws Exception{
  int files=0,values=0;
  try(java.util.stream.Stream<Path> paths=Files.walk(Paths.get("messages"))){
   for(Path p:(Iterable<Path>)paths.filter(x->x.toString().endsWith(".properties")&&!x.getFileName().toString().contains("_"))::iterator){
    Properties props=new Properties();try(Reader r=Files.newBufferedReader(p,StandardCharsets.UTF_8)){props.load(r);}files++;
    for(String key:props.stringPropertyNames()){
     String value=props.getProperty(key);values++;
     assertFalse(p+":"+key,AuthoredLanguage.containsEnglishProfanity(AuthoredLanguage.english(value,false)));
     assertSame(value,AuthoredLanguage.english(value,true));
    }
   }
  }
  assertEquals(9,files);assertTrue(values>5000);
 }
 @Test public void localizedAlternativesDoNotDamageInnocentWordsOrOtherLanguages(){
  for(String innocent:new String[]{"Hell","slut","dictionary","classic","Scunthorpe","Prick","Fan of Blades"})assertEquals(innocent,AuthoredLanguage.select(innocent,false));
  for(String text:AuthoredLanguage.alternatives().values())assertFalse(AuthoredLanguage.containsEnglishProfanity(text));
  assertEquals("SERIOUSLY",AuthoredLanguage.english("FUCK",false));
  assertEquals("FUCK",AuthoredLanguage.english("FUCK",true));
 }
 @Test public void actualMessageLookupRespondsToPreferenceAndLeavesUserArgumentsUntouched(){
  HeadlessTestSupport.start();boolean saved=SPDSettings.apdAdultHumor();Languages language=Messages.lang();
  try{
   Messages.setup(Languages.ENGLISH);
   SPDSettings.apdAdultHumor(false);
   assertEquals("CLERIC, THAT'S WHY",Messages.get("badges$badge.apd_fuck_cleric.title"));
   assertFalse(Messages.get("journal.document.prison_warden.no_support.body").contains("damned"));
   String playerText="My fucking seed and raw technical shit";
   assertEquals(playerText,Messages.format("%s",playerText));
   SPDSettings.apdAdultHumor(true);
   assertEquals("FUCK CLERIC, THAT'S WHY",Messages.get("badges$badge.apd_fuck_cleric.title"));
   SPDSettings.apdAdultHumor(false);
   Messages.setup(Languages.ENGLISH);
   assertEquals("You tried to play Cleric 10 times. Take the hint.",Messages.get("badges$badge.apd_fuck_cleric.desc"));
  }finally{SPDSettings.apdAdultHumor(saved);Messages.setup(language);}
 }
 @Test public void allFiftyLoreEntriesKeepCleanAndAdultVariantsWithoutChangingContext(){
  List<Line> lines=HumorTestSupport.shipped();assertEquals(50,lines.size());
  for(Line line:lines){assertFalse(line.id,AuthoredLanguage.containsEnglishProfanity(line.clean));assertEquals(line.clean,AuthoredLanguage.english(line.clean,false));}
 }
 @Test public void newHardcodedAdultProseCannotBypassReviewedCleanBranches()throws Exception{
  Path root=Paths.get(System.getProperty("apd.game.src"));int reviewed=0;
  Pattern strings=Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");
  try(java.util.stream.Stream<Path> sources=Files.walk(root)){
   for(Path p:(Iterable<Path>)sources.filter(x->x.toString().endsWith(".java"))::iterator){
    String relative=root.relativize(p).toString().replace('\\','/');
    if(relative.equals("apd/humor/AuthoredLanguage.java")||relative.equals("apd/humor/LinePack.java"))continue;
    Matcher literals=strings.matcher(Files.readString(p));
    while(literals.find())if(AuthoredLanguage.containsEnglishProfanity(literals.group())){
     assertEquals("New developer prose needs a reviewed clean branch: "+relative,"scenes/AboutScene.java",relative);reviewed++;
    }
   }
  }
  assertEquals("Both original adult About statements are retained",2,reviewed);
  String about=Files.readString(root.resolve("scenes/AboutScene.java"));
  assertTrue(about.contains("Founded on the principle that shielding should have worked."));
  assertTrue(about.contains("APD began with the shielding mechanic: shielding should have worked."));
 }
}
