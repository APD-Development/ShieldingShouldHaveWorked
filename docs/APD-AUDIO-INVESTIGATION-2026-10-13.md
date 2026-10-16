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


## Source comparison and controlled diagnosis — October 16, 2026

APD source8d9381e5116678f51fb2f2d70b23a681102a85bd and upstream master were read directly. Their Sample.java Git blobs are identical:d83ff2e37c28cc20f6b8b97f54a56112ea97bafd. Both Android launchers select AsynchronousAndroidAudio and both builds declare libGDX1.14.0. No sound wrapper divergence establishes an APD regression.

Sample.play forwards immediately to its loaded Sound wrapper; missing IDs return-1 and are not retained for later retry. Sample has a separate intentional playDelayed collection and loads at most one queued asset per update. Game.update caps unscaled frame delta at200ms; delayed effects use scaled Game.elapsed. These behaviors alone do not establish that normal attack sounds are delayed.

The actual selected libGDX backend creates one HandlerThread named libGDX Sound Management. AsynchronousSound.play posts each operation to that shared Handler and returns0 immediately. The runnable later invokes AndroidSound.play, which calls SoundPool.play; requests have no timestamp or stale cutoff at this layer. A blocked call on the worker could therefore delay subsequent requests independently of the gameplay thread. This is a source-supported possibility, not a measured blockage on RedMagic or proof of the reported cause.

Tengu's current direct sound call sites include PUFF on jumps/teleports, BLAST on bombs, BURNING on fire effects and LIGHTNING on shockers/electricity. No Tengu-only accumulated-sound queue was found in those inspected call sites. This does not exclude other regions or general actor/render delays. Actual trigger timestamps versus SoundPool dispatch and audible output remain unmeasured.

A separate optional Android diagnostic uses the real libGDX AsynchronousSound wrapper and Android HandlerThread with a deliberately held fake Sound endpoint. It checks whether later requests return to the caller, stay queued, then dispatch in order when released. It intentionally produces no audible sound. Pending emulator execution; even a pass proves only this controlled queue mechanism, not spontaneous stalls, Prison/Tengu reproduction, latency on speakers, GC causation or an APD software defect. Production audio, effects, combat, timing and simultaneous playback remain unchanged. Do not discard or suppress sounds based on this evidence alone.

Sources: upstream SPD Sample and AndroidLauncher on master; libgdx/libgdx1.14.0 AsynchronousAndroidAudio.java, AsynchronousSound.java, AndroidSound.java and DefaultAndroidAudio.java. No actual phone is attached. The reported issue stays OPEN and unconfirmed in APD.
