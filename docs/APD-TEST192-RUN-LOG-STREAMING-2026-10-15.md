# TEST192 streaming run-log file reader — 2026-10-15

Status: exact-source candidate; hosted verification pending.

Current production `RunLogReader.read(File)` first copied the complete log into a byte array, even though the format already caps each frame payload at 16 MiB. A controlled 1 GiB sparse file with a bad byte immediately after a valid header therefore made memory cost follow the file's damaged tail rather than the valid prefix. This is a bounded source/API robustness finding, not an observed phone crash or claim about normal player log sizes.

The correction streams ordinary file reads one checksum-validated frame at a time. It keeps the existing byte-array parser for explicit resynchronization, whose scan-ahead behavior requires random access. Header and per-frame allocations remain format-bounded; decoded events and strings are still retained exactly as before. A parser-parity regression checks a valid sealed log, and a sparse-tail regression requires the reader to stop at the first bad byte while reporting the full file length and damaged-byte count.

No log format, event meaning, raw history, save, permanent statistics, scoring, gameplay, application ID, signing or release behavior changes. Required verification: 1,608 core tests, debug assembly, immediate-previous compiled TEST191 APK history, 27 API34 methods and 35 valid 320×640 screenshots. No delivery or physical-phone claim.
