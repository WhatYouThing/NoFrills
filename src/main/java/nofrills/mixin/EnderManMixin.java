package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.monster.EnderMan;
import nofrills.features.slayer.MuteEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderMan.class)
public class EnderManMixin {

    @Inject(method = "playStareSound", at = @At("HEAD"), cancellable = true)
    private void beforePlayAngrySound(CallbackInfo ci) {
        if (MuteEnderman.instance.isActive()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "getAmbientSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;isCreepy()Z"))
    private boolean isAngrySound(boolean original) {
        if (MuteEnderman.instance.isActive()) {
            return false;
        }
        return original;
    }
}
