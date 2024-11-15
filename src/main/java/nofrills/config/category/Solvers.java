package nofrills.config.category;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import nofrills.config.Config;

public class Solvers {
    public static ConfigCategory create(Config defaults, Config config) {
        return ConfigCategory.createBuilder()
                .name(Text.of("Solvers"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Experiment Solver"))
                        .description(OptionDescription.of(Text.of("Helps you do Chronomatron, Ultrasequencer, and Superpairs without having to use your brain. ")))
                        .binding(false, () -> Config.solveExperiments, value -> Config.solveExperiments = value)
                        .controller(Config::booleanController)
                        .build())
                .build();
    }
}