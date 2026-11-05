#!/usr/bin/env python3
# ADHD Pixel Dungeon
# Copyright (C) 2026 Chris Charlton
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>

"""Deliver one already-tested private APK pair and exact key-free shared source. Never rebuild."""
import datetime,hashlib,io,json,os,pathlib,re,subprocess,tarfile,time,zipfile
from apd_existing_build_evidence import application_inputs,certificates,xml_summary
from PIL import Image
ROOT=pathlib.Path.cwd();REPO=os.environ['GITHUB_REPOSITORY'];OUT=ROOT/'owner-delivery';OUT.mkdir(exist_ok=True)
def api(path):return json.loads(subprocess.check_output(['gh','api',path]))
def cmd(*args):return subprocess.check_output(args,text=True)
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def artifact(run,name):
 records=api(f'repos/{REPO}/actions/runs/{run}/artifacts')['artifacts'];found=[a for a in records if a['name']==name and not a['expired']];assert found,(run,name)
 a=max(found,key=lambda a:(a['created_at'],a['id']));z=ROOT/(str(a['id'])+'.zip')
 with z.open('wb') as f:subprocess.run(['gh','api',f'repos/{REPO}/actions/artifacts/{a["id"]}/zip'],stdout=f,check=True)
 assert a['digest']=='sha256:'+sha(z)
 folder=ROOT/('artifact-'+str(a['id']));folder.mkdir(exist_ok=True)
 with zipfile.ZipFile(z) as archive:archive.extractall(folder)
 return folder,a
spec=json.loads(pathlib.Path('docs/APD-CORRECTIVE-PAIR-DELIVERY.json').read_text());source=spec['source'];number=spec['number'];tag=f'apd-alpha-preview-{number}'
assert api('repos/'+REPO)['private']
deadline=time.monotonic()+1500
for field in ('developer','player','display','tester','native'):
 while True:
  record=api(f'repos/{REPO}/actions/runs/{spec[field]}')
  assert record['head_sha']==(spec['tester_test_source'] if field=='tester' else spec.get('native_test_source',source) if field=='native' else source)
  if record['status']=='completed':
   assert record['conclusion']=='success' or record['conclusion']=='failure' and (field=='developer' and spec['native']!=spec['developer'] or field=='display' and spec.get('profile_overrides')),(field,record['conclusion']);break
  assert time.monotonic()<deadline,'Verification still pending; publication remains blocked'
  time.sleep(15)
producer=api(f'repos/{REPO}/actions/runs/{spec['developer']}')
assert producer['run_number']==number
producer_jobs=api(f'repos/{REPO}/actions/runs/{spec['developer']}/jobs')['jobs']
assert any(j['name']=='verify-and-build' and j['conclusion']=='success' for j in producer_jobs)
native_jobs=producer_jobs if spec['native']==spec['developer'] else api(f'repos/{REPO}/actions/runs/{spec["native"]}/jobs')['jobs']
native_job,=[j for j in native_jobs if j['name']==spec.get('native_job_name','android-smoke-check') and j['conclusion']=='success']
if producer['conclusion']=='failure':
 original_job,=[j for j in producer_jobs if j['name']=='android-smoke-check' and j['conclusion']=='failure']
 original_log=subprocess.check_output(['gh','api','--allow-escape-sequences',f'repos/{REPO}/actions/jobs/{original_job["id"]}/logs']).decode()
 assert 'expected:<-1> but was:<0>' in original_log
 assert re.search(r'Tests run:\s*71,\s*Failures:\s*1',original_log)
 (OUT/'API34-original-stale-test-expectation.txt').write_text(original_log)
