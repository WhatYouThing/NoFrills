package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import nofrills.features.general.Fullbright;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {

    @Unique
    @Final
    private static Vector3f AMBIENT_LIGHT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);

    @ModifyExpressionValue(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;vector3fFromRGB24(I)Lorg/joml/Vector3f;", ordinal = 2))
    private static Vector3f getAmbientLight(Vector3f original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Ambient)) {
            return AMBIENT_LIGHT_COLOR;
        }
        return original;
    }

    @ModifyExpressionValue(method = "extract", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 0))
    private static float getGamma(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Gamma)) {
            return 1600.0f;
        }
        return original;
    }
}
