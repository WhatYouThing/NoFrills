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
                        .name(Text.of("Highlight Color"))
                        .description(OptionDescription.of(Text.of("The color used for the starred mob outlines.")))
                        .binding(new Color(0, 255, 255, 255),
                                () -> Config.starredMobColor,
                                value -> Config.starredMobColor = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                        .build())

                .build();
    }
}