# Delivery tooling/docs may change; application/build inputs must remain exact.
trees=[api(f'repos/{REPO}/git/trees/{s}?recursive=1') for s in (source,os.environ['GITHUB_SHA'])];assert all(not x.get('truncated') for x in trees)
assert application_inputs(trees[0]['tree'])==application_inputs(trees[1]['tree'])
if spec.get('native_test_source'):
 test_tree=api(f'repos/{REPO}/git/trees/{spec["native_test_source"]}?recursive=1')
 assert not test_tree.get('truncated') and application_inputs(test_tree['tree'])==application_inputs(trees[0]['tree'])
dev,da=artifact(spec['developer'],'apd-codex-private-test-apk');player,pa=artifact(spec['player'],'apd-player-matching-developer');core,ca=artifact(spec['developer'],'apd-codex-core-tests');native,na=artifact(spec['native'],spec.get('native_artifact','apd-codex-android-checks'))
if spec['native']!=spec['developer']:
 proofs=list(native.rglob('acceptance-source.json'));assert proofs
 native_proof=json.loads(proofs[0].read_text());assert all(json.loads(f.read_text())==native_proof for f in proofs)
 assert native_proof['application_source']==source and native_proof['application_run']==spec['developer'] and native_proof['application_rebuilt'] is False
 assert native_proof['test_source']==spec['native_test_source'] and native_proof['application_sha256']==sha(next(dev.rglob('*.apk')))
# Require every terminal instrumentation result from the successful full run.
import xml.etree.ElementTree as ET
raw=subprocess.check_output(['gh','api','--allow-escape-sequences',f'repos/{REPO}/actions/jobs/{native_job['id']}/logs']).decode()
def terminal_rows(text):
 clean=re.sub(r'(?m)^\d{4}-\d\d-\d\dT[0-9:.]+Z\s*','',re.sub(r'\x1b\[[0-9;]*m','',text))
 parts=re.split(r'INSTRUMENTATION_STATUS_CODE:\s*(-?\d+)',clean);rows=[]
 for i in range(0,len(parts)-1,2):
  block,code=parts[i],int(parts[i+1]);classes=re.findall(r'(?m)^INSTRUMENTATION_STATUS: class=(.+)$',block);names=re.findall(r'(?m)^INSTRUMENTATION_STATUS: test=(.+)$',block)
  if code!=1 and classes and names:rows.append((classes[-1].strip(),names[-1].strip(),code))
 return rows
rows=terminal_rows(raw);expected_count=spec.get('native_case_count',spec['android_tests'])
assert len(rows)==len({r[:2] for r in rows})==expected_count and all(r[2]==0 for r in rows)
assert re.search(r'OK \('+str(expected_count)+r' tests?\)',raw)
actual=[r[:2] for r in rows];case_sources={r[:2]:spec['native'] for r in rows}
if spec.get('combine_original_native'):
 original_rows=terminal_rows(original_log)
 assert len(original_rows)==len({r[:2] for r in original_rows})==spec['android_tests']
 wanted=('com.shatteredpixel.shatteredpixeldungeon.android.HealingSettingsDeviceTest','actualSettingsChooserSavesThresholdAndGatesRealPotionUse')
 assert [r[:2] for r in original_rows if r[2]!=0]==[wanted] and actual==[wanted]
 passed=[r[:2] for r in original_rows if r[2]==0];actual=passed+actual
 case_sources.update({case:spec['developer'] for case in passed})
assert len(actual)==len(set(actual))==spec['android_tests']
# Convert only the actual terminal console records when adb produced no JUnit file.
if not list(native.rglob('TEST-*.xml')):
 suite=ET.Element('testsuite',name='ActualSameAPKNativeRun',tests=str(len(actual)),failures='0',errors='0',skipped='0')
 for cls,name in actual:
  case=ET.SubElement(suite,'testcase',classname=cls,name=name)
  ET.SubElement(ET.SubElement(case,'properties'),'property',name='evidence_run',value=str(case_sources[(cls,name)]))
 ET.ElementTree(suite).write(native/'TEST-actual-native-run.xml',encoding='utf-8',xml_declaration=True)
