package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import nofrills.config.Config;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyExpressionValue(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean onTravel(boolean original) {
        if (Utils.isFixEnabled(Config.antiSwim)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "applyFluidMovingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean onApplyFluidSpeed(boolean original) {
        if (Utils.isFixEnabled(Config.antiSwim)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"))
    private boolean hasHaste(boolean original) {
        if (Config.noHaste && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean hasMiningFatigue(boolean original) {
        if (Config.noHaste && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "getHandSwingDuration", at = @At("RETURN"))
    private int getSwingSpeed(int original) {
        if (Config.viewmodelSpeed != 0 && Utils.isSelf(this)) {
            return Config.viewmodelSpeed;
        }
        return original;
    }
}
