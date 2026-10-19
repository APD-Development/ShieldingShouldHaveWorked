# Native UIMSD charts

Status: implemented, exact-source verification pending.

Options > Charts opens a scrollable bar chart of the authorized grouped query result. Choose count or an aggregate already offered by the table; tap a bar for the same group detail sheet as the table. The chart preserves scope, filters, ordering, disclosure and provenance. Counts and aggregates retain their own formatted text, unknown values have no bar, lower bounds retain their marks, and small samples are explicitly labeled. The display caps at 24 groups and states the omission count without manufacturing an Other group.

Geometry uses a shared zero baseline, supports negative values and normalizes before subtracting to avoid extreme-span overflow. The selected measure has a wrapping heading; all group labels, values and details wrap inside the clipped native ScrollPane. The restore hook retains the selected measure and clamped vertical offset. Group clicks reject stale result identity before opening details.

Four new core tests cover signed geometry, zero, extreme values and invalid/unknown inputs. The existing five chart-model tests remain. New Android acceptance opens Charts from Options through native pointer input, changes measures twice, checks lower-bound and unknown results, records two screenshots, restores scroll and taps a scrolled group. It asserts query identity throughout. Full verification is pending; no physical-phone claim.

No gameplay, score, raw-history, saved-view or save-format changes. TEST291 remains the verified fallback. The next paired APK must retain all previous Changes entries, signing and application identity.