np={'application_source':source,'application_run':spec['developer'],'application_rebuilt':False,'application_sha256':sha(next(dev.rglob('*.apk'))),'native_job':native_job['id'],'passed_cases':len(actual),'single_successful_invocation':not spec.get('combine_original_native',False),'case_sources':[{"class":c,"name":n,"run":case_sources[(c,n)]} for c,n in actual]}
(OUT/'API34-successful-job-console.txt').write_text(raw)
results={'core':xml_summary(core),'player_core':xml_summary(player),'android':xml_summary(native)}
assert results['core']['tests']==results['player_core']['tests']==spec['core_tests']
assert results['android']['tests']==spec['android_tests']
assert {(c['classname'],c['name']) for c in results['android']['cases']}==set(actual)
# Every previously passing full-suite case must remain, along with both new end-to-end cases.
baseline_jobs=api(f'repos/{REPO}/actions/runs/41536788270/jobs')['jobs']
baseline_job,=[j for j in baseline_jobs if j['name']=='android-smoke-check' and j['conclusion']=='success']
baseline_log=subprocess.check_output(['gh','api','--allow-escape-sequences',f'repos/{REPO}/actions/jobs/{baseline_job['id']}/logs']).decode()
baseline_clean=re.sub(r'(?m)^\d{4}-\d\d-\d\dT[0-9:.]+Z\s*','',re.sub(r'\x1b\[[0-9;]*m','',baseline_log))
parts=re.split(r'INSTRUMENTATION_STATUS_CODE:\s*(-?\d+)',baseline_clean);prior=[]
for i in range(0,len(parts)-1,2):
 block,code=parts[i],int(parts[i+1]);classes=re.findall(r'(?m)^INSTRUMENTATION_STATUS: class=(.+)$',block);names=re.findall(r'(?m)^INSTRUMENTATION_STATUS: test=(.+)$',block)
 if code!=1 and classes and names:
  assert code==0;prior.append((classes[-1].strip(),names[-1].strip()))
assert len(prior)==len(set(prior))==69
assert set(prior)<={(c['classname'],c['name']) for c in results['android']['cases']}
assert {c['name'] for c in results['android']['cases']} >= {'actualSettingsChooserSavesThresholdAndGatesRealPotionUse','continuingOlderRunAdoptsZeroWithCrystalsAndPreservesItsItemsAndScore'}
assert any(c['name']=='grassDewControlsRingBreakdownsSearchedWallAndOfflineLicenseRenderNatively' for c in results['android']['cases'])
visual_override=spec.get('profile_overrides',{}).get('redmagic-representative-api36',{})
visual,va=artifact(visual_override.get('run',spec['display']),visual_override.get('artifact','apd-title-display-redmagic-representative-api36'))
images={}
required_visuals={'qol-dew-settings.png','qol-force-weapon.png','qol-sharpshooting-weapon.png','qol-searched-wall.png','qol-searched-wall-color.png','qol-offline-license.png'}
if spec.get('kind') in ('healing-toggle','combined-owner'):required_visuals|={'healing-settings-toggle-on.png','healing-settings-toggle-off.png'}
assert set(spec['visual_review'])==required_visuals
for image in native.rglob('*.png'):
 with Image.open(image) as im:im.verify()
for name,expected in spec['visual_review'].items():
 paths=list(visual.rglob(name));assert len(paths)==1;assert sha(paths[0])==expected
 with Image.open(paths[0]) as im:assert im.size==(1216,2688);im.verify()
 images[name]=expected
profiles={};attempts=api(f'repos/{REPO}/actions/runs/{spec["display"]}/jobs?filter=all&per_page=100')['jobs']
jobs=list({j['name']:j for j in sorted(attempts,key=lambda j:j['id'])}.values())
assert len(jobs)==6
for job in jobs:
 if job['conclusion']!='success':
  assert job['conclusion']=='failure' and any(profile in job['name'] for profile in spec.get('profile_overrides',{})),job['name']
