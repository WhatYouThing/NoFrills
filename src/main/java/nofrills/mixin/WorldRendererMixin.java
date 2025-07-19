package nofrills.mixin;

import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import nofrills.events.WorldRenderEvent;
import nofrills.features.general.NoRender;
import nofrills.misc.EntityRendering;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Unique
    @Final
    private VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(new BufferAllocator(1536 * 20));

    // the compiler sometimes claims that the inject target wasn't found, but it works fine regardless
    @SuppressWarnings("mapping")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;"))
    private void onRenderWorld(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        eventBus.post(new WorldRenderEvent(immediate, tickCounter, camera, new MatrixStack()));
        immediate.draw();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void onBeforeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.deadEntities.value() && entity instanceof LivingEntity) {
            if (!entity.isAlive()) {
                ci.cancel();
            }
        }
        if (NoRender.instance.isActive() && NoRender.treeBits.value() && NoRender.isTreeBlock(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEntity", at = @At("TAIL"))
    private void onRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!entity.isAlive()) {
            return;
        }
        EntityRendering rendering = (EntityRendering) entity;
        Box box = entity.getDimensions(entity.getPose()).getBoxAt(entity.getLerpedPos(tickProgress));
        if (rendering.nofrills_mod$getRenderingOutline()) {
            RenderColor color = rendering.nofrills_mod$getOutlineColors();
            VertexConsumer buffer = vertexConsumers.getBuffer(Rendering.Layers.BoxOutline);
            matrices.push();
            matrices.translate(-cameraX, -cameraY, -cameraZ);
            VertexRendering.drawBox(matrices, buffer, box, color.r, color.g, color.b, color.a);
            matrices.pop();
        }
        if (rendering.nofrills_mod$getRenderingFilled()) {
            RenderColor color = rendering.nofrills_mod$getFilledColors();
            VertexConsumer buffer = vertexConsumers.getBuffer(Rendering.Layers.BoxFilled);
            matrices.push();
            matrices.translate(-cameraX, -cameraY, -cameraZ);
            VertexRendering.drawFilledBox(matrices, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.r, color.g, color.b, color.a);
            matrices.pop();
        }
    }
}
