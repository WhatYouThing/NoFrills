package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.events.TooltipRenderEvent;
import nofrills.features.tweaks.ItemCountFix;
import nofrills.features.tweaks.NoPearlCooldown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract boolean isEmpty();

    @ModifyReturnValue(method = "getTooltipLines", at = @At("RETURN"))
    private List<Component> onGetTooltipLines(List<Component> original) {
        if (!this.isEmpty()) {
            TooltipRenderEvent event = eventBus.post(new TooltipRenderEvent(original, (ItemStack) (Object) this));
            if (event.isCancelled()) {
                return List.of();
            }
            if (event.replacement != null) {
                return event.replacement;
            }
        }
        return original;
    }

    @Inject(method = "limitSize", at = @At("HEAD"), cancellable = true)
    private void onCapCount(int maxStackSize, CallbackInfo ci) {
        if (ItemCountFix.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyAfterUseComponentSideEffects", at = @At("HEAD"), cancellable = true)
    private void onApplyCooldown(LivingEntity user, ItemStack stackBeforeUsing, CallbackInfoReturnable<ItemStack> cir) {
        if (user.equals(mc.player) && NoPearlCooldown.active()) {
            if (stackBeforeUsing.getItem().equals(Items.ENDER_PEARL)) {
                cir.setReturnValue(stackBeforeUsing);
            }
        }
    }
}
