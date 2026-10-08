# APD backend architecture (foundations; read this instead of the code)

Status: contracts + tests only. NO server, NO provider, NO credentials, NO network code is in the game. Nothing in gameplay uses this package yet
(BackendFoundationsTest fails if it does, until that is a deliberate step). Code: `core/.../apd/backend/` (pure Java, imports only `java.*`,
Android API 26-safe). Tests: `core/src/test/.../apd/backend/`.

## Principles (fixed)
1. **Local-first.** Dungeon play, saves, raw run logs, UIMSD history, FloorReplay and reports stay on the device. No backend outage can stop play.
2. **The server owns shared/authoritative state:** accounts, rankings, community content + approvals, votes, developer grants, moderation, audit.
3. **The client is untrusted.** Anyone can modify the APK. Nothing authoritative is decided by, or protected by a secret in, the client.
4. **Data minimisation.** Raw history is `LOCAL_ONLY` (DataPolicy). Servers get the minimum summary a feature needs; more only if the player explicitly provides specific evidence.
5. **Provider-neutral.** One door (`Gateway`) between game and server; accounts model independent of any sign-in provider; no hosting/database/auth vendor chosen.
6. **Non-P2W.** Votes have no weights; nothing purchasable can influence rankings, votes or grants.

## Trust boundaries
| Thing | Who may create it | How the app knows |
|---|---|---|
| Run summary (`RunSubmission`) | client (a CLAIM) | server stamps `TrustLevel`; client label is always `CLIENT_CLAIM` |
| Submitter identity | server (from the authenticated session) | never a field of the submission |
| Ranking position | server only (`Leaderboard.rank`, pure function of server records, per board min trust) | server data |
| Developer achievement (`AchievementGrant`) | developer authority only, offline/server-side signer | ECDSA P-256 signature checked against `TrustRoot` public keys (purpose DEVELOPER_GRANT); gameplay code has NO signing code (architecture test greps for PrivateKey/KeyPairGenerator/initSign/...) |
| Approved content | reviewers' key (purpose CONTENT_APPROVAL) | `ContentVersion.clientMayLoad`: bytes hash == approved hash AND valid approval signature |
| Votes | server (one ballot per account per round) | tallies computed from ballots, never stored separately |
| Audit trail | server | hash chain (`AuditLog`); a published head hash makes edits/removals detectable |
`TrustLevel`: CLIENT_CLAIM < SERVER_RECEIVED < SERVER_CHECKED < REPLAY_VERIFIED; DEVELOPER_ISSUED is a separate axis. Rankings can start lenient and tighten later without changing stored records.

