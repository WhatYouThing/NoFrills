package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import nofrills.events.WorldRenderEvent;
import nofrills.features.general.NoRender;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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

    @SuppressWarnings("mapping")
    // the compiler sometimes claims that the inject target wasn't found, but it works fine regardless
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;"))
    private void onRenderWorld(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        eventBus.post(new WorldRenderEvent(immediate, tickCounter, camera, new MatrixStack()));
        immediate.draw();
    }

    @Inject(method = "fillEntityRenderStates", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;"), cancellable = true)
    private void onBeforeRenderEntity(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates, CallbackInfo ci, @Local Entity entity) {
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
}
