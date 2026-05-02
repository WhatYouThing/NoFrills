package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.features.general.ChatTweaks;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow
    @Final
    public List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    @Final
    private List<ChatHudLine> messages;

    @Shadow
    protected abstract int getWidth();

    @Shadow
    protected abstract double getChatScale();

    @ModifyExpressionValue(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int getLimit(int original) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.extraLines.value()) {
            return ChatTweaks.lines.value();
        }
        return original;
    }

    @ModifyExpressionValue(method = "addVisibleMessage", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int getLimitVisible(int original) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.extraLines.value()) {
            return ChatTweaks.lines.value();
        }
        return original;
    }

    @WrapOperation(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"))
    private void onAddVisibleMessage(ChatHud instance, ChatHudLine message, Operation<Void> original) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.compactChat.value()) {
            String string = message.content().getString().trim();
            if (string.isEmpty() || Pattern.matches("-*", string)) {
                original.call(instance, message);
                return;
            }
            List<ChatHudLine> matching = new ArrayList<>();
            for (ChatHudLine msg : this.messages) {
                if (msg.content().copy().equals(message.content().copy())) {
                    matching.add(msg);
                }
            }
            if (!matching.isEmpty()) {
                MutableText count = Text.literal(Utils.format(" §r({})", matching.size() + 1)).withColor(0x5ca0bf);
                ChatHudLine withCount = new ChatHudLine(
                        message.creationTick(),
                        message.content().copy().append(count),
                        message.signature(),
                        message.indicator()
                );
                this.visibleMessages.removeIf(visible -> matching.stream().anyMatch(match -> visible.addedTime() == match.creationTick()));
                original.call(instance, withCount);
                return;
            }
        }
        original.call(instance, message);
    }
}