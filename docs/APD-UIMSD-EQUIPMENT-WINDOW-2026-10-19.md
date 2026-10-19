# UIMSD equipment-following damage milestone

Status: implemented, final acceptance pending. This is a subset of connected capabilities 1 and 2; it does not complete any of the five larger capabilities.

The native question builder for Equipment, Equipment row actions and Research lab offer an occurrence and explicit N-turn window. The interpreted question excludes the equip turn and includes start+N. Only same-run, exact recorded hero-target displayed damage contributes. Shield absorption is included. Later equipment changes do not terminate this fixed window. Filters select the anchor; dataset-specific Equipment predicates are not silently applied to Damage.

The observed sum is always marked as a lower bound. Zero observed does not establish no damage happened. Unknown clocks, targets and amounts are excluded explicitly; unavailable schemas yield a reason, not an answer. Overflow fails closed. The two evidence lists retain anchor and contributing damage, ordinary row/run links, Back state, exports and QueryCodec-compatible output queries.

No raw log schema, save format, RNG, recording volume, outcomes, scoring or old history is changed. No migration required. Named/general sequence questions, translated terminology registry, complete death context, named experiments and progress remain separate pending work in the tracked checklist.

Verification required: 1,954 core tests, 49 Android methods, 65 screenshots, matched uniquely numbered APKs, all previous history/signing checks. Core includes real RunLogWriter close/reopen/DatasetStore path and a non-mutating timing/storage probe. Android includes actual binary writer -> reload -> pointer menu selection -> visible sum -> damage evidence -> Back -> repeated window selection and menu restoration. Emulator verification will not be described as physical-device testing.
