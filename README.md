# NoFrills Mod

It's in the name. Simple and effective Hypixel Skyblock mod for modern versions of Minecraft.

## Feature Highlights

- **Stonk Fix**: Remove the accidental stonking patch that Microsoft introduced in 1.19, letting you stonk through
  blocks
  just as a 1.8.9 player would.
- **Party Commands**: Adds the `warp`, `ptme`, and `allinv` commands for your party members to use, along with a player
  whitelist/blacklist system, and a manual approval mode for strangers.
- **Hotbar Swap**: Essentially allows you to have a 2nd hotbar, by simplifying the action of clicking a number key on an
  item in your inventory, to a simple Ctrl + Left Click.
- **Fishing**: Keep track of the sea creature cap, and notify yourself (and your party too) when the cap is reached.
  Also includes effects, such as sounds, for when you catch rare sea creatures.
- **Kuudra**: Various features for Kuudra, such as hitbox rendering, health rendering (during the DPS phase), fixing
  being unable to use abilities while building the ballista, and sending a message on mana drain/fresh tools activating.
- `/yeet`: Funny command that instantly closes Minecraft.
- `/nofrills sendCoords`: Patcher-like command for sending your coordinates in chat, along with an option to specify a
  different coords format.
- **Other 1.8.9 Fixes**:
    - **Old Sneak**: Reverts sneaking to use the old eye height, and to not make your hitbox smaller.
    - **Anti Swim**: Tries to prevent the swimming/crawling animation from activating. This feature is still WIP, might
      not work properly in some cases.
    - **No Pearl Cooldown**: Removes the use cooldown from Ender Pearls.
    - **Snow Fix**: Simulates old collision behavior of snow layers, making the Glacite Mineshafts much more bearable.

## Installation

- Head over to the [releases tab](https://github.com/WhatYouThing/NoFrills/releases), grab the .jar file from the most
  recent release, and add it to your mods folder.
- Additional dependencies needed to launch the mod:
    - [Fabric API](https://modrinth.com/mod/fabric-api)
    - [YetAnotherConfigLib](https://modrinth.com/mod/yacl) (Optional if using Skyblocker, it has YACL bundled in)
    - [Mod Menu](https://modrinth.com/mod/modmenu) (Optional, settings can be accessed with `/nofrills`)
- To finish off, open the mod's settings, and configure it to your liking. **Every feature is off by default**.

## Incompatibilities

- The mod currently has no known incompatibilities, but some features might collide with other mods. Make sure that
  you configure your mods properly.

## Credits

- [Orbit](https://github.com/MeteorDevelopment/orbit): Goated event system which keeps the mod blazing fast.
- [Skyblocker](https://github.com/SkyblockerMod/Skyblocker): Has more info on how YACL works than its wiki. Also
  borrowed a tiny bit of code I guess.
- [clientcommands](https://github.com/Earthcomputer/clientcommands): Taught me rendering magic with mixins.
- [ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus): Borrowed some mixins for features like Old Sneak and
  Anti Swim.