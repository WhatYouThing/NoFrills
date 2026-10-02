package nofrills.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.ServerData;
import nofrills.events.*;
import nofrills.features.tweaks.NoLoadingScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static nofrills.Main.mc;
import static nofrills.Main.eventBus;

/** Screen management moved from Minecraft to Gui in 26.3. */
@Mixin(Gui.class)
public abstract class ScreenManagementMixin {
    @Shadow public abstract void setScreen(@Nullable Screen screen);
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void beforeOpen(Screen screen, CallbackInfo ci) {
        if (NoLoadingScreen.instance.isActive() && screen instanceof LevelLoadingScreen) {
            if (NoLoadingScreen.serverOnly.value()) {
                ServerData entry = mc.getCurrentServer();
                if (entry == null || entry.isLan()) return;
            }
            this.setScreen(null);
            ci.cancel();
        }
    }
    @Inject(method = "setScreen", at = @At("TAIL"))
    private void afterOpen(Screen screen, CallbackInfo ci) {
        if (mc.level == null) return;
        if (screen != null) eventBus.post(new ScreenOpenEvent(screen));
        else eventBus.post(new ScreenCloseEvent());
    }
}
