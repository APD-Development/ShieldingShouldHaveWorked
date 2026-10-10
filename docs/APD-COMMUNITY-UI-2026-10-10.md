## 2026-10-10 22:35 UTC — Community preview polish and retained native regressions

Each of the eight Community pages now explains its own future actions beneath the existing explicit offline availability statement. The content-selection title uses separate words so it can wrap on narrow screens. No backend, fake submissions/votes/comments, clans, player-facing creator secrets or undecided rules were introduced.

Six local Community tests passed before the title-only word separation; they will run again in the complete suite. Expanded the existing real touch-navigation Android test: every page must survive scene recreation, render the actual page at 100×120 UI units without text/window overflow, and return to normal layout with exactly one menu. Native results pending; these are synthetic UI bounds plus API 34 emulator checks, not RedMagic acceptance.

The two UIMSD fixes remain the active bug work. Request one final complete verification of the combined source after the current TEST27 run completes; do not cancel useful running checks or label the new source verified early.

