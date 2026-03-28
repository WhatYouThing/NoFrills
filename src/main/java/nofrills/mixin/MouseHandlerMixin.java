package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.entity.player.Inventory;
import nofrills.events.InputEvent;
import nofrills.features.farming.SpaceFarmer;
import nofrills.features.misc.HotbarScrollLock;
import nofrills.features.tweaks.NoCursorReset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        if (eventBus.post(new InputEvent(input, action)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onMouseMove(double timeDelta, CallbackInfo ci) {
        if (SpaceFarmer.instance.isActive() && SpaceFarmer.spaceHeld && mc.options.keyAttack.isDown()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMove", at = @At("TAIL"))
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (NoCursorReset.instance.isActive()) {
            NoCursorReset.updateCursorPos(x, y);
        }
    }

    @ModifyExpressionValue(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getScreenWidth()I"))
    private int onGetWidth(int original) {
        if (NoCursorReset.isActive() && NoCursorReset.cursorX >= 0.0) {
            return (int) Math.floor(NoCursorReset.cursorX * 2);
        }
        return original;
    }

    @ModifyExpressionValue(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getScreenHeight()I"))
    private int onGetHeight(int original) {
        if (NoCursorReset.isActive() && NoCursorReset.cursorY >= 0.0) {
            return (int) Math.floor(NoCursorReset.cursorY * 2);
        }
        return original;
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"), cancellable = true)
    private void onBeforeSetSlot(long window, double horizontal, double vertical, CallbackInfo ci, @Local Inventory inv) {
        if (HotbarScrollLock.instance.isActive()) {
            int selected = inv.getSelectedSlot();
            if (selected == 0 && (horizontal < 0.0 || vertical > 0.0)) {
                ci.cancel();
            } else if (selected == 8 && (horizontal > 0.0 || vertical < 0.0)) {
                ci.cancel();
            }
        }
    }
}
