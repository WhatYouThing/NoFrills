package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import nofrills.events.HudRenderEvent;
import nofrills.events.ScreenCloseEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.features.tweaks.NoLoadingScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onBeforeOpenScreen(Screen screen, CallbackInfo ci) {
        if (NoLoadingScreen.instance.isActive() && screen instanceof LevelLoadingScreen) {
            if (NoLoadingScreen.serverOnly.value()) {
                ServerData serverEntry = this.minecraft.getCurrentServer();
                if (serverEntry == null || serverEntry.isLan()) {
                    return;
                }
            }
            this.setScreen(null);
            ci.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        if (this.minecraft.level == null) return;
        if (screen != null) {
            eventBus.post(new ScreenOpenEvent(screen));
        } else {
            eventBus.post(new ScreenCloseEvent());
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local(name = "graphics") GuiGraphicsExtractor graphics) {
        if (!mc.gui.hud.isHidden()) {
            eventBus.post(new HudRenderEvent(graphics, this.minecraft.font, deltaTracker));
        }
    }
}
