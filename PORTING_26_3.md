# Minecraft 26.3 source build

This port starts from NoFrills upstream `main` at
`f58fc8cb89683a0756d5ffbe7ec76796caa2f506` and retains version 0.4.14.
The required owo port starts from its upstream `26.2` branch at
`9c9772e1728dd4c5f1178ce955d1b3747f53493a` and builds
`io.wispforest:owo-lib:0.13.1+26.3`. Both production artifacts require
Minecraft **26.3 exactly**.

The pinned toolchain is Java 25, Gradle 9.4.1, Fabric Loom 1.16.3,
Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3. Dependency versions are
recorded in `gradle.properties`.

## Build with the genuine owo source port

Use a JDK 25 installation selected through `JAVA_HOME`. From a directory
containing sibling `owo-lib` and `NoFrills` source checkouts:

```bash
mkdir -p port-dependencies
port_repo="$(pwd)/port-dependencies"
cd owo-lib
bash ./gradlew :build :publishMavenJavaPublicationToPortDependenciesRepository \
  --no-daemon --max-workers=4 \
  -PportDependencyRepository="$port_repo"
cd ../NoFrills
bash ./gradlew build --no-daemon --max-workers=4 \
  -PportDependencyRepository="$port_repo"
```

The NoFrills production artifact is `build/libs/nofrills-26.3-0.4.14.jar`.
Install the separate production owo JAR when validating this mod. The library's
local publication includes its generated dependency POM and source artifact;
it is not a replacement interface or stripped GUI implementation.

`portDependencyRepository` is an optional caller-supplied path or repository
URI. There is no hardcoded workspace path. When provided, only
`io.wispforest:owo-lib` resolves exclusively from this repository, so the build
cannot silently substitute an older library from another repository. The
existing official repositories remain the default when the property is absent.
The earlier `owo_local_repository` property is retained as an alias. After
rebuilding the same owo version, add `--refresh-dependencies` to the NoFrills
build to invalidate Gradle's cached dependency artifact.

## Behavior and configuration preservation

- Persisted keybind integers retain the historical GLFW numeric schema.
  `LegacyInput` maps only at native SDL capture, display and pressed-state
  boundaries. Left/right/middle remain persisted as 0/1/2 and map to native
  1/3/2. Keypad keys, modifiers, unknown/unbound and mouse buttons round-trip.
  SDL-only keys use a reserved `10000 + scancode` range. The existing F25 value
  is retained, although SDL exposes no corresponding physical F25 scancode.
  Container protocol button values remain the protocol's existing integers.
- World overlays use actual staged vertex buffers and prepared render types
  with the main color/depth targets, preserved draw order, transparent sorting,
  depth comparisons, full glyph/effect visitors, shadows and lighting. Buffers
  and passes have explicit lifetime management.
- HUD, screen management, entity extraction, viewmodel/equip and swing hooks
  attach to the actual 26.3 owners and methods. Drop protection cancels
  `MultiPlayerGameMode.dropItem` before prediction and packet sending while
  retaining the active-dungeon bypass and protected-stack checks.
- Particle record access preserves the prior per-axis speed logic. Color,
  session, entity and text APIs use their real replacement types.

The configuration schema and all 752 original Setting/Feature constructor keys
and default expressions compare unchanged after normalizing equivalent legacy
key constants. Features and required mixins remain enabled.

## Validation and re-audits

The production build and access-widener validation pass. The final production
JAR passes representative A/F13/keypad/modifier/unknown keyboard captures,
all three primary mouse buttons, modifier/action checks and retained vanilla
event identity. The broader legacy adapter checks cover all mapped historical
keys/buttons and save/reload round trips.

A joint static audit with owo checked 675 actual method/field/injection
descriptors with exact injection owner matching. It found no missing vanilla
targets. Four loader bootstrap calls and one Fabric-injected creative-tab method
require runtime transformation. The standalone production candidate passed cold and post-world full mixin
audits, configuration UI/input persistence, and a fresh offline world. Real GPU
fixtures then exercised the production filled-box, outline and text drawing
paths. They caught an early staged-buffer reset; after moving reset to frame
cleanup, all three render visibly and survive the post-world mixin audit.
Combined testing with the final replacement artifacts remains required before
release.

Repeat builds and the exact owner/descriptor/injection-site audit when the game,
dependencies or sources change. Re-run the full runtime mixin audit with owo
alone, NoFrills plus its required dependencies, and the intended combined pack.
Exercise the configuration UI, save/reload and input bindings, overlays,
viewmodel/equip/swing, tooltips and item protection with synthetic fixtures in
an isolated game directory. Vulkan runtime behavior remains unverified despite
the genuine backend-neutral API compiling. Hypixel-only detection, solvers and
automation need separate authorized server testing; offline success does not
verify those features. Do not copy account/session data or configure development
authentication for the offline tests.
