package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Fixes {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Fixes"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Stonk Fix"))
                        .description(OptionDescription.of(Text.of("Removes Microsoft's accidental client-side Stonking patch, letting you Stonk through blocks almost as if you were on 1.8.9.")))
                        .binding(false, () -> Config.stonkFix, value -> Config.stonkFix = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Old Sneak"))
                        .description(OptionDescription.of(Text.of("Changes sneaking to revert to the old eye height, and to remove the smaller hitbox mechanic.")))
                        .binding(false, () -> Config.oldSneak, value -> Config.oldSneak = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Anti Swim"))
                        .description(OptionDescription.of(Text.of("Prevent the modern swimming/crawling animation from activating.\n\nNote: This feature might not work properly in some cases, because for whatever reason, Hypixel itself tries to make you swim even if not fully submerged in water.")))
                        .binding(false, () -> Config.antiSwim, value -> Config.antiSwim = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("No Pearl Cooldown"))
                        .description(OptionDescription.of(Text.of("Removes the cooldown from Ender Pearls, letting you spam them just as if you were on 1.8.9.")))
                        .binding(false, () -> Config.noPearlCooldown, value -> Config.noPearlCooldown = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Snow Fix"))
                        .description(OptionDescription.of(Text.of("Simulates 1.8.9 collisions for snow layers, greatly reducing lag backs in areas such as the Glacite Tunnels.")))
                        .binding(false, () -> Config.snowFix, value -> Config.snowFix = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("No Drop Swing"))
                        .description(OptionDescription.of(Text.of("Disables the scuffed mechanic which makes you swing your hand after dropping an item.")))
                        .binding(false, () -> Config.noDropSwing, value -> Config.noDropSwing = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Item Count Fix"))
                        .description(OptionDescription.of(Text.of("Prevents the game from hiding item counts for unstackable items. Mostly noticeable in the Bazaar and the Experimentation Table.")))
                        .binding(false, () -> Config.itemCountFix, value -> Config.itemCountFix = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Riding Camera Fix"))
                        .description(OptionDescription.of(Text.of("Gets rid of the delayed/floaty camera movement while riding any entity.")))
                        .binding(false, () -> Config.ridingCamFix, value -> Config.ridingCamFix = value)
                        .controller(Config::booleanController)
                        .build())
                .build();
    }
}