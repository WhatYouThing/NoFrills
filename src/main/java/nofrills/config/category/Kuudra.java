package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Kuudra {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Kuudra"))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Render Hitbox"))
                        .description(OptionDescription.of(Text.of("Renders a hitbox for the big man himself.")))
                        .binding(false, () -> Config.kuudraHitbox, value -> Config.kuudraHitbox = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Render Health"))
                        .description(OptionDescription.of(Text.of("Renders Kuudra's health percentage on screen during the third (DPS) phase.")))
                        .binding(false, () -> Config.kuudraHealth, value -> Config.kuudraHealth = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Announce Missing"))
                        .description(OptionDescription.of(Text.of("Announces in party chat if no supply spawns at either your current pre spot, or at your next spot.")))
                        .binding(false, () -> Config.kuudraMissing, value -> Config.kuudraMissing = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Pile Fix"))
                        .description(OptionDescription.of(Text.of("Allows you to use items (such as the Fire Veil Wand) while building the ballista.")))
                        .binding(false, () -> Config.kuudraPileFix, value -> Config.kuudraPileFix = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Quick Buy"))
                        .description(OptionDescription.of(Text.of("Makes upgrading much less annoying, by automatically buying the optimal upgrades (the Specialist class, and Ballista Mechanic) for you upon pressing space bar.\n\nDuring the DPS/Stunning phase, Human Cannonball is purchased over anything.")))
                        .binding(false, () -> Config.kuudraQuickBuy, value -> Config.kuudraQuickBuy = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Announce Fresh"))
                        .description(OptionDescription.of(Text.of("Sends a message when the Fresh Tools perk activates.")))
                        .binding(false, () -> Config.kuudraFresh, value -> Config.kuudraFresh = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Text.of("Fresh Message"))
                        .description(OptionDescription.of(Text.of("The message to send when Fresh Tools activates.")))
                        .binding("/pc EAT FRESH!", () -> Config.kuudraFreshMsg, value -> Config.kuudraFreshMsg = value)
                        .controller(StringControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Fresh Timer"))
                        .description(OptionDescription.of(Text.of("Render a timer on screen when Fresh Tools activates.")))
                        .binding(false, () -> Config.kuudraFreshTimer, value -> Config.kuudraFreshTimer = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Announce Drain"))
                        .description(OptionDescription.of(Text.of("Sends a message when you drain your mana using an End Stone Sword. Also hides the messages related to the ability.")))
                        .binding(false, () -> Config.kuudraDrain, value -> Config.kuudraDrain = value)
                        .controller(Config::booleanController)
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Text.of("Drain Message"))
                        .description(OptionDescription.of(Text.of("The message to send when you drain your mana. Replaces {mana} with how much mana was used, and {players} with the amount of affected players.")))
                        .binding("/pc Used {mana} Mana on {players} players!", () -> Config.kuudraDrainMsg, value -> Config.kuudraDrainMsg = value)
                        .controller(StringControllerBuilder::create)
                        .build())

                .build();
    }
}