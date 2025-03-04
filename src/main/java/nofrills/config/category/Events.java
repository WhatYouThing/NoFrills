package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

import java.awt.*;

public class Events {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Events"))

                .option(LabelOption.create(Text.of("Spooky Festival")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Chest Alert"))
                        .description(OptionDescription.of(Text.of("Show a title on screen and play a sound effect when any Trick or Treat/Party chest spawns near you during the Spooky Festival.")))
                        .binding(false, () -> Config.spookyChestAlert, value -> Config.spookyChestAlert = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Chest Highlight"))
                        .description(OptionDescription.of(Text.of("Renders a highlight for any Trick or Treat/Party chests that spawn near you during the Spooky Festival.")))
                        .binding(false, () -> Config.spookyChestHighlight, value -> Config.spookyChestHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Color>createBuilder()
                        .name(Text.of("Chest Highlight Color"))
                        .description(OptionDescription.of(Text.of("The color used for the Spooky chest highlight.")))
                        .binding(new Color(255, 170, 0, 170),
                                () -> Config.spookyChestHighlightColor,
                                value -> Config.spookyChestHighlightColor = value)
                        .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                        .build())

                .build();
    }
}