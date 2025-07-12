package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Foraging {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Foraging"))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Hide Tree Bits"))
                        .description(OptionDescription.of(Text.of("Hides the flying wood and leaves blocks that appear when chopping trees down on Galatea.")))
                        .binding(false, () -> Config.hideFlyingLogs, value -> Config.hideFlyingLogs = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Lasso Alert"))
                        .description(OptionDescription.of(Text.of("Plays a sound effect when you can reel in with your lasso.")))
                        .binding(false, () -> Config.lassoAlert, value -> Config.lassoAlert = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Invisibug Highlight"))
                        .description(OptionDescription.of(Text.of("Attempts to locate nearby Invisibugs on the Galatea, and renders red highlights over them.")))
                        .binding(false, () -> Config.invisibugHighlight, value -> Config.invisibugHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Cinderbat Highlight"))
                        .description(OptionDescription.of(Text.of("Attempts to locate nearby Cinderbats on the Crimson Isle, and renders highlights and health bars for them.")))
                        .binding(false, () -> Config.cinderbatHighlight, value -> Config.cinderbatHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Fusion Keybinds"))
                        .description(OptionDescription.of(Text.of("Adds handy keybinds to the Fusion Machine.\n\nSpace: Repeat the previous fusion\nEnter: Confirm the previous fusion\nBackspace: Cancel the previous fusion.")))
                        .binding(false, () -> Config.fusionKeybinds, value -> Config.fusionKeybinds = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}