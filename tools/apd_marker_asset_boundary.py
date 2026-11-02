"""Reusing gameplay evidence is allowed only when the sole runtime-source change is the marker PNG."""
import json,os,subprocess,sys
sys.path.insert(0,'tools')
from apd_existing_build_evidence import application_inputs
repo=os.environ['GITHUB_REPOSITORY'];baseline='5aa76b55f6b57166ff0ddd68794e9fbb6803e71e'
def api(p):return json.loads(subprocess.check_output(['gh','api',p]))
base,current=[api(f'repos/{repo}/git/trees/{s}?recursive=1') for s in (baseline,os.environ['GITHUB_SHA'])]
assert not base.get('truncated') and not current.get('truncated')
a,b=application_inputs(base['tree']),application_inputs(current['tree'])
changed={p for p in a.keys()|b.keys() if a.get(p)!=b.get(p)}
assert changed=={'core/src/main/assets/interfaces/apd_searched_wall.png'},changed
run=api(f'repos/{repo}/actions/runs/41532850839');assert run['head_sha']==baseline and run['status']=='completed' and run['conclusion']=='success'
print('All application Java, build files and other runtime assets match fully verified TEST347; only marker pixels changed.')
