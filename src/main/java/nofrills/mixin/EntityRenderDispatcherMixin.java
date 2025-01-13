package nofrills.mixin;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
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
    @Inject(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V", at = @At("HEAD"), cancellable = true)
    private <E extends Entity, S extends EntityRenderState> void onBeforeRenderEntity(E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityRenderer<? super E, S> renderer, CallbackInfo ci) {
        if (Config.hideDeadMobs) {
            if (entity instanceof LivingEntity && !entity.isAlive()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private <E extends Entity, S extends EntityRenderState> void onRenderEntity(E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityRenderer<? super E, S> renderer, CallbackInfo ci) {
        if (!entity.isAlive()) {
            return;
        }
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        EntityRendering rendering = (EntityRendering) entity;
        if (rendering.nofrills_mod$getRenderingOutline()) {
            RenderColor color = rendering.nofrills_mod$getOutlineColors();
            VertexConsumer buffer = vertexConsumers.getBuffer(Rendering.Layers.BoxOutline);
            VertexRendering.drawBox(matrices, buffer, box, color.r, color.g, color.b, color.a);
        }
        if (rendering.nofrills_mod$getRenderingFilled()) {
            RenderColor color = rendering.nofrills_mod$getFilledColors();
            VertexConsumer buffer = vertexConsumers.getBuffer(Rendering.Layers.BoxFilled);
            VertexRendering.drawFilledBox(matrices, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.r, color.g, color.b, color.a);
        }
    }
}
