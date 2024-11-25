# NoFrills Mod

It's in the name. Simple and effective Hypixel Skyblock mod for modern versions (1.20+) of Minecraft.

## Features

<details>
<summary>Click to expand feature list</summary>

### General

- **Player**

    - **Auto Sprint**
    - **No Selfie Camera**: Prevents you from going into the front facing perspective when pressing F5.

- **Inventory**

    - **Hotbar Swap**: Allows you to easily move items from your inventory to your hotbar by pressing Ctrl + Left Click.
      Essentially like pressing a number key, but much simpler.
    - **Ignore Background**: Prevents you from clicking on the glass panes which act as a background in Skyblock GUI's.

- **Visual**
    - **Hide Dead Mobs**: Prevents the game from rendering dead mobs, and also hides the Skyblock name tags of dead
      mobs.
    - **Old Skins**: Makes only Steve and Alex appear as the default player skins.

- **Overlays**
    - **Etherwarp**: Highlights the block you're aiming at when trying to use the Ether Transmission ability.

- **Wardrobe**
    - **Wardrobe Hotkeys**: Adds number hotkeys (1-9) to the Wardrobe, letting you easily switch your armor sets. Also
      has an option to play a sound effect when using the feature.

- **Party**
    - **Quick Kick**: Adds a kick button in the chat when anyone joins your party through Party Finder.
    - **Party Commands**: Adds a few commands that your party members can use, customizable.

- **Viewmodel**
    - **No Haste**: Prevents Haste (and Mining Fatigue) from affecting your swing speed.
    - **Swing Speed**: Allows you to set a custom swing speed.
    - **Custom Viewmodel**: Allows you to change the position, scale and rotation of your hand.

### Fixes

- **Stonk Fix**: Removes Microsoft's accidental client-side Stonking patch, letting you do secrets almost as if you were
  on 1.8.9.
- **Old Sneak**: Changes sneaking to revert to the old eye height, and to remove the smaller hitbox mechanic.
- **Anti Swim**: Prevents the modern swimming/crawling animation from activating.
- **No Pearl Cooldown**: Removes the cooldown from Ender Pearls, letting you spam them just as if you were on 1.8.9.
- **Snow Fix**: Simulates 1.8.9 collisions for snow layers, greatly reducing lag backs in areas such as the Glacite
  Tunnels.
- **No Drop Swing**: Disables the scuffed mechanic which makes you swing your hand after dropping an item.
- **Item Count Fix**: Prevents the game from hiding item counts for unstackable items. Mostly noticeable in the Bazaar
  and the Experimentation Table.
- **Riding Camera Fix**: Gets rid of the delayed/floaty camera movement while riding any entity.

### Fishing

- **Sea Creature Cap**: Keeps track of how many sea creatures are alive, and allows you to notify yourself (and your
  party) once the cap is reached. Customizable.
- **Rare Sea Creatures**: Notify yourself (and your party) when catching any rare/profitable sea creature.
- **Mute Drake**: Prevents the Reindrake from blowing up your ears, because he's very loud on 1.20+.

### Dungeons

- **Starred Mob Highlight**: Renders hitboxes for every starred mob, making clearing much easier.

### Kuudra

- **Render Hitbox**: Renders a hitbox for Kuudra.
- **Render Health**: Renders Kuudra's exact health during the DPS phase.
- **Announce Missing**: Announces in party chat if no supply spawns at either your pre, or at your next spot.
- **Pile Fix**: Allows you to use your abilities (such as Fire Veil) while building the ballista.
- **Announce Fresh**: Send a message when the Fresh Tools perk activates.
- **Fresh Timer**: Shows a timer for Fresh Tools.
- **Announce Drain**: Send a message when you drain your mana with an End Stone Sword.

### Slayers

- **Highlight Boss**: Renders a hitbox for all of your slayer bosses.
- **Kill Timer**: Tracks how long it takes you to finish your slayer quest.
- **Inferno Demonlord**
    - **Hide Attunement Spam**: Hides the chat messages warning you about using the wrong attunement.
    - **Pillar Warning**: Displays the status (countdown, hits) of your fire pillars.
- **Riftstalker Bloodfiend**
    - **Ice Indicator**: Displays a timer on screen when the boss is about to use Twinclaws, so that you know when to
      use Holy Ice.
    - **Steak Indicator**: Displays text on screen when you're able to finish off the boss with your steak.
    - **Silence Mania**: Allows you to get rid of the extremely loud Mania sound effects, and optionally to replace
      them.
    - **Silence Springs**: Allows you to get rid of the buggy Killer Springs sound effects, and optionally to replace
      them.
- **Voidgloom Seraph**
    - **Hits Display**: Displays the amount of hits you must do to break the hits shield of your boss.

### Mining

- **Corpse Highlight**: Highlights every nearby corpse in the Glacite Mineshafts.

### Farming

- **Space Farmer**: Allows you to farm by holding your space bar, so that you don't have to change your keybinds
  constantly. Also has an option to lock your view while using the feature.

</details>

<details>
<summary>Click to expand command list</summary>

- **Mod Commands** (accessed under `/nofrills`, or `/nf` for short)
    - **checkUpdate**: Manually check if a new version of the mod is available for download.
    - **copyCoords**: Copy your current coordinates to your clipboard, optionally in a specific format.
    - **party**: Command for managing the `Party Commands` feature.
    - **queue**: Shortcut for the Skyblock `/joininstance` command, letting you easily start a Kuudra/Dungeon run.
    - **sendCoords**: Send your current coordinates in the chat, optionally in a specific format.
    - **settings**: Opens the settings GUI (same behavior as running `/nofrills` without any arguments).
- **Other Commands** (not under `/nofrills`)
    - **/yeet**: instantly closes Minecraft.

</details>

## Installation

- Head over to the [releases tab](https://github.com/WhatYouThing/NoFrills/releases), grab the .jar file from the most
  recent release, and add it to your mods folder.
- Additional dependencies needed to launch the mod:
    - [Fabric API](https://modrinth.com/mod/fabric-api)
    - [YetAnotherConfigLib](https://modrinth.com/mod/yacl) (Optional if using Skyblocker, it has YACL bundled in)
    - [Mod Menu](https://modrinth.com/mod/modmenu) (Optional, settings can be accessed with `/nofrills`, or `/nf`)
- To finish off, open the mod's settings, and configure it to your liking. **Every feature is off by default**.

## Incompatibilities

- The mod currently has no known incompatibilities, but some features might collide with other mods. Make sure that
  you configure your mods properly.

## Credits

- [Orbit](https://github.com/MeteorDevelopment/orbit): Event system which keeps the mod blazing fast.
- [Skyblocker](https://github.com/SkyblockerMod/Skyblocker): Has more info on how YACL works than its wiki. Also
  borrowed a tiny bit of code I guess.
- [clientcommands](https://github.com/Earthcomputer/clientcommands): Taught me rendering magic with mixins.
- [ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus): Borrowed some mixins for features like Old Sneak and
  Anti Swim.