package nofrills.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import nofrills.events.HudRenderEvent;
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

@Mixin(InGameHud.class)
public abstract class InGameHudMixin implements TitleRendering {
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
    public abstract TextRenderer getTextRenderer();

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

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!mc.options.hudHidden) {
            eventBus.post(new HudRenderEvent(context, this.getTextRenderer(), tickCounter));
            if (titleTicks > 0) {
                context.getMatrices().pushMatrix();
                context.getMatrices().translate((float) (context.getScaledWindowWidth() / 2), (float) (context.getScaledWindowHeight() / 2));
                context.getMatrices().scale(titleScale, titleScale);
                TextRenderer textRenderer = mc.inGameHud.getTextRenderer();
                Text title = Text.of(titleString);
                int width = textRenderer.getWidth(title);
                context.drawTextWithBackground(textRenderer, title, -width / 2, titleOffset, width, titleColor);
                context.getMatrices().popMatrix();
            }
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.effectDisplay.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderNausea(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.nausea.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.vignette.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;<init>(Lnet/minecraft/client/MinecraftClient;)V"))
    private void onInit(MinecraftClient client, CallbackInfo ci) {
        HudManager.registerElements();
    }
}
