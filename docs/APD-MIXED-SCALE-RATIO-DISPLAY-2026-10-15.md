# Mixed-scale ratio presentation correction — October 15, 2026

## Reproduction

A generic UIMSD RATIO divides raw stored values. That is correct and must remain unchanged. Its presentation previously multiplied that raw ratio by 100 directly and printed raw numerator/denominator sums. When the two numeric columns used different fixed-point formats, the percentage and both displayed parts were wrong. For example, raw 250 hundredths divided by raw 1000 thousandths displayed 25% (250/1000), even though the player-facing values are 2.50 / 1.0 = 250%.

## Bounded correction

CellFormatter now applies the numerator and denominator display scales only at the RATIO presentation boundary. RATE is unchanged. Same-scale ratios remain unchanged apart from fixed-point parts being shown in their declared units. QueryEngine raw values, aggregate sorting order, filters, source rows, history, caches, saves, scores, slider rules, turns, RNG and gameplay are unchanged.

A retained regression covers hundredths/thousandths in both directions, a same-scale control, long and compact text, and confirms the raw AggValue remains unchanged. This candidate requires the full 1,637-core/debug/immediate-previous TEST206-history/28-API34/38-PNG gate before it can be called verified. It must not be claimed as part of delivered TEST204 or compiled TEST206.
