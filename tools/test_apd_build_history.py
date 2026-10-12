import importlib.util,pathlib,tempfile,unittest
spec=importlib.util.spec_from_file_location('history',pathlib.Path(__file__).with_name('apd_build_history.py'));history=importlib.util.module_from_spec(spec);spec.loader.exec_module(history)
class BuildHistoryTests(unittest.TestCase):
 def ledger(self):return {'date':'2026-10-11','previous_label':'TEST50','changes':[{'category':'Bug Fixes','sprite':'V061_BUGFIX','title':'Save protection','text':'Failed writes preserve existing saves.','paths':['core/src/main/java/Test.java']}]}
 def test_missing_application_change_rejected(self):
  with self.assertRaises(ValueError):history.validate({'status':'ahead','files':[{'filename':'android/src/main/AndroidManifest.xml'}]},self.ledger())
 def test_reviewed_changes_embedded_and_duplicate_build_replaced(self):
  ledger=self.ledger();history.validate({'status':'ahead','files':[{'filename':'core/src/main/java/Test.java'}]},ledger)
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);assets=root/'core/src/main/assets/apd/history';assets.mkdir(parents=True);(assets/'index.tsv').write_text('build-1\tBuild 1\tUnknown\tIncomplete\n')
   history.embed(root,ledger,100);history.embed(root,ledger,100)
   self.assertEqual(1,(assets/'index.tsv').read_text().count('codex-test-100\t'))
   self.assertIn('Failed writes preserve existing saves.',(assets/'codex-test-100.tsv').read_text())
   self.assertNotIn('Changes since',(assets/'index.tsv').read_text())
 def test_truncated_comparison_rejected(self):
  with self.assertRaises(ValueError):history.validate({'status':'ahead','files':[{'filename':'docs/x'}]*300},self.ledger())
 def test_previous_apk_history_survives_and_reviewed_corrections_win(self):
  import zipfile
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);assets=root/'core/src/main/assets/apd/history';assets.mkdir(parents=True)
   (assets/'index.tsv').write_text('codex-test-63\tCodex TEST 63\t2026-10-12\tCorrected attribution\n')
   (assets/'codex-test-63.tsv').write_text('Corrected exact build notes.\n')
   apk=root/'old.apk'
   with zipfile.ZipFile(apk,'w') as z:
    z.writestr('assets/apd/history/index.tsv','codex-test-64\tCodex TEST 64\t2026-10-12\tAPK change record\ncodex-test-63\tCodex TEST 63\t2026-10-12\tOld attribution\n')
    z.writestr('assets/apd/history/codex-test-64.tsv','Verified older notes.\n')
    z.writestr('assets/apd/history/codex-test-63.tsv','Outdated cumulative notes.\n')
   history.merge_previous_apk(root,apk,'Codex TEST 64')
   self.assertEqual('Verified older notes.\n',(assets/'codex-test-64.tsv').read_text())
   self.assertEqual('Corrected exact build notes.\n',(assets/'codex-test-63.tsv').read_text())
   history.merge_previous_apk(root,apk,'Codex TEST 64')
   self.assertEqual(1,(assets/'index.tsv').read_text().count('codex-test-64\t'))
 def test_wrong_previous_build_rejected(self):
  import zipfile
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);apk=root/'old.apk'
   with zipfile.ZipFile(apk,'w') as z:z.writestr('assets/apd/history/index.tsv','codex-test-63\tCodex TEST 63\t2026-10-12\tAPK change record\n')
   with self.assertRaises(ValueError):history.merge_previous_apk(root,apk,'Codex TEST 64')
if __name__=='__main__':unittest.main()

