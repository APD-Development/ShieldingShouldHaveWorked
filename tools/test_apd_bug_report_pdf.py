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
from apd_bug_report_pdf import markdown_blocks
class MarkdownBlocksTest(unittest.TestCase):
    def test_adjacent_title_heading_and_body_are_not_dropped(self):
        self.assertEqual(["# Report", "## Source", "commit abc"], markdown_blocks("# Report\n## Source\ncommit abc"))
    def test_tables_remain_one_block(self):
        table="| A | B |\n| --- | --- |\n| 1 | 2 |"
        self.assertEqual(["## Tests",table],markdown_blocks("## Tests\n"+table))
    def test_existing_paragraph_boundaries_remain(self):
        self.assertEqual(["## Tests","Passed","Pending"],markdown_blocks("## Tests\n\nPassed\n\nPending"))
    def test_empty_source_has_no_blocks(self):
        self.assertEqual([],markdown_blocks("\n\n"))
