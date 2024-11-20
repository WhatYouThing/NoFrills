package nofrills.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import nofrills.config.Config;
import nofrills.misc.ScreenOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static nofrills.misc.Utils.SpoofedSlot;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> implements ScreenOptions {
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Unique
    List<Slot> disabledSlots = new ArrayList<>();
    @Unique
    List<SpoofedSlot> spoofedSlots = new ArrayList<>();

    @Unique
    private static boolean isStackNameEmpty(Slot slot) {
        if (slot != null) {
            return slot.getStack().getName().getString().trim().isEmpty();
        }
        return false;
    }

    @Override
    public void nofrills_mod$disableSlot(Slot slot, boolean disabled) {
        if (disabled && !disabledSlots.contains(slot)) {
            disabledSlots.add(slot);
        }
        if (!disabled) {
            disabledSlots.remove(slot);
        }
    }

    @Override
    public void nofrills_mod$spoofSlot(Slot slot, ItemStack replacement) {
        SpoofedSlot spoofedSlot = new SpoofedSlot(slot, replacement);
        if (!spoofedSlots.contains(spoofedSlot)) {
            spoofedSlots.add(spoofedSlot);
        }
    }

    @Override
    public void nofrills_mod$clearSpoof(Slot slot) {
        spoofedSlots.removeIf(spoofedSlot -> spoofedSlot.slot.equals(slot));
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (disabledSlots.contains(slot)) {
            ci.cancel();
        } else if (Config.ignoreBackground) {
            if (isStackNameEmpty(slot)) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;canBeHighlighted()Z"))
    private boolean onDrawHighlight(Slot instance) {
        if (focusedSlot != null && disabledSlots.contains(focusedSlot)) {
            return false;
        } else if (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) {
            return false;
        }
        return instance.canBeHighlighted();
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (focusedSlot != null && disabledSlots.contains(focusedSlot)) {
            ci.cancel();
        } else if (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) {
            ci.cancel();
        }
    }

    @Redirect(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawStack(Slot instance) {
        for (SpoofedSlot spoofedSlot : spoofedSlots) {
            if (spoofedSlot.slot.equals(instance)) {
                return spoofedSlot.replacementStack;
            }
        }
        return instance.getStack();
    }
}
