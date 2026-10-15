from pathlib import Path
import subprocess,json,sys,time,shutil
r=Path('research/runtime').resolve()
cp=':'.join([str(r)]+[str(p) for p in r.glob('*.jar')])
subprocess.run(['java','com.sun.tools.javac.Main','-cp',cp,'-d',str(r),'research/GetFuckedRouteTest.java'],check=True)
outpath=Path(sys.argv[1])
with outpath.open('w') as out:
 result=subprocess.run(['java']+sys.argv[2:]+['-Droute.noSnapshots=true','-javaagent:'+str(r/'byte-buddy-agent-1.15.4.jar'),'-cp',cp,'org.junit.runner.JUnitCore','com.shatteredpixel.shatteredpixeldungeon.actors.hero.GetFuckedRouteTest'],cwd=r/'assets',stdout=out,stderr=subprocess.STDOUT)
archive=Path('research/trials');archive.mkdir(exist_ok=True);trial=str(time.time_ns());shutil.copyfile(outpath,archive/(trial+'.log'));shutil.copyfile('research/GetFuckedRouteTest.java',archive/(trial+'.java'));print('return',result.returncode)
lines=outpath.read_text().splitlines()
for x in [x for x in lines if x.startswith('ROUTE ')][-5:]:
 j=json.loads(x[6:]); print({k:j[k] for k in ['command','pos','HP','time','totalTime','tomeCharge','partialCharge','level','exp','hunger']},[(m['class'],m['id'],m['pos'],m['HP']) for m in j['mobs'] if m['visible']])
for x in lines:
 if not x.startswith(('ROUTE ','SNAPSHOT','RNG','LAYOUT','FLOOR_LAYOUT')):print(x)

sys.exit(result.returncode)
