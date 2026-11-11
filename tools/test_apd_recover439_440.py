# ADHD Pixel Dungeon; Copyright (C) 2026 Chris Charlton
# SPDX-License-Identifier: GPL-3.0-or-later
import copy
import io
from pathlib import Path
import tempfile
import unittest
import zipfile

from apd_recover439_440 import (CASES, SPECS, check_core, check_instrumentation,
                                extract_archive, safe_name, sanitize_snapshot)


class RecoverySafeguardsTest(unittest.TestCase):
    def native_text(self):
        return ('INSTRUMENTATION_STATUS: class=example.NativeTest\n'
                'INSTRUMENTATION_STATUS: test=expectedMethod\n'
                'INSTRUMENTATION_STATUS_CODE: 1\n'
                'INSTRUMENTATION_STATUS: class=example.NativeTest\n'
                'INSTRUMENTATION_STATUS: test=expectedMethod\n'
                'INSTRUMENTATION_STATUS_CODE: 0\n'
                'OK (1 test)\nINSTRUMENTATION_CODE: -1\n')

    def test_requires_completed_exact_native_case(self):
        text = self.native_text()
        check_instrumentation(text, 'example.NativeTest', 'expectedMethod')
        for broken in (text.replace('expectedMethod', 'anotherMethod'),
                       text.replace('example.NativeTest', 'example.OtherTest'),
                       text.replace('INSTRUMENTATION_CODE: -1', ''),
                       text.replace('OK (1 test)', ''),
                       text.replace('STATUS_CODE: 0', 'STATUS_CODE: -3'),
                       text + 'INSTRUMENTATION_STATUS_CODE: 0\n',
                       text + 'AssumptionFailure\n'):
            with self.subTest(output=broken[-100:]), self.assertRaises(RuntimeError):
                check_instrumentation(broken, 'example.NativeTest', 'expectedMethod')

    def test_six_legacy_cases_include_fresh_process_and_stall(self):
        self.assertEqual([case[0] for case in CASES], ['qol', 'wall', 'seed', 'restart', 'gameplay', 'freeze'])
        self.assertEqual(len({(case[1], case[2]) for case in CASES}), 6)
        self.assertEqual(CASES[3][2], 'b_freshProcessReadsEnabledAndCanDisable')

    def test_rejects_path_escape_and_artifact_symlink(self):
        for name in ('../outside', '/absolute', 'a/../../outside', 'a\\..\\outside'):
            with self.subTest(name=name), self.assertRaises(RuntimeError):
                safe_name(name)
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            archive = root / 'bad.zip'
            with zipfile.ZipFile(archive, 'w') as packed:
                entry = zipfile.ZipInfo('link')
                entry.external_attr = 0o120777 << 16
                packed.writestr(entry, '../outside')
            with self.assertRaises(RuntimeError):
                extract_archive(archive, root / 'unpacked')
            self.assertFalse((root / 'unpacked').exists())

    def test_source_removes_keys_credentials_and_externalizes_signing(self):
        snapshot = {
            'android/build.gradle': b"storeFile rootProject.file('keystore/apd-debug.jks')\nstorePassword 'example-private-password'\nkeyPassword 'example-private-password'\nsigningConfig signingConfigs.apdDebug\n",
            'keystore/apd-debug.jks': b'keybytes',
            'other/copy.P12': b'keybytes',
            'docs/old-signing.md': b'example-private-password',
            'core/Unchanged.java': b'package test;\n',
        }
        removed, substitutions = sanitize_snapshot(snapshot)
        self.assertEqual(set(removed), {'keystore/apd-debug.jks', 'other/copy.P12'})
        self.assertIn('docs/old-signing.md', substitutions)
        self.assertTrue(all(b'example-private-password' not in blob for blob in snapshot.values()))
        self.assertIn(b"System.getenv('APD_SIGNING_KEYSTORE')", snapshot['android/build.gradle'])
        self.assertIn(b'apdUnsignedVerification', snapshot['android/build.gradle'])
        self.assertEqual(snapshot['core/Unchanged.java'], b'package test;\n')

    def test_unrecognized_signing_configuration_fails_closed(self):
        with self.assertRaises(RuntimeError):
            sanitize_snapshot({'android/build.gradle': b'unknownSigningConfiguration()'})

    def test_core_rejects_duplicate_hidden_skipped_and_incomplete_results(self):
        previous = SPECS[439]
        SPECS[439] = dict(previous, suites=1, tests=2)
        try:
            good = ('<testsuite tests="2" failures="0" errors="0" skipped="0">'
                    '<testcase classname="A" name="one"/><testcase classname="A" name="two"/>'
                    '</testsuite>')
            with tempfile.TemporaryDirectory() as temporary:
                path = Path(temporary) / 'TEST-test.xml'
                path.write_text(good)
                self.assertEqual(check_core(temporary, 439)['tests'], 2)
                broken = (good.replace('name="two"', 'name="one"'),
                          good.replace('<testcase classname="A" name="two"/>', ''),
                          good.replace('name="two"/>', 'name="two"><skipped/></testcase>'),
                          good.replace('failures="0"', 'failures="1"'))
                for text in broken:
                    path.write_text(text)
                    with self.subTest(text=text), self.assertRaises(RuntimeError):
                        check_core(temporary, 439)
        finally:
            SPECS[439] = previous


if __name__ == '__main__':
    unittest.main()
