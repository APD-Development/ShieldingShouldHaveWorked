#!/usr/bin/env python3
# ADHD Pixel Dungeon
# Copyright (C) 2026 Chris Charlton
# SPDX-License-Identifier: GPL-3.0-or-later

"""Deliver a manually accepted, exact existing private APK pair without rebuilding."""
import datetime
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import subprocess
import zipfile

from apd_existing_build_evidence import certificates, xml_summary
from apd_owner_delivery_policy import require_apk_delivery_allowed


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def api(path):
    return json.loads(subprocess.check_output(['gh', 'api', path]))


def extract_pinned(archive, expected, destination):
    assert digest(archive) == expected, 'Artifact archive differs'
    destination = Path(destination)
    destination.mkdir(exist_ok=True)
    with zipfile.ZipFile(archive) as z:
        assert len(z.infolist()) <= 10000
        assert sum(i.file_size for i in z.infolist()) <= 1500000000
        for entry in z.infolist():
            path = PurePosixPath(entry.filename)
            assert not path.is_absolute() and '..' not in path.parts
            assert '\\' not in entry.filename and entry.file_size <= 200000000
            if entry.is_dir():
                continue
            target = destination.joinpath(*path.parts)
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(z.read(entry))


def verify_files(spec, review, out, evidence, signer):
    out, evidence, signer = Path(out), Path(evidence), Path(signer)
    number = spec['number']
    proof = json.loads((out / f'APD-Preview-{number}-Verification.json').read_text())
    for key in ('source_commit', 'number', 'core_tests', 'android_tests',
                'application_id', 'version_code', 'signing_certificate', 'apk_hashes', 'source_sha256'):
        assert proof[key] == spec[key], ('Prepared proof differs', key)
    assert str(proof['workflow_run']) == str(spec['run_id'])
    assert set(spec['apk_hashes']) == {'Developer', 'Alpha-Testers'}
    for label, expected in spec['apk_hashes'].items():
        apk = out / f'APD-{label}-{number}.apk'
        assert digest(apk) == expected
        assert Path(str(apk) + '.sha256').read_text().split()[0] == expected
        signature = subprocess.check_output([str(signer), 'verify', '--print-certs', str(apk)], text=True)
        assert certificates(signature) == [spec['signing_certificate']]
        metadata = subprocess.check_output([str(signer.parent / 'aapt'), 'dump', 'badging', str(apk)], text=True)
        version = f"4.0.8-apd-INDEV-b{'C' if label == 'Developer' else 'P'}{number}" + ('-TEST' if label == 'Developer' else '')
        assert f"name='{spec['application_id']}'" in metadata
        assert f"versionCode='{spec['version_code']}'" in metadata and f"versionName='{version}'" in metadata
    source = out / f'APD-Source-{number}.zip'
    assert digest(source) == spec['source_sha256']
    with zipfile.ZipFile(source) as z:
        prefix = f'APD-Source-{number}/'
        manifest = json.loads(z.read(prefix + 'CORRESPONDING-SOURCE/manifest.json'))
        assert manifest['source_commit'] == spec['source_commit']
        assert len(manifest['files']) == spec['source_manifest_files']
        for name, expected in manifest['files'].items():
            assert hashlib.sha256(z.read(prefix + name)).hexdigest() == expected, name
    scan = json.loads((out / 'APD-Source-Scan.json').read_text())['supplemental']
    assert scan['source_sha256'] == spec['source_sha256'] and scan['source_findings_reviewed'] is True
    xml_root = evidence / 'core/build/test-results/test'
    summary = xml_summary(str(xml_root))
    assert summary['tests'] == spec['core_tests']
    assert len(list(xml_root.glob('TEST-*.xml'))) == spec['core_suites']
    for label in ('developer', 'alpha'):
        for case in ('qol', 'wall', 'seed', 'restart', 'gameplay', 'freeze'):
            text = (evidence / 'evidence' / f'{label}-{case}.txt').read_text()
            assert 'OK (1 test)' in text and 'INSTRUMENTATION_CODE: -1' in text
            assert len(re.findall(r'^INSTRUMENTATION_STATUS_CODE: 0$', text, re.M)) == 1
            assert not any(x in text for x in ('FAILURES', 'INSTRUMENTATION_FAILED', 'Process crashed', 'AssumptionFailure'))
    assert spec['android_tests'] == 12
    assert review['accepted'] is True and review['source_commit'] == spec['source_commit']
    assert review['original_build_run'] == spec['run_id']
    assert len(review['reviewed_images']) == spec['manual_frames'] > 0
    assert len({x['path'] for x in review['reviewed_images']}) == spec['manual_frames']
    for frame in review['reviewed_images']:
        assert digest(evidence / frame['path']) == frame['sha256'], frame['path']
    return proof


def archive_evidence(evidence, target, images_only=False):
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as z:
        for path in sorted(Path(evidence).rglob('*')):
            if path.is_file() and (not images_only or path.suffix == '.png'):
                entry = zipfile.ZipInfo(path.relative_to(evidence).as_posix(), (2026, 11, 10, 0, 0, 0))
                entry.compress_type = zipfile.ZIP_DEFLATED
                z.writestr(entry, path.read_bytes())


