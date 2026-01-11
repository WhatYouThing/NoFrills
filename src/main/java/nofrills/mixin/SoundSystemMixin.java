package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.sound.SoundSystem;
import nofrills.features.misc.UnfocusedTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {

    @ModifyExpressionValue(method = "getAdjustedVolume(FLnet/minecraft/sound/SoundCategory;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getSoundVolume(Lnet/minecraft/sound/SoundCategory;)F"))
    private float onGetCategoryVolume(float original) {
        if (UnfocusedTweaks.active() && UnfocusedTweaks.muteSounds.value()) {
            return 0.0f;
        }
        return original;
    }
}