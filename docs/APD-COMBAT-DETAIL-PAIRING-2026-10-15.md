# Missing combat-detail association correction — October 15, 2026

## Reproduced cause
The enemy summary looked ahead up to three events for an attack, damage or kill detail without stopping at the next public combat event. If the first detail was missing, it could borrow the next target's true class. A real-writer fixture with Rat followed by Goo made the Rat a known Goo/boss instead of leaving its identity unknown.
Original RowBuilders blob ce006da653934fd69a8379dcda0b49521a44ca9d, detailAfter line 480.
The event tables already stopped at intervening public combat events, but did not stop at a recorded GAP. If both an earlier detail and a later public event were missing, an unpaired later detail could still be assigned to the first attack.
Original EventDatasets blob fbb7618ef7aa6eb53a1bb4dfd0750837c5946b69, detailAfter line 373.

## Small correction
Both lookup paths now stop at a recording GAP; enemy summaries also stop at the same intervening ATTACK/DAMAGE/KILL/HEAL boundaries already recognized by the event tables. Genuine adjacent details remain available. Missing associations remain unknown, rather than assigning another creature's identity, boss classification or other hidden details.
Normal recorder source was checked: public and detailed attack/damage/kill events are emitted as an adjacent pair. The patch leaves unrelated noncombat metadata tolerance and the existing short lookahead limits intact.

Disposable RowBuilders identity advances13to14 so prior derived enemy/event rows rebuild. DerivedStore remains9 because run-summary arithmetic is unchanged. No raw history, log/archive/save format, score, slider, combat timing, RNG or game mechanic changes.

## Tests and evidence
Four new CombatDetailPairingTest methods fail against original source and pass after correction in0.113s. They cover attack, damage and kill identities with matching complete controls, boss classification, and a GAP with missing later public attack. Both enemy summaries and attack-event rows are checked.
Broader local suite passes639tests in21.394s, with baseline121source-guard limits. Cache fixtures retain exact current14expectations, version10rejection/rebuild and byte-identical raw-history assertions. The seven damaged-history provenance regressions and derivation9contract remain retained.
Commits8a27ced4beb4dd46e7014927bf764d1456eb60bc/03338515a319cb49ff839d169ded4649c3374c58 are production corrections;37f93ab3/f8ec76a6 retain cache expectations;9f0c273e adds four regressions.
The next meaningful exact-source gate requires1650core/debug/history from immediate previous producedTEST213(source5de956053e26b7c19860d52bddc75996037cbe01/run38773153990),28API34and38PNG. This patch is not yet fully verified, published or phone-tested.

## Previous package actually completed
TEST213 passed1646unique core and28uniqueAPI34methods,zero failures/errors/skips,debug assembly and complete immediate-previous producedTEST208history. Independent downloaded artifacts: core11992801804/SHA2560696138706fc7a748b2bb880589a36fb51157e8942cedccfd4573dc078a1a0a6;Android11992294686/SHA256d9566e712e6fd9ed17d6c5e1079604b5cf33dfd84aa1070e8d3011e84184e0df. All38PNGdecode320x640;fullcontactsheetopenedandreviewedreadable. InternalAPKartifact11992971428/ZIPdigestb2a6a8d412ed4d0d8d7ceae6a9dd1c04d112357a9e93b2b3955ad8cb6564f6f4 is not the APK payload checksum. No213signingcomparison/privatepublication/owner-save/physical-phoneclaim. PublishedTEST206 remains the owner's delivered candidate.
