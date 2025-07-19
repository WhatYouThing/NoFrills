package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.profiler.Profiler;
import nofrills.features.general.Fullbright;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Shadow
    @Final
    private GpuTexture glTexture;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyBrightness(F)F"), cancellable = true)
    private void beforeRender(float tickProgress, CallbackInfo ci, @Local Profiler profiler) {
        if (Fullbright.instance.isActive()) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.glTexture, Fullbright.color.value().argb);
            profiler.pop();
            ci.cancel();
        }
    }
}
