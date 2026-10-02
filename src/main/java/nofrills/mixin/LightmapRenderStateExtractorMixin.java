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

    @org.spongepowered.asm.mixin.injection.Inject(method = "extract", at = @At("TAIL"))
    private void getAmbientLight(net.minecraft.client.renderer.state.LightmapRenderState state,
            float partialTick, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Ambient)) {
            state.ambientColor = AMBIENT_LIGHT_COLOR;
        }
    }

    @ModifyExpressionValue(method = "extract", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 0))
    private static float getGamma(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Gamma)) {
            return 1600.0f;
        }
        return original;
    }
}
