package nofrills.config.category;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class General {
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
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Inventory"))
                        .collapsed(true)
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
                                .name(Text.of("Old Skins"))
                                .description(OptionDescription.of(Text.of("Forces the game to only apply either Steve or Alex as the default skin for players. Essentially removes all the random default skins added by Microsoft.")))
                                .binding(false, () -> Config.oldSkins, value -> Config.oldSkins = value)
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
                        .name(Text.of("Party"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Quick Kick"))
                                .description(OptionDescription.of(Text.of("When someone joins your party through Party Finder, a kick button for that player will be added in the chat.")))
                                .binding(false, () -> Config.partyQuickKick, value -> Config.partyQuickKick = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.of("Command Prefixes"))
                                .description(OptionDescription.of(Text.of("The list of prefixes used by the command options below, separated with a space. Leave empty to fully disable party commands.")))
                                .binding("! ?", () -> Config.partyPrefixes, value -> Config.partyPrefixes = value)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Config.partyBehaviorList>createBuilder()
                                .name(Text.of("Default Behavior"))
                                .description(OptionDescription.of(Text.of("The default behavior for when someone, who isn't on both the whitelist and blacklist, runs a command.\n\nWhitelisted players will have their commands processed automatically, and blacklisted players will have their commands ignored, regardless of this setting.\n\n\"Automatic\": Process all commands automatically, almost as if everyone were whitelisted.\n\n\"Manual\": When someone runs a command, a button will be added in the chat, which you must manually click to process the command.\n\n\"Ignore\": Ignore all commands, almost as if everyone were blacklisted.\n\nTip: You can manage the whitelist and blacklist with the \"/nofrills party\" command.")))
                                .binding(Config.partyBehaviorList.Manual, () -> Config.partyBehavior, value -> Config.partyBehavior = value)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(Config.partyBehaviorList.class)
                                        .formatValue(value -> Text.of(value.name())))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Warp Command"))
                                .description(OptionDescription.of(Text.of("Adds a \"warp\" command, letting your party members warp themselves into your lobby on demand.")))
                                .binding(false, () -> Config.partyCmdWarp, value -> Config.partyCmdWarp = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Transfer Command"))
                                .description(OptionDescription.of(Text.of("Adds a \"ptme\" command, letting your party members transfer the party to themselves on demand.")))
                                .binding(false, () -> Config.partyCmdTransfer, value -> Config.partyCmdTransfer = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("All Invite Command"))
                                .description(OptionDescription.of(Text.of("Adds a \"allinv\" command, letting your party members toggle the all invite setting on demand.")))
                                .binding(false, () -> Config.partyCmdAllInvite, value -> Config.partyCmdAllInvite = value)
                                .controller(Config::booleanController)
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