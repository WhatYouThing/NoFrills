package nofrills.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import nofrills.events.InputEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        if (input.key() != GLFW.GLFW_KEY_UNKNOWN) {
            if (eventBus.post(new InputEvent(input, action)).isCancelled()) {
                ci.cancel();
            }
        }
    }
}
