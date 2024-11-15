package nofrills.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import nofrills.misc.EntityRendering;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private <E extends Entity> void onRenderEntity(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        EntityRendering rendering = (EntityRendering) entity;
        if (rendering.nofrills_mod$getRenderingOutline() && entity.isAlive()) {
            float[] colors = rendering.nofrills_mod$getRenderingOutlineColors();
            WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), box, colors[0], colors[1], colors[2], colors[3]);
        }
        if (rendering.nofrills_mod$getRenderingFilled() && entity.isAlive()) {
            float[] colors = rendering.nofrills_mod$getRenderingFilledColors();
            WorldRenderer.renderFilledBox(matrices, vertexConsumers.getBuffer(Utils.renderLayers.boxFilled), box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colors[0], colors[1], colors[2], colors[3]);
        }
    }
}
