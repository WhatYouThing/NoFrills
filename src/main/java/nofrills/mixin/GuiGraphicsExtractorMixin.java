package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import nofrills.features.misc.CommandTooltip;
import nofrills.features.misc.TooltipScale;
import nofrills.misc.Utils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Shadow
    public abstract int guiWidth();

    @Shadow
    public abstract int guiHeight();

    @ModifyExpressionValue(method = "componentHoverEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/HoverEvent$ShowText;value()Lnet/minecraft/network/chat/Component;"))
    private Component getHoveredText(Component original, @Local(argsOnly = true) Style style) {
        if (CommandTooltip.instance.isActive() && style.getClickEvent() instanceof ClickEvent.RunCommand runCommand) {
            return original.copy().append("\n\n").append(Utils.getShortTag().append(Utils.format("§7Command: {}", runCommand.command())));
        }
        return original;
    }

    @Inject(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;extractTooltipBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/resources/Identifier;)V"))
    private void beforeDrawTooltip(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier texture, CallbackInfo ci, @Local Vector2ic pos, @Local(ordinal = 2) int width, @Local(ordinal = 3) int height) {
        if (TooltipScale.instance.isActive()) {
            if (TooltipScale.isDynamic()) {
                int screenX = this.guiWidth();
                int screenY = this.guiHeight();
                float scaleX = Math.min((float) screenX / (width + 8), 1.0f);
                float scaleY = Math.min((float) screenY / (height + 8), 1.0f);
                float scale = Math.min(scaleX, scaleY);
                float offsetY = y + (height * scale - y);
                this.pose.translate(x - x * scale, offsetY - offsetY * scale);
                this.pose.scale(scale, scale);
            } else if (TooltipScale.isCustom()) {
                float scale = (float) TooltipScale.scale.value();
                this.pose.translate(x - x * scale, y - y * scale);
                this.pose.scale(scale, scale);
            }
        }
    }
}
