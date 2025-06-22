package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static nofrills.Main.Config;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @ModifyExpressionValue(method = "renderLabelIfPresent", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;sneaking:Z"))
    private boolean isSneaking(boolean original, S state) {
        if (Config.keepNametags() && state instanceof PlayerEntityRenderState) {
            return false;
        }
        return original;
    }
}
