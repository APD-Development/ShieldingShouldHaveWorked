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

"""Reproducible original APD pixel lettering and technical atlas packing.

No external font, stock logo, generated photograph or remote artwork is used.
Unbranded atlas pixels are retained verbatim; only reserved branding cells change.
"""
import hashlib,json,math,pathlib,re
from PIL import Image,ImageDraw

ROOT=pathlib.Path(__file__).resolve().parents[1]
ASSETS=ROOT/'core/src/main/assets'
GLYPHS={
 'A':['01110','10001','10001','11111','10001','10001','10001'],
 'D':['11110','10001','10001','10001','10001','10001','11110'],
 'H':['10001','10001','10001','11111','10001','10001','10001'],
 'P':['11110','10001','10001','11110','10000','10000','10000'],
 'I':['11111','00100','00100','00100','00100','00100','11111'],
 'X':['10001','10001','01010','00100','01010','10001','10001'],
 'E':['11111','10000','10000','11110','10000','10000','11111'],
 'L':['10000','10000','10000','10000','10000','10000','11111'],
 'U':['10001','10001','10001','10001','10001','10001','01110'],
 'N':['10001','11001','11001','10101','10011','10011','10001'],
 'G':['01111','10000','10000','10111','10001','10001','01111'],
 'O':['01110','10001','10001','10001','10001','10001','01110'],
}
OUTPUTS={};PACKED={}
def save(image,path):
    path=ROOT/path;path.parent.mkdir(parents=True,exist_ok=True);image.save(path)
    OUTPUTS[str(path.relative_to(ROOT))]={'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'size':list(image.size),'license':'GPL-3.0-or-later','copyright':'2026 Chris Charlton, original APD artwork; inherited unbranded atlas pixels retain upstream notices'}

def text(draw,value,x,y,scale,color):
    for index,char in enumerate(value):
        for row,bits in enumerate(GLYPHS[char]):
            for col,bit in enumerate(bits):
                if bit=='1':draw.rectangle((x+(index*6+col)*scale,y+row*scale,x+(index*6+col+1)*scale-1,y+(row+1)*scale-1),fill=color)

def centered(draw,value,w,y,scale,color):text(draw,value,(w-(len(value)*6-1)*scale)//2,y,scale,color)

def icon(size,mode='full'):
    base=Image.new('RGBA',(64,64),(0,0,0,0));d=ImageDraw.Draw(base)
    if mode in ('full','background'):
        d.rectangle((0,0,63,63),fill=(10,19,21,255));d.rectangle((2,2,61,61),outline=(75,214,149,255),width=2)
        d.rectangle((6,6,57,57),outline=(33,70,59,255),width=1)
    if mode!='background':
        color=(255,255,255,255) if mode=='monochrome' else (218,255,235,255)
        if size<32:centered(d,'A',64,11,6,color)
        else:centered(d,'APD',64,22,3,color)
    if mode in ('foreground','monochrome'):
        # Android adaptive icons crop the outer third. Keep every glyph in its safe area.
        safe=Image.new('RGBA',(108,108));safe.alpha_composite(base,(22,22));base=safe
    return base.resize((size,size),Image.Resampling.NEAREST)

def logo(w,h,glow=False):
    im=Image.new('RGBA',(w,h),(0,0,0,0));d=ImageDraw.Draw(im)
    if not glow:
        d.rectangle((1,2,w-2,h-3),fill=(11,23,24,248),outline=(80,172,132,255),width=1)
        d.rectangle((4,5,w-5,h-6),outline=(34,63,56,255),width=1)
    color=(50,210,128,85) if glow else (202,255,225,255)
    if h>=90:
        for value,y,scale in [('ADHD',16,3),('PIXEL',45,2),('DUNGEON',66,2)]:centered(d,value,w,y,scale,color)
    else:
        centered(d,'ADHD',w,5,3,color);centered(d,'PIXEL',w//2,32,2,color)
        text(d,'DUNGEON',w//2+4,32,2,color)
    return im

def background(w,h):
    im=Image.new('RGBA',(w,h));d=ImageDraw.Draw(im)
    for y in range(0,h,12):
        for x in range(-12,w,24):
            left=x+(12 if (y//12)%2 else 0);tone=18+((left*7+y*13)%11)
            d.rectangle((left,y,left+22,y+10),fill=(tone//2,tone+5,tone+1,255))
            d.line((left+1,y+1,left+21,y+1),fill=(24,44,36,255))
    d.rectangle((6,6,w-7,h-7),outline=(37,89,63,255),width=2)
    return im

def repack(relative,regions):
    path=ASSETS/relative;original=Image.open(path).convert('RGBA')
    # Construct a new atlas from non-branding pixels plus original APD-generated tiles.
    result=Image.new('RGBA',original.size);result.paste(original)
    boxes=[]
    for box,image in regions:
        result.paste(image.resize((box[2]-box[0],box[3]-box[1]),Image.Resampling.NEAREST),box[:2]);boxes.append(list(box))
    save(result,path.relative_to(ROOT));PACKED[str(path.relative_to(ROOT))]=boxes

def build():
    save(logo(139,100),'core/src/main/assets/interfaces/apd_logo.png')
    save(logo(139,100,True),'core/src/main/assets/interfaces/apd_logo_glow.png')
    save(background(200,360),'core/src/main/assets/splashes/title/apd_bg_port.png')
    save(background(400,200),'core/src/main/assets/splashes/title/apd_bg_land.png')
    button=Image.new('RGBA',(24,24));d=ImageDraw.Draw(button)
    d.rectangle((0,0,23,17),fill=(56,65,58,255),outline=(113,140,117,255),width=1)
    d.rectangle((2,2,21,15),outline=(28,38,32,255),width=1)
    # Retain the source atlas contract: transparent separator then an unused solid swatch.
    d.rectangle((0,21,23,23),fill=(56,65,58,255))
    save(button,'core/src/main/assets/interfaces/apd_button.png')
    flame=Image.new('RGBA',(256,256));d=ImageDraw.Draw(flame)
    for frame in range(24):
        x=(frame%5)*47;y=(frame//5)*47;sway=round(4*math.sin(frame*math.pi/12))
        d.polygon([(x+13,y+42),(x+9,y+30),(x+20+sway,y+15),(x+20+sway,y+4),(x+34,y+22),(x+37,y+34),(x+30,y+43)],fill=(22,161,94,200))
        d.polygon([(x+18,y+42),(x+16,y+32),(x+23+sway,y+19),(x+29,y+31),(x+29,y+42)],fill=(123,255,181,235))
    save(flame,'core/src/main/assets/effects/apd_flame.png')
    # Launcher outputs retain each platform's exact existing dimensions and file names.
    for root in ['android/src/main/res','android/src/debug/res','desktop/src/main/assets/icons','ios/assets/Assets.xcassets/AppIcon.appiconset']:
        for path in sorted((ROOT/root).rglob('*.png')):
            w,h=Image.open(path).size;assert w==h,path
            mode='monochrome' if 'monochrome' in path.name else 'foreground' if 'foreground' in path.name else 'background' if 'background' in path.name else 'full'
            save(icon(w,mode).convert('RGB') if root.startswith('ios/') else icon(w,mode),path.relative_to(ROOT))
    ico=ROOT/'desktop/src/main/assets/icons/windows.ico';icon(256).save(ico,format='ICO',sizes=[(16,16),(32,32),(48,48),(64,64),(128,128),(256,256)],append_images=[icon(n) for n in (16,32,48,64,128)])
    icns=ROOT/'desktop/src/main/assets/icons/mac.icns';icon(1024).save(icns,format='ICNS')
    for path in [ico,icns]:OUTPUTS[str(path.relative_to(ROOT))]={'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'license':'GPL-3.0-or-later','copyright':'2026 Chris Charlton, original APD artwork'}
    for path in (ROOT/'ios/assets/Assets.xcassets').rglob('*.png'):
        if 'AppIcon' in str(path):continue
        w,h=Image.open(path).size
        if 'Banner' in str(path):save(logo(139,100).resize((w,h),Image.Resampling.NEAREST) if h*2>=w else logo(240,57).resize((w,h),Image.Resampling.NEAREST),path.relative_to(ROOT))
    # Replace old stock title graphic/glows but keep boss/death art pixel-identical.
    repack('interfaces/banners.png',[
        ((0,0,139,100),logo(139,100)),((139,0,278,100),logo(139,100,True)),
        ((0,100,240,157),logo(240,57)),((240,100,480,157),logo(240,57,True))])
    repack('interfaces/icons.png',[((119,0,135,16),icon(16))])
    source=(ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/changelist/ChangeIcons.java').read_text()
    body=source.split('public enum ChangeIcons {',1)[1].split(';',1)[0]
    body=re.sub(r'//[^\n]*','',body)
    names=re.findall(r'^\s*(\w+)(?:\([^\n]*?\))?\s*[,;]',body+';',re.M)
    regions=[]
    for name in ['V021_SHPX','V074_SHPX']:
        i=names.index(name);x=(i%8)*16;y=(i//8)*16
        neutral=Image.new('RGBA',(16,16));centered(ImageDraw.Draw(neutral),'I',16,4,1,(194,218,202,255))
        regions.append(((x,y,x+16,y+16),neutral))
    repack('interfaces/change_icons.png',regions)
    OUTPUTS['generator']={'path':'tools/build_apd_branding.py','sha256':hashlib.sha256(pathlib.Path(__file__).read_bytes()).hexdigest(),'license':'GPL-3.0-or-later','font':'Original elementary 5x7 glyph geometry included in this generator; no external font'}
    target=ROOT/'docs/compliance/branding-assets.json';target.write_text(json.dumps({'outputs':OUTPUTS,'repacked_branding_regions':PACKED,'unchanged_regions':'All pixels outside the listed rectangles are copied verbatim; no gameplay item/enemy art is redrawn.'},indent=2)+'\n')

if __name__=='__main__':build()