for name in ('m53-representative','compact-virtual','small-virtual','square-virtual','short-landscape','redmagic-representative-api36'):
 override=spec.get('profile_overrides',{}).get(name,{})
 folder,a=artifact(override.get('run',spec['display']),override.get('artifact','apd-title-display-'+name));summary=xml_summary(folder)
 assert summary['tests']==(6 if name.endswith('api36') else 1)
 proofs=list(folder.rglob('acceptance-source.json'));assert proofs
 p=json.loads(proofs[0].read_text());assert all(json.loads(f.read_text())==p for f in proofs)
 if override:assert p['test_source']==override['test_source']
 assert p['application_source']==source and p['application_run']==spec['developer'] and p['application_rebuilt'] is False
 profiles[name]={'run':override.get('run',spec['display']),'test_source':p.get('test_source'),'artifact':a['id'],'tests':summary['tests'],'application_sha256':p['application_sha256']}
 # All retained image files must decode, not only a representative capture.
 for image in folder.rglob('*.png'):
  with Image.open(image) as im:im.verify()
signer=sorted((pathlib.Path(os.environ['ANDROID_HOME'])/'build-tools').glob('*/apksigner'))[-1]
expected_cert='067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e'
variants={};assets={};hashes={};files=[]
for label,folder in [('Developer',dev),('Alpha-Testers',player)]:
 apk,=folder.rglob('*.apk');assert certificates(cmd(str(signer),'verify','--print-certs',str(apk)))==[expected_cert]
 metadata=cmd(str(signer.parent/'aapt'),'dump','badging',str(apk));assert "name='com.charltonchris29.adhdpixeldungeon.indev'" in metadata and "versionCode='920'" in metadata
 version='4.0.8-apd-INDEV-b'+('C'+str(number)+'-TEST' if label=='Developer' else 'P'+str(number));assert "versionName='"+version+"'" in metadata
 target=OUT/f'APD-{label}-{number}.apk';target.write_bytes(apk.read_bytes());checksum=pathlib.Path(str(target)+'.sha256');checksum.write_text(sha(target)+'  '+target.name+'\n');files.extend([target,checksum]);hashes[label]=sha(target);variants[label]={'version_name':version,'sha256':sha(target)}
 with zipfile.ZipFile(apk) as z:
  assets[label]={n[7:]:z.read(n) for n in z.namelist() if n.startswith('assets/') and not n.endswith('/')}
  assert assets[label]['legal/GPL-3.0.txt']==(ROOT/'LICENSE.txt').read_bytes()
  assert 'interfaces/apd_searched_wall.png' in assets[label]
  assert ('apd/features.txt' in assets[label])==(label=='Developer')
assert all(p['application_sha256']==hashes['Developer'] for p in profiles.values())
assert np['application_sha256']==hashes['Developer']
common=[a for a in assets['Developer'] if a!='apd/features.txt'];assert set(common)==set(assets['Alpha-Testers']) and all(assets['Developer'][n]==assets['Alpha-Testers'][n] for n in common)
tester_run=api(f'repos/{REPO}/actions/runs/{spec["tester"]}');assert tester_run['status']=='completed' and tester_run['conclusion']=='success'
tester,ta=artifact(spec['tester'],'apd-corrective-pair-tester-checks')
tester_proof,=tester.rglob('acceptance-source.json');tp=json.loads(tester_proof.read_text())
assert tp['application_source']==source and tp['application_run']==spec['player'] and tp['application_sha256']==hashes['Alpha-Testers'] and tp['application_rebuilt'] is False
tester_console,=tester.rglob('tester-instrumentation.txt');tester_text=tester_console.read_text();assert 'OK (3 tests)' in tester_text and 'INSTRUMENTATION_STATUS_CODE: -2' not in tester_text
# Package real producing source and every generated runtime asset, with only external signing substitutions.
snapshot={};removed=[]
with tarfile.open(fileobj=io.BytesIO(subprocess.check_output(['git','archive',source]))) as archive:
 for member in archive:
  if not member.isfile():continue
  if member.name.startswith('keystore/') or member.name.lower().endswith(('.jks','.keystore','.p12','.pfx')):removed.append(member.name);continue
  snapshot[member.name]=archive.extractfile(member).read()
