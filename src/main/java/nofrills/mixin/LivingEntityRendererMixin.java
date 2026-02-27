package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.features.misc.ForceNametag;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static nofrills.Main.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {
    @ModifyExpressionValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSneaky()Z"))
    private boolean isSneaking(boolean original, T livingEntity) {
        if (ForceNametag.isActive() && livingEntity instanceof PlayerEntity) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean isInvisible(boolean original, T livingEntity) {
        if (ForceNametag.isActive() && livingEntity instanceof PlayerEntity player && Utils.isPlayer(player)) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("RETURN"))
    protected boolean hasLabel(boolean original, T livingEntity) {
        if (ForceNametag.isActive() && ForceNametag.self.value() && livingEntity.equals(mc.player)) {
            return true;
        }
        return original;
    }
}
