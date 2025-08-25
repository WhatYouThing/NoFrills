package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nofrills.features.misc.CommandTooltip;
import nofrills.features.misc.TooltipScale;
import nofrills.misc.Utils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow
    @Final
    private Matrix3x2fStack matrices;

    @ModifyExpressionValue(method = "drawHoverEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/HoverEvent$ShowText;value()Lnet/minecraft/text/Text;"))
    private Text getHoveredText(Text original, @Local(argsOnly = true) Style style) {
        if (CommandTooltip.instance.isActive() && style.getClickEvent() instanceof ClickEvent.RunCommand runCommand) {
            MutableText tag = Text.literal("[NF] ").withColor(0x5ca0bf);
            return original.copy().append("\n\n").append(tag.append(Utils.format("§7Command: {}", runCommand.command())));
        }
        return original;
    }

    @Inject(method = "drawTooltipImmediately", at = @At("HEAD"))
    private void beforeDrawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture, CallbackInfo ci) {
        if (TooltipScale.instance.isActive()) {
            this.matrices.pushMatrix();
            float scale = (float) TooltipScale.scale.value();
            this.matrices.translate(x - x * scale, y - y * scale);
            this.matrices.scale(scale, scale);
        }
    }

    @Inject(method = "drawTooltipImmediately", at = @At("TAIL"))
    private void afterDrawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture, CallbackInfo ci) {
        if (TooltipScale.instance.isActive()) {
            this.matrices.popMatrix();
        }
    }
}
