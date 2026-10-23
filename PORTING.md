# Super Gacha — Minecraft 26.3 Fabric port

## Source and scope

Upstream: https://github.com/Wang-Xiao-Jing/GachaAddiction

Base: `fabric/26.2`, commit `329fc1eef26c1e54c92ca8296ac8270c2fa8cbfe`.
The existing Fabric implementation, translations, sounds, loot hooks, commands,
configuration, and resource-pack paths are retained.

The NeoForge main branch has additional integrations (FTB Quests, KubeJS,
RarityCore, ColorTooltips, Confluence, Treasure Bags, Loot Bundles, and LDLib2)
which are not implemented by the upstream Fabric branch or this port. Lootr is
the optional Fabric integration included here. Configuration uses the upstream
Fabric JSON file rather than the NeoForge configuration screen.

## Target toolchain

- Minecraft 26.3, Java 25
- Fabric Loader 0.19.5
- Fabric API 0.164.0+26.3
- Loom 1.17.21, Gradle 9.6.0
- Optional Lootr 1.25.42.124 (Modrinth version `gVwn7m2v`)

Minecraft's official class names are used directly; this version has no
obfuscated/intermediary mapping layer.

## Compatibility changes

- Retargeted dependencies and mod metadata to 26.3.
- Replaced GLFW key constants with vanilla `InputConstants`, compatible with
  Minecraft's SDL input backend.
- Retargeted the loot-weight accessor from the removed
  `LootPoolSingletonContainer` to `UniformContainerBase`.
- Updated command item drops to use `Prediction.SERVER_ONLY`, since rewards
  originate on the server.
- Selected a Java 25 compilation toolchain and corrected CI's Java version.
- Added an actual client/server game test for first-open loot reveal, rendering,
  skip input, loot retention, reopening, and command inventory/overflow rewards,
  runnable with and without Lootr.

## Verification

See `README.md` for build and runtime test commands. The runtime test mod lives
in `src/gametest` and is excluded from the distributed mod JAR.

Validated locally on Windows with Java 25, Minecraft 26.3, and the toolchain
listed above. `build` and the vanilla and Lootr chest runtime tests passed,
including preservation of generated loot.
Both runs also passed the `/gacha` inventory and overflow checks. A separate
Fabric SERVER-environment game-test run passed all four required tests (three
mod tests plus Fabric's built-in test), exercising loot-cache startup and hooks
for chests, barrels, dispensers, droppers, hoppers, shulker boxes, double chests,
and chest minecarts. These server tests check retained loot-table identifiers,
first-reveal state, and suppression on reopening; they do not render each GUI.
Screenshots and full logs are saved locally in `build/verification`.

The installable artifact is `build/libs/super-gacha-1.2.0+26.3.jar`.
The mod metadata declares Minecraft `~26.3`, Fabric Loader `>=0.19.5`, Fabric API,
and Java `>=25`; game-test classes are excluded from the distributed JAR.

## Super Gacha identity

Port created and maintained by **alvyn16**, based on **GachaAddiction by XiaoJin / Wang-Xiao-Jing**. See [CREDITS.md](CREDITS.md).

Super Gacha changes the display name, icon, project name, and JAR filename. The original /gacha command, gachaaddiction mod/resource/network IDs, Java packages, configuration filename, and data-pack/resource-pack interfaces stay the same. This preserves compatibility; the two mods cannot be installed together.
