package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.LightmapTextureManager;
import nofrills.features.general.Fullbright;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;ambientLight()F"))
    private float getAmbientLight(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.modes.Ambient)) {
            return Fullbright.ambient;
        }
        return original;
    }

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    private float getGamma(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.modes.Gamma)) {
            return Fullbright.gamma;
        }
        return original;
    }
}
