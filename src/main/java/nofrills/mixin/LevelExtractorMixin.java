package nofrills.mixin;

import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Entity visibility extraction moved out of LevelRenderer in 26.3. */
@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
    @Inject(method = "isEntityVisible", at = @At("HEAD"), cancellable = true)
    private void beforeRenderEntity(Entity entity, Frustum culler, double x, double y, double z,
            float partialTick, long fadeInTime, CallbackInfoReturnable<Boolean> cir) {
        if (NoRender.instance.isActive() && NoRender.shouldCancelRender(entity)) cir.setReturnValue(false);
    }
}