config=snapshot['android/build.gradle'].decode();credentials=set(re.findall(r"(?:storePassword|keyPassword)\s+'([^']+)'",config));substitutions=[]
for n,b in list(snapshot.items()):
 try:s=b.decode('utf-8')
 except UnicodeDecodeError:continue
 for value in credentials:s=s.replace(value,'EXTERNAL_SIGNING_PASSWORD')
 if s.encode()!=b:substitutions.append(n);snapshot[n]=s.encode()
config=snapshot['android/build.gradle'].decode().replace("rootProject.file('keystore/apd-debug.jks')","rootProject.file(System.getenv('APD_SIGNING_KEYSTORE') ?: 'keystore/EXTERNAL-KEY-REQUIRED.jks')")
config=re.sub(r"storePassword\s+'EXTERNAL_SIGNING_PASSWORD'","storePassword System.getenv('APD_SIGNING_STORE_PASSWORD') ?: ''",config);config=re.sub(r"keyPassword\s+'EXTERNAL_SIGNING_PASSWORD'","keyPassword System.getenv('APD_SIGNING_KEY_PASSWORD') ?: System.getenv('APD_SIGNING_STORE_PASSWORD') ?: ''",config)
config=config.replace('signingConfig signingConfigs.apdDebug',"signingConfig project.hasProperty('apdUnsignedVerification') ? null : signingConfigs.apdDebug");snapshot['android/build.gradle']=config.encode()
for n in common:snapshot['core/src/main/assets/'+n]=assets['Developer'][n]
snapshot['CORRESPONDING-SOURCE/generated-inputs/apd/features.txt']=assets['Developer']['apd/features.txt']
readme=f'''APD source for both owner APK variants {number}\nProducing commit {source}; tag {tag}; versionCode920.\nJDK21; Android SDK36; included Gradle9.5 wrapper. Set ANDROID_HOME/local.properties.\nDeveloper: ./gradlew :android:assembleDebug -PapdUnsignedVerification -PapdTestBuild -PapdBuild=C{number}\nAlpha-Testers: ./gradlew :android:assembleDebug -PapdUnsignedVerification -PapdBuild=P{number}\nThese recipes build unsigned reconstructions. For an install-over update, supply the EXISTING external APD key via APD_SIGNING_KEYSTORE / APD_SIGNING_STORE_PASSWORD (optional separate key password) and omit -PapdUnsignedVerification. A different signing key cannot update existing installations; never uninstall or clear player data to work around signing.\nNo key/password/Git history is included. All generated history TSVs and runtime assets match the verified APKs. Developer features are generated by the preserved Gradle flag; the exact input is copied outside the asset root. Signing-only substitutions are explicit in manifest.json. Source reconstruction was not separately rebuilt; do not infer signed-byte reproducibility. GPLv3 or later, no warranty; original attribution/notices retained. Separate compliance audit findings remain documented; no public or external tester distribution was performed here.\n'''
snapshot['docs/SIGNING.md']=readme.encode();substitutions.append('docs/SIGNING.md (replace outdated signing advice with exact safe build instructions)')
if spec.get('native_test_source'):
 snapshot['CORRESPONDING-SOURCE/native-test-only-correction.patch']=subprocess.check_output(['git','diff',source,spec['native_test_source'],'--','android/src/androidTest/java/com/shatteredpixel/shatteredpixeldungeon/android/HealingSettingsDeviceTest.java'])
 readme+='\nCorrected native test source: '+spec['native_test_source']+'. Apply CORRESPONDING-SOURCE/native-test-only-correction.patch to reproduce the accepted instrumentation. The original test expected Off(-1) after turning On; corrected expectation requires saved On(0) to remain unchanged until optional-slider Save. No application input changed and no replacement APK was built. Original failed test runs remain recorded.\n'
