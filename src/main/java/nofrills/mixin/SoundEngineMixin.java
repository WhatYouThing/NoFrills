package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.sounds.SoundEngine;
import nofrills.features.misc.UnfocusedTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @ModifyExpressionValue(method = "calculateVolume(FLnet/minecraft/sounds/SoundSource;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getFinalSoundSourceVolume(Lnet/minecraft/sounds/SoundSource;)F"))
    private float onGetCategoryVolume(float original) {
        if (UnfocusedTweaks.active() && UnfocusedTweaks.muteSounds.value()) {
            return 0.0f;
        }
        return original;
    }
}