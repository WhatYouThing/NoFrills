package nofrills.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import nofrills.features.general.inventorybuttons.InventoryButtons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    @SuppressWarnings("ConstantValue")
    @Inject(method = "init(II)V", at = @At("TAIL"))
    private void onAfterInit(CallbackInfo ci) {
        if (InventoryButtons.instance.isActive() && (Screen) (Object) this instanceof AbstractContainerScreen<?> container) {
            InventoryButtons.addWidgets(container).ifPresent(widgets -> widgets.forEach(this::addRenderableWidget));
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "rebuildWidgets", at = @At("TAIL"))
    private void onAfterRebuild(CallbackInfo ci) {
        if (InventoryButtons.instance.isActive() && (Screen) (Object) this instanceof AbstractContainerScreen<?> container) {
            InventoryButtons.addWidgets(container).ifPresent(widgets -> widgets.forEach(this::addRenderableWidget));
        }
    }
}
