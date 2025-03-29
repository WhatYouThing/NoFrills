package nofrills.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import nofrills.events.HudRenderEvent;
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
    public void nofrills_mod$setRenderTitle(String title, int stayTicks, int yOffset, float scale, int color) {
        titleString = title;
        titleTicks = stayTicks;
        titleOffset = yOffset;
        titleScale = scale;
        titleColor = color;
    }

    @Override
    public boolean nofrills_mod$isRenderingTitle() {
        return titleTicks > 0;
    }

    @Inject(method = "renderTitleAndSubtitle", at = @At("HEAD"))
    private void onRenderTitle(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (titleTicks > 0) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate((float) (context.getScaledWindowWidth() / 2), (float) (context.getScaledWindowHeight() / 2), 0.0F);
            matrices.push();
            matrices.scale(titleScale, titleScale, titleScale);
            TextRenderer textRenderer = mc.inGameHud.getTextRenderer();
            Text title = Text.of(titleString);
            int width = textRenderer.getWidth(title);
            context.drawTextWithBackground(textRenderer, title, -width / 2, titleOffset, width, titleColor);
            matrices.pop();
            matrices.pop();
        }
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void onTickHud(CallbackInfo ci) {
        if (titleTicks > 0) {
            titleTicks--;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        eventBus.post(new HudRenderEvent(context, this.getTextRenderer(), tickCounter));
    }
}
