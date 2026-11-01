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

"""Restore only the established APD key from an external CI secret; never log values."""
import base64,hashlib,os,pathlib,subprocess,tempfile

EXPECTED='067688b3ab6bd5820a67f4c1ba7d5859db578173771cf31d3252b3e4e012246e'
def prepare():
    encoded=os.environ.get('APD_SIGNING_KEYSTORE_BASE64','')
    password=os.environ.get('APD_SIGNING_STORE_PASSWORD','')
    if not encoded or not password:
        raise RuntimeError('APD signing is not configured: supply the EXISTING key in APD_SIGNING_KEYSTORE_BASE64 and APD_SIGNING_STORE_PASSWORD. Refusing a replacement identity.')
    directory=pathlib.Path(tempfile.mkdtemp(prefix='apd-signing-',dir=os.environ.get('RUNNER_TEMP')))
    os.chmod(directory,0o700);path=directory/'existing-apd.jks'
    fd=os.open(path,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
    with os.fdopen(fd,'wb') as out:out.write(base64.b64decode(encoded,validate=True))
    alias=os.environ.get('APD_SIGNING_KEY_ALIAS') or 'apd'
    result=subprocess.run(['keytool','-exportcert','-keystore',str(path),'-alias',alias,'-storepass:env','APD_SIGNING_STORE_PASSWORD'],capture_output=True)
    if result.returncode or hashlib.sha256(result.stdout).hexdigest()!=EXPECTED:
        path.unlink(missing_ok=True)
        raise RuntimeError('APD signing certificate does not match the established install identity; no build permitted.')
    with open(os.environ['GITHUB_ENV'],'a') as out:
        out.write('APD_SIGNING_KEYSTORE='+str(path)+'\n')
    print('Existing APD signing certificate verified; temporary key held outside source.')
if __name__=='__main__':prepare()
