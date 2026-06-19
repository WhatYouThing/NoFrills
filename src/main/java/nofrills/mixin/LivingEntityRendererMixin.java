package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import nofrills.features.misc.ForceNametag;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static nofrills.Main.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {
    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDiscrete()Z"))
    private boolean isSneaking(boolean original, T livingEntity) {
        if (ForceNametag.isActive() && livingEntity instanceof Player) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean isInvisible(boolean original, T livingEntity) {
        if (ForceNametag.isActive() && livingEntity instanceof Player player && Utils.isPlayer(player)) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("RETURN"))
    protected boolean hasLabel(boolean original, T livingEntity) {
        if (ForceNametag.isActive() && ForceNametag.self.value() && livingEntity.equals(mc.player)) {
            return true;
        }
        return original;
    }
}
