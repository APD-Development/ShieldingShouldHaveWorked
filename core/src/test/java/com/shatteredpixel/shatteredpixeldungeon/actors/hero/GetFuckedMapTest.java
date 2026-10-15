package com.shatteredpixel.shatteredpixeldungeon.actors.hero;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.apd.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.utils.Bundle;
import org.json.*;
import org.junit.Test;
import java.util.Base64;
/** Read-only complete map inventory. This never acts a hero or claims a played route. */
public class GetFuckedMapTest {
 @Test public void preserveSeed266GeneratedDungeon() throws Exception {
  HeadlessTestSupport.start();
  LevelGenerationHarness.levelObserver=(depth,l)->{
   JSONObject j=new JSONObject().put("depth",depth).put("width",l.width()).put("height",l.height())
     .put("entrance",l.entrance()).put("exit",l.exit()).put("terrain",new JSONArray(l.map));
   JSONArray heaps=new JSONArray(),mobs=new JSONArray(),traps=new JSONArray();
   for(Heap h:l.heaps.valueList()){
    JSONArray items=new JSONArray();
    for(Item i:h.items)items.put(new JSONObject().put("class",i.getClass().getSimpleName()).put("quantity",i.quantity()).put("level",i.level()).put("cursed",i.cursed));
    heaps.put(new JSONObject().put("pos",h.pos).put("type",h.type.toString()).put("items",items));
   }
   for(Mob m:l.mobs)mobs.put(new JSONObject().put("class",m.getClass().getSimpleName()).put("pos",m.pos).put("HP",m.HP).put("id",m.id()).put("state",m.state.getClass().getSimpleName()));
   for(Trap t:l.traps.valueList())traps.put(new JSONObject().put("class",t.getClass().getSimpleName()).put("pos",t.pos).put("active",t.active).put("visible",t.visible));
   j.put("heaps",heaps).put("mobs",mobs).put("traps",traps);
   System.out.println("MAP266 "+j);
   try{
    Bundle b=new Bundle();b.put("level",l);java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
    Bundle.write(b,out,true);
    System.out.println("GENERATED_LEVEL266 "+depth+" "+Base64.getEncoder().encodeToString(out.toByteArray()));
   }catch(Exception e){throw new RuntimeException(e);}
  };
  try{LevelGenerationHarness.generate(266,Challenges.GET_FUCKED,HeroClass.CLERIC,LevelGenerationHarness.Context.REFERENCE,26);}
  finally{LevelGenerationHarness.levelObserver=null;}
 }
}
