from pathlib import Path
import subprocess,json,sys,time,shutil,fcntl,tempfile
gate=Path("research/engine-run.lock").open("a")
fcntl.flock(gate.fileno(),fcntl.LOCK_EX)
route_source=Path("research/GetFuckedRouteTest.java").read_bytes()
compile_snapshot=tempfile.TemporaryDirectory(prefix="get-fucked-native-test-")
compile_source=Path(compile_snapshot.name)/"GetFuckedRouteTest.java"
compile_source.write_bytes(route_source)
r=Path('research/runtime').resolve()
cp=':'.join([str(r)]+[str(p) for p in r.glob('*.jar')])
compiled_classes=Path(compile_snapshot.name)/'classes'
compiled_classes.mkdir()
subprocess.run(['java','com.sun.tools.javac.Main','-cp',cp,'-d',str(compiled_classes),str(compile_source)],check=True)
cp=str(compiled_classes)+':'+cp
outpath=Path(sys.argv[1])
with outpath.open('w') as out:
 result=subprocess.run(['java']+sys.argv[2:]+['-Droute.noSnapshots=true','-javaagent:'+str(r/'byte-buddy-agent-1.15.4.jar'),'-cp',cp,'org.junit.runner.JUnitCore','com.shatteredpixel.shatteredpixeldungeon.actors.hero.GetFuckedRouteTest'],cwd=r/'assets',stdout=out,stderr=subprocess.STDOUT)
verification_missing=(result.returncode==0 and any(v.startswith('-Droute.expectedEnd=') for v in sys.argv[2:]) and 'CONNECTED_ENDPOINT_STATE_AND_' not in outpath.read_text()) or (result.returncode==0 and '-Droute.expectedNativeState=true' in sys.argv[2:] and 'CONNECTED_FULL_NATIVE_JSON_AND_RNG_VERIFIED' not in outpath.read_text())
if result.returncode==0 and '-Droute.manualTail=true' in sys.argv[2:] and 'MANUAL_BOUNDARY' not in outpath.read_text():verification_missing=True
if result.returncode==0 and '-Droute.expectedNativeFiles=true' in sys.argv[2:] and 'CONNECTED_ALL_NATIVE_FILES_AND_ACTIVE_RNG_VERIFIED' not in outpath.read_text():verification_missing=True
if verification_missing:
 print('FAIL: requested endpoint/native comparison did not execute')
 result.returncode=1
if result.returncode==0 and '-Droute.expectedMissileIdEquivalence=true' in sys.argv[2:]:
 props={v[2:].split('=',1)[0]:v.split('=',1)[1] for v in sys.argv[2:] if v.startswith('-D') and '=' in v}
 comparison=subprocess.run(['python',str(Path('research/native_equivalence.py').resolve()),props['route.expectedEnd'],props['route.earlySwarmOut']],capture_output=True,text=True)
 with outpath.open('a') as out:out.write(comparison.stdout+comparison.stderr)
 result.returncode=comparison.returncode
archive=Path('research/trials');archive.mkdir(exist_ok=True);trial=str(time.time_ns());shutil.copyfile(outpath,archive/(trial+'.log'));(archive/(trial+'.java')).write_bytes(route_source);(archive/(trial+'.args.json')).write_text(json.dumps({'output':str(outpath),'java_args':sys.argv[2:],'exit_code':result.returncode},indent=2));print('return',result.returncode)
lines=outpath.read_text().splitlines()
for x in [x for x in lines if x.startswith('ROUTE ')][-3:]:
 j=json.loads(x[6:]);print({k:j.get(k) for k in ['command','pos','HP','shield','time','totalTime','tomeCharge','partialCharge','level','exp','hunger']},[(m['class'],m['id'],m['pos'],m['HP']) for m in j['mobs'] if m['visible']])
print('\n'.join(x for x in lines[-12:] if len(x)<300))
sys.exit(result.returncode)
