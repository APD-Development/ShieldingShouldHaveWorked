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

"""Compare unsigned reconstruction to distributed APK, excluding signatures only."""
import argparse,hashlib,json,pathlib,zipfile
def entries(path):
 with zipfile.ZipFile(path) as z:
  return {n:hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist() if not n.endswith('/') and not (n.startswith('META-INF/') and (n.upper().endswith(('.SF','.RSA','.DSA','.EC')) or n.upper()=='META-INF/MANIFEST.MF'))}
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('original');p.add_argument('reconstruction');p.add_argument('--report',required=True);a=p.parse_args();old,new=entries(a.original),entries(a.reconstruction)
 r={'same_application_payload':old==new,'original_entries':len(old),'reconstructed_entries':len(new),'missing':sorted(old.keys()-new.keys()),'extra':sorted(new.keys()-old.keys()),'different':[n for n in sorted(old.keys()&new.keys()) if old[n]!=new[n]],'excluded':'ZIP metadata, APK signing block and META-INF cryptographic signature files only; all DEX/resources/assets/native libraries included'}
 pathlib.Path(a.report).write_text(json.dumps(r,indent=2)+'\n');print(json.dumps(r));raise SystemExit(old!=new)
