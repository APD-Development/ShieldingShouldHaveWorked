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

import hashlib,json,pathlib,subprocess,unittest,xml.etree.ElementTree as E

ROOT=pathlib.Path(__file__).resolve().parents[1]
class GplComplianceTests(unittest.TestCase):
    def test_original_license_unchanged_and_bundled_verbatim(self):
        original=subprocess.check_output(['git','show','377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed:LICENSE.txt'],cwd=ROOT)
        self.assertEqual(original,(ROOT/'LICENSE.txt').read_bytes())
        self.assertEqual(original,(ROOT/'core/src/main/assets/legal/GPL-3.0.txt').read_bytes())
    def test_all_original_copyright_lines_are_preserved(self):
        paths=subprocess.check_output(['git','diff','--name-only','377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed'],cwd=ROOT,text=True).splitlines()
        for name in paths:
            if not name.endswith(('.java','.gradle','.py','.sh','.yml','.properties','.xml')):continue
            result=subprocess.run(['git','show','377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed:'+name],cwd=ROOT,text=True,capture_output=True)
            if result.returncode:continue
            before=result.stdout
            after=(ROOT/name).read_text()
            for line in before.splitlines():
                if 'Copyright' in line:self.assertIn(line,after,name)
    def test_runtime_network_services_are_disabled_for_each_platform(self):
        for path in ['android/build.gradle','desktop/build.gradle','ios/build.gradle']:
            text=(ROOT/path).read_text();self.assertNotIn("project(':services:updates:githubUpdates')",text);self.assertNotIn("project(':services:news:shatteredNews')",text)
        for path in [ROOT/'services/news/debugNews/src/main/java/com/shatteredpixel/shatteredpixeldungeon/services/news/NewsImpl.java',ROOT/'services/updates/debugUpdates/src/main/java/com/shatteredpixel/shatteredpixeldungeon/services/updates/UpdateImpl.java']:
            self.assertIn('return false;',path.read_text())
    def test_app_identity_and_signing_configuration_are_not_changed(self):
        original=subprocess.check_output(['git','show','377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed:build.gradle'],cwd=ROOT,text=True)
        self.assertIn("appPackageName = 'com.charltonchris29.adhdpixeldungeon'",original)
        self.assertEqual(hashlib.sha256((ROOT/'keystore/apd-debug.jks').read_bytes()).digest(),hashlib.sha256(subprocess.check_output(['git','show','377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed:keystore/apd-debug.jks'],cwd=ROOT)).digest())
    def test_release_gate_does_not_claim_compliance_with_known_blockers(self):
        import sys
        sys.path.insert(0,str(ROOT/'tools'))
        from apd_gpl_release_gate import check
        errors=check(ROOT,None);self.assertTrue(errors);self.assertIn('Tracked signing material remains',errors)
    def test_full_asset_and_dependency_inventories_are_present(self):
        a=json.loads((ROOT/'docs/compliance/assets.json').read_text());d=json.loads((ROOT/'docs/compliance/dependencies.json').read_text())
        self.assertEqual(7,sum(x['status']!='inherited' for x in a['assets']))
        self.assertTrue(any(x['artifact']=='json' and 'Good, not Evil' in x.get('issue','') for x in d['direct_gradle_declarations']))

if __name__=='__main__':unittest.main()
