package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.render.LightmapTextureManager;
import nofrills.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setVertexBuffer(ILcom/mojang/blaze3d/buffers/GpuBuffer;)V"))
    private void beforeRender(float tickProgress, CallbackInfo ci, @Local RenderPass renderPass) {
        if (Config.fullbright) {
            renderPass.setUniform("BrightnessFactor", new float[]{4269});
        }
    }
}
