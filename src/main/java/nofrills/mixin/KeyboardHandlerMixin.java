package nofrills.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKey(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (event.key() != SettingKeybind.UNKNOWN_KEY) {
            if (eventBus.post(new InputEvent(event, action)).isCancelled()) {
                ci.cancel();
            }
        }
    }
}
