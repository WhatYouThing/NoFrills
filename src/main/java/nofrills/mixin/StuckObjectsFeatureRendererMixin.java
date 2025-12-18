package nofrills.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.ArrowEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StuckObjectsFeatureRenderer.class)
public abstract class StuckObjectsFeatureRendererMixin {

    @Shadow
    @Final
    private Model model;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void beforeRenderFeature(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, PlayerEntityRenderState playerEntityRenderState, float f, float g, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.stuckArrows.value() && this.model instanceof ArrowEntityModel) {
            ci.cancel();
        }
    }
}