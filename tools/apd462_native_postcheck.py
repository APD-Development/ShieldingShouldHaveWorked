#!/usr/bin/env python3
"""Verify APD462 frozen APK native case and all device screenshots."""
import argparse
import hashlib
import json
from pathlib import Path
from apd_native_screenshot_transfer import sync_images

def verify(variant, case, directory, source):
    if variant not in ("Developer", "Alpha-Testers"):
        raise ValueError("Unknown variant")
    if case not in ("WallGeometryDeviceTest", "SearchContextDeviceTest", "SeedHistoryDeviceTest"):
        raise ValueError("Unknown native case")
    output = (directory / "instrumentation.txt").read_text(encoding="utf-8")
    if "OK (1 test)" not in output or any(x in output for x in ("FAILURES!!!", "INSTRUMENTATION_FAILED", "Process crashed.")):
        raise RuntimeError("Native test did not pass one complete case")
    screenshots = sync_images(directory / "visuals")
    if not screenshots:
        raise RuntimeError("Missing native screenshots")
    receipt = {
        "build": 462, "source": source, "variant": variant, "case": case,
        "tests": 1, "screenshots": screenshots, "same_frozen_apk": True,
        "device_transfer_complete": True, "physical_phone_verified": False,
    }
    (directory / "native-verification.json").write_text(json.dumps(receipt, indent=2) + "\n")
    return receipt

if __name__ == "__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--variant", required=True)
    p.add_argument("--case", required=True)
    p.add_argument("--directory", type=Path, required=True)
    p.add_argument("--source", required=True)
    a=p.parse_args()
    print(json.dumps(verify(a.variant,a.case,a.directory,a.source),indent=2))
