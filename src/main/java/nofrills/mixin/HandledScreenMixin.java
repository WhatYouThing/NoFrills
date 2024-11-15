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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> implements ScreenOptions {
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    @Final
    protected T handler;
    @Unique
    List<String> disabledSlots = new ArrayList<>(); // List<Integer> simply did not work properly for whatever reason
    @Unique
    boolean cancelEmpty = false;

    @Unique
    private static boolean isStackNameEmpty(Slot slot) {
        if (slot != null) {
            return slot.getStack().getName().getString().trim().isEmpty();
        }
        return false;
    }

    @Override
    public void nofrills_mod$disableSlot(int slotId, boolean disabled) {
        String newSlot = String.valueOf(slotId);
        if (disabled && !disabledSlots.contains(newSlot)) {
            disabledSlots.add(newSlot);
        }
        if (!disabled) {
            disabledSlots.remove(newSlot);
        }
    }

    @Override
    public void nofrills_mod$disableSlot(int slotId, boolean disabled, ItemStack replacement) {
        nofrills_mod$disableSlot(slotId, disabled);
        this.handler.slots.get(slotId).setStack(replacement);
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (disabledSlots.contains(String.valueOf(slotId))) {
            ci.cancel();
        } else if (cancelEmpty || Config.ignoreBackground) {
            if (isStackNameEmpty(slot)) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;canBeHighlighted()Z"))
    private boolean onDrawHighlight(Slot instance) {
        if (focusedSlot != null && disabledSlots.contains(String.valueOf(focusedSlot.id))) {
            return false;
        } else if (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) {
            return false;
        }
        return instance.canBeHighlighted();
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (focusedSlot != null && disabledSlots.contains(String.valueOf(focusedSlot.id))) {
            ci.cancel();
        } else if (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) {
            ci.cancel();
        }
    }
}
