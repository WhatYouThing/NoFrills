package nofrills.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import nofrills.events.HudRenderEvent;
import nofrills.features.general.ChatTweaks;
import nofrills.features.general.NoRender;
import nofrills.hud.HudManager;
import nofrills.misc.RenderColor;
import nofrills.misc.TitleRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(Gui.class)
public abstract class GuiMixin implements TitleRendering {
    @Unique
    private static String titleString;
    @Unique
    private static int titleTicks = 0;
    @Unique
    private static int titleOffset;
    @Unique
    private static float titleScale;
    @Unique
    private static int titleColor;

    @Shadow
    public abstract Font getFont();

    @Override
    public void nofrills_mod$setRenderTitle(String title, int stayTicks, int yOffset, float scale, RenderColor color) {
        titleString = title;
        titleTicks = stayTicks;
        titleOffset = yOffset;
        titleScale = scale;
        titleColor = color.argb;
    }

    @Override
    public boolean nofrills_mod$isRenderingTitle() {
        return titleTicks > 0;
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void onTickHud(CallbackInfo ci) {
        if (titleTicks > 0) {
            titleTicks--;
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (!mc.options.hideGui) {
            eventBus.post(new HudRenderEvent(context, this.getFont(), tickCounter));
            if (titleTicks > 0) {
                context.pose().pushMatrix();
                context.pose().translate((float) (context.guiWidth() / 2), (float) (context.guiHeight() / 2));
                context.pose().scale(titleScale, titleScale);
                Font textRenderer = mc.gui.getFont();
                Component title = Component.nullToEmpty(titleString);
                int width = textRenderer.width(title);
                context.textWithBackdrop(textRenderer, title, -width / 2, titleOffset, width, titleColor);
                context.pose().popMatrix();
            }
        }
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void onRenderEffectOverlay(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.effectDisplay.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderNausea(GuiGraphicsExtractor context, float nauseaStrength, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractVignette", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIII)V"), cancellable = true)
    private void onRenderVignette(GuiGraphicsExtractor context, Entity entity, CallbackInfo ci, @Local(ordinal = 0) float f) {
        if (NoRender.instance.isActive()) {
            NoRender.VignetteMode mode = NoRender.vignette.value();
            if (mode.equals(NoRender.VignetteMode.None)) return;
            switch (mode) {
                case Ambient -> {
                    if (f <= 0.0f) ci.cancel();
                }
                case Danger -> {
                    if (f > 0.0f) ci.cancel();
                }
                case Both -> ci.cancel();
            }
        }
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;<init>(Lnet/minecraft/client/Minecraft;)V"))
    private void onInit(Minecraft client, CallbackInfo ci) {
        HudManager.registerElements();
    }

    @WrapWithCondition(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;clearMessages(Z)V"))
    private boolean shouldClearChat(ChatComponent instance, boolean clearHistory) {
        return !(ChatTweaks.instance.isActive() && ChatTweaks.keepHistory.value());
    }
}
