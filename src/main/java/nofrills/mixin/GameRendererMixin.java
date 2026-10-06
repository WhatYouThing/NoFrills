package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import nofrills.features.general.NoRender;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;oPortalEffectIntensity:F", opcode = Opcodes.GETFIELD))
    private float onGetLastIntensity(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;portalEffectIntensity:F", opcode = Opcodes.GETFIELD))
    private float onGetIntensity(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float onGetFactor(float original) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            return 0.0f;
        }
        return original;
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;getBuffer(Lnet/minecraft/client/renderer/fog/FogRenderer$FogMode;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", ordinal = 0))
    private GpuBufferSlice onUpdateFogBuffer(FogRenderer instance, FogRenderer.FogMode mode, Operation<GpuBufferSlice> original, @Local(name = "cameraState") CameraRenderState cameraState) {
        if (NoRender.instance.isActive() && NoRender.fog.value() && mode.equals(FogRenderer.FogMode.WORLD) && NoRender.isFogEnvironmental.get()) {
            return original.call(instance, FogRenderer.FogMode.NONE);
        }
        return original.call(instance, mode);
    }
}