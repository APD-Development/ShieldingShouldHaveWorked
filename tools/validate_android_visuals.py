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

"""Reject failed adb output, corrupt PNGs, and tiny placeholder captures."""
import pathlib,struct,sys,zlib

def validate(data):
    if not data.startswith(b'\x89PNG\r\n\x1a\n'):raise ValueError('Not a PNG screenshot')
    pos=8;size=None;saw_pixels=False;saw_end=False
    while pos<len(data):
        if len(data)-pos<12:raise ValueError('Truncated PNG chunk')
        length=struct.unpack('>I',data[pos:pos+4])[0];kind=data[pos+4:pos+8]
        if length>len(data)-pos-12:raise ValueError('Truncated PNG payload')
        payload=data[pos+8:pos+8+length];crc=struct.unpack('>I',data[pos+8+length:pos+12+length])[0]
        if zlib.crc32(kind+payload)&0xffffffff!=crc:raise ValueError('PNG checksum mismatch')
        if kind==b'IHDR':
            if size is not None or length!=13:raise ValueError('Invalid PNG header')
            size=struct.unpack('>II',payload[:8])
            if min(size)<320:raise ValueError('Capture is too small to review an Android screen')
        if kind==b'IDAT':saw_pixels=True
        if kind==b'IEND':saw_end=True
        pos+=12+length
    if not size or not saw_pixels or not saw_end:raise ValueError('Incomplete screenshot')
    return size

if __name__=='__main__':
    folder=pathlib.Path(sys.argv[1])
    for name in (sys.argv[2:] or ['apd-changes.png','spd-changes-reference.png']):
        print(name,validate((folder/name).read_bytes()))
