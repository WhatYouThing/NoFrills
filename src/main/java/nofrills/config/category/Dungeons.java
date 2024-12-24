package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
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
                        .description(OptionDescription.of(Text.of("Solves the F7/M7 terminals for you, turning (most of) them into a simple point and click minigame. Also hides tooltips in every terminal for better visibility.\n\nThis option is still WIP, it currently only solves:\n- Correct all panes\n- Starts With\n- Click in order")))
                        .binding(false, () -> Config.solveTerminals, value -> Config.solveTerminals = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}