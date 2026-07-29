package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import nofrills.features.misc.SoundBlocker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderMan.class)
public class EnderManMixin {

    @WrapOperation(method = "playStareSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void beforePlayAngrySound(Level instance, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
        if (SoundBlocker.instance.isActive() && SoundBlocker.angryEnderman.value()) {
            return;
        }
        original.call(instance, x, y, z, sound, source, volume, pitch, distanceDelay);
    }

    @ModifyExpressionValue(method = "getAmbientSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;isCreepy()Z"))
    private boolean isAngrySound(boolean original) {
        if (SoundBlocker.instance.isActive() && SoundBlocker.angryEnderman.value()) {
            return false;
        }
        return original;
    }
}
