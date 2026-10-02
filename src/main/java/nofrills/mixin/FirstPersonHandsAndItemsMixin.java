package nofrills.mixin;

import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import nofrills.features.general.Viewmodel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {
    @Shadow private float mainHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float oOffHandHeight;

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void onShouldSkipAnimation(ItemStack visibleItem, ItemStack expectedItem, LocalPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) cir.setReturnValue(true);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onUpdateHeldItems(CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            this.mainHandHeight = this.offHandHeight = this.oMainHandHeight = this.oOffHandHeight = 1.0f;
        }
    }
}