## What exists (all tested: BackendFoundationsTest, BackendTrustTest, BackendCommunityTest, BackendOutboxTest; 37 tests)
- `Id` typed opaque ids (player, device, run, submission, content, vote round, report, grant, achievement, audit, outbox). Existing run ids (`LogHeader.runId`) are accepted as `Id.of(RUN, ...)`.
- `Canon` canonical, unambiguous bytes for hashing/signing (type + schema version in every signature, so a signature never validates another record kind).
- `Envelope` schema + major/minor + payload. Minor = fields appended only, readers skip unknown; major = different contract, refused. Same rule as the run logs.
- `TrustLevel`, `Verifier`/`PublicKeyVerifier`, `TrustRoot` (public keys by id, per purpose, validity window, revocation, several active keys = rotation).
- `AchievementGrant` + `GrantLedger` (idempotent, only for this player, only from a trusted unrevoked key).
- `RunSubmission` (exact field set pinned by a test; finished runs only; no identity, no raw log, no hidden information) + `SubmissionPolicy` (shared device/server checks) + `RunSubmissionCodec` (key=value wire form, forward compatible).
- `Leaderboard` (board = ruleset + balance revision + difficulty + min trust; best run per player; total deterministic order).
- `ContentVersion` (immutable version with package hash, ruleset era, state table; REVOKED terminal), approval signature check.
- `Voting` (one ballot per account per round, replace until close, idempotent, no weights), `Report` (+Inbox dedupe, bounded note), `AuditLog`.
- `SyncRecord` (server version, tombstones, LAST_WRITE_WINS with deterministic tie-break, SERVER_WINS) for preferences-type data only.
- `Accounts` (player id independent of credentials; link/unlink; last credential cannot be removed; credentials never moved silently), `LocalProfile` (device id always, player id only if the player signs in).
- `DataPolicy` (every schema classified: LOCAL_ONLY / ACCOUNT_PRIVATE / PUBLIC_ON_SUBMIT; unlisted schemas cannot be sent).
- `Gateway` (OK / OFFLINE / RETRY_LATER / REJECTED, idempotency key), `Outbox` (queue; local-only refused; offline keeps items; permanent refusals dropped and explained; throwing gateway survived), `Backoff` (30 s doubling to 6 h).
- `Permission` / `Roles` (platform roles for APD staff vs group roles; every system asks for a PERMISSION, never a role name; group roles can never reach a platform permission) and `Groups` (generic group with members and roles, last-owner protection, no self-promotion, size limit, every change audited; knows nothing about what a group is for). 4 tests. Useful for creator teams, reviewer pools, moderators, mod-author circles - and a clan layer if one is ever approved.
- `OutboxStore` (queue survives restarts: atomic temp-file+move write, damaged lines skipped and counted, local-only data re-checked on load) and `OfflineGateway` (always OFFLINE: lets the plumbing ship with zero behaviour change before any server exists).
- `apd/backendbridge/RunSubmissionFactory` (the ONLY place local `RunFacts` become a `RunSubmission`: copies the fixed field set, refuses with a reason unless the run is an APD run with a complete sealed log, finished, with outcome/difficulty/balance/ruleset truly recorded - never inferred, never invented; play time stays 0 = not recorded). 3 tests. Nothing in gameplay calls it yet.
- `Evidence` (request for a run log or floor-replay data: provided only with the player's consent, before the deadline, and only if the local log still hashes to the submitted digest; server checks received bytes against that digest). 2 tests. Neutral part of decision 4.
- `apd/backendbridge/SubmissionQueue` (queues one finished run locally from its own log: single complete segment only, refuses otherwise, persists with `OutboxStore`; nothing is sent). Game-side entry: UIMSD run sheet > "Queue for submission (preview, sends nothing)", shown on INDEV builds only. A queued run can be read exactly as it would be sent ("Show what is queued for this run") and taken back ("Take this run out of the queue"): `isQueued`, `preview`, `withdraw`. `PrivacySummary` (read-only plain-language account of what is held locally and what is queued; shown in UIMSD Options > Privacy > "What stays on this device"). 4 tests.
- Test-only: `TestAuthority` (the only signer), a reference in-memory server in `BackendOutboxTest` (dedupe by idempotency key, server-attached identity and trust, lost-reply safety).

## Versioning / migration
Schemas are `apd.<name>` + major.minor. Add a field = minor bump (append only). Anything else = new major, old major served until retired. Event/fact ids in run logs are already permanent (see APD-UIMSD-UNIVERSAL.md); submissions carry telemetry version and ruleset id + balance revision so old runs stay interpretable. Trust roots are data: keys rotate/revoke without a release.

## Privacy assumptions
No hidden analytics. Nothing is sent unless the player signs in AND chooses a feature (submit, vote, publish, sync preferences). Unfinished runs are never submitted (hidden information). Reports and ballots are not public. Account deletion/export are required before any real server (see decisions).

## Not built (needs a decision or later work)
Wiring the game to the Outbox, real Gateway/transport, sign-in UI, replay-evidence protocol (what the server may ask for and the player consents to), anti-cheat beyond digest + policy, moderation tooling, cloud sync UI.

## DECISIONS FOR THE OWNER (nothing here was guessed)
1. **Hosting/database/auth provider (NOT a blocker: everything above is provider-neutral; only a concrete Gateway/transport and the real server need it)** (cost, lock-in, privacy jurisdiction). Contracts above are provider-neutral; options: (a) managed BaaS (fast, vendor lock-in), (b) small self-hosted service + SQL (portable, you operate it), (c) static-first (signed JSON files on a CDN for content/leaderboards, tiny API only for writes). Recommend deciding after (c)-style read paths are prototyped because reads are most of the load and are the cheapest to keep portable.
2. **Sign-in methods** (anonymous device account only? email? Google/Apple/Play Games?). Affects which `Credential.provider`s exist and recovery/account-merge policy. Account MERGE is intentionally unimplemented (rankings/content/votes consequences).
3. **Who holds the developer-grant and content-approval private keys** and how they are stored (offline machine / hardware key / hosted KMS). The app only ever gets the public keys.
4. **Ranking verification level for launch** (`SERVER_CHECKED` plausibility vs `REPLAY_VERIFIED`) and what evidence a player is asked to provide (full log, floor-replay seed data). Replay must stay opt-in; FloorReplay already needs a fixed reference context (see APD-FUTURE-FLOORS-FEASIBILITY.md).
5. **Retention, deletion and export policy** for submissions, profiles, votes and reports (legal/privacy).
6. **Name/profile moderation rules** (public profiles, content names) and who moderates.

## Which roadmap features this supports
Phase 11 community: accounts/profiles (Accounts, LocalProfile), voting (Voting), community content + mod downloads (ContentVersion, approval signatures, TrustRoot), leaderboards + run submission + blind/community review primitives (RunSubmission, evidenceDigest, Leaderboard, TrustLevel), developer achievements (AchievementGrant), moderation/reporting (Report, AuditLog), cross-device preferences (SyncRecord), offline-safe delivery (Outbox, Gateway, Backoff), privacy boundary (DataPolicy). Phase 10 mod support: content trust model (hash + approval) is the safety base for downloads.

## Recommended next steps
1. DONE (preview): local queue + INDEV-only run-sheet action. Next: a flush step that sends the queue through a `Gateway` (still `OfflineGateway` until a transport exists) and shows queue status in APD Settings.
2. Evidence-request protocol design (needs decision 4).
3. After decisions 1-3: first real read-only feature (e.g. signed content catalogue / grants) so the verify path is exercised with real keys.

## Clans: NOT approved, documented option only
The owner is not committed to Clans (plan 11.3-11.5 are EVENTUALLY / CONCEPT). Nothing Clan-specific is built. If Clans are ever approved, what already exists: `Groups` (membership, roles, audit), `Roles`/`Permission`, `Report`/`AuditLog` (moderation), `Voting`, `Leaderboard` (could rank a group's members' best runs on the same boards), `RunSubmission` + trust levels (same-condition events would reuse submissions with a shared seed/ruleset), `DataPolicy` (group data class would need adding). What would still be needed: a group-kind definition ("clan": naming/profanity rules, size, join policy), an event definition (shared seed + ruleset + attempt limit + scoring aggregate: needs the replay/evidence decision), group-level leaderboard boards, invitations/join requests, group moderation workflow, UI. None of it should start without approval; none of the generic parts above depends on it.
