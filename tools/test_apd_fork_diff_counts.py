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

import unittest
from apd_fork_diff_counts import parse_numstat, summarize

class DiffCountsTest(unittest.TestCase):
    def test_regular_binary_and_renamed_paths_are_distinct(self):
        rows = parse_numstat(b'3\t2\ta.java\0-\t-\tsprite.png\0' + b'0\t0\t\0old.java\0new.java\0')
        self.assertEqual(3, len(rows))
        self.assertEqual((3, 2), (rows[0]['added'], rows[0]['deleted']))
        self.assertIsNone(rows[1]['added'])
        self.assertEqual(('old.java', 'new.java'), (rows[2]['previous_path'], rows[2]['path']))
    def test_tabs_in_file_names_are_not_field_separators(self):
        self.assertEqual('a\tb.java', parse_numstat(b'2\t1\ta\tb.java\0')[0]['path'])
    def test_totals_exclude_data_and_do_not_double_count_renames(self):
        rows = parse_numstat(b'3\t1\ta.java\0' + b'2\t0\tnotes.md\0' + b'0\t0\t\0old.py\0new.py\0')
        groups, total = summarize(rows)
        self.assertEqual({'files': 2, 'added': 3, 'deleted': 1, 'net': 2}, total)
