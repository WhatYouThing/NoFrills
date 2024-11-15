package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Mining {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Mining"))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Corpse Highlight"))
                        .description(OptionDescription.of(Text.of("Automatically applies a glow effect to the corpses in the Glacite Mineshafts.\n\nNote: This option might be slightly risky to use, despite being added to Badlion.")))
                        .binding(false, () -> Config.miningCorpseGlow, value -> Config.miningCorpseGlow = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}