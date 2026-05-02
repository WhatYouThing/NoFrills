package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import nofrills.events.HudRenderEvent;
import nofrills.features.general.ChatTweaks;
import nofrills.features.general.NoRender;
import nofrills.features.misc.StreamerMode;
import nofrills.hud.HudManager;
import nofrills.misc.Utils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void onRenderArmorBar(DrawContext context, PlayerEntity player, int y, int i, int healthBarLines, int x, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.armorBar.value()) {
            ci.cancel();
        }
    }

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!mc.options.hudHidden) {
            eventBus.post(new HudRenderEvent(context, this.getTextRenderer(), tickCounter));
        }
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void onRenderFoodBar(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.foodBar.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.effectDisplay.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void onRenderSelectedItemName(DrawContext context, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.selectedItemName.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderNausea(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIIII)V"), cancellable = true)
    private void onRenderVignette(DrawContext context, Entity entity, CallbackInfo ci, @Local(ordinal = 0) float f) {
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

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;<init>(Lnet/minecraft/client/MinecraftClient;)V"))
    private void onInit(MinecraftClient client, CallbackInfo ci) {
        HudManager.registerElements();
    }

    @WrapOperation(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"))
    private void onClearChat(ChatHud instance, boolean clearHistory, Operation<Void> original) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.keepHistory.value()) {
            return;
        }
        original.call(instance, clearHistory);
    }

    @ModifyExpressionValue(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud$SidebarEntry;name:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD))
    private Text onGetEntryName(Text original) {
        if (StreamerMode.isActive()) {
            Optional<String> replacement = StreamerMode.replaceIfNeeded(Utils.toPlain(original));
            if (replacement.isPresent()) {
                String string = replacement.get().trim();
                return Text.literal("§7" + string.replace(" ", " §8"));
            }
        }
        return original;
    }
}
