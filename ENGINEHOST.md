# enginehost EasyRPG plugin

This fork is the independently installable RPG Maker 2000/2003 runtime in
enginehost's shared `rpgmaker` family. The upstream-facing `master` branch
continues to follow EasyRPG Player. `plugin-core` contains the portable Android
contract changeset; release branches start at an EasyRPG revision and merge
that changeset.

The wrapper validates the requested context and existing game directory, then
uses EasyRPG Player's established command-line interface to launch the game in
place. It bypasses the folder browser and does not copy the game. Engine-owned
options currently include save path, encoding, RTP path, soundfont, font path,
test-play mode, and title visibility. Configuration and logs default to the
plugin's private storage; saves default to the game folder unless configured.

EasyRPG Player is GPL-3.0-or-later. The enginehost integration is distributed
under the same terms and preserves upstream authorship and `COPYING`.
