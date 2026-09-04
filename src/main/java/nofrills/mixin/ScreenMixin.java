package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import nofrills.events.TooltipRenderEvent;
import nofrills.features.general.inventorybuttons.InventoryButtons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static nofrills.Main.eventBus;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @ModifyReturnValue(method = "getTooltipFromItem", at = @At("RETURN"))
    private static List<Component> onGetTooltipLines(List<Component> original, @Local(argsOnly = true, name = "itemStack") ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            TooltipRenderEvent event = eventBus.post(new TooltipRenderEvent(original, itemStack));
            if (event.isCancelled()) {
                return List.of();
            }
            if (event.replacement != null) {
                return event.replacement;
            }
        }
        return original;
    }

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
