package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Fixes {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Fixes"))
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Stonk Fix"))
                        .description(OptionDescription.of(Text.of("Removes Microsoft's accidental client-side Stonking patch, letting you Stonk through blocks almost as if you were on 1.8.9.")))
                        .binding(Config.fixModes.Disabled, () -> Config.stonkFix, value -> Config.stonkFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Old Sneak"))
                        .description(OptionDescription.of(Text.of("Changes sneaking to revert to the old eye height, and to remove the smaller hitbox mechanic.")))
                        .binding(Config.fixModes.Disabled, () -> Config.oldSneak, value -> Config.oldSneak = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Anti Swim"))
                        .description(OptionDescription.of(Text.of("Prevent the modern swimming/crawling animation from activating.\n\nNote: This feature might not work properly in some odd cases.")))
                        .binding(Config.fixModes.Disabled, () -> Config.antiSwim, value -> Config.antiSwim = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("No Pearl Cooldown"))
                        .description(OptionDescription.of(Text.of("Removes the cooldown from Ender Pearls, letting you spam them just as if you were on 1.8.9.")))
                        .binding(Config.fixModes.Disabled, () -> Config.noPearlCooldown, value -> Config.noPearlCooldown = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Snow Fix"))
                        .description(OptionDescription.of(Text.of("Simulates 1.8.9 collisions for snow layers, greatly reducing lag backs in areas such as the Glacite Tunnels.")))
                        .binding(Config.fixModes.Disabled, () -> Config.snowFix, value -> Config.snowFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("No Drop Swing"))
                        .description(OptionDescription.of(Text.of("Disables the scuffed mechanic which makes you swing your hand after dropping an item.")))
                        .binding(Config.fixModes.Disabled, () -> Config.noDropSwing, value -> Config.noDropSwing = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Item Count Fix"))
                        .description(OptionDescription.of(Text.of("Prevents the game from hiding item counts for unstackable items. Mostly noticeable in the Bazaar and the Experimentation Table.")))
                        .binding(Config.fixModes.Disabled, () -> Config.itemCountFix, value -> Config.itemCountFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Riding Camera Fix"))
                        .description(OptionDescription.of(Text.of("Gets rid of the delayed/floaty camera movement while riding any entity.")))
                        .binding(Config.fixModes.Disabled, () -> Config.ridingCamFix, value -> Config.ridingCamFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Sneak Fix"))
                        .description(OptionDescription.of(Text.of("Fixes weird behavior with sneaking, such as not having your walk speed reduced when inside of a block, and the ancient bug where the un-sneak animation plays twice when quickly tapping sneak.")))
                        .binding(Config.fixModes.Disabled, () -> Config.sneakFix, value -> Config.sneakFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Middle Click Fix"))
                        .description(OptionDescription.of(Text.of("Allows Pick Block (the middle mouse button) to work just as it does in 1.8.9.")))
                        .binding(Config.fixModes.Disabled, () -> Config.middleClickFix, value -> Config.middleClickFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Armor Stand Fix"))
                        .description(OptionDescription.of(Text.of("Prevents the game from unnecessarily processing entity cramming for every Armor Stand, which can result in reduced load and improved performance.")))
                        .binding(Config.fixModes.Disabled, () -> Config.armorStandFix, value -> Config.armorStandFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Ability Place Fix"))
                        .description(OptionDescription.of(Text.of("Prevents you from being able to place any Skyblock item that is a block and has a right click ability, such as the Spirit Sceptre or the Egglocator.")))
                        .binding(Config.fixModes.Disabled, () -> Config.abilityPlaceFix, value -> Config.abilityPlaceFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .option(Option.<Config.fixModes>createBuilder()
                        .name(Text.of("Efficiency Fix"))
                        .description(OptionDescription.of(Text.of("Fixes the efficiency enchant being lag and ping dependent, because Microsoft decided to no longer update your mining efficiency attribute client side.")))
                        .binding(Config.fixModes.Disabled, () -> Config.efficiencyFix, value -> Config.efficiencyFix = value)
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(Config.fixModes.class)
                                .formatValue(value -> Text.of(value.name())))
                        .build())
                .build();
    }
}