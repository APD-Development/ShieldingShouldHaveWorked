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

"""Exercise secret exclusion, generated inputs, and source-only publication boundaries."""
import hashlib,io,json,pathlib,tarfile,tempfile,unittest,zipfile
from unittest.mock import patch
import apd_corresponding_source as recovery
from apd_publish_source_archive import verify
class SourceRecoveryTests(unittest.TestCase):
 def test_wrong_binary_is_refused_before_reading_source(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d)/'wrong.apk';p.write_bytes(b'not the verified build')
   with patch.object(recovery.subprocess,'check_output') as call:
    with self.assertRaises(ValueError):recovery.package(p,'Developer',pathlib.Path(d)/'source.zip')
    call.assert_not_called()
 def test_archive_has_all_generated_inputs_but_never_credentials_or_key(self):
  source=io.BytesIO()
  with tarfile.open(fileobj=source,mode='w') as t:
   files={'android/build.gradle':b"storeFile rootProject.file('keystore/apd-debug.jks')\nstorePassword 'SYNTHETIC-ONLY-PASSWORD'\nkeyPassword 'SYNTHETIC-ONLY-PASSWORD'\nsigningConfig signingConfigs.apdDebug\n",'keystore/apd-debug.jks':b'SYNTHETIC-KEY','docs/SIGNING.md':b'SYNTHETIC-ONLY-PASSWORD','gradlew':b'#!/bin/sh\n','LICENSE.txt':b'GPL fixture'}
   for n,b in files.items():
    i=tarfile.TarInfo(n);i.size=len(b);t.addfile(i,io.BytesIO(b))
  with tempfile.TemporaryDirectory() as d:
   apk=pathlib.Path(d)/'fixture.apk'
   with zipfile.ZipFile(apk,'w') as z:
    for i in range(366):z.writestr('assets/apd/history/'+str(i)+'.tsv','synthetic history\n')
    z.writestr('assets/apd/features.txt',b'testBuild\ngrudge\ndemoUnlock\nclericPlayable\n')
   config=(hashlib.sha256(apk.read_bytes()).hexdigest(),'C341',True)
   out=pathlib.Path(d)/'APD-Source-341-Developer.zip'
   with patch.dict(recovery.VARIANTS,{'Developer':config}),patch.object(recovery.subprocess,'check_output',return_value=source.getvalue()):
    recovery.package(apk,'Developer',out)
    verify(out)
   with zipfile.ZipFile(out) as z:
    for n in z.namelist():
     self.assertFalse(n.endswith('.jks'));self.assertNotIn(b'SYNTHETIC-ONLY-PASSWORD',z.read(n));self.assertNotIn(b'SYNTHETIC-KEY',z.read(n))
    m=json.loads(z.read('APD-Source-341-Developer/CORRESPONDING-SOURCE/manifest.json'))
    self.assertEqual(366,m['history_tsv_count']);self.assertFalse(m['compiled_reconstruction_verified'])
 def test_binary_upload_cannot_use_source_path(self):
  with self.assertRaises(ValueError):verify('/tmp/APD-Developer-341.apk')
if __name__=='__main__':unittest.main()
