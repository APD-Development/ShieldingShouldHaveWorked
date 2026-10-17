# S22+ title footer touch, bounded candidate (2026-10-17T16:17:41Z)

On TEST254, alpha tester Ozzie (Samsung Galaxy S22+) reported that the small grey bottom-center chevron did not work. This icon is designed to *hide* the title UI, not scroll. The owner demonstrated a working Hide UI response on a different physical phone, so no universal non-response is claimed.

Current `codex/apd-phone-fixes-2026-10-09` source mitigation: enlarge only the invisible Hide UI pointer hit rectangle from 16x16 to 32x20 virtual pixels, expanding sideways and 4px upward within the existing footer gap, and explicitly prioritize its input listener above the full-screen restore listener. Visible icon, other button placement, show/hide behavior, gameplay, saves and original scoring remain unchanged.

Retain `TitleGeometryTest.hideUiTouchTargetUsesOnlyReservedFooterSpace` for non-overlap across multiple portrait/landscape virtual sizes, and extend `TitleMenuLayoutDeviceTest` to inject a native tap outside the visible icon but inside its expanded target, hide, restore, then continue Options. Prior center-tap native assertions remain.

Verification: PENDING complete core suite, Android emulator and required original source gates. This is **not** proof of a fix on Ozzie's physical S22+ or in his installed TEST254. Existing supplemental short-landscape Hide UI failure remains recorded until independently resolved. Latest actually published owner APK TEST262, verified staged TEST267 and earlier fallbacks are unchanged. No distribution, publication, APK build or executable build-request edit was performed.
