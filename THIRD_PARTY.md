# Third-party components

This repository is a fork of EasyRPG Player; nearly the entire tree is
upstream source and stays under the GPL (see LICENSE / COPYING; a GPL fork
cannot be relicensed).

| Component | Version / commit | Licence | Source | Where in tree |
|---|---|---|---|---|
| EasyRPG Player | this branch's line, `plugin/0.8.1.1` | GPL-3.0-or-later | https://github.com/EasyRPG/Player | entire tree (fork) |
| liblcf | external build dependency, not vendored | MIT | https://github.com/EasyRPG/liblcf | linked at build time; source not bundled in this repository |
| SDL2 / SDL3 | external build dependency, not vendored | zlib | https://www.libsdl.org | linked at build time; source not bundled in this repository |

## Obligations

As a GPL-3.0-or-later fork, the source-offer obligation applies to any binary
built from and distributed out of this repository. Enginehost satisfies it by
keeping the fork's full commit history public at the origin above.