def main():
    require_apk_delivery_allowed(Path.cwd())
    repo = os.environ['GITHUB_REPOSITORY']
    assert repo == 'APD-Development/ShieldingShouldHaveWorked' and api('repos/' + repo)['private']
    spec = json.loads(Path('docs/APD-REVIEWED-PAIR-DELIVERY.json').read_text())
    number, source = spec['number'], spec['source_commit']
    tag = f'apd-alpha-preview-{number}'
    run = api(f'repos/{repo}/actions/runs/{spec["run_id"]}')
    assert run['status'] == 'completed' and run['conclusion'] == 'success' and run['head_sha'] == source
    for kind, destination in (('prepared', 'owner-delivery'), ('evidence', 'complete-evidence')):
        pin = spec['artifacts'][kind]
        artifact = api(f'repos/{repo}/actions/artifacts/{pin["id"]}')
        assert not artifact['expired'] and artifact['name'] == pin['name']
        assert artifact['digest'] == 'sha256:' + pin['sha256']
        assert artifact['workflow_run']['id'] == spec['run_id'] and artifact['workflow_run']['head_sha'] == source
        archive = Path(kind + '-original.zip')
        with archive.open('wb') as output:
            subprocess.run(['gh', 'api', f'repos/{repo}/actions/artifacts/{pin["id"]}/zip'], stdout=output, check=True)
        extract_pinned(archive, pin['sha256'], destination)
    out, evidence = Path('owner-delivery'), Path('complete-evidence')
    review = json.loads(Path(spec['visual_review']).read_text())
    signer = sorted(Path(os.environ['ANDROID_HOME']).glob('build-tools/*/apksigner'))[-1]
    verify_files(spec, review, out, evidence, signer)
    archive_evidence(evidence, out / f'APD-{number}-Complete-Test-Evidence.zip')
    archive_evidence(evidence, out / f'APD-{number}-Native-Visuals.zip', True)
    shutil.copyfile(spec['visual_review'], out / f'APD-{number}-Manual-Visual-Review.json')
    with (out / 'release-notes.md').open('a') as notes:
        notes.write('\n\n' + Path(spec['status_note']).read_text())
    # All local acceptance checks finish before creating a release or tag.
    matches = []
    for page in range(1, 101):
        releases = api(f'repos/{repo}/releases?per_page=100&page={page}')
        matches.extend(r for r in releases if r['tag_name'] == tag)
        if len(releases) < 100:
            break
    else:
        raise RuntimeError('Release inventory incomplete')
    assert len(matches) <= 1
    if not matches:
        subprocess.run(['gh', 'release', 'create', tag, '--repo', repo, '--target', source,
                        '--draft', '--prerelease', '--latest=false', '--title', spec['title'],
                        '--notes-file', str(out / 'release-notes.md')], check=True)
        matches = [api(f'repos/{repo}/releases/tags/{tag}')]
    release = matches[0]
    assert release['target_commitish'] == source, 'Existing release source differs'
    existing = {a['name']: a for a in release['assets']}
    files = [p for p in sorted(out.iterdir()) if p.is_file() and p.name != 'release-notes.md']
    for path in files:
        if path.name in existing:
            assert existing[path.name].get('digest') == 'sha256:' + digest(path), 'Never overwrite an existing differing asset'
        else:
            assert release['draft'], 'Do not add missing assets to an already published release'
            subprocess.run(['gh', 'release', 'upload', tag, str(path), '--repo', repo], check=True)
    current = api(f'repos/{repo}/releases/{release["id"]}')
    assets = {a['name']: a for a in current['assets']}
    assert all(assets[p.name].get('digest') == 'sha256:' + digest(p) for p in files)
    downloaded = Path('downloaded-back')
    downloaded.mkdir(exist_ok=True)
    for name in (f'APD-Developer-{number}.apk', f'APD-Alpha-Testers-{number}.apk', f'APD-Source-{number}.zip'):
        subprocess.run(['gh', 'release', 'download', tag, '--repo', repo, '--pattern', name, '--dir', str(downloaded)], check=True)
        assert digest(downloaded / name) == digest(out / name)
    if current['draft']:
        subprocess.run(['gh', 'release', 'edit', tag, '--repo', repo, '--draft=false', '--prerelease',
                        '--latest=false', '--notes-file', str(out / 'release-notes.md')], check=True)
    current = api(f'repos/{repo}/releases/{release["id"]}')
    resolved = api(f'repos/{repo}/git/ref/tags/{tag}')['object']
    assert not current['draft'] and current['published_at'] and resolved['type'] == 'commit' and resolved['sha'] == source
    receipt = dict(number=number, source_commit=source, release_url=current['html_url'], published_utc=current['published_at'],
                   delivery_workflow_run=os.environ['GITHUB_RUN_ID'], downloaded_back_verified=True, rebuilt=False,
                   core_tests=spec['core_tests'], core_suites=spec['core_suites'], android_tests=12,
                   manual_frames=spec['manual_frames'], source_manifest_files=spec['source_manifest_files'],
                   apk_hashes=spec['apk_hashes'], source_sha256=spec['source_sha256'], physical_phone_verified=False,
                   verified_utc=datetime.datetime.now(datetime.timezone.utc).isoformat())
    (out / f'APD-{number}-Delivery-Receipt.json').write_text(json.dumps(receipt, indent=2) + '\n')
    print(json.dumps(receipt, indent=2))


if __name__ == '__main__':
    main()
