package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import nofrills.features.general.NoRender;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/OptionsRenderState;screenEffectScale:F", opcode = Opcodes.GETFIELD))
    private float onGetScreenEffectScale(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }
}