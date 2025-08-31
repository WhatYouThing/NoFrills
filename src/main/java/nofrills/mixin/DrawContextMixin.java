package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import nofrills.features.misc.CommandTooltip;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @ModifyExpressionValue(method = "drawHoverEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/HoverEvent$ShowText;value()Lnet/minecraft/text/Text;"))
    private Text getHoveredText(Text original, @Local(argsOnly = true) Style style) {
        if (CommandTooltip.instance.isActive() && style.getClickEvent() instanceof ClickEvent.RunCommand runCommand) {
            return original.copy().append("\n\n").append(Utils.getShortTag().append(Utils.format("§7Command: {}", runCommand.command())));
        }
        return original;
    }
}
