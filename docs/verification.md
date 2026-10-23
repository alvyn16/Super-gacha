# Super Gacha verification

Verified on Windows with Minecraft 26.3, Java 25, Fabric Loader 0.19.5, and
Fabric API 0.164.0+26.3 after the Super Gacha branding changes.

- `gradlew.bat build runGameTest -PwithoutLootr`: passed. All four required
  server tests passed, including container hooks and loot-cache initialization.
- `gradlew.bat build`: passed with the default development dependencies, after
  isolating the vanilla server test launch from optional Lootr conversion.
- `gradlew.bat runClientGameTest -PwithoutLootr`: passed. Vanilla chest reveal,
  rendering, SPACE/ESC input, loot retention, reopening, and command rewards.
- `gradlew.bat runClientGameTest`: passed with Lootr 1.25.42.124, including
  per-player chest reveal and command inventory/overflow rewards.
- Inspected the packaged metadata: display name **Super Gacha**, port author
  **alvyn16**, original creator **XiaoJin / Wang-Xiao-Jing**, mod ID
  **gachaaddiction**, Minecraft **~26.3**, Java **>=25**.
- Verified that the new logo is bundled and game-test classes are excluded.
- `/gacha`, packet/resource identifiers, and configuration paths are retained.

The server game-test launch disables Lootr and its bundled configuration helper
so vanilla-container assertions run in the intended environment. The client
integration test runs with Lootr enabled separately. Plain `build` therefore
also runs the vanilla server tests without container conversion interference.

Artifact: `build/libs/super-gacha-1.2.0+26.3.jar`.
SHA-256: `69be1375d2403c2969f84b0d4c507bd114ad6d08df8c109077199c8d50575070`.

The [vanilla screenshot](images-vanilla-chest.png) and
[Lootr screenshot](images-lootr-chest.png) were captured from those actual game
test runs and are displayed in the main README.
