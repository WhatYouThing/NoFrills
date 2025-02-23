package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.events.DrawItemTooltip;
import nofrills.features.DungeonSolvers;
import nofrills.misc.ScreenOptions;
import nofrills.misc.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static nofrills.Main.eventBus;
import static nofrills.misc.Utils.DisabledSlot;
import static nofrills.misc.Utils.SpoofedSlot;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenOptions {
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    @Final
    protected T handler;
    @Unique
    List<DisabledSlot> disabledSlots = new ArrayList<>();
    @Unique
    List<SpoofedSlot> spoofedSlots = new ArrayList<>();

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private boolean isStackNameEmpty(Slot slot) {
        if (title.getString().startsWith("Ultrasequencer (")) {
            return false;
        }
        if (slot != null) {
            return slot.getStack().getName().getString().trim().isEmpty();
        }
        return false;
    }

    @Override
    public void nofrills_mod$disableSlot(Slot slot, boolean disabled) {
        disabledSlots.removeIf(disabledSlot -> disabledSlot.isSlot(slot));
        if (disabled) {
            disabledSlots.add(new DisabledSlot(slot));
        }
    }

    @Override
    public void nofrills_mod$spoofSlot(Slot slot, ItemStack replacement) {
        spoofedSlots.removeIf(spoofed -> spoofed.isSlot(slot));
        spoofedSlots.add(new SpoofedSlot(slot, replacement));
    }

    @Override
    public void nofrills_mod$clearSpoof(Slot slot) {
        spoofedSlots.removeIf(spoofed -> spoofed.isSlot(slot));
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (disabledSlots.stream().anyMatch(disabled -> disabled.isSlot(slot))) {
            ci.cancel();
        } else if (Config.ignoreBackground && isStackNameEmpty(slot)) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("TAIL"))
    private void onClickSlotTail(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (spoofedSlots.stream().anyMatch(spoofed -> spoofed.isSlot(slot))) {
            this.handler.setCursorStack(ItemStack.EMPTY); // prevents the real item from showing at the cursor
        }
    }

    @Inject(method = "drawSlotHighlightBack", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlight(DrawContext context, CallbackInfo ci) {
        if (focusedSlot != null && disabledSlots.stream().anyMatch(disabled -> disabled.isSlot(focusedSlot))) {
            ci.cancel();
        } else if (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlotHighlightFront", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlightFront(DrawContext context, CallbackInfo ci) {
        if (focusedSlot != null && disabledSlots.stream().anyMatch(disabled -> disabled.isSlot(focusedSlot))) {
            ci.cancel();
        } else if (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (Config.solveTerminals && DungeonSolvers.isInTerminal) {
            ci.cancel();
        } else if (focusedSlot != null && disabledSlots.stream().anyMatch(disabled -> disabled.isSlot(focusedSlot))) {
            ci.cancel();
        } else if (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;getTooltipFromItem(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private List<Text> onGetTooltipFromItem(List<Text> original) {
        if (Config.priceTooltips && focusedSlot != null) {
            ItemStack stack = focusedSlot.getStack();
            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (!stack.isEmpty() && component != null) {
                eventBus.post(new DrawItemTooltip(original, stack, component.copyNbt()));
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawStack(ItemStack original, DrawContext context, Slot slot) {
        Optional<SpoofedSlot> spoofedSlot = spoofedSlots.stream().filter(spoofed -> spoofed.isSlot(slot)).findFirst();
        if (spoofedSlot.isPresent()) {
            return spoofedSlot.get().replacementStack;
        }
        return original;
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawSpoofedTooltip(ItemStack original) {
        Optional<SpoofedSlot> spoofedSlot = spoofedSlots.stream().filter(spoofed -> spoofed.isSlot(focusedSlot)).findFirst();
        if (spoofedSlot.isPresent()) {
            return spoofedSlot.get().replacementStack;
        }
        return original;
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasCreativeInventory()Z"))
    private boolean onMiddleClick(boolean original) {
        if (Utils.isFixEnabled(Config.middleClickFix)) {
            return true;
        }
        return original;
    }
}
