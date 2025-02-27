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
                        .description(OptionDescription.of(Text.of("Compacts and improves the Sky Mall new buff messages. The buff messages are hidden instead of compacted if none of your armor pieces have mining stats.")))
                        .binding(false, () -> Config.betterSkymall, value -> Config.betterSkymall = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<String>createBuilder()
                        .name(Text.of("Sky Mall Whitelist"))
                        .description(OptionDescription.of(Text.of("Allow a Sky Mall buff message to show, even if you're not wearing your mining armor, if the buff message contains one of these keywords (separated by comma, case insensitive).")))
                        .binding("titanium, goblins", () -> Config.skymallWhitelist, value -> Config.skymallWhitelist = value)
                        .controller(StringControllerBuilder::create)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Treasure Helper"))
                        .description(OptionDescription.of(Text.of("Allows you to click on treasure chests in the Crystal Hollows without having to let go of left click. This feature is only useful for powder mining with the Great Explorer HOTM perk maxed.")))
                        .binding(false, () -> Config.treasureHelper, value -> Config.treasureHelper = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}