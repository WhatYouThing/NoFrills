package nofrills.mixin.external;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.wispforest.owo.config.ConfigWrapper;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;

@Mixin(ConfigWrapper.class)
public class ConfigWrapperMixin {
    @Shadow
    @Final
    protected String name;

    @ModifyReturnValue(method = "fileLocation", at = @At("RETURN"), remap = false)
    private Path getPath(Path original) {
        if (this.name.equals("NoFrillsConfig")) {
            return FabricLoader.getInstance().getConfigDir().resolve("NoFrills/Config.json");
        }
        return original;
    }
}
