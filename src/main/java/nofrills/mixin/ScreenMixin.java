package nofrills.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import nofrills.features.general.inventorybuttons.InventoryButtons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @SuppressWarnings("ConstantValue")
    @Inject(method = "init(II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;setInitialFocus()V"))
    private void onAfterInit(CallbackInfo ci) {
        if (InventoryButtons.instance.isActive() && (Screen) (Object) this instanceof AbstractContainerScreen<?> container) {
            InventoryButtons.addWidgets(container);
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "rebuildWidgets", at = @At("TAIL"))
    private void onAfterRebuild(CallbackInfo ci) {
        if (InventoryButtons.instance.isActive() && (Screen) (Object) this instanceof AbstractContainerScreen<?> container) {
            InventoryButtons.addWidgets(container);
        }
    }
}
