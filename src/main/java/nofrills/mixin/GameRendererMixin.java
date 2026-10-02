package nofrills.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** World-camera portal and nausea strengths are now extracted into player render state. */
@Mixin(LevelExtractor.class)
public abstract class GameRendererMixin {
    @Inject(method = "extractPlayerState", at = @At("TAIL"))
    private void suppressCameraDistortion(Camera camera, DeltaTracker tracker, float partialTick,
            PlayerRenderState state, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            state.portalEffectIntensity = 0.0f;
            state.nauseaEffectIntensity = 0.0f;
        }
    }
}
