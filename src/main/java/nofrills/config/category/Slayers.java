package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Slayers {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Slayers"))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Render Hitbox"))
                        .description(OptionDescription.of(Text.of("Renders a colored hitbox for all of your slayer bosses. For Inferno Demonlord, the hitbox color depends on the required attunement.")))
                        .binding(false, () -> Config.slayerHitboxes, value -> Config.slayerHitboxes = value)
                        .controller(Config::booleanController)
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Inferno Demonlord"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Hide Attunement Spam"))
                                .description(OptionDescription.of(Text.of("Hides the chat messages warning you about using the wrong attunement.")))
                                .binding(false, () -> Config.slayerBlazeNoSpam, value -> Config.slayerBlazeNoSpam = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Pillar Warning"))
                                .description(OptionDescription.of(Text.of("Displays the status (countdown, hits) of your fire pillars.")))
                                .binding(false, () -> Config.slayerBlazePillarWarn, value -> Config.slayerBlazePillarWarn = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Riftstalker Bloodfiend"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Silence Mania"))
                                .description(OptionDescription.of(Text.of("Prevents the loud Mania noise from playing. Applies to everyone's bosses, not just yours.")))
                                .binding(false, () -> Config.slayerVampManiaSilence, value -> Config.slayerVampManiaSilence = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Replace Mania"))
                                .description(OptionDescription.of(Text.of("Replaces the Mania noises with simple clicks when you're fighting your own boss. Requires the option above.")))
                                .binding(false, () -> Config.slayerVampManiaReplace, value -> Config.slayerVampManiaReplace = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Silence Springs"))
                                .description(OptionDescription.of(Text.of("Prevents the buggy Wither spawn noise from playing when your boss spawns a Killer Spring.")))
                                .binding(false, () -> Config.slayerVampSpringSilence, value -> Config.slayerVampSpringSilence = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Voidgloom Seraph"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Hits Display"))
                                .description(OptionDescription.of(Text.of("Shows the amount of hits needed to break the hit shield of your boss.")))
                                .binding(false, () -> Config.slayerEmanHitDisplay, value -> Config.slayerEmanHitDisplay = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .build();
    }
}