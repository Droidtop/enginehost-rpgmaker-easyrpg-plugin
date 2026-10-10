# enginehost EasyRPG plugin

This fork is the independently installable RPG Maker 2000/2003 runtime in
enginehost's shared `rpgmaker` family. The upstream-facing `master` branch
continues to follow EasyRPG Player. `plugin-core` contains the portable Android
contract changeset; release branches start at an EasyRPG revision and merge
that changeset.

The game runs in Enginehost's sandbox (`isolatable`, Enginehost
`docs/engine-sandbox.md`, "Engines with a libretro core"): the Player's own
libretro core (`PLAYER_TARGET_PLATFORM=libretro`), Enginehost's libretro
frontend and its file layer (`plugin-native/`, copied verbatim from
Enginehost) are one library, `enginehost/android/CMakeLists.txt`.
`EasyRpgPlugin` validates the context and hands the core the game folder in
place, plus the command line the Player always took, built from the game's
options (save path, encoding, RTP path, soundfont, fonts, volumes,
compatibility patches, test play, title visibility), through the core's
`easyrpg_enginehost_arguments` variable (`src/platform/libretro/ui.cpp`). Saves
default to the game folder (the bundle declares `writesGameFolder`). Inside
the sandbox only the game and save folders can be read, so an RTP, font or
soundfont option pointing elsewhere is not reachable yet.

EasyRPG Player is GPL-3.0-or-later. The enginehost integration is distributed
under the same terms and preserves upstream authorship and `COPYING`.
