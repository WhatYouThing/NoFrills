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

    - **Slot Binding**: Allows you to bind your inventory slots to your hotbar slots to swap items between them with
      Shift + Left Click, similarly to NEU's slot binding on 1.8.9.
    - **Hotbar Swap**: Allows you to easily move items from your inventory to your hotbar by pressing Ctrl + Left Click.
      Essentially like pressing a number key, but much simpler.
    - **Ignore Background**: Prevents you from clicking on the glass panes which act as a background in Skyblock GUI's.
    - **Middle Click Override**: Replaces your left clicks with middle clicks in any applicable GUI, similarly to the
      Skytils feature.

- **Tooltips**

    - **Price Tooltips**: Adds the lowest available NPC, Motes, Auction House, and Bazaar pricing to item tooltips.

- **Visual**

    - **Hide Dead Mobs**: Prevents the game from rendering dead mobs, and also hides the Skyblock name tags of dead
      mobs.
    - **Keep Nametags**: Prevents the game from hiding the nametag of any sneaking/invisible player.
    - **No Explosions**: Block explosion particles spawned by the server (such as the large explosion from Wither
      Impact).
    - **No Fire Overlay**: Prevents the on fire overlay from rendering.
    - **No Break Particles**: Prevents any broken block particles from spawning.
    - **No Boss Bar**: Prevents the bar at the top of the screen from rendering.
    - **No Loading Screen**: Removes the world loading screen that appears when switching islands.

- **Overlays**

    - **Etherwarp**: Highlights the block you're aiming at when trying to use the Ether Transmission ability.

- **Wardrobe**

    - **Wardrobe Hotkeys**: Adds number hotkeys (1-9) to the Wardrobe, letting you easily switch your armor sets. Also
      has an option to play a sound effect when using the feature.

- **Chat**

    - **Finder Options**: Adds various buttons in the chat when anyone joins your party through Party Finder, such as
      copy name and kick.
    - **Party Commands**: Adds a few commands that your party members can use, customizable.
    - **Party Waypoints**: Automatically creates temporary waypoints for coordinates sent by your party members.
    - **Global Chat Waypoints**: Automatically creates temporary waypoints for coordinates sent in global chat.

- **Viewmodel**

    - **No Haste**: Prevents Haste (and Mining Fatigue) from affecting your swing speed.
    - **Swing Speed**: Allows you to set a custom swing speed.
    - **Custom Viewmodel**: Allows you to change the position, scale and rotation of your hand.
    - **No Equip Animation**: Disables the animation of your held item moving up after it's either changed or updated.

- **Misc**

    - **Terror Fix**: Replicates the glorious sounds of pre-nerf Terror armor.

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
- **Middle Click Fix**: Allows Pick Block (the middle mouse button) to work just as it does in 1.8.9.
- **Armor Stand Fix**: Prevents the game from unnecessarily ticking entity cramming on armor stand entities, which can
  improve performance.
- **Ability Place Fix** Prevents you from being able to place any Skyblock item that is a block and has a right click
  ability, such as the Spirit Sceptre or the Egglocator.
- **Efficiency Fix**: Fixes the efficiency enchant being lag and ping dependent, because Microsoft decided to no longer
  update your mining efficiency attribute client side.
- **Clear Cursor Stack**: Automatically clears your cursor from any ghost items that may appear between GUI changes.

### Events

- **Spooky Festival**
    - **Chest Alert**: Shows a title and plays a sound effect when a Party/Trick or Treat chest spawns nearby during the
      Spooky Festival.
    - **Chest Highlight**: Renders a highlight for nearby Party/Trick or Treat chests during the Spooky Festival.

- **Calendar**
    - **Exact Date**: Calculates the exact start date for every event in the Skyblock calendar.

### Solvers

- **Experimentation Table**
    - **Solve Chronomatron**: Highlights correct clicks and blocks wrong clicks in Chronomatron.
    - **Solve Ultrasequencer**: Highlights the solution in order and blocks wrong clicks in Ultrasequencer.
    - **Solve Superpairs**: Permanently reveals every uncovered reward in Superpairs.

### HUD

- **Fishing Bobber**: Element which displays the state of your fishing bobber.
- **Sea Creatures**: Element which displays the amount of alive sea creatures.
- **TPS**: Element which displays the amount of server ticks in the last second.
- **Lag Meter**: Element which warns you if the server is lagging.
- **Power**: Element which displays the Power blessing level in Dungeons.
- **Day**: Element which displays the current Minecraft day of the server you're on.
- **Ping**: Element which displays your ping, refreshing every second.

### Fishing

- **Track Cap**: Keeps track of how many sea creatures are alive, and allows you to notify yourself (and your
  party) once the cap is reached. Customizable.
- **Rare Sea Creatures**: Notify yourself (and your party) when catching any rare/profitable sea creature.
- **Apply Glow**: Applies a gold-colored glow to any nearby rare sea creature.
- **Mute Drake**: Prevents the Reindrake from blowing up your ears, because he's very loud on 1.20+.

### Foraging

- **Hide Tree Bits**: Hides the flying wood and leaves that appear when chopping down trees on the Galatea.
- **Invisibug Highlight**: Tries to locate Invisibugs on the Galatea and draws red highlights over them.

### Dungeons

- **Starred Mob Highlight**: Renders hitboxes for every starred mob, making clearing much easier.
- **Mini Boss Highlight**: Renders hitboxes for mini bosses.
- **Solve Terminals**: Turns (most) terminals into a fish brain point-and-click minigame.
- **Fast Terminals**: Replaces your left clicks with middle clicks while in any terminal, slightly reducing the delay
  until you can click on another element.
