package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import nofrills.features.general.Viewmodel;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static nofrills.Main.mc;

@Mixin(LivingEntity.SwingState.class)
public abstract class LivingEntitySwingStateMixin {

    @Shadow
    private LivingEntity.@Nullable SwingDescription currentSwing;

    @ModifyExpressionValue(method = "startIfAble", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity$SwingDescription;durationTicks:I", opcode = Opcodes.GETFIELD))
    private int onGetSwingDuration(int original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noSwingReset.value() && mc.player != null && mc.player.getCurrentSwing() == this.currentSwing) {
            return original * 2; // force the check to require 100% swing progress instead of 50%
        }
        return original;
    }
}
