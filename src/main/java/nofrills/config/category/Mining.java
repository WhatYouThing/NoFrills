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
                        .name(Text.of("Safe Pickobulus"))
                        .description(OptionDescription.of(Text.of("Prevents you from being able to use the Pickobulus mining ability on your Private Island and Garden. Plays a note block sound effect once Pickobulus is prevented.")))
                        .binding(false, () -> Config.safePickobulus, value -> Config.safePickobulus = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Worm Cooldown"))
                        .description(OptionDescription.of(Text.of("Tracks the Worm/Scatha spawn cooldown for you while in the Crystal Hollows.")))
                        .binding(false, () -> Config.wormCooldown, value -> Config.wormCooldown = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Worm Alert"))
                        .description(OptionDescription.of(Text.of("Alerts you when your Worm/Scatha is spawned, similarly to the Scatha-Pro mod for 1.8.9.")))
                        .binding(false, () -> Config.wormAlert, value -> Config.wormAlert = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}