package nofrills.config.category;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

import java.awt.*;

public class General {
    private static final String commandInfo = "\n\n\"Automatic\" - Process the command automatically from anyone.\n\"Manual\" - Add a button in the chat which you must click to process the command.\n\"Ignore\"- Process the command only if the player is whitelisted.\n\"Disabled\" - Fully disable the specific command.\n\nTip: You can manage the whitelist and blacklist with the \"/nf party\" command. Whitelisted players always have their commands processed automatically (if not disabled), and blacklisted players always have their commands ignored.";

    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("General"))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Update Checker"))
                        .description(OptionDescription.of(Text.of("When joining Skyblock, the mod will automatically check if there is a new version available.")))
                        .binding(false, () -> Config.updateChecker, value -> Config.updateChecker = value)
                        .controller(Config::booleanController)
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Player"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Auto Sprint"))
                                .description(OptionDescription.of(Text.of("Keeps your sprint key held at all times.")))
                                .binding(false, () -> Config.autoSprint, value -> Config.autoSprint = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("No Selfie Camera"))
                                .description(OptionDescription.of(Text.of("Removes the front facing camera perspective.")))
                                .binding(false, () -> Config.noSelfieCam, value -> Config.noSelfieCam = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Terror Fix"))
                                .description(OptionDescription.of(Text.of("Replicates the behavior of old Terror armor by playing the piston sound effect on each arrow hit.")))
                                .binding(false, () -> Config.terrorFix, value -> Config.terrorFix = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Inventory"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Price Tooltips"))
                                .description(OptionDescription.of(Text.of("Adds the lowest auction house, bazaar, and attribute prices to item tooltips.\n\n\"Lowest BIN\" - The lowest available Buy It Now price for the specific item.\n\"BZ Insta-buy\" - The lowest instant buy Bazaar price for the specific item.\n\"BZ Insta-sell\" - The lowest instant sell Bazaar price for the specific item.\n\"Price for Attribute ?\" - The lowest price for the exact attribute on the item.\n\"Price for Attribute ? (?x Level ?)\" - The lowest price for the attribute, but calculated from a level below, because the exact level doesn't exist on the Auction House.\n\"Price for Roll\" - The lowest available price for the exact item + attribute combo, regardless of attribute levels.")))
                                .binding(false, () -> Config.priceTooltips, value -> Config.priceTooltips = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Hotbar Swap"))
                                .description(OptionDescription.of(Text.of("Allows you to drop down items from your inventory into the hotbar slot below them, swapping the item if needed, with Ctrl + Left Click.")))
                                .binding(false, () -> Config.hotbarSwap, value -> Config.hotbarSwap = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.of("Last Slot Override"))
                                .description(OptionDescription.of(Text.of("If an item were to be dropped down to the 9th slot, this option allows you to redirect it to another slot, as the 9th slot is always taken up by the Skyblock Menu. Set to 9 to not override.")))
                                .binding(8, () -> Config.hotbarSwapOverride, value -> Config.hotbarSwapOverride = value)
                                .controller(option -> Config.intSliderController(option, 1, 9, 1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Ignore Background"))
                                .description(OptionDescription.of(Text.of("Hides the tooltips for, and prevents clicking on items with empty names (such as the filler Glass Panes in most Skyblock GUI's). Does not activate in the Ultrasequencer add-on so that it can be completed normally.")))
                                .binding(false, () -> Config.ignoreBackground, value -> Config.ignoreBackground = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Visual"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Hide Dead Mobs"))
                                .description(OptionDescription.of(Text.of("Prevents the game from rendering dead mobs.\n\nAdditionally, this feature also tries to hide the dead mob's nametag.")))
                                .binding(false, () -> Config.hideDeadMobs, value -> Config.hideDeadMobs = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Keep Chunks"))
                                .description(OptionDescription.of(Text.of("Prevents the server from unloading chunks client-side, letting you see previously visited chunks from further away (depending on your render distance).")))
                                .binding(false, () -> Config.keepChunks, value -> Config.keepChunks = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("No Explosions"))
                                .description(OptionDescription.of(Text.of("Prevents the server from spawning any explosion particles.")))
                                .binding(false, () -> Config.noExplosions, value -> Config.noExplosions = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("No Fire Overlay"))
                                .description(OptionDescription.of(Text.of("Hides the fire overlay that blocks half of your screen while you're on fire.")))
                                .binding(false, () -> Config.noFireOverlay, value -> Config.noFireOverlay = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("No Break Particles"))
                                .description(OptionDescription.of(Text.of("Stops your game from spawning any particles for broken blocks.")))
                                .binding(false, () -> Config.noBreakParticles, value -> Config.noBreakParticles = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Overlays"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Etherwarp"))
                                .description(OptionDescription.of(Text.of("Renders an overlay for the targeted block, as you're about to use the Ether Transmission ability. Changes color depending on if the teleport can succeed or not.")))
                                .binding(false, () -> Config.overlayEtherwarp, value -> Config.overlayEtherwarp = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Wardrobe"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Wardrobe Hotkeys"))
                                .description(OptionDescription.of(Text.of("Adds number hotkeys (1-9) to the Skyblock Wardrobe, letting you easily equip the set you want.")))
                                .binding(false, () -> Config.wardrobeHotkeys, value -> Config.wardrobeHotkeys = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Hotkey Sounds"))
                                .description(OptionDescription.of(Text.of("Play a sound effect when you switch your set using the Wardrobe Hotkeys feature.")))
                                .binding(false, () -> Config.wardrobeHotkeysSound, value -> Config.wardrobeHotkeysSound = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Chat"))
                        .collapsed(true)
                        .option(LabelOption.create(Text.of("Party Chat")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Finder Options"))
                                .description(OptionDescription.of(Text.of("Adds various buttons in chat when anyone joins your party through Party Finder, such as a copy name or kick button.")))
                                .binding(false, () -> Config.partyFinderOptions, value -> Config.partyFinderOptions = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.of("Command Prefixes"))
                                .description(OptionDescription.of(Text.of("The list of prefixes used by the command options below, separated with a space. Leave empty to fully disable all party commands.")))
                                .binding("! ?", () -> Config.partyPrefixes, value -> Config.partyPrefixes = value)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Config.partyBehaviorList>createBuilder()
                                .name(Text.of("Warp Command"))
                                .description(OptionDescription.of(Text.of("Set the behavior of the \"warp\" command, which allows your party members to warp themselves into your server on demand." + commandInfo)))
                                .binding(Config.partyBehaviorList.Disabled, () -> Config.partyCmdWarp, value -> Config.partyCmdWarp = value)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(Config.partyBehaviorList.class)
                                        .formatValue(value -> Text.of(value.name())))
                                .build())
                        .option(Option.<Config.partyBehaviorList>createBuilder()
                                .name(Text.of("Transfer Command"))
                                .description(OptionDescription.of(Text.of("Set the behavior of the \"ptme\" command, which allows your party members to transfer the party to themselves on demand." + commandInfo)))
                                .binding(Config.partyBehaviorList.Disabled, () -> Config.partyCmdTransfer, value -> Config.partyCmdTransfer = value)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(Config.partyBehaviorList.class)
                                        .formatValue(value -> Text.of(value.name())))
                                .build())
                        .option(Option.<Config.partyBehaviorList>createBuilder()
                                .name(Text.of("All Invite Command"))
                                .description(OptionDescription.of(Text.of("Set the behavior of the \"allinv\" command, which allows your party members to toggle the All Invite setting on demand." + commandInfo)))
                                .binding(Config.partyBehaviorList.Disabled, () -> Config.partyCmdAllInvite, value -> Config.partyCmdAllInvite = value)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(Config.partyBehaviorList.class)
                                        .formatValue(value -> Text.of(value.name())))
                                .build())
                        .option(Option.<Config.partyBehaviorList>createBuilder()
                                .name(Text.of("Downtime Command"))
                                .description(OptionDescription.of(Text.of("Set the behavior of the \"dt\" command, letting your party members announce that they need downtime. If anyone uses the command during a Dungeon/Kuudra run, a reminder is set for the end of the run.\n\nNote: This command does nothing if set to Manual mode." + commandInfo)))
                                .binding(Config.partyBehaviorList.Disabled, () -> Config.partyCmdDowntime, value -> Config.partyCmdDowntime = value)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(Config.partyBehaviorList.class)
                                        .formatValue(value -> Text.of(value.name())))
                                .build())
                        .option(Option.<Config.partyBehaviorList>createBuilder()
                                .name(Text.of("Queue Commands"))
                                .description(OptionDescription.of(Text.of("Sets the behavior of the instance queue commands (such as \"!f7\" to enter Floor 7, or \"!k5\"/\"!t5\" to enter Infernal Kuudra), letting your party members queue for Dungeons/Kuudra without being the party leader." + commandInfo)))
                                .binding(Config.partyBehaviorList.Disabled, () -> Config.partyCmdQueue, value -> Config.partyCmdQueue = value)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(Config.partyBehaviorList.class)
                                        .formatValue(value -> Text.of(value.name())))
                                .build())
                        .option(LabelOption.create(Text.of("Waypoints")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Party Waypoints"))
                                .description(OptionDescription.of(Text.of("Automatically creates temporary waypoints for coordinates sent by your party members. Should support any known coordinate format, such as the one from Patcher.")))
                                .binding(false, () -> Config.partyWaypoints, value -> Config.partyWaypoints = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.of("Party Waypoint Duration"))
                                .description(OptionDescription.of(Text.of("The duration that the party waypoints stay rendered for, in seconds.")))
                                .binding(60, () -> Config.partyWaypointTime, value -> Config.partyWaypointTime = value)
                                .controller(option -> Config.intSliderController(option, 1, 300, 1))
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.of("Party Waypoint Color"))
                                .description(OptionDescription.of(Text.of("The color used for the party waypoints.")))
                                .binding(new Color(85, 85, 255, 170),
                                        () -> Config.partyWaypointColor,
                                        value -> Config.partyWaypointColor = value)
                                .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Viewmodel"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("No Haste"))
                                .description(OptionDescription.of(Text.of("Prevents Haste (and Mining Fatigue) from affecting your swing speed.")))
                                .binding(false, () -> Config.noHaste, value -> Config.noHaste = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.of("Swing Speed"))
                                .description(OptionDescription.of(Text.of("Allows you to set a custom hand swing speed. Set to 0 to not override.")))
                                .binding(0, () -> Config.viewmodelSpeed, value -> Config.viewmodelSpeed = value)
                                .controller(option -> Config.intSliderController(option, 0, 20, 1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Custom Viewmodel"))
                                .description(OptionDescription.of(Text.of("Main toggle for if the offset, scale and rotation values should be applied to your viewmodel.")))
                                .binding(false, () -> Config.viewmodelEnable, value -> Config.viewmodelEnable = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(LabelOption.create(Text.of("Offset")))
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("X"))
                                .description(OptionDescription.of(Text.of("The offset for your viewmodel's position along the X axis.")))
                                .binding(0.0f, () -> Config.viewmodelOffsetX, value -> Config.viewmodelOffsetX = value)
                                .controller(option -> Config.floatSliderController(option, -2.0f, 2.0f, 0.01f))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Y"))
                                .description(OptionDescription.of(Text.of("The offset for your viewmodel's position along the Y axis.")))
                                .binding(0.0f, () -> Config.viewmodelOffsetY, value -> Config.viewmodelOffsetY = value)
                                .controller(option -> Config.floatSliderController(option, -2.0f, 2.0f, 0.01f))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Z"))
                                .description(OptionDescription.of(Text.of("The offset for your viewmodel's position along the Z axis.")))
                                .binding(0.0f, () -> Config.viewmodelOffsetZ, value -> Config.viewmodelOffsetZ = value)
                                .controller(option -> Config.floatSliderController(option, -2.0f, 2.0f, 0.01f))
                                .build())
                        .option(LabelOption.create(Text.of("Scale")))
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("X"))
                                .description(OptionDescription.of(Text.of("The scale for your viewmodel along the X axis.")))
                                .binding(1.0f, () -> Config.viewmodelScaleX, value -> Config.viewmodelScaleX = value)
                                .controller(option -> Config.floatSliderController(option, 0.0f, 2.0f, 0.01f))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Y"))
                                .description(OptionDescription.of(Text.of("The scale for your viewmodel along the Y axis.")))
                                .binding(1.0f, () -> Config.viewmodelScaleY, value -> Config.viewmodelScaleY = value)
                                .controller(option -> Config.floatSliderController(option, 0.0f, 2.0f, 0.01f))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Z"))
                                .description(OptionDescription.of(Text.of("The scale for your viewmodel along the Z axis.")))
                                .binding(1.0f, () -> Config.viewmodelScaleZ, value -> Config.viewmodelScaleZ = value)
                                .controller(option -> Config.floatSliderController(option, 0.0f, 2.0f, 0.01f))
                                .build())
                        .option(LabelOption.create(Text.of("Rotation")))
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("X"))
                                .description(OptionDescription.of(Text.of("The rotation for your viewmodel along the X axis.")))
                                .binding(0.0f, () -> Config.viewmodelRotX, value -> Config.viewmodelRotX = value)
                                .controller(option -> Config.floatSliderController(option, -360.0f, 360.0f, 0.5f))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Y"))
                                .description(OptionDescription.of(Text.of("The rotation for your viewmodel along the Y axis.")))
                                .binding(0.0f, () -> Config.viewmodelRotY, value -> Config.viewmodelRotY = value)
                                .controller(option -> Config.floatSliderController(option, -360.0f, 360.0f, 0.5f))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Z"))
                                .description(OptionDescription.of(Text.of("The rotation for your viewmodel along the Z axis.")))
                                .binding(0.0f, () -> Config.viewmodelRotZ, value -> Config.viewmodelRotZ = value)
                                .controller(option -> Config.floatSliderController(option, -360.0f, 360.0f, 0.5f))
                                .build())
                        .build())

                .build();
    }
}