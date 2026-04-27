package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import nofrills.events.HudRenderEvent;
import nofrills.features.general.ChatTweaks;
import nofrills.features.general.NoRender;
import nofrills.hud.HudManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true)
    private static void onRenderArmorBar(GuiGraphicsExtractor graphics, Player player, int yLineBase, int numHealthRows, int healthRowHeight, int xLeft, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.armorBar.value()) {
            ci.cancel();
        }
    }

    @Shadow
    public abstract Font getFont();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!mc.options.hideGui) {
            eventBus.post(new HudRenderEvent(graphics, this.getFont(), deltaTracker));
        }
    }

    @Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
    private void onRenderFoodBar(GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.foodBar.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void onRenderEffectOverlay(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.effectDisplay.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void onRenderSelectedItemName(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.selectedItemName.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderNausea(GuiGraphicsExtractor graphics, float strength, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractVignette", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIII)V"), cancellable = true)
    private void onRenderVignette(GuiGraphicsExtractor context, Entity entity, CallbackInfo ci, @Local(ordinal = 0) float borderWarningStrength) {
        if (NoRender.instance.isActive()) {
            NoRender.VignetteMode mode = NoRender.vignette.value();
            if (mode.equals(NoRender.VignetteMode.None)) return;
            switch (mode) {
                case Ambient -> {
                    if (borderWarningStrength <= 0.0f) ci.cancel();
                }
                case Danger -> {
                    if (borderWarningStrength > 0.0f) ci.cancel();
                }
                case Both -> ci.cancel();
            }
        }
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;<init>(Lnet/minecraft/client/Minecraft;)V"))
    private void onInit(Minecraft minecraft, CallbackInfo ci) {
        HudManager.registerElements();
    }

    @WrapOperation(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;clearMessages(Z)V"))
    private void onClearChat(ChatComponent instance, boolean history, Operation<Void> original) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.keepHistory.value()) {
            return;
        }
        original.call(instance, history);
    }
}
