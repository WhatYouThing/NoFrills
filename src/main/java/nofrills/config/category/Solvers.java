package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Solvers {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Solvers"))

                .option(LabelOption.create(Text.of("Experimentation Table")))

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Solve Chronomatron"))
                        .description(OptionDescription.of(Text.of("Highlights correct clicks and prevents wrong clicks in the Chronomatron add-on experiment.")))
                        .binding(false, () -> Config.solveChronomatron, value -> Config.solveChronomatron = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Solve Ultrasequencer"))
                        .description(OptionDescription.of(Text.of("Reveals the solution and prevents the glass panes from being clicked in the Ultrasequencer add-on experiment.")))
                        .binding(false, () -> Config.solveUltrasequencer, value -> Config.solveUltrasequencer = value)
                        .controller(Config::booleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Solve Superpairs"))
                        .description(OptionDescription.of(Text.of("Reveals every uncovered reward in the Superpairs experiment.")))
                        .binding(false, () -> Config.solveSuperpairs, value -> Config.solveSuperpairs = value)
                        .controller(Config::booleanController)
                        .build())

                .build();
    }
}