package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import nofrills.features.general.ChatTweaks;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

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

    @WrapOperation(method = "addVisibleMessage", at = @At(value = "NEW", target = "(ILnet/minecraft/text/OrderedText;Lnet/minecraft/client/gui/hud/MessageIndicator;Z)Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;"))
    private ChatHudLine.Visible onAddVisibleMessage(int addedTime, OrderedText content, MessageIndicator indicator, boolean endOfEntry, Operation<ChatHudLine.Visible> original, @Local(argsOnly = true) ChatHudLine message) {
        if (ChatTweaks.instance.isActive() && ChatTweaks.compactChat.value() && endOfEntry) {
            String string = message.content().getString().trim();
            if (string.isEmpty() || Pattern.matches("-*", string)) {
                return original.call(addedTime, content, indicator, true);
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
                OrderedText line = withCount.breakLines(mc.textRenderer, MathHelper.floor(this.getWidth() / this.getChatScale())).getLast();
                this.visibleMessages.removeIf(visible -> matching.stream().anyMatch(match -> visible.addedTime() == match.creationTick()));
                return original.call(addedTime, line, indicator, true);
            }
        }
        return original.call(addedTime, content, indicator, endOfEntry);
    }
}