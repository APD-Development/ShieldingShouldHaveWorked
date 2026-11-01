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

"""Exact Git line differences; does not estimate how many lines were 'modified'."""
import argparse, collections, json, pathlib, subprocess
from apd_source_line_counts import component, EXTENSIONS

def parse_numstat(data):
    fields = iter(data.split(b'\0'))
    rows = []
    for field in fields:
        if not field:
            continue
        added, deleted, path = field.split(b'\t', 2)
        old_path = None
        if not path:
            old_path, path = next(fields), next(fields)
        rows.append({'path': path.decode('utf-8', 'surrogateescape'),
                     'previous_path': old_path.decode('utf-8', 'surrogateescape') if old_path is not None else None,
                     'added': None if added == b'-' else int(added),
                     'deleted': None if deleted == b'-' else int(deleted)})
    return rows

def summarize(rows):
    groups = collections.defaultdict(lambda: {'files': 0, 'added': 0, 'deleted': 0})
    for row in rows:
        if row['added'] is None or pathlib.PurePosixPath(row['path']).suffix not in EXTENSIONS:
            continue
        group = groups[component(row['path'])]
        group['files'] += 1
        group['added'] += row['added']
        group['deleted'] += row['deleted']
    total = {key: sum(group[key] for group in groups.values()) for key in ('files', 'added', 'deleted')}
    total['net'] = total['added'] - total['deleted']
    return dict(sorted(groups.items())), total

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('baseline'); parser.add_argument('source'); parser.add_argument('output')
    args = parser.parse_args()
    base = subprocess.check_output(['git', 'rev-parse', args.baseline+'^{commit}'], text=True).strip()
    source = subprocess.check_output(['git', 'rev-parse', args.source+'^{commit}'], text=True).strip()
    rows = parse_numstat(subprocess.check_output(['git', 'diff', '--numstat', '-z', '--find-renames', base, source, '--']))
    groups, total = summarize(rows)
    result = {'baseline_commit': base, 'source_commit': source, 'components': groups, 'source_totals': total,
              'all_changed_files': rows,
              'notes': ['Counts compare these two exact trees, not total developer work over time.',
                        'Added/deleted lines include comments and blanks. A unique modified-line count is not inferred.',
                        'Renames use Git rename detection; binary changes have null line counts.',
                        'Source extensions match the existing source inventory; other text/data changes remain in the file ledger.',
                        'Fork reference verified separately: first APD setup commit 56be46ab950e841857ec2eb7edf7ae64a6cc65b2 directly parents 2bb34a4e91d29c8785a9363cad6ddfe5122b1d4f.',
                        'These are project differences, not an attribution of every changed line to Codex or any individual.']}
    path = pathlib.Path(args.output); path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(result, indent=2)+'\n')
    print(json.dumps({'source_totals': total, 'components': groups}, indent=2))

if __name__ == '__main__': main()
