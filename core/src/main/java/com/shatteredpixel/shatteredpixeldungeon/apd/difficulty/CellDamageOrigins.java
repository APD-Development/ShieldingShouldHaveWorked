package com.shatteredpixel.shatteredpixeldungeon.apd.difficulty;

import com.watabou.utils.Bundle;
/** Causal density only: never changes blob density, diffusion, turns or RNG. */
public final class CellDamageOrigins {
 private static final OffensiveDamageOrigin[] ROLES={OffensiveDamageOrigin.UNATTRIBUTED,OffensiveDamageOrigin.PLAYER,OffensiveDamageOrigin.ENEMY,OffensiveDamageOrigin.BOSS};
 private double[][] mass,scratch;
 private boolean pending,pendingKnown;
 private static int index(OffensiveDamageOrigin role){for(int i=0;i<ROLES.length;i++)if(ROLES[i]==role)return i;throw new IllegalArgumentException("Missing cell origin");}
 private void initialize(int[] density,int length){mass=new double[4][length];if(density!=null)for(int i=0;i<length;i++)mass[0][i]=density[i];}
 public void seed(int[] density,int length,int cell,int amount,OffensiveDamageOrigin role){
  int slot=index(role);if(amount<=0)return;
  if(mass==null){if(slot==0)return;initialize(density,length);}
  synchronizeCell(density==null?0:density[cell],cell);
  mass[slot][cell]+=amount;
 }
 public int scale(int amount,int cell){
  if(amount<=0||mass==null)return amount;double total=0,weighted=0;boolean neutral=true;
  for(int r=0;r<4;r++){double contribution=mass[r][cell];int percentage=ROLES[r].percentage();total+=contribution;weighted+=contribution*percentage;if(contribution>0&&percentage!=100)neutral=false;}
  return neutral||total==0?amount:(int)Math.min(Integer.MAX_VALUE,Math.round(amount*weighted/(100*total)));
 }
 public void synchronize(int[] density){if(mass!=null&&density!=null)for(int i=0;i<density.length;i++)synchronizeCell(density[i],i);}
 private void synchronizeCell(int density,int cell){
  double total=0;for(int r=0;r<4;r++)total+=mass[r][cell];
  if(density<=0){clear(cell);return;}
  if(total==0){mass[0][cell]=density;return;}
  if(total<density-1e-8)mass[0][cell]+=density-total;
  else if(total>density+1e-8)for(int r=0;r<4;r++)mass[r][cell]*=density/total;
 }
 public void clear(int cell){if(mass!=null)for(int r=0;r<4;r++)mass[r][cell]=0;}
 public void clearAll(){mass=null;scratch=null;pendingKnown=false;}
 /** Apply original output density to the proportions of the original nonblocking neighborhood. */
 public void prepareDiffusion(int[] density,int[] output,boolean[] blocking,int width){
  pending=true;pendingKnown=false;if(mass==null)return;synchronize(density);
  if(scratch==null)scratch=new double[4][output.length];else for(int r=0;r<4;r++)java.util.Arrays.fill(scratch[r],0);
  double[][] next=scratch;double[] sums=new double[4];boolean known=false;
  for(int cell=0;cell<output.length;cell++){
   if(output[cell]<=0||blocking[cell])continue;java.util.Arrays.fill(sums,0);
   add(sums,cell,density);
   if(cell%width>0&&!blocking[cell-1])add(sums,cell-1,density);
   if(cell%width<width-1&&!blocking[cell+1])add(sums,cell+1,density);
   if(cell>=width&&!blocking[cell-width])add(sums,cell-width,density);
   if(cell+width<density.length&&!blocking[cell+width])add(sums,cell+width,density);
   double total=0;for(double sum:sums)total+=sum;
   if(total==0)next[0][cell]=output[cell];else for(int r=0;r<4;r++){next[r][cell]=sums[r]*output[cell]/total;if(r>0&&next[r][cell]>0)known=true;}
  }
  pendingKnown=known;
 }
 public void finishDiffusion(){if(!pending)return;if(pendingKnown){double[][] before=mass;mass=scratch;scratch=before;}else clearAll();pending=false;}
 private void add(double[] sums,int cell,int[] density){if(density[cell]>0)for(int r=0;r<4;r++)sums[r]+=mass[r][cell];}
 public static void clearMetadata(Bundle b,String key){b.remove(key+"_version");b.remove(key+"_cells");b.remove(key+"_mass");}
 public void store(Bundle b,String key,int[] density){
  clearMetadata(b,key);if(mass==null||density==null)return;synchronize(density);int count=0;
  for(int i=0;i<density.length;i++)if(density[i]>0&&(mass[1][i]>0||mass[2][i]>0||mass[3][i]>0))count++;
  if(count==0)return;int[] cells=new int[count];String[] values=new String[count*4];int at=0;
  for(int i=0;i<density.length;i++)if(density[i]>0&&(mass[1][i]>0||mass[2][i]>0||mass[3][i]>0)){cells[at]=i;for(int r=0;r<4;r++)values[at*4+r]=Double.toString(mass[r][i]);at++;}
  b.put(key+"_version",1);b.put(key+"_cells",cells);b.put(key+"_mass",values);
 }
 public static CellDamageOrigins restore(Bundle b,String key,int[] density){
  CellDamageOrigins result=new CellDamageOrigins();boolean version=b.contains(key+"_version"),cells=b.contains(key+"_cells"),values=b.contains(key+"_mass");
  if(!version&&!cells&&!values)return result;
  if(!version||!cells||!values||b.getInt(key+"_version")!=1||density==null)throw new IllegalArgumentException("Unsupported cell provenance; preserve save");
  int[] ids=b.getIntArray(key+"_cells");String[] encoded=b.getStringArray(key+"_mass");if((long)ids.length*4!=encoded.length)throw new IllegalArgumentException("Invalid cell provenance shape");
  result.initialize(density,density.length);boolean[] used=new boolean[density.length];
  for(int i=0;i<ids.length;i++){int cell=ids[i];if(cell<0||cell>=density.length||used[cell]||density[cell]<=0)throw new IllegalArgumentException("Invalid provenance cell");used[cell]=true;double sum=0;
   for(int r=0;r<4;r++){double value=Double.parseDouble(encoded[i*4+r]);if(!Double.isFinite(value)||value<0)throw new IllegalArgumentException("Invalid cell contribution");result.mass[r][cell]=value;sum+=value;}
   if(Math.abs(sum-density[cell])>Math.max(1e-8,density[cell]*1e-9))throw new IllegalArgumentException("Cell provenance differs from density");
  }
  return result;
 }
}
