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
 def test_truncated_comparison_rejected(self):
  with self.assertRaises(ValueError):history.validate({'status':'ahead','files':[{'filename':'docs/x'}]*300},self.ledger())
if __name__=='__main__':unittest.main()
