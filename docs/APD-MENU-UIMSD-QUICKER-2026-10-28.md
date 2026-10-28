# Faster menu returns and UIMSD drawing

Base: `326f626dbe0483a2ac8c7d6c257e066c62cf4d0b`, after accepted TEST320. Owner asked for quicker app response and explicitly resumed this work after a journal investigation.

Android retains exactly one TitleScene while visiting known non-gameplay menus. Its cameras and pointer-listener signal are detached from active dispatch; the root is inactive and buttons check hierarchical activity for keyboard input. Back destroys the child and restores the exact title, UI camera and pointer order without calling create. Titles with modal windows are not retained. Other platforms and unknown/gameplay destinations keep normal reconstruction. Callback transitions, reset, resume, context and seamless display/font/language changes discard retained graphics. The frozen TitleScene source and APD Settings are unchanged.

Pending scene requests are applied before draw, including ordinary tap requests queued by the previous update. UIMSD checks dirty ready content before draw as well as update, retaining its existing modal protection. The first complete Home draw records a timestamp for native performance acceptance; that metric includes native input, model preparation and actual UI construction/draw rather than only model handoff.

Local actual-source compile passed. Focused95 JUnit methods passed, including nine new real engine lifecycle tests and28134 query safety comparisons. Full core, native identity/input/rotation acceptance, exact matched developer/player builds, screenshots and display checks remain pending. Existing nine Back routes retain their250ms native round-trip budget and additionally require the same title/camera identity. A new test injects a real Android tap on the title UIMSD button against a separately marked72-run synthetic fixture and requires complete Home draw within250ms. No physical phone acceptance is claimed.

The single retained return applies to title destinations. Nested menu-to-menu returns continue their normal lifecycle, with pending switches now drawn earlier; no claim that every nested scene is retained. No release, main update, signing change, gameplay or real-history change.
