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
                        .name(Text.of("Hide Flying Logs"))
                        .description(OptionDescription.of(Text.of("Hides the flying log and leaves blocks that appear when chopping trees down on Galatea.")))
                        .binding(false, () -> Config.hideFlyingLogs, value -> Config.hideFlyingLogs = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Invisibug Highlight"))
                        .description(OptionDescription.of(Text.of("Attempts to locate nearby Invisibugs on the Galatea, and renders red highlights over them.")))
                        .binding(false, () -> Config.invisibugHighlight, value -> Config.invisibugHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}