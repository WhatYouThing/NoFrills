package nofrills.mixin;

import net.minecraft.item.EnderPearlItem;
import nofrills.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnderPearlItem.class)
public class EnderPearlMixin {
    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"), index = 1)
    private int onUsePearl(int duration) {
        if (Config.noPearlCooldown) {
            return 0; // can't cancel this call the old-fashioned way, but we can override the cooldown to be 0 ticks
        }
        return duration;
    }
}
