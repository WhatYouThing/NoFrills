package nofrills.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.bossBar.value()) {
            ci.cancel();
        }
    }
}
