package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;
import com.watabou.utils.Bundle;
/** Read-only score display from the ranking's own immutable snapshot. Never current preferences. */
public final class CustomScorePresentation {
 private CustomScorePresentation(){}
 private static CustomDungeonRules recorded(Bundle data){
  if(data==null || !data.contains("apd_custom_rules"))return null;
  return CustomDungeonRules.restore(data.getBundle("apd_custom_rules"));
 }
 public static String multiplier(Bundle data){
  try{CustomDungeonRules r=recorded(data);CustomScoreSnapshot scoring=data==null?null:CustomScoreSnapshot.restore(data,r);return scoring==null?"?":scoring.status==CustomScoreSnapshot.Status.NEUTRAL_IDENTITY?"1.00×":"Pending";}
  catch(IllegalArgumentException invalid){return "?";}
 }
 public static Long adjustedIdentityScore(Bundle data,long original){
  try{CustomDungeonRules r=recorded(data);CustomScoreSnapshot scoring=data==null?null:CustomScoreSnapshot.restore(data,r);return scoring==null?null:scoring.adjustedIdentityScore(original);}
  catch(IllegalArgumentException invalid){return null;}
 }
 public static String notice(Bundle data){
  try{CustomDungeonRules r=recorded(data);if(r==null)return "Custom settings were not recorded for this run.";CustomScoreSnapshot scoring=CustomScoreSnapshot.restore(data,r);return scoring==null?"Custom scoring metadata was not recorded for this run.":scoring.status==CustomScoreSnapshot.Status.NEUTRAL_IDENTITY?"":"Custom scoring pending. Original score unchanged.";}
  catch(IllegalArgumentException invalid){return "Custom settings could not be read. Original score unchanged.";}
 }
}
