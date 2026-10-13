# Delayed/overlapping attack audio — lower-priority unconfirmed upstream report

Owner evidence, corrected scope:
- Observed in vanilla Shattered Pixel Dungeon on RedMagic11Pro, Android16, built-in speakers; no headphones.
- Several attack sounds sometimes appear absent, then play together after a delay.
- Most prominently noticed in Prison and during Tengu. These are observations, NOT boundaries; other regions were not closely observed or are not remembered.
- Other games have not shown this symptom to the owner. This does not establish a device or game root cause.
- APD is not confirmed affected; approximately one APD run has been played.
- Whether gameplay/frame rate stalls simultaneously is unknown.

Status: source/audio investigation and audible reproduction pending; no root cause, inherited-defect proof or fix claimed. Current API34 UI/core checks are not acoustic RedMagic/Android16 reproduction evidence.

Later compare actual upstream/APD sample playback, sound loading, callback/actor/render scheduling and Android lifecycle. Include Prison enemies and Tengu attacks, teleports, traps and bombs without assuming region/Tengu causation. Distinguish late triggers from on-time requests with late audible playback. Correlate frame stalls, GC and contention only with measurements. Preserve combat/damage/timing, intended simultaneous effects and the existing audio system. Do not suppress sounds merely to hide symptoms; stale-event discarding requires evidence. Keep open if unconfirmed.

Priority: below October16early-alpha sliders, save/progression stability, scoring interfaces and mandatory UIMSD configuration recording. No audio changes inTEST120.
