# APD title/home screen

`TitleScene` uses six APD art files (all 1x pixel art, scaled by the game's whole-number zoom):

| File | Size | Used for |
|---|---|---|
| `assets/interfaces/apd_logo.png` | 139x100 | logo with two braziers, shared by portrait and landscape |
| `assets/interfaces/apd_logo_glow.png` | 139x100 | additive pulsing crack glow, same layout as the logo |
| `assets/effects/apd_flame.png` | 256x256 | 24 flame frames, 47x47, 5x5 grid, base at (23,46) of each cell |
| `assets/interfaces/apd_button.png` | 24x24 | 9-patch button frame, 8px margins |
| `assets/splashes/title/apd_bg_port.png` | 200x360 | portrait background plate |
| `assets/splashes/title/apd_bg_land.png` | 400x200 | landscape background plate |

Layout: portrait stacks logo, version text, a 2x3 grid (Play/Statistics, Badges/Journal, Options/Quit) and a bottom row
(News/Changes/About). Landscape puts the logo on the left and the same grid on the right. Space the plate doesn't cover is
filled with `#08110D`.

- Statistics opens the Rankings screen until real APD statistics exist.
- Badges opens the journal scene on its badges tab, Journal opens it on the catalog tab.
- The Support button is not on the home screen any more.
- Version text is `v4.0.8-apd-INDEV`. Stock menu icons are reused.

Flame anchors (`LEFT_FLAME_X`, `RIGHT_FLAME_X`, `FLAME_Y` in `TitleScene`) come from the asset notes: bowl rim centres
(10,67) and (129,67) in `apd_logo.png`. These need checking on a device.

Known asset deviations from the written spec: the logo and backgrounds have thousands of colours (smooth shading instead
of about 24 flat colours), and the logo already has small static flame wisps painted above the bowls.
