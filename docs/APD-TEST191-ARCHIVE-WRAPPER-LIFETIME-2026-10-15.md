# TEST191 archive compression-wrapper lifetime — 2026-10-15

Status: exact-source candidate; hosted verification pending.

Current exporter/importer source created ZIP wrappers but never closed them, leaving inflater/deflater cleanup to garbage collection after callers separately closed Android document-provider streams. This controlled source finding is not an observed phone crash or data-loss claim.

The bounded correction closes exporter/importer-owned ZIP wrappers on every path through non-closing adapters, preserving caller ownership. Two tests cover both public caller streams and direct importer ZIP closure. Archive format/manifest, raw history, saves, permanent statistics, scoring, gameplay, application ID and signing are unchanged.

Required verification: 1,606 core tests, debug assembly, immediate-previous compiled TEST190 history, 27 API34 methods and 35 valid 320x640 screenshots. No delivery or physical-phone claim.
