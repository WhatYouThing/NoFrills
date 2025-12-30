package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.mob.EndermanEntity;
import nofrills.features.slayer.MuteEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {

    @Inject(method = "playAngrySound", at = @At("HEAD"), cancellable = true)
    private void beforePlayAngrySound(CallbackInfo ci) {
        if (MuteEnderman.instance.isActive()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "getAmbientSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/EndermanEntity;isAngry()Z"))
    private boolean isAngrySound(boolean original) {
        if (MuteEnderman.instance.isActive()) {
            return false;
        }
        return original;
    }
}
