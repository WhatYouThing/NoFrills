package nofrills.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import nofrills.events.InputEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (input.key() != GLFW.GLFW_KEY_UNKNOWN) {
            if (eventBus.post(new InputEvent(input, action)).isCancelled()) {
                ci.cancel();
            }
        }
    }
}
