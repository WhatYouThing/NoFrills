package nofrills.config.category;

import dev.isxander.yacl3.api.*;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.hud.HudEditorScreen;

import static nofrills.Main.mc;

public class Hud {
    private static final OptionDescription leftHandText = OptionDescription.of(Text.of("Set this hud element to display text going from the right to the left, preventing it from expanding outside of the screen. Only enable this if you intend on moving this element to the right side of your screen."));
    private static final OptionDescription textPosX = OptionDescription.of(Text.of("The X position of this element, as a percentage of your horizontal resolution."));
    private static final OptionDescription textPosY = OptionDescription.of(Text.of("The Y position of this element, as a percentage of your vertical resolution."));

    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("HUD"))

                .option(LabelOption.create(Text.of("HUD Editor")))

                .option(ButtonOption.createBuilder()
                        .name(Text.literal("Open HUD Editor"))
                        .text(Text.literal("Click..."))
                        .description(OptionDescription.of(Text.of("Opens the NoFrills HUD editor screen, which allows you to drag and drop any enabled elements.\n\nPro tip: The HUD editor can be accessed in-game with /nf hudEditor")))
                        .action((screen, opt) -> mc.setScreen(new HudEditorScreen()))
                        .build())

                .option(LabelOption.create(Text.of("Fishing Bobber")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable"))
                        .description(OptionDescription.of(Text.of("Enables the hud element which displays the state of your fishing bobber. Additionally, this element will tell you when to reel in as long as you have \"Fishing Status Holograms\" and \"Fishing Timer\" enabled in the SkyBlock settings.")))
                        .binding(false, () -> Config.bobberEnabled, value -> Config.bobberEnabled = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Left Hand"))
                        .description(leftHandText)
                        .binding(false, () -> Config.bobberLeftHand, value -> Config.bobberLeftHand = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("X Position"))
                        .description(textPosX)
                        .binding(0.01, () -> Config.bobberPosX, value -> Config.bobberPosX = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("Y Position"))
                        .description(textPosY)
                        .binding(0.1, () -> Config.bobberPosY, value -> Config.bobberPosY = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(LabelOption.create(Text.of("Sea Creatures")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable"))
                        .description(OptionDescription.of(Text.of("Enables the hud element which displays the amount of alive sea creatures. Requires Track Cap to be enabled under the Fishing category.")))
                        .binding(false, () -> Config.seaCreaturesEnabled, value -> Config.seaCreaturesEnabled = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Left Hand"))
                        .description(leftHandText)
                        .binding(false, () -> Config.seaCreaturesLeftHand, value -> Config.seaCreaturesLeftHand = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("X Position"))
                        .description(textPosX)
                        .binding(0.01, () -> Config.seaCreaturesPosX, value -> Config.seaCreaturesPosX = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("Y Position"))
                        .description(textPosY)
                        .binding(0.13, () -> Config.seaCreaturesPosY, value -> Config.seaCreaturesPosY = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(LabelOption.create(Text.of("TPS")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable"))
                        .description(OptionDescription.of(Text.of("Enables the hud element which displays how many ticks occurred on the server within the last second.\n\nPro tip: If the TPS value goes above 20, it means that the server is ticking faster to catch up after having a lag spike.")))
                        .binding(false, () -> Config.tpsEnabled, value -> Config.tpsEnabled = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Left Hand"))
                        .description(leftHandText)
                        .binding(false, () -> Config.tpsLeftHand, value -> Config.tpsLeftHand = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("X Position"))
                        .description(textPosX)
                        .binding(0.01, () -> Config.tpsPosX, value -> Config.tpsPosX = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("Y Position"))
                        .description(textPosY)
                        .binding(0.04, () -> Config.tpsPosY, value -> Config.tpsPosY = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(LabelOption.create(Text.of("Lag Meter")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable"))
                        .description(OptionDescription.of(Text.of("Enables the hud element which displays how long ago the last server tick occurred. Essentially a lag spike detector.")))
                        .binding(false, () -> Config.lagMeterEnabled, value -> Config.lagMeterEnabled = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Integer>createBuilder()
                        .name(Text.of("Minimum Time"))
                        .description(OptionDescription.of(Text.of("The minimum amount of time since the last server tick, in milliseconds. If the time is lower than this value, the element will remain hidden.")))
                        .binding(500, () -> Config.lagMeterMinTime, value -> Config.lagMeterMinTime = value)
                        .controller(option -> Config.intSliderController(option, 0, 10000, 50))
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Left Hand"))
                        .description(leftHandText)
                        .binding(false, () -> Config.lagMeterLeftHand, value -> Config.lagMeterLeftHand = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("X Position"))
                        .description(textPosX)
                        .binding(0.01, () -> Config.lagMeterPosX, value -> Config.lagMeterPosX = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("Y Position"))
                        .description(textPosY)
                        .binding(0.19, () -> Config.lagMeterPosY, value -> Config.lagMeterPosY = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(LabelOption.create(Text.of("Power")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable"))
                        .description(OptionDescription.of(Text.of("Enables the hud element which displays the current Power blessing level in Dungeons.")))
                        .binding(false, () -> Config.powerEnabled, value -> Config.powerEnabled = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Dungeons Only"))
                        .description(OptionDescription.of(Text.of("Hide the element while not in Dungeons.")))
                        .binding(false, () -> Config.powerDungeonsOnly, value -> Config.powerDungeonsOnly = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Left Hand"))
                        .description(leftHandText)
                        .binding(false, () -> Config.powerLeftHand, value -> Config.powerLeftHand = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("X Position"))
                        .description(textPosX)
                        .binding(0.01, () -> Config.powerPosX, value -> Config.powerPosX = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("Y Position"))
                        .description(textPosY)
                        .binding(0.16, () -> Config.powerPosY, value -> Config.powerPosY = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(LabelOption.create(Text.of("Day")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable"))
                        .description(OptionDescription.of(Text.of("Enables the hud element which displays the current Minecraft day of the server you're in.")))
                        .binding(false, () -> Config.dayEnabled, value -> Config.dayEnabled = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Left Hand"))
                        .description(leftHandText)
                        .binding(false, () -> Config.dayLeftHand, value -> Config.dayLeftHand = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("X Position"))
                        .description(textPosX)
                        .binding(0.01, () -> Config.dayPosX, value -> Config.dayPosX = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("Y Position"))
                        .description(textPosY)
                        .binding(0.07, () -> Config.dayPosY, value -> Config.dayPosY = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(LabelOption.create(Text.of("Ping")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable"))
                        .description(OptionDescription.of(Text.of("Enables the hud element which displays your current ping.")))
                        .binding(false, () -> Config.pingEnabled, value -> Config.pingEnabled = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Left Hand"))
                        .description(leftHandText)
                        .binding(false, () -> Config.pingLeftHand, value -> Config.pingLeftHand = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("X Position"))
                        .description(textPosX)
                        .binding(0.01, () -> Config.pingPosX, value -> Config.pingPosX = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .option(Option.<Double>createBuilder()
                        .name(Text.of("Y Position"))
                        .description(textPosY)
                        .binding(0.01, () -> Config.pingPosY, value -> Config.pingPosY = value)
                        .controller(option -> Config.doubleSliderController(option, 0, 1, 0.01))
                        .build())

                .build();
    }
}