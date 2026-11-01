package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import nofrills.features.general.Fullbright;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeProbe;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;F)Ljava/lang/Object;"))
    private static <Value> Value getAmbientLight(EnvironmentAttributeProbe instance, EnvironmentAttribute<Value> attribute, float partialTicks, Operation<Value> original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Ambient) && attribute == EnvironmentAttributes.AMBIENT_LIGHT_COLOR) {
            return (Value) LightmapRenderStateExtractor.WHITE;
        }
        return original.call(instance, attribute, partialTicks);
    }

    @ModifyExpressionValue(method = "extract", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 0))
    private static float getGamma(float original) {
        if (Fullbright.instance.isActive() && Fullbright.mode.value().equals(Fullbright.Mode.Gamma)) {
            return 1600.0f;
        }
        return original;
    }
}
