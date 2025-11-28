package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import nofrills.events.InputEvent;
import nofrills.features.farming.SpaceFarmer;
import nofrills.features.tweaks.NoCursorReset;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (eventBus.post(new InputEvent(button, mods, action)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onMouseMove(double timeDelta, CallbackInfo ci) {
        if (SpaceFarmer.instance.isActive() && SpaceFarmer.spaceHeld && mc.options.attackKey.isPressed()) {
            ci.cancel();
        }
    }

    @Inject(method = "onCursorPos", at = @At("TAIL"))
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (NoCursorReset.instance.isActive()) {
            NoCursorReset.updateCursorPos(x, y);
        }
    }

    @ModifyExpressionValue(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getWidth()I"))
    private int onGetWidth(int original) {
        if (NoCursorReset.isActive() && NoCursorReset.cursorX >= 0.0) {
            return (int) Math.floor(NoCursorReset.cursorX * 2);
        }
        return original;
    }

    @ModifyExpressionValue(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHeight()I"))
    private int onGetHeight(int original) {
        if (NoCursorReset.isActive() && NoCursorReset.cursorY >= 0.0) {
            return (int) Math.floor(NoCursorReset.cursorY * 2);
        }
        return original;
    }
}
