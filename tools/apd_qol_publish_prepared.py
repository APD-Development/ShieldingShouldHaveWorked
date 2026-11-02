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

"""Publish only prepared verified private owner artifacts, preserving all prior assets."""
import datetime,hashlib,json,os,pathlib,subprocess,time
repo=os.environ['GITHUB_REPOSITORY'];out=pathlib.Path('owner-delivery');spec=json.loads(pathlib.Path('docs/APD-QOL-OWNER-DELIVERY.json').read_text());number=spec['number'];tag=f'apd-alpha-preview-{number}'
api=lambda p:json.loads(subprocess.check_output(['gh','api',p]));sha=lambda p:hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
assert api('repos/'+repo)['private'];scan=json.loads((out/'APD-Source-Scan.json').read_text());assert scan['supplemental']['source_findings_reviewed']
source,=out.glob('APD-Source-*.zip');assert scan['supplemental']['source_sha256']==sha(source)
proof=out/f'APD-Preview-{number}-Verification.json';evidence=json.loads(proof.read_text());assert evidence['source_commit']==spec['source'] and evidence['core_tests']==2287 and evidence['android_tests']==spec['android_tests']
files=[p for p in out.iterdir() if p.suffix in ('.apk','.sha256','.zip') or p.name=='APD-Source-Scan.json']
# GitHub does not create a draft release's tag until publication; establish and verify it explicitly.
ref=subprocess.run(['gh','api',f'repos/{repo}/git/ref/tags/{tag}'],text=True,capture_output=True)
if ref.returncode:
 assert '404' in ref.stderr,ref.stderr
 subprocess.run(['gh','api','--method','POST',f'repos/{repo}/git/refs','-f','ref=refs/tags/'+tag,'-f','sha='+spec['source']],check=True)
resolved=api(f'repos/{repo}/git/ref/tags/{tag}')['object'];assert resolved['type']=='commit' and resolved['sha']==spec['source']
lookup=subprocess.run(['gh','api',f'repos/{repo}/releases/tags/{tag}'],text=True,capture_output=True)
if lookup.returncode:
 assert '404' in lookup.stderr,lookup.stderr
 subprocess.run(['gh','release','create',tag,'--repo',repo,'--target',spec['source'],'--title',f'APD Alpha Preview {number} — dew, searched walls and ring details','--notes-file',str(out/'release-notes.md'),'--draft','--prerelease','--latest=false'],check=True)
release=api(f'repos/{repo}/releases/tags/{tag}');resolved=api(f'repos/{repo}/git/ref/tags/{tag}')['object'];assert resolved['type']=='commit' and resolved['sha']==spec['source']
existing={a['name']:a for a in release['assets']}
for p in files:
 if p.name in existing:assert existing[p.name].get('digest')=='sha256:'+sha(p),'Existing asset differs; never overwrite'
 else:subprocess.run(['gh','release','upload',tag,'--repo',repo,str(p)],check=True)
release=api(f'repos/{repo}/releases/tags/{tag}');existing={a['name']:a for a in release['assets']}
assert all(existing[p.name].get('digest')=='sha256:'+sha(p) for p in files)
if release['draft']:subprocess.run(['gh','release','edit',tag,'--repo',repo,'--draft=false','--prerelease','--latest=false'],check=True)
# Actual successful downloads, not metadata alone, establish delivery readiness.
verify=pathlib.Path('downloaded-back');verify.mkdir(exist_ok=True)
for pattern in (f'APD-Developer-{number}.apk',f'APD-Alpha-Testers-{number}.apk',source.name):
 subprocess.run(['gh','release','download',tag,'--repo',repo,'--pattern',pattern,'--dir',str(verify)],check=True)
 assert sha(verify/pattern)==sha(out/pattern)
release=api(f'repos/{repo}/releases/tags/{tag}');evidence.update({'release_id':release['id'],'asset_publication':[{k:a.get(k) for k in ('id','name','digest','created_at','browser_download_url')} for a in release['assets']],'published_at':release['published_at'],'downloaded_back_verified':True,'link_sent':False,'delivery_workflow_run':os.environ['GITHUB_RUN_ID'],'download_verified_utc':datetime.datetime.now(datetime.timezone.utc).isoformat()})
proof.write_text(json.dumps(evidence,indent=2)+'\n')
if proof.name not in {a['name'] for a in release['assets']}:subprocess.run(['gh','release','upload',tag,'--repo',repo,str(proof)],check=True)
print('VERIFIED DOWNLOAD LINKS')
for label in ('Developer','Alpha-Testers'):print(f'https://github.com/{repo}/releases/download/{tag}/APD-{label}-{number}.apk')
print(f'https://github.com/{repo}/releases/tag/{tag}')
