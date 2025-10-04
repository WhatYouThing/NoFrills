package nofrills.mixin;

import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.util.math.Box;
import nofrills.events.WorldRenderEvent;
import nofrills.features.general.NoRender;
import nofrills.misc.EntityRendering;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;
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
        if (NoRender.instance.isActive()) {
            if (NoRender.deadEntities.value() && entity instanceof LivingEntity && !entity.isAlive()) {
                ci.cancel();
            }
            if (NoRender.fallingBlocks.value() && entity instanceof FallingBlockEntity) {
                ci.cancel();
            }
            if (NoRender.treeBits.value() && NoRender.isTreeBlock(entity)) {
                ci.cancel();
            }
            if (NoRender.lightning.value() && entity instanceof LightningEntity) {
                ci.cancel();
            }
            if (NoRender.expOrbs.value() && entity instanceof ExperienceOrbEntity) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderEntity", at = @At("TAIL"))
    private void onRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!entity.isAlive()) {
            return;
        }
        EntityRendering rendering = (EntityRendering) entity;
        if (!rendering.nofrills_mod$getRenderingOutline() && !rendering.nofrills_mod$getRenderingFilled()) {
            return;
        }
        Box box = Utils.getLerpedBox(entity, tickProgress);
        matrices.push();
        matrices.translate(-cameraX, -cameraY, -cameraZ);
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
        matrices.pop();
    }
}
