package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import net.minecraft.gizmos.SimpleGizmoCollector;
import nofrills.events.WorldRenderEvent;
import nofrills.features.slayer.NoBossParticles;
import nofrills.misc.SlayerUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Shadow
    @Final
    private SimpleGizmoCollector renderThreadGizmos;

    @Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void onSubmitGizmos(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline, CallbackInfo ci, @Local(name = "poseStack") PoseStack poseStack) {
        eventBus.post(new WorldRenderEvent(levelRenderState, poseStack, this.submitNodeStorage, this.renderThreadGizmos));
    }

    @WrapOperation(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/level/ParticlesRenderState;submit(Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    private void onSubmitParticles(ParticlesRenderState instance, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Operation<Void> original) {
        if (NoBossParticles.instance.isActive() && SlayerUtil.isBossAlive()) {
            return;
        }
        original.call(instance, submitNodeCollector, camera);
    }
}
