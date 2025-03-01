package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

import java.awt.*;

public class Dungeons {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Dungeons"))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Starred Mob Highlight"))
                        .description(OptionDescription.of(Text.of("Renders outlines for every starred mob, making clearing rooms much easier.")))
                        .binding(false, () -> Config.starredMobHighlight, value -> Config.starredMobHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Starred Highlight Color"))
                        .description(OptionDescription.of(Text.of("The color used for the starred mob outlines.")))
                        .binding(new Color(0, 255, 255, 255),
                                () -> Config.starredMobColor,
                                value -> Config.starredMobColor = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Miniboss Highlight"))
                        .description(OptionDescription.of(Text.of("Renders outlines for every dungeons miniboss. Takes priority over Starred Mob Highlight.")))
                        .binding(false, () -> Config.miniHighlight, value -> Config.miniHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Miniboss Highlight Color"))
                        .description(OptionDescription.of(Text.of("The color used for the miniboss outlines.")))
                        .binding(new Color(255, 255, 0, 255),
                                () -> Config.miniColor,
                                value -> Config.miniColor = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Solve Terminals"))
                        .description(OptionDescription.of(Text.of("Solves (most of) the F7/M7 terminals for you, turning them into a simple point and click minigame. This option also hides item tooltips in every terminal for better visibility.\n\nPro tip: Lime Concrete means left click, and Blue Concrete means right click in the \"Change all to same color!\" terminal.")))
                        .binding(false, () -> Config.solveTerminals, value -> Config.solveTerminals = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Fast Terminals"))
                        .description(OptionDescription.of(Text.of("Replaces your left clicks with middle clicks while in any terminal, slightly reducing the time until you can click again.")))
                        .binding(false, () -> Config.fastTerminals, value -> Config.fastTerminals = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Solve Devices"))
                        .description(OptionDescription.of(Text.of("Solves (most of) the F7/M7 devices for you.")))
                        .binding(false, () -> Config.solveDevices, value -> Config.solveDevices = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Announce Melody"))
                        .description(OptionDescription.of(Text.of("Automatically send a message in chat when you get the Melody terminal.")))
                        .binding(false, () -> Config.melodyAnnounce, value -> Config.melodyAnnounce = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<String>createBuilder()
                        .name(Text.of("Melody Message"))
                        .description(OptionDescription.of(Text.of("The message to send when a Melody terminal is opened.")))
                        .binding("/pc Melody", () -> Config.melodyMessage, value -> Config.melodyMessage = value)
                        .controller(StringControllerBuilder::create)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Key Highlight"))
                        .description(OptionDescription.of(Text.of("Renders highlights for Wither and Blood keys.")))
                        .binding(false, () -> Config.keyHighlight, value -> Config.keyHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Key Highlight Color"))
                        .description(OptionDescription.of(Text.of("The color used for the key highlight.")))
                        .binding(new Color(0, 255, 0, 128),
                                () -> Config.keyColor,
                                value -> Config.keyColor = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Spirit Bow Highlight"))
                        .description(OptionDescription.of(Text.of("Renders a highlight for the Spirit Bow in the F4/M4 boss fight.")))
                        .binding(false, () -> Config.spiritHighlight, value -> Config.spiritHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Spirit Bow Highlight Color"))
                        .description(OptionDescription.of(Text.of("The color used for the Spirit Bow highlight.")))
                        .binding(new Color(175, 0, 255, 170),
                                () -> Config.spiritColor,
                                value -> Config.spiritColor = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Wish Reminder"))
                        .description(OptionDescription.of(Text.of("Get notified when to use your Wish ultimate while playing as Healer in F7/M7.")))
                        .binding(false, () -> Config.wishReminder, value -> Config.wishReminder = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Leap Overlay"))
                        .description(OptionDescription.of(Text.of("Replaces the Spirit Leap/Infinileap menu with a custom version, similar to the Odin mod for 1.8.9.")))
                        .binding(false, () -> Config.leapOverlay, value -> Config.leapOverlay = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Blood Camp Reminder"))
                        .description(OptionDescription.of(Text.of("Get notified when its time to start camping Blood Room while playing as Mage. This option is generally useless in slow and/or low cata level parties.")))
                        .binding(false, () -> Config.campReminder, value -> Config.campReminder = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("M5 Ragnarock Axe Reminder"))
                        .description(OptionDescription.of(Text.of("Get notified when its time to use your Rag Axe in M5 as Mage. This option is useless if you are (somehow) not playing LCM.")))
                        .binding(false, () -> Config.ragAxeReminder, value -> Config.ragAxeReminder = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}