- **Solve Devices**: Helps you solve (most) devices on F7/M7.
- **Melody Message**: Sends a message when you get the Melody terminal.
- **Key Highlight**: Draws a box over Wither/Blood Keys to make them more visible.
- **Spirit Bow Highlight**: Highlights the Spirit Bow in F4/M4.
- **Wish Reminder**: Reminds you to wish when Maxor is enraged while playing as Healer in F7/M7.
- **Leap Overlay**: Replaces the Spirit Leap/Infinileap menu with a custom version, similar to the Odin mod for 1.8.9.
- **Leap Message**: Sends a message in party chat when you leap to a teammate. Requires the Leap Overlay.
- **Blood Camp Reminder**: Reminds you to start camping in the Blood Room when playing as Mage.
- **M5 Rag Axe Reminder**: Reminds you to use your Ragnarock Axe when playing M5 as Mage.
- **Hide Mage Beam**: Prevents the Mage Beam/Staff particles from spawning, improving visibility when playing Left Click
  Mage.
- **Dragon Alert**: Notifies you when a dragon is about to spawn in M7, while also accounting for the dragon priority on
  the initial double spawn.
- **Dragon Timer**: Renders a TPS synced timer for exactly when a M7 dragon is supposed to spawn.
- **Dragon Boxes**: Renders the kill areas for every spawning/alive dragon in M7.
- **Dragon Glow**: Applies a glow effect to every spawned M7 dragon.
- **Dragon Stack**: Renders spawn waypoints for every spawning M7 dragon, showing you where to stack your Last Breath
  arrows (or Terminator arrows as Archer).
- **F6/M6 Gyro Timer**: When playing as Mage in F6/M6, renders a timer on screen for when to use your Gyrokinetic Wand
  on the Terracottas and Sadan's ultimate giant.

### Kuudra

- **Render Hitbox**: Renders a hitbox for Kuudra.
- **Render Health**: Renders Kuudra's exact health during the DPS phase.
- **Render DPS**: Renders your team's DPS during the last phase. Infernal tier only.
- **Announce Missing**: Announces in party chat if no supply spawns at either your pre, or at your next spot.
- **Announce Fresh**: Send a message when the Fresh Tools perk activates.
- **Fresh Timer**: Shows a timer for Fresh Tools.
- **Announce Drain**: Send a message when you drain your mana with an End Stone Sword.
- **Stun Waypoint**: Renders a waypoint for the easiest to break pod while you are stunning Kuudra.
- **Supply Beacons**: Renders beacons for any nearby supply crates.
- **Drop Off Beacons**: Renders beacons for every available supply drop-off point.
- **Build Beacons**: Renders beacons for every supply pile that is not built yet.

### Slayers

- **Highlight Boss**: Renders a hitbox for all of your slayer bosses.
- **Kill Timer**: Tracks how long it takes you to finish your slayer quest.
- **Inferno Demonlord**
    - **Hide Attunement Spam**: Hides the chat messages warning you about using the wrong attunement.
    - **Pillar Warning**: Displays the status (countdown, hits) of your fire pillars.
    - **Dagger Fix**: Fixes the Blaze slayer daggers switching their attunement twice when clicking on any block with
      them.
- **Riftstalker Bloodfiend**
    - **Ice Indicator**: Displays a timer on screen when the boss is about to use Twinclaws, so that you know when to
      use Holy Ice.
    - **Steak Indicator**: Displays text on screen when you're able to finish off the boss with your steak.
    - **Ichor Highlight**: Highlights the Blood Ichors spawned by your boss.
    - **Silence Mania**: Allows you to get rid of the extremely loud Mania sound effects, and optionally to replace
      them.
    - **Silence Springs**: Allows you to get rid of the buggy Killer Springs sound effects, and optionally to replace
      them.
- **Voidgloom Seraph**
    - **Hits Display**: Displays the amount of hits you must do to break the hits shield of your boss.

### Mining

- **Corpse Highlight**: Highlights every nearby corpse in the Glacite Mineshafts.
- **Ghost Vision**: Makes Ghosts way easier to see in the Dwarven Mines.
- **Better Sky Mall**: Get Sky Mall buff messages only when you're mining. Also compacts the buff message, and mentions
  the day which the buff is for, e.g. "Sky Mall Buff for Day Summer 1st"
- **Safe Pickobulus**: Prevents you from being able to use the Pickobulus mining ability on your Private Island and
  Garden.
- **Scatha Features**: Track the worm spawn cooldown, and get alerts whenever a worm spawns.
- **Ability Alert**: Alerts you when your Pickaxe Ability is ready to be used, works with every ability.

### Farming

- **Space Farmer**: Allows you to farm by holding your space bar, so that you don't have to change your keybinds
  constantly. Also has an option to lock your view while using the feature.
- **Glowing Mushroom Highlight**: Highlights every glowing mushroom while you are in the Glowing Mushroom Caves.

### Keybinds

- **Refill Pearls**: Refills your Ender Pearls (up to 16) directly from your sacks after pressing.
- **Recipe Lookup**: Easily look up the recipe for the item you are hovering over in a GUI.
- All of these keybinds are configured with Minecraft's dedicated Key Binds menu.

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
    - **getPearls**: Same behavior as the Refill Pearls hotkey.
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

- Some highlight features might not render correctly with Iris shaders active, this might be fixed once the mod is
  updated to 1.21.7.

## Credits

- [Orbit](https://github.com/MeteorDevelopment/orbit): Event system which keeps the mod blazing fast.
- [Skyblocker](https://github.com/SkyblockerMod/Skyblocker): Has more info on how YACL works than its own wiki, and
  allowed me to borrow some obscure functions like reading the scoreboard text.
- [clientcommands](https://github.com/Earthcomputer/clientcommands): Taught me rendering magic with mixins.
- [ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus): Allowed me to borrow the mixin targets for Anti Swim and
  Old Sneak.