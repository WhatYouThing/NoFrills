package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;oPortalEffectIntensity:F"))
    private float onGetLastIntensity(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;portalEffectIntensity:F"))
    private float onGetIntensity(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float onGetFactor(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }
}