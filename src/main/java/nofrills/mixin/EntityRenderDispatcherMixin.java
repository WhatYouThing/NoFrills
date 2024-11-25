package nofrills.mixin;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import nofrills.config.Config;
import nofrills.misc.EntityRendering;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void onBeforeRenderEntity(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Config.hideDeadMobs) {
            if (entity instanceof LivingEntity && !entity.isAlive()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private <E extends Entity> void onRenderEntity(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!entity.isAlive()) {
            return;
        }
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        EntityRendering rendering = (EntityRendering) entity;
        if (rendering.nofrills_mod$getRenderingOutline()) {
            RenderColor color = rendering.nofrills_mod$getOutlineColors();
            VertexConsumer buffer = vertexConsumers.getBuffer(Rendering.Layers.BoxOutline);
            WorldRenderer.drawBox(matrices, buffer, box, color.r, color.g, color.b, color.a);
        }
        if (rendering.nofrills_mod$getRenderingFilled()) {
            RenderColor color = rendering.nofrills_mod$getFilledColors();
            VertexConsumer buffer = vertexConsumers.getBuffer(Rendering.Layers.BoxFilled);
            WorldRenderer.renderFilledBox(matrices, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.r, color.g, color.b, color.a);
        }
    }
}
