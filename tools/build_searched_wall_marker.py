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
# along with this program. If not, see <http://www.gnu.org/licenses/>
"""Editable original 16px overlay tile; no external artwork or font inputs."""
from pathlib import Path
from PIL import Image
im=Image.new('RGBA',(16,16))
points=[(5,9),(6,10),(7,11),(8,10),(9,9),(10,8)]
for x,y in points:im.putpixel((x+1,y+1),(10,25,20,255))
for x,y in points:im.putpixel((x,y),(147,223,171,255))
im.save(Path(__file__).resolve().parents[1]/'core/src/main/assets/interfaces/apd_searched_wall.png')
