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

"""Fail closed before source/binary publication; never publishes or reads key values."""
import argparse, hashlib, json, pathlib, subprocess

def check(root, manifest):
    root=pathlib.Path(root);errors=[]
    blockers=json.loads((root/'docs/compliance/release-blockers.json').read_text())['blockers']
    errors.extend('Unresolved: '+b['id'] for b in blockers if b['status']!='DONE')
    # File names only: do not load/print the keystore or credential values.
    tracked=subprocess.check_output(['git','ls-files','-z'],cwd=root,text=True).split('\0')
    if any(p.lower().endswith(('.jks','.keystore','.p12','.pfx')) for p in tracked):errors.append('Tracked signing material remains')
    if not manifest:errors.append('Missing exact-binary/source release manifest')
    else:
        data=json.loads(pathlib.Path(manifest).read_text())
        required=['source_commit','source_tag','version_name','version_code','binary','binary_sha256','source_archive','source_sha256','source_download_url','recipient_access_evidence','secret_scan_evidence','build_recipe']
        errors.extend('Missing field: '+k for k in required if not data.get(k))
        if not errors:
            tagged=subprocess.check_output(['git','rev-parse',data['source_tag']+'^{}'],cwd=root,text=True).strip()
            if tagged!=data['source_commit']:errors.append('Tag does not identify exact producing source')
            for field,hash_field in [('binary','binary_sha256'),('source_archive','source_sha256')]:
                p=pathlib.Path(data[field]);actual=hashlib.sha256(p.read_bytes()).hexdigest()
                if actual!=data[hash_field]:errors.append('Checksum mismatch: '+field)
            # The archive producer must exclude keys; recipient access, resolved dependency/assets
            # and original scan records are independent required evidence, not inferred from a URL.
    return errors

def publication_run(command, *args, **kwargs):
    """Guard real GitHub binary-release writes, leaving read-only checks untouched."""
    if list(command[:3]) in (['gh', 'release', 'create'], ['gh', 'release', 'upload']):
        import os
        errors = check(pathlib.Path(__file__).resolve().parents[1], os.environ.get('APD_GPL_RELEASE_MANIFEST'))
        if errors:
            raise RuntimeError('GPL publication blocked: ' + '; '.join(errors))
    return subprocess.run(command, *args, **kwargs)

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--manifest');a=p.parse_args()
    errors=check(a.root,a.manifest)
    print(json.dumps({'status':'BLOCKED' if errors else 'READY FOR RELEASE CHECKS','reasons':errors},indent=2))
    raise SystemExit(bool(errors))
