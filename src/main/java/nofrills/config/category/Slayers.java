package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

import java.awt.*;

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
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Hide Attunement Spam"))
                                .description(OptionDescription.of(Text.of("Hides the chat messages warning you about using the wrong attunement.")))
                                .binding(false, () -> Config.blazeNoSpam, value -> Config.blazeNoSpam = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Pillar Warning"))
                                .description(OptionDescription.of(Text.of("Displays the status (countdown, hits) of your fire pillars.\n\nThis option will try to target exclusively your own pillars, so that no false alarms occur if another player spawns a pillar nearby.")))
                                .binding(false, () -> Config.blazePillarWarn, value -> Config.blazePillarWarn = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Dagger Fix"))
                                .description(OptionDescription.of(Text.of("Fixes the Blaze slayer daggers switching their attunement twice when clicking on any block with them.")))
                                .binding(false, () -> Config.blazeDaggerFix, value -> Config.blazeDaggerFix = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Riftstalker Bloodfiend"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Ice Indicator"))
                                .description(OptionDescription.of(Text.of("Shows a timer on screen when your boss is about to use Twinclaws, so that you know when to use Holy Ice.")))
                                .binding(false, () -> Config.vampIce, value -> Config.vampIce = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Steak Indicator"))
                                .description(OptionDescription.of(Text.of("Shows text on screen when you are able to use the steak to finish off your boss. The ice indicator takes priority over this option.")))
                                .binding(false, () -> Config.vampSteak, value -> Config.vampSteak = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Steak Highlight"))
                                .description(OptionDescription.of(Text.of("Changes the highlight color of your boss to a bright red color once you're able to finish it off with your steak.")))
                                .binding(false, () -> Config.vampSteakHighlight, value -> Config.vampSteakHighlight = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Ichor Highlight"))
                                .description(OptionDescription.of(Text.of("Draws a highlight over the stupid Blood Ichor chalices, so that you can actually see it once your boss spawns one.")))
                                .binding(false, () -> Config.vampChalice, value -> Config.vampChalice = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.of("Ichor Highlight Color"))
                                .description(OptionDescription.of(Text.of("The color used for the Blood Ichor chalice highlight.")))
                                .binding(new Color(175, 0, 255, 170),
                                        () -> Config.vampChaliceColor,
                                        value -> Config.vampChaliceColor = value)
                                .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Silence Mania"))
                                .description(OptionDescription.of(Text.of("Prevents the loud Mania noise from playing. Applies to everyone's bosses, not just yours.")))
                                .binding(false, () -> Config.vampManiaSilence, value -> Config.vampManiaSilence = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Replace Mania"))
                                .description(OptionDescription.of(Text.of("Replaces the Mania noises with simple clicks when you're fighting your own boss. Requires the option above.")))
                                .binding(false, () -> Config.vampManiaReplace, value -> Config.vampManiaReplace = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Silence Springs"))
                                .description(OptionDescription.of(Text.of("Prevents the buggy Wither spawn noise from playing when your boss spawns a Killer Spring.")))
                                .binding(false, () -> Config.vampSpringSilence, value -> Config.vampSpringSilence = value)
                                .controller(Config::booleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Replace Springs"))
                                .description(OptionDescription.of(Text.of("Replaces the Killer Springs sound effect with a working version of the Wither spawn noise. Requires the option above.")))
                                .binding(false, () -> Config.vampSpringReplace, value -> Config.vampSpringReplace = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Voidgloom Seraph"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Hits Display"))
                                .description(OptionDescription.of(Text.of("Shows the amount of hits needed to break the hit shield of your boss.")))
                                .binding(false, () -> Config.emanHitDisplay, value -> Config.emanHitDisplay = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .build();
    }
}