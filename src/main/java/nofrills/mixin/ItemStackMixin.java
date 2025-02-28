package nofrills.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nofrills.config.Config;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.mc;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "capCount", at = @At("HEAD"), cancellable = true)
    private void onCapCount(int maxCount, CallbackInfo ci) {
        if (Utils.isFixEnabled(Config.itemCountFix)) {
            ci.cancel();
        }
    }

    @Inject(method = "applyRemainderAndCooldown", at = @At("HEAD"), cancellable = true)
    private void onApplyCooldown(LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (user.equals(mc.player) && Utils.isFixEnabled(Config.noPearlCooldown)) {
            if (stack.getItem().equals(Items.ENDER_PEARL)) {
                cir.setReturnValue(stack);
            }
        }
    }
}
