# Resume handoff (short, kept current after every step)

The long project handoff is `docs/APD-HANDOFF.md`; the canonical plan is `APD_Canonical_Design_and_Implementation_Plan1.docx`. This file is the quick resume note.

## Resume protocol (owner's standing rules)
- After each completed step (tests pass, committed, pushed, this file updated): cancel the earlier resume and schedule ONE new one-shot `send_later` about 5 h 10 min ahead. Message: "Resume: read docs/HANDOFF.md, continue from the next task, don't redo finished work, don't ask me for decisions already recorded." When the queue is finished, cancel it and schedule nothing.
- If a usage-limit message shows a reset time, record it here and schedule the resume for that time + 10 min (weekly limit: same with the weekly reset). Never retry requests while limited.
- When a resume fires: at most 3 attempts without a new commit. Still blocked: stop, record why here, do not schedule a fourth.
- Unattended: no product decisions (record them under Open questions and move to a task that does not need them). Never force-push, change signing keys, or touch anything outside the current task. Never lower testing or code quality to save usage. If a step cannot be finished properly, stop at a clean commit.

## Limit info
- Owner says the 5-hour usage window resets at 1 am (owner's time zone not stated; assume the 5 h 10 min rule unless a UTC reset time is shown).

## Branches
- Work on `claude/adhd-pixel-dungeon-setup-iszc3v`. `main` only gets a sha whose core tests and API 26 + 34 device tests are green (each main push builds `apd-build-N`).
- Commit trailers: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>` and `Claude-Session: https://claude.ai/code/session_01LkLem8wbQUhnUeQsHwAyjg`. No PRs unless asked.

## Done
- Back fix (exit button ate the Back key), difficulty on the character screen: green on API 26/34, shipped (apd-build-70/71).
- Backend foundations, local submission queue, sync registry: main build 72 green.
- UIMSD: saved views, column order, Bosses table, Run type / Modded columns (device runs 79, 83, 84 green).
- Telemetry 1.9 (hero state, landmarks, keys, secrets, wells, pages): core tests green, NOT yet device-tested.
- `PlayRunDeviceTest` (new): run 85 failed because the recorder forgets the log path once the run is sealed; fixed in 513b4fc (path kept from before the death).

## In flight
- Device run 87 (android-tests.yml, head 513b4fc) on the claude branch. Run 86 (head 02a49fd) was red, cause not read (likely the same test).

## Next task (exact)
1. Read run 87 for API 26 and 34. Green: `git push origin HEAD:main` for that sha, check main Build APK once, record `apd-build-N` in docs/APD-HANDOFF.md on the claude branch.
2. Red: read the failure with `get_job_logs` (return_content, the saved file, python grep for `FAILED|AssertionError|APDUI`), fix the test mechanics or the real bug, rerun. Never weaken or skip a test. If run 86's failure was something other than PlayRunDeviceTest, fix that too.
3. Then the rolling queue: small recording batches from "Not yet recorded" in docs/APD-UIMSD-UNIVERSAL.md (each with its storage gate and hidden-information review), then other approved plan tasks. Core tests locally; device runs only at checkpoints.

## Decisions already made (do not ask again)
- Local-first; backend owns shared authoritative state; raw UIMSD history stays local. Provider-neutral interfaces; the provider choice is NOT a blocker. No secret in the APK; gameplay cannot create developer grants. No public deployment, spending, credentials, hidden analytics or private history uploads.
- Clans are NOT approved: only generic reusable group/role foundations.
- Cleric un-ban approved in principle, but needs a ruleset-identity decision (documented in docs/APD-CLERIC-AUDIT.md). Cleric humor and the UIMSD surprised-query reaction are not to be implemented.
- Priority: (1) bugs, regressions, stability; (2) work in progress; (3) approved features, backend, plan work; (4) tests; (5) comment/doc cleanup (low).
- Source comments read like a normal maintained project: short natural developer notes, no AI or chat language, no invented history, comment-only passes stay behaviour-neutral.
- Stop only for a new product/design decision, destructive or irreversible change, major architecture conflict, anything weakening save integrity, telemetry integrity, hidden-information protection or non-P2W, or anything needing credentials, money or external publication.
- Do not manufacture busywork. Stopping condition: every remaining roadmap task is genuinely blocked after reviewing the whole canonical plan.

## Open questions (owner only)
- Cleric un-ban vs ruleset identity.
- Upstream support/Patreon text.
- Backend: provider, sign-in methods, key custody, ranking verification level, retention, moderation.
- Contact Evan Debenham; delete branches `tmp-red-click` and `tmp-red-click2`.
- Possible flake: `CombatTelemetryTest.theTruthBehindAFightIsInternalOnly` failed once locally, passed on rerun. If it recurs, pin the random generator; do not loosen the assertion.

## Final deliverable
When the queue is finished, write ONE concise morning report: stable build and commit, completed work, tests passed or failed, bugs fixed, backend architecture and code, phone-check items, anything stopped or deferred and why, recommended next step.
