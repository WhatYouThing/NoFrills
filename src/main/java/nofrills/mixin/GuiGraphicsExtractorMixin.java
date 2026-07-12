package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.features.misc.CommandTooltip;
import nofrills.features.misc.TooltipScale;
import nofrills.features.tweaks.LegacyTextures;
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
        if (CommandTooltip.instance.isActive() && style.getClickEvent() instanceof ClickEvent.RunCommand(
                String command
        )) {
            return original.copy().append("\n\n").append(Utils.getShortTag().append(Utils.format("§7Command: {}", command)));
        }
        return original;
    }

    @Inject(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;extractTooltipBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/resources/Identifier;)V"))
    private void beforeDrawTooltip(Font font, List<ClientTooltipComponent> lines, int xo, int yo, ClientTooltipPositioner positioner, @Nullable Identifier style, CallbackInfo ci, @Local(name = "textWidth") int textWidth, @Local(name = "tempHeight") int tempHeight) {
        if (TooltipScale.instance.isActive()) {
            if (TooltipScale.isDynamic()) {
                int screenX = this.guiWidth();
                int screenY = this.guiHeight();
                float scaleX = Math.min((float) screenX / (textWidth + 8), 1.0f);
                float scaleY = Math.min((float) screenY / (tempHeight + 8), 1.0f);
                float scale = Math.min(scaleX, scaleY);
                float offsetY = yo + (tempHeight * scale - yo);
                this.pose.translate(xo - xo * scale, offsetY - offsetY * scale);
                this.pose.scale(scale, scale);
            } else if (TooltipScale.isCustom()) {
                float scale = (float) TooltipScale.scale.value();
                this.pose.translate(xo - xo * scale, yo - yo * scale);
                this.pose.scale(scale, scale);
            }
        }
    }

    @WrapOperation(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;extractTooltipBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/resources/Identifier;)V"))
    private void onExtractBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h, Identifier style, Operation<Void> original) {
        if (LegacyTextures.instance.isActive() && LegacyTextures.noTooltipStyle.value()) {
            original.call(graphics, x, y, w, h, null);
        } else {
            original.call(graphics, x, y, w, h, style);
        }
    }

    @Inject(method = "itemCooldown", at = @At("HEAD"), cancellable = true)
    private void onBeforeDrawCooldown(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        if (LegacyTextures.instance.isActive() && LegacyTextures.noBowCooldown.value() && itemStack.is(Items.BOW)) {
            ci.cancel();
        }
    }
}
