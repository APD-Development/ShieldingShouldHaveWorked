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

"""Upload only validated source/checksum assets to the existing private341 release.

Not a binary publication path. Cannot create releases, replace assets, change
visibility, upload APKs, or make a historical binary license-correct.
"""
import argparse,hashlib,json,pathlib,subprocess,zipfile
from apd_corresponding_source import VARIANTS,COMMIT
ROOT=pathlib.Path(__file__).resolve().parents[1]
REPO='APD-Development/ShieldingShouldHaveWorked'
def verify(path):
 path=pathlib.Path(path)
 variant=next((v for v in VARIANTS if path.name=='APD-Source-341-'+v+'.zip'),None)
 if not variant:raise ValueError('Only pinned TEST341 corresponding-source archives allowed')
 with zipfile.ZipFile(path) as z:
  prefix='APD-Source-341-'+variant+'/'
  names=z.namelist()
  if len(names)!=len(set(names)):raise ValueError('Duplicate ZIP entries')
  if any(not n.startswith(prefix) or '..' in pathlib.PurePosixPath(n).parts or n.lower().endswith(('.apk','.jks','.keystore','.p12','.pfx')) or '/.git/' in n for n in names):raise ValueError('Unsafe source archive')
  m=json.loads(z.read(prefix+'CORRESPONDING-SOURCE/manifest.json'))
  assert m['source_commit']==COMMIT and m['variant']==variant and m['apk_sha256']==VARIANTS[variant][0]
  assert m['history_tsv_count']==366
  assert set(names)=={prefix+n for n in m['files']}|{prefix+'CORRESPONDING-SOURCE/manifest.json'}
  for n,h in m['files'].items():
   if hashlib.sha256(z.read(prefix+n)).hexdigest()!=h:raise ValueError('Source entry hash mismatch')
 checksum=path.with_suffix('.zip.sha256')
 assert checksum.read_text()==hashlib.sha256(path.read_bytes()).hexdigest()+'  '+path.name+'\n'
 return checksum
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('archives',nargs='+');p.add_argument('--publish',action='store_true');a=p.parse_args()
 scan=json.loads((ROOT/'docs/compliance/source341-scan.json').read_text());assert scan['all_candidates_reviewed']
 assets=[]
 for archive in a.archives:assets.extend([archive,str(verify(archive))])
 if a.publish:
  repo=json.loads(subprocess.check_output(['gh','api','repos/'+REPO]));assert repo['private'] is True
  release=json.loads(subprocess.check_output(['gh','api','repos/'+REPO+'/releases/tags/apd-alpha-preview-341']));assert release['id']==428527455
  existing={x['name'] for x in release['assets']}
  assets=[x for x in assets if pathlib.Path(x).name not in existing]
  if assets:subprocess.run(['gh','release','upload','apd-alpha-preview-341','--repo',REPO,*assets],check=True)
 print('Validated key-free matching source archives; existing assets are preserved.')
