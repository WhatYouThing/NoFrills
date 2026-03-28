package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.Lightmap;
import nofrills.features.general.Fullbright;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Lightmap.class)
public abstract class LightmapMixin {

    @ModifyExpressionValue(method = "getBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;ambientLight()F"))
    private static float getAmbientLight(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Ambient)) {
            return Fullbright.ambient;
        }
        return original;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/LightmapRenderState;brightness:F", ordinal = 0))
    private static float getGamma(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Gamma)) {
            return Fullbright.gamma;
        }
        return original;
    }
}
