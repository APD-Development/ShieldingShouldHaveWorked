# APD441 source changes, November 11, 2026

The resumed baseline is `293b5a0d5fed957c1d953b301fadf576a766611b` on `codex/apd-phone-fixes-2026-10-09`. APD439/440 original APK recovery is separate from this new source candidate. No claim is made that these changes are in those original APKs.

## Bounded correction and retained work

Only the ordinary Excess Charge description changes in production resources. Its rank factors now state 0.67 / 1.34 / 2.01 per effective imbued-wand level, including temporary bonuses and whole-point rounding. It explains that an existing stronger Barrier is retained rather than added. The existing `Wand.wandUsed` calculation is unchanged.

`ExcessChargeHandlerRegressionTest.java` is recovered byte-for-byte from prior commit `a14cf151638c7863a07f533d3a3de423bbe136e8` (Git blob `481b1b036de405e25e2c86256423c2e1c03f082c`). Its four tests invoke the real handler and preserve charges, time, ownership boundaries and RNG.

Six new `BossPylonTargetingIntegrationTest` methods exercise actual `Hero.checkVisibleMobs` and `AttackIndicator` refresh paths: ranged versus melee eligibility, reachable active pylons, hidden/inactive exclusion, blocked projectile paths including mind vision, preserved manual targets and a vulnerable boss. No additional production targeting changes were needed.

The existing Android QoL case now opens the ordinary Excess Charge window, checks its actual text and reachable last line, and captures normal/normal-bottom/expanded/bottom images. This is test code only; the existing twelve-case native acceptance set remains mandatory.

## Verification and limitations

Freshly compiled current sources: 1,718 main Java files across core/SPD-classes/services and 491 core test files, using the verified prior offline kit only for external dependency JARs. No previous compiled game or test classes were used. All 463 current test-source classes containing JUnit tests were included. Full result: 3,190 tests, zero failures, zero errors, zero skipped cases. All 3,179 original APD439 cases remain included.

The first full local run recorded two missing-history-index errors and zero assertion failures. Its evidence was retained. The exact current Git tree index and 26 missing historical TSVs were recovered, all 902 current game assets were present, and a fresh recompilation/full run passed. No assertion or expected behavior was weakened.

Android compilation and native execution of this candidate are not established by the local core run. The source-only CI request requires full core, Android debug/release/instrumentation and desktop compilation. A new APK must pass all twelve original native cases plus affected-image review before delivery. Signing identity, .indev application ID, saves, statistics, Cleric balance, and all previous builds remain unchanged.

Cursed Wand wording and other owner-decision items remain pending. This slice does not complete the whole description audit.
