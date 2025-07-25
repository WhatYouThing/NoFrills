package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
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

                .option(LabelOption.create(Text.of("Highlights")))

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

                .option(LabelOption.create(Text.of("Reminders")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Wish Reminder"))
                        .description(OptionDescription.of(Text.of("Get notified when to use your Wish ultimate while playing as Healer in F7/M7.")))
                        .binding(false, () -> Config.wishReminder, value -> Config.wishReminder = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Blood Camp Reminder"))
                        .description(OptionDescription.of(Text.of("Get notified when its time to start camping Blood Room while playing as Mage. This option is generally useless in slow and/or low cata level parties.")))
                        .binding(false, () -> Config.campReminder, value -> Config.campReminder = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("M5 Ragnarock Reminder"))
                        .description(OptionDescription.of(Text.of("Get notified when its time to use your Ragnarock in M5 as Mage. This option is useless if you are (somehow) not playing LCM.")))
                        .binding(false, () -> Config.ragAxeReminder, value -> Config.ragAxeReminder = value)
                        .controller(Config::booleanController)
                        .build())

                .option(LabelOption.create(Text.of("Leap Overlay")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Leap Overlay"))
                        .description(OptionDescription.of(Text.of("Replaces the Spirit Leap/Infinileap menu with a custom version, similar to the Odin mod for 1.8.9. Sorts players alphabetically by class first, and name second.")))
                        .binding(false, () -> Config.leapOverlay, value -> Config.leapOverlay = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Leap Message"))
                        .description(OptionDescription.of(Text.of("Sends a message in party chat when you leap to a teammate. Requires the Leap Overlay.")))
                        .binding(false, () -> Config.leapOverlayMsg, value -> Config.leapOverlayMsg = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Healer Color"))
                        .description(OptionDescription.of(Text.of("The color used for the Healer class on the Leap Overlay.")))
                        .binding(new Color(0xecb50c),
                                () -> Config.leapColorHealer,
                                value -> Config.leapColorHealer = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(false))
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Mage Color"))
                        .description(OptionDescription.of(Text.of("The color used for the Mage class on the Leap Overlay.")))
                        .binding(new Color(0x1793c4),
                                () -> Config.leapColorMage,
                                value -> Config.leapColorMage = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(false))
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Berserk Color"))
                        .description(OptionDescription.of(Text.of("The color used for the Berserk class on the Leap Overlay.")))
                        .binding(new Color(0xe7413c),
                                () -> Config.leapColorBers,
                                value -> Config.leapColorBers = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(false))
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Archer Color"))
                        .description(OptionDescription.of(Text.of("The color used for the Archer class on the Leap Overlay.")))
                        .binding(new Color(0x4a14b7),
                                () -> Config.leapColorArch,
                                value -> Config.leapColorArch = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(false))
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Tank Color"))
                        .description(OptionDescription.of(Text.of("The color used for the Tank class on the Leap Overlay.")))
                        .binding(new Color(0x768f46),
                                () -> Config.leapColorTank,
                                value -> Config.leapColorTank = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(false))
                        .build())

                .option(LabelOption.create(Text.of("F6")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Gyro Timer"))
                        .description(OptionDescription.of(Text.of("Renders respawn timers for every Terracotta while playing on F6/M6. Additionally renders a timer on screen for the initial wave of Terracottas, and for when Sadan's ultimate giant is going to spawn.")))
                        .binding(false, () -> Config.gyroTimer, value -> Config.gyroTimer = value)
                        .controller(Config::booleanController)
                        .build())

                .option(LabelOption.create(Text.of("F7")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Solve Terminals"))
                        .description(OptionDescription.of(Text.of("Solves (most of) the F7/M7 terminals for you, turning them into a simple point and click minigame. This option also hides item tooltips in every terminal for better visibility.\n\nPro tip: Lime Concrete means left click, and Blue Concrete means right click in the \"Change all to same color!\" terminal.")))
                        .binding(false, () -> Config.solveTerminals, value -> Config.solveTerminals = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Fast Terminals"))
                        .description(OptionDescription.of(Text.of("Replaces your left clicks with middle clicks while in any terminal, slightly reducing the delay until you can click on another element.")))
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
                        .name(Text.of("Dragon Alert"))
                        .description(OptionDescription.of(Text.of("Notifies you when a dragon is about to spawn in M7, while also accounting for the dragon priority on the initial double spawn.")))
                        .binding(false, () -> Config.dragAlert, value -> Config.dragAlert = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Float>createBuilder()
                        .name(Text.of("Split Power"))
                        .description(OptionDescription.of(Text.of("The minimum Power blessing level you must have in order to perform the split. Set to 0 if you wish to always attempt the split, which is ideal for party finder teams.")))
                        .binding(22.0f, () -> Config.dragSkip, value -> Config.dragSkip = value)
                        .controller(option -> Config.floatSliderController(option, 0.0f, 32.0f, 0.5f))
                        .build())

                .option(Option.<Float>createBuilder()
                        .name(Text.of("Easy Split Power"))
                        .description(OptionDescription.of(Text.of("The minimum Power blessing level you must have in order to perform the split when one of the dragons is Purple.")))
                        .binding(19.0f, () -> Config.dragSkipEasy, value -> Config.dragSkipEasy = value)
                        .controller(option -> Config.floatSliderController(option, 0.0f, 32.0f, 0.5f))
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Dragon Timer"))
                        .description(OptionDescription.of(Text.of("Renders a TPS synced timer for exactly when a M7 dragon is supposed to spawn.")))
                        .binding(false, () -> Config.dragTimer, value -> Config.dragTimer = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Dragon Health"))
                        .description(OptionDescription.of(Text.of("Renders the exact health of the M7 dragons.")))
                        .binding(false, () -> Config.dragHealth, value -> Config.dragHealth = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Dragon Boxes"))
                        .description(OptionDescription.of(Text.of("Renders the \"kill areas\" for every spawning/alive dragon in M7.")))
                        .binding(false, () -> Config.dragBoxes, value -> Config.dragBoxes = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Dragon Glow"))
                        .description(OptionDescription.of(Text.of("Applies a glow effect to every spawned M7 dragon.")))
                        .binding(false, () -> Config.dragGlow, value -> Config.dragGlow = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Dragon Stack"))
                        .description(OptionDescription.of(Text.of("Renders a waypoint for the exact spawn position of every spawning M7 dragon, showing you where to aim.")))
                        .binding(false, () -> Config.dragStack, value -> Config.dragStack = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Advanced Dragon Stack"))
                        .description(OptionDescription.of(Text.of("Replaces the spawn position waypoint from the option above with a replica of the dragon's entire hitbox, in the exact position that it will have as soon as it spawns.")))
                        .binding(false, () -> Config.dragStackAdvanced, value -> Config.dragStackAdvanced = value)
                        .controller(Config::booleanController)
                        .build())

                .option(LabelOption.create(Text.of("Misc")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Hide Mage Beam"))
                        .description(OptionDescription.of(Text.of("Prevents the Mage Beam/Staff particles from spawning, improving visibility when playing Left Click Mage.")))
                        .binding(false, () -> Config.hideMageBeam, value -> Config.hideMageBeam = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}