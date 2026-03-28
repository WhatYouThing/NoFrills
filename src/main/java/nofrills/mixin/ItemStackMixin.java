package nofrills.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.features.tweaks.ItemCountFix;
import nofrills.features.tweaks.NoPearlCooldown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.mc;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "limitSize", at = @At("HEAD"), cancellable = true)
    private void onCapCount(int maxCount, CallbackInfo ci) {
        if (ItemCountFix.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyAfterUseComponentSideEffects", at = @At("HEAD"), cancellable = true)
    private void onApplyCooldown(LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (user.equals(mc.player) && NoPearlCooldown.active()) {
            if (stack.getItem().equals(Items.ENDER_PEARL)) {
                cir.setReturnValue(stack);
            }
        }
    }
}
