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

"""Add APD notices without changing upstream copyright text or strict data formats."""
import argparse, json, pathlib, re, subprocess

BASE = '2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f'
def git(*args): return subprocess.check_output(['git', *args], text=True)
def apply(root):
    root = pathlib.Path(root)
    existing = json.loads((root/'docs/compliance/header-years.json').read_text())['files']
    paths = set(git('diff', '--name-only', BASE, 'HEAD').splitlines())
    paths.update(git('diff', '--name-only').splitlines())
    paths.update(git('ls-files', '--others', '--exclude-standard').splitlines())
    legal = (root/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/TitleScene.java').read_text()
    terms = legal[legal.index(' * This program is free software:'):legal.index(' */')]
    notices = []
    for name in sorted(paths):
        p=root/name
        if not p.is_file() or name == 'LICENSE.txt': continue
        years=existing.get(name,['2026']);years='-'.join([min(years),max(years)]) if len(set(years))>1 else years[0]
        block=' * ADHD Pixel Dungeon\n * Copyright (C) '+years+' Chris Charlton\n *\n'
        suffix=p.suffix.lower()
        inline = name == '.gitignore' or suffix in ('.java','.gradle','.py','.sh','.yml','.yaml','.properties','.xml')
        if name.startswith('keystore/'):
            notices.append(dict(path=name,style='NOT DONE: signing material left untouched; report findings only',years=years));continue
        if inline:
            text=p.read_text();before=re.findall(r'^.*Copyright.*$',text,re.M)
            if 'Copyright (C) '+years+' Chris Charlton' not in text[:2200]:
                if suffix in ('.java','.gradle'):
                    match=re.match(r'/\*[\s\S]*?\*/',text)
                    if match and 'This program is free software:' in match.group(0):
                        at=text.index(' * This program is free software:');text=text[:at]+block+text[at:]
                    else: text='/*\n'+block+terms+' */\n\n'+text
                elif suffix=='.xml':
                    # XML declarations must remain first; avoid introducing double-hyphens.
                    at=text.find('?>')+2 if text.startswith('<?xml') else 0
                    header='\n<!--\n'+block.replace(' *','').replace('*/','')+terms.replace(' *','')+'-->\n'
                    text=text[:at]+header+text[at:]
                else:
                    at=text.find('\n')+1 if text.startswith('#!') else 0
                    header='# ADHD Pixel Dungeon\n# Copyright (C) '+years+' Chris Charlton\n#\n'+''.join('#'+line[2:]+'\n' for line in terms.splitlines())+'\n'
                    text=text[:at]+header+text[at:]
                assert all(line in text for line in before),name
                p.write_text(text)
            style='inline GPL notice; original copyright lines retained'
        else:style='external per-file notice; bytes preserved (binary/strict data/document)'
        notices.append(dict(path=name,years=years,copyright='ADHD Pixel Dungeon / Copyright (C) '+years+' Chris Charlton (APD modifications only)',license='GPL-3.0-or-later for APD modifications; see dependency/asset exceptions and unresolved provenance',style=style))
    target=root/'docs/compliance/file-notices.json'
    target.write_text(json.dumps({'baseline':BASE,'limitation':'Binary/strict-format data are not relabeled internally. This manifest records APD modification notices without altering their bytes. It does not grant rights in third-party material or signing keys.','files':notices},indent=2)+'\n')
    print(json.dumps({'inline':sum(n['style'].startswith('inline') for n in notices),'external':sum(n['style'].startswith('external') for n in notices),'exceptions':sum(n['style'].startswith('NOT DONE') for n in notices)}))
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--root',default='.');a=p.parse_args();apply(a.root)
