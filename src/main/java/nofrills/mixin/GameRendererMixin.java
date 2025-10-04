package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.GameRenderer;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;lastNauseaIntensity:F"))
    private float onGetLastIntensity(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;nauseaIntensity:F"))
    private float onGetIntensity(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEffectFadeFactor(Lnet/minecraft/registry/entry/RegistryEntry;F)F"))
    private float onGetFactor(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }
}