snapshot['CORRESPONDING-SOURCE/README.txt']=readme.encode();manifest={'source_commit':source,'source_tag':tag,'variants':variants,'removed_signing_paths':removed,'credential_substitutions':substitutions,'signing_config_externalized':True,'generated_history_tsv_count':sum(n.startswith('core/src/main/assets/apd/history/') and n.endswith('.tsv') for n in snapshot),'files':{n:hashlib.sha256(b).hexdigest() for n,b in sorted(snapshot.items())}}
for n,b in snapshot.items():assert not any(v.encode() in b for v in credentials)
snapshot['CORRESPONDING-SOURCE/manifest.json']=(json.dumps(manifest,indent=2)+'\n').encode();sourcezip=OUT/f'APD-Source-{number}.zip'
with zipfile.ZipFile(sourcezip,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
 for n,b in sorted(snapshot.items()):
  info=zipfile.ZipInfo(f'APD-Source-{number}/'+n,date_time=(2026,11,2,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=(0o100755 if n=='gradlew' or n.endswith('.sh') else 0o100644)<<16;archive.writestr(info,b)
side=pathlib.Path(str(sourcezip)+'.sha256');side.write_text(sha(sourcezip)+'  '+sourcezip.name+'\n');files.extend([sourcezip,side])
# Preserve the existing verified fallback and verify its actual uploaded APK assets before any new write.
fallback_number=spec.get('fallback_number',354)
fallback=api(f'repos/{REPO}/releases/tags/apd-alpha-preview-{fallback_number}')
fallback_hashes=spec.get('fallback_hashes',{'Developer':'41f0b01ed520007741e01c274b3cd7194bf24e561c28352689c8e86fa1079755','Alpha-Testers':'3b5eaaa8df43a00c11ad4df0e7034f5d6ab701a09e61cb6c1d9e663ea83b4c31'})
for label,h in fallback_hashes.items():
 assert any(a['name']==f'APD-{label}-{fallback_number}.apk' and a.get('digest')=='sha256:'+h for a in fallback['assets'])
evidence={'source_commit':source,'tag':tag,'number':number,'runs':{k:spec[k] for k in ('developer','player','display','native','tester')},'test_summaries':results,'core_tests':spec['core_tests'],'android_tests':results['android']['tests'],'native_case_evidence':np,'native_test_source':spec.get('native_test_source'),'profile_overrides':spec.get('profile_overrides',{}),'original_stale_assertion_runs':spec.get('original_stale_assertion_runs',[]),'retained_fixture_failure_runs':spec.get('retained_fixture_failure_runs',[]),'retained_runner_failure_runs':spec.get('retained_runner_failure_runs',[]),'tester_api34_tests':3,'tester_proof':tp,'tester_artifact':ta['id'],'baseline69_cases_preserved':True,'profiles':profiles,'signing_certificate':expected_cert,'application_id':'com.charltonchris29.adhdpixeldungeon.indev','version_code':920,'apk_hashes':hashes,'source_sha256':sha(sourcezip),'visual_review':images,'visual_capture_artifact':va['id'],'display_job_attempts':[{k:j.get(k) for k in ('id','name','conclusion')} for j in attempts],'retained_display_failure':spec.get('retained_display_failure'),'physical_phone_verified':False,'new_application_apks_created':2,'owner_private_delivery':True,'fallback_preserved':True,'prepared_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'rejected_candidate':{'356':'Saved settings and tint fields passed, but visual review found white glyphs: the unlit tilemap shader ignored tint. Replacement uses the standard tint shader plus actual rendered-pixel checks. No356 publication.'},'retained_prebuild_failure':{'355':'Reviewed-change list omitted the build-request document; stopped before application compilation. Metadata fixed without changing application inputs.'},'behavior':{'saved_colors':8,'immediate_tint':True,'unknown_geometry_consulted':False,'search_memory_preserved':True,'search_rng_or_turns_changed':False,'score_formulas_changed':False}}
if spec.get('kind')=='combined-owner':
 new_images={}
 for name in ('qol-rat-quick-search-setting.png','qol-unidentified-origin-range.png'):
  image,=visual.rglob(name)
  with Image.open(image) as decoded:assert decoded.size==(1216,2688);decoded.verify()
  new_images[name]=sha(image)
  native_image,=native.rglob(name)
  with Image.open(native_image) as decoded:decoded.verify()
  assert name in spec['combined_visual_review'] and sha(image)==spec['combined_visual_review'][name]
 evidence['combined_visual_review']=new_images
 evidence['behavior']={'source_specific_unidentified_origins':True,'legacy_recovery_requires_reliable_evidence':True,'generation_ranges_do_not_read_hidden_upgrade_level':True,'normal_pickup_turns_and_enemy_policy_preserved':True,'interrupted_wall_target_and_manual_count_saved':True,'negative_wall_feedback_requires_completed_guaranteed_check':True,'rat_room_redesign_floor5_only':True,'rat_quick_search_off_default':True,'rat_quick_search_requires_floor5_main_branch_and_goo_defeat':True,'normal_actor_turn_payment':True,'unsupported_temporary_effects_refused_until_safe_to_price':True,'existing_generated_rooms_not_repainted':True,'no_score_formula_change':True}
 evidence['source_only_check']=spec.get('source_only_check')
 evidence['target_delivery_utc']='2026-11-05T01:15:00Z'
 evidence['candidate_report']='docs/APD-COMBINED-CANDIDATE-2026-11-04.md'
receipt=OUT/f'APD-Preview-{number}-Verification.json';receipt.write_text(json.dumps(evidence,indent=2)+'\n');files.append(receipt)
notes=f"""Choose from eight saved searched-wall checkmark colors in APD Settings under Visibility. Color changes apply immediately, with no scene reload or turn. Positions which known terrain geometry rules out as doorways are left unmarked; hidden room layout and secret-door identity are never consulted. Marks still mean searched, not guaranteed secret-free. Saved search history and actual searching remain unchanged.

Source `{source}`. Both variants passed all{spec['core_tests']} core cases. The Developer APK passed all{spec['android_tests']} native API34 cases in one successful invocation; the actual tester APK separately passed3 cases including the real color picker and unchanged search/turn state. All six display profiles passed, including the API36 gameplay/color check. The original square-display emulator went offline and its missing captures failed the job; only that job was retried on the same unchanged APK, with the original failure retained. These are automated/emulator results; physical-phone acceptance is pending. Existing TEST354 healing/conversion/throwing fixes are retained. Original signing/app ID retained: install over, never uninstall or clear player data.

Attempt355 stopped on a missing reviewed metadata path before application compilation; it produced no APK. Candidate356 was rejected during visual review because the unlit tilemap shader ignored tint. The replacement uses the standard tint-aware shader and requires actual Magenta pixels after a native color-picker tap.356 was not published. No application rebuild was performed for delivery.

"""
if spec.get('kind')=='healing-toggle':
 notes=f"""Healing confirmation now has a real On/Off toggle. Tapping Off turns it On immediately and the same button displays On; tapping On turns it Off. Turning On asks before every identified Healing-potion drink without requiring a slider. Optional HP thresholds remain available. The preference persists, and changing it or canceling a drink consumes no potion or turn. Previously, the Off button only saved Off again; it was not a toggle. This correction is new in {number}, not357.

Both APKs passed all{spec['core_tests']} core tests. Native acceptance completed all {spec['android_tests']} API34 case identities:70 passed in the original full run, and the corrected toggle case passed separately on the same APK. This is not a single71-pass invocation. The actual Alpha-Testers APK passed3 acceptance cases, and all six display profiles passed, including API36. Native taps verify Off-to-On-to-Off, immediate label changes, persistence, and a real Drink tap at low HP followed by cancellation. Original runs failed a stale Off expectation after enabling On. The expanded fixture also left a restored settings modal over its later hotbar tap; the fixture now closes the parent before pause and asserts the hotbar is unobstructed. Corrected instrumentation passed on the same unchanged APKs, with both failure histories retained. No replacement application build or new build number was made for that test correction. These are automated/emulator results; physical-phone acceptance is pending. Existing357 checkmark colors and doorway filtering are retained.

Exact source `{source}`. Original signing/application ID retained; install over the existing app, never uninstall or clear data. No application rebuild for publication.

"""
if spec.get('kind')=='combined-owner':
 notes=(ROOT/'docs/APD-COMBINED-CANDIDATE-2026-11-04.md').read_text().split('## Evidence')[0]
 notes+='\n## Completed candidate verification\n\n'
 notes+=f'Exact producing source `{source}`. Both actual variants passed all {spec["core_tests"]} core tests. The Developer APK passed all {spec["android_tests"]} API34 native cases in one successful invocation, retaining the existing case identities and adding stronger combined-feature assertions. All six display profiles passed. The actual Alpha-Testers APK separately passed three native acceptance cases; new setting and origin/range rendering assertions ran on both variants. Automated/emulator evidence only; physical-phone acceptance remains pending.\n\n'
 notes+='Rat King Quick Search refuses unsupported temporary timing effects rather than inventing a price. Existing generated rooms are preserved; new room layout applies when Floor5 is generated. Existing gear only recovers historically supported facts. Source reconstruction is supplied with generated inputs and external signing instructions; byte-for-byte reproduction has not been separately claimed.\n\n'
 notes+='The initial historical Floor5 fingerprint failures remain documented. The original golden archive is unchanged, with eleven explicit new Floor5 references for the approved chest/king layout; every other stable floor remains on its original hash. Full candidate checks succeeded after that reviewed update.\n\n'
 notes+=f'Verification runs: Developer/native {spec["developer"]}; Alpha-Testers {spec["player"]}; display {spec["display"]}; tester acceptance {spec["tester"]}. Source check {spec.get("source_only_check")}. APK publication, workflow invocation and owner link delivery are separate events; this report does not infer schedule causation.\n\n'
 notes+='Install over the existing APD app using the same signing key/application ID. Never uninstall or clear player data. Private owner delivery only; existing release assets and TEST358 fallback remain preserved.\n\n'
for label in ('Developer','Alpha-Testers'):notes+=f'- [{label} APK](https://github.com/{REPO}/releases/download/{tag}/APD-{label}-{number}.apk) — SHA256 `{hashes[label]}`\n'
notes+=f'\n[Matching source for both variants](https://github.com/{REPO}/releases/download/{tag}/{sourcezip.name}) — SHA256 `{sha(sourcezip)}`. Build instructions and generated inputs are included; signing material is excluded. GPLv3 or later, no warranty; unofficial independent fork with original credits retained. Separate GPL audit findings, including historical key exposure and inherited license exceptions, remain recorded and are not declared closed by this owner preview.\n\nExisting verified fallback and all assets are preserved. Private owner delivery only; no automatic posting to external testers or public release.\n';body=OUT/'release-notes.md';body.write_text(notes)
# Preparation is separated from publication so scan and reviewed archive evidence can be inspected first.
print(json.dumps({'prepared':True,'files':[p.name for p in files],'hashes':hashes,'source_sha256':sha(sourcezip),'history_count':manifest['generated_history_tsv_count']}))
