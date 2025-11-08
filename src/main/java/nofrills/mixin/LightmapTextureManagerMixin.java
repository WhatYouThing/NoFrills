package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.LightmapTextureManager;
import nofrills.features.general.Fullbright;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Unique
    @Final
    float brightness = 4269.0f;

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;ambientLight()F"))
    private float getAmbientLight(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.modes.Ambient)) {
            return brightness;
        }
        return original;
    }

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    private float getGamma(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.modes.Gamma)) {
            return brightness;
        }
        return original;
    }
}
