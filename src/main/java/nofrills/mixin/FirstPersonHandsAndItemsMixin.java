package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import nofrills.features.general.Viewmodel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z"))
    private boolean onAreHandsBusy(boolean original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F"))
    private float onGetSwapScale(float original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            return 1.0f;
        }
        return original;
    }

    @ModifyReturnValue(method = "shouldInstantlyReplaceVisibleItem", at = @At("RETURN"))
    private boolean onShouldReplace(boolean original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            return true;
        }
        return original;
    }
}
