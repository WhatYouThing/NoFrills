package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Farming {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Farming"))

                .group(OptionGroup.createBuilder()
                        .name(Text.of("Keybinds"))
                        .collapsed(true)

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Space Farmer"))
                                .description(OptionDescription.of(Text.of("Removes the need for having to change your keybinds for farming, by making holding your space bar behave just like holding left click.\n\nSneak, and start holding space to activate. Once activated, you can stop sneaking.\n\nNote: Only works while on any of your Garden plots.")))
                                .binding(false, () -> Config.spaceFarmer, value -> Config.spaceFarmer = value)
                                .controller(Config::booleanController)
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Lock View"))
                                .description(OptionDescription.of(Text.of("Locks your camera while Space Farmer is active.")))
                                .binding(false, () -> Config.lockView, value -> Config.lockView = value)
                                .controller(Config::booleanController)
                                .build())
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Glowing Shroom Highlight"))
                        .description(OptionDescription.of(Text.of("Highlights glowing mushrooms (the ones with particles) while you are in the glowing mushroom caves.")))
                        .binding(false, () -> Config.shroomHighlight, value -> Config.shroomHighlight = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}