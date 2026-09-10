# Licences covering this bundle

The payload is the EasyRPG Player Android build plus the native libraries it
links. Each component keeps its own licence; nothing here relicenses anything.

| Component | Licence | Notice shipped in the payload |
| --- | --- | --- |
| EasyRPG Player (`https://github.com/EasyRPG/Player`) | GPL-3.0-or-later | `COPYING.easyrpg` |
| liblcf (`https://github.com/EasyRPG/liblcf`) | MIT | stated here; upstream ships no notice file in this tree |
| Shinonome fonts | see notice | `AUTHORS.shinonome` |
| ttyp0 font | see notice | `LICENSE.ttyp0` |

The native support libraries are produced by EasyRPG's own `buildscripts`
repository at the revision pinned in the workflow, and carry the licences of
their respective upstreams.

Because the Player is GPL-3.0-or-later, the bundle as distributed is covered by
that licence. The corresponding source is the branch this bundle was built
from, in this repository.
