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

"""Verify platform branding while protecting all unrelated atlas pixels."""
import hashlib,io,json,pathlib,subprocess,unittest
from PIL import Image,ImageChops,ImageDraw
ROOT=pathlib.Path(__file__).resolve().parents[1]
class BrandingTests(unittest.TestCase):
 def test_every_declared_output_matches_manifest(self):
  m=json.loads((ROOT/'docs/compliance/branding-assets.json').read_text())
  for name,item in m['outputs'].items():
   path=ROOT/(item['path'] if name=='generator' else name)
   self.assertEqual(item['sha256'],hashlib.sha256(path.read_bytes()).hexdigest(),name)
   if 'size' in item:self.assertEqual(tuple(item['size']),Image.open(io.BytesIO(path.read_bytes())).size,name)
 def test_only_branding_rectangles_change_in_shared_atlases(self):
  m=json.loads((ROOT/'docs/compliance/branding-assets.json').read_text())
  for path,boxes in m['repacked_branding_regions'].items():
   old=Image.open(io.BytesIO(subprocess.check_output(['git','show','377c3e20b7dc6ec6c4d149c5c1b4ea7cda3bb7ed:'+path],cwd=ROOT))).convert('RGBA')
   new=Image.open(io.BytesIO((ROOT/path).read_bytes())).convert('RGBA');self.assertEqual(old.size,new.size)
   for box in boxes:
    old.paste((0,0,0,0),box);new.paste((0,0,0,0),box)
   self.assertEqual(old.tobytes(),new.tobytes(),path)
 def test_ios_launchers_have_no_alpha_and_legacy_title_layers_are_absent(self):
  for path in (ROOT/'ios/assets/Assets.xcassets/AppIcon.appiconset').glob('*.png'):self.assertEqual('RGB',Image.open(io.BytesIO(path.read_bytes())).mode)
  for name in ['archs','back_clusters','mid_mixed','front_small']:self.assertFalse((ROOT/('core/src/main/assets/splashes/title/'+name+'.png')).exists())
 def test_android_adaptive_lettering_inside_safe_area(self):
  for path in (ROOT/'android/src').rglob('ic_launcher_foreground.png'):
   im=Image.open(io.BytesIO(path.read_bytes()));box=im.getbbox();w,h=im.size
   self.assertGreaterEqual(box[0],w*.25);self.assertGreaterEqual(box[1],h*.25)
   self.assertLessEqual(box[2],w*.75);self.assertLessEqual(box[3],h*.75)
if __name__=='__main__':unittest.main()
