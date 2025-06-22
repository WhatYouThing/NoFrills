package nofrills.mixin;

import net.minecraft.client.Mouse;
import nofrills.events.InputEvent;
import nofrills.features.SpaceFarmer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.*;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (eventBus.post(new InputEvent(button, mods, action)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onMouseMove(double timeDelta, CallbackInfo ci) {
        if (Config.lockView() && SpaceFarmer.spaceHeld && mc.options.attackKey.isPressed()) {
            ci.cancel();
        }
    }
}
