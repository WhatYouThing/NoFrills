package nofrills.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StuckInBodyLayer.class)
public abstract class StuckInBodyLayerMixin {

    @Shadow
    @Final
    private Model<?> model;

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void beforeRenderFeature(PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, int i, AvatarRenderState playerEntityRenderState, float f, float g, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.stuckArrows.value() && this.model instanceof ArrowModel) {
            ci.cancel();
        }
    }
}