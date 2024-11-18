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
                        .name(Text.of("Highlight Boss"))
                        .description(OptionDescription.of(Text.of("Render hitboxes for all of your slayer bosses.\n\nFor Inferno Demonlord, the highlight color depends on the required attunement.")))
                        .binding(false, () -> Config.slayerHitboxes, value -> Config.slayerHitboxes = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Kill Timer"))
                        .description(OptionDescription.of(Text.of("Tracks how long it takes to finish your slayer quests, and shows the exact time in the chat.")))
                        .binding(false, () -> Config.slayerKillTime, value -> Config.slayerKillTime = value)
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
                                .description(OptionDescription.of(Text.of("Displays the status (countdown, hits) of your fire pillars on screen. This option might false alarm when another player is fighting their boss near you.")))
                                .binding(false, () -> Config.slayerBlazePillarWarn, value -> Config.slayerBlazePillarWarn = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Riftstalker Bloodfiend"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Ice Indicator"))
                                .description(OptionDescription.of(Text.of("Shows a timer on screen when your boss is about to use Twinclaws, so that you know when to use Holy Ice.")))
                                .binding(false, () -> Config.slayerVampIndicatorIce, value -> Config.slayerVampIndicatorIce = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Steak Indicator"))
                                .description(OptionDescription.of(Text.of("Shows text on screen when you are able to use the steak to finish off your boss. The ice indicator takes priority over this option.")))
                                .binding(false, () -> Config.slayerVampIndicatorSteak, value -> Config.slayerVampIndicatorSteak = value)
                                .controller(Config::booleanController)
                                .build())
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
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Replace Springs"))
                                .description(OptionDescription.of(Text.of("Replaces the Killer Springs sound effect with a working version of the Wither spawn noise. Requires the option above.")))
                                .binding(false, () -> Config.slayerVampSpringReplace, value -> Config.slayerVampSpringReplace = value)
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