package nofrills.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import nofrills.features.misc.SoundBlocker;
import nofrills.features.misc.UnfocusedTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @ModifyExpressionValue(method = "calculateVolume(FLnet/minecraft/sounds/SoundSource;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getFinalSoundSourceVolume(Lnet/minecraft/sounds/SoundSource;)F"))
    private float onGetCategoryVolume(float original) {
        if (UnfocusedTweaks.isActive() && UnfocusedTweaks.muteSounds.value()) {
            return 0.0f;
        }
        return original;
    }

    @WrapMethod(method = "play")
    private SoundEngine.PlayResult onBeforePlaySound(SoundInstance instance, Operation<SoundEngine.PlayResult> original) {
        if (SoundBlocker.instance.isActive() && SoundBlocker.data.value().has("custom")) {
            for (JsonElement element : SoundBlocker.data.value().get("custom").getAsJsonArray()) {
                Identifier customIdentifier = Identifier.tryParse(element.getAsString());
                if (customIdentifier != null && instance.getIdentifier().equals(customIdentifier)) {
                    return SoundEngine.PlayResult.NOT_STARTED;
                }
            }
        }
        return original.call(instance);
    }
}