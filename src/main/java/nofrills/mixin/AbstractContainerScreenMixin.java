package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.events.ScreenRenderEvent;
import nofrills.events.SlotClickEvent;
import nofrills.events.TooltipRenderEvent;
import nofrills.features.dungeons.LeapOverlay;
import nofrills.features.dungeons.TerminalSolvers;
import nofrills.features.general.NoRender;
import nofrills.features.general.SlotBinding;
import nofrills.features.tweaks.MiddleClickFix;
import nofrills.features.tweaks.MiddleClickOverride;
import nofrills.misc.ScreenOptions;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.eventBus;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements ScreenOptions {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    @Final
    protected T menu;
    @Unique
    List<LeapOverlay.LeapButton> leapButtons = new ArrayList<>();

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Override
    public void nofrills_mod$addLeapButton(LeapOverlay.LeapTarget target) {
        leapButtons.add(new LeapOverlay.LeapButton(target, leapButtons.size()));
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", ordinal = 1))
    private void onClickSlotRedirect(AbstractContainerScreen<?> instance, Slot slot, int slotId, int button, ContainerInput actionType, Operation<Void> original) {
        if (MiddleClickOverride.shouldOverride(slot, button, actionType)) {
            instance.slotClicked(slot, slotId, GLFW.GLFW_MOUSE_BUTTON_3, ContainerInput.CLONE);
        } else {
            original.call(instance, slot, slotId, button, actionType);
        }
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo ci) {
        if (LeapOverlay.isLeapMenu(this.title.getString()) || NoRender.shouldHideTooltip(slot, this.title.getString()) || SlotOptions.isDisabled(slot)) {
            ci.cancel();
            return;
        }
        if (eventBus.post(new SlotClickEvent(slot, slotId, button, actionType, this.title.getString(), this.menu)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("TAIL"))
    private void onClickSlotTail(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo ci) {
        if (SlotOptions.isSpoofed(slot)) {
            this.menu.setCarried(ItemStack.EMPTY); // prevents the real item from showing at the cursor
        }
    }

    @Inject(method = "extractSlotHighlightBack", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlight(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (NoRender.shouldHideTooltip(hoveredSlot, this.title.getString()) || SlotOptions.isDisabled(hoveredSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractSlotHighlightFront", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlightFront(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (NoRender.shouldHideTooltip(hoveredSlot, this.title.getString()) || SlotOptions.isDisabled(hoveredSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"), cancellable = true)
    private void onDrawTooltip(GuiGraphicsExtractor context, int x, int y, CallbackInfo ci) {
        if (TerminalSolvers.shouldHideTooltips(this.title.getString()) || NoRender.shouldHideTooltip(hoveredSlot, this.title.getString()) || SlotOptions.isDisabled(hoveredSlot) || SlotBinding.isBinding()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "extractTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getTooltipFromContainerItem(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private List<Component> onGetTooltipFromItem(List<Component> original, @Local ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            eventBus.post(new TooltipRenderEvent(original, itemStack, Utils.getCustomData(itemStack), this.getTitle().getString()));
        }
        return original;
    }

    @ModifyExpressionValue(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onDrawStack(ItemStack original, GuiGraphicsExtractor context, Slot slot) {
        if (SlotOptions.isSpoofed(slot)) {
            return SlotOptions.getSpoofed(slot);
        }
        return original;
    }

    @ModifyArg(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"), index = 4)
    private @Nullable String onDrawStackCount(@Nullable String stackCountText, @Local(argsOnly = true) Slot slot) {
        if (SlotOptions.hasCount(slot)) {
            return SlotOptions.getCount(slot);
        }
        return stackCountText;
    }

    @ModifyExpressionValue(method = "extractTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onDrawSpoofedTooltip(ItemStack original) {
        if (SlotOptions.isSpoofed(hoveredSlot)) {
            return SlotOptions.getSpoofed(hoveredSlot);
        }
        return original;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (LeapOverlay.isLeapMenu(this.title.getString())) {
            for (LeapOverlay.LeapButton button : leapButtons) {
                button.extractRenderState(context, mouseX, mouseY, delta);
            }
            ci.cancel();
        }
    }

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void onRenderSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (SlotOptions.hasBackground(slot)) {
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, SlotOptions.getBackground(slot).argb);
        }
    }

    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlotHighlightBack(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void onBeforeHighlightRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        eventBus.post(new ScreenRenderEvent.Before(context, mouseX, mouseY, deltaTicks, this.title.getString(), this.menu, this.hoveredSlot));
    }

    @SuppressWarnings("mapping")
    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"))
    private void onAfterRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        eventBus.post(new ScreenRenderEvent.After(context, mouseX, mouseY, delta, this.title.getString(), this.menu, this.hoveredSlot));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (LeapOverlay.isLeapMenu(this.title.getString()) && click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            for (LeapOverlay.LeapButton leapButton : leapButtons) {
                if (leapButton.isHovered(click.x(), click.y())) {
                    leapButton.click(this.menu);
                    cir.setReturnValue(true);
                }
            }
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"))
    private boolean onMiddleClick(boolean original) {
        if (MiddleClickFix.active()) {
            return true;
        }
        return original;
    }
}
