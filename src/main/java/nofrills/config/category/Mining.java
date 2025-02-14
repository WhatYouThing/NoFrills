package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Mining {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Mining"))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Corpse Highlight"))
                        .description(OptionDescription.of(Text.of("Automatically applies a glow effect to the corpses in the Glacite Mineshafts.\n\nNote: This option might be slightly risky to use, despite being a feature in Badlion and Coleweight.")))
                        .binding(false, () -> Config.miningCorpseGlow, value -> Config.miningCorpseGlow = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Ghost Vision"))
                        .description(OptionDescription.of(Text.of("Makes Ghosts way easier to see by turning them into blue blobs.")))
                        .binding(false, () -> Config.ghostVision, value -> Config.ghostVision = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Better Sky Mall"))
                        .description(OptionDescription.of(Text.of("Compacts the new buff messages from the Sky Mall HOTM perk. If you are not wearing any mining armor, the messages will be hidden instead of compacted.")))
                        .binding(false, () -> Config.betterSkymall, value -> Config.betterSkymall = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<String>createBuilder()
                        .name(Text.of("Sky Mall Whitelist"))
                        .description(OptionDescription.of(Text.of("Allow a Sky Mall buff message to show, even if you're not wearing your mining armor, if the buff message contains one of these keywords (separated by comma, case insensitive).")))
                        .binding("titanium, goblins", () -> Config.skymallWhitelist, value -> Config.skymallWhitelist = value)
                        .controller(StringControllerBuilder::create)
                        .build())

                .build();
    }
}