package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.ChatComponent;
import nofrills.features.general.ChatTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @ModifyExpressionValue(method = "addMessageToQueue", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int getLimit(int original) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.extraLines.value()) {
            return ChatTweaks.lines.value();
        }
        return original;
    }

    @ModifyExpressionValue(method = "addMessageToDisplayQueue", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int getLimitVisible(int original) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.extraLines.value()) {
            return ChatTweaks.lines.value();
        }
        return original;
    }
}