package nofrills.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(StatusEffectsDisplay.class)
public abstract class StatusEffectsDisplayMixin {

    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void onDrawEffects(DrawContext context, Collection<StatusEffectInstance> effects, int x, int height, int mouseX, int mouseY, int width, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.effectDisplay.value()) {
            ci.cancel();
        }
    }
}
